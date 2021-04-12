package com.fuyu.basic.authentication.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuyu.basic.commons.spring.web.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * json 形式的 session 超时策略实现
 *
 * @author maurice
 */
@Component
public class JsonSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

    @Autowired
    private ObjectMapper mapper;

    public JsonSessionInformationExpiredStrategy(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
        HttpServletResponse response = event.getResponse();

        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        RestResult<Map<String, Object>> result = new RestResult<>(
                httpStatus.getReasonPhrase(),
                httpStatus.value(),
                RestResult.ERROR_EXECUTE_CODE,
                new LinkedHashMap<>()
        );

        response.getWriter().write(mapper.writeValueAsString(result));

    }
}
