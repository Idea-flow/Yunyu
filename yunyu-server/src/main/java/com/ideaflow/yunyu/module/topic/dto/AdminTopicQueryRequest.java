package com.ideaflow.yunyu.module.topic.dto;

import lombok.Data;

/**
 * 后台专题列表查询请求类。
 * 作用：承接后台专题列表的搜索、状态筛选和分页参数。
 */
@Data
public class AdminTopicQueryRequest {

    private String keyword;
    private String status;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
