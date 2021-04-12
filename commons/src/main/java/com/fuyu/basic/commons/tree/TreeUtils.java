package com.fuyu.basic.commons.tree;

import com.fuyu.basic.commons.Casts;

import java.util.ArrayList;
import java.util.List;

/**
 * 树工具类，用户合并或拆解等
 *
 * @author maurice.chen
 */
public class TreeUtils {

    /**
     * 绑定泛型树
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     * @param <R>  返回类型
     * @return 绑定后的树形结合
     */
    public static <P, T, R extends Tree<P, T>> List<R> buildGenericTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> treeList = TreeUtils.buildTree(list);

        List<R> result = new ArrayList<>();

        for (Tree<P, T> tree : treeList) {
            result.add(Casts.cast(tree));
        }

        return result;
    }

    /**
     * 绑定树形
     *
     * @param list 树形集合
     * @param <P>  树形父类类型
     * @param <T>  属性孩子类型
     * @return 绑定后的树形结合
     */
    public static <P, T> List<Tree<P, T>> buildTree(List<? extends Tree<P, T>> list) {
        List<Tree<P, T>> result = new ArrayList<>();

        for (Tree<P, T> root : list) {
            //顶级节点的根节点为NULL
            if (root.getParent() == null || "".equals(root.getParent())) {
                findChildren(root, list);
                result.add(root);
            }
        }

        if (result.isEmpty()) {

            List<Tree<P, T>> children = new ArrayList<>();

            List<Tree<P, T>> clone = new ArrayList<>(list);

            for (Tree<P, T> root : list) {

                for (Tree<P,T > child : list) {

                    if (child.isChildren(root) ) {
                        children.add(child);
                    }

                }
            }

            clone.removeAll(children);

            for (Tree<P, T> root : clone) {
                findChildren(root, list);
                result.add(root);
            }

        }

        return result;
    }


    /**
     * 获取孩子节点合并到父类
     *
     * @param parent 父类对象
     * @param list   树形数据集合
     * @param <P>    树形父类类型
     * @param <T>    属性孩子类型
     */
    private static <P, T> void findChildren(Tree<P, T> parent, List<? extends Tree<P, T>> list) {

        for (Tree<P, T> entity : list) {
            if (entity.getParent() == null || "".equals(entity.getParent())) {
                continue;
            }
            if (entity.isChildren(parent)) {
                findChildren(entity, list);
                parent.getChildren().add(entity);
            }
        }
    }

}
