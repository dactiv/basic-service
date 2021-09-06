package com.github.dactiv.basic.authentication.service.plugin;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 插件实例
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PluginInstance extends Instance {

    private static final long serialVersionUID = 6418529914611005984L;

    /**
     * 版本号
     */
    private Version version;

    /**
     * info 信息
     */
    private Map<String, Object> info;

}
