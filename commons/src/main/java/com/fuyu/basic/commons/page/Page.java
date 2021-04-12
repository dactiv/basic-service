package com.fuyu.basic.commons.page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页对象
 *
 * @author maurice.chen
 **/
public class Page<T> implements Serializable {

    private static final long serialVersionUID = -8548642105903724207L;
    /**
     * 分页请求
     */
    private PageRequest pageRequest;
    /**
     * 数据元素
     */
    private List<T> elements;

    /**
     * 分页对象
     */
    public Page() {
    }

    /**
     * 分页对象
     *
     * @param pageRequest 分页请求
     * @param elements    数据元素
     */
    public Page(PageRequest pageRequest, List<T> elements) {
        this.pageRequest = pageRequest;
        this.elements = elements;
    }

    /**
     * 获取当前页号
     *
     * @return 页号
     */
    public int getNumber() {
        return pageRequest == null ? 0 : pageRequest.getPageNumber();
    }

    /**
     * 获取每页的内容大小
     *
     * @return 内容数量
     */
    public int getSize() {
        return pageRequest == null ? 0 : pageRequest.getPageSize();
    }

    /**
     * 获取数据元素数量
     *
     * @return 数据元素数量
     */
    public int getNumberOfElements() {
        return elements.size();
    }

    /**
     * 判断是否存在上一页
     *
     * @return true 表示存在，否则 false
     */
    public boolean hasPrevious() {
        return getNumber() > 1;
    }

    /**
     * 判断是否存在下一页
     *
     * @return true 表示存在，否则 false
     */
    public boolean hasNext() {
        return getNumberOfElements() == getSize();
    }

    /**
     * 判断是否为首页
     *
     * @return ture 表示首页，否则 false
     */
    public boolean isFirst() {
        return !hasPrevious();
    }

    /**
     * 判断是否为尾页
     *
     * @return true 表示尾页，否则 false
     */
    public boolean isLast() {
        return !hasNext();
    }

    /**
     * 判断是否存在数据元素
     *
     * @return true 表示存在，否则 false
     */
    public boolean hasContent() {
        return !elements.isEmpty();
    }

    /**
     * 获取数据元素
     *
     * @return 当前也的分页数据集合
     */
    public List<T> getContent() {
        return Collections.unmodifiableList(elements);
    }

}
