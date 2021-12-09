package com.github.dactiv.basic.config.service;

import com.github.dactiv.basic.config.dao.AccessCryptoPredicateDao;
import com.github.dactiv.basic.config.domain.entity.AccessCryptoPredicateEntity;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * tb_access_crypto_predicate 的业务逻辑
 *
 * <p>Table: tb_access_crypto_predicate - 访问加解密条件表</p>
 *
 * @see AccessCryptoPredicateEntity
 *
 * @author maurice.chen
 *
 * @since 2021-12-09 11:28:04
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessCryptoPredicateService extends BasicService<AccessCryptoPredicateDao, AccessCryptoPredicateEntity> {

}
