package com.ideaflow.yunyu.module.comment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台评论条目响应类。
 * 作用：向评论管理页返回评论内容、文章信息、作者信息和审核状态等展示字段。
 */
@Data
public class AdminCommentItemResponse {

    @Schema(description = "评论 ID。", example = "101")
    private Long id;
    @Schema(description = "所属文章 ID。", example = "18")
    private Long postId;
    @Schema(description = "所属文章标题。", example = "Spring Boot 与 Nuxt 联调记录")
    private String postTitle;
    @Schema(description = "所属文章 slug。", example = "spring-boot-nuxt-integration-notes")
    private String postSlug;
    @Schema(description = "评论用户 ID。", example = "3")
    private Long userId;
    @Schema(description = "评论用户名。", example = "内容编辑小王")
    private String userName;
    @Schema(description = "评论用户邮箱。", example = "editor@example.com")
    private String userEmail;
    @Schema(description = "直接回复目标评论 ID。根评论时为空。", example = "88")
    private Long replyCommentId;
    @Schema(description = "评论根节点 ID。根评论时通常与当前评论 ID 一致或为空。", example = "80")
    private Long rootId;
    @Schema(description = "被回复用户名称。", example = "站长")
    private String replyToUserName;
    @Schema(description = "评论内容。", example = "这篇文章写得很清楚。")
    private String content;
    @Schema(description = "评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;
    @Schema(description = "评论来源 IP。", example = "127.0.0.1")
    private String ip;
    @Schema(description = "创建时间。", example = "2026-04-26T20:30:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
