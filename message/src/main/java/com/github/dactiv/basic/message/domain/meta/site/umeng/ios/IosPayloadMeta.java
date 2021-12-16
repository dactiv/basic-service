package com.github.dactiv.basic.message.domain.meta.site.umeng.ios;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 友盟 ios payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadMeta {

    private IosPayloadApsMeta aps;

    public IosPayloadMeta() {

    }

    public IosPayloadApsMeta getAps() {
        return aps;
    }

    public void setAps(IosPayloadApsMeta aps) {
        this.aps = aps;
    }
}
