package com.ideaflow.yunyu.security.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 配置属性类。
 * 作用：统一读取 JWT 签名密钥与有效期，供 Token 生成和解析流程复用。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "yunyu.security.jwt")
public class JwtProperties {

    @NotBlank
    @Size(min = 64)
    private String secret;

    @Min(1)
    private int expireDays = 7;
}
