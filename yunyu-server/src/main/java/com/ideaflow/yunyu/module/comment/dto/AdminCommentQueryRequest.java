package com.ideaflow.yunyu.module.comment.dto;

import lombok.Data;

/**
 * 后台评论列表查询请求类。
 * 作用：承接评论管理页的关键词、状态、文章、用户和分页筛选参数。
 */
@Data
public class AdminCommentQueryRequest {

    private String keyword;
    private String status;
    private Long postId;
    private Long userId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
