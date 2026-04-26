package com.ideaflow.yunyu.module.site.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台站点配置更新请求类。
 * 作用：承接站长在后台提交的站点基础信息、SEO 配置与主题配置字段，并在进入业务层前完成基础校验。
 */
@Data
public class AdminSiteConfigUpdateRequest {

    @Schema(description = "站点名称。", example = "云屿")
    @NotBlank(message = "站点名称不能为空")
    @Size(max = 64, message = "站点名称长度不能超过64个字符")
    private String siteName;

    @Schema(description = "站点副标题。", example = "记录技术与生活")
    @NotBlank(message = "站点副标题不能为空")
    @Size(max = 255, message = "站点副标题长度不能超过255个字符")
    private String siteSubTitle;

    @Schema(description = "页脚文案。", example = "持续记录，持续分享。")
    @Size(max = 255, message = "页脚文案长度不能超过255个字符")
    private String footerText;

    @Schema(description = "站点 Logo 地址。", example = "https://cdn.example.com/assets/logo.png")
    @Size(max = 255, message = "Logo 地址长度不能超过255个字符")
    private String logoUrl;

    @Schema(description = "站点 Favicon 地址。", example = "https://cdn.example.com/assets/favicon.ico")
    @Size(max = 255, message = "Favicon 地址长度不能超过255个字符")
    private String faviconUrl;

    @Schema(description = "站点默认标题模板。", example = "云屿 | 技术与生活")
    @NotBlank(message = "默认标题不能为空")
    @Size(max = 128, message = "默认标题长度不能超过128个字符")
    private String defaultTitle;

    @Schema(description = "站点默认描述。", example = "记录技术实践、阅读思考与生活感受。")
    @NotBlank(message = "默认描述不能为空")
    @Size(max = 255, message = "默认描述长度不能超过255个字符")
    private String defaultDescription;

    @Schema(description = "站点主色，格式必须为 #RRGGBB。", example = "#2563EB")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "主色需为 #RRGGBB 格式")
    private String primaryColor;

    @Schema(description = "站点辅助色，格式必须为 #RRGGBB。", example = "#0F172A")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "辅助色需为 #RRGGBB 格式")
    private String secondaryColor;

    @Schema(description = "是否启用公众号验证码访问。", example = "false")
    private Boolean wechatAccessCodeEnabled;

    @Schema(description = "公众号访问验证码。仅在启用公众号验证码时填写。", example = "yunyu-2026")
    @Size(max = 128, message = "公众号验证码长度不能超过128个字符")
    private String wechatAccessCode;

    @Schema(description = "公众号验证码提示文案。", example = "关注公众号后回复验证码获取访问权限")
    @Size(max = 255, message = "公众号验证码提示文案长度不能超过255个字符")
    private String wechatAccessCodeHint;

    @Schema(description = "公众号二维码图片地址。", example = "https://cdn.example.com/assets/wechat-qrcode.png")
    @Size(max = 255, message = "公众号二维码地址长度不能超过255个字符")
    private String wechatQrCodeUrl;
}
