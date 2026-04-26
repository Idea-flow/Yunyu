package com.ideaflow.yunyu.module.tag.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台标签条目响应类。
 * 作用：向后台标签管理页返回单个标签的展示信息，便于前端列表和表单复用。
 */
@Data
public class AdminTagItemResponse {

    @Schema(description = "标签 ID。", example = "3")
    private Long id;
    @Schema(description = "标签名称。", example = "Spring Boot")
    private String name;
    @Schema(description = "标签 slug。", example = "spring-boot")
    private String slug;
    @Schema(description = "标签描述。", example = "与 Spring Boot 相关的实践记录。")
    private String description;
    @Schema(description = "标签封面图地址。", example = "https://cdn.example.com/tags/spring-boot.png")
    private String coverUrl;
    @Schema(description = "标签状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "排序值。", example = "8")
    private Integer sortOrder;
    @Schema(description = "关联文章数。", example = "24")
    private Long relatedPostCount;
    @Schema(description = "创建时间。", example = "2026-04-01T10:00:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
