package com.ideaflow.yunyu.module.post.site.vo;

import java.util.List;
import lombok.Data;

/**
 * 前台内容访问状态响应类。
 * 作用：向文章详情页返回当前访客的访问状态、规则命中情况以及站点级公众号验证码配置。
 */
@Data
public class SiteContentAccessStateResponse {

    private Boolean loggedIn;
    private Boolean articleAccessAllowed;
    private Boolean tailHiddenAccessAllowed;
    private List<String> articleAccessRuleTypes;
    private List<String> articleAccessPendingRuleTypes;
    private List<String> tailHiddenAccessRuleTypes;
    private List<String> tailHiddenAccessPendingRuleTypes;
    private Boolean wechatAccessCodeEnabled;
    private String wechatAccessCodeHint;
    private String wechatQrCodeUrl;
}
