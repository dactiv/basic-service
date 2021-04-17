package com.github.dactiv.basic.message.service.support.site.umeng.ios;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 友盟 ios payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayload {

    private IosPayloadAps aps;

    public IosPayload() {

    }

    public IosPayloadAps getAps() {
        return aps;
    }

    public void setAps(IosPayloadAps aps) {
        this.aps = aps;
    }
}
