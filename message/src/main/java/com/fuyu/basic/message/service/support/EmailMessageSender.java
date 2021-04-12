package com.fuyu.basic.message.service.support;

import com.fuyu.basic.commons.enumerate.support.ExecuteStatus;
import com.fuyu.basic.commons.spring.web.RestResult;
import com.fuyu.basic.message.RabbitmqConfig;
import com.fuyu.basic.message.dao.entity.EmailMessage;
import com.fuyu.basic.message.dao.entity.SiteMessage;
import com.fuyu.basic.message.service.AbstractMessageSender;
import com.fuyu.basic.message.service.MessageService;
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

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 邮件消息发送者实现
 *
 * @author maurice
 */
@Component
public class EmailMessageSender extends AbstractMessageSender<EmailMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageSender.class);

    public static final String DEFAULT_QUEUE_NAME = "message.email.queue";

    /**
     * 默认的消息类型
     */
    private static final String DEFAULT_TYPE = "email";

    @Autowired
    private MessageService messageService;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送邮件用户名
     */
    @Value("${spring.mail.username}")
    private String sendMailUsername;

    /**
     * 最大重试次数
     */
    @Value("${spring.mail.max-retry-count:3}")
    private Integer maxRetryCount;

    @Override
    protected void afterBindValueSetting(EmailMessage entity, Map<String, Object> value) {
        entity.setMaxRetryCount(maxRetryCount);
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

        data.forEach(entity -> {

            entity.setLastSendTime(new Date());

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            try {

                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

                helper.setFrom(entity.getFromUser());
                helper.setTo(entity.getToUser());
                helper.setSubject(entity.getTitle());
                helper.setText(entity.getContent(), true);

                mailSender.send(mimeMessage);

                entity.setRemark("发送邮件成功");
                entity.setSuccessTime(new Date());
                entity.setStatus(ExecuteStatus.Success.getValue());
            } catch (Exception ex) {
                LOGGER.error("发送邮件错误", ex);
                entity.setStatus(ExecuteStatus.Failure.getValue());
                entity.setRemark(ex.getCause().getMessage());
            }

            if (ExecuteStatus.Failure.getValue().equals(entity.getStatus()) && entity.isRetry()) {

                entity.setRetryCount(entity.getRetryCount() + 1);
                messageService.saveEmailMessage(entity);

                amqpTemplate.convertAndSend(
                        RabbitmqConfig.DEFAULT_DELAY_EXCHANGE,
                        DEFAULT_QUEUE_NAME,
                        Collections.singletonList(entity),
                        message -> {
                            message.getMessageProperties().setDelay(entity.getNextIntervalTime());
                            return message;
                        });

            } else {

                messageService.saveEmailMessage(entity);

            }
        });
    }

    @Override
    protected RestResult<Map<String, Object>> send(List<EmailMessage> entity) {

        entity.forEach(x -> x.setFromUser(sendMailUsername));

        messageService.saveEmailMessages(entity);

        amqpTemplate.convertAndSend(RabbitmqConfig.DEFAULT_DELAY_EXCHANGE, DEFAULT_QUEUE_NAME, entity);

        return new RestResult<>(
                "发送邮件成功",
                HttpStatus.OK.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                new LinkedHashMap<>()
        );
    }

    @Override
    public String getMessageType() {
        return DEFAULT_TYPE;
    }
}
