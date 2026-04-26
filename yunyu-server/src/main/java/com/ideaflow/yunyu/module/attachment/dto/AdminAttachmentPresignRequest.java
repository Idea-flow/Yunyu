package com.ideaflow.yunyu.module.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台附件预签名请求类。
 * 作用：承接前端在上传前提交的文件基础元信息，用于后端生成预签名上传地址。
 */
@Data
public class AdminAttachmentPresignRequest {

    @Schema(description = "原始文件名。用于生成对象键和回显展示。", example = "spring-boot-guide.png")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件 MIME 类型。需与后续真实上传内容保持一致。", example = "image/png")
    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @Schema(description = "文件大小，单位字节。必须大于 0，且会受当前启用 S3 配置的最大文件大小限制。", example = "245760")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long sizeBytes;
}
