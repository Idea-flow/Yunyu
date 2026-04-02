package com.ideaflow.yunyu.module.comment.vo;

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

    private List<AdminCommentItemResponse> list;
    private Long total;
    private Long pageNo;
    private Long pageSize;
    private Long totalPages;
}
