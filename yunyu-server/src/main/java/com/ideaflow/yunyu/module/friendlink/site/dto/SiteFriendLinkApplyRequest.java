package com.ideaflow.yunyu.module.friendlink.site.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台友链申请请求类。
 * 作用：承接前台友链申请页提交的站点资料与联系信息，并在进入业务层前完成参数校验。
 */
@Data
public class SiteFriendLinkApplyRequest {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 100, message = "站点名称长度不能超过100个字符")
    private String siteName;

    @NotBlank(message = "站点地址不能为空")
    @Size(max = 255, message = "站点地址长度不能超过255个字符")
    @Pattern(regexp = "^https?://.+", message = "站点地址需以 http:// 或 https:// 开头")
    private String siteUrl;

    @Size(max = 255, message = "图标地址长度不能超过255个字符")
    private String logoUrl;

    @Size(max = 255, message = "站点简介长度不能超过255个字符")
    private String description;

    @NotBlank(message = "联系人名称不能为空")
    @Size(max = 64, message = "联系人名称长度不能超过64个字符")
    private String contactName;

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 128, message = "联系邮箱长度不能超过128个字符")
    private String contactEmail;

    @Size(max = 500, message = "申请留言长度不能超过500个字符")
    private String contactMessage;

    @Pattern(regexp = "^#([0-9a-fA-F]{6})$", message = "主题色需为 #RRGGBB 格式")
    private String themeColor;
}
