package com.ideaflow.yunyu.module.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.comment.entity.CommentEntity;

/**
 * 评论 Mapper 接口。
 * 作用：提供 `comment` 表的基础 CRUD、前台评论查询和后台审核管理查询能力。
 */
public interface CommentMapper extends BaseMapper<CommentEntity> {
}
