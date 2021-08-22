package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.config.entity.ConfigAccessCryptoPredicate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 访问加解密断言数据访问
 *
 * @author maurice.chen
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
