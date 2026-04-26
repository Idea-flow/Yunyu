package com.ideaflow.yunyu.module.storage.model;

import java.util.List;
import lombok.Data;

/**
 * 站点 S3 存储配置模型。
 * 作用：描述站点配置中心中 S3 多配置集合与当前启用配置键。
 */
@Data
public class SiteStorageS3Config {

    private String activeProfileKey;
    private List<SiteStorageS3Profile> profiles;
}
