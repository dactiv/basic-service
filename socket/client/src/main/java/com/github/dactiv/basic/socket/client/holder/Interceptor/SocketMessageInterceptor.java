package com.github.dactiv.basic.socket.client.holder.Interceptor;

import com.github.dactiv.basic.socket.client.SocketClientTemplate;
import com.github.dactiv.basic.socket.client.SocketResultResponseBodyAdvice;
import com.github.dactiv.basic.socket.client.entity.SocketResult;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolder;
import com.github.dactiv.basic.socket.client.holder.annotation.SocketMessage;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import com.github.dactiv.framework.spring.web.result.RestResponseBodyAdvice;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * spring mvc 拦截器的 socket 结果集持有者实现
 *
 * @author maurice.chen
 */
@Slf4j
@Setter
@AllArgsConstructor(staticName = "of")
public class SocketMessageInterceptor implements MethodInterceptor {

    private final SocketClientTemplate socketClientTemplate;

    private final RestResponseBodyAdvice responseBodyAdvice;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        SocketMessage message = AnnotationUtils.findAnnotation(invocation.getMethod(), SocketMessage.class);

        if (Objects.isNull(message)) {
            return invocation.proceed();
        }

        try {

            Object returnValue = invocation.proceed();

            SocketResult socketResult = SocketResultHolder.get();

            List<String> filterResultIds = new LinkedList<>(FilterResultHolder.get());

            Optional<HttpServletRequest> optional = SpringMvcUtils.getHttpServletRequest();

            if (optional.isPresent()) {
                HttpServletRequest httpServletRequest = optional.get();
                String id = responseBodyAdvice.getFilterResultId(httpServletRequest);
                if (StringUtils.isNotBlank(id) && !filterResultIds.contains(id)) {
                    filterResultIds.add(id);
                }
            }

            if (StringUtils.isNotBlank(message.value())) {
                String id = message.value();
                boolean ignoreOtherIds = message.ignoreOtherIds();

                if (!ignoreOtherIds) {
                    filterResultIds = Collections.singletonList(id);
                } else {
                    filterResultIds.add(id);
                }
            }

            socketClientTemplate.asyncSendSocketResult(socketResult, filterResultIds);

            return returnValue;
        } finally {
            SocketResultHolder.clear();
        }
    }
}
