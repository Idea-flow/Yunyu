package com.ideaflow.yunyu.module.attachment.vo;

import lombok.Data;

/**
 * 后台附件秒传检查响应类。
 * 作用：向前端返回当前哈希是否命中已上传附件及命中附件详情。
 */
@Data
public class AdminAttachmentExistsCheckResponse {

    private Boolean exists;
    private AdminAttachmentItemResponse attachment;
}

