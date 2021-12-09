package com.github.dactiv.basic.config.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.framework.commons.id.number.NumberIdEntity;
import com.github.dactiv.framework.crypto.access.AccessCryptoPredicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * <p>访问加解密断言扩展实体类</p>
 * <p>Table: tb_access_crypto_predicate - 访问加解密断言</p>
 *
 * @author maurice
 * @since 2021-05-06 11:59:41
 */
@Data
@NoArgsConstructor
@Alias("configAccessCryptoPredicate")
@TableName("tb_access_crypto_predicate")
@EqualsAndHashCode(callSuper = true)
public class AccessCryptoPredicateEntity extends AccessCryptoPredicate implements NumberIdEntity<Integer> {

    private static final long serialVersionUID = 5764360440250914629L;
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 访问加解密 id
     */
    private Integer accessCryptoId;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

}
