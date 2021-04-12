package com.fuyu.basic.commons;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;


/**
 * 服务信息实体
 *
 * @author maurice.chen
 */
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 8585738456959644815L;

    /**
     * 默认的应用名称 key 名称
     */
    public static final String DEFAULT_APPLICATION_NAME_KEY = "name";
    /**
     * 默认的应用组 key 名称
     */
    public static final String DEFAULT_GROUP_ID_KEY = "groupId";
    /**
     * 默认的 artifact key 名称
     */
    public static final String DEFAULT_ARTIFACT_ID_KEY = "artifactId";
    /**
     * 默认的版本 key 名称
     */
    public static final String DEFAULT_VERSION_KEY = "version";

    private String service;

    private Version version;

    private Map<String, Object> info;

    private LocalDateTime creationTime = LocalDateTime.now();

    public ServiceInfo() {
    }

    public ServiceInfo(String service, Version version, Map<String, Object> info) {
        this.service = service;
        this.version = version;
        this.info = info;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServiceInfo serviceInfo = (ServiceInfo) o;

        return Objects.equals(toString(), serviceInfo.toString());
    }

    @Override
    public String toString() {
        return "ServiceInfo{service='" + service + "'" + ", version=" + version + '}';
    }

    public static ServiceInfo build(String service, Map<String, Object> info) {

        Version version = null;

        if (info.containsKey(DEFAULT_VERSION_KEY)) {

            String versionString = info.get(DEFAULT_VERSION_KEY).toString();

            String groupId = null;
            String artifactId = null;

            if (info.containsKey(DEFAULT_GROUP_ID_KEY)) {
                groupId = info.get(DEFAULT_GROUP_ID_KEY).toString();
            }

            if (info.containsKey(DEFAULT_ARTIFACT_ID_KEY)) {
                artifactId = info.get(DEFAULT_ARTIFACT_ID_KEY).toString();
            }

            version = VersionUtil.parseVersion(versionString, groupId, artifactId);
        }

        String name = info.containsKey(DEFAULT_APPLICATION_NAME_KEY)
                ? info.get(DEFAULT_APPLICATION_NAME_KEY).toString()
                : service;

        return new ServiceInfo(name, version, info);
    }
}
