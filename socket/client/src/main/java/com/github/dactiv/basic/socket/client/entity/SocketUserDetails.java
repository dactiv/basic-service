package com.github.dactiv.basic.socket.client.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.basic.socket.client.enumerate.ConnectStatus;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.basjes.parse.useragent.UserAgent;

import java.util.Date;
import java.util.Objects;

/**
 * spring security socket 用户明细实现
 *
 * @author maurice
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SocketUserDetails extends MobileUserDetails {

    public static final String DEFAULT_TYPE = "SocketUser";

    public static final String NICKNAME_FIELD = "nickname";

    private static final long serialVersionUID = 5604124001415380826L;

    /**
     * 创建时间
     */
    private Date creationTime;

    /**
     * 链接状态
     *
     * @see ConnectStatus
     */
    private Integer connectStatus = ConnectStatus.Disconnected.getValue();

    /**
     * 链接 socket 服务器的 ip 地址
     */
    @JsonIgnore
    private String socketServerIp;

    /**
     * 链接 socket 服务器的端口
     */
    @JsonIgnore
    private Integer port;

    /**
     * 最后链接时间
     */
    private Date connectTime;

    /**
     * socket 用户明细实现
     */
    public SocketUserDetails() {
        setType(DEFAULT_TYPE);
    }

    /**
     * socket 用户明细实现
     *
     * @param connectStatus 链接状态
     */
    public SocketUserDetails(Integer connectStatus) {
        this.connectStatus = connectStatus;
        setType(DEFAULT_TYPE);
    }

    /**
     * socket 用户明细实现
     *
     * @param id               用户 id
     * @param username         登录账户
     * @param password         密码
     * @param deviceIdentified 设备唯一是被
     * @param device           设备
     */
    public SocketUserDetails(Integer id,
                             String username,
                             String password,
                             String deviceIdentified,
                             UserAgent device) {

        super(id, username, password, deviceIdentified, device);
        setType(DEFAULT_TYPE);

    }
}
