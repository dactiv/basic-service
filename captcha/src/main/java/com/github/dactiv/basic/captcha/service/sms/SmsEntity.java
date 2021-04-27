package com.github.dactiv.basic.captcha.service.sms;

import com.github.dactiv.basic.captcha.service.SimpleMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 短信验证码描述实体
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SmsEntity extends SimpleMessageType implements Serializable {

    private static final long serialVersionUID = -1235954873943241073L;
    /**
     * 手机号码
     */
    @Pattern(
            regexp = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$",
            message = "手机号码格式错误"
    )
    @NotBlank(message = "手机号码不能为空")
    private String phoneNumber;

}
