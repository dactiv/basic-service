package com.fuyu.basic.support.spring.web.mobile;

/**
 * 设备信息的精简版实现
 *
 * @author maurice
 */
public class LiteDevice implements Device {

    public static final LiteDevice NORMAL_INSTANCE = new LiteDevice(DeviceType.NORMAL);

    public static final LiteDevice MOBILE_INSTANCE = new LiteDevice(DeviceType.MOBILE);

    public static final LiteDevice TABLET_INSTANCE = new LiteDevice(DeviceType.TABLET);

    private final DeviceType deviceType;

    private final DevicePlatform devicePlatform;

    @Override
    public DevicePlatform getDevicePlatform() {
        return this.devicePlatform;
    }

    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    /**
     * 创建设备类型为 NORMAL 且设备平台为 UNKNOWN 的 LiteDevice
     */
    public LiteDevice() {
        this(DeviceType.NORMAL, DevicePlatform.UNKNOWN);
    }

    /**
     * 使用 DevicePlatform UNKNOWN 创建一个 LiteDevice
     *
     * @param deviceType 设备的类型，即NORMAL，MOBILE，TABLET
     */
    public LiteDevice(DeviceType deviceType) {
        this(deviceType, DevicePlatform.UNKNOWN);
    }

    /**
     * 创建一个 LiteDevice
     *
     * @param deviceType     设备的类型，即NORMAL，MOBILE，TABLET
     * @param devicePlatform 设备平台，即IOS或ANDROID
     */
    public LiteDevice(DeviceType deviceType, DevicePlatform devicePlatform) {
        this.deviceType = deviceType;
        this.devicePlatform = devicePlatform;
    }

    @Override
    public boolean isNormal() {
        return this.deviceType == DeviceType.NORMAL;
    }

    @Override
    public boolean isMobile() {
        return this.deviceType == DeviceType.MOBILE;
    }

    @Override
    public boolean isTablet() {
        return this.deviceType == DeviceType.TABLET;
    }

    public static Device from(DeviceType deviceType, DevicePlatform devicePlatform) {
        return new LiteDevice(deviceType, devicePlatform);
    }

    @Override
    public String toString() {
        return "[LiteDevice type" + "=" + this.deviceType + "]";
    }
}
