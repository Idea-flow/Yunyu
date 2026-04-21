package com.ideaflow.yunyu.module.site.model;

import java.util.List;
import lombok.Data;

/**
 * 站点 S3 存储配置项模型。
 * 作用：作为站点配置中心在服务层内部流转的标准 S3 配置对象。
 */
@Data
public class SiteStorageS3Profile {

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
