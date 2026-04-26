package com.ideaflow.yunyu.module.contentaccess.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 文章级访问控制配置类。
 * 作用：描述整篇文章访问前需要满足的规则集合，以及文章独立访问码相关配置。
 */
@Data
public class ContentAccessArticleConfig {

    @Schema(description = "是否启用整篇文章访问控制。默认 false。", example = "false")
    private Boolean enabled;

    @Schema(description = "文章访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。", example = "[\"LOGIN\"]")
    private List<String> ruleTypes;

    @Schema(description = "文章访问码。当规则中包含 ACCESS_CODE 时必填。", example = "spring-2026")
    private String articleAccessCode;

    @Schema(description = "文章访问码提示文案。当规则中包含 ACCESS_CODE 时建议填写，便于前台提示用户。", example = "请输入文章访问码后查看全文")
    private String articleAccessCodeHint;
}
