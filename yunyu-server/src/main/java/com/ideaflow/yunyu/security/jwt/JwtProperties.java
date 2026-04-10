package com.ideaflow.yunyu.security.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 配置属性类。
 * 作用：统一读取 JWT 签名密钥与有效期，供 Token 生成和解析流程复用。
 */
@Validated
@ConfigurationProperties(prefix = "yunyu.security.jwt")
public class JwtProperties {

    @NotBlank
    @Size(min = 64)
    private String secret;

    @Min(1)
    private int expireDays = 7;

    /**
     * 获取 JWT 签名密钥。
     *
     * @return JWT 签名密钥
     */
    public String getSecret() {
        return secret;
    }

    /**
     * 设置 JWT 签名密钥。
     *
     * @param secret JWT 签名密钥
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 获取 JWT 有效天数。
     *
     * @return JWT 有效天数
     */
    public int getExpireDays() {
        return expireDays;
    }

    /**
     * 设置 JWT 有效天数。
     *
     * @param expireDays JWT 有效天数
     */
    public void setExpireDays(int expireDays) {
        this.expireDays = expireDays;
    }
}
