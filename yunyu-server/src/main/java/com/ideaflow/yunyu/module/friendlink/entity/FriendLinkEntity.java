package com.ideaflow.yunyu.module.friendlink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 友链实体类。
 * 作用：映射 `friend_link` 表，为前台友链展示、友链申请和后台审核管理提供统一持久化对象。
 */
@Data
@TableName("friend_link")
public class FriendLinkEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String siteName;
    private String siteUrl;
    private String logoUrl;
    private String description;
    private String contactName;
    private String contactEmail;
    private String contactMessage;
    private String themeColor;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
