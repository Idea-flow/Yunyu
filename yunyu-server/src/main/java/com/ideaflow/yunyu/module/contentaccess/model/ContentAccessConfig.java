package com.ideaflow.yunyu.module.contentaccess.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一内容访问控制配置类。
 * 作用：承载文章级访问控制和尾部隐藏内容访问控制的聚合配置，统一映射到正文扩展 JSON 字段。
 */
@Data
public class ContentAccessConfig {

    @Schema(description = "配置版本号，当前默认传 1", example = "1")
    private Integer version;

    @Schema(description = "整篇文章访问控制配置。默认不启用；若启用访问码规则，必须同时提供文章访问码和提示文案。")
    private ContentAccessArticleConfig articleAccess;

    @Schema(description = "文章尾部隐藏内容访问控制配置。默认不启用；若启用则还需要填写隐藏内容标题、规则和隐藏正文。")
    private ContentAccessTailHiddenConfig tailHiddenAccess;
}
