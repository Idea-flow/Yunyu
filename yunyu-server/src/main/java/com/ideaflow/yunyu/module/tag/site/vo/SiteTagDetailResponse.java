package com.ideaflow.yunyu.module.tag.site.vo;

import com.ideaflow.yunyu.module.post.site.vo.SitePostListResponse;
import lombok.Data;

/**
 * 前台标签详情响应类。
 * 作用：统一向标签详情页返回标签信息和该标签下的文章分页数据。
 */
@Data
public class SiteTagDetailResponse {

    private SiteTagItemResponse tag;
    private SitePostListResponse posts;
}
