package com.ideaflow.yunyu.module.site.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台站点配置响应类。
 * 作用：向后台站点设置页面返回统一的站点基础信息、SEO 配置与主题配置结构。
 */
@Data
public class AdminSiteConfigResponse {

    @Schema(description = "站点名称。", example = "云屿")
    private String siteName;
    @Schema(description = "站点副标题。", example = "记录技术与生活")
    private String siteSubTitle;
    @Schema(description = "页脚文案。", example = "持续记录，持续分享。")
    private String footerText;
    @Schema(description = "站点 Logo 地址。", example = "https://cdn.example.com/assets/logo.png")
    private String logoUrl;
    @Schema(description = "站点 Favicon 地址。", example = "https://cdn.example.com/assets/favicon.ico")
    private String faviconUrl;
    @Schema(description = "默认标题模板。", example = "云屿 | 技术与生活")
    private String defaultTitle;
    @Schema(description = "默认描述。", example = "记录技术实践、阅读思考与生活感受。")
    private String defaultDescription;
    @Schema(description = "站点主色。", example = "#2563EB")
    private String primaryColor;
    @Schema(description = "站点辅助色。", example = "#0F172A")
    private String secondaryColor;
    @Schema(description = "是否启用公众号验证码访问。", example = "false")
    private Boolean wechatAccessCodeEnabled;
    @Schema(description = "公众号访问验证码。", example = "yunyu-2026")
    private String wechatAccessCode;
    @Schema(description = "公众号验证码提示文案。", example = "关注公众号后回复验证码获取访问权限")
    private String wechatAccessCodeHint;
    @Schema(description = "公众号二维码图片地址。", example = "https://cdn.example.com/assets/wechat-qrcode.png")
    private String wechatQrCodeUrl;
}
