package com.ideaflow.yunyu.module.attachment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台附件预签名请求类。
 * 作用：承接前端在上传前提交的文件基础元信息，用于后端生成预签名上传地址。
 */
@Data
public class AdminAttachmentPresignRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @Min(value = 1, message = "文件大小必须大于0")
    private Long sizeBytes;
}
