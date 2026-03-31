package com.ideaflow.yunyu.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.post.entity.TopicPostEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 专题文章关联 Mapper 接口。
 * 作用：提供 `topic_post` 表的基础 CRUD 能力，并支持按文章维度查询所属专题。
 */
public interface TopicPostMapper extends BaseMapper<TopicPostEntity> {

    /**
     * 查询文章绑定的专题ID列表。
     *
     * @param postId 文章ID
     * @return 专题ID列表
     */
    @Select("SELECT topic_id FROM topic_post WHERE post_id = #{postId} ORDER BY sort_order ASC, id ASC")
    List<Long> selectTopicIdsByPostId(@Param("postId") Long postId);

    /**
     * 查询文章绑定的专题名称列表。
     *
     * @param postId 文章ID
     * @return 专题名称列表
     */
    @Select("""
            SELECT t.name
            FROM topic_post tp
            INNER JOIN topic t ON t.id = tp.topic_id
            WHERE tp.post_id = #{postId} AND t.deleted = 0
            ORDER BY tp.sort_order ASC, tp.id ASC
            """)
    List<String> selectTopicNamesByPostId(@Param("postId") Long postId);
}
