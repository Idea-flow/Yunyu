package com.ideaflow.yunyu.module.ai.admin.scene.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.ai.admin.dto.AdminPostAiMetaGenerateRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.ideaflow.yunyu.module.ai.internal.service.protocol.ChatCompletionsService;
import com.ideaflow.yunyu.module.post.entity.PostEntity;
import com.ideaflow.yunyu.module.post.mapper.PostMapper;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 文章元信息 AI 生成场景服务类。
 * 作用：承接文章元信息生成业务语义，固定复用 Chat Completions 服务，并对生成结果执行结构化校验与 slug 唯一化处理。
 */
@Service
public class PostMetaGenerateService {

    private static final int SLUG_MAX_LENGTH = 220;
    private static final int SUMMARY_MAX_LENGTH = 500;
    private static final int SEO_TITLE_MAX_LENGTH = 255;
    private static final int SEO_DESCRIPTION_MAX_LENGTH = 500;
    private static final int PROMPT_MARKDOWN_MAX_LENGTH = 6000;

    private final ChatCompletionsService chatCompletionsService;
    private final PostMapper postMapper;
    private final ObjectMapper objectMapper;

    /**
     * 创建文章元信息场景服务。
     *
     * @param chatCompletionsService Chat 协议服务
     * @param postMapper 文章 Mapper
     * @param objectMapper JSON 对象映射器
     */
    public PostMetaGenerateService(ChatCompletionsService chatCompletionsService,
                                   PostMapper postMapper,
                                   ObjectMapper objectMapper) {
        this.chatCompletionsService = chatCompletionsService;
        this.postMapper = postMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 以非流式方式生成文章元信息。
     *
     * @param request 元信息生成请求
     * @return OpenAI Chat 风格响应体
     */
    public JsonNode generate(AdminPostAiMetaGenerateRequest request) {
        JsonNode requestBody = buildChatRequest(request, false);
        JsonNode responseBody = chatCompletionsService.create(requestBody);
        return normalizeGeneratedMetaResponse(requestBody, responseBody);
    }

    /**
     * 以流式方式生成文章元信息。
     *
     * @param request 元信息生成请求
     * @param emitter SSE 输出器
     */
    public void streamGenerate(AdminPostAiMetaGenerateRequest request, SseEmitter emitter) {
        JsonNode requestBody = buildChatRequest(request, true);
        chatCompletionsService.stream(requestBody, emitter);
    }

    /**
     * 构建底层 Chat 请求体。
     * 作用：把标题、正文和可选模型参数转换为固定的 Chat Completions 请求结构，统一提示词与截断策略。
     *
     * @param request 元信息生成请求
     * @param stream 是否流式
     * @return Chat 请求体
     */
    private JsonNode buildChatRequest(AdminPostAiMetaGenerateRequest request, boolean stream) {
        AdminPostAiMetaGenerateRequest normalizedRequest = request == null
                ? new AdminPostAiMetaGenerateRequest()
                : request;

        String normalizedTitle = trimToLength(normalizedRequest.getTitle(), 200);
        String normalizedMarkdown = normalizeOptionalText(normalizedRequest.getContentMarkdown());
        if (isBlank(normalizedTitle) && isBlank(normalizedMarkdown)) {
            throw new BizException(ResultCode.BAD_REQUEST, "标题或正文至少填写一项");
        }

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("stream", stream);

        if (!isBlank(normalizedRequest.getModel())) {
            rootNode.put("model", normalizedRequest.getModel().trim());
        }
        if (normalizedRequest.getTemperature() != null) {
            if (normalizedRequest.getTemperature() < 0 || normalizedRequest.getTemperature() > 2) {
                throw new BizException(ResultCode.BAD_REQUEST, "temperature 需在 0-2 之间");
            }
            rootNode.put("temperature", normalizedRequest.getTemperature());
        }
        if (normalizedRequest.getMaxTokens() != null) {
            if (normalizedRequest.getMaxTokens() < 1 || normalizedRequest.getMaxTokens() > 128000) {
                throw new BizException(ResultCode.BAD_REQUEST, "maxTokens 需在 1-128000 之间");
            }
            rootNode.put("max_tokens", normalizedRequest.getMaxTokens());
        }

        ArrayNode messagesNode = rootNode.putArray("messages");
        ObjectNode systemMessageNode = messagesNode.addObject();
        systemMessageNode.put("role", "system");
        systemMessageNode.put("content", String.join("\n",
                "你是博客文章元信息生成助手。",
                "请仅输出 JSON，不要输出任何额外说明、Markdown 或代码块。",
                "必须包含字段：slug、summary、seoTitle、seoDescription。",
                "slug 仅允许小写字母、数字和连字符。"
        ));

        ObjectNode userMessageNode = messagesNode.addObject();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", String.join("\n",
                "请根据以下文章信息生成元信息 JSON：",
                "标题：" + (isBlank(normalizedTitle) ? "(空)" : normalizedTitle),
                "正文（Markdown）：",
                buildPromptMarkdown(normalizedMarkdown)
        ));
        return rootNode;
    }

    /**
     * 构建用于提示词的正文文本。
     * 作用：限制传给模型的正文长度，避免正文过长导致无效消耗。
     *
     * @param markdown 原始 Markdown
     * @return 截断后的 Markdown 文本
     */
    private String buildPromptMarkdown(String markdown) {
        if (isBlank(markdown)) {
            return "(空)";
        }

        if (markdown.length() <= PROMPT_MARKDOWN_MAX_LENGTH) {
            return markdown;
        }
        return markdown.substring(0, PROMPT_MARKDOWN_MAX_LENGTH)
                + "\n\n[内容已截断，以上为正文前 6000 字符]";
    }

    /**
     * 规范化 Chat 返回中的元信息内容。
     * 作用：在保持 OpenAI 响应结构不变的前提下，兜底校验 message.content 中的 JSON 数据并写回标准化结果。
     *
     * @param requestBody 请求体
     * @param responseBody 响应体
     * @return 规范化后的响应体
     */
    private JsonNode normalizeGeneratedMetaResponse(JsonNode requestBody, JsonNode responseBody) {
        if (!(responseBody instanceof ObjectNode responseNode)) {
            return responseBody;
        }

        ObjectNode normalizedResponseNode = responseNode.deepCopy();
        JsonNode choicesNode = normalizedResponseNode.path("choices");
        if (!choicesNode.isArray() || choicesNode.isEmpty()) {
            return normalizedResponseNode;
        }

        JsonNode firstChoiceNode = choicesNode.get(0);
        if (!(firstChoiceNode instanceof ObjectNode firstChoiceObjectNode)) {
            return normalizedResponseNode;
        }

        JsonNode messageNode = firstChoiceObjectNode.path("message");
        if (!(messageNode instanceof ObjectNode messageObjectNode)) {
            return normalizedResponseNode;
        }

        String rawContent = extractMessageContent(messageObjectNode.path("content"));
        ObjectNode parsedMetaNode = parseGeneratedMetaJson(rawContent);
        if (parsedMetaNode == null) {
            return normalizedResponseNode;
        }

        String fallbackTitle = extractTitleFromRequest(requestBody);
        ObjectNode normalizedMetaNode = normalizeGeneratedMeta(parsedMetaNode, fallbackTitle);
        messageObjectNode.put("content", normalizedMetaNode.toString());
        return normalizedResponseNode;
    }

    /**
     * 提取 message.content 文本。
     * 作用：兼容 content 为字符串或数组结构两种常见返回形态。
     *
     * @param contentNode content 节点
     * @return 提取后的文本
     */
    private String extractMessageContent(JsonNode contentNode) {
        if (contentNode == null || contentNode.isNull()) {
            return "";
        }

        if (contentNode.isTextual()) {
            return contentNode.asText("");
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode itemNode : contentNode) {
                if (itemNode == null || itemNode.isNull()) {
                    continue;
                }

                if (itemNode.isTextual()) {
                    builder.append(itemNode.asText(""));
                    continue;
                }

                String text = itemNode.path("text").asText("");
                if (!isBlank(text)) {
                    builder.append(text);
                }
            }
            return builder.toString();
        }

        return contentNode.toString();
    }

    /**
     * 解析模型输出中的 JSON。
     * 作用：兼容模型返回 ```json 包裹或带额外文本前后缀的场景。
     *
     * @param rawContent 原始文本
     * @return 解析后的 JSON 对象节点，解析失败返回 null
     */
    private ObjectNode parseGeneratedMetaJson(String rawContent) {
        if (isBlank(rawContent)) {
            return null;
        }

        String jsonLikeText = extractJsonLikeText(rawContent);
        if (isBlank(jsonLikeText)) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(jsonLikeText);
            return node instanceof ObjectNode objectNode ? objectNode : null;
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * 从文本中提取 JSON 片段。
     *
     * @param rawContent 原始文本
     * @return JSON 文本
     */
    private String extractJsonLikeText(String rawContent) {
        String cleanedContent = rawContent
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();

        int firstBraceIndex = cleanedContent.indexOf('{');
        int lastBraceIndex = cleanedContent.lastIndexOf('}');
        if (firstBraceIndex >= 0 && lastBraceIndex > firstBraceIndex) {
            return cleanedContent.substring(firstBraceIndex, lastBraceIndex + 1);
        }
        return cleanedContent;
    }

    /**
     * 规范化生成元信息。
     * 作用：统一执行字段长度约束、slug 规范化与唯一化、以及回退字段兜底。
     *
     * @param parsedMetaNode 解析出的元信息节点
     * @param fallbackTitle 回退标题
     * @return 规范化后的元信息节点
     */
    private ObjectNode normalizeGeneratedMeta(ObjectNode parsedMetaNode, String fallbackTitle) {
        String slug = normalizeSlug(readText(parsedMetaNode, "slug", ""), fallbackTitle);
        String uniqueSlug = ensureUniqueSlug(slug);

        String summary = trimToLength(readText(parsedMetaNode, "summary", ""), SUMMARY_MAX_LENGTH);
        if (isBlank(summary) && !isBlank(fallbackTitle)) {
            summary = trimToLength(fallbackTitle, Math.min(SUMMARY_MAX_LENGTH, 120));
        }

        String seoTitle = trimToLength(readText(parsedMetaNode, "seoTitle", ""), SEO_TITLE_MAX_LENGTH);
        if (isBlank(seoTitle) && !isBlank(fallbackTitle)) {
            seoTitle = trimToLength(fallbackTitle, SEO_TITLE_MAX_LENGTH);
        }

        String seoDescription = trimToLength(readText(parsedMetaNode, "seoDescription", ""), SEO_DESCRIPTION_MAX_LENGTH);
        if (isBlank(seoDescription)) {
            seoDescription = trimToLength(summary, SEO_DESCRIPTION_MAX_LENGTH);
        }

        ObjectNode normalizedMetaNode = objectMapper.createObjectNode();
        normalizedMetaNode.put("slug", uniqueSlug);
        normalizedMetaNode.put("summary", summary);
        normalizedMetaNode.put("seoTitle", seoTitle);
        normalizedMetaNode.put("seoDescription", seoDescription);

        ArrayNode slugCandidatesNode = normalizedMetaNode.putArray("slugCandidates");
        for (String candidate : buildSlugCandidates(uniqueSlug)) {
            slugCandidatesNode.add(candidate);
        }
        return normalizedMetaNode;
    }

    /**
     * 规范化 slug。
     *
     * @param slug 原始 slug
     * @param fallbackTitle 回退标题
     * @return 规范化后的 slug
     */
    private String normalizeSlug(String slug, String fallbackTitle) {
        String source = isBlank(slug) ? fallbackTitle : slug;
        if (isBlank(source)) {
            source = "post-" + System.currentTimeMillis();
        }

        String normalizedSlug = source.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\-\\s_]", "-")
                .replaceAll("[\\s_]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (isBlank(normalizedSlug)) {
            normalizedSlug = "post-" + System.currentTimeMillis();
        }

        normalizedSlug = trimToLength(normalizedSlug, SLUG_MAX_LENGTH);
        normalizedSlug = normalizedSlug.replaceAll("-+$", "");
        return isBlank(normalizedSlug) ? "post-" + System.currentTimeMillis() : normalizedSlug;
    }

    /**
     * 确保 slug 在 post 表中唯一。
     *
     * @param baseSlug 基础 slug
     * @return 唯一 slug
     */
    private String ensureUniqueSlug(String baseSlug) {
        String candidateSlug = baseSlug;
        if (!existsSlug(candidateSlug)) {
            return candidateSlug;
        }

        int index = 2;
        while (index < 10000) {
            String suffix = "-" + index;
            String base = baseSlug;
            if (base.length() + suffix.length() > SLUG_MAX_LENGTH) {
                base = base.substring(0, Math.max(1, SLUG_MAX_LENGTH - suffix.length())).replaceAll("-+$", "");
            }

            candidateSlug = base + suffix;
            if (!existsSlug(candidateSlug)) {
                return candidateSlug;
            }
            index++;
        }

        return "post-" + System.currentTimeMillis();
    }

    /**
     * 构建 slug 候选列表。
     *
     * @param uniqueSlug 已确认可用的 slug
     * @return 候选列表
     */
    private Set<String> buildSlugCandidates(String uniqueSlug) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(uniqueSlug);

        String fallbackCandidate = normalizeSlug(uniqueSlug + "-alt", uniqueSlug);
        if (!isBlank(fallbackCandidate)) {
            candidates.add(fallbackCandidate);
        }
        return candidates;
    }

    /**
     * 判断 slug 是否已存在。
     *
     * @param slug slug
     * @return 是否存在
     */
    private boolean existsSlug(String slug) {
        Long count = postMapper.selectCount(new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getSlug, slug)
                .eq(PostEntity::getDeleted, 0));
        return count != null && count > 0;
    }

    /**
     * 从请求体提取回退标题。
     * 作用：当模型未返回足够字段时，尽量从消息里提取一个可用标题兜底。
     *
     * @param requestBody 请求体
     * @return 回退标题
     */
    private String extractTitleFromRequest(JsonNode requestBody) {
        if (requestBody == null || requestBody.isNull()) {
            return "";
        }

        JsonNode messagesNode = requestBody.path("messages");
        if (!messagesNode.isArray() || messagesNode.isEmpty()) {
            return "";
        }

        for (int index = messagesNode.size() - 1; index >= 0; index--) {
            JsonNode messageNode = messagesNode.get(index);
            if (messageNode == null || messageNode.isNull()) {
                continue;
            }

            if (!"user".equalsIgnoreCase(messageNode.path("role").asText(""))) {
                continue;
            }

            String content = extractMessageContent(messageNode.path("content"));
            if (isBlank(content)) {
                continue;
            }

            String firstLine = content.trim().split("\\R", 2)[0].trim();
            return trimToLength(firstLine, 120);
        }

        return "";
    }

    /**
     * 读取对象字段文本。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @param defaultValue 默认值
     * @return 字段文本
     */
    private String readText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || node.get(fieldName) == null || node.get(fieldName).isNull()) {
            return defaultValue;
        }
        String value = node.path(fieldName).asText(defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    /**
     * 按最大长度裁剪文本。
     *
     * @param value 原始值
     * @param maxLength 最大长度
     * @return 裁剪后的文本
     */
    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        String trimmedValue = value.trim();
        if (trimmedValue.length() <= maxLength) {
            return trimmedValue;
        }
        return trimmedValue.substring(0, maxLength).trim();
    }

    /**
     * 标准化可选文本。
     *
     * @param value 原始值
     * @return 去首尾空白后的文本
     */
    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 原始值
     * @return 是否为空白
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
