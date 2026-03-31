package com.ideaflow.yunyu.module.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.auth.entity.UserAuthEntity;

/**
 * 用户认证方式 Mapper 接口。
 * 作用：提供 `user_auth` 表的基础 CRUD 与绑定关系查询能力。
 */
public interface UserAuthMapper extends BaseMapper<UserAuthEntity> {
}
