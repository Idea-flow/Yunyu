package com.ideaflow.yunyu.module.topic.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台专题条目响应类。
 * 作用：向后台专题管理页返回单个专题的展示信息，便于前端列表和表单复用。
 */
@Data
public class AdminTopicItemResponse {

    @Schema(description = "专题 ID。", example = "5")
    private Long id;
    @Schema(description = "专题名称。", example = "全栈实践")
    private String name;
    @Schema(description = "专题 slug。", example = "full-stack-practice")
    private String slug;
    @Schema(description = "专题描述。", example = "围绕前后端协作与工程实践的专题合集。")
    private String description;
    @Schema(description = "专题封面图地址。", example = "https://cdn.example.com/topics/full-stack.png")
    private String coverUrl;
    @Schema(description = "专题状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "排序值。", example = "5")
    private Integer sortOrder;
    @Schema(description = "关联文章数。", example = "18")
    private Long relatedPostCount;
    @Schema(description = "创建时间。", example = "2026-04-01T10:00:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
