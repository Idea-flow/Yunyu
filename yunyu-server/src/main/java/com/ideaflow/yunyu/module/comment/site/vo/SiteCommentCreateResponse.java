package com.ideaflow.yunyu.module.comment.site.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前台评论发布响应类。
 * 作用：向文章详情页反馈评论是否立即可见以及当前审核状态提示文案。
 */
@Data
@AllArgsConstructor
public class SiteCommentCreateResponse {

    private Long commentId;
    private String status;
    private Boolean visible;
    private String message;
}
