package com.ideaflow.yunyu.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求对象。
 * 作用：承载用户自助注册所需的邮箱与密码参数，并在控制层完成基础格式校验。
 */
public class RegisterRequest {

    @NotBlank(message = "注册邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;

    @NotBlank(message = "注册密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度需为8-20位")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)\\S+$", message = "密码需同时包含字母和数字，且不能包含空格")
    private String password;

    /**
     * 获取注册邮箱。
     *
     * @return 注册邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置注册邮箱。
     *
     * @param email 注册邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取注册密码。
     *
     * @return 注册密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置注册密码。
     *
     * @param password 注册密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
