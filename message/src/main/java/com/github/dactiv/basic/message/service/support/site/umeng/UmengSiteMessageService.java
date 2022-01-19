package com.github.dactiv.basic.message.service.support.site.umeng;

import com.github.dactiv.basic.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.basic.commons.feign.authentication.AuthenticationFeignClient;
import com.github.dactiv.basic.message.config.site.SiteConfig;
import com.github.dactiv.basic.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.basic.message.domain.meta.site.umeng.BasicMessageMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.PolicyMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.android.AndroidMessageMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.android.AndroidPayloadBodyMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.android.AndroidPayloadMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.android.AndroidPolicyMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.ios.IosPayloadApsAlertMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.ios.IosPayloadApsMeta;
import com.github.dactiv.basic.message.domain.meta.site.umeng.ios.IosPayloadMeta;
import com.github.dactiv.basic.message.enumerate.site.ument.UmengMessageTypeEnum;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.ErrorCodeException;
import nl.basjes.parse.useragent.UserAgent;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 友盟站内信消息服务
 *
 * @author maurice
 */
@Component
public class UmengSiteMessageService implements SiteMessageChannelSender {

    /**
     * 默认类型
     */
    public static final String DEFAULT_TYPE = "umeng";

    /**
     * 默认的用户别名类型名称
     */
    public static final String DEFAULT_USER_ALIAS_TYPE = "USER_ID";

    /**
     * 默认成功消息字段名称
     */
    public static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";

    private final SiteConfig config;

    private final RestTemplate restTemplate;

    private final AuthenticationFeignClient authenticationFeignClient;

    public UmengSiteMessageService(SiteConfig config, RestTemplate restTemplate, AuthenticationFeignClient authenticationFeignClient) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.authenticationFeignClient = authenticationFeignClient;
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RestResult<Map<String, Object>> sendSiteMessage(SiteMessageEntity message) {

        List<String> types = Arrays.asList(ResourceSourceEnum.MOBILE.toString(), ResourceSourceEnum.USER_CENTER.toString());

        // 获取最后一次认证信息
        Map<String, Object> info = authenticationFeignClient.getLastAuthenticationInfo(message.getToUserId(), types);

        if (MapUtils.isEmpty(info)) {
            return RestResult.of(
                    "id 为[" + message.getToUserId() + "]的用户找不到设备信息，无法发送站内信",
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()
            );
        }

        // 得到设备信息
        Map<String, Object> device = Casts.cast(info.get("device"), Map.class);

        BasicMessageMeta basicMessage = null;
        // 根据谁被信息构造基础消息对象
        if ("ANDROID".equals(device.get(UserAgent.OPERATING_SYSTEM_NAME))) {
            basicMessage = getAndroidMessage(message, UmengMessageTypeEnum.Customize);
        } else if ("IOS".equals(device.get(UserAgent.OPERATING_SYSTEM_NAME))) {
            basicMessage = getIosMessage(message, UmengMessageTypeEnum.Customize);
        }

        if (basicMessage == null) {
            return RestResult.of(
                    device + "设备不需要发送消息推送",
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase()
            );
        }

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = Casts.writeValueAsString(basicMessage);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        String sign = DigestUtils.md5Hex(("POST" + config.getUmeng().getUrl() + requestJson + basicMessage.getSecretKey()).getBytes(StandardCharsets.UTF_8));
        // 发送消息
        ResponseEntity<String> result = restTemplate.postForEntity(config.getUmeng().getUrl() + "?sign=" + sign, entity, String.class);
        // OK 为成功，否则失败
        if (result.getStatusCode().equals(HttpStatus.OK)) {

            Map<String, Object> resultBody = Casts.readValue(result.getBody(), Map.class);

            Map<String, Object> data = new LinkedHashMap<>();

            if (resultBody.containsKey(RestResult.DEFAULT_DATA_NAME)) {
                data = Casts.cast(resultBody.get(RestResult.DEFAULT_DATA_NAME), Map.class);
            }

            if (DEFAULT_SUCCESS_MESSAGE.equals(resultBody.get("ret"))) {

                return RestResult.of(
                        "id 为[" + message.getId() + "] 记录推送消息给 [" + message.getToUserId() + "] 的用户成功",
                        result.getStatusCodeValue(),
                        result.getStatusCode().getReasonPhrase(),
                        data
                );

            } else {
                return RestResult.of(
                        resultBody.get("ret").toString(),
                        result.getStatusCodeValue(),
                        ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                        data
                );
            }
        }

        return RestResult.of(
                result.getStatusCode().getReasonPhrase(),
                result.getStatusCodeValue(),
                ErrorCodeException.DEFAULT_EXCEPTION_CODE
        );
    }

    /**
     * 获取 ios 消息实体
     *
     * @param entity 站内信实体
     *
     * @return 基础消息实体
     */
    @SuppressWarnings("unchecked")
    public BasicMessageMeta getIosMessage(SiteMessageEntity entity, UmengMessageTypeEnum type) {
        BasicMessageMeta result = new BasicMessageMeta();

        result.setProductionMode(config.getUmeng().isProductionMode());
        result.setAppkey(config.getUmeng().getIos().getAppKey());
        result.setSecretKey(config.getUmeng().getIos().getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getToUserId().toString());

        IosPayloadMeta iosPayload = new IosPayloadMeta();

        IosPayloadApsMeta iosPayloadAps = new IosPayloadApsMeta();

        iosPayload.setAps(iosPayloadAps);

        IosPayloadApsAlertMeta iosPayloadApsAlert = new IosPayloadApsAlertMeta();

        iosPayloadApsAlert.setTitle(entity.getTitle());
        iosPayloadApsAlert.setBody(entity.getContent());

        iosPayloadAps.setAlert(iosPayloadApsAlert);

        Map<String, Object> payloadMap = Casts.convertValue(iosPayload, Map.class);

        payloadMap.putAll(entity.getMeta());
        //payloadMap.put("type", entity.getType());

        result.setPayload(payloadMap);

        PolicyMeta policy = new PolicyMeta();

        policy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(policy);

        result.setDescription("IOS");

        return result;
    }

    /**
     * 获取安卓消息实体
     *
     * @param entity 站内信实体
     *
     * @return 基础消息实体
     */
    public BasicMessageMeta getAndroidMessage(SiteMessageEntity entity, UmengMessageTypeEnum type) {

        AndroidMessageMeta result = new AndroidMessageMeta();

        result.setProductionMode(config.getUmeng().isProductionMode());
        result.setAppkey(config.getUmeng().getAndroid().getAppKey());
        result.setSecretKey(config.getUmeng().getAndroid().getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getToUserId().toString());

        AndroidPayloadMeta androidPayload = new AndroidPayloadMeta();

        androidPayload.setDisplayType("notification");

        androidPayload.getExtra().putAll(entity.getMeta());

        AndroidPayloadBodyMeta androidPayloadBody = new AndroidPayloadBodyMeta();

        androidPayloadBody.setTicker(entity.getContent());
        androidPayloadBody.setTitle(entity.getTitle());
        androidPayloadBody.setText(entity.getContent());

        androidPayloadBody.setAfterOpen("go_app");

        androidPayload.setBody(androidPayloadBody);
        result.setPayload(androidPayload);

        AndroidPolicyMeta androidPolicy = new AndroidPolicyMeta();

        androidPolicy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(androidPolicy);

        result.setDescription("ANDROID");

        if (!config.getUmeng().getAndroid().getIgnoreActivityType().contains(entity.getType())) {
            result.setMipush(config.getUmeng().getAndroid().isPush());
            result.setMiActivity(config.getUmeng().getAndroid().getActivity());
        }

        return result;
    }

    public Date getExpireTime(Date currentDate) {

        LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plus(config.getUmeng().getExpireTime().getValue(), config.getUmeng().getExpireTime().getUnit().toChronoUnit());

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
