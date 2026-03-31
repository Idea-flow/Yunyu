package com.ideaflow.yunyu.module.post.dto;

import lombok.Data;

/**
 * 后台文章列表查询请求类。
 * 作用：承接后台文章列表的搜索、状态筛选和分页参数。
 */
@Data
public class AdminPostQueryRequest {

    private String keyword;
    private String status;
    private Long categoryId;
    private Long tagId;
    private Long topicId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
