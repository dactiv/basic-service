package com.github.dactiv.basic.message.service.support.site.umeng;

import com.github.dactiv.basic.message.dao.entity.SiteMessage;
import com.github.dactiv.basic.message.service.AuthenticationService;
import com.github.dactiv.basic.message.service.support.site.SiteMessageChannelSender;
import com.github.dactiv.basic.message.service.support.site.umeng.android.AndroidMessage;
import com.github.dactiv.basic.message.service.support.site.umeng.android.AndroidPayload;
import com.github.dactiv.basic.message.service.support.site.umeng.android.AndroidPayloadBody;
import com.github.dactiv.basic.message.service.support.site.umeng.android.AndroidPolicy;
import com.github.dactiv.basic.message.service.support.site.umeng.ios.IosPayload;
import com.github.dactiv.basic.message.service.support.site.umeng.ios.IosPayloadAps;
import com.github.dactiv.basic.message.service.support.site.umeng.ios.IosPayloadApsAlert;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.spring.security.enumerate.ResourceSource;
import com.github.dactiv.framework.spring.web.mobile.DevicePlatform;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
@ConfigurationProperties("spring.site.message.umeng")
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
     * 接口调用地址
     */
    private String url;
    /**
     * 环境配置
     */
    private boolean productionMode;
    /**
     * 消息超时时间
     */
    private long expireTimeInSecond;
    /**
     * 安卓配置项
     */
    private AndroidProperties android;
    /**
     * ios 配置项
     */
    private IosProperties ios;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AuthenticationService authenticationService;

    public UmengSiteMessageService() {
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RestResult<Map<String, Object>> sendSiteMessage(SiteMessage message) throws Exception {

        List<String> types = Arrays.asList(ResourceSource.Mobile.toString(), ResourceSource.UserCenter.toString());

        // 获取最后一次认证信息
        Map<String, Object> info = authenticationService.getLastAuthenticationInfo(message.getToUserId(), types);

        if (MapUtils.isEmpty(info)) {
            return new RestResult<>(
                    "id 为[" + message.getToUserId() + "]的用户找不到设备信息，无法发送站内信",
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()
            );
        }

        // 得到设备信息
        String device = info.get("device").toString();

        BasicMessage basicMessage = null;
        // 根据谁被信息构造基础消息对象
        if (DevicePlatform.ANDROID.toString().equals(device)) {
            basicMessage = getAndroidMessage(message, MessageType.Customize);
        } else if (DevicePlatform.IOS.toString().equals(device)) {
            basicMessage = getIosMessage(message, MessageType.Customize);
        }

        if (basicMessage == null) {
            return new RestResult<>(
                    device + "设备不需要发送消息推送",
                    HttpStatus.NOT_FOUND.value(),
                    HttpStatus.NOT_FOUND.getReasonPhrase()
            );
        }

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = Casts.writeValueAsString(basicMessage);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        String sign = DigestUtils.md5Hex(("POST" + url + requestJson + basicMessage.getSecretKey()).getBytes(StandardCharsets.UTF_8));
        // 发送消息
        ResponseEntity<String> result = restTemplate.postForEntity(url + "?sign=" + sign, entity, String.class);
        // OK 为成功，否则失败
        if (result.getStatusCode().equals(HttpStatus.OK)) {

            Map<String, Object> resultBody = Casts.readValue(result.getBody(), Map.class);

            Map<String, Object> data = new LinkedHashMap<>();

            if (resultBody.containsKey("data")) {
                data = Casts.cast(resultBody.get("data"), Map.class);
            }

            if ("SUCCESS".equals(resultBody.get("ret"))) {

                return new RestResult<>(
                        "id 为[" + message.getId() + "] 记录推送消息给 [" + message.getToUserId() + "] 的用户成功",
                        result.getStatusCodeValue(),
                        result.getStatusCode().getReasonPhrase(),
                        data
                );

            } else {
                return new RestResult<>(
                        resultBody.get("ret").toString(),
                        result.getStatusCodeValue(),
                        RestResult.ERROR_EXECUTE_CODE,
                        data
                );
            }
        }

        return new RestResult<>(
                result.getStatusCode().getReasonPhrase(),
                result.getStatusCodeValue(),
                RestResult.ERROR_EXECUTE_CODE
        );
    }

    /**
     * 获取 ios 消息实体
     *
     * @param entity 站内信实体
     * @return 基础消息实体
     */
    @SuppressWarnings("unchecked")
    public BasicMessage getIosMessage(SiteMessage entity, MessageType type) {
        BasicMessage result = new BasicMessage();

        result.setProductionMode(productionMode);
        result.setAppkey(ios.getAppKey());
        result.setSecretKey(ios.getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getToUserId().toString());

        IosPayload iosPayload = new IosPayload();

        IosPayloadAps iosPayloadAps = new IosPayloadAps();

        iosPayload.setAps(iosPayloadAps);

        IosPayloadApsAlert iosPayloadApsAlert = new IosPayloadApsAlert();

        iosPayloadApsAlert.setTitle(entity.getTitle());
        iosPayloadApsAlert.setBody(entity.getContent());

        iosPayloadAps.setAlert(iosPayloadApsAlert);

        Map<String, Object> payloadMap = Casts.convertValue(iosPayload, Map.class);

        payloadMap.putAll(entity.getLink());
        payloadMap.put("type", entity.getType());

        result.setPayload(payloadMap);

        Policy policy = new Policy();

        policy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(policy);

        result.setDescription(entity.getType() + "-" + DevicePlatform.IOS.toString());

        return result;
    }

    /**
     * 获取安卓消息实体
     *
     * @param entity 站内信实体
     * @return 基础消息实体
     */
    public BasicMessage getAndroidMessage(SiteMessage entity, MessageType type) {

        AndroidMessage result = new AndroidMessage();

        result.setProductionMode(productionMode);
        result.setAppkey(android.getAppKey());
        result.setSecretKey(android.getSecretKey());
        result.setType(type.getName());
        result.setAliasType(DEFAULT_USER_ALIAS_TYPE);
        result.setAlias(entity.getToUserId().toString());

        AndroidPayload androidPayload = new AndroidPayload();

        androidPayload.setDisplayType("notification");

        androidPayload.getExtra().putAll(entity.getLink());

        AndroidPayloadBody androidPayloadBody = new AndroidPayloadBody();

        androidPayloadBody.setTicker(entity.getContent());
        androidPayloadBody.setTitle(entity.getTitle());
        androidPayloadBody.setText(entity.getContent());

        androidPayloadBody.setAfterOpen("go_app");

        /*try {
            androidPayloadBody.setActivity(objectMapper.writeValueAsString(entity.getLink()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }*/

        androidPayload.setBody(androidPayloadBody);
        result.setPayload(androidPayload);

        AndroidPolicy androidPolicy = new AndroidPolicy();

        androidPolicy.setExpireTime(getExpireTime(result.getTimestamp()));
        result.setPolicy(androidPolicy);

        result.setDescription(entity.getType() + "-" + DevicePlatform.ANDROID.toString());

        if (!android.getIgnoreActivityType().contains(entity.getType())) {
            result.setMipush(android.isPush());
            result.setMiActivity(android.getActivity());
        }

        return result;
    }

    public Date getExpireTime(Date currentDate) {

        LocalDateTime localDateTime = LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        localDateTime = localDateTime.plusSeconds(expireTimeInSecond);

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isProductionMode() {
        return productionMode;
    }

    public void setProductionMode(boolean productionMode) {
        this.productionMode = productionMode;
    }

    public long getExpireTimeInSecond() {
        return expireTimeInSecond;
    }

    public void setExpireTimeInSecond(long expireTimeInSecond) {
        this.expireTimeInSecond = expireTimeInSecond;
    }

    public AndroidProperties getAndroid() {
        return android;
    }

    public void setAndroid(AndroidProperties android) {
        this.android = android;
    }

    public IosProperties getIos() {
        return ios;
    }

    public void setIos(IosProperties ios) {
        this.ios = ios;
    }
}
