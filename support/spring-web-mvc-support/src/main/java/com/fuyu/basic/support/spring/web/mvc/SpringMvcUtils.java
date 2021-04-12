package com.fuyu.basic.support.spring.web.mvc;

import com.fuyu.basic.commons.Casts;
import com.fuyu.basic.commons.exception.SystemException;
import com.fuyu.basic.support.spring.web.mobile.Device;
import com.fuyu.basic.support.spring.web.mobile.DeviceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * spring mvc 工具类
 *
 * @author maurice.chen
 **/
public class SpringMvcUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpringMvcUtils.class);

    private final static String IP_UNKNOWN_STRING = "unknown";

    private final static Integer IP_MIN_LENGTH = 15;

    public final static String COMMA_STRING = ",";

    /**
     * 获取 request 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     * @return attribute 值
     */
    public static <T> T getRequestAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_REQUEST));
    }

    /**
     * 设置 session 的 attribute
     *
     * @param name attribute 名称
     * @param value 值
     */
    public static void setSessionAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_SESSION);
    }

    /**
     * 设置 request 的 attribute
     *
     * @param name attribute 名称
     * @param value 值
     */
    public static void setRequestAttribute(String name, Object value) {
        getCurrentRequestAttributes().setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 获取 session 的 attribute
     *
     * @param name attribute 名称
     * @param <T>  attribute 类型
     * @return attribute 值
     */
    public static <T> T getSessionAttribute(String name) {
        return Casts.cast(getCurrentRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION));
    }

    /**
     * 获取 spring mvc RequestAttributes
     *
     * @return RequestAttributes
     */
    public static RequestAttributes getCurrentRequestAttributes() {
        return RequestContextHolder.currentRequestAttributes();
    }

    /**
     * 获取 HttpServletRequest
     *
     * @return httpServletRequest
     */
    public static Optional<HttpServletRequest> getHttpServletRequest() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            return Optional.of(servletRequestAttributes.getRequest());
        }

        return Optional.empty();
    }

    /**
     * 获取 HttpServletResponse
     *
     * @return httpServletResponse
     */
    public static Optional<HttpServletResponse> getHttpServletResponse() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            return Optional.ofNullable(servletRequestAttributes.getResponse());
        }

        return Optional.empty();
    }

    /**
     * 获取 http 响应状态
     *
     * @param httpServletResponse http servlet response
     * @return http 响应状态
     */
    public static HttpStatus getHttpStatus(HttpServletResponse httpServletResponse) {
        HttpStatus result;
        try {
            result = HttpStatus.valueOf(httpServletResponse.getStatus());
        } catch (Exception e) {
            result = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return result;
    }

    /**
     * 获取当前设备
     *
     * @return 设备
     */
    public static Device getCurrentDevice() {
        return DeviceUtils.getCurrentDevice(RequestContextHolder.currentRequestAttributes());
    }

    /**
     * 获取当前设备,如果后去不了，抛出异常
     *
     * @return 设备
     */
    public static Device getRequiredCurrentDevice() {

        Optional<HttpServletRequest> optional = getHttpServletRequest();

        if (!optional.isPresent()) {
            throw new SystemException("当前线程中无法获取 HttpServletRequest 信息");
        }

        return DeviceUtils.getRequiredCurrentDevice(optional.get());
    }

    /**
     * 获取设备唯一识别
     *
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();

        if (!optional.isPresent()) {
            throw new SystemException("当前线程中无法获取 HttpServletRequest 信息");
        }

        return getDeviceIdentified(optional.get());
    }

    /**
     * 获取设备唯一识别
     *
     * @param request http servlet request
     * @return 设备唯一识别
     */
    public static String getDeviceIdentified(HttpServletRequest request) {
        String deviceIdentified = request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME);

        if (StringUtils.isEmpty(deviceIdentified)) {
            deviceIdentified = request.getSession().getId();
        }

        return deviceIdentified;
    }

    /**
     * 获取请求的头里的设备唯一识别
     *
     * @return 唯一识别
     */
    public static String getRequestHeaderDeviceIdentified() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();

        return optional
                .map(request -> request.getHeader(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_HEADER_NAME))
                .orElse(null);

    }

    /**
     * 创建下载类型的 ResponseEntity
     *
     * @param fileName 下载文件名称
     * @param path 文件路径
     *
     * @return 下载类型的 ResponseEntity
     *
     * @throws IOException 获取路径文件失败抛出
     */
    public static ResponseEntity<byte[]> createDownloadResponseEntity(String fileName, String path) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        return new ResponseEntity<>(FileCopyUtils.copyToByteArray(new File(path)), headers, HttpStatus.CREATED);


    }

    /**
     * 获取 ip 地址
     *
     * @return ip 地址
     */
    public static String getIpAddress() {
        Optional<HttpServletRequest> optional = getHttpServletRequest();
        return optional.map(SpringMvcUtils::getIpAddress).orElse(null);
    }

    /**
     * 获取 ip 地址
     *
     * @param request request http servlet reques
     * @return ip 地址
     */
    public static String getIpAddress(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isEmpty(ip) || IP_UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || IP_UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || IP_UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || IP_UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || IP_UNKNOWN_STRING.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        //使用代理，则获取第一个IP地址
        if (StringUtils.isNotEmpty(ip) && ip.length() > IP_MIN_LENGTH) {
            if (ip.indexOf(COMMA_STRING) > 0) {
                ip = ip.substring(0, ip.indexOf(COMMA_STRING));
            }
        }

        return ip;
    }

}
