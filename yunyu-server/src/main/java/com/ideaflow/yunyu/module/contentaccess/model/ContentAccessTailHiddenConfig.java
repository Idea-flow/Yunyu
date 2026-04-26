package com.ideaflow.yunyu.module.contentaccess.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 尾部隐藏内容访问控制配置类。
 * 作用：描述文章尾部隐藏内容模块的标题和可见规则，为前台详情页裁决提供配置来源。
 */
@Data
public class ContentAccessTailHiddenConfig {

    @Schema(description = "是否启用尾部隐藏内容。默认 false。", example = "false")
    private Boolean enabled;

    @Schema(description = "隐藏内容模块标题。启用尾部隐藏内容时必填。", example = "进阶扩展内容")
    private String title;

    @Schema(description = "尾部隐藏内容访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。", example = "[\"LOGIN\"]")
    private List<String> ruleTypes;
}
