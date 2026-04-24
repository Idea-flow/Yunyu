/**
 * 内容访问规则类型。
 * 作用：统一约束文章访问控制和隐藏内容访问控制支持的规则标识。
 */
export type ContentAccessRuleType = 'LOGIN' | 'WECHAT_ACCESS_CODE' | 'ACCESS_CODE'

/**
 * 文章级访问控制配置类型。
 * 作用：描述整篇文章访问前的规则组合，以及文章独立访问码相关字段。
 */
export interface ContentAccessArticleConfig {
  enabled: boolean
  ruleTypes: ContentAccessRuleType[]
  articleAccessCode: string
  articleAccessCodeHint: string
}

/**
 * 尾部隐藏内容访问控制配置类型。
 * 作用：描述文章尾部隐藏内容模块的标题和访问规则组合。
 */
export interface ContentAccessTailHiddenConfig {
  enabled: boolean
  title: string
  ruleTypes: ContentAccessRuleType[]
}

/**
 * 统一内容访问控制配置类型。
 * 作用：承接后台文章编辑器和前台详情页共用的内容访问控制配置结构。
 */
export interface ContentAccessConfig {
  version: number
  articleAccess: ContentAccessArticleConfig
  tailHiddenAccess: ContentAccessTailHiddenConfig
}

/**
 * 前台内容访问状态类型。
 * 作用：描述当前访客在文章详情页下的访问状态和站点级公众号验证码配置。
 */
export interface SiteContentAccessState {
  loggedIn: boolean
  articleAccessAllowed: boolean
  tailHiddenAccessAllowed: boolean
  articleAccessRuleTypes: ContentAccessRuleType[]
  articleAccessPendingRuleTypes: ContentAccessRuleType[]
  tailHiddenAccessRuleTypes: ContentAccessRuleType[]
  tailHiddenAccessPendingRuleTypes: ContentAccessRuleType[]
  wechatAccessCodeEnabled: boolean
  wechatAccessCodeHint: string
  wechatQrCodeUrl: string
}

/**
 * 内容访问校验请求类型。
 * 作用：描述前台提交访问码或公众号验证码时需要携带的范围、规则和验证码字段。
 */
export interface SiteContentAccessVerifyRequest {
  scopeType: 'ARTICLE' | 'TAIL_HIDDEN'
  ruleType: 'ACCESS_CODE' | 'WECHAT_ACCESS_CODE'
  accessCode: string
}

/**
 * 内容访问校验响应类型。
 * 作用：承接服务端校验成功后返回的最新访问状态，便于前台即时刷新解锁结果。
 */
export interface SiteContentAccessVerifyResponse {
  granted: boolean
  contentAccessState: SiteContentAccessState
}

/**
 * 创建默认内容访问控制配置。
 * 作用：为后台表单和前台容错场景提供稳定的初始化结构。
 */
export function createDefaultContentAccessConfig(): ContentAccessConfig {
  return {
    version: 1,
    articleAccess: {
      enabled: false,
      ruleTypes: [],
      articleAccessCode: '',
      articleAccessCodeHint: ''
    },
    tailHiddenAccess: {
      enabled: false,
      title: '隐藏内容',
      ruleTypes: []
    }
  }
}
