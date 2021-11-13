package com.github.dactiv.basic.authentication.service.plugin.support;

import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.entity.Group;
import com.github.dactiv.basic.authentication.entity.Resource;
import com.github.dactiv.basic.authentication.service.AuthorizationService;
import com.github.dactiv.basic.authentication.service.plugin.PluginInstance;
import com.github.dactiv.basic.authentication.service.plugin.PluginResourceInterceptor;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.nacos.event.NacosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 同步超级管理员组资源的拦截器实现
 *
 * @author maurice.chen
 */
@Component
public class SyncAdminGroupResourceInterceptor implements PluginResourceInterceptor {

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private AuthorizationService authorizationService;

    @Override
    public void postSyncPlugin(PluginInstance instance, List<Resource> newResourceList) {
        authorizationService.deleteAuthorizationCache();

        Group group = authorizationService.getGroup(applicationConfig.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }

        List<String> newResourceIds = newResourceList
                .stream()
                .filter(r -> r.getSources().stream().anyMatch(s -> group.getSources().contains(s)))
                .map(IdEntity::getId)
                .collect(Collectors.toList());
        // 覆盖当前应用的资源
        group.getResourceMap().put(instance.getServiceName(), newResourceIds);
        authorizationService.saveGroup(group);

    }

    @Override
    public void postDisabledApplicationResource(NacosService nacosService) {
        Group group = authorizationService.getGroup(applicationConfig.getAdminGroupId());

        // 如果配置了管理员组 线删除同步一次管理员資源
        if (Objects.isNull(group)) {
            return;
        }
        // 移除当前应用的资源
        group.getResourceMap().remove(nacosService.getName());
        authorizationService.saveGroup(group);
    }
}
