package com.ideaflow.yunyu.module.storage.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置项响应类。
 * 作用：向后台 S3 配置页返回单个配置项的完整展示字段。
 */
@Data
public class AdminSiteStorageS3ProfileResponse {

    @Schema(description = "S3 配置键。", example = "r2-main")
    private String profileKey;
    @Schema(description = "S3 配置名称。", example = "Cloudflare R2 主配置")
    private String name;
    @Schema(description = "该配置是否启用。", example = "true")
    private Boolean enabled;
    @Schema(description = "S3 Endpoint 地址。", example = "https://<account-id>.r2.cloudflarestorage.com")
    private String endpoint;
    @Schema(description = "S3 区域标识。", example = "auto")
    private String region;
    @Schema(description = "目标 Bucket 名称。", example = "yunyu-assets")
    private String bucket;
    @Schema(description = "S3 Access Key。", example = "AKIAIOSFODNN7EXAMPLE")
    private String accessKey;
    @Schema(description = "S3 Secret Key。", example = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
    private String secretKey;
    @Schema(description = "是否启用 Path Style 访问。", example = "false")
    private Boolean pathStyleAccess;
    @Schema(description = "公开访问基地址。", example = "https://cdn.example.com")
    private String publicBaseUrl;
    @Schema(description = "预签名有效期，单位秒。", example = "300")
    private Integer presignExpireSeconds;
    @Schema(description = "允许上传的最大文件大小，单位 MB。", example = "20")
    private Integer maxFileSizeMb;
    @Schema(description = "允许上传的 MIME 类型列表。", example = "[\"image/png\",\"image/jpeg\",\"video/mp4\"]")
    private List<String> allowedContentTypes;
}
