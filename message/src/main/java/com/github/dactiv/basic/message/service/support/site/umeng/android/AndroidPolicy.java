package com.github.dactiv.basic.message.service.support.site.umeng.android;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.dactiv.basic.message.service.support.site.umeng.Policy;

/**
 * 友盟安卓 Policy 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AndroidPolicy extends Policy {

    private Integer maxSendNum;

    public AndroidPolicy() {
    }

    public Integer getMaxSendNum() {
        return maxSendNum;
    }

    public void setMaxSendNum(Integer maxSendNum) {
        this.maxSendNum = maxSendNum;
    }
}
