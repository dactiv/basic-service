package com.github.dactiv.basic.captcha.service.picture;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 图片验证码描述实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class PictureEntity {

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
