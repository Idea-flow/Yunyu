package com.ideaflow.yunyu.module.category.site.vo;

import com.ideaflow.yunyu.module.post.site.vo.SitePostListResponse;
import lombok.Data;

/**
 * 前台分类详情响应类。
 * 作用：统一向分类页返回分类信息和该分类下的文章分页数据。
 */
@Data
public class SiteCategoryDetailResponse {

    private SiteCategoryItemResponse category;
    private SitePostListResponse posts;
}
