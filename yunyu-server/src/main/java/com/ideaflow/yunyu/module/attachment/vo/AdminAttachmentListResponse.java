package com.ideaflow.yunyu.module.attachment.vo;

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

    private List<AdminAttachmentItemResponse> list;
    private Long total;
    private Long pageNo;
    private Long pageSize;
    private Long totalPages;
}
