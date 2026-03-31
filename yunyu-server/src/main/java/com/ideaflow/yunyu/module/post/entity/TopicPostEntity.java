package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 专题文章关联实体类。
 * 作用：映射 `topic_post` 表，为文章与专题之间的多对多关联关系提供持久化对象。
 */
@Data
@TableName("topic_post")
public class TopicPostEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long topicId;
    private Long postId;
    private Integer sortOrder;
    private LocalDateTime createdTime;
}
