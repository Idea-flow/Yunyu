package com.ideaflow.yunyu.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * JWT 编解码配置类。
 * 作用：集中创建 Nimbus JWT 所需的对称密钥、编码器与解码器 Bean，避免与业务服务形成循环依赖。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtCodecConfiguration {

    /**
     * 构建 JWT 对称签名密钥。
     *
     * @param jwtProperties JWT 配置属性
     * @return HMAC 签名密钥
     */
    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }

    /**
     * 创建 JWT 解码器。
     *
     * @param jwtSecretKey JWT 对称签名密钥
     * @return JWT 解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    /**
     * 创建 JWT 编码器。
     *
     * @param jwtSecretKey JWT 对称签名密钥
     * @return JWT 编码器
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey));
    }
}
