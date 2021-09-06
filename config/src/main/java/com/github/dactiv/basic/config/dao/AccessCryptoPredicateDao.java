package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.config.entity.ConfigAccessCryptoPredicate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * tb_access_crypto_predicate 访问加解密断言数据访问
 *
 * <p>Table: tb_access_crypto_predicate - 访问加解密断言</p>
 *
 * @author maurice
 * @see ConfigAccessCryptoPredicate
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface AccessCryptoPredicateDao extends BaseMapper<ConfigAccessCryptoPredicate> {

    /**
     * 删除访问加解密断言
     *
     * @param id 访问加解密 id
     */
    @Delete("<script>DELETE FROM tb_access_crypto_predicate WHERE access_crypto_id = #{id}</script>")
    void deleteByAccessCryptoId(@Param("id") Integer id);
}
