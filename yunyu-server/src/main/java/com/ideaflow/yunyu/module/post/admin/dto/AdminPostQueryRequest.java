package com.ideaflow.yunyu.module.post.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台文章列表查询请求类。
 * 作用：承接后台文章列表的搜索、状态筛选和分页参数。
 */
@Data
public class AdminPostQueryRequest {

    @Schema(description = "关键词。可匹配文章标题、摘要等文本字段。", example = "Spring Boot")
    private String keyword;
    @Schema(description = "文章状态。DRAFT=草稿，PUBLISHED=发布，OFFLINE=下线。", example = "PUBLISHED", allowableValues = {"DRAFT", "PUBLISHED", "OFFLINE"})
    private String status;
    @Schema(description = "分类 ID 筛选。", example = "12")
    private Long categoryId;
    @Schema(description = "标签 ID 筛选。", example = "3")
    private Long tagId;
    @Schema(description = "专题 ID 筛选。", example = "5")
    private Long topicId;
    @Schema(description = "是否置顶筛选。1=是，0=否。", example = "1")
    private Integer isTop;
    @Schema(description = "是否推荐筛选。1=是，0=否。", example = "0")
    private Integer isRecommend;
    @Schema(description = "是否允许评论筛选。1=允许，0=不允许。", example = "1")
    private Integer allowComment;
    @Schema(description = "页码。默认 1。", example = "1")
    private Integer pageNo = 1;
    @Schema(description = "每页条数。默认 10。", example = "10")
    private Integer pageSize = 10;
}
