package com.ideaflow.yunyu.module.comment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 后台评论主评论响应类。
 * 作用：向后台评论树形审核页返回一级评论及其回复流、筛选命中和前台可见性信息。
 */
@Data
public class AdminCommentThreadRootItemResponse {

    @Schema(description = "评论 ID。", example = "101")
    private Long id;
    @Schema(description = "所属文章 ID。", example = "18")
    private Long postId;
    @Schema(description = "评论用户 ID。", example = "3")
    private Long userId;
    @Schema(description = "评论用户名。", example = "内容编辑小王")
    private String userName;
    @Schema(description = "评论用户邮箱。", example = "editor@example.com")
    private String userEmail;
    @Schema(description = "评论内容。", example = "这篇文章写得很清楚。")
    private String content;
    @Schema(description = "评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;
    @Schema(description = "评论来源 IP。", example = "127.0.0.1")
    private String ip;
    @Schema(description = "创建时间。", example = "2026-04-26T20:30:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
    @Schema(description = "当前评论在前台是否可见。", example = "true")
    private Boolean visibleOnSite;
    @Schema(description = "当前评论本身是否命中后台筛选条件。", example = "true")
    private Boolean matchedByFilter;
    @Schema(description = "是否存在命中筛选条件的后代回复。", example = "false")
    private Boolean hasMatchingDescendant;
    @Schema(description = "当前根评论下的回复列表。")
    private List<AdminCommentThreadReplyItemResponse> replies;
}
