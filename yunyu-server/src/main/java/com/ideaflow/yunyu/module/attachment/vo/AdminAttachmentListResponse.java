package com.ideaflow.yunyu.module.attachment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台附件列表响应类。
 * 作用：返回后台附件列表页所需的分页数据和条目集合。
 */
@Data
@AllArgsConstructor
public class AdminAttachmentListResponse {

    @Schema(description = "附件列表数据。")
    private List<AdminAttachmentItemResponse> list;
    @Schema(description = "总记录数。", example = "128")
    private Long total;
    @Schema(description = "当前页码。", example = "1")
    private Long pageNo;
    @Schema(description = "当前每页条数。", example = "10")
    private Long pageSize;
    @Schema(description = "总页数。", example = "13")
    private Long totalPages;
}
