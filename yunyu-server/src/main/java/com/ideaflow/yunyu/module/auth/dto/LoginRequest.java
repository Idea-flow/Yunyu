package com.ideaflow.yunyu.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求对象。
 * 作用：承载本地账号密码登录所需的账号与密码参数。
 */
public class LoginRequest {

    @NotBlank(message = "登录账号不能为空")
    private String account;

    @NotBlank(message = "登录密码不能为空")
    private String password;

    /**
     * 获取登录账号。
     *
     * @return 登录账号
     */
    public String getAccount() {
        return account;
    }

    /**
     * 设置登录账号。
     *
     * @param account 登录账号
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * 获取登录密码。
     *
     * @return 登录密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登录密码。
     *
     * @param password 登录密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
