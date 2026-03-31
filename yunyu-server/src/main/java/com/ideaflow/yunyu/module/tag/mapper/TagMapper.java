package com.ideaflow.yunyu.module.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 标签 Mapper 接口。
 * 作用：提供 `tag` 表基础 CRUD 能力，并支持统计标签关联文章数量。
 */
public interface TagMapper extends BaseMapper<TagEntity> {

    /**
     * 统计标签关联文章数。
     *
     * @param tagId 标签ID
     * @return 关联文章数
     */
    @Select("SELECT COUNT(*) FROM post_tag WHERE tag_id = #{tagId}")
    Long countRelatedPostByTagId(@Param("tagId") Long tagId);
}
