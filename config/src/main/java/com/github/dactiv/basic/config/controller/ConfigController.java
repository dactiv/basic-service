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
 * ?????????????????????
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
     * ???????????????????????????????????????????????????
     *
     * @param names ??????????????????
     *
     * @return ??????????????????
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
     * ??????????????????
     *
     * @param name ????????????
     *
     * @return ??????????????????
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
     * ???????????????????????????
     *
     * @param type ???
     *
     * @return ?????????????????????
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
     * ?????? token id ???????????? token
     *
     * @param id token id
     *
     * @return ?????? token
     */
    @PreAuthorize("hasRole('BASIC')")
    @GetMapping("obtainAccessToken")
    public AccessToken obtainAccessToken(@RequestParam String id) {
        return accessCryptoService.getAccessTokeBucket(id).get();
    }

    /**
     * ???????????? token
     *
     * @param deviceIdentified ??????????????????
     *
     * @return ?????? token
     */
    @RequestMapping("getPublicToken")
    public AccessToken getPublicToken(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified) {
        return accessCryptoService.getPublicKey(deviceIdentified);

    }

    /**
     * ??????????????????
     *
     * @param deviceIdentified ??????????????????
     * @param token            token ??????
     * @param key              ????????????
     *
     * @return ???????????????????????????????????? token
     */
    @PostMapping("getAccessToken")
    @Auditable(principal = "token", type = "??????????????????")
    public AccessToken getAccessToken(@RequestHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME) String deviceIdentified,
                                      @RequestParam String token,
                                      @RequestParam String key) {

        RBucket<SimpleExpirationToken> bucket = accessCryptoService.getPrivateTokenBucket(token);
        // ?????????????????????????????? token ???????????? token??? ??????????????????????????????????????????????????????????????? token
        SimpleExpirationToken privateToken = bucket.get();

        String sha1Token = new Hash(HashAlgorithmMode.SHA1.getName(), deviceIdentified).getHex();

        if (privateToken == null || !privateToken.getToken().equals(token) || !sha1Token.equals(token)) {
            return createCamouflageToken();
        }

        if (log.isDebugEnabled()) {
            log.debug("???????????? access token, ?????? token ???:{}, ??????????????????:{}", token, key);
        }

        RsaCipherService rsa = cipherAlgorithmService.getCipherService("RSA");
        // ????????????????????????????????????
        ByteSource publicKey = rsa.decrypt(Base64.decode(key), privateToken.getKey().obtainBytes());

        // ??????????????? token
        AccessToken requestToken = accessCryptoService.generateAccessToken(token);

        // ???????????????????????????????????? token??????????????????????????????????????????????????????????????????
        SignToken requestSignToken = createSignToken(
                rsa,
                requestToken,
                publicKey,
                privateToken
        );

        bucket.deleteAsync();

        if (log.isDebugEnabled()) {
            log.debug(
                    "???????????? access token, ??? [{}] ???????????????, ??????????????????????????????: {}, ????????? AES ?????????: {}",
                    token,
                    requestSignToken,
                    requestToken.getKey().getBase64());
        }

        return requestSignToken;
    }

    /**
     * ??????????????????
     *
     * @param request http ??????
     *
     * @return ????????????
     */
    @PostMapping("generateAccessToken")
    public AccessToken generateAccessToken(HttpServletRequest request) {
        String deviceId = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);
        return accessCryptoService.generateAccessToken(deviceId);
    }

    /**
     * ?????????????????????????????? token
     *
     * @param rsa          rsa ????????????
     * @param token        ?????? token ??????
     * @param publicKey    ??????
     * @param privateToken ??????token
     *
     * @return ?????????????????? token
     */
    private SignToken createSignToken(RsaCipherService rsa,
                                      AccessToken token,
                                      ByteSource publicKey,
                                      SimpleExpirationToken privateToken) {
        // ??? token ??? key ??????
        ByteSource encryptAccessCryptoKey = rsa.encrypt(token.getKey().obtainBytes(), publicKey.obtainBytes());
        // ???????????????????????? token????????????????????? token ?????????
        // ???????????????????????????????????? encryptAccessCryptoKey??????????????????????????????
        SimpleToken temp = SimpleToken.build(token.getType(), token.getName(), encryptAccessCryptoKey);
        temp.setToken(token.getToken());

        // ?????????????????????????????????????????????????????????????????????
        ByteSource byteSourceSign = rsa.sign(encryptAccessCryptoKey.obtainBytes(), privateToken.getKey().obtainBytes());

        return new SignToken(temp, byteSourceSign);
    }

    /**
     * ????????????????????????????????? token
     *
     * @return ?????? token
     */
    private AccessToken createCamouflageToken() {
        ByteSource byteSource = new SimpleByteSource(String.valueOf(System.currentTimeMillis()));
        return SimpleToken.generate(properties.getCamouflageAccessCryptoName(), byteSource);
    }

    /**
     * ??????????????????
     *
     * @param service       ?????????
     * @param enumerateName ?????????
     *
     * @return ????????????
     */
    @GetMapping("getServiceEnumerate")
    public Map<String, Object> getServiceEnumerate(@RequestParam String service,
                                                   @RequestParam String enumerateName) {
        return enumerateResourceService.getServiceEnumerate(service, enumerateName);
    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
     */
    @GetMapping("enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:*]')")
    @Plugin(name = "??????????????????", id = "enumerate", parent = "config", icon = "icon-enum-major-o", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
    public Map<String, Map<String, Map<String, Object>>> enumerate() {
        return enumerateResourceService.getServiceEnumerate();
    }

    /**
     * ??????????????????
     *
     * @return ????????????????????????
     */
    @PostMapping("syncEnumerate")
    @Idempotent(key = "idempotent:config:sync-enumerate")
    @PreAuthorize("hasAuthority('perms[enumerate:sync]')")
    @Plugin(name = "??????????????????", parent = "enumerate", sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE, audit = true)
    public RestResult<Map<String, Map<String, Map<String, Object>>>> syncEnumerate() {

        enumerateResourceService.syncEnumerate();

        return RestResult.ofSuccess("????????????????????????", enumerateResourceService.getServiceEnumerate());

    }

    /**
     * ??????????????????
     *
     * @return ??????????????????
     */
    @GetMapping("environment")
    @PreAuthorize("hasAuthority('perms[environment:*]')")
    @Plugin(name = "??????????????????", id = "environment", parent = "config", icon = "icon-variable", type = ResourceType.Menu, sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE)
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
                    log.warn("?????? [" + s + "] ????????????????????????", e);
                }
            }
        });

        return result;
    }

}
