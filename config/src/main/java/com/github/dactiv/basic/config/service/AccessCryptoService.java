package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.github.dactiv.basic.config.dao.AccessCryptoDao;
import com.github.dactiv.basic.config.dao.AccessCryptoPredicateDao;
import com.github.dactiv.basic.config.entity.ConfigAccessCrypto;
import com.github.dactiv.basic.config.entity.ConfigAccessCryptoPredicate;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import org.redisson.api.RFuture;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 访问加解密管理
 *
 * @author maurice
 */
@Service
@RefreshScope
@Transactional(rollbackFor = Exception.class)
public class AccessCryptoService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessCryptoService.class);

    @Autowired
    private AccessCryptoPredicateDao accessCryptoPredicateDao;

    @Autowired
    private AccessCryptoDao accessCryptoDao;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 存储在 redis 的访问加解密集合 key 名称
     */
    @Value("${spring.application.crypto.access.redis.access-crypto-list-key:access:cryptos}")
    private String accessCryptoListKey;

    /**
     * 密码算法服务
     */
    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    /**
     * 保存访问加解密断言
     *
     * @param predicate 访问加解密断言
     */
    public void saveAccessCryptoPredicate(ConfigAccessCryptoPredicate predicate) {
        if (Objects.isNull(predicate.getId())) {
            accessCryptoPredicateDao.insert(predicate);
        } else {
            accessCryptoPredicateDao.updateById(predicate);
        }
    }

    // ----------------------------------------- 访问加解密管理 ----------------------------------------- //

    /**
     * 获取访问加解密
     *
     * @param id 访问加解密 ID
     *
     * @return 字典实体
     */
    public ConfigAccessCrypto getAccessCrypto(Integer id) {

        ConfigAccessCrypto accessCrypto = accessCryptoDao.selectById(id);

        if (accessCrypto != null) {

            List<ConfigAccessCryptoPredicate> accessCryptoPredicates = getConfigAccessCryptoPredicatesByAccessCryptoId(id);

            accessCrypto.getPredicates().addAll(accessCryptoPredicates);
        }

        return accessCrypto;
    }

    /**
     * 获取访问加解密断言扩展集合
     *
     * @param accessCryptoId 访问加解 id
     *
     * @return 访问加解密断言扩展集合
     */
    public List<ConfigAccessCryptoPredicate> getConfigAccessCryptoPredicatesByAccessCryptoId(Integer accessCryptoId) {

        return accessCryptoPredicateDao.selectList(
                Wrappers
                        .<ConfigAccessCryptoPredicate>lambdaQuery()
                        .eq(AccessCryptoPredicate::getAccessCryptoId, accessCryptoId)
        );

    }

    /**
     * 查找访问加解密
     *
     * @param wrapper 包装器
     *
     * @return 访问加解密集合
     */
    public List<ConfigAccessCrypto> findAccessCryptoList(Wrapper<ConfigAccessCrypto> wrapper) {
        return findAccessCryptoList(wrapper, false);
    }

    /**
     * 查找访问加解密
     *
     * @param wrapper        包装器
     * @param loadPredicates 是否加载访问加解密断言
     *
     * @return 访问加解密集合
     */
    public List<ConfigAccessCrypto> findAccessCryptoList(Wrapper<ConfigAccessCrypto> wrapper, boolean loadPredicates) {
        List<ConfigAccessCrypto> accessCryptoList = accessCryptoDao.selectList(wrapper);

        if (loadPredicates) {
            accessCryptoList.forEach(x -> x.getPredicates().addAll(getConfigAccessCryptoPredicatesByAccessCryptoId(x.getId())));
        }

        return accessCryptoList;
    }

    /**
     * 查找访问加解密分页信息
     *
     * @param pageRequest 分页请求
     * @param wrapper     包装器
     *
     * @return 分页实体
     */
    public Page<ConfigAccessCrypto> findAccessCryptoPage(PageRequest pageRequest, Wrapper<ConfigAccessCrypto> wrapper) {

        PageDTO<ConfigAccessCrypto> page = MybatisPlusQueryGenerator.createQueryPage(pageRequest);

        page.addOrder(OrderItem.desc(IdEntity.ID_FIELD_NAME));

        IPage<ConfigAccessCrypto> result = accessCryptoDao.selectPage(page, wrapper);

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void saveAccessCrypto(ConfigAccessCrypto entity) {

        if (YesOrNo.No.getValue().equals(entity.getRequestDecrypt())
                && YesOrNo.No.getValue().equals(entity.getResponseEncrypt())) {
            throw new ServiceException("请求解密或响应解密，必须存在一个为'是'的状态");
        }

        if (Objects.nonNull(entity.getId())) {
            updateAccessCrypto(entity);
            accessCryptoPredicateDao.deleteByAccessCryptoId(entity.getId());
        } else {
            insertAccessCrypto(entity);
        }

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
                    .map(p -> Casts.of(p, ConfigAccessCryptoPredicate.class))
                    .peek(p -> p.setAccessCryptoId(entity.getId()))
                    .forEach(this::saveAccessCryptoPredicate);
        }

        syncRedisAccessCryptoList();
    }

    /**
     * 更新访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void updateAccessCrypto(ConfigAccessCrypto entity) {
        accessCryptoDao.updateById(entity);
    }

    /**
     * 新增访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void insertAccessCrypto(ConfigAccessCrypto entity) {
        accessCryptoDao.insert(entity);
    }

    /**
     * 删除字典值
     *
     * @param ids 字典实体
     */
    public void deleteAccessCrypto(List<Integer> ids) {
        for (Integer id : ids) {
            accessCryptoDao.deleteById(id);
            accessCryptoPredicateDao.deleteByAccessCryptoId(id);
        }
        syncRedisAccessCryptoList();
    }

    /**
     * 通过访问加解密集合到 redis 中
     */
    public void syncRedisAccessCryptoList() {

        RList<AccessCrypto> accessCryptos = redissonClient.getList(accessCryptoListKey);

        RFuture<Boolean> booleanRFuture = accessCryptos.removeAllAsync(accessCryptos.readAll());

        List<ConfigAccessCrypto> data = findAccessCryptoList(
                Wrappers
                        .<ConfigAccessCrypto>lambdaQuery()
                        .eq(AccessCrypto::getEnabled, YesOrNo.Yes.getValue()),
                true
        );

        List<AccessCrypto> result = data
                .stream()
                .map(configAccessCrypto -> {

                    AccessCrypto accessCrypto = Casts.of(
                            configAccessCrypto,
                            AccessCrypto.class,
                            "predicates"
                    );

                    accessCrypto.getPredicates()
                            .stream()
                            .map(p -> Casts.of(p, AccessCryptoPredicate.class))
                            .forEach(p -> accessCrypto.getPredicates().add(p));

                    return accessCrypto;
                })
                .collect(Collectors.toList());


        booleanRFuture.onComplete((success, throwable) -> {

            if (!success) {
                return;
            }

            accessCryptos.addAllAsync(result);

        });

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("同步: " + data + " 到 redis [" + accessCryptoListKey + "] 中");
        }
    }

    @Override
    public void afterPropertiesSet() {
        syncRedisAccessCryptoList();
    }

    /**
     * 获取密码算法服务
     *
     * @return 密码算法服务
     */
    public CipherAlgorithmService getCipherAlgorithmService() {
        return cipherAlgorithmService;
    }
}