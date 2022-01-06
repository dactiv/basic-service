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
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
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
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessCryptoService extends BasicService<AccessCryptoDao, AccessCryptoEntity> {

    private final AccessCryptoPredicateService predicateService;

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

}
