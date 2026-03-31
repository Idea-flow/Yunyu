package com.ideaflow.yunyu.security.jwt;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 令牌服务类。
 * 作用：负责生成、解析与校验当前系统使用的 Bearer Token。
 */
@Service
public class JwtTokenService {

    private final JwtProperties jwtProperties;

    /**
     * 创建 JWT 令牌服务。
     *
     * @param jwtProperties JWT 配置属性
     */
    public JwtTokenService(JwtProperties jwtProperties) {
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
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("email", loginUser.getEmail())
                .claim("userName", loginUser.getUserName())
                .claim("role", loginUser.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(buildSecretKey())
                .compact();
    }

    /**
     * 解析 JWT Claims。
     *
     * @param token JWT 令牌
     * @return JWT Claims
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(buildSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BizException(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        }
    }

    /**
     * 获取配置的 JWT 过期秒数。
     *
     * @return JWT 过期秒数
     */
    public long getExpireSeconds() {
        return jwtProperties.getExpireDays() * 24L * 60L * 60L;
    }

    /**
     * 构建 HMAC 签名密钥。
     *
     * @return 签名密钥
     */
    private SecretKey buildSecretKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
