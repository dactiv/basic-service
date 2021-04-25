package com.github.dactiv.basic.gateway;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.framework.commons.RestResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

/**
 * rest result 形式的 sentinel 异常响应
 *
 * @author maurice.chen
 */
public class RestResultGatewayBlockExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    public RestResultGatewayBlockExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        if (!BlockException.isBlockException(ex)) {
            return Mono.error(ex);
        }

        return exchange.getResponse().writeWith(Mono.create(dataBuffer -> {

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            ServerHttpResponse response = exchange.getResponse();
            HttpStatus status = response.getStatusCode();

            RestResult<Object> result = new RestResult<>(
                    status.getReasonPhrase(),
                    status.value(),
                    RestResult.ERROR_EXECUTE_CODE,
                    new LinkedHashMap<>()
            );

            byte[] bytes;

            try {
                bytes = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                bytes = e.getMessage().getBytes();
            }

            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            dataBuffer.success(buffer);
        }));

    }

}
