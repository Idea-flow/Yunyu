package com.ideaflow.yunyu.module.comment.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台评论条目响应类。
 * 作用：向评论管理页返回评论内容、文章信息、作者信息和审核状态等展示字段。
 */
@Data
public class AdminCommentItemResponse {

    private Long id;
    private Long postId;
    private String postTitle;
    private String postSlug;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long replyCommentId;
    private Long rootId;
    private String replyToUserName;
    private String content;
    private String status;
    private String ip;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
