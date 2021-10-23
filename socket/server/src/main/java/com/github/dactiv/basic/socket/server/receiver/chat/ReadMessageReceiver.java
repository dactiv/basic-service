package com.github.dactiv.basic.socket.server.receiver.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.controller.chat.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.basic.socket.server.service.chat.data.BasicMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.ContactMessage;
import com.github.dactiv.basic.socket.server.service.chat.data.GlobalMessage;
import com.github.dactiv.framework.minio.data.FileObject;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 读取消息 MQ 接收者
 *
 * @author maurice.chen
 */
@Component
public class ReadMessageReceiver {

    /**
     * MQ 队列名称
     */
    public static final String DEFAULT_QUEUE_NAME = "read.chat.message";

    @Autowired
    private ChatService chatService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void onMessage(@Payload ReadMessageRequestBody body,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {

        Map<Integer, ContactMessage<BasicMessage.UserMessageBody>> map = chatService.getUnreadMessageData(body.getRecipientId());

        ContactMessage<BasicMessage.UserMessageBody> message = map.get(body.getSenderId());

        if (Objects.nonNull(message) && CollectionUtils.isNotEmpty(message.getMessages())) {
            readMessage(body.getMessageIds(), message.getMessages(), body.getCreationTime());
        }

        channel.basicAck(tag, false);
    }

    private void readMessage(List<String> ids, List<BasicMessage.UserMessageBody> messages, Date readTime) throws Exception {
        List<BasicMessage.UserMessageBody> userMessageBodies = messages
                .stream()
                .filter(umb -> ids.contains(umb.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userMessageBodies)) {
            return;
        }

        for (BasicMessage.UserMessageBody userMessageBody : userMessageBodies) {

            for (String filename : userMessageBody.getFilenames()) {

                FileObject messageFileObject = FileObject.of(
                        chatService.getChatConfig().getMessage().getBucket(),
                        filename
                );

                List<BasicMessage.FileMessage> messageList = chatService.getMinioTemplate().readJsonValue(
                        messageFileObject,
                        new TypeReference<>() {
                        }
                );

                Optional<BasicMessage.FileMessage> messageOptional = messageList
                        .stream()
                        .filter(m -> m.getId().equals(userMessageBody.getId()))
                        .findFirst();

                if (messageOptional.isPresent()) {
                    BasicMessage.FileMessage fileMessage = messageOptional.get();
                    fileMessage.setRead(true);
                    fileMessage.setReadTime(readTime);
                }

                chatService.getMinioTemplate().writeJsonValue(messageFileObject, messageList);
            }
        }
    }
}
