package com.ideaflow.yunyu.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 用户实体类。
 * 作用：映射 `user` 表，为用户模块和认证模块提供统一的用户持久化对象。
 */
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

    /**
     * 获取用户ID。
     *
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID。
     *
     * @param id 用户ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取邮箱。
     *
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置邮箱。
     *
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置用户名。
     *
     * @param userName 用户名
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取头像地址。
     *
     * @return 头像地址
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * 设置头像地址。
     *
     * @param avatarUrl 头像地址
     */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 获取明文密码。
     *
     * @return 明文密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置明文密码。
     *
     * @param password 明文密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取密码哈希。
     *
     * @return 密码哈希
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * 设置密码哈希。
     *
     * @param passwordHash 密码哈希
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 获取角色。
     *
     * @return 角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 设置角色。
     *
     * @param role 角色
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 获取状态。
     *
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态。
     *
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取最后登录时间。
     *
     * @return 最后登录时间
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * 设置最后登录时间。
     *
     * @param lastLoginAt 最后登录时间
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * 获取最后登录IP。
     *
     * @return 最后登录IP
     */
    public String getLastLoginIp() {
        return lastLoginIp;
    }

    /**
     * 设置最后登录IP。
     *
     * @param lastLoginIp 最后登录IP
     */
    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
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

    /**
     * 获取删除标记。
     *
     * @return 删除标记
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置删除标记。
     *
     * @param deleted 删除标记
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
