package com.github.dactiv.basic.socket.client.entity;

import lombok.*;

/**
 *
 * 但返回值的 socket 结果集
 *
 * @param <R> 返回值类型
 *
 * @author maurice
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "of")
public class ReturnValueSocketResult<R> extends SocketResult{

    private static final long serialVersionUID = 7934586207040058796L;
    /**
     * 返回值
     */
    @NonNull
    private R returnValue;
}
