package com.fuyu.basic.config.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.support.crypto.access.AccessCrypto;
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
