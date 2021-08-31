package com.github.dactiv.basic.authentication.service.security.handler;

import com.github.dactiv.basic.authentication.entity.AuthenticationInfo;
import com.github.dactiv.basic.authentication.entity.MemberUser;
import com.github.dactiv.basic.authentication.service.AuthenticationService;
import com.github.dactiv.basic.authentication.service.security.MemberUserDetailsService;
import com.github.dactiv.basic.authentication.service.security.MobileUserDetailsService;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.framework.spring.security.entity.MobileUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mobile.Device;
import com.github.dactiv.framework.spring.web.mobile.DeviceUtils;
import com.github.dactiv.framework.spring.web.mobile.LiteDeviceResolver;
import com.github.dactiv.framework.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MobileUserDetailsService mobileAuthenticationService;

    @Autowired
    private CaptchaAuthenticationFailureResponse jsonAuthenticationFailureHandler;

    @Autowired
    private AuthenticationService authenticationService;

    private final LiteDeviceResolver deviceResolver = new LiteDeviceResolver();

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();

        jsonAuthenticationFailureHandler.deleteAllowableFailureNumber(request);

        if (SecurityUserDetails.class.isAssignableFrom(details.getClass())) {

            SecurityUserDetails userDetails = Casts.cast(details);

            if (StringUtils.equals(userDetails.getType(), ResourceSource.UserCenter.toString())) {

                String identified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

                if (StringUtils.isNotEmpty(identified)) {

                    MemberUser memberUser = mobileAuthenticationService
                            .getUserService()
                            .getMemberUser(Casts.cast(userDetails.getId()));

                    MobileUserDetails mobileUserDetails = mobileAuthenticationService.createMobileUserDetails(
                            memberUser,
                            identified,
                            request
                    );

                    mobileUserDetails.setResourceAuthorities(userDetails.getResourceAuthorities());
                    mobileUserDetails.setRoleAuthorities(userDetails.getRoleAuthorities());

                    Map<String, Object> data = mobileAuthenticationService
                            .createMobileAuthenticationResult(mobileUserDetails);

                    Object isNew = request.getAttribute(MemberUserDetailsService.DEFAULT_IS_NEW_MEMBER_KEY_NAME);

                    if (Objects.nonNull(isNew)) {
                        data.put(MemberUserDetailsService.DEFAULT_IS_NEW_MEMBER_KEY_NAME, isNew);
                    }

                    result.setData(data);

                }
            }

            Device device = deviceResolver.resolveDevice(request);

            AuthenticationInfo info = new AuthenticationInfo();

            info.setDevice(device.getDevicePlatform().toString());
            info.setUserId(Casts.cast(userDetails.getId(), Integer.class));
            info.setType(userDetails.getType());
            info.setIp(SpringMvcUtils.getIpAddress(request));
            info.setRetryCount(0);
            // FIXME 省市县没有通过 ip 分析后进行赋值，如果有接口，节点改成 MQ 形式保存。
            authenticationService.saveAuthenticationInfo(info);

            authenticationService.validAuthenticationInfo(info);

        }

        if (MobileUserDetails.class.isAssignableFrom(details.getClass())) {

            MobileUserDetails mobileUserDetails = Casts.cast(details);

            Map<String, Object> data = mobileAuthenticationService.createMobileAuthenticationResult(mobileUserDetails);

            result.setData(data);
        }
    }
}
