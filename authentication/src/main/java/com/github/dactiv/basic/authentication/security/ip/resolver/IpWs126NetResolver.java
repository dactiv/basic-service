package com.github.dactiv.basic.authentication.security.ip.resolver;

import com.github.dactiv.basic.authentication.domain.meta.IpRegionMeta;
import com.github.dactiv.basic.authentication.security.ip.IpResolver;
import com.github.dactiv.framework.commons.Casts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ip.ws.126.net 的 ip 解析器实现
 *
 * @author maurice.chen
 */
@Slf4j
@Component
public class IpWs126NetResolver implements IpResolver {

    public static final String DEFAULT_TYPE = "ip.ws.126.net";

    private final RestTemplate restTemplate;

    public IpWs126NetResolver(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public IpRegionMeta getIpRegionMeta(String ipAddress) {

        IpRegionMeta result = IpRegionMeta.of(ipAddress);
        String url = "https://" + DEFAULT_TYPE + "/ipquery?ip=" + ipAddress;
        ResponseEntity<String> body = restTemplate.getForEntity(url, String.class);

        if (!HttpStatus.OK.equals(body.getStatusCode())) {
            log.warn("调用远程服务 ["+ url +"] 响应错误, 无法解析 ip.");
        } else {
            String text = body.getBody();

            String content = StringUtils.substringBetween(text, Casts.PATH_VARIABLE_SYMBOL_START, Casts.PATH_VARIABLE_SYMBOL_END);
            content = RegExUtils.replaceAll(content, " ", "");
            content = StringUtils.replace(content, IpRegionMeta.CITY_NAME, "\"" + IpRegionMeta.CITY_NAME + "\"");
            content = StringUtils.replace(content, IpRegionMeta.PROVINCE_NAME, "\"" + IpRegionMeta.PROVINCE_NAME + "\"");

            String json = Casts.PATH_VARIABLE_SYMBOL_START + content + Casts.PATH_VARIABLE_SYMBOL_END;
            //noinspection unchecked
            Map<String, Object> object = Casts.readValue(json, Map.class);
            result.setProvince(object.getOrDefault(IpRegionMeta.PROVINCE_NAME, StringUtils.EMPTY).toString());
            result.setCity(object.getOrDefault(IpRegionMeta.CITY_NAME, StringUtils.EMPTY).toString());
        }

        return result;
    }

    @Override
    public boolean isSupport(String type) {
        return DEFAULT_TYPE.equals(type);
    }
}
