package com.fuyu.basic.support.spring.web.result;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.exception.ErrorCodeException;
import com.fuyu.basic.commons.exception.ServiceException;
import com.fuyu.basic.commons.spring.web.RestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * rest 格式的全局错误实现
 *
 * @author maurice.chen
 */
public class RestResultErrorAttributes extends DefaultErrorAttributes {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestResultErrorAttributes.class);

    public static final String DEFAULT_ERROR_EXECUTE_ATTR_NAME = "REST_ERROR_ATTRIBUTES_EXECUTE";

    private static final String[] DEFAULT_BINDING_RESULT_IGNORE_FIELD = {
            "rejectedValue",
            "bindingFailure",
            "objectName",
            "source",
            "codes",
            "arguments"
    };

    private static final List<Class<? extends Exception>> DEFAULT_GET_MESSAGE_EXCEPTION = Collections.singletonList(
            ServiceException.class
    );

    private static final List<HttpStatus> DEFAULT_GET_HTTP_STATUSES_MESSAGE = Arrays.asList(
            HttpStatus.FORBIDDEN,
            HttpStatus.UNAUTHORIZED
    );

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        HttpStatus status = getStatus(webRequest);

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        RestResult<Object> result = new RestResult<>(
                "服务器异常，请稍后再试。",
                status.value(),
                RestResult.ERROR_EXECUTE_CODE,
                new LinkedHashMap<>()
        );

        Throwable error = getError(webRequest);

        if (error != null) {

            BindingResult bindingResult = extractBindingResult(error);

            if (bindingResult != null && bindingResult.hasErrors()) {

                List<Map<String, Object>> filedErrorResult = bindingResult.getAllErrors()
                        .stream()
                        .filter(o -> FieldError.class.isAssignableFrom(o.getClass()))
                        .map(o -> Casts.castObjectToMap(o, DEFAULT_BINDING_RESULT_IGNORE_FIELD))
                        .collect(Collectors.toList());

                result.setMessage("参数验证不通过");
                result.setData(filedErrorResult);

            } else if (error instanceof ErrorCodeException) {
                ErrorCodeException errorCodeException = Casts.cast(error, ErrorCodeException.class);

                result.setExecuteCode(errorCodeException.getErrorCode());
                result.setMessage(errorCodeException.getMessage());
            }

            if (DEFAULT_GET_MESSAGE_EXCEPTION.stream().anyMatch(e -> e.isAssignableFrom(error.getClass()))) {
                result.setMessage(error.getMessage());
            }

            LOGGER.error("服务器异常", error);

        }

        if (DEFAULT_GET_HTTP_STATUSES_MESSAGE.contains(status)) {
            result.setMessage(status.getReasonPhrase());
        }

        webRequest.setAttribute(DEFAULT_ERROR_EXECUTE_ATTR_NAME, true, RequestAttributes.SCOPE_REQUEST);

        return Casts.castObjectToMap(result);
    }

    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return Casts.cast(error);
        }
        if (error instanceof MethodArgumentNotValidException) {
            return Casts.cast(error, MethodArgumentNotValidException.class).getBindingResult();
        }
        return null;
    }


    private HttpStatus getStatus(WebRequest webRequest) {

        Integer status = Casts.cast(webRequest.getAttribute(
                "javax.servlet.error.status_code",
                RequestAttributes.SCOPE_REQUEST
        ));

        if (status == null) {
            return null;
        }

        try {
            return HttpStatus.valueOf(status);
        } catch (Exception e) {
            return null;
        }
    }
}
