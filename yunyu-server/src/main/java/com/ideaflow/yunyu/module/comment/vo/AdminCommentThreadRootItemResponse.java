package com.ideaflow.yunyu.module.comment.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 后台评论主评论响应类。
 * 作用：向后台评论树形审核页返回一级评论及其回复流、筛选命中和前台可见性信息。
 */
@Data
public class AdminCommentThreadRootItemResponse {

    private Long id;
    private Long postId;
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
    private Boolean hasMatchingDescendant;
    private List<AdminCommentThreadReplyItemResponse> replies;
}
