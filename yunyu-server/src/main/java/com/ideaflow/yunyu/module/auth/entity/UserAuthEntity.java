package com.ideaflow.yunyu.module.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户认证方式实体类。
 * 作用：映射 `user_auth` 表，用于管理本地登录和第三方登录绑定关系。
 */
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

    /**
     * 获取认证ID。
     *
     * @return 认证ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置认证ID。
     *
     * @param id 认证ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取用户ID。
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID。
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取认证类型。
     *
     * @return 认证类型
     */
    public String getAuthType() {
        return authType;
    }

    /**
     * 设置认证类型。
     *
     * @param authType 认证类型
     */
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * 获取认证唯一标识。
     *
     * @return 认证唯一标识
     */
    public String getAuthIdentity() {
        return authIdentity;
    }

    /**
     * 设置认证唯一标识。
     *
     * @param authIdentity 认证唯一标识
     */
    public void setAuthIdentity(String authIdentity) {
        this.authIdentity = authIdentity;
    }

    /**
     * 获取认证名称。
     *
     * @return 认证名称
     */
    public String getAuthName() {
        return authName;
    }

    /**
     * 设置认证名称。
     *
     * @param authName 认证名称
     */
    public void setAuthName(String authName) {
        this.authName = authName;
    }

    /**
     * 获取认证邮箱。
     *
     * @return 认证邮箱
     */
    public String getAuthEmail() {
        return authEmail;
    }

    /**
     * 设置认证邮箱。
     *
     * @param authEmail 认证邮箱
     */
    public void setAuthEmail(String authEmail) {
        this.authEmail = authEmail;
    }

    /**
     * 获取邮箱验证标记。
     *
     * @return 邮箱验证标记
     */
    public Integer getEmailVerified() {
        return emailVerified;
    }

    /**
     * 设置邮箱验证标记。
     *
     * @param emailVerified 邮箱验证标记
     */
    public void setEmailVerified(Integer emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * 获取原始用户信息。
     *
     * @return 原始用户信息
     */
    public String getRawUserInfo() {
        return rawUserInfo;
    }

    /**
     * 设置原始用户信息。
     *
     * @param rawUserInfo 原始用户信息
     */
    public void setRawUserInfo(String rawUserInfo) {
        this.rawUserInfo = rawUserInfo;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间。
     *
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedTime 更新时间
     */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
