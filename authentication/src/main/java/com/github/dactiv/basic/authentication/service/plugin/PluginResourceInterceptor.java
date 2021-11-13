package com.github.dactiv.basic.authentication.service.plugin;

import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.framework.nacos.event.NacosService;

import java.util.List;

/**
 * 插件资源拦截器
 *
 * @author maurice.chen
 */
public interface PluginResourceInterceptor {

    /**
     * 同步插件完成后的处理
     *
     * @param instance 插件实例
     * @param newResourceList 插件新资源集合
     */
    void postSyncPlugin(PluginInstance instance, List<Resource> newResourceList);

    /**
     * 禁用应用资源后的处理
     *
     * @param nacosService nacos 服务
     */
    default void postDisabledApplicationResource(NacosService nacosService) {

    }
}
