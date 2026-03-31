package com.ideaflow.yunyu.module.post.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台文章列表响应类。
 * 作用：统一返回后台文章列表数据和分页信息，便于前端列表和分页组件直接消费。
 */
@Data
@AllArgsConstructor
public class AdminPostListResponse {

    private final List<AdminPostItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;
}
