package com.ideaflow.yunyu.security.jwt;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.auth.service.AuthService;
import com.ideaflow.yunyu.security.LoginUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JWT 登录用户认证转换器。
 * 作用：将 Spring Security 已解码的 JWT 转换为项目内部使用的 LoginUser 认证对象。
 */
@Component
public class JwtLoginUserAuthenticationConverter implements Converter<Jwt, PreAuthenticatedAuthenticationToken> {

    private final AuthService authService;

    /**
     * 创建 JWT 登录用户认证转换器。
     *
     * @param authService 认证服务
     */
    public JwtLoginUserAuthenticationConverter(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 将 JWT 转换为项目认证对象。
     *
     * @param jwt 已通过签名校验的 JWT
     * @return Spring Security 认证对象
     */
    @Override
    public PreAuthenticatedAuthenticationToken convert(Jwt jwt) {
        Long userId = parseUserId(jwt);
        LoginUser loginUser = loadLoginUser(userId);
        return new PreAuthenticatedAuthenticationToken(
                loginUser,
                jwt.getTokenValue(),
                List.of(new SimpleGrantedAuthority("ROLE_" + loginUser.getRole()))
        );
    }

    /**
     * 解析 JWT 中的用户ID。
     *
     * @param jwt 已通过签名校验的 JWT
     * @return 用户ID
     */
    private Long parseUserId(Jwt jwt) {
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException(ResultCode.UNAUTHORIZED.getMessage(), exception);
        }
    }

    /**
     * 加载并校验当前登录用户。
     *
     * @param userId 用户ID
     * @return 当前登录用户
     */
    private LoginUser loadLoginUser(Long userId) {
        try {
            return authService.loadLoginUser(userId);
        } catch (BizException exception) {
            if (exception.getCode() == ResultCode.FORBIDDEN.getCode()) {
                throw new DisabledException(exception.getMessage(), exception);
            }
            throw new BadCredentialsException(ResultCode.UNAUTHORIZED.getMessage(), exception);
        }
    }
}
