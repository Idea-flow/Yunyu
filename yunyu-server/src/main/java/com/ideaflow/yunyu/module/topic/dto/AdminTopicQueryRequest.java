package com.ideaflow.yunyu.module.topic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台专题列表查询请求类。
 * 作用：承接后台专题列表的搜索、状态筛选和分页参数。
 */
@Data
public class AdminTopicQueryRequest {

    @Schema(description = "关键词。可匹配专题名称或 slug。", example = "全栈实践")
    private String keyword;
    @Schema(description = "专题状态。ACTIVE=启用，DISABLED=停用。", example = "ACTIVE", allowableValues = {"ACTIVE", "DISABLED"})
    private String status;
    @Schema(description = "页码。默认 1。", example = "1")
    private Integer pageNo = 1;
    @Schema(description = "每页条数。默认 10。", example = "10")
    private Integer pageSize = 10;
}
