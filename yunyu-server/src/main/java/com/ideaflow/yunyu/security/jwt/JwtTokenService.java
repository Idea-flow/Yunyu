package com.ideaflow.yunyu.security.jwt;

import com.ideaflow.yunyu.security.LoginUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * JWT 令牌服务类。
 * 作用：基于 Spring Security Nimbus 组件统一生成当前系统使用的 Bearer Token。
 */
@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    /**
     * 创建 JWT 令牌服务。
     *
     * @param jwtEncoder JWT 编码器
     * @param jwtProperties JWT 配置属性
     */
    public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 生成访问令牌。
     *
     * @param loginUser 当前登录用户
     * @return JWT 访问令牌
     */
    public String generateToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireDays(), ChronoUnit.DAYS);
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .issuedAt(now)
                .expiresAt(expireAt)
                .claim("email", loginUser.getEmail())
                .claim("userName", loginUser.getUserName())
                .claim("role", loginUser.getRole())
                .claim("status", loginUser.getStatus())
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS512).type("JWT").build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    }

    /**
     * 获取配置的 JWT 过期秒数。
     *
     * @return JWT 过期秒数
     */
    public long getExpireSeconds() {
        return jwtProperties.getExpireDays() * 24L * 60L * 60L;
    }
}
