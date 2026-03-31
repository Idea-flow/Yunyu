package com.ideaflow.yunyu.module.category.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 分类实体类。
 * 作用：映射 `category` 表，为后台分类管理和文章分类关联提供统一的持久化对象。
 */
@Data
@TableName("category")
public class CategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String coverUrl;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
