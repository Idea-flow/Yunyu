package com.ideaflow.yunyu.module.friendlink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "关键词。可匹配站点名称、站点地址、联系人等文本字段。", example = "IdeaFlow")
    private String keyword;

    @Schema(description = "友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。", example = "APPROVED", allowableValues = {"PENDING", "APPROVED", "REJECTED", "OFFLINE"})
    @Pattern(regexp = "PENDING|APPROVED|REJECTED|OFFLINE", message = "状态不合法")
    private String status;

    @Schema(description = "页码。最小为 1。", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNo;

    @Schema(description = "每页条数。范围 1-100。", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize;
}
