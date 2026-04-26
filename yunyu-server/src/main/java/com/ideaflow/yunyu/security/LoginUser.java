package com.ideaflow.yunyu.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户对象。
 * 作用：承载 JWT 认证通过后的最小用户身份信息，并作为当前请求上下文中的认证主体。
 */
@Data
@AllArgsConstructor
public class LoginUser {

    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;
    private final String status;
}
