package com.ideaflow.yunyu.module.comment.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台评论回复项响应类。
 * 作用：向后台评论树形审核页返回根评论下的回复流条目，并保留回复目标与筛选命中信息。
 */
@Data
public class AdminCommentThreadReplyItemResponse {

    private Long id;
    private Long postId;
    private Long rootId;
    private Long replyCommentId;
    private String replyToUserName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String content;
    private String status;
    private String ip;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Boolean visibleOnSite;
    private Boolean matchedByFilter;
}
