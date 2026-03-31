package com.ideaflow.yunyu.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.auth.dto.LoginRequest;
import com.ideaflow.yunyu.module.auth.entity.UserAuthEntity;
import com.ideaflow.yunyu.module.auth.mapper.UserAuthMapper;
import com.ideaflow.yunyu.module.auth.vo.CurrentUserResponse;
import com.ideaflow.yunyu.module.auth.vo.LoginResponse;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import com.ideaflow.yunyu.security.LoginUser;
import com.ideaflow.yunyu.security.SecurityUtils;
import com.ideaflow.yunyu.security.jwt.JwtTokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务类。
 * 作用：处理本地账号密码登录、当前用户信息读取以及认证用户加载等核心认证流程。
 */
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 创建认证服务。
     *
     * @param userMapper 用户 Mapper
     * @param userAuthMapper 用户认证方式 Mapper
     * @param jwtTokenService JWT 服务
     */
    public AuthService(UserMapper userMapper, UserAuthMapper userAuthMapper, JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 执行本地账号密码登录。
     *
     * @param request 登录请求
     * @param loginIp 登录 IP
     * @return 登录响应
     */
    public LoginResponse login(LoginRequest request, String loginIp) {
        UserEntity userEntity = findUserByAccount(request.getAccount());
        if (userEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号或密码错误");
        }
        if (!"ACTIVE".equals(userEntity.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPasswordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号或密码错误");
        }

        ensureLocalAuthBinding(userEntity);
        updateLastLoginInfo(userEntity.getId(), loginIp);

        LoginUser loginUser = toLoginUser(userEntity);
        String accessToken = jwtTokenService.generateToken(loginUser);
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getExpireSeconds(),
                loginUser.getUserId(),
                loginUser.getEmail(),
                loginUser.getUserName(),
                loginUser.getRole()
        );
    }

    /**
     * 根据用户ID加载当前认证用户。
     *
     * @param userId 用户ID
     * @return 当前认证用户
     */
    public LoginUser loadLoginUser(Long userId) {
        UserEntity userEntity = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .eq(UserEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (userEntity == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        }

        if (!"ACTIVE".equals(userEntity.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
        return toLoginUser(userEntity);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户响应
     */
    public CurrentUserResponse getCurrentUser() {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        return new CurrentUserResponse(
                loginUser.getUserId(),
                loginUser.getEmail(),
                loginUser.getUserName(),
                loginUser.getRole(),
                loginUser.getStatus()
        );
    }

    /**
     * 按登录账号查询用户。
     *
     * @param account 登录账号
     * @return 用户记录
     */
    private UserEntity findUserByAccount(String account) {
        return userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getDeleted, 0)
                .and(wrapper -> wrapper
                        .eq(UserEntity::getEmail, account)
                        .or()
                        .eq(UserEntity::getUserName, account))
                .last("LIMIT 1"));
    }

    /**
     * 更新用户最后登录信息。
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    private void updateLastLoginInfo(Long userId, String loginIp) {
        userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .set(UserEntity::getLastLoginAt, LocalDateTime.now())
                .set(UserEntity::getLastLoginIp, loginIp)
                .set(UserEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 确保本地登录方式绑定存在。
     *
     * @param userEntity 用户记录
     */
    private void ensureLocalAuthBinding(UserEntity userEntity) {
        UserAuthEntity userAuthEntity = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuthEntity>()
                .eq(UserAuthEntity::getUserId, userEntity.getId())
                .eq(UserAuthEntity::getAuthType, "LOCAL")
                .last("LIMIT 1"));
        if (userAuthEntity == null) {
            userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUserId(userEntity.getId());
            userAuthEntity.setAuthType("LOCAL");
            userAuthEntity.setAuthIdentity(userEntity.getEmail());
            userAuthEntity.setAuthName(userEntity.getUserName());
            userAuthEntity.setAuthEmail(userEntity.getEmail());
            userAuthEntity.setEmailVerified(1);
            userAuthMapper.insert(userAuthEntity);
            return;
        }

        userAuthEntity.setAuthIdentity(userEntity.getEmail());
        userAuthEntity.setAuthName(userEntity.getUserName());
        userAuthEntity.setAuthEmail(userEntity.getEmail());
        userAuthEntity.setEmailVerified(1);
        userAuthMapper.updateById(userAuthEntity);
    }

    /**
     * 将用户记录转换为当前登录用户对象。
     *
     * @param userEntity 用户记录
     * @return 当前登录用户
     */
    private LoginUser toLoginUser(UserEntity userEntity) {
        return new LoginUser(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getUserName(),
                userEntity.getRole(),
                userEntity.getStatus()
        );
    }
}
