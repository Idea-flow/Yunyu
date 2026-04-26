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
  wechatAccessCodeEnabled: boolean
  wechatAccessCode: string
  wechatAccessCodeHint: string
  wechatQrCodeUrl: string
}

/**
 * 后台 S3 配置项表单类型。
 * 作用：统一描述后台站点设置页中单个 S3 配置项的字段结构。
 */
export interface AdminS3ProfileForm {
  profileKey: string
  name: string
  enabled: boolean
  endpoint: string
  region: string
  bucket: string
  accessKey: string
  secretKey: string
  pathStyleAccess: boolean
  publicBaseUrl: string
  presignExpireSeconds: number
  maxFileSizeMb: number
  allowedContentTypes: string[]
}

/**
 * 后台 S3 配置表单类型。
 * 作用：统一描述后台站点设置页中 S3 多配置集合和启用配置键。
 */
export interface AdminS3ConfigForm {
  activeProfileKey: string
  profiles: AdminS3ProfileForm[]
}

/**
 * 后台 S3 连接测试响应类型。
 * 作用：描述后台测试 S3 配置连接后的结果状态和提示信息。
 */
export interface AdminS3ConnectionTestResponse {
  success: boolean
  message: string
}

/**
 * 后台 AI 配置项表单类型。
 * 作用：统一描述后台站点设置页中单个 AI 提供商配置项字段。
 */
export interface AdminAiProviderProfileForm {
  profileKey: string
  name: string
  enabled: boolean
  upstreamBaseUrl: string
  apiKey: string
  model: string
  upstreamProtocol: 'COMPLETIONS' | 'RESPONSES'
  connectTimeoutMs: number
  readTimeoutMs: number
  writeTimeoutMs: number
  maxTokens: number
  temperature: number
}

/**
 * 后台 AI 配置表单类型。
 * 作用：统一描述后台 AI 多配置集合和当前启用配置键。
 */
export interface AdminAiProviderConfigForm {
  activeProfileKey: string
  profiles: AdminAiProviderProfileForm[]
}

/**
 * 后台 AI 连接测试响应类型。
 * 作用：描述后台测试 AI 配置连接后的结果状态和提示信息。
 */
export interface AdminAiProviderConnectionTestResponse {
  success: boolean
  message: string
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
