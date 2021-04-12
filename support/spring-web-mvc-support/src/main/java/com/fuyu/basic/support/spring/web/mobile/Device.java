package com.fuyu.basic.support.spring.web.mobile;

import java.io.Serializable;

/***
 * 设备信息接口,用于判断访问时使用什么设备信息
 *
 * @author maurice
 */
public interface Device extends Serializable {

    /**
     * 判断此设备是否普通设备。
     *
     * @return 如果此设备不是移动设备或平板电脑设备，则为 true。
     */
    default boolean isNormal() {
        return true;
    }

    /**
     * 判断是否移动设备
     *
     * @return 如果此设备是移动设备（例如Apple iPhone或Nexus One Android），则为 true
     */
    default boolean isMobile() {
        return false;
    }

    /**
     * 判断设备是平板设备.
     *
     * @return 如果此设备是平板设备（例如Apple iPad或Motorola Xoom），则为true。
     */
    default boolean isTablet() {
        return false;
    }

    /**
     * 获取设备平台
     *
     * @return 设备平台
     */
    default DevicePlatform getDevicePlatform() {
        return DevicePlatform.UNKNOWN;
    }

}
