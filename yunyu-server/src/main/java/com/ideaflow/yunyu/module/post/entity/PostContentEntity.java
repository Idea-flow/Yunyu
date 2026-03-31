package com.ideaflow.yunyu.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文章内容实体类。
 * 作用：映射 `post_content` 表，用于存放文章正文和阅读时长等扩展内容信息。
 */
@Data
@TableName("post_content")
public class PostContentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String contentMarkdown;
    private String contentHtml;
    private String contentPlainText;
    private String contentTocJson;
    private String videoUrl;
    private Integer readingTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
