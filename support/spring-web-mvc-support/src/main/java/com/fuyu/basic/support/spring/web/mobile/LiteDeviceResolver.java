package com.fuyu.basic.support.spring.web.mobile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * 设备信息的精简版解析器实现
 *
 * @author maurice
 */
public class LiteDeviceResolver implements DeviceResolver {

    private final List<String> mobileUserAgentPrefixes = new ArrayList<>();

    private final List<String> mobileUserAgentKeywords = new ArrayList<>();

    private final List<String> tabletUserAgentKeywords = new ArrayList<>();

    private final List<String> normalUserAgentKeywords = new ArrayList<>();

    public LiteDeviceResolver() {
        init();
    }

    public LiteDeviceResolver(List<String> normalUserAgentKeywords) {
        init();
        this.normalUserAgentKeywords.addAll(normalUserAgentKeywords);
    }

    @Override
    public Device resolveDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // 普通设备的UserAgent关键字检测
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            for (String keyword : normalUserAgentKeywords) {
                if (userAgent.contains(keyword)) {
                    return resolveFallback(request);
                }
            }
        }

        // 平板电脑设备的 UserAgent 关键字检测
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();

            // Android
            if (userAgent.contains("android") && !userAgent.contains("mobile")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.ANDROID);
            }
            // Apple
            if (userAgent.contains("ipad")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.IOS);
            }
            // Kindle Fire
            if (userAgent.contains("silk") && !userAgent.contains("mobile")) {
                return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.UNKNOWN);
            }
            for (String keyword : tabletUserAgentKeywords) {
                if (userAgent.contains(keyword)) {
                    return resolveWithPlatform(DeviceType.TABLET, DevicePlatform.UNKNOWN);
                }
            }
        }
        // UAProf 检测
        if (request.getHeader("x-wap-profile") != null || request.getHeader("Profile") != null) {
            if (userAgent != null) {
                // Android
                if (userAgent.contains("android")) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.ANDROID);
                }
                // Apple
                if (userAgent.contains("iphone") || userAgent.contains("ipod") || userAgent.contains("ipad")) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.IOS);
                }
            }
            return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
        }
        // User-Agent 前缀检测
        if (userAgent != null && userAgent.length() >= 4) {
            String prefix = userAgent.substring(0, 4).toLowerCase();
            if (mobileUserAgentPrefixes.contains(prefix)) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
            }
        }
        // 基于 Accept-header 检测
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("wap")) {
            return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
        }
        // 移动设备 UserAgent 关键字检测
        if (userAgent != null) {
            // Android
            if (userAgent.contains("android")) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.ANDROID);
            }
            // Apple
            if (userAgent.contains("ios")) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.IOS);
            }
            for (String keyword : mobileUserAgentKeywords) {
                if (userAgent.contains(keyword)) {
                    return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
                }
            }
        }

        // OperaMini
        Enumeration<String> headers = request.getHeaderNames();

        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            if (header.contains("OperaMini")) {
                return resolveWithPlatform(DeviceType.MOBILE, DevicePlatform.UNKNOWN);
            }
        }

        return resolveFallback(request);
    }

    protected Device resolveWithPlatform(DeviceType deviceType, DevicePlatform devicePlatform) {
        return LiteDevice.from(deviceType, devicePlatform);
    }

    /**
     * 获取用于标识移动设备 user agent 前缀列表
     *
     * @return 标识移动设备 user agent 前缀列表
     */
    protected List<String> getMobileUserAgentPrefixes() {
        return mobileUserAgentPrefixes;
    }

    /**
     * 获取用于标识移动设备的 user agent 关键字列表。主要用于通过移动平台或操作系统进行匹配。
     *
     * @return 用于标识移动设备的 user agent 关键字列表
     */
    protected List<String> getMobileUserAgentKeywords() {
        return mobileUserAgentKeywords;
    }

    /**
     * 获取用于标识平板电脑设备的 user agent 关键字列表。主要用于通过平板电脑平台或操作系统进行匹配。
     *
     * @return 用于标识平板电脑设备的 user agent 关键字列表
     */
    protected List<String> getTabletUserAgentKeywords() {
        return tabletUserAgentKeywords;
    }

    /**
     * 获取用于标识普通设备的用户代理关键字列表。此列表中的所有项目优先于移动和平板电脑用户代理关键字，从而有效地覆盖了这些关键字。
     *
     * @return 用于标识普通设备的用户代理关键字列表
     */
    protected List<String> getNormalUserAgentKeywords() {
        return normalUserAgentKeywords;
    }

    /**
     * 初始化此设备解析器实现。注册已知的设备签名字符串集。子类可以重写以注册其他字符串。
     */
    protected void init() {
        getMobileUserAgentPrefixes().addAll(
                Arrays.asList(KNOWN_MOBILE_USER_AGENT_PREFIXES));
        getMobileUserAgentKeywords().addAll(
                Arrays.asList(KNOWN_MOBILE_USER_AGENT_KEYWORDS));
        getTabletUserAgentKeywords().addAll(
                Arrays.asList(KNOWN_TABLET_USER_AGENT_KEYWORDS));
    }

    /**
     * 如果此解析器未匹配任何移动设备，则调用回退。
     * <p>
     * 此方法默认返回一个既不是移动设备也不是平板电脑的“普通”设备
     * <p>
     * 子类可以覆盖以尝试其他移动设备或平板电脑设备匹配，然后再退回到“正常”设备。
     *
     * @return 设备
     */
    protected Device resolveFallback(HttpServletRequest request) {
        return LiteDevice.NORMAL_INSTANCE;
    }

    private static final String[] KNOWN_MOBILE_USER_AGENT_PREFIXES = new String[]{
            "w3c ", "w3c-", "acs-", "alav", "alca", "amoi", "avan", "benq", "bird",
            "blac", "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric",
            "hipt", "htc_", "inno", "ipaq", "ipod", "jigs", "kddi", "keji", "leno",
            "lg-c", "lg-d", "lg-g", "lge-", "lg/u", "maui", "maxo", "midp", "mits",
            "mmef", "mobi", "mot-", "moto", "mwbp", "nec-", "newt", "noki", "palm",
            "pana", "pant", "phil", "play", "port", "prox", "qwap", "sage", "sams",
            "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
            "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tosh",
            "tsm-", "upg1", "upsi", "vk-v", "voda", "wap-", "wapa", "wapi", "wapp",
            "wapr", "webc", "winw", "winw", "xda ", "xda-"};

    private static final String[] KNOWN_MOBILE_USER_AGENT_KEYWORDS = new String[]{
            "blackberry", "webos", "ipod", "lge vx", "midp", "maemo", "mmp", "mobile",
            "netfront", "hiptop", "nintendo DS", "novarra", "openweb", "opera mobi",
            "opera mini", "palm", "psp", "phone", "smartphone", "symbian", "up.browser",
            "up.link", "wap", "windows ce"};

    private static final String[] KNOWN_TABLET_USER_AGENT_KEYWORDS = new String[]{
            "ipad", "playbook", "hp-tablet", "kindle"};
}
