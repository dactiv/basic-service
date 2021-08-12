package com.github.dactiv.basic.config.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.springframework.beans.BeanUtils;

import java.util.Date;

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
public class ConfigAccessCryptoPredicate extends AccessCryptoPredicate implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 5764360440250914629L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();


    public ConfigAccessCryptoPredicate(AccessCryptoPredicate accessCryptoPredicate) {
        BeanUtils.copyProperties(accessCryptoPredicate, this);
    }
}
