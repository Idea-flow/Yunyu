package com.ideaflow.yunyu.module.category.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台创建分类请求类。
 * 作用：承接后台创建分类时提交的字段，并在进入业务层前完成基础参数校验。
 */
@Data
public class AdminCategoryCreateRequest {

    @Schema(description = "分类名称。创建分类时必填。", example = "后端开发")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    @Schema(description = "分类 slug。建议使用小写英文和连字符。", example = "backend-development")
    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Schema(description = "分类描述。可选。", example = "记录 Spring Boot、数据库与服务端工程实践。")
    @Size(max = 255, message = "分类描述长度不能超过255个字符")
    private String description;

    @Schema(description = "分类封面图片地址。可选。", example = "https://cdn.example.com/categories/backend.png")
    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @Schema(description = "分类排序值。数值越小越靠前。", example = "10")
    private Integer sortOrder;

    @Schema(description = "分类状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
