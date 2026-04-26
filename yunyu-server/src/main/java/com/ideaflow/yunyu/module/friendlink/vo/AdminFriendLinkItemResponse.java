package com.ideaflow.yunyu.module.friendlink.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台友链条目响应类。
 * 作用：向后台友链管理页返回单条友链的站点信息、联系信息、排序与审核状态。
 */
@Data
public class AdminFriendLinkItemResponse {

    @Schema(description = "友链 ID。", example = "9")
    private Long id;
    @Schema(description = "站点名称。", example = "IdeaFlow")
    private String siteName;
    @Schema(description = "站点地址。", example = "https://ideaflow.example.com")
    private String siteUrl;
    @Schema(description = "站点 Logo 地址。", example = "https://cdn.example.com/logos/ideaflow.png")
    private String logoUrl;
    @Schema(description = "站点简介。", example = "专注工程实践与产品思考。")
    private String description;
    @Schema(description = "联系人名称。", example = "王小明")
    private String contactName;
    @Schema(description = "联系邮箱。", example = "editor@example.com")
    private String contactEmail;
    @Schema(description = "申请留言或备注。", example = "欢迎互链。")
    private String contactMessage;
    @Schema(description = "站点主题色。", example = "#2563EB")
    private String themeColor;
    @Schema(description = "排序值。值越小越靠前。", example = "10")
    private Integer sortOrder;
    @Schema(description = "友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "OFFLINE"})
    private String status;
    @Schema(description = "创建时间。", example = "2026-04-26T20:30:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
