package com.ideaflow.yunyu.module.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台评论回复项响应类。
 * 作用：向后台评论树形审核页返回根评论下的回复流条目，并保留回复目标与筛选命中信息。
 */
@Data
public class AdminCommentThreadReplyItemResponse {

    @Schema(description = "评论 ID。", example = "102")
    private Long id;
    @Schema(description = "所属文章 ID。", example = "18")
    private Long postId;
    @Schema(description = "所属根评论 ID。", example = "101")
    private Long rootId;
    @Schema(description = "直接回复的评论 ID。", example = "101")
    private Long replyCommentId;
    @Schema(description = "被回复用户名称。", example = "站长")
    private String replyToUserName;
    @Schema(description = "评论用户 ID。", example = "5")
    private Long userId;
    @Schema(description = "评论用户名。", example = "访客张三")
    private String userName;
    @Schema(description = "评论用户邮箱。", example = "visitor@example.com")
    private String userEmail;
    @Schema(description = "评论内容。", example = "我补充一个实践细节。")
    private String content;
    @Schema(description = "评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;
    @Schema(description = "评论来源 IP。", example = "127.0.0.1")
    private String ip;
    @Schema(description = "创建时间。", example = "2026-04-26T20:31:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
    @Schema(description = "当前回复在前台是否可见。", example = "false")
    private Boolean visibleOnSite;
    @Schema(description = "当前回复是否命中后台筛选条件。", example = "true")
    private Boolean matchedByFilter;
}
