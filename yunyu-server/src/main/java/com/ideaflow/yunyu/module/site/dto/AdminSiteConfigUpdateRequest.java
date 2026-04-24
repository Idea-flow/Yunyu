package com.ideaflow.yunyu.module.site.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 后台站点配置更新请求类。
 * 作用：承接站长在后台提交的站点基础信息、SEO 配置与主题配置字段，并在进入业务层前完成基础校验。
 */
public class AdminSiteConfigUpdateRequest {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 64, message = "站点名称长度不能超过64个字符")
    private String siteName;

    @NotBlank(message = "站点副标题不能为空")
    @Size(max = 255, message = "站点副标题长度不能超过255个字符")
    private String siteSubTitle;

    @Size(max = 255, message = "页脚文案长度不能超过255个字符")
    private String footerText;

    @Size(max = 255, message = "Logo 地址长度不能超过255个字符")
    private String logoUrl;

    @Size(max = 255, message = "Favicon 地址长度不能超过255个字符")
    private String faviconUrl;

    @NotBlank(message = "默认标题不能为空")
    @Size(max = 128, message = "默认标题长度不能超过128个字符")
    private String defaultTitle;

    @NotBlank(message = "默认描述不能为空")
    @Size(max = 255, message = "默认描述长度不能超过255个字符")
    private String defaultDescription;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "主色需为 #RRGGBB 格式")
    private String primaryColor;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "辅助色需为 #RRGGBB 格式")
    private String secondaryColor;

    private Boolean wechatAccessCodeEnabled;

    @Size(max = 128, message = "公众号验证码长度不能超过128个字符")
    private String wechatAccessCode;

    @Size(max = 255, message = "公众号验证码提示文案长度不能超过255个字符")
    private String wechatAccessCodeHint;

    @Size(max = 255, message = "公众号二维码地址长度不能超过255个字符")
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
