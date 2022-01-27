package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.commons.SystemConstants;
import com.github.dactiv.basic.message.config.MailConfig;
import com.github.dactiv.basic.message.domain.body.EmailMessageBody;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.basic.message.domain.entity.EmailMessageEntity;
import com.github.dactiv.basic.message.service.EmailMessageService;
import com.github.dactiv.basic.message.service.basic.BatchMessageSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.idempotent.ConcurrentProperties;
import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.minio.data.Bucket;
import com.github.dactiv.framework.minio.data.FileObject;
import com.rabbitmq.client.Channel;
import io.minio.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Slf4j
@Component
@RefreshScope
public class EmailMessageSender extends BatchMessageSender<EmailMessageBody, EmailMessageEntity> implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "message.email.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "email";

    private final Map<String, JavaMailSenderImpl> mailSenderMap = new LinkedHashMap<>();

    private final AmqpTemplate amqpTemplate;

    private final EmailMessageService emailMessageService;

    private final ConcurrentInterceptor concurrentInterceptor;

    private final MailConfig config;

    public EmailMessageSender(AmqpTemplate amqpTemplate,
                              EmailMessageService emailMessageService,
                              ConcurrentInterceptor concurrentInterceptor,
                              MailConfig config) {
        this.amqpTemplate = amqpTemplate;
        this.emailMessageService = emailMessageService;
        this.concurrentInterceptor = concurrentInterceptor;
        this.config = config;
    }

    @Override
    protected RestResult<Object> send(List<EmailMessageEntity> entities) {
        entities
                .stream()
                .map(BasicMessageEntity::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条邮件消息完成",
                entities.stream().map(BasicMessageEntity::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<EmailMessageEntity> getBatchMessageBodyContent(List<EmailMessageBody> result) {
        return result.stream().flatMap(this::createEmailMessageEntity).collect(Collectors.toList());
    }

    /**
     * 发送邮件
     *
     * @param id      邮件实体 id
     * @param channel 频道信息
     * @param tag     ack 值
     *
     * @throws Exception 发送失败或确认 ack 错误时抛出。
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_MESSAGE_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void sendEmail(@Payload Integer id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        EmailMessageEntity entity = sendEmail(id);

        if (ExecuteStatus.Retrying.equals(entity.getExecuteStatus()) && entity.getRetryCount() < getMaxRetryCount()) {
            throw new SystemException(entity.getException());
        }

        channel.basicAck(tag, false);
    }

    /**
     * 发送邮件
     *
     * @param id 邮件实体 id
     */
    @Transactional(rollbackFor = Exception.class)
    public EmailMessageEntity sendEmail(Integer id) {

        EmailMessageEntity entity = emailMessageService.get(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        entity.setLastSendTime(new Date());
        entity.setRetryCount(entity.getRetryCount() + 1);

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType().toString());

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(entity.getFromEmail());
            helper.setTo(entity.getToEmail());
            helper.setSubject(entity.getTitle());
            helper.setText(entity.getContent(), true);

            if (YesOrNo.Yes.equals(entity.getHasAttachment())) {

                for (AttachmentEntity a : entity.getAttachmentList()) {

                    InputStreamSource iss;

                    if (Objects.nonNull(entity.getBatchId())) {
                        byte[] bytes = attachmentCache.get(entity.getBatchId()).get(a.getName());
                        iss = new ByteArrayResource(bytes);
                    } else {
                        FileObject fileObject = FileObject.of(
                                attachmentConfig.getBucketName(DEFAULT_TYPE),
                                a.getName()
                        );
                        GetObjectResponse response = minioTemplate.getObject(fileObject);

                        iss = new ByteArrayResource(IOUtils.toByteArray(response));
                    }

                    helper.addAttachment(a.getName(), iss);
                }

            }

            mailSender.send(mimeMessage);

            ExecuteStatus.success(entity);
        } catch (Exception ex) {
            log.error("发送邮件错误", ex);
            ExecuteStatus.failure(entity, ex.getCause().getMessage());
        }

        emailMessageService.save(entity);

        if (Objects.nonNull(entity.getBatchId())) {
            ConcurrentProperties properties = config.getBatchUpdateConcurrent().ofSuffix(entity.getBatchId());
            concurrentInterceptor.invoke(properties, () -> updateBatchMessage(entity));
        }

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<EmailMessageEntity> content) {
        emailMessageService.save(content);
        return true;
    }

    /**
     * 通过邮件消息 body 构造邮件消息并保存信息
     *
     * @param body 邮件消息 body
     *
     * @return 邮件消息流
     */
    private Stream<EmailMessageEntity> createEmailMessageEntity(EmailMessageBody body) {

        List<EmailMessageEntity> result = new LinkedList<>();

        if (body.getToEmails().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[email_nen]", "true");
            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationFeignClient.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                EmailMessageEntity entity = ofEntity(body);
                entity.setToEmail(user.get("email").toString());

                result.add(entity);
            }

        } else {

            for (String toEmail : body.getToEmails()) {

                EmailMessageEntity entity = ofEntity(body);
                entity.setToEmail(toEmail);

                result.add(entity);
            }
        }

        return result.stream();
    }

    /**
     * 创建邮件消息实体
     *
     * @param body 邮件消息 body
     *
     * @return 邮件消息实体
     */
    private EmailMessageEntity ofEntity(EmailMessageBody body) {
        EmailMessageEntity entity = Casts.of(body, EmailMessageEntity.class, "attachmentList");

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType().toString());

        if (Objects.isNull(mailSender)) {
            throw new SystemException("找不到类型为 [" + entity.getType() + "] 的邮件发送者");
        }

        entity.setFromEmail(mailSender.getUsername());

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setHasAttachment(YesOrNo.Yes);
            body.getAttachmentList().forEach(a -> entity.getAttachmentList().add(Casts.of(a, AttachmentEntity.class)));
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        config.getAccounts().entrySet().forEach(this::generateMailSender);
        minioTemplate.makeBucketIfNotExists(Bucket.of(attachmentConfig.getBucketName(getMessageType())));
    }

    /**
     * 生成邮件发送者
     *
     * @param entry 账户配置信息
     */
    private void generateMailSender(Map.Entry<String, MailProperties> entry) {

        MailProperties mailProperties = entry.getValue();

        JavaMailSenderImpl mailSender = mailSenderMap.computeIfAbsent(
                StringUtils.capitalize(entry.getKey()),
                k -> new JavaMailSenderImpl()
        );

        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        if (MapUtils.isNotEmpty(mailProperties.getProperties())) {
            mailSender.getJavaMailProperties().putAll(mailProperties.getProperties());
        }

        if (MapUtils.isEmpty(mailSender.getJavaMailProperties()) && MapUtils.isNotEmpty(config.getProperties())) {
            mailSender.getJavaMailProperties().putAll(config.getProperties());
        }

        mailSender.setHost(StringUtils.defaultIfEmpty(mailProperties.getHost(), config.getHost()));
        mailSender.setPort(Objects.nonNull(mailProperties.getPort()) ? mailProperties.getPort() : config.getPort());
        mailSender.setProtocol(StringUtils.defaultIfEmpty(mailProperties.getProtocol(), config.getProtocol()));

        Charset encoding = Objects.nonNull(mailProperties.getDefaultEncoding()) ? mailProperties.getDefaultEncoding() : config.getDefaultEncoding();
        if (Objects.nonNull(encoding)) {
            mailSender.setDefaultEncoding(encoding.toString());
        }

        String jndiName = StringUtils.defaultIfEmpty(mailProperties.getJndiName(), config.getJndiName());

        if (StringUtils.isNotBlank(jndiName)) {
            try {
                Session session = JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(jndiName, Session.class);
                mailSender.setSession(session);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Unable to find Session in JNDI location %s", jndiName), e);
            }
        }

    }
}
