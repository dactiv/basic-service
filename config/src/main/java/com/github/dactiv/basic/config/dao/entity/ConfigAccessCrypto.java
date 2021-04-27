package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.dactiv.framework.commons.jackson.JacksonDateTime;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 访问加解密配置扩展
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@Alias("configAccessCrypto")
@TableName("tb_access_crypto")
@EqualsAndHashCode(callSuper = true)
public class ConfigAccessCrypto extends AccessCrypto {

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

    /**
     * 加解密条件
     */
    @TableField(exist = false)
    private List<AccessCryptoPredicate> predicates = new ArrayList<>();

}
