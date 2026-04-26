package com.ideaflow.yunyu.module.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台附件上传完成请求类。
 * 作用：承接前端完成直传后提交的对象定位信息和文件哈希，用于后端落库与去重。
 */
@Data
public class AdminAttachmentCompleteRequest {

    @Schema(description = "原始文件名。应与申请预签名时提交的文件名一致。", example = "spring-boot-guide.png")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @Schema(description = "文件 MIME 类型。应与真实上传文件保持一致。", example = "image/png")
    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @Schema(description = "文件大小，单位字节。", example = "245760")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long sizeBytes;

    @Schema(description = "文件内容的 SHA-256 摘要，用于去重和秒传判断。", example = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a")
    @NotBlank(message = "sha256 不能为空")
    private String sha256;

    @Schema(description = "对象最终所在的 Bucket 名称。通常来自预签名响应。", example = "yunyu-assets")
    @NotBlank(message = "bucket 不能为空")
    private String bucket;

    @Schema(description = "对象最终写入的 object key。通常来自预签名响应。", example = "attachments/2026/04/spring-boot-guide.png")
    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    @Schema(description = "命中的存储配置键。通常来自预签名响应。", example = "r2-main")
    @NotBlank(message = "storageConfigKey 不能为空")
    private String storageConfigKey;

    @Schema(description = "对象存储返回的 ETag。可选；如果客户端可获取，建议原样回传。", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
    private String etag;
}
