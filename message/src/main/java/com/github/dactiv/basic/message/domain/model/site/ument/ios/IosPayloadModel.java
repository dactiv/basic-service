package com.github.dactiv.basic.message.domain.model.site.ument.ios;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 友盟 ios payload 实体
 *
 * @author maurice
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IosPayloadModel {

    private IosPayloadApsModel aps;

    public IosPayloadModel() {

    }

    public IosPayloadApsModel getAps() {
        return aps;
    }

    public void setAps(IosPayloadApsModel aps) {
        this.aps = aps;
    }
}
