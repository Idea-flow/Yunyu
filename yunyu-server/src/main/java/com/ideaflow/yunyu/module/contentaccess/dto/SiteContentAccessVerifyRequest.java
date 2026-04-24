package com.ideaflow.yunyu.module.contentaccess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 前台内容访问校验请求类。
 * 作用：承接前台文章详情页提交的文章访问码或公众号验证码，并标识校验范围与规则类型。
 */
@Data
public class SiteContentAccessVerifyRequest {

    @NotBlank(message = "访问范围不能为空")
    @Pattern(regexp = "ARTICLE|TAIL_HIDDEN", message = "访问范围不合法")
    private String scopeType;

    @NotBlank(message = "规则类型不能为空")
    @Pattern(regexp = "ACCESS_CODE|WECHAT_ACCESS_CODE", message = "规则类型不合法")
    private String ruleType;

    @NotBlank(message = "访问验证码不能为空")
    private String accessCode;
}
