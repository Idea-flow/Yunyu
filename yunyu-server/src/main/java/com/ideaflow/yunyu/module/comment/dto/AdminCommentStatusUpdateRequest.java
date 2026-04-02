package com.ideaflow.yunyu.module.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台评论状态更新请求类。
 * 作用：承接站长对评论执行审核通过、驳回或退回待审时提交的目标状态。
 */
@Data
public class AdminCommentStatusUpdateRequest {

    @NotBlank(message = "评论状态不能为空")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "评论状态不合法")
    private String status;
}
