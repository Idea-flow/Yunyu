package com.ideaflow.yunyu.module.attachment.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台附件查询请求类。
 * 作用：承接后台附件列表页的筛选和分页参数。
 */
@Data
public class AdminAttachmentQueryRequest {

    @Schema(description = "关键词。会按文件名等可搜索字段模糊匹配。", example = "spring-boot")
    private String keyword;

    @Schema(description = "MIME 类型筛选。可按图片、视频等具体类型过滤。", example = "image/png")
    private String mimeType;

    @Schema(description = "页码。未传时后端会按默认分页处理。", example = "1")
    private Integer pageNo;

    @Schema(description = "每页条数。未传时后端会按默认分页处理。", example = "10")
    private Integer pageSize;
}
