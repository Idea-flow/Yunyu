package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文章标签关联实体类。
 * 作用：映射 `post_tag` 表，为文章与标签之间的多对多关联关系提供持久化对象。
 */
@Data
@TableName("post_tag")
public class PostTagEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long tagId;
    private LocalDateTime createdTime;
}
