package com.fuyu.basic.commons.page;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 分页请求对象
 *
 * @author maurice.chen
 **/
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -7063877675141922463L;

    /**
     * 分页范围开始参数
     */
    public static final String FIRST_RESULT = "first";
    /**
     * 分页范围结束参数
     */
    public static final String LAST_RESULT = "last";

    /**
     * 默认当前页数
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * 页号
     */
    private int page = DEFAULT_PAGE;
    /**
     * 每页大小
     */
    private int size = DEFAULT_SIZE;

    /**
     * 分页请求对象
     */
    public PageRequest() {

    }

    /**
     * 分页请求对象，用于在分页查询时，通过该对象得知要查询的页数。
     *
     * @param page 页号
     * @param size 内容大小
     */
    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    /**
     * 获取每页的内容大小
     *
     * @return 内容数量
     */
    public int getPageSize() {
        return size;
    }

    /**
     * 获取当前页号
     *
     * @return 页号
     */
    public int getPageNumber() {
        return page;
    }

    /**
     * 设置当前页号
     *
     * @param page 页号
     */
    public void setPageNumber(int page) {
        this.page = page;
    }

    /**
     * 获取每页的内容大小
     *
     * @param size 内容数量
     */
    public void setPageSize(int size) {
        this.size = size;
    }

    /**
     * 获取指定第一个返回记录行的偏移量
     *
     * @return 偏移量
     */
    public int getOffset() {
        int offset = (page - 1) * size;
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    /**
     * 获取分页范围的起始和结束的 Map 对象
     *
     * @return {"first":{@link #getOffset()}, "last": {@link #getPageSize()}}
     */
    public Map<String, Object> getOffsetMap() {
        Map<String, Object> map = new HashMap<String, Object>(16);
        map.put(FIRST_RESULT, getOffset());
        map.put(LAST_RESULT, getPageSize());
        return map;
    }

}
