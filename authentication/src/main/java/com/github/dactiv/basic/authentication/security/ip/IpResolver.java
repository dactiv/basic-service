package com.github.dactiv.basic.authentication.security.ip;

import com.github.dactiv.basic.authentication.domain.meta.IpRegionMeta;

/**
 * ip 解析器
 *
 * @author maurice.chen
 */
public interface IpResolver {

    /**
     * 获取 id 区域元数据
     *
     * @param ipAddress ip 地址
     *
     * @return id 区域元数据
     */
    IpRegionMeta getIpRegionMeta(String ipAddress);

    /**
     * 是否支持类型
     *
     * @param type ip 解析器类型
     *
     * @return true 是，否则 false
     */
    boolean isSupport(String type);
}
