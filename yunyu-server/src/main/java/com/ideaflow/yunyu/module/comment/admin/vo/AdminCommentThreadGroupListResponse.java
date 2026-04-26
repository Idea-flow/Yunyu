package com.ideaflow.yunyu.module.comment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台评论树形审核列表响应类。
 * 作用：承接按文章分组后的评论审核视图数据与分页信息，供后台评论管理页统一渲染。
 */
@Data
@AllArgsConstructor
public class AdminCommentThreadGroupListResponse {

    @Schema(description = "按文章分组后的评论树列表。")
    private List<AdminCommentThreadGroupResponse> list;
    @Schema(description = "总记录数。", example = "8")
    private Long total;
    @Schema(description = "当前页码。", example = "1")
    private Long pageNo;
    @Schema(description = "当前每页条数。", example = "10")
    private Long pageSize;
    @Schema(description = "总页数。", example = "1")
    private Long totalPages;
}
