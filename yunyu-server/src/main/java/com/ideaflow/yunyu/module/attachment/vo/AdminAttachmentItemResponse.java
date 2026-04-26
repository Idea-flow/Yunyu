package com.ideaflow.yunyu.module.attachment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台附件条目响应类。
 * 作用：向后台附件管理和上传回执返回单个附件记录信息。
 */
@Data
public class AdminAttachmentItemResponse {

    @Schema(description = "附件 ID。", example = "12")
    private Long id;
    @Schema(description = "原始文件名。", example = "spring-boot-guide.png")
    private String fileName;
    @Schema(description = "文件扩展名。", example = "png")
    private String fileExt;
    @Schema(description = "文件 MIME 类型。", example = "image/png")
    private String mimeType;
    @Schema(description = "文件大小，单位字节。", example = "245760")
    private Long sizeBytes;
    @Schema(description = "文件 SHA-256 摘要。", example = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a")
    private String sha256;
    @Schema(description = "存储提供商标识。", example = "S3")
    private String storageProvider;
    @Schema(description = "命中的存储配置键。", example = "r2-main")
    private String storageConfigKey;
    @Schema(description = "对象所在 Bucket。", example = "yunyu-assets")
    private String bucket;
    @Schema(description = "对象在存储中的 object key。", example = "attachments/2026/04/spring-boot-guide.png")
    private String objectKey;
    @Schema(description = "对外访问地址。", example = "https://cdn.example.com/attachments/2026/04/spring-boot-guide.png")
    private String accessUrl;
    @Schema(description = "对象存储返回的 ETag。", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
    private String etag;
    @Schema(description = "上传者用户 ID。", example = "1")
    private Long uploaderUserId;
    @Schema(description = "创建时间。", example = "2026-04-26T20:30:00")
    private LocalDateTime createdTime;
    @Schema(description = "更新时间。", example = "2026-04-26T20:35:00")
    private LocalDateTime updatedTime;
}
