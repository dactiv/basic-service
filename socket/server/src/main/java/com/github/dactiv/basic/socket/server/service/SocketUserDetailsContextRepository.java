package com.github.dactiv.basic.socket.server.service;

import com.github.dactiv.basic.socket.client.domain.SocketUserDetails;
import com.github.dactiv.framework.spring.security.authentication.DeviceIdContextRepository;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.web.device.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.context.SecurityContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * socket 用户明细上下文仓库实现
 *
 * @author maurice.chen
 */
public class SocketUserDetailsContextRepository extends DeviceIdContextRepository {

    public SocketUserDetailsContextRepository(AuthenticationProperties properties, RedissonClient redissonClient) {
        super(properties, redissonClient);
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(context.getAuthentication())) {
            return;
        }

        Object details = context.getAuthentication().getDetails();

        if (!SocketUserDetails.class.isAssignableFrom(details.getClass())) {
            return;
        }

        String token = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isBlank(token)) {
            return;
        }

        RBucket<SecurityContext> bucket = getSecurityContextBucket(token);
        setSecurityContext(context, bucket);

    }
}
