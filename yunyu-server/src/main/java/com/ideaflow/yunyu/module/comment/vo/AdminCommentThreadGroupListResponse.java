package com.ideaflow.yunyu.module.comment.vo;

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

    private List<AdminCommentThreadGroupResponse> list;
    private Long total;
    private Long pageNo;
    private Long pageSize;
    private Long totalPages;
}
