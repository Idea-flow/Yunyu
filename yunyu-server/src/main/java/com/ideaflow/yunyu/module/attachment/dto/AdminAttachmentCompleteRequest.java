package com.ideaflow.yunyu.module.attachment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台附件上传完成请求类。
 * 作用：承接前端完成直传后提交的对象定位信息和文件哈希，用于后端落库与去重。
 */
@Data
public class AdminAttachmentCompleteRequest {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @Min(value = 1, message = "文件大小必须大于0")
    private Long sizeBytes;

    @NotBlank(message = "sha256 不能为空")
    private String sha256;

    @NotBlank(message = "bucket 不能为空")
    private String bucket;

    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    @NotBlank(message = "storageConfigKey 不能为空")
    private String storageConfigKey;

    private String etag;
}
