package com.ideaflow.yunyu.module.topic.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台创建专题请求类。
 * 作用：承接后台创建专题时提交的字段，并在进入业务层前完成基础参数校验。
 */
@Data
public class AdminTopicCreateRequest {

    @Schema(description = "专题名称。创建专题时必填。", example = "Nuxt 工程实践")
    @NotBlank(message = "专题名称不能为空")
    @Size(max = 100, message = "专题名称长度不能超过100个字符")
    private String name;

    @Schema(description = "专题 slug。建议使用小写英文和连字符。", example = "nuxt-engineering-practice")
    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Schema(description = "专题简介。可选。", example = "持续收录 Nuxt 项目的工程化实践与部署经验。")
    @Size(max = 500, message = "专题简介长度不能超过500个字符")
    private String description;

    @Schema(description = "专题封面图片地址。可选。", example = "https://cdn.example.com/topics/nuxt.png")
    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @Schema(description = "专题排序值。数值越小越靠前。", example = "20")
    private Integer sortOrder;

    @Schema(description = "专题状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
