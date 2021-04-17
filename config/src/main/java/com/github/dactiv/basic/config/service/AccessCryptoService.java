package com.github.dactiv.basic.config.service;

import com.github.dactiv.framework.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.basic.config.dao.AccessCryptoDao;
import com.github.dactiv.basic.config.dao.AccessCryptoPredicateDao;
import com.github.dactiv.framework.crypto.CipherAlgorithmService;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
     * @return 字典实体
     */
    public AccessCrypto getAccessCrypto(Integer id) {

        AccessCrypto accessCrypto = accessCryptoDao.get(id);

        if (accessCrypto != null) {

            Map<String, Object> filter = new LinkedHashMap<>();
            filter.put("accessCryptoIdEq", id);
            List<AccessCryptoPredicate> accessCryptoPredicates = accessCryptoPredicateDao.find(filter);

            accessCrypto.setPredicates(accessCryptoPredicates);
        }

        return accessCrypto;
    }

    /**
     * 查找访问加解密
     *
     * @param filter 过滤条件
     * @return 访问加解密集合
     */
    public List<AccessCrypto> findAccessCryptoList(Map<String, Object> filter) {
        return findAccessCryptoList(filter, false);
    }

    /**
     * 查找访问加解密
     *
     * @param filter         过滤条件
     * @param loadPredicates 是否加载访问加解密条件
     * @return 访问加解密集合
     */
    public List<AccessCrypto> findAccessCryptoList(Map<String, Object> filter, boolean loadPredicates) {
        List<AccessCrypto> accessCryptoList = accessCryptoDao.find(filter);

        if (loadPredicates) {
            accessCryptoList.forEach(x -> {
                Map<String, Object> predicateFilter = new LinkedHashMap<>();
                predicateFilter.put("accessCryptoIdEq", x.getId());
                x.setPredicates(accessCryptoPredicateDao.find(predicateFilter));
            });

        }

        return accessCryptoList;
    }

    /**
     * 获取访问加解密
     *
     * @param filter 过滤条件
     * @return 访问加解密
     */
    public AccessCrypto getAccessCryptoByFilter(Map<String, Object> filter) {
        List<AccessCrypto> result = findAccessCryptoList(filter);

        if (result.size() > 1) {
            throw new ServiceException("通过条件[" + filter + "]查询出来的记录等于" + result.size() + "条,并非单一记录");
        }

        Iterator<AccessCrypto> iterator = result.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * 查找访问加解密分页信息
     *
     * @param pageRequest 分页请求
     * @param filter      过滤条件
     * @return 分页实体
     */
    public Page<AccessCrypto> findAccessCryptoPage(PageRequest pageRequest, Map<String, Object> filter) {

        filter.putAll(pageRequest.getOffsetMap());

        List<AccessCrypto> data = findAccessCryptoList(filter);

        return new Page<>(pageRequest, data);
    }

    /**
     * 保存访问加解密
     *
     * @param entity 访问加解密实体
     */
    public void saveAccessCrypto(AccessCrypto entity) {

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
    public void updateAccessCrypto(AccessCrypto entity) {

        accessCryptoDao.update(entity);

        accessCryptoPredicateDao.deleteByAccessCryptoId(entity.getId());

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
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
    public void insertAccessCrypto(AccessCrypto entity) {

        accessCryptoDao.insert(entity);

        if (!entity.getPredicates().isEmpty()) {
            entity.getPredicates()
                    .stream()
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
            accessCryptoDao.delete(id);
            accessCryptoPredicateDao.deleteByAccessCryptoId(id);
        }
        syncAccessCryptoListToRedis();
    }

    /**
     * 通过访问加解密集合到 redis 中
     */
    public void syncAccessCryptoListToRedis() {

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("enabledEq", DisabledOrEnabled.Enabled.getValue());

        List<AccessCrypto> data = findAccessCryptoList(filter, true);

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