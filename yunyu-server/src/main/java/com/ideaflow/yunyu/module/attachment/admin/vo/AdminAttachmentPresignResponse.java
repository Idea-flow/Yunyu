package com.ideaflow.yunyu.module.attachment.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * 后台附件预签名响应类。
 * 作用：向前端返回直传所需的预签名地址、请求头和对象访问信息。
 */
@Data
public class AdminAttachmentPresignResponse {

    @Schema(description = "预签名上传地址。客户端应按返回的 HTTP 方法和请求头直传文件。", example = "https://example-bucket.s3.amazonaws.com/attachments/2026/04/spring-boot-guide.png?X-Amz-Algorithm=...")
    private String uploadUrl;
    @Schema(description = "上传所需的 HTTP 方法。", example = "PUT")
    private String httpMethod;
    @Schema(description = "上传所需附加请求头。", example = "{\"Content-Type\":\"image/png\"}")
    private Map<String, String> headers;
    @Schema(description = "命中的存储配置键。", example = "r2-main")
    private String storageConfigKey;
    @Schema(description = "目标 Bucket 名称。", example = "yunyu-assets")
    private String bucket;
    @Schema(description = "目标 object key。", example = "attachments/2026/04/spring-boot-guide.png")
    private String objectKey;
    @Schema(description = "上传完成后可访问的资源地址。", example = "https://cdn.example.com/attachments/2026/04/spring-boot-guide.png")
    private String accessUrl;
    @Schema(description = "预签名过期时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime expireAt;
}
