package com.ideaflow.yunyu.module.category.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台分类条目响应类。
 * 作用：向后台分类管理页返回单个分类的展示信息，便于前端列表和表单复用。
 */
@Data
public class AdminCategoryItemResponse {

    @Schema(description = "分类 ID。", example = "12")
    private Long id;
    @Schema(description = "分类名称。", example = "后端")
    private String name;
    @Schema(description = "分类 slug。", example = "backend")
    private String slug;
    @Schema(description = "分类描述。", example = "后端开发相关文章归档。")
    private String description;
    @Schema(description = "分类封面图地址。", example = "https://cdn.example.com/categories/backend.png")
    private String coverUrl;
    @Schema(description = "分类状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "排序值。", example = "10")
    private Integer sortOrder;
    @Schema(description = "关联文章数。", example = "36")
    private Long relatedPostCount;
    @Schema(description = "创建时间。", example = "2026-04-01T10:00:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
