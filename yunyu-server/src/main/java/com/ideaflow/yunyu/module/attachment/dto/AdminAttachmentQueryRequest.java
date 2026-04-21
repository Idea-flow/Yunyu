package com.ideaflow.yunyu.module.attachment.dto;

import lombok.Data;

/**
 * 后台附件查询请求类。
 * 作用：承接后台附件列表页的筛选和分页参数。
 */
@Data
public class AdminAttachmentQueryRequest {

    private String keyword;
    private String mimeType;
    private Integer pageNo;
    private Integer pageSize;
}
