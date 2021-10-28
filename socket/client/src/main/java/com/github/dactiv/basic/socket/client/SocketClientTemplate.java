package com.github.dactiv.basic.socket.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dactiv.basic.socket.client.entity.*;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.framework.spring.security.authentication.service.feign.FeignAuthenticationConfiguration;
import com.github.dactiv.framework.spring.web.result.filter.holder.FilterResultHolder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.parameters.P;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * socket 客户端模版，用于发送 socket 消息到客户端使用
 *
 * @author maurice.chen
 */
@Setter
@Slf4j
@AllArgsConstructor(staticName = "of")
public class SocketClientTemplate implements DisposableBean {

    private static final String DEFAULT_SERVER_SERVICE_ID = "socket-server";

    @NonNull
    private DiscoveryClient discoveryClient;

    @NonNull
    private RestTemplate restTemplate;

    @NonNull
    private ThreadPoolTaskExecutor taskExecutor;

    @NonNull
    private AuthenticationProperties properties;

    /**
     * 广播消息
     *
     * @param broadcastMessage 多播 socket 消息对象
     *
     * @return {@link RestResult} 的 map 映射
     */
    public Map<String, Object> broadcast(BroadcastMessage<?> broadcastMessage) {

        List<Map<String, Object>> result = broadcast(Collections.singletonList(broadcastMessage));

        if (result.isEmpty()) {
            return null;
        }

        return result.iterator().next();
    }

    /**
     * 广播消息
     *
     * @param broadcastMessages 多播 socket 消息对象集合
     *
     * @return {@link RestResult} 的 map 映射集合
     */
    public List<Map<String, Object>> broadcast(List<BroadcastMessage<?>> broadcastMessages) {
        return postSocketMessage(BroadcastMessage.DEFAULT_TYPE, broadcastMessages);
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessage 多播 socket 消息对象
     */
    public void asyncBroadcast(BroadcastMessage<?> broadcastMessage) {
        asyncBroadcast(broadcastMessage, null);
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessage 多播 socket 消息对象
     * @param filterResultIds  过滤结果集 id 集合
     */
    public void asyncBroadcast(BroadcastMessage<?> broadcastMessage, List<String> filterResultIds) {
        asyncBroadcast(broadcastMessage, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessage 多播 socket 消息对象集合
     * @param filterResultIds  过滤结果集 id 集合
     * @param listenable       回调监听器
     */
    public void asyncBroadcast(BroadcastMessage<?> broadcastMessage,
                               List<String> filterResultIds,
                               ListenableFutureCallback<Map<String, Object>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }
                    return broadcast(broadcastMessage);
                })
                .addCallback(listenable);
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessages 多播 socket 消息对象集合
     */
    public void asyncBroadcast(List<BroadcastMessage<?>> broadcastMessages) {
        asyncBroadcast(broadcastMessages, null);
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessages 多播 socket 消息对象集合
     * @param filterResultIds   过滤结果集 id 集合
     */
    public void asyncBroadcast(List<BroadcastMessage<?>> broadcastMessages, List<String> filterResultIds) {
        asyncBroadcast(broadcastMessages, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步广播消息
     *
     * @param broadcastMessages 多播 socket 消息对象集合
     * @param filterResultIds   过滤结果集 id 集合
     * @param listenable        回调监听器
     */
    public void asyncBroadcast(List<BroadcastMessage<?>> broadcastMessages,
                               List<String> filterResultIds,
                               ListenableFutureCallback<List<Map<String, Object>>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }
                    return broadcast(broadcastMessages);
                })
                .addCallback(listenable);
    }

    /**
     * 单播信息
     *
     * @param unicastMessage 单播 socket 消息对象
     *
     * @return {@link RestResult} 的 map 映射
     */
    public Map<String, Object> unicast(UnicastMessage<?> unicastMessage) {

        List<Map<String, Object>> result = unicast(Collections.singletonList(unicastMessage));

        if (result.isEmpty()) {
            return null;
        }

        return result.iterator().next();
    }

    /**
     * 单播信息
     *
     * @param unicastMessages 单播 socket 消息对象集合
     *
     * @return {@link RestResult} 的 map 映射集合
     */
    public List<Map<String, Object>> unicast(List<UnicastMessage<?>> unicastMessages) {

        List<UnicastMessage<?>> tempList = new LinkedList<>(unicastMessages);

        List<SocketUserMessage<?>> socketUserMessages = tempList
                .stream()
                .filter(u -> SocketUserMessage.class.isAssignableFrom(u.getClass()))
                .map(Casts::<SocketUserMessage<?>>cast)
                .collect(Collectors.toList());

        tempList.removeAll(socketUserMessages);

        List<Map<String, Object>> result = socketUserMessages.stream()
                .collect(Collectors.groupingBy(u -> u.getDetails().getSocketServerIp()))
                .entrySet()
                .stream()
                .flatMap(e -> postSocketMessage(e.getKey(), SocketUserMessage.DEFAULT_TYPE, e.getValue()).stream())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(tempList)) {
            List<Map<String, Object>> tempResult = postSocketMessage(UnicastMessage.DEFAULT_TYPE, tempList);

            result.addAll(tempResult);
        }

        return result;

    }

    /**
     * 异步单播信息
     *
     * @param unicastMessage 多播 socket 消息对象
     */
    public void asyncUnicast(UnicastMessage<?> unicastMessage) {
        asyncUnicast(unicastMessage, null);
    }

    /**
     * 异步单播信息
     *
     * @param unicastMessage  多播 socket 消息对象
     * @param filterResultIds 过滤结果集 id 集合
     */
    public void asyncUnicast(UnicastMessage<?> unicastMessage, List<String> filterResultIds) {
        asyncUnicast(unicastMessage, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步单播信息
     *
     * @param unicastMessage  多播 socket 消息对象集合
     * @param filterResultIds 过滤结果集 id 集合
     * @param listenable      回调监听器
     */
    public void asyncUnicast(UnicastMessage<?> unicastMessage,
                             List<String> filterResultIds,
                             ListenableFutureCallback<Map<String, Object>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }
                    return unicast(unicastMessage);
                })
                .addCallback(listenable);
    }

    /**
     * 异步单播信息
     *
     * @param unicastMessages 单播 socket 消息对象集合
     */
    public void asyncUnicast(List<UnicastMessage<?>> unicastMessages) {
        asyncUnicast(unicastMessages, null);
    }

    /**
     * 异步单播信息
     *
     * @param unicastMessages 单播 socket 消息对象集合
     * @param filterResultIds 过滤结果集 id 集合
     */
    public void asyncUnicast(List<UnicastMessage<?>> unicastMessages, List<String> filterResultIds) {
        asyncUnicast(unicastMessages, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步单播信息
     *
     * @param unicastMessages 单播 socket 消息对象集合
     * @param filterResultIds 过滤结果集 id 集合
     * @param listenable      回调监听器
     */
    public void asyncUnicast(List<UnicastMessage<?>> unicastMessages,
                             List<String> filterResultIds,
                             ListenableFutureCallback<List<Map<String, Object>>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }
                    return unicast(unicastMessages);
                })
                .addCallback(listenable);
    }

    /**
     * 单播单数据循环单播 socket 消息
     *
     * @param multipleUnicastMessage 单数据循环单播 socket 消息
     *
     * @return {@link RestResult} 的 map 映射
     */
    public Map<String, Object> multipleUnicast(MultipleUnicastMessage<?> multipleUnicastMessage) {

        List<Map<String, Object>> result = multipleUnicast(Collections.singletonList(multipleUnicastMessage));

        if (result.isEmpty()) {
            return null;
        }

        return result.iterator().next();
    }

    /**
     * 单播单数据循环单播 socket 消息
     *
     * @param multipleUnicastMessages 单数据循环单播 socket 消息集合
     *
     * @return {@link RestResult} 的 map 映射集合
     */
    public List<Map<String, Object>> multipleUnicast(List<MultipleUnicastMessage<?>> multipleUnicastMessages) {

        List<MultipleUnicastMessage<?>> tempList = new LinkedList<>(multipleUnicastMessages);

        List<MultipleSocketUserMessage<?>> socketUserMessages = tempList
                .stream()
                .filter(u -> MultipleSocketUserMessage.class.isAssignableFrom(u.getClass()))
                .map(Casts::<MultipleSocketUserMessage<?>>cast)
                .collect(Collectors.toList());

        tempList.removeAll(socketUserMessages);

        Map<String, List<MultipleSocketUserMessage<?>>> postData = new LinkedHashMap<>();

        for (MultipleSocketUserMessage<?> message : socketUserMessages) {

            Map<String, List<SocketUserDetails>> group = message
                    .getSocketUserDetails()
                    .stream()
                    .collect(Collectors.groupingBy(SocketUserDetails::getSocketServerIp));

            for (Map.Entry<String, List<SocketUserDetails>> entry : group.entrySet()) {

                List<MultipleSocketUserMessage<?>> list = postData.computeIfAbsent(entry.getKey(), k -> new LinkedList<>());

                MultipleSocketUserMessage<?> data = MultipleSocketUserMessage.ofSocketUserDetails(
                        entry.getValue(),
                        message.getEvent(),
                        message.getMessage()
                );

                list.add(data);

            }

        }

        List<Map<String, Object>> result = postData
                .entrySet()
                .stream()
                .flatMap(e -> postSocketMessage(e.getKey(), MultipleUnicastMessage.DEFAULT_TYPE, e.getValue()).stream())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(tempList)) {
            List<Map<String, Object>> tempResult = postSocketMessage(UnicastMessage.DEFAULT_TYPE, tempList);

            result.addAll(tempResult);
        }

        return result;
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessage 单数据循环单播 socket 消息
     */
    public void asyncMultipleUnicast(MultipleUnicastMessage<?> multipleUnicastMessage) {
        asyncMultipleUnicast(multipleUnicastMessage, null);
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessage 单数据循环单播 socket 消息
     * @param filterResultIds        过滤结果集 id 集合
     */
    public void asyncMultipleUnicast(MultipleUnicastMessage<?> multipleUnicastMessage, List<String> filterResultIds) {
        asyncMultipleUnicast(multipleUnicastMessage, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessage 单数据循环单播 socket 消息
     * @param filterResultIds        过滤结果集 id 集合
     * @param listenable             回调监听器
     */
    public void asyncMultipleUnicast(MultipleUnicastMessage<?> multipleUnicastMessage,
                                     List<String> filterResultIds,
                                     ListenableFutureCallback<Map<String, Object>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }

                    return multipleUnicast(multipleUnicastMessage);
                })
                .addCallback(listenable);
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessages 单播 socket 消息对象集合
     */
    public void asyncMultipleUnicast(List<MultipleUnicastMessage<?>> multipleUnicastMessages) {
        asyncMultipleUnicast(multipleUnicastMessages, null);
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessages 单播 socket 消息对象集合
     * @param filterResultIds         过滤结果集 id 集合
     */
    public void asyncMultipleUnicast(List<MultipleUnicastMessage<?>> multipleUnicastMessages,
                                     List<String> filterResultIds) {
        asyncMultipleUnicast(multipleUnicastMessages, filterResultIds, new LogListenableFutureCallback<>());
    }

    /**
     * 异步单数据循环单播
     *
     * @param multipleUnicastMessages 单播 socket 消息对象集合
     * @param filterResultIds         过滤结果集 id 集合
     * @param listenable              回调监听器
     */
    public void asyncMultipleUnicast(List<MultipleUnicastMessage<?>> multipleUnicastMessages,
                                     List<String> filterResultIds,
                                     ListenableFutureCallback<List<Map<String, Object>>> listenable) {
        taskExecutor
                .submitListenable(() -> {
                    if (CollectionUtils.isNotEmpty(filterResultIds)) {
                        FilterResultHolder.set(filterResultIds);
                    }
                    return multipleUnicast(multipleUnicastMessages);
                })
                .addCallback(listenable);
    }

    /**
     * 提交 socket 消息到指定 socket 服务器
     *
     * @param type   消息类型: multipleUnicast 单数据循环单播，unicast: 根据设备识别单播，broadcast: 广播
     * @param values socket 消息集合
     *
     * @return {@link RestResult} 的 map 映射集合
     */
    private List<Map<String, Object>> postSocketMessage(String type, List<? extends SocketMessage<?>> values) {
        return postSocketMessage(null, type, values);
    }

    /**
     * 提交 socket 消息到指定 socket 服务器
     *
     * @param ip     socket 服务 ip 地址
     * @param type   消息类型: unicastUser: 根据用户所在服务器单播，
     *               unicast: 根据设备识别单播，
     *               broadcast: 广播,
     *               multipleUnicast: 同数据结果，单播多客户端
     * @param values socket 消息集合
     *
     * @return {@link RestResult} 的 map 映射集合
     */
    private List<Map<String, Object>> postSocketMessage(String ip, String type, List<? extends SocketMessage<?>> values) {

        List<String> urls = new LinkedList<>();

        if (StringUtils.isNotBlank(ip)) {

            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(DEFAULT_SERVER_SERVICE_ID);

            ServiceInstance instance = serviceInstances
                    .stream()
                    .filter(s -> s.getHost().equals(ip))
                    .findFirst()
                    .orElseThrow(() -> new SystemException("找不到 IP 为 [" + ip + "] 的服务实例"));

            urls.add(instance.getUri() + "/" + type);

        } else {
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(DEFAULT_SERVER_SERVICE_ID);

            urls = serviceInstances
                    .stream()
                    .map(s -> s.getUri() + "/" + type)
                    .collect(Collectors.toList());
        }

        HttpHeaders httpHeaders = FeignAuthenticationConfiguration.of(properties);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> data = Casts.convertValue(values, new TypeReference<>() {
        });

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(data, httpHeaders);

        List<Map<String, Object>> result = new LinkedList<>();

        for (String url : urls) {

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            if (Objects.nonNull(response.getBody())) {
                result.addAll(response.getBody());
            }
        }

        return result;

    }

    /**
     * 发送 socket 结果集
     *
     * @param result socket 结果集
     */
    public void sendSocketResult(SocketResult result) {

        if (CollectionUtils.isNotEmpty(result.getUnicastMessages())) {
            unicast(result.getUnicastMessages());
        }

        if (CollectionUtils.isNotEmpty(result.getBroadcastMessages())) {
            broadcast(result.getBroadcastMessages());
        }

        if (CollectionUtils.isNotEmpty(result.getMultipleUnicastMessages())) {
            multipleUnicast(result.getMultipleUnicastMessages());
        }
    }

    /**
     * 异步发送 socket 结果集
     *
     * @param result socket 结果集
     */
    public void asyncSendSocketResult(SocketResult result) {
        asyncSendSocketResult(result, null);
    }

    /**
     * 异步发送 socket 结果集
     *
     * @param result          socket 结果集
     * @param filterResultIds 过滤结果集 id 集合
     */
    public void asyncSendSocketResult(SocketResult result, List<String> filterResultIds) {

        if (CollectionUtils.isNotEmpty(result.getUnicastMessages())) {
            asyncUnicast(result.getUnicastMessages(), filterResultIds);
        }

        if (CollectionUtils.isNotEmpty(result.getBroadcastMessages())) {
            asyncBroadcast(result.getBroadcastMessages(), filterResultIds);
        }

        if (CollectionUtils.isNotEmpty(result.getMultipleUnicastMessages())) {
            asyncMultipleUnicast(result.getMultipleUnicastMessages(), filterResultIds);
        }
    }

    @Override
    public void destroy() {
        taskExecutor.destroy();
    }

    /**
     * 打印日志的 ListenableFutureCallback 实现
     *
     * @author maurice.chen
     */
    static class LogListenableFutureCallback<T> implements ListenableFutureCallback<T> {
        @Override
        public void onFailure(Throwable ex) {
            log.error("发送 socket 消息失败", ex);
        }

        @Override
        public void onSuccess(T result) {
            if (log.isDebugEnabled()) {
                log.debug("发送 socket 消息成功, 响应内容为:" + result);
            }
        }
    }
}
