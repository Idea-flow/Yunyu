package com.ideaflow.yunyu.module.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.post.entity.PostTagEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文章标签关联 Mapper 接口。
 * 作用：提供 `post_tag` 表的基础 CRUD 能力，并支持按文章维度查询关联标签。
 */
public interface PostTagMapper extends BaseMapper<PostTagEntity> {

    /**
     * 查询文章绑定的标签ID列表。
     *
     * @param postId 文章ID
     * @return 标签ID列表
     */
    @Select("SELECT tag_id FROM post_tag WHERE post_id = #{postId} ORDER BY id ASC")
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);

    /**
     * 查询文章绑定的标签名称列表。
     *
     * @param postId 文章ID
     * @return 标签名称列表
     */
    @Select("""
            SELECT t.name
            FROM post_tag pt
            INNER JOIN tag t ON t.id = pt.tag_id
            WHERE pt.post_id = #{postId} AND t.deleted = 0
            ORDER BY pt.id ASC
            """)
    List<String> selectTagNamesByPostId(@Param("postId") Long postId);
}
