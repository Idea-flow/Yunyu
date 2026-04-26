package com.ideaflow.yunyu.module.comment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 后台评论文章分组响应类。
 * 作用：向后台评论树形审核页返回某篇文章下的评论概览、状态统计和评论树结构。
 */
@Data
public class AdminCommentThreadGroupResponse {

    @Schema(description = "文章 ID。", example = "18")
    private Long postId;
    @Schema(description = "文章标题。", example = "Spring Boot 与 Nuxt 联调记录")
    private String postTitle;
    @Schema(description = "文章 slug。", example = "spring-boot-nuxt-integration-notes")
    private String postSlug;
    @Schema(description = "该文章下的评论总数。", example = "24")
    private Long totalCommentCount;
    @Schema(description = "待审核评论数。", example = "5")
    private Long pendingCommentCount;
    @Schema(description = "已通过评论数。", example = "17")
    private Long approvedCommentCount;
    @Schema(description = "已驳回评论数。", example = "2")
    private Long rejectedCommentCount;
    @Schema(description = "最新评论时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime latestCommentTime;
    @Schema(description = "该文章下的根评论列表。")
    private List<AdminCommentThreadRootItemResponse> roots;
}
