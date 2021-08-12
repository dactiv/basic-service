package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.crypto.access.AccessCrypto;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.ArrayList;
import java.util.Date;
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
public class ConfigAccessCrypto extends AccessCrypto implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 126959369778385198L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 加解密条件
     */
    @TableField(exist = false)
    private List<AccessCryptoPredicate> predicates = new ArrayList<>();

}
