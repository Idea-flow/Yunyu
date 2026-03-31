package com.ideaflow.yunyu.security;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类。
 * 作用：统一从 Spring Security 上下文中获取当前登录用户，避免业务代码重复处理认证细节。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前登录用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BizException(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        }
        return loginUser;
    }
}
