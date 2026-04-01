package com.ideaflow.yunyu.module.site.dto;

import lombok.Data;

/**
 * 前台文章查询请求类。
 * 作用：统一承接前台文章列表、分类页和专题页的筛选与分页参数。
 */
@Data
public class SitePostQueryRequest {

    private String keyword;
    private String categorySlug;
    private String topicSlug;
    private String tagSlug;
    private Integer pageNo;
    private Integer pageSize;
}
