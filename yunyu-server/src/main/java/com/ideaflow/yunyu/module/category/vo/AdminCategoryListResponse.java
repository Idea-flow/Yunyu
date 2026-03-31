package com.ideaflow.yunyu.module.category.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台分类列表响应类。
 * 作用：统一返回后台分类列表数据和分页信息，便于前端分类工作台直接消费。
 */
@Data
@AllArgsConstructor
public class AdminCategoryListResponse {

    private final List<AdminCategoryItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;
}
