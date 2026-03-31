package com.ideaflow.yunyu.security;

/**
 * 当前登录用户对象。
 * 作用：承载 JWT 认证通过后的最小用户身份信息，并作为当前请求上下文中的认证主体。
 */
public class LoginUser {

    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;
    private final String status;

    /**
     * 创建当前登录用户对象。
     *
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param userName 用户名称
     * @param role 用户角色
     * @param status 用户状态
     */
    public LoginUser(Long userId, String email, String userName, String role, String status) {
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
