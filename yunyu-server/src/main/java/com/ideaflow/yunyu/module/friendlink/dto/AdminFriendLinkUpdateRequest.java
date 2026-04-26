package com.ideaflow.yunyu.module.friendlink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台更新友链请求类。
 * 作用：承接后台编辑友链时提交的站点信息、联系信息与审核状态字段，并在进入业务层前完成参数校验。
 */
@Data
public class AdminFriendLinkUpdateRequest {

    @Schema(description = "友链站点名称。更新时必填。", example = "IdeaFlow")
    @NotBlank(message = "站点名称不能为空")
    @Size(max = 100, message = "站点名称长度不能超过100个字符")
    private String siteName;

    @Schema(description = "友链站点地址。必须以 http:// 或 https:// 开头。", example = "https://ideaflow.example.com")
    @NotBlank(message = "站点地址不能为空")
    @Size(max = 255, message = "站点地址长度不能超过255个字符")
    @Pattern(regexp = "^https?://.+", message = "站点地址需以 http:// 或 https:// 开头")
    private String siteUrl;

    @Schema(description = "站点 Logo 或图标地址。可为空。", example = "https://cdn.example.com/logos/ideaflow.png")
    @Size(max = 255, message = "图标地址长度不能超过255个字符")
    private String logoUrl;

    @Schema(description = "站点简介。用于后台展示和前台友链卡片说明。", example = "专注工程实践与产品思考。")
    @Size(max = 255, message = "站点简介长度不能超过255个字符")
    private String description;

    @Schema(description = "联系人名称。可为空。", example = "王小明")
    @Size(max = 64, message = "联系人名称长度不能超过64个字符")
    private String contactName;

    @Schema(description = "联系邮箱。可为空。", example = "editor@example.com")
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 128, message = "联系邮箱长度不能超过128个字符")
    private String contactEmail;

    @Schema(description = "申请留言或备注。可为空。", example = "已在本站添加贵站友链，欢迎互链。")
    @Size(max = 500, message = "申请留言长度不能超过500个字符")
    private String contactMessage;

    @Schema(description = "站点主题色。格式必须为 #RRGGBB。", example = "#2563EB")
    @Pattern(regexp = "^#([0-9a-fA-F]{6})$", message = "主题色需为 #RRGGBB 格式")
    private String themeColor;

    @Schema(description = "排序值。值越小越靠前；未传时按后端默认处理。", example = "10")
    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;

    @Schema(description = "友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "OFFLINE"})
    @Pattern(regexp = "PENDING|APPROVED|REJECTED|OFFLINE", message = "状态不合法")
    private String status;
}
