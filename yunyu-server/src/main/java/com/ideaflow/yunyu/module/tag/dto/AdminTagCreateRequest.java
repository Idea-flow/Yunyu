package com.ideaflow.yunyu.module.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台创建标签请求类。
 * 作用：承接后台创建标签时提交的字段，并在进入业务层前完成基础参数校验。
 */
@Data
public class AdminTagCreateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称长度不能超过64个字符")
    private String name;

    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Size(max = 255, message = "标签描述长度不能超过255个字符")
    private String description;

    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
