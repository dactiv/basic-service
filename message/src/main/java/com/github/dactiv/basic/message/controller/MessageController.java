package com.github.dactiv.basic.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.message.service.MessageSender;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 *
 * @author maurice
 */
@RestController
public class MessageController {

    private static final String DEFAULT_TYPE_PARAM_NAME = "messageType";

    @Autowired
    private List<MessageSender> messageSenders;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送消息
     *
     * @param request http servlet request
     * @return 消息结果集
     */
    @PostMapping("sendMessage")
    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('BASIC')")
    public RestResult<Map<String, Object>> sendMessage(HttpServletRequest request) throws Exception {

        if (MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())) {
            Map<String, Object> parameter = objectMapper.readValue(request.getInputStream(), Map.class);
            String type = parameter.get(DEFAULT_TYPE_PARAM_NAME).toString();
            return getMessageService(type).send(parameter);
        } else {
            String type = request.getParameter(DEFAULT_TYPE_PARAM_NAME);
            Map<String, Object> parameter = new LinkedHashMap<>(request.getParameterMap());
            return getMessageService(type).send(parameter);
        }
    }

    /**
     * 更具类型获取验证码服务
     *
     * @param type 消息类型
     * @return 验证码服务
     */
    private MessageSender getMessageService(String type) {
        return messageSenders
                .stream()
                .filter(c -> c.getMessageType().equals(type))
                .findFirst()
                .orElseThrow(() -> new ServiceException("找不到类型为[ " + type + " ]的消息发送服务"));
    }

}
