package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.config.entity.ConfigAccessCrypto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_access_crypto 访问加解密数据访问
 *
 * <p>Table: tb_access_crypto - 访问加解密</p>
 *
 * @see ConfigAccessCrypto
 *
 * @author maurice
 *
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface AccessCryptoDao extends BaseMapper<ConfigAccessCrypto> {
}
