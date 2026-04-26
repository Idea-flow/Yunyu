package com.ideaflow.yunyu.module.user.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台用户列表查询请求类。
 * 作用：承接站长在后台对用户列表进行搜索、角色筛选和状态筛选时的查询参数。
 */
@Data
public class AdminUserQueryRequest {

    @Schema(description = "搜索关键词。可匹配邮箱或用户名。", example = "editor")
    private String keyword;
    @Schema(description = "角色筛选。SUPER_ADMIN=站长，USER=普通用户。", example = "USER", allowableValues = {"SUPER_ADMIN", "USER"})
    private String role;
    @Schema(description = "状态筛选。ACTIVE=启用，DISABLED=禁用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "页码。默认 1。", example = "1")
    private Integer pageNo = 1;
    @Schema(description = "每页条数。默认 10。", example = "10")
    private Integer pageSize = 10;
}
