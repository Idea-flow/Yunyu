package com.ideaflow.yunyu.module.friendlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台友链状态更新请求类。
 * 作用：承接后台快速审核或上下架友链时提交的目标状态字段。
 */
@Data
public class AdminFriendLinkStatusUpdateRequest {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED|OFFLINE", message = "状态不合法")
    private String status;
}
