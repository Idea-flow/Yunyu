package com.ideaflow.yunyu.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户实体类。
 * 作用：映射 `user` 表，为用户模块和认证模块提供统一的用户持久化对象。
 */
@Data
@TableName("user")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String userName;
    private String avatarUrl;
    private String password;
    private String passwordHash;
    private String role;
    private String status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer deleted;
}
