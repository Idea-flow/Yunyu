package com.ideaflow.yunyu.module.topic.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.post.site.service.PostSiteService;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import com.ideaflow.yunyu.module.topic.site.vo.SiteTopicDetailResponse;
import com.ideaflow.yunyu.module.topic.site.vo.SiteTopicItemResponse;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 前台专题服务类。
 * 作用：承接前台专题列表与专题详情查询，并复用前台文章服务完成专题页文章分页。
 */
@Service
public class TopicSiteService {

    private final TopicMapper topicMapper;
    private final PostMapper postMapper;
    private final PostSiteService postSiteService;

    /**
     * 创建前台专题服务。
     *
     * @param topicMapper 专题 Mapper
     * @param postMapper 文章 Mapper
     * @param postSiteService 前台文章服务
     */
    public TopicSiteService(TopicMapper topicMapper,
                            PostMapper postMapper,
                            PostSiteService postSiteService) {
        this.topicMapper = topicMapper;
        this.postMapper = postMapper;
        this.postSiteService = postSiteService;
    }

    /**
     * 查询前台专题列表。
     *
     * @return 专题列表
     */
    public List<SiteTopicItemResponse> listTopics() {
        List<TopicEntity> topicEntities = topicMapper.selectList(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getDeleted, 0)
                .eq(TopicEntity::getStatus, "ACTIVE")
                .orderByAsc(TopicEntity::getSortOrder)
                .orderByAsc(TopicEntity::getId));

        return topicEntities.stream()
                .map(this::toSiteTopicItemResponse)
                .toList();
    }

    /**
     * 查询前台专题详情。
     *
     * @param slug 专题 slug
     * @param request 查询请求
     * @return 专题详情
     */
    public SiteTopicDetailResponse getTopicDetail(String slug, SitePostQueryRequest request) {
        TopicEntity topicEntity = findTopicBySlug(slug);
        SitePostQueryRequest actualRequest = copyRequest(request);
        actualRequest.setTopicSlug(topicEntity.getSlug());

        SiteTopicDetailResponse response = new SiteTopicDetailResponse();
        response.setTopic(toSiteTopicItemResponse(topicEntity));
        response.setPosts(postSiteService.listPosts(actualRequest));
        return response;
    }

    /**
     * 通过 slug 查询前台可见专题。
     *
     * @param slug 专题 slug
     * @return 专题实体
     */
    private TopicEntity findTopicBySlug(String slug) {
        TopicEntity topicEntity = topicMapper.selectOne(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getSlug, slug)
                .eq(TopicEntity::getDeleted, 0)
                .eq(TopicEntity::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (topicEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "专题不存在");
        }
        return topicEntity;
    }

    /**
     * 转换前台专题摘要响应。
     *
     * @param topicEntity 专题实体
     * @return 专题摘要响应
     */
    private SiteTopicItemResponse toSiteTopicItemResponse(TopicEntity topicEntity) {
        SiteTopicItemResponse response = new SiteTopicItemResponse();
        response.setId(topicEntity.getId());
        response.setName(topicEntity.getName());
        response.setSlug(topicEntity.getSlug());
        response.setSummary(defaultString(topicEntity.getSummary()));
        response.setCoverUrl(defaultString(topicEntity.getCoverUrl()));
        response.setArticleCount(countPostsByTopicId(topicEntity.getId()));
        return response;
    }

    /**
     * 统计专题下已发布文章数量。
     *
     * @param topicId 专题 ID
     * @return 文章数量
     */
    private Long countPostsByTopicId(Long topicId) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .inSql(PostEntity::getId, "SELECT post_id FROM topic_post WHERE topic_id = " + topicId));
        return count == null ? 0L : count;
    }

    /**
     * 复制查询参数。
     *
     * @param request 原始请求
     * @return 复制后的请求
     */
    private SitePostQueryRequest copyRequest(SitePostQueryRequest request) {
        SitePostQueryRequest copiedRequest = new SitePostQueryRequest();
        if (request == null) {
            return copiedRequest;
        }
        copiedRequest.setKeyword(request.getKeyword());
        copiedRequest.setCategorySlug(request.getCategorySlug());
        copiedRequest.setTopicSlug(request.getTopicSlug());
        copiedRequest.setTagSlug(request.getTagSlug());
        copiedRequest.setPageNo(request.getPageNo());
        copiedRequest.setPageSize(request.getPageSize());
        return copiedRequest;
    }

    /**
     * 返回默认字符串。
     *
     * @param value 原始值
     * @return 默认化后的字符串
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
