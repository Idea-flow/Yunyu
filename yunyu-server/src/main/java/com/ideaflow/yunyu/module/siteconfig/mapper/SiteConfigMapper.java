package com.ideaflow.yunyu.module.siteconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.siteconfig.entity.SiteConfigEntity;

/**
 * 站点配置 Mapper 接口。
 * 作用：提供 `site_config` 表的基础 CRUD 能力，供前台聚合接口读取站点基础配置。
 */
public interface SiteConfigMapper extends BaseMapper<SiteConfigEntity> {
}
