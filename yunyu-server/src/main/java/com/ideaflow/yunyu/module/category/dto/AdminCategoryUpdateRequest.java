package com.ideaflow.yunyu.module.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台更新分类请求类。
 * 作用：承接后台编辑分类时提交的字段，并在更新前完成基础参数校验。
 */
@Data
public class AdminCategoryUpdateRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Size(max = 255, message = "分类描述长度不能超过255个字符")
    private String description;

    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    private Integer sortOrder;

    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
