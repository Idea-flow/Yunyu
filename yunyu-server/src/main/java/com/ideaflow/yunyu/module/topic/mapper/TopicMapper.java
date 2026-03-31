package com.ideaflow.yunyu.module.topic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 专题 Mapper 接口。
 * 作用：提供 `topic` 表基础 CRUD 能力，并支持统计专题关联文章数量。
 */
public interface TopicMapper extends BaseMapper<TopicEntity> {

    /**
     * 统计专题关联文章数。
     *
     * @param topicId 专题ID
     * @return 关联文章数
     */
    @Select("SELECT COUNT(*) FROM topic_post WHERE topic_id = #{topicId}")
    Long countRelatedPostByTopicId(@Param("topicId") Long topicId);
}
