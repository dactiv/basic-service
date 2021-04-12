package com.fuyu.basic.captcha.service.picture;

/**
 * 图片验证码描述实体
 *
 * @author maurice
 */
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

    /**
     * 图片验证码描述实体
     */
    public PictureEntity() {
    }

    /**
     * 图片验证码描述实体
     *
     * @param width      宽度
     * @param height     高度
     * @param codeLength 验证码长度
     * @param fontStyle  字体样式
     */
    public PictureEntity(int width, int height, int codeLength, String fontStyle) {
        this.width = width;
        this.height = height;
        this.codeLength = codeLength;
        this.fontStyle = fontStyle;
    }

    /**
     * 获取宽度
     *
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * 设置宽度
     *
     * @param width 宽度
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * 获取高度
     *
     * @return 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * 设置高度
     *
     * @param height 高度
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * 获取验证码字符长度
     *
     * @return 验证码字符长度
     */
    public int getCodeLength() {
        return codeLength;
    }

    /**
     * 设置验证码字符长度
     *
     * @param codeLength 验证码字符长度
     */
    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    /**
     * 获取验证码的字体样式
     *
     * @return 验证码的字体样式
     */
    public String getFontStyle() {
        return fontStyle;
    }

    /**
     * 设置验证码的字体样式
     *
     * @param fontStyle 验证码的字体样式
     */
    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }
}
