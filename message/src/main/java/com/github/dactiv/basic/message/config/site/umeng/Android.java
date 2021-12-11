package com.github.dactiv.basic.message.config.site.umeng;

import com.github.dactiv.basic.message.enumerate.MessageTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 安卓配置信息实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
public class Android {

    private String appKey;

    private String secretKey;

    private boolean push;

    private String activity;

    private List<MessageTypeEnum> ignoreActivityType = new ArrayList<>();

}
