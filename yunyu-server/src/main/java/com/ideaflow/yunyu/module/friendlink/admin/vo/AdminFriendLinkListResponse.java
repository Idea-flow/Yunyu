package com.ideaflow.yunyu.module.friendlink.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台友链列表响应类。
 * 作用：统一返回后台友链列表数据和分页信息，便于后台管理页直接消费。
 */
@Data
@AllArgsConstructor
public class AdminFriendLinkListResponse {

    @Schema(description = "友链列表数据。")
    private final List<AdminFriendLinkItemResponse> list;
    @Schema(description = "总记录数。", example = "24")
    private final long total;
    @Schema(description = "当前页码。", example = "1")
    private final long pageNo;
    @Schema(description = "当前每页条数。", example = "10")
    private final long pageSize;
    @Schema(description = "总页数。", example = "3")
    private final long totalPages;
}
