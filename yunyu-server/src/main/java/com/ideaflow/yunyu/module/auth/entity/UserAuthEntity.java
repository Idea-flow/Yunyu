package com.ideaflow.yunyu.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户认证方式实体类。
 * 作用：映射 `user_auth` 表，用于管理本地登录和第三方登录绑定关系。
 */
@Data
@TableName("user_auth")
public class UserAuthEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String authType;
    private String authIdentity;
    private String authName;
    private String authEmail;
    private Integer emailVerified;
    private String rawUserInfo;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
