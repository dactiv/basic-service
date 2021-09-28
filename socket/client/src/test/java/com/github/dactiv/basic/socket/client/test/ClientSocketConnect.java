package com.github.dactiv.basic.socket.client.test;


import com.alibaba.nacos.api.common.Constants;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Map;


/**
 * 客户端链接 socket 服务
 *
 * @author maurice.chen
 */
@Slf4j
public class ClientSocketConnect {

    public static void main(String[] args) {

        RestTemplate restTemplate = new RestTemplate();

        String deviceId = "0163ec02-f5ba-4290-bc2b-7e73624d8a23";
        String username = "admin";
        String password = "123456";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY, username);
        body.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY, password);

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(AuthenticationProperties.SECURITY_FORM_TYPE_HEADER_NAME, ResourceSource.Console.toString());
        httpHeaders.add(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME, deviceId);
        httpHeaders.add(RestResponseBodyAdvice.DEFAULT_NOT_FORMAT_ATTR_NAME, "true");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, httpHeaders);

        //noinspection unchecked
        Map<String, Object> result = restTemplate.postForObject("http://localhost:8080/authentication/login", entity, Map.class);

        if (MapUtils.isEmpty(result) || !result.containsKey(RestResult.DEFAULT_DATA_NAME)) {
            throw new SystemException("登陆失败");
        }

        //noinspection unchecked
        Map<String, Object> data = Casts.cast(result.get(RestResult.DEFAULT_DATA_NAME), Map.class);

        MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
        queryMap.add("did", deviceId);
        queryMap.add("uid", data.get(IdEntity.ID_FIELD_NAME).toString());
        queryMap.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY,username);
        queryMap.add(RequestAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY, data.get(Constants.TOKEN).toString());

        Thread thread = new Thread(() -> {

            IO.Options options = new IO.Options();
            options.query = Casts.castRequestBodyMapToString(queryMap);
            options.transports = new String[]{WebSocket.NAME};
            options.port = 8800;

            try {
                Socket socket = IO.socket("http://localhost:8080", options);

                socket.on(Socket.EVENT_CONNECT, argv -> log.info("连接成功"));
                socket.on(Socket.EVENT_DISCONNECT, argv -> log.info("连接断开"));

                socket.open();
            } catch (URISyntaxException e) {
                log.error("链接 socket 失败", e);
            }

        });

        thread.start();
    }
}
