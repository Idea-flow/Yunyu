package com.ideaflow.yunyu.module.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 前台专题列表响应类。
 * 作用：统一承接前台专题列表页需要的专题集合数据。
 */
@Data
public class SiteTopicListResponse {

    private List<SiteTopicItemResponse> list;
}
