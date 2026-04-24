package com.ideaflow.yunyu.module.contentaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 内容访问授权缓存实体类。
 * 作用：映射 `content_access_grant` 表，记录用户或访客在指定内容范围下已通过的访问规则及有效期。
 */
@Data
@TableName("content_access_grant")
public class ContentAccessGrantEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String scopeType;
    private Long scopeId;
    private String ruleType;
    private String grantTargetType;
    private Long userId;
    private String visitorIdHash;
    private LocalDateTime grantedAt;
    private LocalDateTime expireAt;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
