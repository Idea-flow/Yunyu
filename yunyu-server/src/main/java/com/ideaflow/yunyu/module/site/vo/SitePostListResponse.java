package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 前台文章列表响应类。
 * 作用：统一承接前台文章分页查询结果，供文章列表页、分类页和专题页复用。
 */
@Data
public class SitePostListResponse {

    private List<SitePostSummaryResponse> list;
    private Long total;
    private Long pageNo;
    private Long pageSize;
    private Long totalPages;
}
