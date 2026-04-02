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

    @Size(max = 255, message = "默认分享图地址长度不能超过255个字符")
    private String defaultShareImage;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "主色需为 #RRGGBB 格式")
    private String primaryColor;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "辅助色需为 #RRGGBB 格式")
    private String secondaryColor;

    @NotBlank(message = "首页风格不能为空")
    @Size(max = 32, message = "首页风格长度不能超过32个字符")
    private String homeStyle;

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
     * 获取默认分享图地址。
     *
     * @return 默认分享图地址
     */
    public String getDefaultShareImage() {
        return defaultShareImage;
    }

    /**
     * 设置默认分享图地址。
     *
     * @param defaultShareImage 默认分享图地址
     */
    public void setDefaultShareImage(String defaultShareImage) {
        this.defaultShareImage = defaultShareImage;
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
     * 获取首页风格。
     *
     * @return 首页风格
     */
    public String getHomeStyle() {
        return homeStyle;
    }

    /**
     * 设置首页风格。
     *
     * @param homeStyle 首页风格
     */
    public void setHomeStyle(String homeStyle) {
        this.homeStyle = homeStyle;
    }
}
