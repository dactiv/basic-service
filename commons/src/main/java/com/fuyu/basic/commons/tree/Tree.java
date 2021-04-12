package com.fuyu.basic.commons.tree;

import java.util.List;

/**
 * 树形接口
 *
 * @param <P> 父类对象
 * @param <C> 子类对象
 * @author maurice.chen
 */
public interface Tree<P, C> {

    /**
     * 获取孩子节点集合
     *
     * @return 孩子节点集合
     */
    List<Tree<P, C>> getChildren();

    /**
     * 获取父节点
     *
     * @return 父节点
     */
    P getParent();

    /**
     * 是否孩子节点
     *
     * @param parent 父类实体
     * @return true 为是，否则 false
     */
    boolean isChildren(Tree<P, C> parent);


}
