package com.github.dactiv.basic.socket.server.service.chat.data;

import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.page.ScrollPage;
import com.github.dactiv.framework.commons.page.ScrollPageRequest;
import lombok.AllArgsConstructor;
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
public class GlobalMessagePage extends ScrollPage<GlobalMessage.FileMessage> {

    private static final long serialVersionUID = -321127303631279127L;

    /**
     * 时间范围
     */
    private List<Date> timeFrame;

    /**
     * 全局消息分页
     *
     * @param pageRequest 分页请求
     * @param elements    分页元素集合
     * @param timeFrame   时间范围
     */
    public GlobalMessagePage(ScrollPageRequest pageRequest,
                             List<GlobalMessage.FileMessage> elements,
                             List<Date> timeFrame) {
        super(pageRequest, elements);
        this.timeFrame = timeFrame;
    }

    /**
     * 创建一个全局消息分页
     *
     * @param pageRequest 分页请求
     * @param elements    分页元素集合
     * @param timeFrame   时间范围
     *
     * @return 全局消息分页
     */
    public static GlobalMessagePage of(ScrollPageRequest pageRequest,
                                       List<GlobalMessage.FileMessage> elements,
                                       List<Date> timeFrame) {
        return new GlobalMessagePage(pageRequest, elements, timeFrame);
    }

}
