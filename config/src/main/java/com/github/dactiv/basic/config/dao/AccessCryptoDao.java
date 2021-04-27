package com.github.dactiv.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.basic.config.dao.entity.ConfigAccessCrypto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 访问加解密数据访问
 *
 * @author maurice.chen
 */
@Mapper
@Repository
public interface AccessCryptoDao extends BaseMapper<ConfigAccessCrypto> {
}
