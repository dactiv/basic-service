package com.github.dactiv.basic.socket.client.holder;

import com.github.dactiv.basic.socket.client.entity.SocketResult;
import com.github.dactiv.basic.socket.client.holder.strategy.ThreadLocalSocketResultHolderStrategy;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;

/**
 * socket 结果集持有者
 *
 * @author maurice.chen
 */
public class SocketResultHolder {

    public static final String MODE_THREAD_LOCAL = "MODE_THREAD_LOCAL";

    public static final String SYSTEM_PROPERTY = "sg.socket.client.holder.strategy";

    private static String strategyName = System.getProperty(SYSTEM_PROPERTY);

    private static SocketResultHolderStrategy strategy;

    private static int initializeCount = 0;

    static {
        // 初始化
        initialize();
    }

    /**
     * 获取初始化次数
     *
     * @return 初始化次数
     */
    public static int getInitializeCount() {
        return initializeCount;
    }

    /**
     * 初始化策略实现类
     */
    private static void initialize() {

        // 如果没有配置策略名称，设置默认使用本地线程資源
        if (!StringUtils.hasText(strategyName)) {
            strategyName = MODE_THREAD_LOCAL;
        }

        // 如果策略名称为本地线程資源，创建本地线程資源
        if (strategyName.equals(MODE_THREAD_LOCAL)) {
            strategy = new ThreadLocalSocketResultHolderStrategy();
        } else {
            // 否则尝试去获取自定义的策略实现类
            try {
                Class<?> clazz = Class.forName(strategyName);
                Constructor<?> customStrategy = clazz.getConstructor();
                strategy = (SocketResultHolderStrategy) customStrategy.newInstance();
            } catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }

        initializeCount++;
    }

    /**
     * 获取当前 socket 结果集
     *
     * @return 当前 socket 结果集
     */
    public static SocketResult get() {
        return strategy.get();
    }

    /**
     * 清除当前 socket 结果集
     *
     */
    public static void clear() {
        strategy.clear();
    }

    /**
     * 设置当前 socket 结果集
     *
     * @param socketResult socket 结果集
     */
    public static void set(SocketResult socketResult) {
        strategy.set(socketResult);
    }

    /**
     * 创建当前 socket 结果集
     *
     * @return socket 结果集
     */
    public static SocketResult create() {
        return strategy.create();
    }

    /**
     * 设置策略名称
     *
     * @param strategyName 策略名称
     */
    public static void setStrategyName(String strategyName) {
        SocketResultHolder.strategyName = strategyName;
        initialize();
    }

    /**
     * 获取当前策略实现类
     *
     * @return 当前策略实现类
     */
    public static SocketResultHolderStrategy getContextHolderStrategy() {
        return strategy;
    }

}
