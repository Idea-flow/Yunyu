package com.ideaflow.yunyu.module.post.dto;

/**
 * 后台文章列表查询请求类。
 * 作用：承接后台文章列表的搜索、状态筛选和分页参数。
 */
public class AdminPostQueryRequest {

    private String keyword;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    /** 获取关键词。 */
    public String getKeyword() { return keyword; }
    /** 设置关键词。 */
    public void setKeyword(String keyword) { this.keyword = keyword; }
    /** 获取状态。 */
    public String getStatus() { return status; }
    /** 设置状态。 */
    public void setStatus(String status) { this.status = status; }
    /** 获取页码。 */
    public Integer getPageNo() { return pageNo; }
    /** 设置页码。 */
    public void setPageNo(Integer pageNo) { this.pageNo = pageNo; }
    /** 获取每页条数。 */
    public Integer getPageSize() { return pageSize; }
    /** 设置每页条数。 */
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
