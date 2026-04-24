package com.ideaflow.yunyu.module.contentaccess.model;

import lombok.Data;

/**
 * 统一内容访问控制配置类。
 * 作用：承载文章级访问控制和尾部隐藏内容访问控制的聚合配置，统一映射到正文扩展 JSON 字段。
 */
@Data
public class ContentAccessConfig {

    private Integer version;
    private ContentAccessArticleConfig articleAccess;
    private ContentAccessTailHiddenConfig tailHiddenAccess;
}
