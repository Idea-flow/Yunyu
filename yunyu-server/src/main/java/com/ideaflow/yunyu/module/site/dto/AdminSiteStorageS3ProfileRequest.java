package com.ideaflow.yunyu.module.site.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置项请求类。
 * 作用：承接后台站点设置页中单个 S3 配置项的可编辑字段。
 */
@Data
public class AdminSiteStorageS3ProfileRequest {

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
