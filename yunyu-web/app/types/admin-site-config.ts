/**
 * 后台站点配置表单类型。
 * 作用：统一描述后台站点设置页需要编辑的全部字段，避免页面与接口层字段散落不一致。
 */
export interface AdminSiteConfigForm {
  siteName: string
  siteSubTitle: string
  footerText: string
  logoUrl: string
  faviconUrl: string
  defaultTitle: string
  defaultDescription: string
  defaultShareImage: string
  primaryColor: string
  secondaryColor: string
  homeStyle: string
}
