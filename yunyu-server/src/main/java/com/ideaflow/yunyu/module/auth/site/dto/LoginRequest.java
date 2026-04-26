package com.ideaflow.yunyu.module.auth.site.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求对象。
 * 作用：承载本地账号密码登录所需的账号与密码参数。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "登录账号不能为空")
    private String account;

    @NotBlank(message = "登录密码不能为空")
    private String password;
}
