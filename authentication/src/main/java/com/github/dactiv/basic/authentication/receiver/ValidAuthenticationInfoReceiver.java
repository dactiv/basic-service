package com.github.dactiv.basic.authentication.receiver;

import com.github.dactiv.basic.authentication.config.ApplicationConfig;
import com.github.dactiv.basic.authentication.security.ip.IpResolver;
import com.github.dactiv.basic.authentication.service.AuthenticationInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 验证认证信息 MQ 接收者
 *
 * @author maurice.chen
 */
@Slf4j
/*@Component*/
public class ValidAuthenticationInfoReceiver {

    public static final String DEFAULT_QUEUE_NAME = "authentication.valid.info";

    private final AuthenticationInfoService authenticationInfoService;

    private final ApplicationConfig config;

    private final List<IpResolver> ipResolvers;

    public ValidAuthenticationInfoReceiver(AuthenticationInfoService authenticationInfoService,
                                           ObjectProvider<IpResolver> ipResolverProvider,
                                           ApplicationConfig config) {
        this.authenticationInfoService = authenticationInfoService;
        this.ipResolvers = ipResolverProvider.orderedStream().collect(Collectors.toList());
        this.config = config;
    }

    /*@RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DEFAULT_QUEUE_NAME, durable = "true"),
                    exchange = @Exchange(value = SystemConstants.SYS_AUTHENTICATION_RABBITMQ_EXCHANGE),
                    key = DEFAULT_QUEUE_NAME
            )
    )
    public void validAuthenticationInfo(@Payload AuthenticationInfoEntity info,
                                        Channel channel,
                                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        IpRegionMeta ipRegionMeta = info.getIpRegion();
        Optional<IpResolver> ipResolver = ipResolvers
                .stream()
                .filter(resolver -> resolver.isSupport(config.getIpResolverType()))
                .findFirst();

        if (ipResolver.isPresent()) {
            try {
                IpRegionMeta source = ipResolver.get().getIpRegionMeta(ipRegionMeta.getIpAddress());
                info.setIpRegion(source);
            } catch (Exception e) {
                log.error("执行 ip 解析出错", e);
            }
        } else {
            log.warn("找不到类型为 [" + config.getIpResolverType() + "] 的 ip 解析器");
        }

        authenticationInfoService.save(info);
        authenticationInfoService.validAuthenticationInfo(info);

        channel.basicAck(tag, false);

    }*/
}
