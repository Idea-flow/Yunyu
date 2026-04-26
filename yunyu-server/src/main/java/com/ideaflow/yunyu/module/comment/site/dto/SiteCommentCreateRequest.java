package com.ideaflow.yunyu.module.comment.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台发布评论请求类。
 * 作用：承接前台文章详情页提交的评论内容与回复目标，并在进入业务层前完成基础校验。
 */
@Data
public class SiteCommentCreateRequest {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000个字符")
    private String content;

    private Long replyCommentId;
}
