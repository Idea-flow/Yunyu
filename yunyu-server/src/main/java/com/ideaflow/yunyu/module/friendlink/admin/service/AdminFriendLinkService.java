package com.ideaflow.yunyu.module.friendlink.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkCreateRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkQueryRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkStatusUpdateRequest;
import com.ideaflow.yunyu.module.friendlink.admin.dto.AdminFriendLinkUpdateRequest;
import com.ideaflow.yunyu.module.friendlink.entity.FriendLinkEntity;
import com.ideaflow.yunyu.module.friendlink.mapper.FriendLinkMapper;
import com.ideaflow.yunyu.module.friendlink.admin.vo.AdminFriendLinkItemResponse;
import com.ideaflow.yunyu.module.friendlink.admin.vo.AdminFriendLinkListResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台友链管理服务类。
 * 作用：处理后台友链的查询、创建、编辑、审核、上下架和删除逻辑。
 */
@Service
public class AdminFriendLinkService {

    private static final String DEFAULT_THEME_COLOR = "#7CC6B8";
    private final FriendLinkMapper friendLinkMapper;

    /**
     * 创建后台友链管理服务。
     *
     * @param friendLinkMapper 友链 Mapper
     */
    public AdminFriendLinkService(FriendLinkMapper friendLinkMapper) {
        this.friendLinkMapper = friendLinkMapper;
    }

    /**
     * 查询后台友链列表。
     *
     * @param request 查询请求
     * @return 友链分页结果
     */
    public AdminFriendLinkListResponse listFriendLinks(AdminFriendLinkQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<FriendLinkEntity> page = friendLinkMapper.selectPage(new Page<>(pageNo, pageSize), buildFriendLinkListQuery(request));

        List<AdminFriendLinkItemResponse> items = page.getRecords()
                .stream()
                .map(this::toAdminFriendLinkItemResponse)
                .toList();

        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminFriendLinkListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 查询单个友链详情。
     *
     * @param friendLinkId 友链ID
     * @return 友链详情
     */
    public AdminFriendLinkItemResponse getFriendLink(Long friendLinkId) {
        return toAdminFriendLinkItemResponse(findFriendLinkOrThrow(friendLinkId));
    }

    /**
     * 创建友链。
     *
     * @param request 创建请求
     * @return 创建后的友链详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminFriendLinkItemResponse createFriendLink(AdminFriendLinkCreateRequest request) {
        String siteName = request.getSiteName().trim();
        String siteUrl = normalizeSiteUrl(request.getSiteUrl());
        ensureSiteNameUnique(siteName, null);
        ensureSiteUrlUnique(siteUrl, null);

        FriendLinkEntity friendLinkEntity = new FriendLinkEntity();
        friendLinkEntity.setSiteName(siteName);
        friendLinkEntity.setSiteUrl(siteUrl);
        friendLinkEntity.setLogoUrl(normalizeOptionalValue(request.getLogoUrl()));
        friendLinkEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        friendLinkEntity.setContactName(normalizeOptionalValue(request.getContactName()));
        friendLinkEntity.setContactEmail(normalizeOptionalValue(request.getContactEmail()));
        friendLinkEntity.setContactMessage(normalizeOptionalValue(request.getContactMessage()));
        friendLinkEntity.setThemeColor(normalizeThemeColor(request.getThemeColor()));
        friendLinkEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        friendLinkEntity.setStatus(resolveStatus(request.getStatus(), "APPROVED"));
        friendLinkEntity.setCreatedTime(LocalDateTime.now());
        friendLinkEntity.setUpdatedTime(LocalDateTime.now());
        friendLinkEntity.setDeleted(0);
        friendLinkMapper.insert(friendLinkEntity);

        return getFriendLink(friendLinkEntity.getId());
    }

    /**
     * 更新友链。
     *
     * @param friendLinkId 友链ID
     * @param request 更新请求
     * @return 更新后的友链详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminFriendLinkItemResponse updateFriendLink(Long friendLinkId, AdminFriendLinkUpdateRequest request) {
        FriendLinkEntity friendLinkEntity = findFriendLinkOrThrow(friendLinkId);
        String siteName = request.getSiteName().trim();
        String siteUrl = normalizeSiteUrl(request.getSiteUrl());
        ensureSiteNameUnique(siteName, friendLinkId);
        ensureSiteUrlUnique(siteUrl, friendLinkId);

        friendLinkEntity.setSiteName(siteName);
        friendLinkEntity.setSiteUrl(siteUrl);
        friendLinkEntity.setLogoUrl(normalizeOptionalValue(request.getLogoUrl()));
        friendLinkEntity.setDescription(normalizeOptionalValue(request.getDescription()));
        friendLinkEntity.setContactName(normalizeOptionalValue(request.getContactName()));
        friendLinkEntity.setContactEmail(normalizeOptionalValue(request.getContactEmail()));
        friendLinkEntity.setContactMessage(normalizeOptionalValue(request.getContactMessage()));
        friendLinkEntity.setThemeColor(normalizeThemeColor(request.getThemeColor()));
        friendLinkEntity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        friendLinkEntity.setStatus(resolveStatus(request.getStatus(), friendLinkEntity.getStatus()));
        friendLinkEntity.setUpdatedTime(LocalDateTime.now());
        friendLinkMapper.updateById(friendLinkEntity);

        return getFriendLink(friendLinkId);
    }

    /**
     * 快速更新友链状态。
     *
     * @param friendLinkId 友链ID
     * @param request 状态更新请求
     * @return 更新后的友链详情
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminFriendLinkItemResponse updateFriendLinkStatus(Long friendLinkId,
                                                              AdminFriendLinkStatusUpdateRequest request) {
        FriendLinkEntity friendLinkEntity = findFriendLinkOrThrow(friendLinkId);
        friendLinkEntity.setStatus(resolveStatus(request.getStatus(), friendLinkEntity.getStatus()));
        friendLinkEntity.setUpdatedTime(LocalDateTime.now());
        friendLinkMapper.updateById(friendLinkEntity);
        return getFriendLink(friendLinkId);
    }

    /**
     * 删除友链。
     *
     * @param friendLinkId 友链ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriendLink(Long friendLinkId) {
        findFriendLinkOrThrow(friendLinkId);
        friendLinkMapper.update(null, new LambdaUpdateWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getId, friendLinkId)
                .eq(FriendLinkEntity::getDeleted, 0)
                .set(FriendLinkEntity::getDeleted, 1)
                .set(FriendLinkEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 构建友链列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<FriendLinkEntity> buildFriendLinkListQuery(AdminFriendLinkQueryRequest request) {
        LambdaQueryWrapper<FriendLinkEntity> queryWrapper = new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getDeleted, 0)
                .orderByAsc(FriendLinkEntity::getSortOrder)
                .orderByDesc(FriendLinkEntity::getCreatedTime)
                .orderByDesc(FriendLinkEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(FriendLinkEntity::getSiteName, keyword)
                    .or()
                    .like(FriendLinkEntity::getSiteUrl, keyword)
                    .or()
                    .like(FriendLinkEntity::getContactName, keyword)
                    .or()
                    .like(FriendLinkEntity::getContactEmail, keyword)
                    .or()
                    .like(FriendLinkEntity::getDescription, keyword));
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(FriendLinkEntity::getStatus, request.getStatus().trim());
        }

        return queryWrapper;
    }

    /**
     * 查询友链，不存在时抛出异常。
     *
     * @param friendLinkId 友链ID
     * @return 友链实体
     */
    private FriendLinkEntity findFriendLinkOrThrow(Long friendLinkId) {
        FriendLinkEntity friendLinkEntity = friendLinkMapper.selectOne(new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getId, friendLinkId)
                .eq(FriendLinkEntity::getDeleted, 0)
                .last("LIMIT 1"));

        if (friendLinkEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "友链不存在");
        }

        return friendLinkEntity;
    }

    /**
     * 校验站点名称唯一性。
     *
     * @param siteName 站点名称
     * @param excludeFriendLinkId 排除的友链ID
     */
    private void ensureSiteNameUnique(String siteName, Long excludeFriendLinkId) {
        LambdaQueryWrapper<FriendLinkEntity> queryWrapper = new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getSiteName, siteName)
                .eq(FriendLinkEntity::getDeleted, 0);

        if (excludeFriendLinkId != null) {
            queryWrapper.ne(FriendLinkEntity::getId, excludeFriendLinkId);
        }

        Long count = friendLinkMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该站点名称已存在");
        }
    }

    /**
     * 校验站点地址唯一性。
     *
     * @param siteUrl 站点地址
     * @param excludeFriendLinkId 排除的友链ID
     */
    private void ensureSiteUrlUnique(String siteUrl, Long excludeFriendLinkId) {
        LambdaQueryWrapper<FriendLinkEntity> queryWrapper = new LambdaQueryWrapper<FriendLinkEntity>()
                .eq(FriendLinkEntity::getSiteUrl, siteUrl)
                .eq(FriendLinkEntity::getDeleted, 0);

        if (excludeFriendLinkId != null) {
            queryWrapper.ne(FriendLinkEntity::getId, excludeFriendLinkId);
        }

        Long count = friendLinkMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "该站点地址已存在");
        }
    }

    /**
     * 转换后台友链条目响应。
     *
     * @param entity 友链实体
     * @return 后台友链条目
     */
    private AdminFriendLinkItemResponse toAdminFriendLinkItemResponse(FriendLinkEntity entity) {
        AdminFriendLinkItemResponse response = new AdminFriendLinkItemResponse();
        response.setId(entity.getId());
        response.setSiteName(entity.getSiteName());
        response.setSiteUrl(entity.getSiteUrl());
        response.setLogoUrl(entity.getLogoUrl());
        response.setDescription(entity.getDescription());
        response.setContactName(entity.getContactName());
        response.setContactEmail(entity.getContactEmail());
        response.setContactMessage(entity.getContactMessage());
        response.setThemeColor(entity.getThemeColor());
        response.setSortOrder(entity.getSortOrder());
        response.setStatus(entity.getStatus());
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());
        return response;
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
     * 标准化分页大小。
     *
     * @param pageSize 原始分页大小
     * @return 标准化后的分页大小
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
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

    /**
     * 标准化排序值。
     *
     * @param sortOrder 原始排序值
     * @return 标准化后的排序值
     */
    private Integer normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            return 0;
        }
        return sortOrder;
    }

    /**
     * 解析状态字段。
     *
     * @param status 原始状态
     * @param defaultStatus 默认状态
     * @return 最终状态
     */
    private String resolveStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }
        return status.trim().toUpperCase();
    }
}
