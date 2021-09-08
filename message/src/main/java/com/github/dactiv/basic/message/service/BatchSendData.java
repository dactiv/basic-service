package com.github.dactiv.basic.message.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量发送数据实体
 *
 * @author maurice.chen
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class BatchSendData implements Serializable {

    private static final long serialVersionUID = -8597871271801245042L;

    private Integer simpleCount = 0;

    private Map<Integer, Integer> intervalCountGroup = new LinkedHashMap<>();

    /**
     * 直接发送的数据
     */
    private Map<Integer, List<Integer>> simpleDataGroup = new LinkedHashMap<>();

    /**
     * 存在间隔时间发送的数据
     */
    private Map<Integer, Map<Integer, List<Integer>>> intervalDataGroup = new LinkedHashMap<>();
}
