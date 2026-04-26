package com.ideaflow.yunyu.module.friendlink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台友链状态更新请求类。
 * 作用：承接后台快速审核或上下架友链时提交的目标状态字段。
 */
@Data
public class AdminFriendLinkStatusUpdateRequest {

    @Schema(description = "友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "OFFLINE"})
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED|OFFLINE", message = "状态不合法")
    private String status;
}
