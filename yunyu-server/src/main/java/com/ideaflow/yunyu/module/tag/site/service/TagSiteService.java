package com.ideaflow.yunyu.module.tag.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.post.site.dto.SitePostQueryRequest;
import com.ideaflow.yunyu.module.post.site.service.PostSiteService;
import com.ideaflow.yunyu.module.tag.entity.TagEntity;
import com.ideaflow.yunyu.module.tag.mapper.TagMapper;
import com.ideaflow.yunyu.module.tag.site.vo.SiteTagDetailResponse;
import com.ideaflow.yunyu.module.tag.site.vo.SiteTagItemResponse;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 前台标签服务类。
 * 作用：承接前台标签列表与标签详情查询，并复用前台文章服务完成标签页文章分页。
 */
@Service
public class TagSiteService {

    private final TagMapper tagMapper;
    private final PostMapper postMapper;
    private final PostSiteService postSiteService;

    /**
     * 创建前台标签服务。
     *
     * @param tagMapper 标签 Mapper
     * @param postMapper 文章 Mapper
     * @param postSiteService 前台文章服务
     */
    public TagSiteService(TagMapper tagMapper,
                          PostMapper postMapper,
                          PostSiteService postSiteService) {
        this.tagMapper = tagMapper;
        this.postMapper = postMapper;
        this.postSiteService = postSiteService;
    }

    /**
     * 查询前台标签列表。
     *
     * @return 标签列表
     */
    public List<SiteTagItemResponse> listTags() {
        List<TagEntity> tagEntities = tagMapper.selectList(new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getDeleted, 0)
                .eq(TagEntity::getStatus, "ACTIVE")
                .orderByAsc(TagEntity::getName)
                .orderByAsc(TagEntity::getId));

        return tagEntities.stream()
                .map(this::toSiteTagItemResponse)
                .toList();
    }

    /**
     * 查询前台标签详情。
     *
     * @param slug 标签 slug
     * @param request 查询请求
     * @return 标签详情
     */
    public SiteTagDetailResponse getTagDetail(String slug, SitePostQueryRequest request) {
        TagEntity tagEntity = findTagBySlug(slug);
        SitePostQueryRequest actualRequest = copyRequest(request);
        actualRequest.setTagSlug(tagEntity.getSlug());

        SiteTagDetailResponse response = new SiteTagDetailResponse();
        response.setTag(toSiteTagItemResponse(tagEntity));
        response.setPosts(postSiteService.listPosts(actualRequest));
        return response;
    }

    /**
     * 通过 slug 查询前台可见标签。
     *
     * @param slug 标签 slug
     * @return 标签实体
     */
    private TagEntity findTagBySlug(String slug) {
        TagEntity tagEntity = tagMapper.selectOne(new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getSlug, slug)
                .eq(TagEntity::getDeleted, 0)
                .eq(TagEntity::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (tagEntity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return tagEntity;
    }

    /**
     * 转换前台标签摘要响应。
     *
     * @param tagEntity 标签实体
     * @return 标签摘要响应
     */
    private SiteTagItemResponse toSiteTagItemResponse(TagEntity tagEntity) {
        SiteTagItemResponse response = new SiteTagItemResponse();
        response.setId(tagEntity.getId());
        response.setName(tagEntity.getName());
        response.setSlug(tagEntity.getSlug());
        response.setDescription(defaultString(tagEntity.getDescription()));
        response.setArticleCount(countPostsByTagId(tagEntity.getId()));
        return response;
    }

    /**
     * 统计标签下已发布文章数量。
     *
     * @param tagId 标签 ID
     * @return 文章数量
     */
    private Long countPostsByTagId(Long tagId) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED")
                .inSql(PostEntity::getId, "SELECT post_id FROM post_tag WHERE tag_id = " + tagId));
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
