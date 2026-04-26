package com.ideaflow.yunyu.module.topic.site.vo;

import com.ideaflow.yunyu.module.post.site.vo.SitePostListResponse;
import lombok.Data;

/**
 * 前台专题详情响应类。
 * 作用：统一向专题页返回专题信息和专题下的文章分页数据。
 */
@Data
public class SiteTopicDetailResponse {

    private SiteTopicItemResponse topic;
    private SitePostListResponse posts;
}
