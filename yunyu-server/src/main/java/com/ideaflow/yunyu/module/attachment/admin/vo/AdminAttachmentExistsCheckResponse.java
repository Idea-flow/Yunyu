package com.ideaflow.yunyu.module.attachment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台附件秒传检查响应类。
 * 作用：向前端返回当前哈希是否命中已上传附件及命中附件详情。
 */
@Data
public class AdminAttachmentExistsCheckResponse {

    @Schema(description = "是否已存在相同 SHA-256 的附件。", example = "true")
    private Boolean exists;
    @Schema(description = "命中时返回的附件详情；未命中时为空。")
    private AdminAttachmentItemResponse attachment;
}
