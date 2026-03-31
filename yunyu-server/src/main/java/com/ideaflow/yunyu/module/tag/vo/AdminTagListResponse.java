package com.ideaflow.yunyu.module.tag.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台标签列表响应类。
 * 作用：统一返回后台标签列表数据和分页信息，便于前端标签工作台直接消费。
 */
@Data
@AllArgsConstructor
public class AdminTagListResponse {

    private final List<AdminTagItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;
}
