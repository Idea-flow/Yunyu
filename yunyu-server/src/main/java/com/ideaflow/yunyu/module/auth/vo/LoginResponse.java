package com.ideaflow.yunyu.module.auth.vo;

/**
 * 登录响应对象。
 * 作用：向前端返回访问令牌、有效期与当前登录用户的基础身份信息。
 */
public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;

    /**
     * 创建登录响应对象。
     *
     * @param accessToken 访问令牌
     * @param tokenType 令牌类型
     * @param expiresIn 有效秒数
     * @param userId 用户ID
     * @param email 用户邮箱
     * @param userName 用户名称
     * @param role 用户角色
     */
    public LoginResponse(String accessToken, String tokenType, long expiresIn, Long userId, String email, String userName, String role) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.userName = userName;
        this.role = role;
    }

    /**
     * 获取访问令牌。
     *
     * @return 访问令牌
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 获取令牌类型。
     *
     * @return 令牌类型
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * 获取有效秒数。
     *
     * @return 有效秒数
     */
    public long getExpiresIn() {
        return expiresIn;
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
}
