package com.ideaflow.yunyu.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前用户响应对象。
 * 作用：向前端返回当前请求上下文中的登录用户基础信息。
 */
@Data
@AllArgsConstructor
public class CurrentUserResponse {

    private final Long userId;
    private final String email;
    private final String userName;
    private final String role;
    private final String status;
}
