package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置响应类。
 * 作用：向后台站点设置页返回当前启用配置标识与多套 S3 配置项列表。
 */
@Data
public class AdminSiteStorageS3ConfigResponse {

    private String activeProfileKey;
    private List<AdminSiteStorageS3ProfileResponse> profiles;
}
