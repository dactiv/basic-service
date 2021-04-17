package com.github.dactiv.basic.authentication.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.framework.commons.IntegerIdEntity;
import com.github.dactiv.framework.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.framework.spring.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>认证信息实体类</p>
 * <p>Table: tb_authentication_info - 认证信息表</p>
 *
 * @author maurice
 * @since 2020-06-01 09:22:12
 */
@Alias("authenticationInfo")
@Document(indexName = "authentication-info", type = ElasticsearchAuditEventRepository.DEFAULT_ES_TYPE_VALUE)
public class AuthenticationInfo extends IntegerIdEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Integer userId;

    /**
     * 用户类型
     */
    private String type;

    /**
     * ip 地址
     */
    private String ip;

    /**
     * 设备名称
     */
    private String device;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区域
     */
    private String area;

    /**
     * 同步 es 状态：0.处理中，1.成功，99.失败
     */
    private Integer syncStatus = ExecuteStatus.Processing.getValue();

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 备注
     */
    private String remark;

    /**
     * 认证信息表实体类
     */
    public AuthenticationInfo() {
    }

    /**
     * 获取用户 id
     *
     * @return Integer
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * 设置用户 id
     *
     * @param userId 用户 id
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * 获取用户类型
     *
     * @return String
     */
    public String getType() {
        return this.type;
    }

    /**
     * 设置用户类型
     *
     * @param type 用户类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取ip 地址
     *
     * @return String
     */
    public String getIp() {
        return this.ip;
    }

    /**
     * 设置ip 地址
     *
     * @param ip ip 地址
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * 获取设备名称
     *
     * @return String
     */
    public String getDevice() {
        return this.device;
    }

    /**
     * 设置设备名称
     *
     * @param device 设备名称
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * 获取省
     *
     * @return String
     */
    public String getProvince() {
        return this.province;
    }

    /**
     * 设置省
     *
     * @param province 省
     */
    public void setProvince(String province) {
        this.province = province;
    }

    /**
     * 获取市
     *
     * @return String
     */
    public String getCity() {
        return this.city;
    }

    /**
     * 设置市
     *
     * @param city 市
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * 获取区域
     *
     * @return String
     */
    public String getArea() {
        return this.area;
    }

    /**
     * 设置区域
     *
     * @param area 区域
     */
    public void setArea(String area) {
        this.area = area;
    }

    /**
     * 获取同步 es 状态：0.处理中，1.成功，99.失败
     *
     * @return 同步 es 状态：0.处理中，1.成功，99.失败
     */
    public Integer getSyncStatus() {
        return syncStatus;
    }

    /**
     * 设置同步 es 状态：0.处理中，1.成功，99.失败
     *
     * @param syncStatus 同步 es 状态：0.处理中，1.成功，99.失败
     */
    public void setSyncStatus(Integer syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * 获取重试次数
     *
     * @return 重试次数
     */
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount 重试次数
     */
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取唯一索引查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public Map<String, Object> getUniqueFilter() {
        Map<String, Object> filter = new LinkedHashMap<>();
        return filter;
    }

    // -------------------- 将代码新增添加在这里，以免代码重新生成后覆盖新增内容 -------------------- //


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthenticationInfo that = (AuthenticationInfo) o;

        return userId.equals(that.userId) &&
                type.equals(that.type) &&
                ip.equals(that.ip) &&
                device.equals(that.device) &&
                StringUtils.equals(province, that.province) &&
                StringUtils.equals(city, that.city) &&
                StringUtils.equals(area, that.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type, ip, device, province, city, area);
    }
}