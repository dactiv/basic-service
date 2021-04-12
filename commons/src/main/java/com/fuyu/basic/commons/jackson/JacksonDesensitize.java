package com.fuyu.basic.commons.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 敏感数据加 * 的 json 序列化实现
 *
 * @author maurice
 */
public class JacksonDesensitize extends JsonSerializer<String> {

    /**
     * 默认倍数值
     */
    private static final int DEFAULT_MULTIPLE_VALUE = 2;
    /**
     * 默认脱敏符号
     */
    private static final String DEFAULT_DESENSITIZE_SYMBOL = "*";

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        int avgLength = s.length() / DEFAULT_MULTIPLE_VALUE;

        if (avgLength == 1) {
            jsonGenerator.writeString(DEFAULT_DESENSITIZE_SYMBOL);
        }

        String left = StringUtils.substring(s, 0, avgLength);
        String right = StringUtils.substring(s, avgLength, s.length());

        // 左脱敏
        String leftPad = serializeString(left, true);
        // 有脱敏
        String rightPad = serializeString(right, false);

        jsonGenerator.writeString(leftPad + rightPad);
    }

    /**
     * 序列化字符串
     *
     * @param s    字符串
     * @param left 是否左边脱敏，true 是，否则 false
     * @return 脱敏后的字符串
     */
    private String serializeString(String s, boolean left) {

        int avgLength = s.length() / DEFAULT_MULTIPLE_VALUE;

        if (avgLength == 1) {
            return s;
        }

        String result;

        if (left) {
            String temp = StringUtils.substring(s, 0, avgLength);
            result = StringUtils.rightPad(temp, s.length(), DEFAULT_DESENSITIZE_SYMBOL);
        } else {

            int index = 0;

            if (s.length() > DEFAULT_MULTIPLE_VALUE) {
                index = s.length() / DEFAULT_MULTIPLE_VALUE;
            }

            String temp = StringUtils.substring(s, index, s.length());
            result = StringUtils.leftPad(temp, s.length() - 1, DEFAULT_DESENSITIZE_SYMBOL);
        }

        return result;

    }
}
