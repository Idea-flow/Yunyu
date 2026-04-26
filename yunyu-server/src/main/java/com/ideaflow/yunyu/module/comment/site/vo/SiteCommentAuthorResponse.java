package com.ideaflow.yunyu.module.comment.site.vo;

import lombok.Data;

/**
 * 前台评论作者响应类。
 * 作用：向文章评论区返回作者基础展示信息，避免直接暴露用户实体。
 */
@Data
public class SiteCommentAuthorResponse {

    private Long userId;
    private String userName;
    private String avatarUrl;
}
