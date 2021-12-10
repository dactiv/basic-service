package com.github.dactiv.basic.socket.server.receiver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.socket.server.domain.body.request.ReadMessageRequestBody;
import com.github.dactiv.basic.socket.server.service.chat.ChatService;
import com.github.dactiv.basic.socket.server.domain.model.BasicMessageModel;
import com.github.dactiv.basic.socket.server.domain.ContactMessage;
import com.github.dactiv.framework.minio.data.FileObject;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private final ChatService chatService;

    public ReadMessageReceiver(ChatService chatService) {
        this.chatService = chatService;
    }

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

        readMessage(body);

        channel.basicAck(tag, false);
    }

    private void readMessage(ReadMessageRequestBody body) throws Exception {
        Map<Integer, ContactMessage<BasicMessageModel.UserMessageBody>> map = chatService.getUnreadMessageData(body.getRecipientId());

        if (MapUtils.isEmpty(map)) {
            return ;
        }

        ContactMessage<BasicMessageModel.UserMessageBody> message = map.get(body.getSenderId());

        if (Objects.isNull(message)) {
            return ;
        }

        List<BasicMessageModel.UserMessageBody> userMessageBodies = message
                .getMessages()
                .stream()
                .filter(umb -> body.getMessageIds().contains(umb.getId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userMessageBodies)) {
            return;
        }

        for (BasicMessageModel.UserMessageBody userMessageBody : userMessageBodies) {

            for (String filename : userMessageBody.getFilenames()) {

                FileObject messageFileObject = FileObject.of(
                        chatService.getChatConfig().getMessage().getBucket(),
                        filename
                );

                List<BasicMessageModel.FileMessage> messageList = chatService.getMinioTemplate().readJsonValue(
                        messageFileObject,
                        new TypeReference<>() {
                        }
                );

                Optional<BasicMessageModel.FileMessage> messageOptional = messageList
                        .stream()
                        .filter(m -> m.getId().equals(userMessageBody.getId()))
                        .findFirst();

                if (messageOptional.isPresent()) {
                    BasicMessageModel.FileMessage fileMessage = messageOptional.get();
                    fileMessage.setRead(true);
                    fileMessage.setReadTime(body.getCreationTime());
                }

                chatService.getMinioTemplate().writeJsonValue(messageFileObject, messageList);
            }
        }
        message.getMessages().removeIf(m -> userMessageBodies.stream().anyMatch(umb -> umb.getId().equals(m.getId())));

        if (CollectionUtils.isEmpty(message.getMessages())) {
            map.remove(body.getSenderId());
        }

        String filename = MessageFormat.format(
                chatService.getChatConfig().getContact().getUnreadMessageFileToken(),
                body.getRecipientId()
        );

        FileObject fileObject = FileObject.of(chatService.getChatConfig().getContact().getUnreadBucket(), filename);
        chatService.getMinioTemplate().writeJsonValue(fileObject, map);
    }

}
