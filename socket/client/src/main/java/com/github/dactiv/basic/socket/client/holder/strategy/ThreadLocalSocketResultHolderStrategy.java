package com.github.dactiv.basic.socket.client.holder.strategy;

import com.github.dactiv.basic.socket.client.entity.SocketResult;
import com.github.dactiv.basic.socket.client.holder.SocketResultHolderStrategy;

import java.util.Objects;

/**
 * Thread Local 的 socket 结果集持有者实现
 *
 * @author maurice.chen
 */
public class ThreadLocalSocketResultHolderStrategy implements SocketResultHolderStrategy {

    private static final ThreadLocal<SocketResult> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public void clear() {
        THREAD_LOCAL.remove();
    }

    @Override
    public SocketResult get() {
        SocketResult result = THREAD_LOCAL.get();

        if (result == null) {
            result = create();
            THREAD_LOCAL.set(result);
        }

        return result;
    }

    @Override
    public void set(SocketResult result) {
        Objects.requireNonNull(result, "socket result 不能为空");
        THREAD_LOCAL.set(result);
    }

    @Override
    public SocketResult create() {
        return new SocketResult();
    }
}
