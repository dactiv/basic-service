package com.github.dactiv.basic.captcha.service.picture.generator;

import com.github.dactiv.basic.captcha.service.picture.PictureCaptchaGenerator;
import com.github.dactiv.basic.captcha.service.picture.PictureEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;

/**
 * 简单的图片验证码生成器实现
 *
 * @author maurice
 */
@Component
public class SimplePictureCaptchaGenerator implements PictureCaptchaGenerator {
    /**
     * 默认最大的颜色值
     */
    private static final Integer DEFAULT_MAX_COLOR_VALUE = 255;

    /**
     * 默认的字体
     */
    private static final String DEFAULT_FONT_STYLE = "Comic Sans MS";

    private static final String DEFAULT_PICTURE_TYPE = "JPEG";

    /**
     * 默认的干扰线集合
     */
    private static final Color[] DEFAULT_INTERFERENCE_COLORS = new Color[] {
            Color.GREEN,
            Color.PINK,
            Color.MAGENTA,
            Color.LIGHT_GRAY,
            Color.ORANGE,
            Color.CYAN
    };

    /**
     * 默认的干扰线集合
     */
    private static final Color[] DEFAULT_FONT_COLORS = new Color[] {
            Color.BLUE,
            Color.RED,
            Color.BLACK
    };

    /**
     * 默认图片的宽度
     */
    private static final Integer DEFAULT_WIDTH = 100;

    /**
     * 默认图片的高度
     */
    private static final Integer DEFAULT_HEIGHT = 30;

    /**
     * 默认的验证码长度
     */
    private static final Integer DEFAULT_CODE_LENGTH = 6;

    /**
     * 默认的验证码映射表数组
     */
    private static final char[] DEFAULT_CODE_MAP_TABLE = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };


    @Override
    public String generateCaptcha(PictureEntity imgCaptcha, OutputStream os) throws Exception {
        if (imgCaptcha.getWidth() <= 0) {
            imgCaptcha.setWidth(DEFAULT_WIDTH);
        }

        if (imgCaptcha.getHeight() <= 0) {
            imgCaptcha.setHeight(DEFAULT_HEIGHT);
        }

        if (imgCaptcha.getCodeLength() <= 0) {
            imgCaptcha.setCodeLength(DEFAULT_CODE_LENGTH);
        }

        if (StringUtils.isBlank(imgCaptcha.getFontStyle())) {
            imgCaptcha.setFontStyle(DEFAULT_FONT_STYLE);
        }

        BufferedImage image = new BufferedImage(imgCaptcha.getWidth(), imgCaptcha.getHeight(), BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.getGraphics();
        //生成随机类
        Random random = new Random();
        // 设定背景色
        g.setColor(new Color(DEFAULT_MAX_COLOR_VALUE, DEFAULT_MAX_COLOR_VALUE, DEFAULT_MAX_COLOR_VALUE));
        g.fillRect(0, 0, imgCaptcha.getWidth(), imgCaptcha.getHeight());
        //设定字体
        g.setFont(new Font(imgCaptcha.getFontStyle(), Font.PLAIN, 18));
        // 随机产生168条干扰线，使图象中的认证码不易被其它程序探测到
        for (int i = 0; i < imgCaptcha.getWidth() * 2; i++) {

            g.setColor(DEFAULT_INTERFERENCE_COLORS[random.nextInt(DEFAULT_INTERFERENCE_COLORS.length)]);

            int x = random.nextInt(imgCaptcha.getWidth());
            int y = random.nextInt(imgCaptcha.getHeight());
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        //取随机产生的码
        StringBuilder strEnsure = new StringBuilder();
        //4代表4位验证码,如果要生成更多位的认证码,则加大数值
        for (int i = 0; i < imgCaptcha.getCodeLength(); ++i) {
            strEnsure.append(DEFAULT_CODE_MAP_TABLE[(int) (DEFAULT_CODE_MAP_TABLE.length * Math.random())]);
            // 将认证码显示到图象中
            g.setColor(DEFAULT_FONT_COLORS[random.nextInt(DEFAULT_FONT_COLORS.length)]);
            // 直接生成
            String str = strEnsure.substring(i, i + 1);
            // 设置随便码在背景图图片上的位置
            g.drawString(str, 5 + i * (imgCaptcha.getWidth() / imgCaptcha.getCodeLength()), (imgCaptcha.getHeight() / 2) + (13 / 2));
        }

        ImageIO.write(image, DEFAULT_PICTURE_TYPE, os);

        // 释放图形上下文
        g.dispose();

        return strEnsure.toString();
    }
}
