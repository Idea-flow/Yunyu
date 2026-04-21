package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置项响应类。
 * 作用：向后台 S3 配置页返回单个配置项的完整展示字段。
 */
@Data
public class AdminSiteStorageS3ProfileResponse {

    private String profileKey;
    private String name;
    private Boolean enabled;
    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
    private Boolean pathStyleAccess;
    private String publicBaseUrl;
    private Integer presignExpireSeconds;
    private Integer maxFileSizeMb;
    private List<String> allowedContentTypes;
}
