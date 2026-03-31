package com.ideaflow.yunyu.module.user.vo;

import java.util.List;

/**
 * 后台用户列表响应类。
 * 作用：统一返回后台用户列表数据和总数，便于前端页面构建管理表格与统计信息。
 */
public class AdminUserListResponse {

    private final List<AdminUserItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;

    /**
     * 创建后台用户列表响应对象。
     *
     * @param list 用户列表
     * @param total 总数
     * @param pageNo 当前页码
     * @param pageSize 每页条数
     * @param totalPages 总页数
     */
    public AdminUserListResponse(List<AdminUserItemResponse> list, long total, long pageNo, long pageSize, long totalPages) {
        this.list = list;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    /**
     * 获取用户列表。
     *
     * @return 用户列表
     */
    public List<AdminUserItemResponse> getList() {
        return list;
    }

    /**
     * 获取总数。
     *
     * @return 总数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取当前页码。
     *
     * @return 当前页码
     */
    public long getPageNo() {
        return pageNo;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 获取总页数。
     *
     * @return 总页数
     */
    public long getTotalPages() {
        return totalPages;
    }
}
