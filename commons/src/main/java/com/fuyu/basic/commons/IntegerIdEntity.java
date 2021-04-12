package com.fuyu.basic.commons;

import java.util.Date;

/**
 * 整型类型的 ID 主键实体
 *
 * @author maurice
 */
public class IntegerIdEntity extends IdEntity<Integer> {

    public static final String DEFAULT_CREATION_TIME_NAME = "creationTime";

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * String 类型的 ID 主键实体
     */
    public IntegerIdEntity() {
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
