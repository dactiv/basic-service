package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.config.config.ApplicationConfig;
import com.github.dactiv.basic.config.dao.AccessCryptoDao;
import com.github.dactiv.basic.config.domain.entity.AccessCryptoEntity;
import com.github.dactiv.basic.config.domain.entity.AccessCryptoPredicateEntity;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessToken;
import com.github.dactiv.framework.crypto.access.token.SimpleExpirationToken;
import com.github.dactiv.framework.crypto.access.token.SimpleToken;
import com.github.dactiv.framework.crypto.algorithm.Base64;
import com.github.dactiv.framework.crypto.algorithm.ByteSource;
import com.github.dactiv.framework.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.framework.crypto.algorithm.cipher.AbstractBlockCipherService;
import com.github.dactiv.framework.crypto.algorithm.hash.Hash;
import com.github.dactiv.framework.crypto.algorithm.hash.HashAlgorithmMode;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_access_crypto 的业务逻辑
 *
 * <p>Table: tb_access_crypto - 访问加解密表</p>
 *
 * @author maurice.chen
 * @see AccessCryptoEntity
 * @since 2021-12-09 11:28:04
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessCryptoService extends BasicService<AccessCryptoDao, AccessCryptoEntity> {

    private final AccessCryptoPredicateService predicateService;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    private final ApplicationConfig config;

    private final RedissonClient redissonClient;

    public AccessCryptoService(AccessCryptoPredicateService predicateService,
                               ApplicationConfig config,
                               RedissonClient redissonClient) {
        this.predicateService = predicateService;
        this.config = config;
        this.redissonClient = redissonClient;
    }

    /**
     * 获取全部访问加解密集合
     *
     * @return 访问加解密集合
     */
    public List<AccessCryptoEntity> getAll() {

        RList<AccessCryptoEntity> accessCryptoEntities = redissonClient.getList(config.getAccessCryptoCache().getName());
        if (CollectionUtils.isNotEmpty(accessCryptoEntities)) {
            return accessCryptoEntities;
        }

        List<AccessCryptoEntity> result = lambdaQuery()
                .eq(AccessCrypto::getEnabled, DisabledOrEnabled.Enabled.getValue())
                .list()
                .stream()
                .peek(this::loadAccessCryptoPredicate)
                .collect(Collectors.toList());

        accessCryptoEntities.addAllAsync(result);

        TimeProperties expiresTime = config.getAccessCryptoCache().getExpiresTime();
        if (Objects.nonNull(expiresTime)) {
            accessCryptoEntities.expireAsync(expiresTime.getValue(), expiresTime.getUnit());
        }

        return result;
    }

    @Override
    public AccessCryptoEntity get(Serializable id) {
        AccessCryptoEntity result = super.get(id);
        loadAccessCryptoPredicate(result);
        return result;
    }

    /**
     * 加载通讯加解密断言条件集合
     *
     * @param accessCryptoEntity 通讯加解密实体
     */
    public void loadAccessCryptoPredicate(AccessCryptoEntity accessCryptoEntity) {

        List<AccessCryptoPredicateEntity> accessCryptoPredicates = predicateService
                .lambdaQuery()
                .eq(AccessCryptoPredicateEntity::getAccessCryptoId, accessCryptoEntity.getId())
                .list();

        accessCryptoEntity.setPredicates(new LinkedList<>(accessCryptoPredicates));
    }

    @Override
    public int save(AccessCryptoEntity entity) {
        if (YesOrNo.No.equals(entity.getRequestDecrypt())
                && YesOrNo.No.equals(entity.getResponseEncrypt())) {
            throw new ServiceException("请求解密或响应解密，必须存在一个为'是'的状态");
        }

        boolean isNew = Objects.isNull(entity.getId());

        int result = super.save(entity);

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
                    .map(p -> Casts.of(p, AccessCryptoPredicateEntity.class))
                    .peek(p -> p.setAccessCryptoId(entity.getId()))
                    .forEach(predicateService::save);
        }

        RList<AccessCryptoEntity> accessCryptos = redissonClient.getList(config.getAccessCryptoCache().getName());

        if (isNew) {
            accessCryptos.addAsync(entity);
        } else {
            Optional<AccessCryptoEntity> optional = accessCryptos
                    .stream()
                    .filter(c -> c.getId().equals(entity.getId()))
                    .findFirst();
            if (optional.isPresent()) {
                int index = accessCryptos.indexOf(optional.get());
                accessCryptos.setAsync(index, entity);
            }
        }

        return result;
    }

    @Override
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        predicateService.delete(
                Wrappers
                        .<AccessCryptoPredicateEntity>lambdaQuery()
                        .in(AccessCryptoPredicateEntity::getAccessCryptoId, ids)
        );

        RList<AccessCryptoEntity> accessCryptos = redissonClient.getList(config.getAccessCryptoCache().getName());
        List<AccessCryptoEntity> removeObjs = accessCryptos
                .stream()
                .filter(p -> ids.contains(p.getId()))
                .collect(Collectors.toList());
        accessCryptos.removeAllAsync(removeObjs);

        return super.deleteById(ids, errorThrow);
    }

    /**
     * 获取公共 token
     *
     * @param deviceId 设备唯一识别
     *
     * @return 访问 token
     */
    public AccessToken getPublicKey(String deviceId) {
        String token = new Hash(HashAlgorithmMode.SHA1.getName(), deviceId).getHex();

        RBucket<SimpleExpirationToken> bucket = getPrivateTokenBucket(token);

        bucket.deleteAsync();

        // 获取当前访问加解密的公共密钥
        ByteSource publicByteSource = new SimpleByteSource(Base64.decode(config.getRsa().getPublicKey()));
        // 获取当前访问加解密的私有密钥
        ByteSource privateByteSource = new SimpleByteSource(Base64.decode(config.getRsa().getPrivateKey()));

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
                config.getPrivateKeyCache().getExpiresTime()
        );

        // 存储私有 token
        if (Objects.nonNull(privateToken.getMaxInactiveInterval())) {
            bucket.set(
                    privateToken,
                    privateToken.getMaxInactiveInterval().getValue(),
                    privateToken.getMaxInactiveInterval().getUnit()
            );
        } else {
            bucket.set(privateToken);
        }

        if (log.isDebugEnabled()) {
            log.debug("生成 public token, 当前 token 为:{}, 密钥为:{}", result.getToken(), result.getKey().getBase64());
        }

        return result;
    }

    /**
     * 获取私有 token 的 redis 桶
     *
     * @param token token 值
     *
     * @return redis 桶
     */
    public RBucket<SimpleExpirationToken> getPrivateTokenBucket(String token) {
        return redissonClient.getBucket(config.getPrivateKeyCache().getName(token));
    }

    /**
     * 生成访问秘钥
     *
     * @param token token 信息
     *
     * @return 访问秘钥
     */
    public AccessToken generateAccessToken(String token) {
        // 根据请求解密算法模型创建块密码服务
        AbstractBlockCipherService cipherService = cipherAlgorithmService.getCipherService(config.getAlgorithm());

        // 生成请求解密访问 token 密钥
        ByteSource requestAccessCryptoKey = new SimpleByteSource(cipherService.generateKey().getEncoded());

        // 创建请求解密的 token 信息
        SimpleExpirationToken requestToken = new SimpleExpirationToken(
                SimpleToken.generate(SimpleToken.ACCESS_TOKEN_KEY_NAME, requestAccessCryptoKey),
                config.getAccessTokenKeyCache().getExpiresTime()
        );

        requestToken.setToken(token);

        RBucket<SimpleExpirationToken> requestBucket = getAccessTokeBucket(requestToken.getToken());
        // 存储到缓存中
        if (Objects.nonNull(requestToken.getMaxInactiveInterval())) {
            requestBucket.set(
                    requestToken,
                    requestToken.getMaxInactiveInterval().getValue(),
                    requestToken.getMaxInactiveInterval().getUnit()
            );
        } else {
            requestBucket.set(requestToken);
        }

        return requestToken;
    }

    /**
     * 获取访问 token 的 redis 桶
     *
     * @param token token 值
     *
     * @return redis 桶
     */
    public RBucket<SimpleExpirationToken> getAccessTokeBucket(String token) {
        return redissonClient.getBucket(config.getAccessTokenKeyCache().getName(token));
    }

}
