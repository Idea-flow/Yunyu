package com.ideaflow.yunyu.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.auth.entity.UserAuthEntity;
import com.ideaflow.yunyu.module.auth.mapper.UserAuthMapper;
import com.ideaflow.yunyu.module.user.dto.AdminUserCreateRequest;
import com.ideaflow.yunyu.module.user.dto.AdminUserQueryRequest;
import com.ideaflow.yunyu.module.user.dto.AdminUserUpdateRequest;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import com.ideaflow.yunyu.module.user.vo.AdminUserItemResponse;
import com.ideaflow.yunyu.module.user.vo.AdminUserListResponse;
import com.ideaflow.yunyu.security.LoginUser;
import com.ideaflow.yunyu.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台用户管理服务类。
 * 作用：处理站长在后台执行的用户查询、创建、更新和删除操作，并负责同步本地认证绑定关系。
 */
@Service
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 创建后台用户管理服务。
     *
     * @param userMapper 用户 Mapper
     * @param userAuthMapper 用户认证方式 Mapper
     */
    public AdminUserService(UserMapper userMapper, UserAuthMapper userAuthMapper) {
        this.userMapper = userMapper;
        this.userAuthMapper = userAuthMapper;
    }

    /**
     * 查询后台用户列表。
     *
     * @param request 查询请求
     * @return 用户列表响应
     */
    public AdminUserListResponse listUsers(AdminUserQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<UserEntity> page = userMapper.selectPage(new Page<>(pageNo, pageSize), buildUserListQuery(request));

        List<AdminUserItemResponse> items = page.getRecords()
                .stream()
                .map(this::toAdminUserItemResponse)
                .toList();
        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminUserListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 获取单个用户详情。
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    public AdminUserItemResponse getUser(Long userId) {
        return toAdminUserItemResponse(findUserOrThrow(userId));
    }

    /**
     * 创建新用户。
     *
     * @param request 创建请求
     * @return 创建后的用户详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUserItemResponse createUser(AdminUserCreateRequest request) {
        String email = normalizeEmail(request.getEmail());
        ensureEmailUnique(email, null);

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setUserName(request.getUserName().trim());
        userEntity.setAvatarUrl(normalizeOptionalValue(request.getAvatarUrl()));
        userEntity.setPassword(request.getPassword());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(resolveRole(request.getRole()));
        userEntity.setStatus(resolveStatus(request.getStatus()));
        userEntity.setDeleted(0);
        userEntity.setCreatedTime(LocalDateTime.now());
        userEntity.setUpdatedTime(LocalDateTime.now());
        userMapper.insert(userEntity);

        syncLocalAuth(userEntity);
        return toAdminUserItemResponse(findUserOrThrow(userEntity.getId()));
    }

    /**
     * 更新用户信息。
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminUserItemResponse updateUser(Long userId, AdminUserUpdateRequest request) {
        UserEntity userEntity = findUserOrThrow(userId);
        String email = normalizeEmail(request.getEmail());
        ensureEmailUnique(email, userId);

        userEntity.setEmail(email);
        userEntity.setUserName(request.getUserName().trim());
        userEntity.setAvatarUrl(normalizeOptionalValue(request.getAvatarUrl()));
        userEntity.setRole(resolveRole(request.getRole()));
        userEntity.setStatus(resolveStatus(request.getStatus()));
        userEntity.setUpdatedTime(LocalDateTime.now());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            userEntity.setPassword(request.getPassword());
            userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(userEntity);
        syncLocalAuth(userEntity);
        return toAdminUserItemResponse(findUserOrThrow(userId));
    }

    /**
     * 删除用户。
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        UserEntity userEntity = findUserOrThrow(userId);
        LoginUser currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getUserId().equals(userId)) {
            throw new BizException(ResultCode.BAD_REQUEST, "不能删除当前登录账号");
        }

        if ("SUPER_ADMIN".equals(userEntity.getRole())) {
            long remainingAdminCount = userMapper.selectCount(new LambdaQueryWrapper<UserEntity>()
                    .eq(UserEntity::getDeleted, 0)
                    .eq(UserEntity::getRole, "SUPER_ADMIN"));
            if (remainingAdminCount <= 1) {
                throw new BizException(ResultCode.BAD_REQUEST, "至少需要保留一个站长账号");
            }
        }

        userMapper.update(null, new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .eq(UserEntity::getDeleted, 0)
                .set(UserEntity::getDeleted, 1)
                .set(UserEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 查询用户，不存在时抛出业务异常。
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    private UserEntity findUserOrThrow(Long userId) {
        UserEntity userEntity = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .eq(UserEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (userEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return userEntity;
    }

    /**
     * 校验邮箱是否已被其他有效用户占用。
     *
     * @param email 邮箱
     * @param excludeUserId 需要排除的用户ID
     */
    private void ensureEmailUnique(String email, Long excludeUserId) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, email)
                .eq(UserEntity::getDeleted, 0);
        if (excludeUserId != null) {
            queryWrapper.ne(UserEntity::getId, excludeUserId);
        }

        Long count = userMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该邮箱已被占用");
        }
    }

    /**
     * 构建用户列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<UserEntity> buildUserListQuery(AdminUserQueryRequest request) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getDeleted, 0)
                .orderByDesc(UserEntity::getCreatedTime)
                .orderByDesc(UserEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(UserEntity::getEmail, keyword)
                    .or()
                    .like(UserEntity::getUserName, keyword));
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            queryWrapper.eq(UserEntity::getRole, request.getRole().trim());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(UserEntity::getStatus, request.getStatus().trim());
        }
        return queryWrapper;
    }

    /**
     * 标准化页码。
     *
     * @param pageNo 原始页码
     * @return 标准化后的页码
     */
    private long normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            return 1;
        }
        return pageNo;
    }

    /**
     * 标准化每页条数。
     *
     * @param pageSize 原始每页条数
     * @return 标准化后的每页条数
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 50);
    }

    /**
     * 同步本地登录绑定。
     *
     * @param userEntity 用户实体
     */
    private void syncLocalAuth(UserEntity userEntity) {
        UserAuthEntity userAuthEntity = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuthEntity>()
                .eq(UserAuthEntity::getUserId, userEntity.getId())
                .eq(UserAuthEntity::getAuthType, "LOCAL")
                .last("LIMIT 1"));

        if (userAuthEntity == null) {
            userAuthEntity = new UserAuthEntity();
            userAuthEntity.setUserId(userEntity.getId());
            userAuthEntity.setAuthType("LOCAL");
        }

        userAuthEntity.setAuthIdentity(userEntity.getEmail());
        userAuthEntity.setAuthName(userEntity.getUserName());
        userAuthEntity.setAuthEmail(userEntity.getEmail());
        userAuthEntity.setEmailVerified(1);

        if (userAuthEntity.getId() == null) {
            userAuthMapper.insert(userAuthEntity);
            return;
        }
        userAuthMapper.updateById(userAuthEntity);
    }

    /**
     * 标准化邮箱。
     *
     * @param email 原始邮箱
     * @return 标准化后的邮箱
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    /**
     * 标准化可选字符串字段。
     *
     * @param value 原始值
     * @return 标准化后的值
     */
    private String normalizeOptionalValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 解析角色字段。
     *
     * @param role 原始角色
     * @return 角色
     */
    private String resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.trim();
    }

    /**
     * 解析状态字段。
     *
     * @param status 原始状态
     * @return 状态
     */
    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim();
    }

    /**
     * 转换后台用户响应对象。
     *
     * @param userEntity 用户实体
     * @return 后台用户响应对象
     */
    private AdminUserItemResponse toAdminUserItemResponse(UserEntity userEntity) {
        AdminUserItemResponse response = new AdminUserItemResponse();
        response.setId(userEntity.getId());
        response.setEmail(userEntity.getEmail());
        response.setUserName(userEntity.getUserName());
        response.setAvatarUrl(userEntity.getAvatarUrl());
        response.setRole(userEntity.getRole());
        response.setStatus(userEntity.getStatus());
        response.setLastLoginAt(userEntity.getLastLoginAt());
        response.setLastLoginIp(userEntity.getLastLoginIp());
        response.setCreatedTime(userEntity.getCreatedTime());
        response.setUpdatedTime(userEntity.getUpdatedTime());
        return response;
    }
}
