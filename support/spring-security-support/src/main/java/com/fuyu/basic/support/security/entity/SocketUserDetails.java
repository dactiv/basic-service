package com.fuyu.basic.support.security.entity;

import com.fuyu.basic.support.security.enumerate.ConnectStatus;
import com.fuyu.basic.support.security.enumerate.ResourceSource;
import com.fuyu.basic.support.spring.web.mobile.Device;

/**
 * spring security socket 用户明细实现
 *
 * @author maurice
 */
public class SocketUserDetails extends MobileUserDetails {

    /**
     * 链接状态
     */
    private Integer connectStatus = ConnectStatus.Disconnected.getValue();

    /**
     * socket 用户明细实现
     */
    public SocketUserDetails() {
        setType(ResourceSource.SocketUser.toString());
    }

    /**
     * socket 用户明细实现
     *
     * @param connectStatus 链接状态
     */
    public SocketUserDetails(Integer connectStatus) {
        this.connectStatus = connectStatus;
        setType(ResourceSource.SocketUser.toString());
    }

    /**
     * socket 用户明细实现
     *
     * @param id               用户 id
     * @param username         登录账户
     * @param password         密码
     * @param deviceIdentified 设备唯一是被
     * @param device           设备
     * @param connectStatus    链接状态
     */
    public SocketUserDetails(Integer id, String username, String password, String deviceIdentified, Device device, Integer connectStatus) {
        super(id, username, password, deviceIdentified, device);
        this.connectStatus = connectStatus;
        setType(ResourceSource.SocketUser.toString());
    }
    /**
     * 获取链接状态
     *
     * @return 链接状态
     */
    public Integer getConnectStatus() {
        return connectStatus;
    }

    /**
     * 设置链接状态
     *
     * @param connectStatus 链接状态
     */
    public void setConnectStatus(Integer connectStatus) {
        this.connectStatus = connectStatus;
    }

}
