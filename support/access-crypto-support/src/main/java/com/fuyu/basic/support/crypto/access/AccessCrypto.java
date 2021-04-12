package com.fuyu.basic.support.crypto.access;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fuyu.basic.commons.IntegerIdEntity;
import com.fuyu.basic.commons.enumerate.NameValueEnumUtils;
import com.fuyu.basic.commons.enumerate.support.DisabledOrEnabled;
import com.fuyu.basic.commons.enumerate.support.YesOrNo;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问加解密
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessCrypto extends IntegerIdEntity {

    private static final long serialVersionUID = -8441240266365812046L;

    public static final String DEFAULT_REQUEST_DECRYPT_FIELD_NAME = "requestDecrypt";

    public static final String DEFAULT_RESPONSE_ENCRYPT_FIELD_NAME = "responseEncrypt";

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    /**
     * 值
     */
    private String value;

    /**
     * 是否启用，1.是，0.否
     */
    private Integer enabled = DisabledOrEnabled.Enabled.getValue();

    /**
     * 是否请求加密，0.否，1.是
     */
    private Integer requestDecrypt = YesOrNo.Yes.getValue();

    /**
     * 是否响应加密，0.否，1.是
     */
    private Integer responseEncrypt = YesOrNo.No.getValue();

    /**
     * 加解密条件
     */
    private List<AccessCryptoPredicate> predicates = new ArrayList<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 访问加解密
     */
    public AccessCrypto() {
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /***
     * 获取值
     *
     * @return 值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 获取是否启用
     *
     * @return 1.是，0.否
     */
    public Integer getEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用
     *
     * @param enabled 1.是，0.否
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取是否启用名称
     *
     * @return 是否启用名称
     */
    public String getEnabledName() {
        return NameValueEnumUtils.getName(this.enabled, DisabledOrEnabled.class);
    }

    /**
     * 获取是否响应加密
     *
     * @return 0.否，1.是
     */
    public Integer getResponseEncrypt() {
        return responseEncrypt;
    }

    /**
     * 设置是否响应加密
     *
     * @param responseEncrypt 0.否，1.是
     */
    public void setResponseEncrypt(Integer responseEncrypt) {
        this.responseEncrypt = responseEncrypt;
    }

    /**
     * 获取是否响应加解密名称
     *
     * @return 是否响应加解密名称
     */
    public String getResponseEncryptName() {
        return NameValueEnumUtils.getName(this.responseEncrypt, YesOrNo.class);
    }

    /**
     * 获取是否请求加密
     *
     * @return 0.否，1.是
     */
    public Integer getRequestDecrypt() {
        return requestDecrypt;
    }

    /**
     * 设置是否请求加密
     *
     * @param requestDecrypt 0.否，1.是
     */
    public void setRequestDecrypt(Integer requestDecrypt) {
        this.requestDecrypt = requestDecrypt;
    }

    /**
     * 获取是否响应加解密名称
     *
     * @return 是否响应加解密名称
     */
    public String getRequestDecryptName() {
        return NameValueEnumUtils.getName(this.requestDecrypt, YesOrNo.class);
    }

    /**
     * 获取访问加解密条件
     *
     * @return 访问加解密条件
     */
    public List<AccessCryptoPredicate> getPredicates() {
        return predicates;
    }

    /**
     * 设置访问加解密条件
     *
     * @param predicates 访问加解密条件
     */
    public void setPredicates(List<AccessCryptoPredicate> predicates) {
        this.predicates = predicates;
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
     * 获取访问加解密名称
     *
     * @return 访问加解密名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置访问加解密名称
     *
     * @param name 访问加解密名称
     */
    public void setName(String name) {
        this.name = name;
    }

}
