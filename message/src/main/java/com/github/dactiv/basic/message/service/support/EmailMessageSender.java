package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.BasicMessage;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.message.service.support.body.EmailMessageBody;
import com.github.dactiv.basic.message.service.support.mail.MailConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Component
@RefreshScope
public class EmailMessageSender extends AbstractMessageSender<EmailMessageBody, EmailMessage> implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageSender.class);

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
    private AttachmentMessageService attachmentMessageService;

    @Autowired
    private MailConfig mailConfig;

    public EmailMessageSender() {
    }

    @Override
    protected String getRetryMessageQueueName() {
        return DEFAULT_QUEUE_NAME;
    }

    /**
     * 发送邮件
     *
     * @param data 邮件实体
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, delayed = "true")
            ),
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void sendEmail(@Payload List<EmailMessage> data,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        channel.basicAck(tag, false);

        data.forEach(this::send);
    }

    private void send(EmailMessage entity) {

        entity.setLastSendTime(new Date());

        JavaMailSender mailSender = mailSenderMap.get(entity.getType());

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(entity.getFromEmail());
            helper.setTo(entity.getToEmail());
            helper.setSubject(entity.getTitle());
            helper.setText(entity.getContent(), true);

            mailSender.send(mimeMessage);

            ExecuteStatus.success(entity,"发送邮件成功");
        } catch (Exception ex) {
            LOGGER.error("发送邮件错误", ex);
            ExecuteStatus.failure(entity,ex.getCause().getMessage());
        }

        retry(entity);

        attachmentMessageService.saveEmailMessage(entity);

        updateBatchMessage(entity);
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<EmailMessage> entities) {

        entities.forEach(e -> attachmentMessageService.saveEmailMessage(e));

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entities);

        Map<String, Object> data = Map.of(
                DEFAULT_MESSAGE_COUNT_KEY, entities.size(),
                IdEntity.ID_FIELD_NAME, entities.stream().map(BasicMessage::getId).collect(Collectors.toList())
        );

        return RestResult.ofSuccess("发送邮件成功", data);
    }

    @Override
    protected List<EmailMessage> createSendEntity(List<EmailMessageBody> result) {
        return result.stream().flatMap(this::createAndSaveEmailMessageEntity).collect(Collectors.toList());
    }

    /**
     * 通过邮件消息 body 构造邮件消息并保存信息
     *
     * @param body 邮件消息 body
     *
     * @return 邮件消息流
     */
    private Stream<EmailMessage> createAndSaveEmailMessageEntity(EmailMessageBody body) {

        List<EmailMessage> result = new LinkedList<>();

        for (String toEmail : body.getToEmails()) {

            EmailMessage entity = Casts.of(body, EmailMessage.class);

            entity.setToEmail(toEmail);
            entity.setMaxRetryCount(maxRetryCount);

            result.add(entity);
        }

        return result.stream();
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
