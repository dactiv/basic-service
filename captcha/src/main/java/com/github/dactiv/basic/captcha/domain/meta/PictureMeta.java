package com.github.dactiv.basic.captcha.domain.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 图片验证码描述实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class PictureMeta implements Serializable {

    private static final long serialVersionUID = 2309714787571972942L;
    /**
     * 宽度
     */
    private int width;

    /**
     * 高度
     */
    private int height;

    /**
     * 验证码长度
     */
    private int codeLength;

    /**
     * 字体样式
     */
    private String fontStyle;
}
