package com.ideaflow.yunyu.module.user.dto;

/**
 * 后台用户列表查询请求类。
 * 作用：承接站长在后台对用户列表进行搜索、角色筛选和状态筛选时的查询参数。
 */
public class AdminUserQueryRequest {

    private String keyword;
    private String role;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    /**
     * 获取搜索关键词。
     *
     * @return 搜索关键词
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * 设置搜索关键词。
     *
     * @param keyword 搜索关键词
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * 获取角色筛选值。
     *
     * @return 角色筛选值
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置角色筛选值。
     *
     * @param role 角色筛选值
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取状态筛选值。
     *
     * @return 状态筛选值
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态筛选值。
     *
     * @param status 状态筛选值
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取页码。
     *
     * @return 页码
     */
    public Integer getPageNo() {
        return pageNo;
    }

    /**
     * 设置页码。
     *
     * @param pageNo 页码
     */
    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    /**
     * 获取每页条数。
     *
     * @return 每页条数
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页条数。
     *
     * @param pageSize 每页条数
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
