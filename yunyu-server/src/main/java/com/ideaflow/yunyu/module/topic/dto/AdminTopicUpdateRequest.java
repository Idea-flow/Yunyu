package com.ideaflow.yunyu.module.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台更新专题请求类。
 * 作用：承接后台编辑专题时提交的字段，并在更新前完成基础参数校验。
 */
@Data
public class AdminTopicUpdateRequest {

    @NotBlank(message = "专题名称不能为空")
    @Size(max = 100, message = "专题名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "Slug不能为空")
    @Size(max = 120, message = "Slug长度不能超过120个字符")
    private String slug;

    @Size(max = 500, message = "专题简介长度不能超过500个字符")
    private String description;

    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    private Integer sortOrder;

    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
