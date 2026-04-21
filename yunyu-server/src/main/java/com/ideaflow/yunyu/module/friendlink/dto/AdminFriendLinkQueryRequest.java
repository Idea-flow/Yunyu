package com.ideaflow.yunyu.module.friendlink.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台友链查询请求类。
 * 作用：承接后台友链管理页的关键词、状态和分页筛选参数。
 */
@Data
public class AdminFriendLinkQueryRequest {

    private String keyword;

    @Pattern(regexp = "PENDING|APPROVED|REJECTED|OFFLINE", message = "状态不合法")
    private String status;

    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNo;

    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize;
}
