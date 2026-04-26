package com.ideaflow.yunyu.module.ai.service.config;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.adapter.UpstreamProtocolAdapter;
import com.ideaflow.yunyu.module.ai.adapter.UpstreamProtocolAdapterRegistry;
import com.ideaflow.yunyu.module.ai.dto.AdminSiteAiProviderConfigUpdateRequest;
import com.ideaflow.yunyu.module.ai.dto.AdminSiteAiProviderProfileRequest;
import com.ideaflow.yunyu.module.ai.model.AiProviderConfig;
import com.ideaflow.yunyu.module.ai.model.AiProviderProfile;
import com.ideaflow.yunyu.module.ai.model.UpstreamProtocolType;
import com.ideaflow.yunyu.module.ai.vo.AdminSiteAiProviderConfigResponse;
import com.ideaflow.yunyu.module.ai.vo.AdminSiteAiProviderConnectionTestResponse;
import com.ideaflow.yunyu.module.ai.vo.AdminSiteAiProviderProfileResponse;
import com.ideaflow.yunyu.module.site.service.SiteConfigService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 提供商配置服务类。
 * 作用：统一维护 AI 提供商多配置、单启用校验、连接测试与配置读取能力。
 */
@Service
public class AiProviderConfigService {

    private static final String AI_PROVIDER_CONFIG_KEY = "ai.provider.profiles";
    private static final String AI_PROVIDER_CONFIG_NAME = "AI 提供商配置";

    private final SiteConfigService siteConfigService;
    private final ObjectMapper objectMapper;
    private final UpstreamProtocolAdapterRegistry adapterRegistry;

    /**
     * 创建 AI 提供商配置服务。
     *
     * @param siteConfigService 站点配置统一服务
     * @param objectMapper JSON 对象映射器
     * @param adapterRegistry 协议适配器注册表
     */
    public AiProviderConfigService(SiteConfigService siteConfigService,
                                   ObjectMapper objectMapper,
                                   UpstreamProtocolAdapterRegistry adapterRegistry) {
        this.siteConfigService = siteConfigService;
        this.objectMapper = objectMapper;
        this.adapterRegistry = adapterRegistry;
    }

    /**
     * 查询后台 AI 配置。
     *
     * @return 后台 AI 配置响应
     */
    public AdminSiteAiProviderConfigResponse getAdminConfig() {
        AiProviderConfig config = readConfigModel();
        AdminSiteAiProviderConfigResponse response = new AdminSiteAiProviderConfigResponse();
        response.setActiveProfileKey(config.getActiveProfileKey());

        List<AdminSiteAiProviderProfileResponse> profileResponses = new ArrayList<>();
        for (AiProviderProfile profile : config.getProfiles()) {
            AdminSiteAiProviderProfileResponse profileResponse = new AdminSiteAiProviderProfileResponse();
            profileResponse.setProfileKey(profile.getProfileKey());
            profileResponse.setName(profile.getName());
            profileResponse.setEnabled(Boolean.TRUE.equals(profile.getEnabled()));
            profileResponse.setUpstreamBaseUrl(profile.getUpstreamBaseUrl());
            profileResponse.setApiKey(profile.getApiKey());
            profileResponse.setModel(profile.getModel());
            profileResponse.setUpstreamProtocol(profile.getUpstreamProtocol());
            profileResponse.setConnectTimeoutMs(profile.getConnectTimeoutMs());
            profileResponse.setReadTimeoutMs(profile.getReadTimeoutMs());
            profileResponse.setWriteTimeoutMs(profile.getWriteTimeoutMs());
            profileResponse.setMaxTokens(profile.getMaxTokens());
            profileResponse.setTemperature(profile.getTemperature());
            profileResponses.add(profileResponse);
        }
        response.setProfiles(profileResponses);
        return response;
    }

    /**
     * 更新后台 AI 配置。
     *
     * @param request 更新请求
     * @return 更新后的后台配置
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminSiteAiProviderConfigResponse updateAdminConfig(AdminSiteAiProviderConfigUpdateRequest request) {
        AiProviderConfig config = normalizeAndValidate(request);
        siteConfigService.saveConfig(
                AI_PROVIDER_CONFIG_KEY,
                AI_PROVIDER_CONFIG_NAME,
                writeConfigJson(config),
                "AI 上游多配置与单启用管理"
        );
        return getAdminConfig();
    }

    /**
     * 测试单个 AI 提供商配置连接。
     *
     * @param request 配置项请求
     * @return 连接测试结果
     */
    public AdminSiteAiProviderConnectionTestResponse testConnection(AdminSiteAiProviderProfileRequest request) {
        AiProviderProfile profile;
        try {
            profile = normalizeProfile(request, new HashMap<>());
        } catch (BizException exception) {
            return new AdminSiteAiProviderConnectionTestResponse(Boolean.FALSE, exception.getMessage());
        }

        UpstreamProtocolType protocolType = UpstreamProtocolType.fromText(profile.getUpstreamProtocol());
        UpstreamProtocolAdapter adapter = adapterRegistry.getAdapter(protocolType);
        String endpointPath = protocolType == UpstreamProtocolType.COMPLETIONS ? "/v1/chat/completions" : "/v1/responses";

        try {
            adapter.invoke(profile, endpointPath, buildPingRequest(profile, protocolType));
            return new AdminSiteAiProviderConnectionTestResponse(Boolean.TRUE, "连接测试成功");
        } catch (BizException exception) {
            return new AdminSiteAiProviderConnectionTestResponse(Boolean.FALSE, exception.getMessage());
        } catch (Exception exception) {
            return new AdminSiteAiProviderConnectionTestResponse(Boolean.FALSE, "连接测试失败：" + exception.getMessage());
        }
    }

    /**
     * 获取当前启用配置。
     *
     * @return 当前启用配置
     */
    public AiProviderProfile getActiveProfileOrThrow() {
        AiProviderConfig config = readConfigModel();
        for (AiProviderProfile profile : config.getProfiles()) {
            if (profile.getProfileKey().equals(config.getActiveProfileKey())) {
                return profile;
            }
        }
        throw new BizException(ResultCode.BAD_REQUEST, "当前未找到可用的 AI 启用配置");
    }

    /**
     * 读取配置模型。
     *
     * @return 配置模型
     */
    private AiProviderConfig readConfigModel() {
        JsonNode rootNode = siteConfigService.getConfigJsonNodeByKey(AI_PROVIDER_CONFIG_KEY);
        AiProviderConfig config = new AiProviderConfig();
        config.setActiveProfileKey(readText(rootNode, "activeProfileKey", ""));

        List<AiProviderProfile> profiles = new ArrayList<>();
        JsonNode profilesNode = rootNode.path("profiles");
        if (profilesNode.isArray()) {
            for (JsonNode profileNode : profilesNode) {
                AiProviderProfile profile = new AiProviderProfile();
                profile.setProfileKey(readText(profileNode, "profileKey", ""));
                profile.setName(readText(profileNode, "name", ""));
                profile.setEnabled(profileNode.path("enabled").asBoolean(false));
                profile.setUpstreamBaseUrl(readText(profileNode, "upstreamBaseUrl", ""));
                profile.setApiKey(readText(profileNode, "apiKey", ""));
                profile.setModel(readText(profileNode, "model", ""));
                profile.setUpstreamProtocol(readText(profileNode, "upstreamProtocol", "COMPLETIONS"));
                profile.setConnectTimeoutMs(profileNode.path("connectTimeoutMs").asInt(3000));
                profile.setReadTimeoutMs(profileNode.path("readTimeoutMs").asInt(15000));
                profile.setWriteTimeoutMs(profileNode.path("writeTimeoutMs").asInt(15000));
                profile.setMaxTokens(profileNode.path("maxTokens").asInt(800));
                profile.setTemperature(profileNode.path("temperature").isMissingNode()
                        ? 0.4
                        : profileNode.path("temperature").asDouble(0.4));
                profiles.add(profile);
            }
        }

        if (config.getActiveProfileKey().isBlank()) {
            for (AiProviderProfile profile : profiles) {
                if (Boolean.TRUE.equals(profile.getEnabled())) {
                    config.setActiveProfileKey(profile.getProfileKey());
                    break;
                }
            }
        }

        config.setProfiles(profiles);
        return config;
    }

    /**
     * 构建测试请求体。
     *
     * @param profile 配置项
     * @param protocolType 协议类型
     * @return 测试请求 JSON
     */
    private JsonNode buildPingRequest(AiProviderProfile profile, UpstreamProtocolType protocolType) {
        if (protocolType == UpstreamProtocolType.COMPLETIONS) {
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("model", profile.getModel());
            rootNode.put("stream", false);
            rootNode.put("max_tokens", 1);
            ArrayNode messagesNode = rootNode.putArray("messages");
            ObjectNode messageNode = messagesNode.addObject();
            messageNode.put("role", "user");
            messageNode.put("content", "ping");
            return rootNode;
        }

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("model", profile.getModel());
        rootNode.put("stream", false);
        rootNode.put("max_output_tokens", 1);
        rootNode.put("input", "ping");
        return rootNode;
    }

    /**
     * 规范化并校验更新请求。
     *
     * @param request 更新请求
     * @return 规范化后的配置模型
     */
    private AiProviderConfig normalizeAndValidate(AdminSiteAiProviderConfigUpdateRequest request) {
        if (request == null || request.getProfiles() == null || request.getProfiles().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "至少需要配置一个 AI 提供商");
        }

        Map<String, String> previousApiKeyMap = new HashMap<>();
        AiProviderConfig previousConfig = readConfigModel();
        for (AiProviderProfile profile : previousConfig.getProfiles()) {
            previousApiKeyMap.put(profile.getProfileKey(), profile.getApiKey());
        }

        List<AiProviderProfile> profiles = new ArrayList<>();
        Set<String> profileKeys = new HashSet<>();
        String enabledProfileKey = "";
        int enabledCount = 0;

        for (AdminSiteAiProviderProfileRequest profileRequest : request.getProfiles()) {
            AiProviderProfile profile = normalizeProfile(profileRequest, previousApiKeyMap);
            if (!profileKeys.add(profile.getProfileKey())) {
                throw new BizException(ResultCode.BAD_REQUEST, "AI 配置键不能重复：" + profile.getProfileKey());
            }

            if (Boolean.TRUE.equals(profile.getEnabled())) {
                enabledCount++;
                enabledProfileKey = profile.getProfileKey();
            }
            profiles.add(profile);
        }

        if (enabledCount != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "AI 提供商配置必须且只能启用一个");
        }

        String activeProfileKey = normalizeText(request.getActiveProfileKey());
        if (activeProfileKey.isBlank()) {
            activeProfileKey = enabledProfileKey;
        }
        if (!activeProfileKey.equals(enabledProfileKey)) {
            throw new BizException(ResultCode.BAD_REQUEST, "activeProfileKey 必须与启用配置一致");
        }

        AiProviderConfig config = new AiProviderConfig();
        config.setActiveProfileKey(activeProfileKey);
        config.setProfiles(profiles);
        return config;
    }

    /**
     * 规范化配置项。
     *
     * @param request 配置项请求
     * @param previousApiKeyMap 历史 API Key 映射
     * @return 规范化后的配置项
     */
    private AiProviderProfile normalizeProfile(AdminSiteAiProviderProfileRequest request,
                                               Map<String, String> previousApiKeyMap) {
        if (request == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "AI 配置项不能为空");
        }

        AiProviderProfile profile = new AiProviderProfile();
        profile.setProfileKey(requireText(request.getProfileKey(), "AI 配置键不能为空"));
        profile.setName(requireText(request.getName(), "AI 配置名称不能为空"));
        profile.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        profile.setUpstreamBaseUrl(requireText(request.getUpstreamBaseUrl(), "上游服务地址不能为空"));
        profile.setApiKey(resolveApiKey(request.getApiKey(), profile.getProfileKey(), previousApiKeyMap));
        profile.setModel(requireText(request.getModel(), "模型名称不能为空"));
        profile.setUpstreamProtocol(UpstreamProtocolType.fromText(request.getUpstreamProtocol()).name());

        int connectTimeoutMs = request.getConnectTimeoutMs() == null ? 3000 : request.getConnectTimeoutMs();
        int readTimeoutMs = request.getReadTimeoutMs() == null ? 15000 : request.getReadTimeoutMs();
        int writeTimeoutMs = request.getWriteTimeoutMs() == null ? 15000 : request.getWriteTimeoutMs();
        int maxTokens = request.getMaxTokens() == null ? 800 : request.getMaxTokens();
        double temperature = request.getTemperature() == null ? 0.4 : request.getTemperature();

        if (connectTimeoutMs < 100 || connectTimeoutMs > 120000) {
            throw new BizException(ResultCode.BAD_REQUEST, "connectTimeoutMs 需在 100-120000 之间");
        }
        if (readTimeoutMs < 100 || readTimeoutMs > 120000) {
            throw new BizException(ResultCode.BAD_REQUEST, "readTimeoutMs 需在 100-120000 之间");
        }
        if (writeTimeoutMs < 100 || writeTimeoutMs > 120000) {
            throw new BizException(ResultCode.BAD_REQUEST, "writeTimeoutMs 需在 100-120000 之间");
        }
        if (maxTokens < 1 || maxTokens > 128000) {
            throw new BizException(ResultCode.BAD_REQUEST, "maxTokens 需在 1-128000 之间");
        }
        if (temperature < 0 || temperature > 2) {
            throw new BizException(ResultCode.BAD_REQUEST, "temperature 需在 0-2 之间");
        }

        profile.setConnectTimeoutMs(connectTimeoutMs);
        profile.setReadTimeoutMs(readTimeoutMs);
        profile.setWriteTimeoutMs(writeTimeoutMs);
        profile.setMaxTokens(maxTokens);
        profile.setTemperature(temperature);
        return profile;
    }

    /**
     * 解析最终应保存的 API Key。
     * 作用：当前直接使用页面提交原值，不再依赖后端脱敏回填兼容逻辑。
     *
     * @param apiKeyInput 页面提交的 API Key 文本
     * @param profileKey 配置键
     * @param previousApiKeyMap 历史 API Key 映射
     * @return 最终 API Key
     */
    private String resolveApiKey(String apiKeyInput, String profileKey, Map<String, String> previousApiKeyMap) {
        return requireText(apiKeyInput, "API Key 不能为空");
    }

    /**
     * 写入配置 JSON。
     *
     * @param config 配置模型
     * @return 配置 JSON
     */
    private String writeConfigJson(AiProviderConfig config) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("activeProfileKey", config.getActiveProfileKey());

        ArrayNode profilesNode = objectMapper.createArrayNode();
        for (AiProviderProfile profile : config.getProfiles()) {
            ObjectNode profileNode = objectMapper.createObjectNode();
            profileNode.put("profileKey", profile.getProfileKey());
            profileNode.put("name", profile.getName());
            profileNode.put("enabled", Boolean.TRUE.equals(profile.getEnabled()));
            profileNode.put("upstreamBaseUrl", profile.getUpstreamBaseUrl());
            profileNode.put("apiKey", profile.getApiKey());
            profileNode.put("model", profile.getModel());
            profileNode.put("upstreamProtocol", profile.getUpstreamProtocol());
            profileNode.put("connectTimeoutMs", profile.getConnectTimeoutMs());
            profileNode.put("readTimeoutMs", profile.getReadTimeoutMs());
            profileNode.put("writeTimeoutMs", profile.getWriteTimeoutMs());
            profileNode.put("maxTokens", profile.getMaxTokens());
            profileNode.put("temperature", profile.getTemperature());
            profilesNode.add(profileNode);
        }

        rootNode.set("profiles", profilesNode);
        return siteConfigService.writeJson(rootNode);
    }

    /**
     * 读取文本字段。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 文本值
     */
    private String readText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || node.get(fieldName) == null || node.get(fieldName).isNull()) {
            return defaultValue;
        }

        String value = node.path(fieldName).asText(defaultValue);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 校验并读取必填文本。
     *
     * @param value 原始值
     * @param errorMessage 错误提示
     * @return 标准化文本
     */
    private String requireText(String value, String errorMessage) {
        String normalizedValue = normalizeText(value);
        if (normalizedValue.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, errorMessage);
        }
        return normalizedValue;
    }

    /**
     * 标准化文本。
     *
     * @param value 原始值
     * @return 标准化后的文本
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
