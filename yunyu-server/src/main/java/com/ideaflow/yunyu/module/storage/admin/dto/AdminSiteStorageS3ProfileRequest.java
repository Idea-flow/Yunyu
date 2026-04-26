package com.ideaflow.yunyu.module.storage.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置项请求类。
 * 作用：承接后台站点设置页中单个 S3 配置项的可编辑字段。
 */
@Data
public class AdminSiteStorageS3ProfileRequest {

    @Schema(description = "S3 配置键。建议使用稳定且可读的英文标识。", example = "r2-main")
    private String profileKey;

    @Schema(description = "S3 配置名称。用于后台展示与人工区分。", example = "Cloudflare R2 主配置")
    private String name;

    @Schema(description = "该配置是否启用。保存整组配置时必须且只能有一个为 true。", example = "true")
    private Boolean enabled;

    @Schema(description = "S3 Endpoint 地址。通常为对象存储服务提供的 API 入口。", example = "https://<account-id>.r2.cloudflarestorage.com")
    private String endpoint;

    @Schema(description = "S3 区域标识。未传时默认 auto。", example = "auto")
    private String region;

    @Schema(description = "目标 Bucket 名称。保存或测试连接时必填。", example = "yunyu-assets")
    private String bucket;

    @Schema(description = "S3 Access Key。保存或测试连接时必须提供完整明文值。", example = "AKIAIOSFODNN7EXAMPLE")
    private String accessKey;

    @Schema(description = "S3 Secret Key。保存或测试连接时必须提供完整明文值。", example = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
    private String secretKey;

    @Schema(description = "是否启用 Path Style 访问。多数兼容型对象存储需要按实际厂商要求设置。未传时默认 false。", example = "false")
    private Boolean pathStyleAccess;

    @Schema(description = "公开访问基地址。用于拼接对外资源 URL；如走 CDN 或自定义域名，建议填写完整地址。", example = "https://cdn.example.com")
    private String publicBaseUrl;

    @Schema(description = "预签名上传地址有效期，单位秒。有效范围 60-3600；未传时默认 300。", example = "300")
    private Integer presignExpireSeconds;

    @Schema(description = "允许上传的最大文件大小，单位 MB。有效范围 1-2048；未传时默认 20。", example = "20")
    private Integer maxFileSizeMb;

    @Schema(description = "允许上传的 MIME 类型列表。至少配置一个，保存时会自动去重并转为小写。", example = "[\"image/png\",\"image/jpeg\",\"video/mp4\"]")
    private List<String> allowedContentTypes;
}
