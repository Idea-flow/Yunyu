package com.ideaflow.yunyu.module.comment.dto;

import lombok.Data;

/**
 * 前台评论列表查询请求类。
 * 作用：承接文章评论区的分页参数，便于根评论分页加载。
 */
@Data
public class SiteCommentQueryRequest {

    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
