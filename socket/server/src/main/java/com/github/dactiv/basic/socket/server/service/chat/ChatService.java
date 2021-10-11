package com.github.dactiv.basic.socket.server.service.chat;

import com.github.dactiv.basic.commons.feign.authentication.AuthenticationService;
import com.github.dactiv.basic.socket.client.entity.SocketUserDetails;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.basic.socket.server.config.SocketServerProperties;
import com.github.dactiv.basic.socket.server.enitty.Contact;
import com.github.dactiv.basic.socket.server.enitty.Room;
import com.github.dactiv.basic.socket.server.receiver.SaveChatMessageReceiver;
import com.github.dactiv.basic.socket.server.service.SocketServerManager;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.github.dactiv.basic.commons.Constants.SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE;

/**
 * 聊天业务逻辑服务
 *
 * @author maurice.chen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ChatService {

    @Autowired
    private SocketServerProperties properties;

    @Autowired
    private SocketServerManager socketServerManager;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送消息
     *
     * @param sender 发送人用户
     * @param recipientId 收信人用户 id
     * @param content 消息内容
     */
    @SocketMessage
    public Contact.Message sendMessage(SecurityUserDetails sender, Integer recipientId, String content) {
        Integer senderId = Casts.cast(sender.getId());

        Contact.Message message = new Contact.Message();

        message.setContent(content);
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setCryptoType(properties.getChat().getCryptoType());
        message.setCryptoKey(properties.getChat().getCryptoKey());

        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setId(senderId);
        contactMessage.setRecipientId(recipientId);
        contactMessage.setLastSendTime(new Date());
        contactMessage.setLastMessage(RegExUtils.replaceAll(message.getContent(),"<[.[^<]]*>", ""));

        contactMessage.getMessages().add(message);

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(recipientId);

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    Room.CHAT_MESSAGE_EVENT_NAME,
                    contactMessage
            );
        }

        amqpTemplate.convertAndSend(
                SYS_SOCKET_SERVER_RABBITMQ_EXCHANGE,
                SaveChatMessageReceiver.DEFAULT_QUEUE_NAME,
                contactMessage

        );

        return message;

        //SocketResultHolder.get().addUnicastMessage(recipientId);
    }

    /**
     * 读取信息
     *
     * @param senderId 发送人用户 id
     * @param recipientId 收信人用户 id
     * @param messageIds 读取的消息 id 集合
     */
    @SocketMessage
    public void readMessage(Integer senderId, Integer recipientId, List<String> messageIds) {

        SocketUserDetails userDetails = socketServerManager.getSocketUserDetails(senderId);

        if (Objects.nonNull(userDetails) && ConnectStatus.Connect.getValue().equals(userDetails.getConnectStatus())) {

            SocketResultHolder.get().addUnicastMessage(
                    userDetails.getDeviceIdentified(),
                    Room.CHAT_READ_MESSAGE_EVENT_NAME,
                    Map.of(IdEntity.ID_FIELD_NAME, recipientId, Contact.DEFAULT_MESSAGE_IDS, messageIds)
            );
        }
    }
}
