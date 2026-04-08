package com.ideaflow.yunyu.module.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.category.entity.CategoryEntity;
import com.ideaflow.yunyu.module.category.mapper.CategoryMapper;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import com.ideaflow.yunyu.module.site.dto.AdminHomepageConfigUpdateRequest;
import com.ideaflow.yunyu.module.site.dto.AdminHomepageHeroStatRequest;
import com.ideaflow.yunyu.module.site.entity.SiteConfigEntity;
import com.ideaflow.yunyu.module.site.mapper.SiteConfigMapper;
import com.ideaflow.yunyu.module.site.vo.AdminHomepageConfigResponse;
import com.ideaflow.yunyu.module.site.vo.AdminHomepageHeroStatResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomepageConfigResponse;
import com.ideaflow.yunyu.module.site.vo.SiteHomepageHeroStatResponse;
import com.ideaflow.yunyu.module.topic.entity.TopicEntity;
import com.ideaflow.yunyu.module.topic.mapper.TopicMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首页配置服务类。
 * 作用：统一负责首页品牌首屏配置的读取、保存、默认值回退和前后台响应结构转换。
 */
@Service
public class HomepageConfigService {

    private static final String HOMEPAGE_CONFIG_KEY = "homepage_config";
    private static final String DEFAULT_HERO_LAYOUT = "brand";
    private static final String DEFAULT_BACKGROUND_MODE = "gradient-grid";
    private static final String DEFAULT_HERO_EYEBROW = "Yunyu / 云屿";
    private static final String DEFAULT_HERO_TITLE = "把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站";
    private static final String DEFAULT_HERO_SUBTITLE = "记录技术、审美、创作与阅读的个人博客与内容网站";
    private static final String DEFAULT_PRIMARY_BUTTON_TEXT = "查看文章";
    private static final String DEFAULT_PRIMARY_BUTTON_LINK = "/posts";
    private static final String DEFAULT_SECONDARY_BUTTON_TEXT = "进入专题";
    private static final String DEFAULT_SECONDARY_BUTTON_LINK = "/topics";
    private static final boolean DEFAULT_HERO_VISUAL_CLICKABLE = true;
    private static final String DEFAULT_FEATURED_SECTION_TITLE = "推荐";
    private static final String DEFAULT_LATEST_SECTION_TITLE = "最新文章";
    private static final String DEFAULT_CATEGORY_SECTION_TITLE = "分类";
    private static final String DEFAULT_TOPIC_SECTION_TITLE = "专题";

    private final SiteConfigMapper siteConfigMapper;
    private final PostMapper postMapper;
    private final CategoryMapper categoryMapper;
    private final TopicMapper topicMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建首页配置服务。
     *
     * @param siteConfigMapper 站点配置 Mapper
     * @param postMapper 文章 Mapper
     * @param categoryMapper 分类 Mapper
     * @param topicMapper 专题 Mapper
     * @param objectMapper JSON 对象映射器
     */
    public HomepageConfigService(SiteConfigMapper siteConfigMapper,
                                 PostMapper postMapper,
                                 CategoryMapper categoryMapper,
                                 TopicMapper topicMapper,
                                 ObjectMapper objectMapper) {
        this.siteConfigMapper = siteConfigMapper;
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.topicMapper = topicMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询后台首页配置。
     *
     * @return 后台首页配置响应
     */
    public AdminHomepageConfigResponse getAdminHomepageConfig() {
        JsonNode configNode = readHomepageConfigNode();
        AdminHomepageConfigResponse response = new AdminHomepageConfigResponse();
        response.setHeroEnabled(readJsonBoolean(configNode, "heroEnabled", true));
        response.setHeroLayout(readHeroLayout(configNode));
        response.setHeroBackgroundMode(readBackgroundMode(configNode));
        response.setHeroEyebrow(readJsonText(configNode, "heroEyebrow", DEFAULT_HERO_EYEBROW));
        response.setHeroTitle(readJsonText(configNode, "heroTitle", DEFAULT_HERO_TITLE));
        response.setHeroSubtitle(readJsonText(configNode, "heroSubtitle", DEFAULT_HERO_SUBTITLE));
        response.setHeroPrimaryButtonText(readJsonText(configNode, "heroPrimaryButtonText", DEFAULT_PRIMARY_BUTTON_TEXT));
        response.setHeroPrimaryButtonLink(readJsonText(configNode, "heroPrimaryButtonLink", DEFAULT_PRIMARY_BUTTON_LINK));
        response.setHeroSecondaryButtonText(readJsonText(configNode, "heroSecondaryButtonText", DEFAULT_SECONDARY_BUTTON_TEXT));
        response.setHeroSecondaryButtonLink(readJsonText(configNode, "heroSecondaryButtonLink", DEFAULT_SECONDARY_BUTTON_LINK));
        response.setHeroVisualPostId(readJsonLong(configNode, "heroVisualPostId"));
        response.setHeroVisualClickable(readJsonBoolean(configNode, "heroVisualClickable", DEFAULT_HERO_VISUAL_CLICKABLE));
        response.setHeroKeywords(readHeroKeywords(configNode));
        response.setShowHeroKeywords(readJsonBoolean(configNode, "showHeroKeywords", true));
        response.setShowHeroStats(readJsonBoolean(configNode, "showHeroStats", true));
        response.setHeroStats(readAdminHeroStats(configNode));
        response.setShowFeaturedSection(readJsonBoolean(configNode, "showFeaturedSection", true));
        response.setFeaturedSectionTitle(readJsonText(configNode, "featuredSectionTitle", DEFAULT_FEATURED_SECTION_TITLE));
        response.setShowLatestSection(readJsonBoolean(configNode, "showLatestSection", true));
        response.setLatestSectionTitle(readJsonText(configNode, "latestSectionTitle", DEFAULT_LATEST_SECTION_TITLE));
        response.setShowCategorySection(readJsonBoolean(configNode, "showCategorySection", true));
        response.setCategorySectionTitle(readJsonText(configNode, "categorySectionTitle", DEFAULT_CATEGORY_SECTION_TITLE));
        response.setShowTopicSection(readJsonBoolean(configNode, "showTopicSection", true));
        response.setTopicSectionTitle(readJsonText(configNode, "topicSectionTitle", DEFAULT_TOPIC_SECTION_TITLE));
        return response;
    }

    /**
     * 更新后台首页配置。
     *
     * @param request 首页配置更新请求
     * @return 更新后的首页配置
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminHomepageConfigResponse updateHomepageConfig(AdminHomepageConfigUpdateRequest request) {
        validateHeroVisualPost(request.getHeroVisualPostId());
        saveConfig(HOMEPAGE_CONFIG_KEY, "首页配置", buildHomepageConfigJson(request), "首页无封面首屏与首页模块开关配置");
        return getAdminHomepageConfig();
    }

    /**
     * 查询前台首页配置。
     *
     * @return 前台首页配置响应
     */
    public SiteHomepageConfigResponse getSiteHomepageConfig() {
        JsonNode configNode = readHomepageConfigNode();
        SiteHomepageConfigResponse response = new SiteHomepageConfigResponse();
        response.setHeroEnabled(readJsonBoolean(configNode, "heroEnabled", true));
        response.setHeroLayout(readHeroLayout(configNode));
        response.setHeroBackgroundMode(readBackgroundMode(configNode));
        response.setHeroEyebrow(readJsonText(configNode, "heroEyebrow", DEFAULT_HERO_EYEBROW));
        response.setHeroTitle(readJsonText(configNode, "heroTitle", DEFAULT_HERO_TITLE));
        response.setHeroSubtitle(readJsonText(configNode, "heroSubtitle", DEFAULT_HERO_SUBTITLE));
        response.setHeroPrimaryButtonText(readJsonText(configNode, "heroPrimaryButtonText", DEFAULT_PRIMARY_BUTTON_TEXT));
        response.setHeroPrimaryButtonLink(readJsonText(configNode, "heroPrimaryButtonLink", DEFAULT_PRIMARY_BUTTON_LINK));
        response.setHeroSecondaryButtonText(readJsonText(configNode, "heroSecondaryButtonText", DEFAULT_SECONDARY_BUTTON_TEXT));
        response.setHeroSecondaryButtonLink(readJsonText(configNode, "heroSecondaryButtonLink", DEFAULT_SECONDARY_BUTTON_LINK));
        response.setHeroVisualPostId(readJsonLong(configNode, "heroVisualPostId"));
        response.setHeroVisualClickable(readJsonBoolean(configNode, "heroVisualClickable", DEFAULT_HERO_VISUAL_CLICKABLE));
        response.setHeroKeywords(readHeroKeywords(configNode));
        response.setShowHeroKeywords(readJsonBoolean(configNode, "showHeroKeywords", true));
        response.setShowHeroStats(readJsonBoolean(configNode, "showHeroStats", true));
        response.setHeroStats(readSiteHeroStats(configNode));
        response.setShowFeaturedSection(readJsonBoolean(configNode, "showFeaturedSection", true));
        response.setFeaturedSectionTitle(readJsonText(configNode, "featuredSectionTitle", DEFAULT_FEATURED_SECTION_TITLE));
        response.setShowLatestSection(readJsonBoolean(configNode, "showLatestSection", true));
        response.setLatestSectionTitle(readJsonText(configNode, "latestSectionTitle", DEFAULT_LATEST_SECTION_TITLE));
        response.setShowCategorySection(readJsonBoolean(configNode, "showCategorySection", true));
        response.setCategorySectionTitle(readJsonText(configNode, "categorySectionTitle", DEFAULT_CATEGORY_SECTION_TITLE));
        response.setShowTopicSection(readJsonBoolean(configNode, "showTopicSection", true));
        response.setTopicSectionTitle(readJsonText(configNode, "topicSectionTitle", DEFAULT_TOPIC_SECTION_TITLE));
        return response;
    }

    /**
     * 读取首页配置 JSON 节点。
     *
     * @return 首页配置 JSON 节点
     */
    private JsonNode readHomepageConfigNode() {
        SiteConfigEntity configEntity = findConfigByKey(HOMEPAGE_CONFIG_KEY);
        return readJsonNode(configEntity == null ? null : configEntity.getConfigJson());
    }

    /**
     * 构建首页配置 JSON。
     *
     * @param request 首页配置更新请求
     * @return 首页配置 JSON 字符串
     */
    private String buildHomepageConfigJson(AdminHomepageConfigUpdateRequest request) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("heroEnabled", normalizeBoolean(request.getHeroEnabled(), true));
        jsonNode.put("heroLayout", normalizeHeroLayout(request.getHeroLayout()));
        jsonNode.put("heroBackgroundMode", normalizeBackgroundMode(request.getHeroBackgroundMode()));
        jsonNode.put("heroEyebrow", normalizeText(request.getHeroEyebrow()));
        jsonNode.put("heroTitle", normalizeText(request.getHeroTitle()));
        jsonNode.put("heroSubtitle", normalizeText(request.getHeroSubtitle()));
        jsonNode.put("heroPrimaryButtonText", normalizeText(request.getHeroPrimaryButtonText()));
        jsonNode.put("heroPrimaryButtonLink", normalizeText(request.getHeroPrimaryButtonLink()));
        jsonNode.put("heroSecondaryButtonText", normalizeText(request.getHeroSecondaryButtonText()));
        jsonNode.put("heroSecondaryButtonLink", normalizeText(request.getHeroSecondaryButtonLink()));
        if (request.getHeroVisualPostId() == null) {
            jsonNode.putNull("heroVisualPostId");
        } else {
            jsonNode.put("heroVisualPostId", request.getHeroVisualPostId());
        }
        jsonNode.put("heroVisualClickable", normalizeBoolean(request.getHeroVisualClickable(), DEFAULT_HERO_VISUAL_CLICKABLE));
        jsonNode.set("heroKeywords", buildHeroKeywordsArray(request.getHeroKeywords()));
        jsonNode.put("showHeroKeywords", normalizeBoolean(request.getShowHeroKeywords(), true));
        jsonNode.put("showHeroStats", normalizeBoolean(request.getShowHeroStats(), true));
        jsonNode.set("heroStats", buildHeroStatsArray(request.getHeroStats()));
        jsonNode.put("showFeaturedSection", normalizeBoolean(request.getShowFeaturedSection(), true));
        jsonNode.put("featuredSectionTitle", normalizeText(request.getFeaturedSectionTitle()));
        jsonNode.put("showLatestSection", normalizeBoolean(request.getShowLatestSection(), true));
        jsonNode.put("latestSectionTitle", normalizeText(request.getLatestSectionTitle()));
        jsonNode.put("showCategorySection", normalizeBoolean(request.getShowCategorySection(), true));
        jsonNode.put("categorySectionTitle", normalizeText(request.getCategorySectionTitle()));
        jsonNode.put("showTopicSection", normalizeBoolean(request.getShowTopicSection(), true));
        jsonNode.put("topicSectionTitle", normalizeText(request.getTopicSectionTitle()));
        return writeJson(jsonNode);
    }

    /**
     * 读取首页布局值。
     *
     * @param configNode 配置节点
     * @return 首页布局值
     */
    private String readHeroLayout(JsonNode configNode) {
        return normalizeHeroLayout(readJsonText(configNode, "heroLayout", DEFAULT_HERO_LAYOUT));
    }

    /**
     * 读取首页背景模式值。
     *
     * @param configNode 配置节点
     * @return 首页背景模式值
     */
    private String readBackgroundMode(JsonNode configNode) {
        return normalizeBackgroundMode(readJsonText(configNode, "heroBackgroundMode", DEFAULT_BACKGROUND_MODE));
    }

    /**
     * 读取首页关键词列表。
     *
     * @param configNode 配置节点
     * @return 首页关键词列表
     */
    private List<String> readHeroKeywords(JsonNode configNode) {
        JsonNode keywordsNode = configNode.path("heroKeywords");
        List<String> keywords = new ArrayList<>();
        if (keywordsNode.isArray()) {
            for (JsonNode keywordNode : keywordsNode) {
                String keyword = normalizeText(keywordNode.asText(""));
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                }
                if (keywords.size() >= 6) {
                    break;
                }
            }

            return keywords;
        }

        return List.of("写作", "技术", "审美", "长期主义");
    }

    /**
     * 读取后台首页统计项列表。
     *
     * @param configNode 配置节点
     * @return 后台首页统计项列表
     */
    private List<AdminHomepageHeroStatResponse> readAdminHeroStats(JsonNode configNode) {
        JsonNode statsNode = configNode.path("heroStats");
        List<AdminHomepageHeroStatResponse> stats = new ArrayList<>();
        if (statsNode.isArray()) {
            for (JsonNode statNode : statsNode) {
                String label = normalizeText(statNode.path("label").asText(""));
                String value = normalizeText(statNode.path("value").asText(""));
                if (label.isEmpty() || value.isEmpty()) {
                    continue;
                }
                AdminHomepageHeroStatResponse item = new AdminHomepageHeroStatResponse();
                item.setLabel(label);
                item.setValue(value);
                stats.add(item);
                if (stats.size() >= 4) {
                    break;
                }
            }
        }

        if (!stats.isEmpty()) {
            return stats;
        }

        return buildDefaultAdminHeroStats();
    }

    /**
     * 读取前台首页统计项列表。
     *
     * @param configNode 配置节点
     * @return 前台首页统计项列表
     */
    private List<SiteHomepageHeroStatResponse> readSiteHeroStats(JsonNode configNode) {
        JsonNode statsNode = configNode.path("heroStats");
        List<SiteHomepageHeroStatResponse> stats = new ArrayList<>();
        if (statsNode.isArray()) {
            for (JsonNode statNode : statsNode) {
                String label = normalizeText(statNode.path("label").asText(""));
                String value = normalizeText(statNode.path("value").asText(""));
                if (label.isEmpty() || value.isEmpty()) {
                    continue;
                }
                SiteHomepageHeroStatResponse item = new SiteHomepageHeroStatResponse();
                item.setLabel(label);
                item.setValue(value);
                stats.add(item);
                if (stats.size() >= 4) {
                    break;
                }
            }
        }

        if (!stats.isEmpty()) {
            return stats;
        }

        return buildDefaultSiteHeroStats();
    }

    /**
     * 构建首页关键词数组节点。
     *
     * @param keywords 关键词列表
     * @return 数组节点
     */
    private ArrayNode buildHeroKeywordsArray(List<String> keywords) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        if (keywords == null) {
            return arrayNode;
        }

        int count = 0;
        for (String keyword : keywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (normalizedKeyword.isEmpty()) {
                continue;
            }
            arrayNode.add(normalizedKeyword);
            count++;
            if (count >= 6) {
                break;
            }
        }
        return arrayNode;
    }

    /**
     * 构建首页统计项数组节点。
     *
     * @param heroStats 统计项列表
     * @return 数组节点
     */
    private ArrayNode buildHeroStatsArray(List<AdminHomepageHeroStatRequest> heroStats) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        if (heroStats == null) {
            return arrayNode;
        }

        int count = 0;
        for (AdminHomepageHeroStatRequest heroStat : heroStats) {
            if (heroStat == null) {
                continue;
            }
            String label = normalizeText(heroStat.getLabel());
            String value = normalizeText(heroStat.getValue());
            if (label.isEmpty() || value.isEmpty()) {
                continue;
            }
            ObjectNode statNode = objectMapper.createObjectNode();
            statNode.put("label", label);
            statNode.put("value", value);
            arrayNode.add(statNode);
            count++;
            if (count >= 4) {
                break;
            }
        }
        return arrayNode;
    }

    /**
     * 构建默认后台首页统计项。
     *
     * @return 默认统计项列表
     */
    private List<AdminHomepageHeroStatResponse> buildDefaultAdminHeroStats() {
        List<AdminHomepageHeroStatResponse> stats = new ArrayList<>();
        stats.add(buildAdminHeroStat("文章", String.valueOf(countPublishedPosts())));
        stats.add(buildAdminHeroStat("专题", String.valueOf(countActiveTopics())));
        stats.add(buildAdminHeroStat("分类", String.valueOf(countActiveCategories())));
        return stats;
    }

    /**
     * 构建默认前台首页统计项。
     *
     * @return 默认统计项列表
     */
    private List<SiteHomepageHeroStatResponse> buildDefaultSiteHeroStats() {
        List<SiteHomepageHeroStatResponse> stats = new ArrayList<>();
        stats.add(buildSiteHeroStat("文章", String.valueOf(countPublishedPosts())));
        stats.add(buildSiteHeroStat("专题", String.valueOf(countActiveTopics())));
        stats.add(buildSiteHeroStat("分类", String.valueOf(countActiveCategories())));
        return stats;
    }

    /**
     * 创建后台统计项响应。
     *
     * @param label 标签
     * @param value 数值
     * @return 后台统计项响应
     */
    private AdminHomepageHeroStatResponse buildAdminHeroStat(String label, String value) {
        AdminHomepageHeroStatResponse response = new AdminHomepageHeroStatResponse();
        response.setLabel(label);
        response.setValue(value);
        return response;
    }

    /**
     * 创建前台统计项响应。
     *
     * @param label 标签
     * @param value 数值
     * @return 前台统计项响应
     */
    private SiteHomepageHeroStatResponse buildSiteHeroStat(String label, String value) {
        SiteHomepageHeroStatResponse response = new SiteHomepageHeroStatResponse();
        response.setLabel(label);
        response.setValue(value);
        return response;
    }

    /**
     * 统计已发布文章数量。
     *
     * @return 已发布文章数量
     */
    private long countPublishedPosts() {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getDeleted, 0)
                .eq(PostEntity::getStatus, "PUBLISHED"));
        return count == null ? 0L : count;
    }

    /**
     * 统计有效分类数量。
     *
     * @return 有效分类数量
     */
    private long countActiveCategories() {
        Long count = categoryMapper.selectCount(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getDeleted, 0)
                .eq(CategoryEntity::getStatus, "ACTIVE"));
        return count == null ? 0L : count;
    }

    /**
     * 统计有效专题数量。
     *
     * @return 有效专题数量
     */
    private long countActiveTopics() {
        Long count = topicMapper.selectCount(new LambdaQueryWrapper<TopicEntity>()
                .eq(TopicEntity::getDeleted, 0)
                .eq(TopicEntity::getStatus, "ACTIVE"));
        return count == null ? 0L : count;
    }

    /**
     * 按配置键查询配置实体。
     *
     * @param configKey 配置键
     * @return 配置实体
     */
    private SiteConfigEntity findConfigByKey(String configKey) {
        return siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfigEntity>()
                .eq(SiteConfigEntity::getConfigKey, configKey)
                .last("LIMIT 1"));
    }

    /**
     * 保存配置项。
     *
     * @param configKey 配置键
     * @param configName 配置名称
     * @param configJson 配置 JSON
     * @param remark 配置备注
     */
    private void saveConfig(String configKey, String configName, String configJson, String remark) {
        SiteConfigEntity entity = findConfigByKey(configKey);
        LocalDateTime now = LocalDateTime.now();

        if (entity == null) {
            entity = new SiteConfigEntity();
            entity.setConfigKey(configKey);
            entity.setCreatedTime(now);
        }

        entity.setConfigName(configName);
        entity.setConfigJson(configJson);
        entity.setRemark(remark);
        entity.setUpdatedTime(now);

        if (entity.getId() == null) {
            siteConfigMapper.insert(entity);
            return;
        }

        siteConfigMapper.updateById(entity);
    }

    /**
     * 读取 JSON 节点。
     *
     * @param configJson JSON 字符串
     * @return JSON 节点
     */
    private JsonNode readJsonNode(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return objectMapper.createObjectNode();
        }

        try {
            return objectMapper.readTree(configJson);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "首页配置 JSON 解析失败");
        }
    }

    /**
     * 从 JSON 节点读取文本值。
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 文本值
     */
    private String readJsonText(JsonNode jsonNode, String fieldName, String defaultValue) {
        if (jsonNode == null || jsonNode.get(fieldName) == null || jsonNode.get(fieldName).isNull()) {
            return defaultValue;
        }

        String value = jsonNode.path(fieldName).asText(defaultValue);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * 从 JSON 节点读取长整型值。
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @return 长整型值
     */
    private Long readJsonLong(JsonNode jsonNode, String fieldName) {
        if (jsonNode == null || jsonNode.get(fieldName) == null || jsonNode.get(fieldName).isNull()) {
            return null;
        }

        JsonNode fieldNode = jsonNode.get(fieldName);
        if (fieldNode.isNumber()) {
            long value = fieldNode.asLong();
            return value > 0 ? value : null;
        }

        String value = normalizeText(fieldNode.asText(""));
        if (value.isEmpty()) {
            return null;
        }

        try {
            long numberValue = Long.parseLong(value);
            return numberValue > 0 ? numberValue : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * 从 JSON 节点读取布尔值。
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private boolean readJsonBoolean(JsonNode jsonNode, String fieldName, boolean defaultValue) {
        if (jsonNode == null || jsonNode.get(fieldName) == null || jsonNode.get(fieldName).isNull()) {
            return defaultValue;
        }
        return jsonNode.path(fieldName).asBoolean(defaultValue);
    }

    /**
     * 将 JSON 节点序列化为字符串。
     *
     * @param jsonNode JSON 节点
     * @return JSON 字符串
     */
    private String writeJson(ObjectNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "首页配置 JSON 序列化失败");
        }
    }

    /**
     * 规范化首页布局值。
     *
     * @param value 原始值
     * @return 规范化后的布局值
     */
    private String normalizeHeroLayout(String value) {
        return DEFAULT_HERO_LAYOUT.equals(normalizeText(value)) ? DEFAULT_HERO_LAYOUT : DEFAULT_HERO_LAYOUT;
    }

    /**
     * 规范化背景模式值。
     *
     * @param value 原始值
     * @return 规范化后的背景模式值
     */
    private String normalizeBackgroundMode(String value) {
        String normalizedValue = normalizeText(value);
        return switch (normalizedValue) {
            case "gradient-grid", "soft-glow", "minimal-lines", "keyword-cloud" -> normalizedValue;
            default -> DEFAULT_BACKGROUND_MODE;
        };
    }

    /**
     * 规范化布尔值。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 规范化后的布尔值
     */
    private boolean normalizeBoolean(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 校验首页首屏视觉文章是否存在。
     *
     * @param postId 文章ID
     */
    private void validateHeroVisualPost(Long postId) {
        if (postId == null) {
            return;
        }

        PostEntity postEntity = postMapper.selectOne(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getDeleted, 0)
                .last("LIMIT 1"));

        if (postEntity == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "首屏视觉文章不存在");
        }
    }

    /**
     * 规范化文本值。
     *
     * @param value 原始文本
     * @return 去除前后空白后的文本
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
