package com.ideaflow.yunyu.module.tag.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台更新标签请求类。
 * 作用：承接后台编辑标签时提交的字段，并在更新前完成基础参数校验。
 */
@Data
public class AdminTagUpdateRequest {

    @Schema(description = "标签名称。更新标签时必填。", example = "Spring Boot")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称长度不能超过64个字符")
    private String name;

    @Schema(description = "标签 slug。更新时必填。", example = "spring-boot")
    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Schema(description = "标签描述。可选。", example = "与 Spring Boot 相关的实践记录。")
    @Size(max = 255, message = "标签描述长度不能超过255个字符")
    private String description;

    @Schema(description = "标签状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
