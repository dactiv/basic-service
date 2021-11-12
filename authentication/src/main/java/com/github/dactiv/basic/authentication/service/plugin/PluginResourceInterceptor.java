package com.github.dactiv.basic.authentication.service.plugin;

import com.github.dactiv.basic.authentication.entity.Resource;

import java.util.List;

/**
 * 插件资源拦截器
 *
 * @author maurice.chen
 */
public interface PluginResourceInterceptor {

    /**
     * 是否支持插件实例
     *
     * @param instance 插件实例
     *
     * @return true 是，否则 false
     */
    boolean isSupport(PluginInstance instance);

    /**
     * 同步插件完成后的处理
     *
     * @param instance 插件实例
     * @param newResourceList 插件新资源集合
     */
    void postSyncPlugin(PluginInstance instance, List<Resource> newResourceList);
}
