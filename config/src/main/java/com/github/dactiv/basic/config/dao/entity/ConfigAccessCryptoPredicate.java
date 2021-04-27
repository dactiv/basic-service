package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

/**
 * 访问加解密条件扩展
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@Alias("configAccessCryptoPredicate")
@TableName("tb_access_crypto_predicate")
@EqualsAndHashCode(callSuper = true)
public class ConfigAccessCryptoPredicate extends AccessCryptoPredicate {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @JsonSerialize(using = JacksonDateTime.Serializer.class)
    @JsonDeserialize(using = JacksonDateTime.Deserializer.class)
    private LocalDateTime creationTime = LocalDateTime.now();

    /**
     * 版本号
     */
    @Version
    @JsonIgnore
    private Integer updateVersion = 1;

    public ConfigAccessCryptoPredicate(AccessCryptoPredicate accessCryptoPredicate) {
        BeanUtils.copyProperties(accessCryptoPredicate, this);
    }
}
