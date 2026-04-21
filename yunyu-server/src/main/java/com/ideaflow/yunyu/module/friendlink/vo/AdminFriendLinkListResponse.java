package com.ideaflow.yunyu.module.friendlink.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 后台友链列表响应类。
 * 作用：统一返回后台友链列表数据和分页信息，便于后台管理页直接消费。
 */
@Data
@AllArgsConstructor
public class AdminFriendLinkListResponse {

    private final List<AdminFriendLinkItemResponse> list;
    private final long total;
    private final long pageNo;
    private final long pageSize;
    private final long totalPages;
}
