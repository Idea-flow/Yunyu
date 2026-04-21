package com.ideaflow.yunyu.module.site.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.site.dto.AdminSiteStorageS3ConfigUpdateRequest;
import com.ideaflow.yunyu.module.site.dto.AdminSiteStorageS3ProfileRequest;
import com.ideaflow.yunyu.module.site.model.SiteStorageS3Config;
import com.ideaflow.yunyu.module.site.model.SiteStorageS3Profile;
import com.ideaflow.yunyu.module.site.vo.AdminSiteStorageS3ConnectionTestResponse;
import com.ideaflow.yunyu.module.site.vo.AdminSiteStorageS3ConfigResponse;
import com.ideaflow.yunyu.module.site.vo.AdminSiteStorageS3ProfileResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/**
 * 站点 S3 配置服务类。
 * 作用：统一维护后台 S3 多配置的读取、保存和单启用校验，并为附件模块提供启用配置查询能力。
 */
@Service
public class SiteStorageS3ConfigService {

    private static final String STORAGE_S3_CONFIG_KEY = "storage.s3.profiles";
    private static final String STORAGE_S3_CONFIG_NAME = "站点 S3 存储配置";

    private final SiteConfigService siteConfigService;
    private final ObjectMapper objectMapper;

    /**
     * 创建站点 S3 配置服务。
     *
     * @param siteConfigService 站点配置统一服务
     * @param objectMapper JSON 对象映射器
     */
    public SiteStorageS3ConfigService(SiteConfigService siteConfigService,
                                      ObjectMapper objectMapper) {
        this.siteConfigService = siteConfigService;
        this.objectMapper = objectMapper;
    }

    /**
     * 查询后台 S3 配置。
     *
     * @return S3 配置响应
     */
    public AdminSiteStorageS3ConfigResponse getAdminS3Config() {
        SiteStorageS3Config model = readConfigModel();
        return toResponse(model);
    }

    /**
     * 更新后台 S3 配置。
     *
     * @param request 更新请求
     * @return 更新后的 S3 配置响应
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminSiteStorageS3ConfigResponse updateAdminS3Config(AdminSiteStorageS3ConfigUpdateRequest request) {
        SiteStorageS3Config model = normalizeAndValidate(request);
        String configJson = writeConfigJson(model);
        siteConfigService.saveConfig(
                STORAGE_S3_CONFIG_KEY,
                STORAGE_S3_CONFIG_NAME,
                configJson,
                "附件上传所使用的 S3 多环境配置，要求单启用"
        );
        return getAdminS3Config();
    }

    /**
     * 获取当前启用 S3 配置。
     *
     * @return 当前启用配置
     */
    public SiteStorageS3Profile getActiveProfileOrThrow() {
        SiteStorageS3Config model = readConfigModel();
        if (model.getProfiles().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "尚未配置可用的 S3 存储，请先在站点设置中配置并启用");
        }

        for (SiteStorageS3Profile profile : model.getProfiles()) {
            if (profile.getProfileKey().equals(model.getActiveProfileKey())) {
                return profile;
            }
        }

        throw new BizException(ResultCode.BAD_REQUEST, "当前启用的 S3 配置不存在，请先修正站点配置");
    }

    /**
     * 按配置键获取 S3 配置。
     *
     * @param profileKey 配置键
     * @return 匹配到的配置
     */
    public SiteStorageS3Profile getProfileByKeyOrThrow(String profileKey) {
        if (profileKey == null || profileKey.isBlank()) {
            throw new BizException(ResultCode.BAD_REQUEST, "S3 配置键不能为空");
        }

        SiteStorageS3Config model = readConfigModel();
        for (SiteStorageS3Profile profile : model.getProfiles()) {
            if (profile.getProfileKey().equals(profileKey.trim())) {
                return profile;
            }
        }

        throw new BizException(ResultCode.NOT_FOUND, "找不到指定的 S3 配置，请先检查站点设置");
    }

    /**
     * 测试 S3 配置连接是否可用。
     *
     * @param request 待测试的 S3 配置项
     * @return 连接测试结果
     */
    public AdminSiteStorageS3ConnectionTestResponse testConnection(AdminSiteStorageS3ProfileRequest request) {
        SiteStorageS3Profile profile = normalizeProfile(request);
        HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                .bucket(profile.getBucket())
                .build();

        try (S3Client s3Client = createS3Client(profile)) {
            s3Client.headBucket(headBucketRequest);
            return new AdminSiteStorageS3ConnectionTestResponse(Boolean.TRUE, "连接成功，可访问目标 Bucket");
        } catch (SdkException exception) {
            String message = normalizeText(exception.getMessage());
            if (message.isEmpty()) {
                message = "连接失败，请检查 endpoint、bucket、accessKey 和 secretKey";
            }
            return new AdminSiteStorageS3ConnectionTestResponse(Boolean.FALSE, message);
        }
    }

    /**
     * 读取并解析配置模型。
     *
     * @return 配置模型
     */
    private SiteStorageS3Config readConfigModel() {
        JsonNode rootNode = siteConfigService.getConfigJsonNodeByKey(STORAGE_S3_CONFIG_KEY);
        SiteStorageS3Config model = new SiteStorageS3Config();
        model.setActiveProfileKey(readText(rootNode, "activeProfileKey", ""));

        List<SiteStorageS3Profile> profiles = new ArrayList<>();
        JsonNode profilesNode = rootNode.path("profiles");
        if (profilesNode.isArray()) {
            for (JsonNode profileNode : profilesNode) {
                SiteStorageS3Profile profile = new SiteStorageS3Profile();
                profile.setProfileKey(readText(profileNode, "profileKey", ""));
                profile.setName(readText(profileNode, "name", ""));
                profile.setEnabled(profileNode.path("enabled").asBoolean(false));
                profile.setEndpoint(readText(profileNode, "endpoint", ""));
                profile.setRegion(readText(profileNode, "region", "auto"));
                profile.setBucket(readText(profileNode, "bucket", ""));
                profile.setAccessKey(readText(profileNode, "accessKey", ""));
                profile.setSecretKey(readText(profileNode, "secretKey", ""));
                profile.setPathStyleAccess(profileNode.path("pathStyleAccess").asBoolean(false));
                profile.setPublicBaseUrl(readText(profileNode, "publicBaseUrl", ""));
                profile.setPresignExpireSeconds(profileNode.path("presignExpireSeconds").asInt(300));
                profile.setMaxFileSizeMb(profileNode.path("maxFileSizeMb").asInt(20));
                profile.setAllowedContentTypes(readAllowedContentTypes(profileNode.path("allowedContentTypes")));
                profiles.add(profile);
            }
        }

        model.setProfiles(profiles);
        if (model.getActiveProfileKey().isBlank()) {
            for (SiteStorageS3Profile profile : profiles) {
                if (Boolean.TRUE.equals(profile.getEnabled())) {
                    model.setActiveProfileKey(profile.getProfileKey());
                    break;
                }
            }
        }

        return model;
    }

    /**
     * 校验并规范化更新请求。
     *
     * @param request 更新请求
     * @return 规范化后的配置模型
     */
    private SiteStorageS3Config normalizeAndValidate(AdminSiteStorageS3ConfigUpdateRequest request) {
        if (request == null || request.getProfiles() == null || request.getProfiles().isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "至少需要配置一个 S3 存储项");
        }

        SiteStorageS3Config model = new SiteStorageS3Config();
        List<SiteStorageS3Profile> normalizedProfiles = new ArrayList<>();
        Set<String> profileKeys = new HashSet<>();
        int enabledCount = 0;
        String enabledProfileKey = "";

        for (AdminSiteStorageS3ProfileRequest item : request.getProfiles()) {
            SiteStorageS3Profile profile = normalizeProfile(item);
            if (!profileKeys.add(profile.getProfileKey())) {
                throw new BizException(ResultCode.BAD_REQUEST, "S3 配置键不能重复：" + profile.getProfileKey());
            }

            if (Boolean.TRUE.equals(profile.getEnabled())) {
                enabledCount++;
                enabledProfileKey = profile.getProfileKey();
            }
            normalizedProfiles.add(profile);
        }

        if (enabledCount != 1) {
            throw new BizException(ResultCode.BAD_REQUEST, "S3 配置必须且只能启用一个");
        }

        String activeProfileKey = normalizeText(request.getActiveProfileKey());
        if (activeProfileKey.isBlank()) {
            activeProfileKey = enabledProfileKey;
        }
        if (!activeProfileKey.equals(enabledProfileKey)) {
            throw new BizException(ResultCode.BAD_REQUEST, "activeProfileKey 必须与启用的配置项一致");
        }

        model.setActiveProfileKey(activeProfileKey);
        model.setProfiles(normalizedProfiles);
        return model;
    }

    /**
     * 规范化单个配置项。
     *
     * @param item 原始配置项
     * @return 规范化后的配置项
     */
    private SiteStorageS3Profile normalizeProfile(AdminSiteStorageS3ProfileRequest item) {
        if (item == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "S3 配置项不能为空");
        }

        SiteStorageS3Profile profile = new SiteStorageS3Profile();
        profile.setProfileKey(requireText(item.getProfileKey(), "S3 配置键不能为空"));
        profile.setName(requireText(item.getName(), "S3 配置名称不能为空"));
        profile.setEnabled(Boolean.TRUE.equals(item.getEnabled()));
        profile.setEndpoint(requireText(item.getEndpoint(), "S3 Endpoint 不能为空"));
        profile.setRegion(defaultText(item.getRegion(), "auto"));
        profile.setBucket(requireText(item.getBucket(), "S3 Bucket 不能为空"));
        profile.setAccessKey(requireText(item.getAccessKey(), "S3 AccessKey 不能为空"));
        profile.setSecretKey(requireText(item.getSecretKey(), "S3 SecretKey 不能为空"));
        profile.setPathStyleAccess(Boolean.TRUE.equals(item.getPathStyleAccess()));
        profile.setPublicBaseUrl(normalizeText(item.getPublicBaseUrl()));

        int presignExpireSeconds = item.getPresignExpireSeconds() == null ? 300 : item.getPresignExpireSeconds();
        if (presignExpireSeconds < 60 || presignExpireSeconds > 3600) {
            throw new BizException(ResultCode.BAD_REQUEST, "presignExpireSeconds 需在 60-3600 秒之间");
        }
        profile.setPresignExpireSeconds(presignExpireSeconds);

        int maxFileSizeMb = item.getMaxFileSizeMb() == null ? 20 : item.getMaxFileSizeMb();
        if (maxFileSizeMb < 1 || maxFileSizeMb > 2048) {
            throw new BizException(ResultCode.BAD_REQUEST, "maxFileSizeMb 需在 1-2048 之间");
        }
        profile.setMaxFileSizeMb(maxFileSizeMb);

        List<String> allowedContentTypes = new ArrayList<>();
        if (item.getAllowedContentTypes() != null) {
            for (String contentType : item.getAllowedContentTypes()) {
                String normalizedContentType = normalizeText(contentType).toLowerCase(Locale.ROOT);
                if (!normalizedContentType.isEmpty()) {
                    allowedContentTypes.add(normalizedContentType);
                }
            }
        }
        if (allowedContentTypes.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "allowedContentTypes 至少配置一个 MIME 类型");
        }
        profile.setAllowedContentTypes(allowedContentTypes.stream().distinct().toList());
        return profile;
    }

    /**
     * 将配置模型写入 JSON。
     *
     * @param model 配置模型
     * @return JSON 字符串
     */
    private String writeConfigJson(SiteStorageS3Config model) {
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("activeProfileKey", model.getActiveProfileKey());

        ArrayNode profilesNode = objectMapper.createArrayNode();
        for (SiteStorageS3Profile profile : model.getProfiles()) {
            ObjectNode profileNode = objectMapper.createObjectNode();
            profileNode.put("profileKey", profile.getProfileKey());
            profileNode.put("name", profile.getName());
            profileNode.put("enabled", Boolean.TRUE.equals(profile.getEnabled()));
            profileNode.put("endpoint", profile.getEndpoint());
            profileNode.put("region", profile.getRegion());
            profileNode.put("bucket", profile.getBucket());
            profileNode.put("accessKey", profile.getAccessKey());
            profileNode.put("secretKey", profile.getSecretKey());
            profileNode.put("pathStyleAccess", Boolean.TRUE.equals(profile.getPathStyleAccess()));
            profileNode.put("publicBaseUrl", profile.getPublicBaseUrl());
            profileNode.put("presignExpireSeconds", profile.getPresignExpireSeconds());
            profileNode.put("maxFileSizeMb", profile.getMaxFileSizeMb());

            ArrayNode contentTypesNode = objectMapper.createArrayNode();
            for (String contentType : profile.getAllowedContentTypes()) {
                contentTypesNode.add(contentType);
            }
            profileNode.set("allowedContentTypes", contentTypesNode);
            profilesNode.add(profileNode);
        }

        rootNode.set("profiles", profilesNode);
        return siteConfigService.writeJson(rootNode);
    }

    /**
     * 转换后台响应。
     *
     * @param model 配置模型
     * @return 后台响应
     */
    private AdminSiteStorageS3ConfigResponse toResponse(SiteStorageS3Config model) {
        AdminSiteStorageS3ConfigResponse response = new AdminSiteStorageS3ConfigResponse();
        response.setActiveProfileKey(model.getActiveProfileKey());

        List<AdminSiteStorageS3ProfileResponse> profileResponses = new ArrayList<>();
        for (SiteStorageS3Profile profile : model.getProfiles()) {
            AdminSiteStorageS3ProfileResponse profileResponse = new AdminSiteStorageS3ProfileResponse();
            profileResponse.setProfileKey(profile.getProfileKey());
            profileResponse.setName(profile.getName());
            profileResponse.setEnabled(Boolean.TRUE.equals(profile.getEnabled()));
            profileResponse.setEndpoint(profile.getEndpoint());
            profileResponse.setRegion(profile.getRegion());
            profileResponse.setBucket(profile.getBucket());
            profileResponse.setAccessKey(profile.getAccessKey());
            profileResponse.setSecretKey(profile.getSecretKey());
            profileResponse.setPathStyleAccess(Boolean.TRUE.equals(profile.getPathStyleAccess()));
            profileResponse.setPublicBaseUrl(profile.getPublicBaseUrl());
            profileResponse.setPresignExpireSeconds(profile.getPresignExpireSeconds());
            profileResponse.setMaxFileSizeMb(profile.getMaxFileSizeMb());
            profileResponse.setAllowedContentTypes(profile.getAllowedContentTypes());
            profileResponses.add(profileResponse);
        }
        response.setProfiles(profileResponses);
        return response;
    }

    /**
     * 读取字符串字段。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private String readText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || node.get(fieldName) == null || node.get(fieldName).isNull()) {
            return defaultValue;
        }
        return defaultText(node.get(fieldName).asText(defaultValue), defaultValue);
    }

    /**
     * 读取允许的内容类型。
     *
     * @param node JSON 节点
     * @return 允许的内容类型列表
     */
    private List<String> readAllowedContentTypes(JsonNode node) {
        List<String> contentTypes = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                String contentType = normalizeText(item.asText(""));
                if (!contentType.isEmpty()) {
                    contentTypes.add(contentType.toLowerCase(Locale.ROOT));
                }
            }
        }

        if (contentTypes.isEmpty()) {
            return List.of("image/jpeg", "image/png", "image/webp", "image/gif", "video/mp4");
        }
        return contentTypes.stream().distinct().toList();
    }

    /**
     * 读取必填文本。
     *
     * @param value 原始值
     * @param errorMessage 错误提示
     * @return 文本值
     */
    private String requireText(String value, String errorMessage) {
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, errorMessage);
        }
        return normalized;
    }

    /**
     * 读取默认文本。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 文本值
     */
    private String defaultText(String value, String defaultValue) {
        String normalized = normalizeText(value);
        return normalized.isEmpty() ? defaultValue : normalized;
    }

    /**
     * 规范化文本。
     *
     * @param value 原始值
     * @return 规范化结果
     */
    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 创建 S3 客户端。
     *
     * @param profile S3 配置
     * @return S3 客户端
     */
    private S3Client createS3Client(SiteStorageS3Profile profile) {
        return S3Client.builder()
                .region(Region.of(profile.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey())
                ))
                .endpointOverride(java.net.URI.create(profile.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(Boolean.TRUE.equals(profile.getPathStyleAccess()))
                        .build())
                .build();
    }
}
