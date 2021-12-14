package com.github.dactiv.basic.socket.server.domain;

import com.github.dactiv.basic.socket.server.domain.meta.BasicMessageMeta;
import com.github.dactiv.framework.commons.page.ScrollPage;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 全局消息分页
 *
 * @author maurice.chen
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GlobalMessagePage extends ScrollPage<BasicMessageMeta.FileMessage> {

    private static final long serialVersionUID = -321127303631279127L;

    /**
     * 最后发送消息
     */
    private String lastMessage;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 全局消息分页
     *
     * @param pageRequest 分页请求
     * @param elements    分页元素集合
     */
    public GlobalMessagePage(ScrollPageRequest pageRequest,
                             List<BasicMessageMeta.FileMessage> elements) {
        super(pageRequest, elements);
    }

    /**
     * 创建一个全局消息分页
     *
     * @param pageRequest 分页请求
     * @param elements    分页元素集合
     *
     * @return 全局消息分页
     */
    public static GlobalMessagePage of(ScrollPageRequest pageRequest,
                                       List<BasicMessageMeta.FileMessage> elements) {
        return new GlobalMessagePage(pageRequest, elements);
    }

}
