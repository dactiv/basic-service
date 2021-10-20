package com.github.dactiv.basic.socket.client;

import com.github.dactiv.basic.socket.client.entity.ReturnValueSocketResult;
import com.github.dactiv.basic.socket.client.entity.SocketResult;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.spring.web.SpringWebMvcProperties;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * socket 结果集响应处理器
 *
 * @author maurice
 */
@Setter
@ControllerAdvice
@EqualsAndHashCode(callSuper = true)
public class SocketResultResponseBodyAdvice extends RestResponseBodyAdvice {

    @NonNull
    private SocketClientTemplate socketClientTemplate;

    public SocketResultResponseBodyAdvice(SpringWebMvcProperties properties,
                                          @NonNull SocketClientTemplate socketClientTemplate) {
        super(properties);
        this.socketClientTemplate = socketClientTemplate;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        Object returnValue = body;

        if (Objects.nonNull(body) && SocketResult.class.isAssignableFrom(body.getClass())) {

            SocketResult result = Casts.cast(body);
            List<String> filterResultIds = new LinkedList<>(FilterResultHolder.get());
            socketClientTemplate.asyncSendSocketResult(result, filterResultIds);

            if (ReturnValueSocketResult.class.isAssignableFrom(result.getClass())) {

                ReturnValueSocketResult<?> returnValueSocketResult = Casts.cast(result);

                returnValue = returnValueSocketResult.getReturnValue();
            }
        }

        return super.beforeBodyWrite(returnValue, returnType, selectedContentType, selectedConverterType, request, response);
    }

}
