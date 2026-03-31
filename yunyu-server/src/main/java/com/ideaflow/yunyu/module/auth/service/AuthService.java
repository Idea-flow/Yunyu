package com.ideaflow.yunyu.module.auth.service;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.auth.dto.LoginRequest;
import com.ideaflow.yunyu.module.auth.vo.CurrentUserResponse;
import com.ideaflow.yunyu.module.auth.vo.LoginResponse;
import com.ideaflow.yunyu.security.LoginUser;
import com.ideaflow.yunyu.security.SecurityUtils;
import com.ideaflow.yunyu.security.jwt.JwtTokenService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务类。
 * 作用：处理本地账号密码登录、当前用户信息读取以及认证用户加载等核心认证流程。
 */
@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 创建认证服务。
     *
     * @param jdbcTemplate JDBC 模板
     * @param jwtTokenService JWT 服务
     */
    public AuthService(JdbcTemplate jdbcTemplate, JwtTokenService jwtTokenService) {
        this.jdbcTemplate = jdbcTemplate;
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
        UserRecord userRecord = findUserByAccount(request.getAccount());
        if (userRecord == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号或密码错误");
        }
        if (!"ACTIVE".equals(userRecord.status())) {
            throw new BizException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), userRecord.passwordHash())) {
            throw new BizException(ResultCode.BAD_REQUEST, "账号或密码错误");
        }

        ensureLocalAuthBinding(userRecord);
        updateLastLoginInfo(userRecord.id(), loginIp);

        LoginUser loginUser = toLoginUser(userRecord);
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
        String sql = """
                SELECT id, email, user_name, password_hash, role, status
                FROM user
                WHERE id = ? AND deleted = 0
                LIMIT 1
                """;
        List<UserRecord> results = jdbcTemplate.query(sql, this::mapUserRecord, userId);
        if (results.isEmpty()) {
            throw new BizException(ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        }

        UserRecord userRecord = results.getFirst();
        if (!"ACTIVE".equals(userRecord.status())) {
            throw new BizException(ResultCode.FORBIDDEN, "当前账号已被禁用");
        }
        return toLoginUser(userRecord);
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
    private UserRecord findUserByAccount(String account) {
        String sql = """
                SELECT id, email, user_name, password_hash, role, status
                FROM user
                WHERE deleted = 0
                  AND (email = ? OR user_name = ?)
                LIMIT 1
                """;
        List<UserRecord> results = jdbcTemplate.query(sql, this::mapUserRecord, account, account);
        return results.isEmpty() ? null : results.getFirst();
    }

    /**
     * 更新用户最后登录信息。
     *
     * @param userId 用户ID
     * @param loginIp 登录IP
     */
    private void updateLastLoginInfo(Long userId, String loginIp) {
        String sql = """
                UPDATE user
                SET last_login_at = ?, last_login_ip = ?, updated_time = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, LocalDateTime.now(), loginIp, userId);
    }

    /**
     * 确保本地登录方式绑定存在。
     *
     * @param userRecord 用户记录
     */
    private void ensureLocalAuthBinding(UserRecord userRecord) {
        String sql = """
                INSERT INTO user_auth (user_id, auth_type, auth_identity, auth_name, auth_email, email_verified, raw_user_info, created_time, updated_time)
                VALUES (?, 'LOCAL', ?, ?, ?, 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                auth_name = VALUES(auth_name),
                auth_email = VALUES(auth_email),
                email_verified = VALUES(email_verified),
                updated_time = CURRENT_TIMESTAMP
                """;
        jdbcTemplate.update(sql, userRecord.id(), userRecord.email(), userRecord.userName(), userRecord.email());
    }

    /**
     * 将用户记录转换为当前登录用户对象。
     *
     * @param userRecord 用户记录
     * @return 当前登录用户
     */
    private LoginUser toLoginUser(UserRecord userRecord) {
        return new LoginUser(
                userRecord.id(),
                userRecord.email(),
                userRecord.userName(),
                userRecord.role(),
                userRecord.status()
        );
    }

    /**
     * 映射用户查询结果。
     *
     * @param resultSet 结果集
     * @param rowNum 行号
     * @return 用户记录
     * @throws SQLException SQL 异常
     */
    private UserRecord mapUserRecord(ResultSet resultSet, int rowNum) throws SQLException {
        return new UserRecord(
                resultSet.getLong("id"),
                resultSet.getString("email"),
                resultSet.getString("user_name"),
                resultSet.getString("password_hash"),
                resultSet.getString("role"),
                resultSet.getString("status")
        );
    }

    /**
     * 用户查询记录。
     * 作用：作为认证流程中的内部查询载体，减少零散字段传递。
     */
    private record UserRecord(Long id, String email, String userName, String passwordHash, String role, String status) {
    }
}
