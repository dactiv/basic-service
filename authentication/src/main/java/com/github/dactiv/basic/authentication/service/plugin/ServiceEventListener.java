package com.github.dactiv.basic.authentication.service.plugin;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.TimeProperties;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 服务事件监听实现
 *
 * @author maurice.chen
 */
@Data
@Slf4j
@RequiredArgsConstructor(staticName = "of")
public class ServiceEventListener implements EventListener {

    /**
     * 创建时间
     */
    private Date creationTime = new Date();
    /**
     * 过去时间
     */
    @NonNull
    private TimeProperties expirationTime;

    /**
     * 服务信息
     */
    @NonNull
    private Service service;

    /**
     * 插件資源服务
     */
    @NonNull
    private PluginResourceService pluginResourceService;

    @Override
    public void onEvent(Event event) {

        if (!NamingEvent.class.isAssignableFrom(event.getClass())) {
            return;
        }

        NamingEvent namingEvent = Casts.cast(event);

        if (namingEvent.getInstances().isEmpty()) {
            long seconds = getPluginResourceService().getProperties().getExpirationTime().toSeconds();
            log.info("服务 [" + service.getName() + "] 的实例已全部下线" + seconds + "后移除对该服务的订阅");
            pluginResourceService.disabledApplicationResource(namingEvent.getServiceName());
            return;
        }

        log.info("服务 [" + service.getName() + "] 发生变化，执行同步插件資源。");

        pluginResourceService.syncPluginResource(
                namingEvent.getGroupName(),
                namingEvent.getServiceName(),
                namingEvent.getInstances()
        );
    }

    /**
     * 是否过期
     *
     * @return true 是，否则 false
     */
    public boolean isExpired() {
        return new Date().getTime() - creationTime.getTime() > expirationTime.toMillis();
    }
}
