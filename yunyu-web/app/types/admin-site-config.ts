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
  primaryColor: string
  secondaryColor: string
}

/**
 * 后台首页首屏统计项表单类型。
 * 作用：统一描述后台首页设置页中首屏统计项的可编辑结构。
 */
export interface AdminHomepageHeroStatForm {
  label: string
  value: string
}

/**
 * 后台首页配置表单类型。
 * 作用：统一描述后台首页首屏和首页模块开关相关的可编辑字段。
 */
export interface AdminHomepageConfigForm {
  heroEnabled: boolean
  heroLayout: 'brand'
  heroBackgroundMode: 'gradient-grid' | 'soft-glow' | 'minimal-lines' | 'keyword-cloud'
  heroEyebrow: string
  heroTitle: string
  heroSubtitle: string
  heroPrimaryButtonText: string
  heroPrimaryButtonLink: string
  heroSecondaryButtonText: string
  heroSecondaryButtonLink: string
  heroVisualPostId: number | null
  heroVisualClickable: boolean
  heroKeywords: string[]
  showHeroKeywords: boolean
  showHeroStats: boolean
  heroStats: AdminHomepageHeroStatForm[]
  showFeaturedSection: boolean
  featuredSectionTitle: string
  showLatestSection: boolean
  latestSectionTitle: string
  showCategorySection: boolean
  categorySectionTitle: string
  showTopicSection: boolean
  topicSectionTitle: string
}
