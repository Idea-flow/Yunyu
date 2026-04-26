package com.ideaflow.yunyu.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台更新用户请求类。
 * 作用：承接站长在后台编辑用户时提交的字段，用于更新用户基础信息、状态和权限。
 */
@Data
public class AdminUserUpdateRequest {

    @Schema(description = "用户邮箱。更新后仍需保持全站唯一。", example = "editor@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    @Schema(description = "用户名。用于后台与前台展示。", example = "内容编辑小王")
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String userName;

    @Schema(description = "头像图片地址。可为空。", example = "https://cdn.example.com/avatars/editor.png")
    @Size(max = 255, message = "头像地址长度不能超过255个字符")
    private String avatarUrl;

    @Schema(description = "登录密码。可为空；仅在需要重置密码时填写，长度需在 6-32 个字符之间。", example = "yunyu123456")
    @Size(min = 6, max = 32, message = "密码长度需在6到32个字符之间")
    private String password;

    @Schema(description = "用户角色。SUPER_ADMIN=站长，USER=普通用户。调整为 SUPER_ADMIN 前应确认授权。", example = "USER", allowableValues = {"SUPER_ADMIN", "USER"})
    @Pattern(regexp = "SUPER_ADMIN|USER", message = "角色不合法")
    private String role;

    @Schema(description = "用户状态。ACTIVE=启用，DISABLED=禁用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态不合法")
    private String status;
}
