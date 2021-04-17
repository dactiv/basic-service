package com.github.dactiv.basic.config.controller;


import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.basic.config.dao.entity.DataDictionary;
import com.github.dactiv.basic.config.service.AccessCryptoService;
import com.github.dactiv.basic.config.service.DictionaryService;
import com.github.dactiv.basic.config.service.DiscoveryEnumerateResourceService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.CryptoAlgorithm;
import com.github.dactiv.framework.crypto.access.token.SignToken;
import com.github.dactiv.framework.crypto.access.token.SimpleExpirationToken;
import com.github.dactiv.framework.crypto.access.token.SimpleToken;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AbstractBlockCipherService;
import com.github.dactiv.framework.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.framework.crypto.algorithm.exception.CryptoException;
import com.github.dactiv.framework.crypto.algorithm.hash.Hash;
import com.github.dactiv.framework.crypto.algorithm.hash.HashAlgorithmMode;
import com.github.dactiv.framework.spring.security.audit.Auditable;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 配置管理控制器
 *
 * @author maurice.chen
 */
@RefreshScope
@RestController
public class ConfigController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private AccessCryptoService accessCryptoService;

    @Autowired
    private DiscoveryEnumerateResourceService discoveryEnumerateResourceService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CryptoAlgorithm accessTokenAlgorithm;

    /**
     * 存储在 redis 的私有 token 超时时间(单位:秒)
     */
    @Value("${spring.application.crypto.access.private-token-interval:30}")
    private Integer privateTokenInterval;

    /**
     * 存储在 redis 的私有 token 超时时间(单位:秒)
     */
    @Value("${spring.application.crypto.access.access-token-interval:1800}")
    private Integer accessTokenInterval;

    /**
     * 伪装访问加解密的成功信息
     */
    @Value("${spring.application.crypto.access.camouflage-access-crypto-name:success access crypto}")
    private String camouflageAccessCryptoName;
    /**
     * 共有密钥
     */
    @Value("${spring.application.crypto.access.rsa.public-key}")
    private String publicKey;
    /**
     * 私有密钥
     */
    @Value("${spring.application.crypto.access.rsa.private-key}")
    private String privateKey;

    /**
     * 存储在 redis 的私有 token key 名称
     */
    @Value("${spring.application.crypto.access.redis.private-token-key:access:crypto:token:private:}")
    private String privateTokenKey;

    /**
     * 存储在 redis 的访问 token key 名称
     */
    @Value("${spring.application.crypto.access.redis.access-token-key:access:crypto:token:}")
    private String accessTokenKey;

    /**
     * 获取数据字典
     *
     * @param name 字典名称
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{name:.*}")
    public List<DataDictionary> findDataDictionaries(@PathVariable String name) {
        Map<String, Object> filter = new LinkedHashMap<>(16);

        int index = StringUtils.indexOf(name, "*");

        if (index > 0) {
            filter.put("codeLike", StringUtils.substring(name, 0, index));
        } else {
            filter.put("codeEq", name);
        }

        return dictionaryService.findDataDictionaries(filter);

    }

    /**
     * 获取所有访问加解密
     *
     * @param type 值
     * @return 访问加解密集合
     */
    @GetMapping("findAccessCrypto")
    public Map<String, Object> findAccessCrypto(@RequestParam String type) {

        Map<String, Object> filter = new LinkedHashMap<>();

        filter.put("typeEq", type);

        List<AccessCrypto> accessCryptoList = accessCryptoService.findAccessCryptoList(filter);

        Map<String, Object> entry = new LinkedHashMap<>();

        accessCryptoList.forEach(a -> {
            Map<String, Object> field = new LinkedHashMap<>();

            field.put(AccessCrypto.DEFAULT_REQUEST_DECRYPT_FIELD_NAME, a.getRequestDecrypt());
            field.put(AccessCrypto.DEFAULT_RESPONSE_ENCRYPT_FIELD_NAME, a.getResponseEncrypt());

            entry.put(StringUtils.removeEnd(a.getValue(), "/**"), field);
        });

        return entry;
    }

    /**
     * 获取公共 token
     *
     * @param deviceIdentified 设备唯一识别
     * @return 访问 token
     */
    @RequestMapping("getPublicToken")
    public AccessToken getPublicToken(
            @RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified) {

        String token = new Hash(HashAlgorithmMode.SHA1.getName(), deviceIdentified).getHex();

        redisTemplate.delete(getPrivateTokenKey(token));

        // 获取当前访问加解密的公共密钥
        ByteSource publicByteSource = new SimpleByteSource(Base64.decode(publicKey));
        // 获取当前访问加解密的私有密钥
        ByteSource privateByteSource = new SimpleByteSource(Base64.decode(privateKey));

        // 创建一个生成密钥类型 token，设置密钥为公共密钥，返回给客户端
        SimpleToken result = SimpleToken.generate(AccessToken.PUBLIC_TOKEN_KEY_NAME, publicByteSource);
        result.setToken(token);
        // 创建一个生成密钥类型 token，设置密钥为私有密钥，存储在缓存中
        SimpleToken temp = SimpleToken.generate(AccessToken.ACCESS_TOKEN_KEY_NAME, privateByteSource);
        // 将 token 设置为返回给客户端的 token，因为在获取访问 token 时，
        // 可以通过客户端传过来的密钥获取出私有密钥 token 的信息，
        // 详情查看本类的 getAccessToken 访问流程。
        temp.setToken(result.getToken());
        // 将私有密钥 token 转换为私有 token
        SimpleExpirationToken privateToken = new SimpleExpirationToken(
                temp,
                Duration.ofSeconds(privateTokenInterval)
        );
        // 获取存储在缓存的 key 名称
        String key = getPrivateTokenKey(privateToken.getToken());
        // 存储私有 token
        redisTemplate.opsForValue().set(key, privateToken, privateToken.getMaxInactiveInterval());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("生成 public token, 当前 token 为:"
                    + result.getToken() + ", 密钥为:" + publicByteSource.getBase64());
        }

        return result;

    }

    /**
     * 获取访问密钥
     *
     * @param deviceIdentified 设备唯一识别
     * @param token            token 信息
     * @param key              密钥信息
     * @return 一个或多个的带签名信息的 token
     */
    @PostMapping("getAccessToken")
    @Auditable(principal = "token", type = "获取访问密钥")
    public AccessToken getAccessToken(
            @RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
            @RequestParam String token,
            @RequestParam String key) {

        Object value = redisTemplate.opsForValue().get(getPrivateTokenKey(token));
        // 根据客户端请求过来的 token 获取私有 token， 如果没有。表示存在问题，返回一个伪装成功的 token
        SimpleExpirationToken privateToken = Casts.cast(value);

        String sha1Token = new Hash(HashAlgorithmMode.SHA1.getName(), deviceIdentified).getHex();

        if (privateToken == null || !privateToken.getToken().equals(token) || !sha1Token.equals(token)) {
            return createCamouflageToken();
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("正在生成 access token, 当前 token 为:" + token + ", 客户端密钥为:" + key);
        }

        RsaCipherService rsa = accessCryptoService.getCipherAlgorithmService().getCipherService("RSA");
        // 解析出客户端发给来的密钥
        ByteSource publicKey = rsa.decrypt(Base64.decode(key), privateToken.getKey().obtainBytes());

        // 根据请求解密算法模型创建块密码服务
        AbstractBlockCipherService cipherService = accessCryptoService
                .getCipherAlgorithmService().getCipherService(accessTokenAlgorithm);

        // 生成请求解密访问 token 密钥
        ByteSource requestAccessCryptoKey = new SimpleByteSource(cipherService.generateKey().getEncoded());

        // 创建请求解密的 token 信息
        SimpleExpirationToken requestToken = new SimpleExpirationToken(
                SimpleToken.generate(SimpleToken.ACCESS_TOKEN_KEY_NAME, requestAccessCryptoKey),
                Duration.ofSeconds(accessTokenInterval)
        );

        requestToken.setToken(token);
        // 存储到缓存中
        redisTemplate.opsForValue().set(
                getAccessTokenKey(requestToken.getToken()),
                requestToken,
                requestToken.getMaxInactiveInterval()
        );

        // 创建带签名校验的请求解密 token，这个是为了让客户端可以通过签名校验是否正确
        SignToken requestSignToken = createSignToken(
                rsa,
                requestToken,
                publicKey,
                privateToken
        );

        redisTemplate.delete(getPrivateTokenKey(token));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("生成新的 access token, 给 [" + token + "] 客户端使用, 本次返回未加密信息为:" + requestSignToken + ", 原文的 AES 密钥为:" + requestAccessCryptoKey.getBase64());
        }

        return requestSignToken;
    }

    /**
     * 创建一个带签名信息的 token
     *
     * @param rsa          rsa 算法服务
     * @param token        访问 token 信息
     * @param publicKey    密钥
     * @param privateToken 私有token
     * @return 带签名信息的 token
     */
    private SignToken createSignToken(RsaCipherService rsa,
                                      AccessToken token,
                                      ByteSource publicKey,
                                      SimpleExpirationToken privateToken) {
        // 将 token 的 key 加密
        ByteSource encryptAccessCryptoKey = rsa.encrypt(token.getKey().obtainBytes(), publicKey.obtainBytes());
        // 创建一个新的临时 token，客户端收到该 token 时候，
        // 对用自身申城的密钥解密出 encryptAccessCryptoKey，得到真正的加密密钥
        SimpleToken temp = SimpleToken.build(token.getType(), token.getName(), encryptAccessCryptoKey);
        temp.setToken(token.getToken());

        // 将加密的对称密钥生成一个签名，让客户端进行验证
        ByteSource byteSourceSign = rsa.sign(encryptAccessCryptoKey.obtainBytes(), privateToken.getKey().obtainBytes());

        return new SignToken(temp, byteSourceSign);
    }

    /**
     * 获取私有 token 的缓存名称
     *
     * @param token token 值
     * @return 缓存名称
     */
    private String getPrivateTokenKey(String token) {
        return privateTokenKey + token;
    }

    /**
     * 获取访问加解密 token 的缓存名称
     *
     * @param token token 值
     * @return 缓存名称
     */
    private String getAccessTokenKey(String token) {
        return accessTokenKey + token;
    }

    /**
     * 创建一个伪装成功的访问 token
     *
     * @return 访问 token
     */
    private AccessToken createCamouflageToken() {
        ByteSource byteSource = new SimpleByteSource(String.valueOf(System.currentTimeMillis()));
        return SimpleToken.generate(camouflageAccessCryptoName, byteSource);
    }

    /**
     * 获取服务枚举
     *
     * @param service       服务名
     * @param enumerateName 枚举名
     * @return 枚举信息
     */
    @GetMapping("getServiceEnumerate")
    public Map<String, Object> getServiceEnumerate(@RequestParam String service,
                                                   @RequestParam String enumerateName) {
        return discoveryEnumerateResourceService.getServiceEnumerate(service, enumerateName);
    }

    /**
     * 获取服务枚举名
     *
     * @param service 服务名
     * @return 服务枚举信息
     */
    @GetMapping("getServiceEnumerateName")
    public Set<String> getServiceEnumerateName(@RequestParam String service) {
        return discoveryEnumerateResourceService.getServiceEnumerateName(service);
    }

}
