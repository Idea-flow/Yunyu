package com.ideaflow.yunyu.module.tag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 标签实体类。
 * 作用：映射 `tag` 表，为后台标签管理和文章标签关联提供统一的持久化对象。
 */
@Data
@TableName("tag")
public class TagEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
