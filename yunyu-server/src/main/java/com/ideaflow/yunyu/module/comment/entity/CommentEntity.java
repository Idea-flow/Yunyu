package com.ideaflow.yunyu.module.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评论实体类。
 * 作用：映射 `comment` 表，为前台评论展示、评论发布与后台评论审核提供统一持久化对象。
 */
@Data
@TableName("comment")
public class CommentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private Long replyCommentId;
    private Long rootId;
    private String content;
    private String status;
    private String ip;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
