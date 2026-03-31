package com.ideaflow.yunyu.module.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台创建文章请求类。
 * 作用：承接后台创建文章时提交的主表与正文表字段，并在进入业务层前完成参数校验。
 */
@Data
public class AdminPostCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "Slug不能为空")
    @Size(max = 220, message = "Slug长度不能超过220个字符")
    private String slug;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    private String summary;

    @Size(max = 255, message = "封面地址长度不能超过255个字符")
    private String coverUrl;

    @Pattern(regexp = "DRAFT|PUBLISHED|OFFLINE", message = "状态不合法")
    private String status;

    @Size(max = 255, message = "SEO标题长度不能超过255个字符")
    private String seoTitle;

    @Size(max = 500, message = "SEO描述长度不能超过500个字符")
    private String seoDescription;

    private String contentMarkdown;
}
