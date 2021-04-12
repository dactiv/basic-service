package com.fuyu.basic.commons;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 主键 ID 实体类
 *
 * @author maurice.chen
 **/
public class IdEntity<T> implements Serializable {

    private static final long serialVersionUID = 2276247579226767445L;

    /**
     * 默认主键 ID 的字段名称
     */
    public static final String DEFAULT_ID_NAME = "id";

    /**
     * 默认主键 id 集合字段名称
     */
    public static final String DEFAULT_IDS_NAME = "ids";

    /**
     * 主键 ID
     */
    private T id;

    /**
     * 主键 ID 实体类
     */
    public IdEntity() {

    }

    /**
     * 获取主键 ID
     *
     * @return 主键 ID
     */
    public T getId() {
        return id;
    }

    /**
     * 设置主键 ID
     *
     * @param id 主键 ID
     */
    public void setId(T id) {
        this.id = id;
    }

    /**
     * 转型 IdEntity 实体为 map
     *
     * @return map
     */
    public Map<String, Object> idEntityToMap() {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(DEFAULT_ID_NAME, id);

        return result;
    }

    /**
     * 转型整个实体为 map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        return Casts.castObjectToMap(this);
    }
}
