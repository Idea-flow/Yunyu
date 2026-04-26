package com.ideaflow.yunyu.module.comment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台评论列表响应类。
 * 作用：承接评论管理页列表数据与分页信息，便于后台统一渲染。
 */
@Data
@AllArgsConstructor
public class AdminCommentListResponse {

    @Schema(description = "评论列表数据。")
    private List<AdminCommentItemResponse> list;
    @Schema(description = "总记录数。", example = "58")
    private Long total;
    @Schema(description = "当前页码。", example = "1")
    private Long pageNo;
    @Schema(description = "当前每页条数。", example = "10")
    private Long pageSize;
    @Schema(description = "总页数。", example = "6")
    private Long totalPages;
}
