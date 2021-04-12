package com.fuyu.basic.gateway.filter;

import com.fuyu.basic.commons.exception.SystemException;
import com.fuyu.basic.gateway.AccessCryptoResolver;
import com.fuyu.basic.support.crypto.access.AccessCrypto;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 响应加密 filter
 *
 * @author maurice
 */
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class ResponseEncryptFilter implements GlobalFilter, Ordered {

    @Autowired
    private AccessCryptoResolver accessCryptoResolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        Optional<AccessCrypto> optional = accessCryptoResolver.getAccessCryptoList()
                .stream()
                .filter(a -> accessCryptoResolver.isResponseEncrypt(a, exchange))
                .findFirst();

        if (optional.isPresent()) {
            return chain.filter(exchange.mutate().response(decorate(exchange, optional.get())).build());
        } else {
            return chain.filter(exchange);
        }
    }

    private ServerHttpResponse decorate(ServerWebExchange exchange, AccessCrypto accessCrypto) {
        return new ServerHttpResponseDecorator(exchange.getResponse()) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

                //String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
                HttpHeaders httpHeaders = new HttpHeaders();
                // explicitly add it in this way instead of
                // 'httpHeaders.setContentType(originalResponseContentType)'
                // this will prevent exception in case of using non-standard media
                // types like "Content-Type: image"
                httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

                ClientResponse clientResponse = prepareClientResponse(body, httpHeaders);

                // TODO: flux or mono
                Mono<String> modifiedBody = clientResponse.bodyToMono(String.class)
                        .flatMap(originalBody ->
                                accessCryptoResolver.encryptResponseBody(exchange, accessCrypto, originalBody))
                        .doOnError(Mono::error)
                        .switchIfEmpty(Mono.error(new SystemException("response 加密错误")));

                BodyInserter<Mono<String>, org.springframework.http.ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(modifiedBody, String.class);
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(
                        exchange, exchange.getResponse().getHeaders());
                return bodyInserter.insert(outputMessage, new BodyInserterContext())
                        .then(Mono.defer(() -> {
                            Flux<DataBuffer> messageBody = outputMessage.getBody();
                            HttpHeaders headers = getDelegate().getHeaders();
                            if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
                                messageBody = messageBody.doOnNext(data -> headers
                                        .setContentLength(data.readableByteCount()));
                            }
                            // TODO: fail if isStreamingMediaType?
                            return getDelegate().writeWith(messageBody);
                        }));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }

            private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body,
                                                         HttpHeaders httpHeaders) {
                return ClientResponse.create(
                        exchange.getResponse().getStatusCode() == null
                                ? HttpStatus.OK
                                : exchange.getResponse().getStatusCode()
                ).headers(headers -> headers.putAll(httpHeaders)).body(Flux.from(body)).build();
            }

        };
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
}
