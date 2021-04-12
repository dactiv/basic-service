package com.fuyu.basic.support.spring.web.mobile;

import javax.servlet.http.HttpServletRequest;

/**
 * 设备解析器
 *
 * @author maurice
 */
public interface DeviceResolver {

    /**
     * 解析并得到设备
     *
     * @param request HttpServletRequest
     * @return 设备信息
     */
    default Device resolveDevice(HttpServletRequest request) {
        return null;
    }

}
