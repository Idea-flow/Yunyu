package com.ideaflow.yunyu.module.topic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 专题实体类。
 * 作用：映射 `topic` 表，为后台专题管理和文章专题关联提供统一的持久化对象。
 */
@Data
@TableName("topic")
public class TopicEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    private String summary;
    private String coverUrl;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
