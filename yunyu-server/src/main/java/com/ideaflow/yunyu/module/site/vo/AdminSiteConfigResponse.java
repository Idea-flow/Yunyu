package com.ideaflow.yunyu.module.site.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台站点配置响应类。
 * 作用：向后台站点设置页面返回统一的站点基础信息、SEO 配置与主题配置结构。
 */
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

    /**
     * 获取站点名称。
     *
     * @return 站点名称
     */
    public String getSiteName() {
        return siteName;
    }

    /**
     * 设置站点名称。
     *
     * @param siteName 站点名称
     */
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    /**
     * 获取站点副标题。
     *
     * @return 站点副标题
     */
    public String getSiteSubTitle() {
        return siteSubTitle;
    }

    /**
     * 设置站点副标题。
     *
     * @param siteSubTitle 站点副标题
     */
    public void setSiteSubTitle(String siteSubTitle) {
        this.siteSubTitle = siteSubTitle;
    }

    /**
     * 获取页脚文案。
     *
     * @return 页脚文案
     */
    public String getFooterText() {
        return footerText;
    }

    /**
     * 设置页脚文案。
     *
     * @param footerText 页脚文案
     */
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    /**
     * 获取 Logo 地址。
     *
     * @return Logo 地址
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * 设置 Logo 地址。
     *
     * @param logoUrl Logo 地址
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    /**
     * 获取 Favicon 地址。
     *
     * @return Favicon 地址
     */
    public String getFaviconUrl() {
        return faviconUrl;
    }

    /**
     * 设置 Favicon 地址。
     *
     * @param faviconUrl Favicon 地址
     */
    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    /**
     * 获取默认标题。
     *
     * @return 默认标题
     */
    public String getDefaultTitle() {
        return defaultTitle;
    }

    /**
     * 设置默认标题。
     *
     * @param defaultTitle 默认标题
     */
    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    /**
     * 获取默认描述。
     *
     * @return 默认描述
     */
    public String getDefaultDescription() {
        return defaultDescription;
    }

    /**
     * 设置默认描述。
     *
     * @param defaultDescription 默认描述
     */
    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    /**
     * 获取主色。
     *
     * @return 主色
     */
    public String getPrimaryColor() {
        return primaryColor;
    }

    /**
     * 设置主色。
     *
     * @param primaryColor 主色
     */
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * 获取辅助色。
     *
     * @return 辅助色
     */
    public String getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * 设置辅助色。
     *
     * @param secondaryColor 辅助色
     */
    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    /**
     * 获取是否启用公众号验证码。
     *
     * @return 是否启用公众号验证码
     */
    public Boolean getWechatAccessCodeEnabled() {
        return wechatAccessCodeEnabled;
    }

    /**
     * 设置是否启用公众号验证码。
     *
     * @param wechatAccessCodeEnabled 是否启用公众号验证码
     */
    public void setWechatAccessCodeEnabled(Boolean wechatAccessCodeEnabled) {
        this.wechatAccessCodeEnabled = wechatAccessCodeEnabled;
    }

    /**
     * 获取公众号验证码。
     *
     * @return 公众号验证码
     */
    public String getWechatAccessCode() {
        return wechatAccessCode;
    }

    /**
     * 设置公众号验证码。
     *
     * @param wechatAccessCode 公众号验证码
     */
    public void setWechatAccessCode(String wechatAccessCode) {
        this.wechatAccessCode = wechatAccessCode;
    }

    /**
     * 获取公众号验证码提示文案。
     *
     * @return 公众号验证码提示文案
     */
    public String getWechatAccessCodeHint() {
        return wechatAccessCodeHint;
    }

    /**
     * 设置公众号验证码提示文案。
     *
     * @param wechatAccessCodeHint 公众号验证码提示文案
     */
    public void setWechatAccessCodeHint(String wechatAccessCodeHint) {
        this.wechatAccessCodeHint = wechatAccessCodeHint;
    }

    /**
     * 获取公众号二维码地址。
     *
     * @return 公众号二维码地址
     */
    public String getWechatQrCodeUrl() {
        return wechatQrCodeUrl;
    }

    /**
     * 设置公众号二维码地址。
     *
     * @param wechatQrCodeUrl 公众号二维码地址
     */
    public void setWechatQrCodeUrl(String wechatQrCodeUrl) {
        this.wechatQrCodeUrl = wechatQrCodeUrl;
    }

}
