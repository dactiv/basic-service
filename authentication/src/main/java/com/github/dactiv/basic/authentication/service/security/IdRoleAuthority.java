package com.github.dactiv.basic.authentication.service.security;

import com.github.dactiv.framework.commons.id.BasicIdentification;
import com.github.dactiv.framework.spring.security.entity.RoleAuthority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 带 id 的 RoleAuthority 实现，用于用户被分配组时存储 json 格式使用
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = true)
public class IdRoleAuthority extends RoleAuthority implements BasicIdentification<Integer> {

    private static final long serialVersionUID = 5630516721501929606L;

    /**
     * 主键 id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 权限信息
     */
    private String authority;

    /**
     * 转换 RoleAuthority
     *
     * @return RoleAuthority
     */
    public RoleAuthority toRoleAuthority() {
        return new RoleAuthority(name, authority);
    }
}
