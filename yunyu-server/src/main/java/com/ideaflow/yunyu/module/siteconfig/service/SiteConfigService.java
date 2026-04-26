package com.ideaflow.yunyu.module.siteconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.siteconfig.entity.SiteConfigEntity;
import com.ideaflow.yunyu.module.siteconfig.mapper.SiteConfigMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站点配置统一服务类。
 * 作用：统一封装 site_config 的读写能力，并提供内存缓存，供前后台与附件模块复用配置读取逻辑。
 */
@Service
public class SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;
    private final ObjectMapper objectMapper;
    private final Map<String, SiteConfigEntity> configCache = new ConcurrentHashMap<>();
    private final Object loadLock = new Object();
    private volatile boolean cacheLoaded;

    /**
     * 创建站点配置统一服务。
     *
     * @param siteConfigMapper 站点配置 Mapper
     * @param objectMapper JSON 对象映射器
     */
    public SiteConfigService(SiteConfigMapper siteConfigMapper, ObjectMapper objectMapper) {
        this.siteConfigMapper = siteConfigMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询全部配置。
     *
     * @return 全部配置列表
     */
    public List<SiteConfigEntity> getAllConfigs() {
        ensureCacheLoaded();
        return new ArrayList<>(configCache.values());
    }

    /**
     * 按配置键查询配置实体。
     *
     * @param configKey 配置键
     * @return 配置实体，不存在时返回 null
     */
    public SiteConfigEntity getConfigByKey(String configKey) {
        if (configKey == null || configKey.isBlank()) {
            return null;
        }
        ensureCacheLoaded();
        return configCache.get(configKey.trim());
    }

    /**
     * 按配置键读取 JSON 节点。
     *
     * @param configKey 配置键
     * @return JSON 节点，不存在时返回空对象节点
     */
    public JsonNode getConfigJsonNodeByKey(String configKey) {
        SiteConfigEntity entity = getConfigByKey(configKey);
        return readJsonNode(entity == null ? null : entity.getConfigJson());
    }

    /**
     * 按配置键读取原始 JSON 文本。
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置 JSON 文本
     */
    public String getConfigJsonTextByKey(String configKey, String defaultValue) {
        SiteConfigEntity entity = getConfigByKey(configKey);
        if (entity == null || entity.getConfigJson() == null || entity.getConfigJson().isBlank()) {
            return defaultValue;
        }
        return entity.getConfigJson();
    }

    /**
     * 获取当前 JSON 对象映射器。
     * 作用：给需要做复杂对象映射的业务服务提供统一的 Jackson 实例，避免各处重复构造。
     *
     * @return Jackson 对象映射器
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 保存配置项。
     *
     * @param configKey 配置键
     * @param configName 配置名称
     * @param configJson 配置 JSON
     * @param remark 备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(String configKey, String configName, String configJson, String remark) {
        String normalizedKey = normalizeRequiredText(configKey, "配置键不能为空");
        String normalizedName = normalizeRequiredText(configName, "配置名称不能为空");
        String normalizedJson = normalizeJson(configJson);

        SiteConfigEntity entity = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfigEntity>()
                .eq(SiteConfigEntity::getConfigKey, normalizedKey)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();

        if (entity == null) {
            entity = new SiteConfigEntity();
            entity.setConfigKey(normalizedKey);
            entity.setCreatedTime(now);
        }

        entity.setConfigName(normalizedName);
        entity.setConfigJson(normalizedJson);
        entity.setRemark(normalizeNullableText(remark));
        entity.setUpdatedTime(now);

        if (entity.getId() == null) {
            siteConfigMapper.insert(entity);
        } else {
            siteConfigMapper.updateById(entity);
        }

        configCache.put(normalizedKey, entity);
        cacheLoaded = true;
    }

    /**
     * 删除配置项。
     *
     * @param configKey 配置键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String configKey) {
        String normalizedKey = normalizeRequiredText(configKey, "配置键不能为空");
        SiteConfigEntity entity = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfigEntity>()
                .eq(SiteConfigEntity::getConfigKey, normalizedKey)
                .last("LIMIT 1"));

        if (entity == null) {
            return;
        }

        siteConfigMapper.deleteById(entity.getId());
        configCache.remove(normalizedKey);
        cacheLoaded = true;
    }

    /**
     * 主动清空缓存并重新加载。
     */
    public void refreshCache() {
        synchronized (loadLock) {
            configCache.clear();
            cacheLoaded = false;
        }
        ensureCacheLoaded();
    }

    /**
     * 读取 JSON 文本为节点。
     *
     * @param configJson JSON 文本
     * @return JSON 节点
     */
    public JsonNode readJsonNode(String configJson) {
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
     * 序列化 JSON 节点。
     *
     * @param jsonNode JSON 节点
     * @return JSON 字符串
     */
    public String writeJson(ObjectNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "站点配置 JSON 序列化失败");
        }
    }

    /**
     * 规范化可空文本。
     *
     * @param value 原始值
     * @return 规范化后的文本
     */
    public String normalizeNullableText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 保证缓存已加载。
     */
    private void ensureCacheLoaded() {
        if (cacheLoaded) {
            return;
        }

        synchronized (loadLock) {
            if (cacheLoaded) {
                return;
            }

            List<SiteConfigEntity> entities = siteConfigMapper.selectList(new LambdaQueryWrapper<SiteConfigEntity>()
                    .orderByAsc(SiteConfigEntity::getId));
            configCache.clear();
            for (SiteConfigEntity entity : entities) {
                configCache.put(entity.getConfigKey(), entity);
            }
            cacheLoaded = true;
        }
    }

    /**
     * 校验并规范化必填文本。
     *
     * @param value 原始文本
     * @param errorMessage 错误提示
     * @return 规范化后的文本
     */
    private String normalizeRequiredText(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, errorMessage);
        }
        return value.trim();
    }

    /**
     * 校验并规范化 JSON 文本。
     *
     * @param jsonText JSON 文本
     * @return 规范化后的 JSON 文本
     */
    private String normalizeJson(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "配置内容不能为空");
        }

        JsonNode jsonNode = readJsonNode(jsonText);
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "配置内容序列化失败");
        }
    }
}
