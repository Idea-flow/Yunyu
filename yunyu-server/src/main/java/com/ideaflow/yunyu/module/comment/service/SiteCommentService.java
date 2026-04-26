package com.ideaflow.yunyu.module.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.comment.dto.SiteCommentCreateRequest;
import com.ideaflow.yunyu.module.comment.dto.SiteCommentQueryRequest;
import com.ideaflow.yunyu.module.comment.entity.CommentEntity;
import com.ideaflow.yunyu.module.comment.mapper.CommentMapper;
import com.ideaflow.yunyu.module.comment.vo.SiteCommentAuthorResponse;
import com.ideaflow.yunyu.module.comment.vo.SiteCommentCreateResponse;
import com.ideaflow.yunyu.module.comment.vo.SiteCommentItemResponse;
import com.ideaflow.yunyu.module.comment.vo.SiteCommentListResponse;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.site.entity.SiteConfigEntity;
import com.ideaflow.yunyu.module.site.mapper.SiteConfigMapper;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import com.ideaflow.yunyu.security.LoginUser;
import com.ideaflow.yunyu.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 前台评论服务类。
 * 作用：处理文章评论区的展示、登录用户评论发布、回复归档与评论可见性控制逻辑。
 */
@Service
public class SiteCommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建前台评论服务。
     *
     * @param commentMapper 评论 Mapper
     * @param postMapper 文章 Mapper
     * @param userMapper 用户 Mapper
     * @param siteConfigMapper 站点配置 Mapper
     * @param objectMapper Jackson 对象映射器
     */
    public SiteCommentService(CommentMapper commentMapper,
                              PostMapper postMapper,
                              UserMapper userMapper,
                              SiteConfigMapper siteConfigMapper,
                              ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.siteConfigMapper = siteConfigMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询文章评论列表。
     *
     * @param slug 文章 slug
     * @param request 查询请求
     * @return 评论分页结果
     */
    public SiteCommentListResponse listPostComments(String slug, SiteCommentQueryRequest request) {
        PostEntity postEntity = findPublishedPostBySlug(slug);
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<CommentEntity> rootPage = commentMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<CommentEntity>()
                        .eq(CommentEntity::getPostId, postEntity.getId())
                        .eq(CommentEntity::getDeleted, 0)
                        .eq(CommentEntity::getStatus, "APPROVED")
                        .isNull(CommentEntity::getRootId)
                        .orderByDesc(CommentEntity::getCreatedTime)
                        .orderByDesc(CommentEntity::getId)
        );

        List<CommentEntity> rootComments = rootPage.getRecords();
        List<Long> rootIds = rootComments.stream()
                .map(CommentEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        List<CommentEntity> replyComments = rootIds.isEmpty()
                ? Collections.emptyList()
                : commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                        .eq(CommentEntity::getPostId, postEntity.getId())
                        .eq(CommentEntity::getDeleted, 0)
                        .eq(CommentEntity::getStatus, "APPROVED")
                        .in(CommentEntity::getRootId, rootIds)
                        .orderByAsc(CommentEntity::getCreatedTime)
                        .orderByAsc(CommentEntity::getId));

        List<CommentEntity> allComments = new ArrayList<>(rootComments);
        allComments.addAll(replyComments);
        Map<Long, CommentEntity> commentMap = allComments.stream()
                .filter(comment -> comment.getId() != null)
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
        Map<Long, UserEntity> userMap = loadUserMap(allComments);

        List<SiteCommentItemResponse> items = rootComments.stream()
                .map(comment -> toSiteCommentItemResponse(comment, userMap, commentMap, replyComments))
                .toList();

        long totalPages = rootPage.getPages() <= 0 ? 1 : rootPage.getPages();
        return new SiteCommentListResponse(items, rootPage.getTotal(), defaultLong(postEntity.getCommentCount()), pageNo, pageSize, totalPages);
    }

    /**
     * 发布一条评论。
     *
     * @param slug 文章 slug
     * @param request 评论请求
     * @param clientIp 客户端 IP
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SiteCommentCreateResponse createComment(String slug, SiteCommentCreateRequest request, String clientIp) {
        LoginUser loginUser = SecurityUtils.getCurrentUser();
        PostEntity postEntity = findPublishedPostBySlug(slug);
        ensureCommentEnabled(postEntity);

        CommentEntity replyComment = resolveReplyComment(postEntity.getId(), request.getReplyCommentId());
        LocalDateTime now = LocalDateTime.now();
        String normalizedContent = request.getContent().trim();
        String targetStatus = "SUPER_ADMIN".equals(loginUser.getRole()) ? "APPROVED" : "PENDING";

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setPostId(postEntity.getId());
        commentEntity.setUserId(loginUser.getUserId());
        commentEntity.setReplyCommentId(replyComment == null ? null : replyComment.getId());
        commentEntity.setRootId(resolveRootId(replyComment));
        commentEntity.setContent(normalizedContent);
        commentEntity.setStatus(targetStatus);
        commentEntity.setIp(normalizeIp(clientIp));
        commentEntity.setCreatedTime(now);
        commentEntity.setUpdatedTime(now);
        commentEntity.setDeleted(0);
        commentMapper.insert(commentEntity);

        if ("APPROVED".equals(targetStatus)) {
            syncApprovedCommentCount(postEntity.getId());
            return new SiteCommentCreateResponse(commentEntity.getId(), targetStatus, true, "评论已发布");
        }

        return new SiteCommentCreateResponse(commentEntity.getId(), targetStatus, false, "评论已提交，审核通过后会展示在文章下方");
    }

    /**
     * 将评论实体转换为前台评论响应对象。
     *
     * @param commentEntity 评论实体
     * @param userMap 用户映射
     * @param commentMap 评论映射
     * @param replyComments 当前页根评论下的全部回复
     * @return 前台评论响应对象
     */
    private SiteCommentItemResponse toSiteCommentItemResponse(CommentEntity commentEntity,
                                                              Map<Long, UserEntity> userMap,
                                                              Map<Long, CommentEntity> commentMap,
                                                              List<CommentEntity> replyComments) {
        SiteCommentItemResponse response = new SiteCommentItemResponse();
        response.setId(commentEntity.getId());
        response.setPostId(commentEntity.getPostId());
        response.setReplyCommentId(commentEntity.getReplyCommentId());
        response.setRootId(commentEntity.getRootId());
        response.setContent(commentEntity.getContent());
        response.setCreatedTime(commentEntity.getCreatedTime());
        response.setAuthor(toSiteCommentAuthorResponse(userMap.get(commentEntity.getUserId())));
        response.setReplyToUserName(resolveReplyToUserName(commentEntity, userMap, commentMap));
        response.setReplies(replyComments.stream()
                .filter(reply -> Objects.equals(reply.getRootId(), commentEntity.getId()))
                .map(reply -> toReplyResponse(reply, userMap, commentMap))
                .toList());
        return response;
    }

    /**
     * 转换楼层内回复对象。
     *
     * @param replyEntity 回复实体
     * @param userMap 用户映射
     * @param commentMap 评论映射
     * @return 回复响应对象
     */
    private SiteCommentItemResponse toReplyResponse(CommentEntity replyEntity,
                                                    Map<Long, UserEntity> userMap,
                                                    Map<Long, CommentEntity> commentMap) {
        SiteCommentItemResponse response = new SiteCommentItemResponse();
        response.setId(replyEntity.getId());
        response.setPostId(replyEntity.getPostId());
        response.setReplyCommentId(replyEntity.getReplyCommentId());
        response.setRootId(replyEntity.getRootId());
        response.setContent(replyEntity.getContent());
        response.setCreatedTime(replyEntity.getCreatedTime());
        response.setAuthor(toSiteCommentAuthorResponse(userMap.get(replyEntity.getUserId())));
        response.setReplyToUserName(resolveReplyToUserName(replyEntity, userMap, commentMap));
        response.setReplies(Collections.emptyList());
        return response;
    }

    /**
     * 转换评论作者响应对象。
     *
     * @param userEntity 用户实体
     * @return 作者响应对象
     */
    private SiteCommentAuthorResponse toSiteCommentAuthorResponse(UserEntity userEntity) {
        SiteCommentAuthorResponse response = new SiteCommentAuthorResponse();
        if (userEntity == null) {
            response.setUserId(0L);
            response.setUserName("未知用户");
            response.setAvatarUrl("");
            return response;
        }
        response.setUserId(userEntity.getId());
        response.setUserName(defaultString(userEntity.getUserName(), "匿名用户"));
        response.setAvatarUrl(defaultString(userEntity.getAvatarUrl(), ""));
        return response;
    }

    /**
     * 加载评论作者映射。
     *
     * @param comments 评论集合
     * @return 用户映射
     */
    private Map<Long, UserEntity> loadUserMap(List<CommentEntity> comments) {
        Set<Long> userIds = comments.stream()
                .map(CommentEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .in(UserEntity::getId, userIds)
                        .eq(UserEntity::getDeleted, 0))
                .stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
    }

    /**
     * 解析回复对象用户名。
     *
     * @param commentEntity 当前评论
     * @param userMap 用户映射
     * @param commentMap 评论映射
     * @return 回复对象用户名
     */
    private String resolveReplyToUserName(CommentEntity commentEntity,
                                          Map<Long, UserEntity> userMap,
                                          Map<Long, CommentEntity> commentMap) {
        if (commentEntity.getReplyCommentId() == null) {
            return null;
        }
        CommentEntity targetComment = commentMap.get(commentEntity.getReplyCommentId());
        if (targetComment == null) {
            return null;
        }
        UserEntity targetUser = userMap.get(targetComment.getUserId());
        return targetUser == null ? null : defaultString(targetUser.getUserName(), "匿名用户");
    }

    /**
     * 查询文章回复目标评论。
     *
     * @param postId 文章ID
     * @param replyCommentId 回复评论ID
     * @return 回复目标评论
     */
    private CommentEntity resolveReplyComment(Long postId, Long replyCommentId) {
        if (replyCommentId == null || replyCommentId <= 0) {
            return null;
        }

        CommentEntity commentEntity = commentMapper.selectOne(new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getId, replyCommentId)
                .eq(CommentEntity::getPostId, postId)
                .eq(CommentEntity::getDeleted, 0)
                .eq(CommentEntity::getStatus, "APPROVED")
                .last("LIMIT 1"));
        if (commentEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "回复的评论不存在或暂不可回复");
        }
        return commentEntity;
    }

    /**
     * 解析回复所归属的根评论 ID。
     *
     * @param replyComment 回复目标评论
     * @return 根评论ID
     */
    private Long resolveRootId(CommentEntity replyComment) {
        if (replyComment == null) {
            return null;
        }
        return replyComment.getRootId() == null ? replyComment.getId() : replyComment.getRootId();
    }

    /**
     * 校验评论开关。
     *
     * @param postEntity 文章实体
     */
    private void ensureCommentEnabled(PostEntity postEntity) {
        if (postEntity.getAllowComment() == null || postEntity.getAllowComment() != 1) {
            throw new BizException(ResultCode.FORBIDDEN, "当前文章暂未开启评论");
        }
        if (!isSiteCommentEnabled()) {
            throw new BizException(ResultCode.FORBIDDEN, "站点当前已关闭评论功能");
        }
    }

    /**
     * 判断站点评论总开关是否开启。
     *
     * @return 是否开启评论
     */
    private boolean isSiteCommentEnabled() {
        SiteConfigEntity siteConfigEntity = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfigEntity>()
                .eq(SiteConfigEntity::getConfigKey, "site.feature")
                .last("LIMIT 1"));
        if (siteConfigEntity == null || siteConfigEntity.getConfigJson() == null || siteConfigEntity.getConfigJson().isBlank()) {
            return true;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(siteConfigEntity.getConfigJson());
            return !jsonNode.has("allowComment") || jsonNode.get("allowComment").asBoolean(true);
        } catch (Exception exception) {
            return true;
        }
    }

    /**
     * 按 slug 查询已发布文章。
     *
     * @param slug 文章 slug
     * @return 文章实体
     */
    private PostEntity findPublishedPostBySlug(String slug) {
        PostEntity postEntity = postMapper.selectOne(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getSlug, slug)
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .last("LIMIT 1"));
        if (postEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "文章不存在");
        }
        return postEntity;
    }

    /**
     * 同步文章已通过审核的评论总数。
     *
     * @param postId 文章ID
     */
    private void syncApprovedCommentCount(Long postId) {
        Long approvedCount = commentMapper.selectCount(new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getPostId, postId)
                .eq(CommentEntity::getDeleted, 0)
                .eq(CommentEntity::getStatus, "APPROVED"));
        postMapper.update(null, new UpdateWrapper<PostEntity>()
                .eq("id", postId)
                .set("comment_count", defaultLong(approvedCount))
                .set("updated_time", LocalDateTime.now()));
    }

    /**
     * 标准化页码。
     *
     * @param pageNo 原始页码
     * @return 标准化页码
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
     * @return 标准化分页大小
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 20);
    }

    /**
     * 标准化客户端 IP。
     *
     * @param clientIp 原始 IP
     * @return 标准化后的 IP
     */
    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return null;
        }
        return clientIp.trim();
    }

    /**
     * 兜底字符串值。
     *
     * @param value 原始值
     * @param fallback 兜底值
     * @return 最终字符串
     */
    private String defaultString(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    /**
     * 兜底长整型值。
     *
     * @param value 原始值
     * @return 最终长整型值
     */
    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
