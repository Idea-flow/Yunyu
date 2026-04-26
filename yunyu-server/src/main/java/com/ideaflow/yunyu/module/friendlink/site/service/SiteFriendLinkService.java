package com.ideaflow.yunyu.module.friendlink.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.friendlink.site.dto.SiteFriendLinkApplyRequest;
import com.ideaflow.yunyu.module.friendlink.entity.FriendLinkEntity;
import com.ideaflow.yunyu.module.friendlink.mapper.FriendLinkMapper;
import com.ideaflow.yunyu.module.friendlink.site.vo.SiteFriendLinkApplyResponse;
import com.ideaflow.yunyu.module.friendlink.site.vo.SiteFriendLinkItemResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 前台友链服务类。
 * 作用：处理前台友链展示和匿名友链申请逻辑。
 */
@Service
public class SiteFriendLinkService {

    private static final String DEFAULT_THEME_COLOR = "#7CC6B8";
    private final FriendLinkMapper friendLinkMapper;

    /**
     * 创建前台友链服务。
     *
     * @param friendLinkMapper 友链 Mapper
     */
    public SiteFriendLinkService(FriendLinkMapper friendLinkMapper) {
        this.friendLinkMapper = friendLinkMapper;
    }

    /**
     * 查询前台已通过审核的友链列表。
     *
     * @return 友链列表
     */
    public List<SiteFriendLinkItemResponse> listApprovedFriendLinks() {
        return friendLinkMapper.selectList(new LambdaQueryWrapper<FriendLinkEntity>()
                        .eq(FriendLinkEntity::getDeleted, 0)
                        .eq(FriendLinkEntity::getStatus, "APPROVED")
                        .orderByAsc(FriendLinkEntity::getSortOrder)
                        .orderByAsc(FriendLinkEntity::getId))
                .stream()
                .map(this::toSiteFriendLinkItemResponse)
                .toList();
    }

    /**
     * 提交友链申请。
     *
     * @param request 友链申请请求
     * @return 申请结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SiteFriendLinkApplyResponse applyFriendLink(SiteFriendLinkApplyRequest request) {
        String siteName = request.getSiteName().trim();
        String siteUrl = normalizeSiteUrl(request.getSiteUrl());
        ensureFriendLinkCanApply(siteName, siteUrl);

        FriendLinkEntity friendLinkEntity = new FriendLinkEntity();
        friendLinkEntity.setSiteName(siteName);
        friendLinkEntity.setSiteUrl(siteUrl);
        friendLinkEntity.setLogoUrl(normalizeOptionalValue(request.getLogoUrl()));
        friendLinkEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        friendLinkEntity.setContactName(request.getContactName().trim());
        friendLinkEntity.setContactEmail(request.getContactEmail().trim());
        friendLinkEntity.setContactMessage(normalizeOptionalValue(request.getContactMessage()));
        friendLinkEntity.setThemeColor(normalizeThemeColor(request.getThemeColor()));
        friendLinkEntity.setSortOrder(0);
        friendLinkEntity.setStatus("PENDING");
        friendLinkEntity.setCreatedTime(LocalDateTime.now());
        friendLinkEntity.setUpdatedTime(LocalDateTime.now());
        friendLinkEntity.setDeleted(0);
        friendLinkMapper.insert(friendLinkEntity);

        return new SiteFriendLinkApplyResponse(friendLinkEntity.getId(), "PENDING", "申请已提交，审核通过后会展示在友链页。");
    }

    /**
     * 校验当前友链是否可以提交申请。
     *
     * @param siteName 站点名称
     * @param siteUrl 站点地址
     */
    private void ensureFriendLinkCanApply(String siteName, String siteUrl) {
        FriendLinkEntity existingByName = friendLinkMapper.selectOne(new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getSiteName, siteName)
                .eq(FriendLinkEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (existingByName != null) {
            throwExistingFriendLinkException(existingByName);
        }

        FriendLinkEntity existingByUrl = friendLinkMapper.selectOne(new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getSiteUrl, siteUrl)
                .eq(FriendLinkEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (existingByUrl != null) {
            throwExistingFriendLinkException(existingByUrl);
        }
    }

    /**
     * 根据已存在记录抛出更准确的重复申请异常。
     *
     * @param entity 已存在的友链记录
     */
    private void throwExistingFriendLinkException(FriendLinkEntity entity) {
        if ("PENDING".equalsIgnoreCase(entity.getStatus())) {
            throw new BizException(ResultCode.BAD_REQUEST, "该站点已提交友链申请，请等待审核");
        }

        throw new BizException(ResultCode.BAD_REQUEST, "该站点已存在友链记录，无需重复申请");
    }

    /**
     * 转换前台友链条目响应。
     *
     * @param entity 友链实体
     * @return 前台友链条目
     */
    private SiteFriendLinkItemResponse toSiteFriendLinkItemResponse(FriendLinkEntity entity) {
        SiteFriendLinkItemResponse response = new SiteFriendLinkItemResponse();
        response.setId(entity.getId());
        response.setSiteName(entity.getSiteName());
        response.setSiteUrl(entity.getSiteUrl());
        response.setLogoUrl(entity.getLogoUrl());
        response.setDescription(entity.getDescription());
        response.setThemeColor(normalizeThemeColor(entity.getThemeColor()));
        return response;
    }

    /**
     * 标准化站点地址。
     *
     * @param siteUrl 原始站点地址
     * @return 标准化后的站点地址
     */
    private String normalizeSiteUrl(String siteUrl) {
        String normalized = siteUrl.trim();
        if (normalized.endsWith("/") && normalized.length() > "https://".length()) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    /**
     * 标准化可选字段。
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
     * 标准化主题色。
     *
     * @param themeColor 原始主题色
     * @return 标准化后的主题色
     */
    private String normalizeThemeColor(String themeColor) {
        String normalized = normalizeOptionalValue(themeColor);
        return normalized == null ? DEFAULT_THEME_COLOR : normalized.toUpperCase();
    }
}
