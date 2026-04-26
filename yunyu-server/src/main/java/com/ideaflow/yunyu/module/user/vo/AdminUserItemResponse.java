package com.ideaflow.yunyu.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台用户条目响应类。
 * 作用：向后台用户管理页面返回单个用户的展示信息，避免直接暴露实体类中的敏感字段。
 */
@Data
public class AdminUserItemResponse {

    @Schema(description = "用户 ID。", example = "3")
    private Long id;
    @Schema(description = "邮箱。", example = "editor@example.com")
    private String email;
    @Schema(description = "用户名。", example = "内容编辑小王")
    private String userName;
    @Schema(description = "头像地址。", example = "https://cdn.example.com/avatars/editor.png")
    private String avatarUrl;
    @Schema(description = "角色。SUPER_ADMIN=站长，USER=普通用户。", example = "USER", allowableValues = {"SUPER_ADMIN", "USER"})
    private String role;
    @Schema(description = "状态。ACTIVE=启用，DISABLED=禁用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "最后登录时间。", example = "2026-04-26T20:30:00")
    private LocalDateTime lastLoginAt;
    @Schema(description = "最后登录 IP。", example = "127.0.0.1")
    private String lastLoginIp;
    @Schema(description = "创建时间。", example = "2026-04-01T10:00:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
