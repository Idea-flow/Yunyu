package com.ideaflow.yunyu.module.contentaccess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.contentaccess.entity.ContentAccessGrantEntity;

/**
 * 内容访问授权缓存 Mapper 接口。
 * 作用：提供 `content_access_grant` 表的基础读写能力，供内容访问控制服务复用。
 */
public interface ContentAccessGrantMapper extends BaseMapper<ContentAccessGrantEntity> {
}
