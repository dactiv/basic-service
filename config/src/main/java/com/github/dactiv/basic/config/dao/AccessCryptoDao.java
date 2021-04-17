package com.github.dactiv.basic.config.dao;

import com.github.dactiv.framework.commons.BasicCurdDao;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 访问加解密数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface AccessCryptoDao extends BasicCurdDao<AccessCrypto, Integer> {
}
