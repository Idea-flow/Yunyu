package com.ideaflow.yunyu.module.contentaccess.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.contentaccess.entity.ContentAccessGrantEntity;
import com.ideaflow.yunyu.module.contentaccess.mapper.ContentAccessGrantMapper;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内容访问授权缓存服务类。
 * 作用：统一管理用户或访客的内容访问授权记录，支持授权写入、有效期校验与访客标识哈希处理。
 */
@Service
public class ContentAccessGrantService {

    private static final int DEFAULT_GRANT_EXPIRE_DAYS = 7;
    private final ContentAccessGrantMapper contentAccessGrantMapper;

    /**
     * 创建内容访问授权缓存服务。
     *
     * @param contentAccessGrantMapper 授权缓存 Mapper
     */
    public ContentAccessGrantService(ContentAccessGrantMapper contentAccessGrantMapper) {
        this.contentAccessGrantMapper = contentAccessGrantMapper;
    }

    /**
     * 判断当前主体是否已拥有指定规则授权。
     *
     * @param scopeType 授权范围类型
     * @param scopeId 授权范围 ID
     * @param ruleType 规则类型
     * @param userId 用户 ID
     * @param visitorIdHash 访客标识哈希
     * @return 是否已授权
     */
    public boolean hasValidGrant(String scopeType, Long scopeId, String ruleType, Long userId, String visitorIdHash) {
        if (scopeType == null || scopeType.isBlank() || scopeId == null || ruleType == null || ruleType.isBlank()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ContentAccessGrantEntity> queryWrapper = new LambdaQueryWrapper<ContentAccessGrantEntity>()
                .eq(ContentAccessGrantEntity::getScopeType, scopeType.trim())
                .eq(ContentAccessGrantEntity::getScopeId, scopeId)
                .eq(ContentAccessGrantEntity::getRuleType, ruleType.trim())
                .gt(ContentAccessGrantEntity::getExpireAt, now)
                .orderByDesc(ContentAccessGrantEntity::getExpireAt)
                .last("LIMIT 1");

        if (userId != null && userId > 0) {
            queryWrapper.eq(ContentAccessGrantEntity::getGrantTargetType, "USER")
                    .eq(ContentAccessGrantEntity::getUserId, userId);
        } else if (visitorIdHash != null && !visitorIdHash.isBlank()) {
            queryWrapper.eq(ContentAccessGrantEntity::getGrantTargetType, "VISITOR")
                    .eq(ContentAccessGrantEntity::getVisitorIdHash, visitorIdHash);
        } else {
            return false;
        }

        return contentAccessGrantMapper.selectOne(queryWrapper) != null;
    }

    /**
     * 写入或刷新内容访问授权。
     *
     * @param scopeType 授权范围类型
     * @param scopeId 授权范围 ID
     * @param ruleType 规则类型
     * @param userId 用户 ID
     * @param visitorIdHash 访客标识哈希
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantAccess(String scopeType, Long scopeId, String ruleType, Long userId, String visitorIdHash) {
        String normalizedScopeType = normalizeRequiredText(scopeType, "授权范围不能为空");
        String normalizedRuleType = normalizeRequiredText(ruleType, "规则类型不能为空");
        String grantTargetType = userId != null && userId > 0 ? "USER" : "VISITOR";
        if ("VISITOR".equals(grantTargetType) && (visitorIdHash == null || visitorIdHash.isBlank())) {
            throw new BizException(ResultCode.BAD_REQUEST, "访客标识不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusDays(DEFAULT_GRANT_EXPIRE_DAYS);
        LambdaUpdateWrapper<ContentAccessGrantEntity> updateWrapper = new LambdaUpdateWrapper<ContentAccessGrantEntity>()
                .eq(ContentAccessGrantEntity::getScopeType, normalizedScopeType)
                .eq(ContentAccessGrantEntity::getScopeId, scopeId)
                .eq(ContentAccessGrantEntity::getRuleType, normalizedRuleType)
                .eq(ContentAccessGrantEntity::getGrantTargetType, grantTargetType);

        if ("USER".equals(grantTargetType)) {
            updateWrapper.eq(ContentAccessGrantEntity::getUserId, userId);
        } else {
            updateWrapper.eq(ContentAccessGrantEntity::getVisitorIdHash, visitorIdHash);
        }

        ContentAccessGrantEntity existingGrant = contentAccessGrantMapper.selectOne(new LambdaQueryWrapper<ContentAccessGrantEntity>()
                .eq(ContentAccessGrantEntity::getScopeType, normalizedScopeType)
                .eq(ContentAccessGrantEntity::getScopeId, scopeId)
                .eq(ContentAccessGrantEntity::getRuleType, normalizedRuleType)
                .eq(ContentAccessGrantEntity::getGrantTargetType, grantTargetType)
                .eq("USER".equals(grantTargetType), ContentAccessGrantEntity::getUserId, userId)
                .eq("VISITOR".equals(grantTargetType), ContentAccessGrantEntity::getVisitorIdHash, visitorIdHash)
                .last("LIMIT 1"));

        if (existingGrant == null) {
            ContentAccessGrantEntity entity = new ContentAccessGrantEntity();
            entity.setScopeType(normalizedScopeType);
            entity.setScopeId(scopeId);
            entity.setRuleType(normalizedRuleType);
            entity.setGrantTargetType(grantTargetType);
            entity.setUserId("USER".equals(grantTargetType) ? userId : null);
            entity.setVisitorIdHash("VISITOR".equals(grantTargetType) ? visitorIdHash : null);
            entity.setGrantedAt(now);
            entity.setExpireAt(expireAt);
            entity.setCreatedTime(now);
            entity.setUpdatedTime(now);
            contentAccessGrantMapper.insert(entity);
            return;
        }

        existingGrant.setGrantedAt(now);
        existingGrant.setExpireAt(expireAt);
        existingGrant.setUpdatedTime(now);
        contentAccessGrantMapper.updateById(existingGrant);
    }

    /**
     * 清理文章相关的内容访问授权缓存。
     * 作用：当后台修改文章访问规则、访问码或隐藏内容规则时，主动失效旧授权，
     * 避免前台继续沿用历史通过记录导致内容被错误放行。
     *
     * @param postId 文章 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearPostAccessGrants(Long postId) {
        if (postId == null || postId <= 0) {
            return;
        }

        contentAccessGrantMapper.delete(new LambdaQueryWrapper<ContentAccessGrantEntity>()
                .eq(ContentAccessGrantEntity::getScopeId, postId)
                .in(ContentAccessGrantEntity::getScopeType, List.of("ARTICLE", "TAIL_HIDDEN")));
    }

    /**
     * 计算访客标识哈希。
     * 作用：避免明文存储前端传来的 visitorId，同时保持同一访客可重复命中授权缓存。
     *
     * @param visitorId 原始访客标识
     * @return 哈希值
     */
    public String hashVisitorId(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(visitorId.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "访客标识处理失败");
        }
    }

    /**
     * 规范化必填文本。
     *
     * @param value 原始文本
     * @param message 错误信息
     * @return 规范化结果
     */
    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
