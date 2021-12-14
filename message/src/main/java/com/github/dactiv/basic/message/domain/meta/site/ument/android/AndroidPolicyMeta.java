package com.github.dactiv.basic.message.domain.meta.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.basic.message.domain.meta.site.ument.PolicyMeta;

/**
 * 友盟安卓 Policy 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPolicyMeta extends PolicyMeta {

    private Integer maxSendNum;

    public AndroidPolicyMeta() {
    }

    public Integer getMaxSendNum() {
        return maxSendNum;
    }

    public void setMaxSendNum(Integer maxSendNum) {
        this.maxSendNum = maxSendNum;
    }
}
