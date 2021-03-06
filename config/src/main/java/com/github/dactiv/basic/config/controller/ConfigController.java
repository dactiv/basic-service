package com.github.dactiv.basic.config.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.config.config.ApplicationConfig;
import com.github.dactiv.basic.config.domain.entity.AccessCryptoEntity;
import com.github.dactiv.basic.config.domain.entity.DataDictionaryEntity;
import com.github.dactiv.basic.config.domain.meta.DataDictionaryMeta;
import com.github.dactiv.basic.config.service.AccessCryptoService;
import com.github.dactiv.basic.config.service.DictionaryService;
import com.github.dactiv.basic.config.service.EnumerateResourceService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.token.SignToken;
import com.github.dactiv.framework.crypto.access.token.SimpleExpirationToken;
import com.github.dactiv.framework.crypto.access.token.SimpleToken;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.framework.crypto.algorithm.hash.Hash;
import com.github.dactiv.framework.crypto.algorithm.hash.HashAlgorithmMode;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.security.audit.Auditable;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置管理控制器
 *
 * @author maurice.chen
 */
@Slf4j
@RefreshScope
@RestController
public class ConfigController {

    public static final String DEFAULT_EVN_URI = "actuator/env";

    private final DictionaryService dictionaryService;

    private final AccessCryptoService accessCryptoService;

    private final EnumerateResourceService enumerateResourceService;

    private final DiscoveryClient discoveryClient;

    private final ApplicationConfig properties;

    private final RestTemplate restTemplate;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    public ConfigController(DictionaryService dictionaryService,
                            AccessCryptoService accessCryptoService,
                            EnumerateResourceService enumerateResourceService,
                            DiscoveryClient discoveryClient,
                            ApplicationConfig properties,
                            RestTemplate restTemplate) {

        this.dictionaryService = dictionaryService;
        this.accessCryptoService = accessCryptoService;
        this.enumerateResourceService = enumerateResourceService;
        this.discoveryClient = discoveryClient;
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * 根据名称集合，分组获取所有数据字典
     *
     * @param names 字典名称集合
     *
     * @return 分组数据字典
     */
    @GetMapping("findGroupDataDictionaries")
    public Map<String, List<DataDictionaryMeta>> findGroupDataDictionaries(@RequestParam List<String> names) {
        Map<String, List<DataDictionaryMeta>> group = new LinkedHashMap<>();
        for (String name : names) {
            group.put(name, findDataDictionaries(name));
        }
        return group;
    }

    /**
     * 获取数据字典
     *
     * @param name 字典名称
     *
     * @return 数据字典集合
     */
    @GetMapping("findDataDictionaries/{name:.*}")
    public List<DataDictionaryMeta> findDataDictionaries(@PathVariable String name) {

        int index = StringUtils.indexOf(name, "*");

        LambdaQueryWrapper<DataDictionaryEntity> wrapper = Wrappers.lambdaQuery();

        wrapper.select(
                DataDictionaryEntity::getName,
                DataDictionaryEntity::getValue,
                DataDictionaryEntity::getValueType,
                DataDictionaryEntity::getLevel
        );

        if (index > 0) {
            wrapper.like(DataDictionaryEntity::getCode, StringUtils.substring(name, 0, index));
        } else {
            wrapper.eq(DataDictionaryEntity::getCode, name);
        }

        wrapper.orderByAsc(DataDictionaryEntity::getSort);

        return dictionaryService
                .getDataDictionaryService()
                .find(wrapper)
                .stream()
                .map(e -> Casts.of(e, DataDictionaryMeta.class))
                .peek(e -> e.setValue(Casts.cast(e.getValue(), e.getValueType().getClassType())))
                .peek(e -> e.setValueType(null))
                .collect(Collectors.toList());

    }

    /**
     * 获取所有访问加解密
     *
     * @param type 值
     *
     * @return 访问加解密集合
     */
    @GetMapping("findAccessCrypto")
    public Map<String, Object> findAccessCrypto(@RequestParam String type) {

        List<AccessCryptoEntity> accessCryptoList = accessCryptoService.find(
                Wrappers
                        .<AccessCryptoEntity>lambdaQuery()
                        .eq(AccessCrypto::getType, type)
        );

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
     * 通过 token id 获取访问 token
     *
     * @param id token id
     *
     * @return 访问 token
     */
    @PreAuthorize("hasRole('BASIC')")
    @GetMapping("obtainAccessToken")
    public AccessToken obtainAccessToken(@RequestParam String id) {
        return accessCryptoService.getAccessTokeBucket(id).get();
    }

    /**
     * 获取公共 token
     *
     * @param deviceIdentified 设备唯一识别
     *
     * @return 访问 token
     */
    @RequestMapping("getPublicToken")
    public AccessToken getPublicToken(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified) {
        return accessCryptoService.getPublicKey(deviceIdentified);

    }

    /**
     * 获取访问密钥
     *
     * @param deviceIdentified 设备唯一识别
     * @param token            token 信息
     * @param key              密钥信息
     *
     * @return 一个或多个的带签名信息的 token
     */
    @PostMapping("getAccessToken")
    @Auditable(principal = "token", type = "获取访问密钥")
    public AccessToken getAccessToken(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                      @RequestParam String token,
                                      @RequestParam String key) {

        RBucket<SimpleExpirationToken> bucket = accessCryptoService.getPrivateTokenBucket(token);
        // 根据客户端请求过来的 token 获取私有 token， 如果没有。表示存在问题，返回一个伪装成功的 token
        SimpleExpirationToken privateToken = bucket.get();

        String sha1Token = new Hash(HashAlgorithmMode.SHA1.getName(), deviceIdentified).getHex();

        if (privateToken == null || !privateToken.getToken().equals(token) || !sha1Token.equals(token)) {
            return createCamouflageToken();
        }

        if (log.isDebugEnabled()) {
            log.debug("正在生成 access token, 当前 token 为:{}, 客户端密钥为:{}", token, key);
        }

        RsaCipherService rsa = cipherAlgorithmService.getCipherService("RSA");
        // 解析出客户端发给来的密钥
        ByteSource publicKey = rsa.decrypt(Base64.decode(key), privateToken.getKey().obtainBytes());

        // 生成访问加 token
        AccessToken requestToken = accessCryptoService.generateAccessToken(token);

        // 创建带签名校验的请求解密 token，这个是为了让客户端可以通过签名校验是否正确
        SignToken requestSignToken = createSignToken(
                rsa,
                requestToken,
                publicKey,
                privateToken
        );

        bucket.deleteAsync();

        if (log.isDebugEnabled()) {
            log.debug(
                    "生成新的 access token, 给 [{}] 客户端使用, 本次返回未加密信息为: {}, 原文的 AES 密钥为: {}",
                    token,
                    requestSignToken,
                    requestToken.getKey().getBase64());
        }

        return requestSignToken;
    }

    /**
     * 生成访问秘钥
     *
     * @param request http 请求
     *
     * @return 访问秘钥
     */
    @PostMapping("generateAccessToken")
    public AccessToken generateAccessToken(HttpServletRequest request) {
        String deviceId = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        return accessCryptoService.generateAccessToken(deviceId);
    }

    /**
     * 创建一个带签名信息的 token
     *
     * @param rsa          rsa 算法服务
     * @param token        访问 token 信息
     * @param publicKey    密钥
     * @param privateToken 私有token
     *
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
     * 创建一个伪装成功的访问 token
     *
     * @return 访问 token
     */
    private AccessToken createCamouflageToken() {
        ByteSource byteSource = new SimpleByteSource(String.valueOf(System.currentTimeMillis()));
        return SimpleToken.generate(properties.getCamouflageAccessCryptoName(), byteSource);
    }

    /**
     * 获取服务枚举
     *
     * @param service       服务名
     * @param enumerateName 枚举名
     *
     * @return 枚举信息
     */
    @GetMapping("getServiceEnumerate")
    public Map<String, Object> getServiceEnumerate(@RequestParam String service,
                                                   @RequestParam String enumerateName) {
        return enumerateResourceService.getServiceEnumerate(service, enumerateName);
    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:*]')")
    @Plugin(name = "系统枚举查询", id = "enumerate", parent = "config", icon = "icon-enum-major-o", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return enumerateResourceService.getServiceEnumerate();
    }

    /**
     * 同步所有枚举
     *
     * @return 所有服务枚举信息
     */
    @PostMapping("syncEnumerate")
    @Idempotent(key = "idempotent:config:sync-enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:sync]')")
    @Plugin(name = "同步所有枚举", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() {

        enumerateResourceService.syncEnumerate();

        return RestResult.ofSuccess("同步系统枚举成功", enumerateResourceService.getServiceEnumerate());

    }

    /**
     * 获取服务枚举
     *
     * @return 服务枚举信息
     */
    @GetMapping("environment")
    @PreAuthorize("hasAuthority('perms[environment:*]')")
    @Plugin(name = "环境变量查询", id = "environment", parent = "config", icon = "icon-variable", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Object> environment() {

        Map<String, Object> result = new LinkedHashMap<>();

        List<String> services = discoveryClient.getServices();

        services.forEach(s -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(s);

            ServiceInstance instance = instances.stream().findFirst().orElse(null);

            if (Objects.nonNull(instance)) {
                String url = instance.getUri() + "/" + DEFAULT_EVN_URI;
                try {
                    //noinspection unchecked
                    Map<String, Object> data = restTemplate.getForObject(url, Map.class);
                    result.put(s, data);
                } catch (Exception e) {
                    log.warn("获取 [" + s + "] 服务环境变量出错", e);
                }
            }
        });

        return result;
    }

}
