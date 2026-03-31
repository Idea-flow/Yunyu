package com.ideaflow.yunyu.module.topic.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台专题列表响应类。
 * 作用：统一返回后台专题列表数据和分页信息，便于前端专题工作台直接消费。
 */
@Data
@AllArgsConstructor
public class AdminTopicListResponse {

    private final List<AdminTopicItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;
}
