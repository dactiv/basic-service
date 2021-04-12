package com.fuyu.basic.config.dao;

import com.fuyu.basic.commons.BasicCurdDao;
import com.fuyu.basic.support.crypto.access.AccessCryptoPredicate;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 访问加解密条件数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface AccessCryptoPredicateDao extends BasicCurdDao<AccessCryptoPredicate, Integer> {

    /**
     * 删除访问加解密条件
     *
     * @param id 访问加解密 id
     */
    void deleteByAccessCryptoId(Integer id);
}
