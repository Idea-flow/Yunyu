package com.ideaflow.yunyu.module.comment.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.comment.admin.dto.AdminCommentQueryRequest;
import com.ideaflow.yunyu.module.comment.entity.CommentEntity;
import com.ideaflow.yunyu.module.comment.mapper.CommentMapper;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentItemResponse;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentListResponse;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentThreadGroupListResponse;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentThreadGroupResponse;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentThreadReplyItemResponse;
import com.ideaflow.yunyu.module.comment.admin.vo.AdminCommentThreadRootItemResponse;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.user.entity.UserEntity;
import com.ideaflow.yunyu.module.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 后台评论管理服务类。
 * 作用：处理评论管理页的列表查询、审核流转、软删除与评论计数同步逻辑。
 */
@Service
public class AdminCommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    /**
     * 创建后台评论管理服务。
     *
     * @param commentMapper 评论 Mapper
     * @param postMapper 文章 Mapper
     * @param userMapper 用户 Mapper
     */
    public AdminCommentService(CommentMapper commentMapper, PostMapper postMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    /**
     * 查询后台评论列表。
     *
     * @param request 查询请求
     * @return 评论列表响应
     */
    public AdminCommentListResponse listComments(AdminCommentQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        Page<CommentEntity> page = commentMapper.selectPage(new Page<>(pageNo, pageSize), buildCommentListQuery(request));
        List<CommentEntity> comments = page.getRecords();

        Map<Long, PostEntity> postMap = loadPostMap(comments);
        Map<Long, UserEntity> userMap = loadUserMap(comments);
        Map<Long, CommentEntity> replyCommentMap = loadReplyCommentMap(comments);
        Map<Long, UserEntity> replyUserMap = loadReplyUserMap(replyCommentMap.values());

        List<AdminCommentItemResponse> items = comments.stream()
                .map(comment -> toAdminCommentItemResponse(comment, postMap, userMap, replyCommentMap, replyUserMap))
                .toList();
        long totalPages = page.getPages() <= 0 ? 1 : page.getPages();
        return new AdminCommentListResponse(items, page.getTotal(), pageNo, pageSize, totalPages);
    }

    /**
     * 查询后台评论树形审核列表。
     *
     * @param request 查询请求
     * @return 按文章分组的评论树形审核响应
     */
    public AdminCommentThreadGroupListResponse listCommentThreadGroups(AdminCommentQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());
        List<CommentEntity> matchedComments = commentMapper.selectList(buildCommentListQuery(request));
        List<Long> matchedPostIds = matchedComments.stream()
                .map(CommentEntity::getPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        long total = matchedPostIds.size();
        long totalPages = total == 0 ? 1 : (long) Math.ceil((double) total / pageSize);
        List<Long> currentPagePostIds = paginatePostIds(matchedPostIds, pageNo, pageSize);

        if (currentPagePostIds.isEmpty()) {
            return new AdminCommentThreadGroupListResponse(Collections.emptyList(), total, pageNo, pageSize, totalPages);
        }

        List<CommentEntity> pageComments = commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                .in(CommentEntity::getPostId, currentPagePostIds)
                .eq(CommentEntity::getDeleted, 0)
                .orderByDesc(CommentEntity::getCreatedTime)
                .orderByDesc(CommentEntity::getId));

        Map<Long, PostEntity> postMap = loadPostMap(pageComments);
        Map<Long, UserEntity> userMap = loadUserMap(pageComments);
        Map<Long, CommentEntity> replyCommentMap = loadReplyCommentMap(pageComments);
        Map<Long, UserEntity> replyUserMap = loadReplyUserMap(replyCommentMap.values());
        Set<Long> matchedCommentIds = matchedComments.stream()
                .filter(comment -> currentPagePostIds.contains(comment.getPostId()))
                .map(CommentEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<CommentEntity>> commentsByPost = pageComments.stream()
                .filter(comment -> comment.getPostId() != null)
                .collect(Collectors.groupingBy(CommentEntity::getPostId, LinkedHashMap::new, Collectors.toList()));
        boolean filteredByComment = isCommentLevelFilterActive(request);

        List<AdminCommentThreadGroupResponse> groups = currentPagePostIds.stream()
                .map(postId -> buildCommentThreadGroupResponse(postId,
                        commentsByPost.getOrDefault(postId, Collections.emptyList()),
                        postMap,
                        userMap,
                        replyCommentMap,
                        replyUserMap,
                        matchedCommentIds,
                        filteredByComment))
                .toList();

        return new AdminCommentThreadGroupListResponse(groups, total, pageNo, pageSize, totalPages);
    }

    /**
     * 更新评论状态。
     *
     * @param commentId 评论ID
     * @param status 目标状态
     * @return 更新后的评论条目
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminCommentItemResponse updateCommentStatus(Long commentId, String status) {
        CommentEntity targetComment = findCommentOrThrow(commentId);
        String normalizedStatus = status.trim();
        LocalDateTime now = LocalDateTime.now();

        if ("APPROVED".equals(normalizedStatus)) {
            commentMapper.update(null, new LambdaUpdateWrapper<CommentEntity>()
                    .eq(CommentEntity::getId, commentId)
                    .eq(CommentEntity::getDeleted, 0)
                    .set(CommentEntity::getStatus, normalizedStatus)
                    .set(CommentEntity::getUpdatedTime, now));
        } else {
            List<Long> subtreeCommentIds = collectSubtreeCommentIds(targetComment);
            commentMapper.update(null, new LambdaUpdateWrapper<CommentEntity>()
                    .in(CommentEntity::getId, subtreeCommentIds)
                    .eq(CommentEntity::getDeleted, 0)
                    .set(CommentEntity::getStatus, normalizedStatus)
                    .set(CommentEntity::getUpdatedTime, now));
        }

        syncApprovedCommentCount(targetComment.getPostId());
        return getComment(commentId);
    }

    /**
     * 软删除评论。
     *
     * @param commentId 评论ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        CommentEntity targetComment = findCommentOrThrow(commentId);
        List<Long> subtreeCommentIds = collectSubtreeCommentIds(targetComment);
        commentMapper.update(null, new LambdaUpdateWrapper<CommentEntity>()
                .in(CommentEntity::getId, subtreeCommentIds)
                .eq(CommentEntity::getDeleted, 0)
                .set(CommentEntity::getDeleted, 1)
                .set(CommentEntity::getUpdatedTime, LocalDateTime.now()));
        syncApprovedCommentCount(targetComment.getPostId());
    }

    /**
     * 获取单条评论详情。
     *
     * @param commentId 评论ID
     * @return 评论条目响应
     */
    public AdminCommentItemResponse getComment(Long commentId) {
        CommentEntity commentEntity = findCommentOrThrow(commentId);
        List<CommentEntity> comments = Collections.singletonList(commentEntity);
        Map<Long, PostEntity> postMap = loadPostMap(comments);
        Map<Long, UserEntity> userMap = loadUserMap(comments);
        Map<Long, CommentEntity> replyCommentMap = loadReplyCommentMap(comments);
        Map<Long, UserEntity> replyUserMap = loadReplyUserMap(replyCommentMap.values());
        return toAdminCommentItemResponse(commentEntity, postMap, userMap, replyCommentMap, replyUserMap);
    }

    /**
     * 构建评论列表查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    private LambdaQueryWrapper<CommentEntity> buildCommentListQuery(AdminCommentQueryRequest request) {
        LambdaQueryWrapper<CommentEntity> queryWrapper = new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getDeleted, 0)
                .orderByDesc(CommentEntity::getCreatedTime)
                .orderByDesc(CommentEntity::getId);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            queryWrapper.eq(CommentEntity::getStatus, request.getStatus().trim());
        }

        if (request.getPostId() != null && request.getPostId() > 0) {
            queryWrapper.eq(CommentEntity::getPostId, request.getPostId());
        }

        if (request.getUserId() != null && request.getUserId() > 0) {
            queryWrapper.eq(CommentEntity::getUserId, request.getUserId());
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            List<Long> matchedPostIds = findMatchedPostIds(keyword);
            List<Long> matchedUserIds = findMatchedUserIds(keyword);
            Long numericKeyword = parseLong(keyword);

            queryWrapper.and(wrapper -> {
                wrapper.like(CommentEntity::getContent, keyword);
                if (!matchedPostIds.isEmpty()) {
                    wrapper.or().in(CommentEntity::getPostId, matchedPostIds);
                }
                if (!matchedUserIds.isEmpty()) {
                    wrapper.or().in(CommentEntity::getUserId, matchedUserIds);
                }
                if (numericKeyword != null) {
                    wrapper.or().eq(CommentEntity::getId, numericKeyword)
                            .or().eq(CommentEntity::getPostId, numericKeyword)
                            .or().eq(CommentEntity::getUserId, numericKeyword);
                }
            });
        }

        return queryWrapper;
    }

    /**
     * 将评论实体转换为后台评论条目响应。
     *
     * @param commentEntity 评论实体
     * @param postMap 文章映射
     * @param userMap 用户映射
     * @param replyCommentMap 回复目标评论映射
     * @param replyUserMap 回复目标用户映射
     * @return 后台评论条目响应
     */
    private AdminCommentItemResponse toAdminCommentItemResponse(CommentEntity commentEntity,
                                                                Map<Long, PostEntity> postMap,
                                                                Map<Long, UserEntity> userMap,
                                                                Map<Long, CommentEntity> replyCommentMap,
                                                                Map<Long, UserEntity> replyUserMap) {
        PostEntity postEntity = postMap.get(commentEntity.getPostId());
        UserEntity userEntity = userMap.get(commentEntity.getUserId());
        CommentEntity replyComment = replyCommentMap.get(commentEntity.getReplyCommentId());
        UserEntity replyUser = replyComment == null ? null : replyUserMap.get(replyComment.getUserId());

        AdminCommentItemResponse response = new AdminCommentItemResponse();
        response.setId(commentEntity.getId());
        response.setPostId(commentEntity.getPostId());
        response.setPostTitle(postEntity == null ? "未知文章" : defaultString(postEntity.getTitle(), "未命名文章"));
        response.setPostSlug(postEntity == null ? "" : defaultString(postEntity.getSlug(), ""));
        response.setUserId(commentEntity.getUserId());
        response.setUserName(userEntity == null ? "未知用户" : defaultString(userEntity.getUserName(), "匿名用户"));
        response.setUserEmail(userEntity == null ? "" : defaultString(userEntity.getEmail(), ""));
        response.setReplyCommentId(commentEntity.getReplyCommentId());
        response.setRootId(commentEntity.getRootId());
        response.setReplyToUserName(replyUser == null ? null : defaultString(replyUser.getUserName(), "匿名用户"));
        response.setContent(commentEntity.getContent());
        response.setStatus(commentEntity.getStatus());
        response.setIp(defaultString(commentEntity.getIp(), ""));
        response.setCreatedTime(commentEntity.getCreatedTime());
        response.setUpdatedTime(commentEntity.getUpdatedTime());
        return response;
    }

    /**
     * 构建后台评论文章分组响应。
     *
     * @param postId 文章ID
     * @param comments 当前文章评论集合
     * @param postMap 文章映射
     * @param userMap 用户映射
     * @param replyCommentMap 回复目标评论映射
     * @param replyUserMap 回复目标用户映射
     * @param matchedCommentIds 命中评论ID集合
     * @param filteredByComment 当前是否启用评论级筛选
     * @return 文章分组响应
     */
    private AdminCommentThreadGroupResponse buildCommentThreadGroupResponse(Long postId,
                                                                           List<CommentEntity> comments,
                                                                           Map<Long, PostEntity> postMap,
                                                                           Map<Long, UserEntity> userMap,
                                                                           Map<Long, CommentEntity> replyCommentMap,
                                                                           Map<Long, UserEntity> replyUserMap,
                                                                           Set<Long> matchedCommentIds,
                                                                           boolean filteredByComment) {
        PostEntity postEntity = postMap.get(postId);
        List<CommentEntity> rootComments = comments.stream()
                .filter(comment -> comment.getRootId() == null)
                .sorted((left, right) -> compareCommentByTimeDesc(left, right))
                .toList();
        Map<Long, List<CommentEntity>> repliesByRootId = comments.stream()
                .filter(comment -> comment.getRootId() != null)
                .collect(Collectors.groupingBy(CommentEntity::getRootId, LinkedHashMap::new, Collectors.collectingAndThen(Collectors.toList(), list -> {
                    list.sort(this::compareCommentByTimeAsc);
                    return list;
                })));

        List<AdminCommentThreadRootItemResponse> roots = new ArrayList<>();
        for (CommentEntity rootComment : rootComments) {
            List<CommentEntity> allReplies = repliesByRootId.getOrDefault(rootComment.getId(), Collections.emptyList());
            boolean matchedRoot = matchedCommentIds.contains(rootComment.getId());
            List<CommentEntity> matchedReplies = allReplies.stream()
                    .filter(reply -> matchedCommentIds.contains(reply.getId()))
                    .toList();
            boolean shouldInclude = !filteredByComment || matchedRoot || !matchedReplies.isEmpty();
            if (!shouldInclude) {
                continue;
            }

            List<CommentEntity> displayReplies = !filteredByComment || matchedRoot ? allReplies : matchedReplies;
            roots.add(toAdminCommentThreadRootItemResponse(rootComment,
                    displayReplies,
                    userMap,
                    replyCommentMap,
                    replyUserMap,
                    matchedCommentIds,
                    !matchedReplies.isEmpty()));
        }

        AdminCommentThreadGroupResponse response = new AdminCommentThreadGroupResponse();
        response.setPostId(postId);
        response.setPostTitle(postEntity == null ? "未知文章" : defaultString(postEntity.getTitle(), "未命名文章"));
        response.setPostSlug(postEntity == null ? "" : defaultString(postEntity.getSlug(), ""));
        response.setTotalCommentCount((long) comments.size());
        response.setPendingCommentCount(countCommentsByStatus(comments, "PENDING"));
        response.setApprovedCommentCount(countCommentsByStatus(comments, "APPROVED"));
        response.setRejectedCommentCount(countCommentsByStatus(comments, "REJECTED"));
        response.setLatestCommentTime(resolveLatestCommentTime(comments));
        response.setRoots(roots);
        return response;
    }

    /**
     * 构建后台评论主评论响应。
     *
     * @param rootComment 根评论实体
     * @param replies 需要展示的回复集合
     * @param userMap 用户映射
     * @param replyCommentMap 回复目标评论映射
     * @param replyUserMap 回复目标用户映射
     * @param matchedCommentIds 命中评论ID集合
     * @param hasMatchingDescendant 是否存在命中的后代回复
     * @return 主评论响应
     */
    private AdminCommentThreadRootItemResponse toAdminCommentThreadRootItemResponse(CommentEntity rootComment,
                                                                                    List<CommentEntity> replies,
                                                                                    Map<Long, UserEntity> userMap,
                                                                                    Map<Long, CommentEntity> replyCommentMap,
                                                                                    Map<Long, UserEntity> replyUserMap,
                                                                                    Set<Long> matchedCommentIds,
                                                                                    boolean hasMatchingDescendant) {
        UserEntity userEntity = userMap.get(rootComment.getUserId());
        AdminCommentThreadRootItemResponse response = new AdminCommentThreadRootItemResponse();
        response.setId(rootComment.getId());
        response.setPostId(rootComment.getPostId());
        response.setUserId(rootComment.getUserId());
        response.setUserName(userEntity == null ? "未知用户" : defaultString(userEntity.getUserName(), "匿名用户"));
        response.setUserEmail(userEntity == null ? "" : defaultString(userEntity.getEmail(), ""));
        response.setContent(rootComment.getContent());
        response.setStatus(rootComment.getStatus());
        response.setIp(defaultString(rootComment.getIp(), ""));
        response.setCreatedTime(rootComment.getCreatedTime());
        response.setUpdatedTime(rootComment.getUpdatedTime());
        response.setVisibleOnSite(isCommentVisibleOnSite(rootComment));
        response.setMatchedByFilter(matchedCommentIds.contains(rootComment.getId()));
        response.setHasMatchingDescendant(hasMatchingDescendant);
        response.setReplies(replies.stream()
                .map(reply -> toAdminCommentThreadReplyItemResponse(reply, userMap, replyCommentMap, replyUserMap, matchedCommentIds))
                .toList());
        return response;
    }

    /**
     * 构建后台评论回复流响应。
     *
     * @param replyComment 回复评论实体
     * @param userMap 用户映射
     * @param replyCommentMap 回复目标评论映射
     * @param replyUserMap 回复目标用户映射
     * @param matchedCommentIds 命中评论ID集合
     * @return 回复流响应
     */
    private AdminCommentThreadReplyItemResponse toAdminCommentThreadReplyItemResponse(CommentEntity replyComment,
                                                                                      Map<Long, UserEntity> userMap,
                                                                                      Map<Long, CommentEntity> replyCommentMap,
                                                                                      Map<Long, UserEntity> replyUserMap,
                                                                                      Set<Long> matchedCommentIds) {
        UserEntity userEntity = userMap.get(replyComment.getUserId());
        CommentEntity targetComment = replyCommentMap.get(replyComment.getReplyCommentId());
        UserEntity replyTargetUser = targetComment == null ? null : replyUserMap.get(targetComment.getUserId());

        AdminCommentThreadReplyItemResponse response = new AdminCommentThreadReplyItemResponse();
        response.setId(replyComment.getId());
        response.setPostId(replyComment.getPostId());
        response.setRootId(replyComment.getRootId());
        response.setReplyCommentId(replyComment.getReplyCommentId());
        response.setReplyToUserName(replyTargetUser == null ? null : defaultString(replyTargetUser.getUserName(), "匿名用户"));
        response.setUserId(replyComment.getUserId());
        response.setUserName(userEntity == null ? "未知用户" : defaultString(userEntity.getUserName(), "匿名用户"));
        response.setUserEmail(userEntity == null ? "" : defaultString(userEntity.getEmail(), ""));
        response.setContent(replyComment.getContent());
        response.setStatus(replyComment.getStatus());
        response.setIp(defaultString(replyComment.getIp(), ""));
        response.setCreatedTime(replyComment.getCreatedTime());
        response.setUpdatedTime(replyComment.getUpdatedTime());
        response.setVisibleOnSite(isCommentVisibleOnSite(replyComment));
        response.setMatchedByFilter(matchedCommentIds.contains(replyComment.getId()));
        return response;
    }

    /**
     * 加载评论关联文章映射。
     *
     * @param comments 评论集合
     * @return 文章映射
     */
    private Map<Long, PostEntity> loadPostMap(List<CommentEntity> comments) {
        Set<Long> postIds = comments.stream()
                .map(CommentEntity::getPostId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                        .in(PostEntity::getId, postIds)
                        .eq(PostEntity::getDeleted, 0))
                .stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
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
     * 加载回复目标评论映射。
     *
     * @param comments 评论集合
     * @return 回复目标评论映射
     */
    private Map<Long, CommentEntity> loadReplyCommentMap(List<CommentEntity> comments) {
        Set<Long> replyCommentIds = comments.stream()
                .map(CommentEntity::getReplyCommentId)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        if (replyCommentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                        .in(CommentEntity::getId, replyCommentIds)
                        .eq(CommentEntity::getDeleted, 0))
                .stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);
    }

    /**
     * 加载回复目标作者映射。
     *
     * @param replyComments 回复目标评论集合
     * @return 回复目标作者映射
     */
    private Map<Long, UserEntity> loadReplyUserMap(Iterable<CommentEntity> replyComments) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (CommentEntity replyComment : replyComments) {
            if (replyComment != null && replyComment.getUserId() != null) {
                userIds.add(replyComment.getUserId());
            }
        }
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
     * 查询匹配关键词的文章ID集合。
     *
     * @param keyword 搜索关键词
     * @return 文章ID集合
     */
    private List<Long> findMatchedPostIds(String keyword) {
        return postMapper.selectList(new LambdaQueryWrapper<PostEntity>()
                        .eq(PostEntity::getDeleted, 0)
                        .and(wrapper -> wrapper
                                .like(PostEntity::getTitle, keyword)
                                .or()
                                .like(PostEntity::getSlug, keyword)))
                .stream()
                .map(PostEntity::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 判断当前请求是否启用了评论级筛选。
     *
     * @param request 查询请求
     * @return 是否启用评论级筛选
     */
    private boolean isCommentLevelFilterActive(AdminCommentQueryRequest request) {
        return (request.getKeyword() != null && !request.getKeyword().isBlank())
                || (request.getStatus() != null && !request.getStatus().isBlank())
                || (request.getUserId() != null && request.getUserId() > 0);
    }

    /**
     * 对文章ID列表执行分页。
     *
     * @param postIds 文章ID列表
     * @param pageNo 当前页码
     * @param pageSize 每页数量
     * @return 当前页文章ID列表
     */
    private List<Long> paginatePostIds(List<Long> postIds, long pageNo, long pageSize) {
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }

        int fromIndex = (int) Math.min(postIds.size(), Math.max(0, (pageNo - 1) * pageSize));
        int toIndex = (int) Math.min(postIds.size(), fromIndex + pageSize);
        if (fromIndex >= toIndex) {
            return Collections.emptyList();
        }
        return postIds.subList(fromIndex, toIndex);
    }

    /**
     * 统计指定状态的评论数量。
     *
     * @param comments 评论集合
     * @param status 目标状态
     * @return 评论数量
     */
    private long countCommentsByStatus(List<CommentEntity> comments, String status) {
        return comments.stream()
                .filter(comment -> Objects.equals(comment.getStatus(), status))
                .count();
    }

    /**
     * 解析文章下最近一条评论时间。
     *
     * @param comments 评论集合
     * @return 最近评论时间
     */
    private LocalDateTime resolveLatestCommentTime(List<CommentEntity> comments) {
        return comments.stream()
                .map(CommentEntity::getCreatedTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * 判断评论当前是否在前台可见。
     *
     * @param commentEntity 评论实体
     * @return 是否前台可见
     */
    private boolean isCommentVisibleOnSite(CommentEntity commentEntity) {
        return commentEntity != null && Objects.equals(commentEntity.getStatus(), "APPROVED");
    }

    /**
     * 比较评论创建时间，按从新到旧排序。
     *
     * @param left 左侧评论
     * @param right 右侧评论
     * @return 比较结果
     */
    private int compareCommentByTimeDesc(CommentEntity left, CommentEntity right) {
        return compareCommentByTimeAsc(right, left);
    }

    /**
     * 比较评论创建时间，按从旧到新排序。
     *
     * @param left 左侧评论
     * @param right 右侧评论
     * @return 比较结果
     */
    private int compareCommentByTimeAsc(CommentEntity left, CommentEntity right) {
        LocalDateTime leftTime = left == null ? null : left.getCreatedTime();
        LocalDateTime rightTime = right == null ? null : right.getCreatedTime();
        if (leftTime == null && rightTime == null) {
            return compareCommentId(left, right);
        }
        if (leftTime == null) {
            return -1;
        }
        if (rightTime == null) {
            return 1;
        }

        int timeCompare = leftTime.compareTo(rightTime);
        return timeCompare != 0 ? timeCompare : compareCommentId(left, right);
    }

    /**
     * 比较评论ID。
     *
     * @param left 左侧评论
     * @param right 右侧评论
     * @return 比较结果
     */
    private int compareCommentId(CommentEntity left, CommentEntity right) {
        Long leftId = left == null ? null : left.getId();
        Long rightId = right == null ? null : right.getId();
        if (leftId == null && rightId == null) {
            return 0;
        }
        if (leftId == null) {
            return -1;
        }
        if (rightId == null) {
            return 1;
        }
        return leftId.compareTo(rightId);
    }

    /**
     * 查询匹配关键词的用户ID集合。
     *
     * @param keyword 搜索关键词
     * @return 用户ID集合
     */
    private List<Long> findMatchedUserIds(String keyword) {
        return userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                        .eq(UserEntity::getDeleted, 0)
                        .and(wrapper -> wrapper
                                .like(UserEntity::getUserName, keyword)
                                .or()
                                .like(UserEntity::getEmail, keyword)))
                .stream()
                .map(UserEntity::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 收集指定评论及其回复子树的评论ID。
     *
     * @param commentEntity 起始评论
     * @return 子树评论ID列表
     */
    private List<Long> collectSubtreeCommentIds(CommentEntity commentEntity) {
        Long threadRootId = commentEntity.getRootId() == null ? commentEntity.getId() : commentEntity.getRootId();
        List<CommentEntity> threadComments = commentMapper.selectList(new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getPostId, commentEntity.getPostId())
                .eq(CommentEntity::getDeleted, 0)
                .and(wrapper -> wrapper
                        .eq(CommentEntity::getId, threadRootId)
                        .or()
                        .eq(CommentEntity::getRootId, threadRootId))
                .orderByAsc(CommentEntity::getCreatedTime)
                .orderByAsc(CommentEntity::getId));

        Map<Long, List<Long>> childMap = new LinkedHashMap<>();
        for (CommentEntity item : threadComments) {
            if (item.getReplyCommentId() != null) {
                childMap.computeIfAbsent(item.getReplyCommentId(), key -> new ArrayList<>()).add(item.getId());
            }
        }

        List<Long> ids = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(commentEntity.getId());

        while (!queue.isEmpty()) {
            Long currentId = queue.removeFirst();
            ids.add(currentId);
            for (Long childId : childMap.getOrDefault(currentId, Collections.emptyList())) {
                queue.addLast(childId);
            }
        }

        return ids;
    }

    /**
     * 按ID查询评论，不存在时抛出异常。
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    private CommentEntity findCommentOrThrow(Long commentId) {
        CommentEntity commentEntity = commentMapper.selectOne(new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getId, commentId)
                .eq(CommentEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (commentEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评论不存在");
        }
        return commentEntity;
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
                .set("comment_count", approvedCount == null ? 0L : approvedCount)
                .set("updated_time", LocalDateTime.now()));
    }

    /**
     * 解析长整型值。
     *
     * @param value 原始字符串
     * @return 长整型值
     */
    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
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
        return Math.min(pageSize, 50);
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
}
