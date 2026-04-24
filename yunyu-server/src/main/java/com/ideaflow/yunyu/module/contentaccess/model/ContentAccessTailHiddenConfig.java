package com.ideaflow.yunyu.module.contentaccess.model;

import java.util.List;
import lombok.Data;

/**
 * 尾部隐藏内容访问控制配置类。
 * 作用：描述文章尾部隐藏内容模块的标题和可见规则，为前台详情页裁决提供配置来源。
 */
@Data
public class ContentAccessTailHiddenConfig {

    private Boolean enabled;
    private String title;
    private List<String> ruleTypes;
}
