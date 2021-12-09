package com.github.dactiv.basic.message.domain.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.basic.message.domain.AttachmentMessage;
import com.github.dactiv.basic.message.domain.entity.AttachmentEntity;
import com.github.dactiv.basic.message.domain.entity.BasicMessageEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 站内信消息 body
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiteMessageBody extends BasicMessageEntity implements AttachmentMessage {

    private static final long serialVersionUID = 4341146261560926962L;

    /**
     * 标题
     */
    private String title;

    /**
     * /**
     * 接收方用户 id 集合
     */
    @NotEmpty
    private List<String> toUserIds;

    /**
     * 附件
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<AttachmentEntity> attachmentList = new ArrayList<>();

    /**
     * 是否推送消息：0.否，1.是
     */
    @NotNull
    private Integer isPush;

    /**
     * 数据
     */
    private Map<String, Object> data;
}
