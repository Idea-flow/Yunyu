package com.ideaflow.yunyu.module.post.vo;

import java.util.List;

/**
 * 后台文章列表响应类。
 * 作用：统一返回后台文章列表数据和分页信息，便于前端列表和分页组件直接消费。
 */
public class AdminPostListResponse {

    private final List<AdminPostItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;

    /**
     * 创建后台文章列表响应对象。
     *
     * @param list 文章列表
     * @param total 总数
     * @param pageNo 当前页码
     * @param pageSize 每页条数
     * @param totalPages 总页数
     */
    public AdminPostListResponse(List<AdminPostItemResponse> list, long total, long pageNo, long pageSize, long totalPages) {
        this.list = list;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    /** 获取列表。 */
    public List<AdminPostItemResponse> getList() { return list; }
    /** 获取总数。 */
    public long getTotal() { return total; }
    /** 获取页码。 */
    public long getPageNo() { return pageNo; }
    /** 获取每页条数。 */
    public long getPageSize() { return pageSize; }
    /** 获取总页数。 */
    public long getTotalPages() { return totalPages; }
}
