package com.ideaflow.yunyu.module.site.dto;

import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置更新请求类。
 * 作用：承接后台 S3 配置页提交的完整配置集合与当前启用配置标识。
 */
@Data
public class AdminSiteStorageS3ConfigUpdateRequest {

    private String activeProfileKey;
    private List<AdminSiteStorageS3ProfileRequest> profiles;
}
