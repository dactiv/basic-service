package com.github.dactiv.basic.config.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dactiv.basic.config.dao.AccessCryptoDao;
import com.github.dactiv.basic.config.dao.AccessCryptoPredicateDao;
import com.github.dactiv.basic.config.dao.entity.ConfigAccessCrypto;
import com.github.dactiv.basic.config.dao.entity.ConfigAccessCryptoPredicate;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import com.github.dactiv.framework.spring.web.filter.generator.mybatis.MybatisPlusQueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储在 redis 的访问加解密集合 key 名称
     */
    @Value("${spring.application.crypto.access.redis.access-crypto-list-key:access:crypto:all}")
    private String accessCryptoListKey;

    /**
     * 密码算法服务
     */
    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

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
     * 获取访问加解密条件扩展集合
     *
     * @param accessCryptoId 访问加解 id
     *
     * @return 访问加解密条件扩展集合
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
     * @param loadPredicates 是否加载访问加解密条件
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
     * @param pageable 分页请求
     * @param wrapper  包装器
     *
     * @return 分页实体
     */
    public Page<ConfigAccessCrypto> findAccessCryptoPage(Pageable pageable, Wrapper<ConfigAccessCrypto> wrapper) {

        IPage<ConfigAccessCrypto> result = accessCryptoDao.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageable),
                wrapper
        );

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
            throw new ServiceException("请求解密或响应解密，必须存在一个为 1 的状态");
        }

        if (Objects.nonNull(entity.getId())) {
            updateAccessCrypto(entity);
        } else {
            insertAccessCrypto(entity);
        }

        syncAccessCryptoListToRedis();
    }

    /**
     * 更新访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void updateAccessCrypto(ConfigAccessCrypto entity) {

        accessCryptoDao.updateById(entity);

        accessCryptoPredicateDao.deleteByAccessCryptoId(entity.getId());

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
                    .map(ConfigAccessCryptoPredicate::new)
                    .peek(p -> p.setAccessCryptoId(entity.getId()))
                    .collect(Collectors.toList())
                    .forEach(p -> accessCryptoPredicateDao.insert(p));
        }
    }

    /**
     * 新增访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void insertAccessCrypto(ConfigAccessCrypto entity) {

        accessCryptoDao.insert(entity);

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
                    .map(ConfigAccessCryptoPredicate::new)
                    .peek(p -> p.setAccessCryptoId(entity.getId()))
                    .collect(Collectors.toList())
                    .forEach(p -> accessCryptoPredicateDao.insert(p));
        }

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
        syncAccessCryptoListToRedis();
    }

    /**
     * 通过访问加解密集合到 redis 中
     */
    public void syncAccessCryptoListToRedis() {

        List<ConfigAccessCrypto> data = findAccessCryptoList(
                Wrappers
                        .<ConfigAccessCrypto>lambdaQuery()
                        .eq(AccessCrypto::getEnabled, YesOrNo.Yes.getValue()),
                true
        );

        redisTemplate.opsForValue().set(accessCryptoListKey, data);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("同步: " + data + " 到 redis [" + accessCryptoListKey + "] 中");
        }
    }

    @Override
    public void afterPropertiesSet() {
        syncAccessCryptoListToRedis();
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