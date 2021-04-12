package com.fuyu.basic.commons;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 基础增删改查类
 *
 * @author maurice.chen
 */
public interface BasicCurdDao<T, PK extends Serializable> {

    /**
     * 获取实体
     *
     * @param id 主键值
     * @return 实体
     */
    T get(PK id);

    /**
     * 锁行并获取实体
     *
     * @param id 主键 id
     * @return 实体
     */
    T lock(PK id);

    /**
     * 新增对象
     *
     * @param entity 持久化实体
     */
    void insert(T entity);

    /**
     * 修改对象
     *
     * @param entity 持久化实体
     */
    void update(T entity);

    /**
     * 删除对象
     *
     * @param id 主键值
     */
    void delete(PK id);

    /**
     * 查找实体
     *
     * @param filter 过滤条件
     * @return 实体集合
     */
    List<T> find(Map<String, Object> filter);

    /**
     * 统计数量
     *
     * @param filter 过滤条件
     * @return 统计值
     */
    long count(Map<String, Object> filter);

}
