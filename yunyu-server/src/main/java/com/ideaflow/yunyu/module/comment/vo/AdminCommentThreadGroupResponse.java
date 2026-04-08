package com.ideaflow.yunyu.module.comment.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 后台评论文章分组响应类。
 * 作用：向后台评论树形审核页返回某篇文章下的评论概览、状态统计和评论树结构。
 */
@Data
public class AdminCommentThreadGroupResponse {

    private Long postId;
    private String postTitle;
    private String postSlug;
    private Long totalCommentCount;
    private Long pendingCommentCount;
    private Long approvedCommentCount;
    private Long rejectedCommentCount;
    private LocalDateTime latestCommentTime;
    private List<AdminCommentThreadRootItemResponse> roots;
}
