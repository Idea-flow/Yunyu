package com.ideaflow.yunyu.module.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.site.dto.AdminSiteConfigUpdateRequest;
import com.ideaflow.yunyu.module.site.entity.SiteConfigEntity;
import com.ideaflow.yunyu.module.site.mapper.SiteConfigMapper;
import com.ideaflow.yunyu.module.site.vo.AdminSiteConfigResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 后台站点配置服务类。
 * 作用：负责聚合站点基础信息、SEO 配置与主题配置的读取和保存，为后台站点设置页面提供统一业务入口。
 */
@Service
public class AdminSiteConfigService {

    private static final String SITE_BASE_KEY = "site.base";
    private static final String SITE_SEO_KEY = "site.seo";
    private static final String SITE_THEME_KEY = "site.theme";

    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建后台站点配置服务。
     *
     * @param siteConfigMapper 站点配置 Mapper
     * @param objectMapper JSON 对象映射器
     */
    public AdminSiteConfigService(SiteConfigMapper siteConfigMapper, ObjectMapper objectMapper) {
        this.siteConfigMapper = siteConfigMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询后台站点配置。
     * 当数据库中尚未存在对应配置时，会自动回退到系统默认值，保证后台页面首次进入也能直接编辑。
     *
     * @return 站点配置响应
     */
    public AdminSiteConfigResponse getSiteConfig() {
        SiteConfigEntity baseConfig = findConfigByKey(SITE_BASE_KEY);
        SiteConfigEntity seoConfig = findConfigByKey(SITE_SEO_KEY);
        SiteConfigEntity themeConfig = findConfigByKey(SITE_THEME_KEY);

        JsonNode baseNode = readJsonNode(baseConfig == null ? null : baseConfig.getConfigJson());
        JsonNode seoNode = readJsonNode(seoConfig == null ? null : seoConfig.getConfigJson());
        JsonNode themeNode = readJsonNode(themeConfig == null ? null : themeConfig.getConfigJson());

        AdminSiteConfigResponse response = new AdminSiteConfigResponse();
        response.setSiteName(readJsonText(baseNode, "siteName", "云屿"));
        response.setSiteSubTitle(readJsonText(baseNode, "siteSubTitle", "在二次元场景与情绪里漫游的内容站"));
        response.setFooterText(readJsonText(baseNode, "footerText", "云屿 Yunyu"));
        response.setLogoUrl(readJsonText(baseNode, "logoUrl", ""));
        response.setFaviconUrl(readJsonText(baseNode, "faviconUrl", ""));
        response.setDefaultTitle(readJsonText(seoNode, "defaultTitle", response.getSiteName()));
        response.setDefaultDescription(readJsonText(seoNode, "defaultDescription", response.getSiteSubTitle()));
        response.setPrimaryColor(readJsonText(themeNode, "primaryColor", "#38BDF8"));
        response.setSecondaryColor(readJsonText(themeNode, "secondaryColor", "#FB923C"));
        return response;
    }

    /**
     * 更新后台站点配置。
     * 会将一个后台表单拆分写入 `site.base`、`site.seo` 与 `site.theme` 三类配置项。
     *
     * @param request 站点配置更新请求
     * @return 更新后的站点配置
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminSiteConfigResponse updateSiteConfig(AdminSiteConfigUpdateRequest request) {
        saveConfig(SITE_BASE_KEY, "站点基础配置", buildBaseConfigJson(request), "站点名称、副标题、页脚与图标等基础展示信息");
        saveConfig(SITE_SEO_KEY, "站点 SEO 配置", buildSeoConfigJson(request), "站点默认标题、描述与分享图配置");
        saveConfig(SITE_THEME_KEY, "站点主题配置", buildThemeConfigJson(request), "站点主题色与首页风格配置");
        return getSiteConfig();
    }

    /**
     * 构建基础配置 JSON。
     *
     * @param request 更新请求
     * @return 基础配置 JSON 字符串
     */
    private String buildBaseConfigJson(AdminSiteConfigUpdateRequest request) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("siteName", normalizeText(request.getSiteName()));
        jsonNode.put("siteSubTitle", normalizeText(request.getSiteSubTitle()));
        jsonNode.put("footerText", normalizeText(request.getFooterText()));
        jsonNode.put("logoUrl", normalizeText(request.getLogoUrl()));
        jsonNode.put("faviconUrl", normalizeText(request.getFaviconUrl()));
        return writeJson(jsonNode);
    }

    /**
     * 构建 SEO 配置 JSON。
     *
     * @param request 更新请求
     * @return SEO 配置 JSON 字符串
     */
    private String buildSeoConfigJson(AdminSiteConfigUpdateRequest request) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("defaultTitle", normalizeText(request.getDefaultTitle()));
        jsonNode.put("defaultDescription", normalizeText(request.getDefaultDescription()));
        return writeJson(jsonNode);
    }

    /**
     * 构建主题配置 JSON。
     *
     * @param request 更新请求
     * @return 主题配置 JSON 字符串
     */
    private String buildThemeConfigJson(AdminSiteConfigUpdateRequest request) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("primaryColor", normalizeText(request.getPrimaryColor()));
        jsonNode.put("secondaryColor", normalizeText(request.getSecondaryColor()));
        return writeJson(jsonNode);
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
     * @param remark 备注
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
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "站点配置 JSON 解析失败");
        }
    }

    /**
     * 从 JSON 节点中读取文本值。
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
     * 将 JSON 节点序列化为字符串。
     *
     * @param jsonNode JSON 节点
     * @return JSON 字符串
     */
    private String writeJson(ObjectNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "站点配置 JSON 序列化失败");
        }
    }

    /**
     * 规范化文本值。
     *
     * @param value 原始值
     * @return 去除前后空白后的文本
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
