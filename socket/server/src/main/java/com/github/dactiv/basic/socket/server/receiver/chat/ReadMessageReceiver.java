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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        GlobalMessage sourceUserMessage = chatService.getGlobalMessage(
                body.getSenderId(),
                body.getRecipientId(),
                false
        );

        GlobalMessage targetUserMessage = chatService.getGlobalMessage(
                body.getRecipientId(),
                body.getSenderId(),
                false
        );

        GlobalMessage globalMessage = chatService.getGlobalMessage(
                body.getSenderId(),
                body.getRecipientId(),
                true
        );

        for (Map.Entry<String, List<String>> entry : body.getMessages().entrySet()) {
            readMessage(sourceUserMessage, entry, body.getCreationTime());
            readMessage(targetUserMessage, entry, body.getCreationTime());
            readMessage(globalMessage, entry, body.getCreationTime());
        }

        Map<Integer, ContactMessage> map = chatService.getUnreadMessageData(body.getRecipientId());

        map.remove(body.getSenderId());

        FileObject fileObject = chatService.getRecentContactFileObject(body.getRecipientId());

        chatService.getMinioTemplate().writeJsonValue(fileObject, map);

        channel.basicAck(tag, false);
    }

    /**
     * 读取消息
     *
     * @param globalMessage 全局消息实体
     * @param entry         消息主键和文件映射的实体
     * @param readTime      读取消息时间
     *
     * @throws Exception 更新失败时抛出
     */
    private void readMessage(GlobalMessage globalMessage,
                             Map.Entry<String, List<String>> entry,
                             Date readTime) throws Exception {

        String filename = "";

        if (entry.getValue().contains(globalMessage.getCurrentMessageFile())) {
            filename = globalMessage.getCurrentMessageFile();
        }

        if (StringUtils.isEmpty(filename)) {

            Optional<String> optional = globalMessage
                    .getMessageFileMap()
                    .keySet()
                    .stream()
                    .filter(s -> entry.getValue().contains(s))
                    .findFirst();

            if (optional.isPresent()) {
                filename = optional.get();
            }
        }

        if (StringUtils.isEmpty(filename)) {
            return;
        }

        FileObject fileObject = FileObject.of(globalMessage.getBucketName(), filename);
        List<BasicMessage.FileMessage> messages = chatService.getMinioTemplate().readJsonValue(
                fileObject,
                new TypeReference<>() {
                }
        );

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        Optional<BasicMessage.FileMessage> optional = messages
                .stream()
                .filter(m -> m.getId().equals(entry.getKey()))
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }

        BasicMessage.FileMessage fileMessage = optional.get();

        fileMessage.setRead(true);
        fileMessage.setReadTime(readTime);

        chatService.getMinioTemplate().writeJsonValue(fileObject, messages);
    }
}
