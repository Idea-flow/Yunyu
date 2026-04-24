package com.ideaflow.yunyu.module.contentaccess.model;

import java.util.List;
import lombok.Data;

/**
 * 文章级访问控制配置类。
 * 作用：描述整篇文章访问前需要满足的规则集合，以及文章独立访问码相关配置。
 */
@Data
public class ContentAccessArticleConfig {

    private Boolean enabled;
    private List<String> ruleTypes;
    private String articleAccessCode;
    private String articleAccessCodeHint;
}
