package com.ideaflow.yunyu.module.comment.site.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 前台评论条目响应类。
 * 作用：描述文章详情页评论区的楼层、回复对象和回复列表结构。
 */
@Data
public class SiteCommentItemResponse {

    private Long id;
    private Long postId;
    private Long replyCommentId;
    private Long rootId;
    private String content;
    private String replyToUserName;
    private LocalDateTime createdTime;
    private SiteCommentAuthorResponse author;
    private List<SiteCommentItemResponse> replies;
}
