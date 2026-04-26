package com.ideaflow.yunyu.module.user.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台用户列表响应类。
 * 作用：统一返回后台用户列表数据和总数，便于前端页面构建管理表格与统计信息。
 */
@Data
@AllArgsConstructor
public class AdminUserListResponse {

    @Schema(description = "用户列表数据。")
    private final List<AdminUserItemResponse> list;
    @Schema(description = "总记录数。", example = "42")
    private final long total;
    @Schema(description = "当前页码。", example = "1")
    private final long pageNo;
    @Schema(description = "当前每页条数。", example = "10")
    private final long pageSize;
    @Schema(description = "总页数。", example = "5")
    private final long totalPages;
}
