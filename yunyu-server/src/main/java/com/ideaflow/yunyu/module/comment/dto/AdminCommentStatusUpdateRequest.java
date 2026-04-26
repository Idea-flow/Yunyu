package com.ideaflow.yunyu.module.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台评论状态更新请求类。
 * 作用：承接站长对评论执行审核通过、驳回或退回待审时提交的目标状态。
 */
@Data
public class AdminCommentStatusUpdateRequest {

    @Schema(description = "评论审核状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
    @NotBlank(message = "评论状态不能为空")
    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "评论状态不合法")
    private String status;
}
