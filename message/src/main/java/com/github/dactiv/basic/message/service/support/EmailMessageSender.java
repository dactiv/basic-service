package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.Attachment;
import com.github.dactiv.basic.message.entity.BasicMessage;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.commons.feign.file.FileManagerService;
import com.github.dactiv.basic.message.service.basic.BatchMessageSender;
import com.github.dactiv.basic.message.service.support.body.EmailMessageBody;
import com.github.dactiv.basic.message.service.support.mail.MailConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
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
public class EmailMessageSender extends BatchMessageSender<EmailMessageBody, EmailMessage> implements InitializingBean {

    public static final String DEFAULT_QUEUE_NAME = "message.email.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "email";

    private final Map<String, JavaMailSenderImpl> mailSenderMap = new LinkedHashMap<>();

    /**
     * 最大重试次数
     */
    @Value("${message.mail.max-retry-count:3}")
    private Integer maxRetryCount;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private AttachmentMessageService attachmentMessageService;

    @Autowired
    private MailConfig mailConfig;

    public EmailMessageSender() {
    }

    @Override
    protected RestResult<Object> send(List<EmailMessage> entities) {
        entities
                .stream()
                .map(BasicMessage::getId)
                .forEach(id ->
                        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, id));

        return RestResult.ofSuccess(
                "发送 " + entities.size() + " 条邮件消息完成",
                entities.stream().map(BasicMessage::getId).collect(Collectors.toList())
        );
    }

    @Override
    protected List<EmailMessage> getBatchMessageBodyContent(List<EmailMessageBody> result) {
        return result.stream().flatMap(this::createEmailMessageEntity).collect(Collectors.toList());
    }

    /**
     * 发送邮件
     *
     * @param id      邮件实体 id
     * @param channel 频道信息
     * @param tag     ack 值
     * @throws Exception 发送失败或确认 ack 错误时抛出。
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, delayed = "true"),
                    key = DEFAULT_QUEUE_NAME
            ),
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void sendEmail(@Payload Integer id,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        EmailMessage entity = sendEmail(id);

        if (ExecuteStatus.Retrying.getValue().equals(entity.getStatus()) && entity.getRetryCount() < maxRetryCount) {
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
    public EmailMessage sendEmail(Integer id) {

        EmailMessage entity = attachmentMessageService.getEmailMessage(id);

        if (Objects.isNull(entity)) {
            return null;
        }

        entity.setLastSendTime(new Date());
        entity.setRetryCount(entity.getRetryCount() + 1);

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType());

        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(entity.getFromEmail());
            helper.setTo(entity.getToEmail());
            helper.setSubject(entity.getTitle());
            helper.setText(entity.getContent(), true);

            if (YesOrNo.Yes.getValue().equals(entity.getHasAttachment())) {

                for (Attachment a : entity.getAttachmentList()) {

                    InputStreamSource iss;

                    if (Objects.nonNull(entity.getBatchId())) {
                        byte[] bytes = attachmentCache.get(entity.getBatchId()).get(a.getName());
                        iss = new ByteArrayResource(bytes);
                    } else {

                        ResponseEntity<byte[]> response = fileManagerService.get(
                                a.getMeta().get(FileManagerService.DEFAULT_BUCKET_NAME).toString(),
                                a.getName()
                        );

                        iss = new ByteArrayResource(Objects.requireNonNull(response.getBody()));
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

        attachmentMessageService.saveEmailMessage(entity);

        updateBatchMessage(entity);

        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected boolean preSend(List<EmailMessage> content) {
        content.forEach(e -> attachmentMessageService.saveEmailMessage(e));
        return true;
    }

    /**
     * 通过邮件消息 body 构造邮件消息并保存信息
     *
     * @param body 邮件消息 body
     * @return 邮件消息流
     */
    private Stream<EmailMessage> createEmailMessageEntity(EmailMessageBody body) {

        List<EmailMessage> result = new LinkedList<>();

        if (body.getToEmails().contains(DEFAULT_ALL_USER_KEY)) {
            Map<String, Object> filter = new LinkedHashMap<>();

            filter.put("filter_[email_nen]", "true");
            filter.put("filter_[status_eq]", "1");

            List<Map<String, Object>> users = authenticationService.findMemberUser(filter);

            for (Map<String, Object> user : users) {

                EmailMessage entity = ofEntity(body);
                entity.setToEmail(user.get("email").toString());

                result.add(entity);
            }

        } else {

            for (String toEmail : body.getToEmails()) {

                EmailMessage entity = ofEntity(body);
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
     * @return 邮件消息实体
     */
    private EmailMessage ofEntity(EmailMessageBody body) {
        EmailMessage entity = Casts.of(body, EmailMessage.class, "attachmentList");

        JavaMailSenderImpl mailSender = mailSenderMap.get(entity.getType());

        if (Objects.isNull(mailSender)) {
            throw new SystemException("找不到类型为 [" + entity.getType() + "] 的邮件发送者");
        }

        entity.setFromEmail(mailSender.getUsername());
        entity.setMaxRetryCount(maxRetryCount);

        if (CollectionUtils.isNotEmpty(body.getAttachmentList())) {
            entity.setHasAttachment(YesOrNo.Yes.getValue());
            body.getAttachmentList().forEach(a -> entity.getAttachmentList().add(Casts.of(a, Attachment.class)));
        }

        return entity;
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }

    @Override
    public void afterPropertiesSet() {

        mailConfig.getAccounts().entrySet().forEach(this::generateMailSender);

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

        if (MapUtils.isEmpty(mailSender.getJavaMailProperties()) && MapUtils.isNotEmpty(mailConfig.getProperties())) {
            mailSender.getJavaMailProperties().putAll(mailConfig.getProperties());
        }

        mailSender.setHost(StringUtils.defaultIfEmpty(mailProperties.getHost(), mailConfig.getHost()));
        mailSender.setPort(Objects.nonNull(mailProperties.getPort()) ? mailProperties.getPort() : mailConfig.getPort());
        mailSender.setProtocol(StringUtils.defaultIfEmpty(mailProperties.getProtocol(), mailConfig.getProtocol()));

        Charset encoding = Objects.nonNull(mailProperties.getDefaultEncoding()) ? mailProperties.getDefaultEncoding() : mailConfig.getDefaultEncoding();
        if (Objects.nonNull(encoding)) {
            mailSender.setDefaultEncoding(encoding.toString());
        }

        String jndiName = StringUtils.defaultIfEmpty(mailProperties.getJndiName(), mailConfig.getJndiName());

        if (StringUtils.isNotEmpty(jndiName)) {
            try {
                Session session = JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(jndiName, Session.class);
                mailSender.setSession(session);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Unable to find Session in JNDI location %s", jndiName), e);
            }
        }

    }
}
