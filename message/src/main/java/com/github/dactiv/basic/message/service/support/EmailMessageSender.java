package com.github.dactiv.basic.message.service.support;

import com.github.dactiv.basic.message.RabbitmqConfig;
import com.github.dactiv.basic.message.entity.EmailMessage;
import com.github.dactiv.basic.message.service.AbstractMessageSender;
import com.github.dactiv.basic.message.service.AttachmentMessageService;
import com.github.dactiv.basic.message.service.support.body.EmailMessageBody;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Component
public class EmailMessageSender extends AbstractMessageSender<EmailMessageBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageSender.class);

    public static final String DEFAULT_QUEUE_NAME = "message.email.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "email";

    @Autowired
    private AttachmentMessageService attachmentMessageService;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 最大重试次数
     */
    @Value("${spring.mail.max-retry-count:3}")
    private Integer maxRetryCount;

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
    @Transactional(rollbackFor = Exception.class)
    protected RestResult<Map<String, Object>> send(List<EmailMessageBody> entities) {

        List<EmailMessage> messages = entities
                .stream()
                .flatMap(this::createAndSaveEmailMessageEntity)
                .collect(Collectors.toList());

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, messages);

        return RestResult.ofSuccess("发送邮件成功", Map.of(DEFAULT_MESSAGE_COUNT_KEY, messages.size()));
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

            attachmentMessageService.saveEmailMessage(entity);

            result.add(entity);
        }

        return result.stream();
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
