package com.ideaflow.yunyu.module.auth.vo;

/**
 * 当前用户响应对象。
 * 作用：向前端返回当前请求上下文中的登录用户基础信息。
 */
public class CurrentUserResponse {

    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;
    private final String status;

    /**
     * 创建当前用户响应对象。
     *
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param userName 用户名称
     * @param role 用户角色
     * @param status 用户状态
     */
    public CurrentUserResponse(Long userId, String email, String userName, String role, String status) {
        this.userId = userId;
        this.email = email;
        this.userName = userName;
        this.role = role;
        this.status = status;
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
     * 获取用户邮箱。
     *
     * @return 用户邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 获取用户名称。
     *
     * @return 用户名称
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 获取用户角色。
     *
     * @return 用户角色
     */
    public String getRole() {
        return role;
    }

    /**
     * 获取用户状态。
     *
     * @return 用户状态
     */
    public String getStatus() {
        return status;
    }
}
