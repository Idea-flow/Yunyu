package com.ideaflow.yunyu.module.comment.site.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前台评论列表响应类。
 * 作用：向文章评论区返回根评论分页结果、总页数和当前可见评论总数。
 */
@Data
@AllArgsConstructor
public class SiteCommentListResponse {

    private List<SiteCommentItemResponse> list;
    private Long total;
    private Long commentCount;
    private Long pageNo;
    private Long pageSize;
    private Long totalPages;
}
