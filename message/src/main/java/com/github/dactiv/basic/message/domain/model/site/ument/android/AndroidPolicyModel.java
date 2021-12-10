package com.github.dactiv.basic.message.domain.model.site.ument.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.basic.message.domain.model.site.ument.PolicyModel;

/**
 * 友盟安卓 Policy 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPolicyModel extends PolicyModel {

    private Integer maxSendNum;

    public AndroidPolicyModel() {
    }

    public Integer getMaxSendNum() {
        return maxSendNum;
    }

    public void setMaxSendNum(Integer maxSendNum) {
        this.maxSendNum = maxSendNum;
    }
}
