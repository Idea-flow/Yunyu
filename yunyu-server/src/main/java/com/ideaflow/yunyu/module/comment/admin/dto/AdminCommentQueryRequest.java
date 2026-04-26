package com.ideaflow.yunyu.module.comment.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台评论列表查询请求类。
 * 作用：承接评论管理页的关键词、状态、文章、用户和分页筛选参数。
 */
@Data
public class AdminCommentQueryRequest {

    @Schema(description = "关键词。可用于匹配评论内容、评论人名称等文本字段。", example = "写得很好")
    private String keyword;

    @Schema(description = "评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    private String status;

    @Schema(description = "文章 ID。仅查看某篇文章下的评论时填写。", example = "18")
    private Long postId;

    @Schema(description = "评论用户 ID。用于按评论作者筛选。", example = "3")
    private Long userId;

    @Schema(description = "页码。默认 1。", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数。默认 10。", example = "10")
    private Integer pageSize = 10;
}
