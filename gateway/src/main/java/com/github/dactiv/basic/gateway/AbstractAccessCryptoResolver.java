package com.github.dactiv.basic.gateway;

import com.github.dactiv.basic.gateway.config.ApplicationConfig;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.CodecUtils;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AbstractBlockCipherService;
import com.github.dactiv.framework.crypto.algorithm.exception.CryptoException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 抽象的访问加解密解析器实现
 *
 * @author maurice
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractAccessCryptoResolver implements AccessCryptoResolver {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractAccessCryptoResolver.class);

    private final ApplicationConfig config;

    private final List<RoutePredicateFactory> predicates;

    private final CipherAlgorithmService cipherAlgorithmService;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final ExpressionParser parser = new SpelExpressionParser();

    public AbstractAccessCryptoResolver(ApplicationConfig config,
                                        List<RoutePredicateFactory> predicates,
                                        ObjectProvider<CipherAlgorithmService> cipherAlgorithmService) {
        this.config = config;
        this.predicates = predicates;
        this.cipherAlgorithmService = cipherAlgorithmService.getIfUnique(CipherAlgorithmService::new);
    }

    @Override
    public boolean isRequestDecrypt(AccessCrypto accessCrypto, ServerWebExchange serverWebExchange) {
        // 判断是否符合请求解密
        return YesOrNo.Yes.equals(accessCrypto.getRequestDecrypt())
                && antPathMatcher.match(accessCrypto.getValue(), serverWebExchange.getRequest().getPath().value())
                && isAllMatchPredicate(serverWebExchange, accessCrypto.getPredicates());
    }

    /**
     * 创建断言条件
     *
     * @param acp        访问加解密断言
     * @param predicates 路由条件集合
     *
     * @return 断言条件
     */
    private Optional<Predicate<ServerWebExchange>> createPredicate(AccessCryptoPredicate acp, List<RoutePredicateFactory> predicates) {

        return predicates
                .stream()
                .filter(p -> acp.getName().equals(p.name()))
                .map(p -> createPredicate(p, acp))
                .findFirst();
    }

    /**
     * 创建断言条件
     *
     * @param p   路由条件
     * @param acp 访问加解密断言
     *
     * @return 断言条件
     */
    private Predicate<ServerWebExchange> createPredicate(RoutePredicateFactory p, AccessCryptoPredicate acp) {

        String value = acp.getValue();

        Object config = p.newConfig();
        // 如果某些路由断言的配置为多个字段时候，使用空格分隔字段在进行注入
        Arrays.stream(StringUtils.split(value)).forEach(v -> {

            String trimString = StringUtils.trim(v);

            String fieldName = StringUtils.substringBefore(trimString, this.config.getParamNameValueDelimiter());

            if (StringUtils.isBlank(fieldName)) {
                return;
            }

            String fieldValue = StringUtils.substringAfter(trimString, this.config.getParamNameValueDelimiter());

            if (StringUtils.isBlank(fieldValue)) {
                return;
            }

            // 使用 spring el 进行值的注入
            parser.parseExpression(fieldName).setValue(config, fieldValue);

        });

        return p.apply(config);
    }

    @Override
    public Mono<String> decryptRequestBody(ServerWebExchange serverWebExchange, AccessCrypto accessCrypto, String body) {
        // 获取客户端发过来的访问 token 值
        String accessToken = getAccessTokenValue(serverWebExchange);
        // 获取请求界面的访问 token
        AccessToken token = getAccessToken(accessToken);

        if (token == null) {
            throw new CryptoException("找不到访问 token 为 [" + accessToken + "]的 request 解密信息");
        }
        // 以字节形式去获取密文内容
        List<ByteSource> byteSource = getRequestCipher(body);

        MultiValueMap<String, String> newRequestBody = new LinkedMultiValueMap<>();

        byteSource.stream().map(b -> {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + accessCrypto.getName() + "] 对 token 为:"
                        + token.getToken() + "的密文:" + b.getBase64() + "开始解密");
            }

            // 使用访问加解密 的请求解密算法获取密码服务
            AbstractBlockCipherService cipherService = cipherAlgorithmService.getCipherService(config.getAlgorithm());
            // 解密
            ByteSource plaintextSource = cipherService.decrypt(b.obtainBytes(), token.getKey().obtainBytes());
            // 得到新的明文信息
            String plaintext = CodecUtils.toString(plaintextSource.obtainBytes());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + accessCrypto.getName() + "] 对 token 为:"
                        + token.getToken() + "的解密结果为:" + plaintext);
            }

            return Casts.castRequestBodyMap(plaintext);

        }).collect(Collectors.toList()).forEach(newRequestBody::putAll);
        // 获取原始的请求 body map
        MultiValueMap<String, String> originalBody = Casts.castRequestBodyMap(body);
        // 删除密文阐述
        originalBody.remove(config.getCipherTextParamName());
        // 将原始的请求 body 里还存在的参数添加到新的请求 body 里
        newRequestBody.putAll(originalBody);

        // 返回新的明文信息给路由
        return Mono.just(Casts.castRequestBodyMapToString(newRequestBody));
    }

    /**
     * 获取请求解密的访问 token
     *
     * @param accessToken 访问 token
     *
     * @return 访问 token
     */
    protected abstract AccessToken getAccessToken(String accessToken);

    /**
     * 获取访问 token
     *
     * @param serverWebExchange Server Web Exchange
     *
     * @return 访问 token
     */
    private String getAccessTokenValue(ServerWebExchange serverWebExchange) {

        List<String> accessToken = serverWebExchange.getRequest().getHeaders().get(config.getAccessTokenHeaders());

        if (CollectionUtils.isEmpty(accessToken)) {
            throw new CryptoException("key 值不能为 null");
        }

        return accessToken.iterator().next();
    }

    /**
     * 获取请求参数的密文内容
     *
     * @param body 请求参数内容
     *
     * @return 字节原实体
     */
    protected List<ByteSource> getRequestCipher(String body) {
        MultiValueMap<String, String> requestBody = decodeRequestBodyToMap(body);
        List<String> values = requestBody.get(config.getCipherTextParamName());

        return values.stream().map(value -> {

            try {
                String cipherText = URLDecoder.decode(value, CodecUtils.DEFAULT_ENCODING);

                byte[] plainText = CodecUtils.toBytes(cipherText);

                if (!Base64.isBase64(plainText)) {
                    throw new CryptoException("密文未经过 base64 编码:" + cipherText);
                }

                return new SimpleByteSource(Base64.decode(cipherText));

            } catch (UnsupportedEncodingException e) {
                throw new CryptoException(e);
            }

        }).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * 解码 request body 成 map
     *
     * @param body request body
     *
     * @return 解码后的 request body
     */
    protected MultiValueMap<String, String> decodeRequestBodyToMap(String body) {
        return Casts.castRequestBodyMap(body);
    }

    @Override
    public boolean isResponseEncrypt(AccessCrypto accessCrypto, ServerWebExchange serverWebExchange) {
        return YesOrNo.Yes.equals(accessCrypto.getResponseEncrypt())
                && antPathMatcher.match(accessCrypto.getValue(), serverWebExchange.getRequest().getPath().value())
                && isAllMatchPredicate(serverWebExchange, accessCrypto.getPredicates());
    }

    /**
     * 判断本次请求是否服务全部访问加解密断言
     *
     * @param serverWebExchange Server Web Exchange
     * @param acpList           访问加解密断言集合
     *
     * @return true 是，否则 false
     */
    private boolean isAllMatchPredicate(ServerWebExchange serverWebExchange, List<AccessCryptoPredicate> acpList) {
        return acpList
                .stream()
                .map(acp -> createPredicate(acp, predicates))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .allMatch(p -> p.test(serverWebExchange));
    }

    @Override
    public Mono<String> encryptResponseBody(ServerWebExchange serverWebExchange, AccessCrypto accessCrypto, String originalBody) {
        // 获取客户端发过来的访问 token 值,详情参考
        // config 项目的 com.github.dactiv.basic.config.test.controller.TestConfigController,
        // 了解获取流程
        String accessToken = getAccessTokenValue(serverWebExchange);
        // 获取响应加密的访问 token
        AccessToken token = getAccessToken(accessToken);

        if (accessToken == null) {
            throw new CryptoException("找不到访问 token 为 [" + accessToken + "]的 response 解密信息");
        }
        // 使用访问加解密的响应算法获取密码服务
        AbstractBlockCipherService cipherService = cipherAlgorithmService.getCipherService(config.getAlgorithm());
        // 加密响应内容
        ByteSource byteSource = cipherService.encrypt(CodecUtils.toBytes(originalBody), token.getKey().obtainBytes());
        // 返回加密内容
        return Mono.just(byteSource.getBase64());
    }
}
