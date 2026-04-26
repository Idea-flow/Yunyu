package com.ideaflow.yunyu.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应对象。
 * 作用：向前端返回访问令牌、有效期与当前登录用户的基础身份信息。
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;
}
