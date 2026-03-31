package com.ideaflow.yunyu.module.category.dto;

import lombok.Data;

/**
 * 后台分类列表查询请求类。
 * 作用：承接后台分类列表的搜索、状态筛选和分页参数。
 */
@Data
public class AdminCategoryQueryRequest {

    private String keyword;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
