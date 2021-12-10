package com.github.dactiv.basic.authentication.security.handler;

import com.github.dactiv.basic.authentication.domain.entity.AuthenticationInfoEntity;
import com.github.dactiv.basic.authentication.domain.entity.MemberUserEntity;
import com.github.dactiv.basic.authentication.receiver.ValidAuthenticationInfoReceiver;
import com.github.dactiv.basic.authentication.security.MemberUserDetailsService;
import com.github.dactiv.basic.authentication.security.MobileUserDetailsService;
import com.github.dactiv.basic.commons.Constants;
import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * json 形式的认证失败具柄实现
 *
 * @author maurice.chen
 */
@Component
public class CaptchaAuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final MobileUserDetailsService mobileAuthenticationService;

    private final CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler;

    private final AmqpTemplate amqpTemplate;

    public CaptchaAuthenticationSuccessResponse(MobileUserDetailsService mobileAuthenticationService,
                                                CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler,
                                                AmqpTemplate amqpTemplate) {
        this.mobileAuthenticationService = mobileAuthenticationService;
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.amqpTemplate = amqpTemplate;
    }

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();

        jsonAuthenticationFailureHandler.deleteAllowableFailureNumber(request);

        AuthenticationInfoEntity info = new AuthenticationInfoEntity();

        UserAgent device = DeviceUtils.getRequiredCurrentDevice(request);
        info.setDevice(device.toMap());

        if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {
            MobileUserDetails mobileUserDetails = Casts.cast(details);
            Map<String, Object> data = mobileAuthenticationService.createMobileAuthenticationResult(mobileUserDetails);
            result.setData(data);

            info.setUserId(Casts.cast(mobileUserDetails.getId(), Integer.class));
            info.setType(mobileUserDetails.getType());
        } else if (SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

            SecurityUserDetails userDetails = Casts.cast(details);

            String identified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

            if (StringUtils.isNotBlank(identified)) {

                Map<String, Object> data;

                if (StringUtils.equals(ResourceSourceEnum.USER_CENTER.toString(), userDetails.getType())) {
                    Boolean isNew = Casts.cast(
                            request.getAttribute(MemberUserDetailsService.DEFAULT_IS_NEW_MEMBER_KEY_NAME)
                    );
                    data = createUserCenterDetailsData(identified, device, isNew, userDetails);
                } else {
                    data = createUserDetailsData(identified, device, userDetails);
                }

                result.setData(data);

            }

            info.setUserId(Casts.cast(userDetails.getId(), Integer.class));
            info.setType(userDetails.getType());
        }

        info.setIp(SpringMvcUtils.getIpAddress(request));
        info.setRetryCount(0);

        amqpTemplate.convertAndSend(
                Constants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE,
                ValidAuthenticationInfoReceiver.DEFAULT_QUEUE_NAME,
                info
        );
    }

    private Map<String, Object> createUserDetailsData(String identified,
                                                      UserAgent device,
                                                      SecurityUserDetails userDetails) {

        MobileUserDetails mobileUserDetails = mobileAuthenticationService.createMobileUserDetails(
                userDetails,
                identified,
                device
        );

        mobileUserDetails.setResourceAuthorities(userDetails.getResourceAuthorities());
        mobileUserDetails.setRoleAuthorities(userDetails.getRoleAuthorities());

        Map<String, Object> data = mobileAuthenticationService.createMobileAuthenticationResult(mobileUserDetails);

        data.put(CaptchaAuthenticationFailureResponse.DEFAULT_TYPE_PARAM_NAME, userDetails.getType());

        return data;
    }

    private Map<String, Object> createUserCenterDetailsData(String identified,
                                                            UserAgent device,
                                                            Boolean isNew,
                                                            SecurityUserDetails userDetails) {
        Integer userId = Casts.cast(userDetails.getId());
        MemberUserEntity memberUser = mobileAuthenticationService.getMemberUserService().get(userId);

        MobileUserDetails mobileUserDetails = mobileAuthenticationService.createMobileUserDetails(
                memberUser,
                identified,
                device
        );

        mobileUserDetails.setResourceAuthorities(userDetails.getResourceAuthorities());
        mobileUserDetails.setRoleAuthorities(userDetails.getRoleAuthorities());

        Map<String, Object> data = mobileAuthenticationService.createMobileAuthenticationResult(mobileUserDetails);

        if (Objects.nonNull(isNew)) {
            data.put(MemberUserDetailsService.DEFAULT_IS_NEW_MEMBER_KEY_NAME, isNew);
        }

        return data;
    }
}
