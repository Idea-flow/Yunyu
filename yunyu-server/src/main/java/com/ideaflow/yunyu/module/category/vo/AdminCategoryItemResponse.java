package com.ideaflow.yunyu.module.category.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台分类条目响应类。
 * 作用：向后台分类管理页返回单个分类的展示信息，便于前端列表和表单复用。
 */
@Data
public class AdminCategoryItemResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String coverUrl;
    private String status;
    private Integer sortOrder;
    private Long relatedPostCount;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
