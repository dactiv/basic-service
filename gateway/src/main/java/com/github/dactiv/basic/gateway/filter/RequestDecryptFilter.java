package com.github.dactiv.basic.gateway.filter;

import com.github.dactiv.basic.gateway.AccessCryptoResolver;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 请求解密 filter
 *
 * @author maurice
 */
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class RequestDecryptFilter implements GlobalFilter, Ordered {

    private final AccessCryptoResolver accessCryptoResolver;

    public RequestDecryptFilter(AccessCryptoResolver accessCryptoResolver) {
        this.accessCryptoResolver = accessCryptoResolver;
    }

    @Override
    public Mono filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Optional<AccessCrypto> optional = accessCryptoResolver.getAccessCryptoList()
                .stream()
                .filter(m -> accessCryptoResolver.isRequestDecrypt(m, exchange))
                .findFirst();

        if (optional.isPresent()) {
            ServerRequest serverRequest = ServerRequest
                    .create(exchange, HandlerStrategies.withDefaults().messageReaders());

            Mono<String> modifiedBody = serverRequest
                    .bodyToMono(String.class)
                    .switchIfEmpty(Mono.just(Casts.castRequestBodyMapToString(serverRequest.queryParams())))
                    .flatMap(body -> accessCryptoResolver.decryptRequestBody(exchange, optional.get(), body))
                    .doOnError(Mono::error)
                    .switchIfEmpty(Mono.error(new SystemException("request 密文内容不正确")));

            BodyInserter<Mono<String>, org.springframework.http.ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
            HttpHeaders headers = new HttpHeaders();
            headers.putAll(exchange.getRequest().getHeaders());

            headers.remove(HttpHeaders.CONTENT_LENGTH);

            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

            return bodyInserter.insert(outputMessage, new BodyInserterContext())
                    .then(Mono.defer(() -> {

                        ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);

                        return chain.filter(exchange.mutate().request(decorator).build());
                    }));
        } else {
            return chain.filter(exchange);
        }
    }

    ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
                                        CachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                if (contentLength > 0) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                }

                return httpHeaders;
            }

            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
