import type { ArticleTocItem } from './post'

/**
 * 前台站点基础信息类型。
 * 作用：统一描述首页、详情页和布局头尾所需的站点配置字段。
 */
export interface SiteBaseInfo {
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

/**
 * 前台文章摘要类型。
 * 作用：统一描述首页卡片、列表页和相关推荐卡片的文章数据结构。
 */
export interface SiteTagLink {
  name: string
  slug: string
}

/**
 * 前台文章摘要类型。
 * 作用：统一描述首页卡片、列表页和相关推荐卡片的文章数据结构。
 */
export interface SiteTopicLink {
  name: string
  slug: string
}

/**
 * 前台文章摘要类型。
 * 作用：统一描述首页卡片、列表页和相关推荐卡片的文章数据结构。
 */
export interface SitePostSummary {
  id: number
  slug: string
  title: string
  summary: string
  coverUrl: string
  categoryName: string
  categorySlug: string
  tagItems: SiteTagLink[]
  tagNames: string[]
  topicItems: SiteTopicLink[]
  topicNames: string[]
  authorName: string
  authorAvatarUrl: string
  publishedAt: string
  readingMinutes: number
  viewCount: number
  likeCount: number
  top: boolean
  recommend: boolean
}

/**
 * 前台文章分页响应类型。
 * 作用：统一承接文章列表、分类页和专题页的分页结果。
 */
export interface SitePostListResponse {
  list: SitePostSummary[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 前台分类摘要类型。
 * 作用：统一描述首页和分类页的分类入口展示结构。
 */
export interface SiteCategoryItem {
  id: number
  name: string
  slug: string
  description: string
  coverUrl: string
  articleCount: number
}

/**
 * 前台专题摘要类型。
 * 作用：统一描述首页和专题页的专题入口展示结构。
 */
export interface SiteTopicItem {
  id: number
  name: string
  slug: string
  summary: string
  coverUrl: string
  articleCount: number
}

/**
 * 前台标签摘要类型。
 * 作用：统一描述标签列表页和标签详情页的标签展示结构。
 */
export interface SiteTagItem {
  id: number
  name: string
  slug: string
  description: string
  articleCount: number
}

/**
 * 前台首页聚合响应类型。
 * 作用：统一承接首页所需的站点信息、推荐文章、最新文章、分类和专题数据。
 */
export interface SiteHomeResponse {
  siteInfo: SiteBaseInfo
  featuredPosts: SitePostSummary[]
  latestPosts: SitePostSummary[]
  categories: SiteCategoryItem[]
  topics: SiteTopicItem[]
}

/**
 * 前台文章详情类型。
 * 作用：统一描述文章详情页正文、目录、SEO 与相关推荐所需字段。
 */
export interface SitePostDetail {
  id: number
  slug: string
  title: string
  summary: string
  coverUrl: string
  categoryName: string
  categorySlug: string
  tagItems: SiteTagLink[]
  tagNames: string[]
  topicItems: SiteTopicLink[]
  topicNames: string[]
  authorName: string
  authorAvatarUrl: string
  publishedAt: string
  readingMinutes: number
  viewCount: number
  likeCount: number
  commentCount: number
  seoTitle: string
  seoDescription: string
  contentMarkdown: string
  contentHtml: string
  contentTocJson: string
  allowComment: boolean
  relatedPosts: SitePostSummary[]
}

/**
 * 前台分类详情类型。
 * 作用：统一描述分类页顶部信息与该分类下文章分页结果。
 */
export interface SiteCategoryDetail {
  category: SiteCategoryItem
  posts: SitePostListResponse
}

/**
 * 前台专题详情类型。
 * 作用：统一描述专题页顶部信息与专题下文章分页结果。
 */
export interface SiteTopicDetail {
  topic: SiteTopicItem
  posts: SitePostListResponse
}

/**
 * 前台标签详情类型。
 * 作用：统一描述标签页顶部信息与该标签下文章分页结果。
 */
export interface SiteTagDetail {
  tag: SiteTagItem
  posts: SitePostListResponse
}

/**
 * 前台文章查询参数类型。
 * 作用：统一描述前台文章列表相关接口的搜索与分页参数。
 */
export interface SitePostQuery {
  keyword?: string
  categorySlug?: string
  topicSlug?: string
  tagSlug?: string
  pageNo?: number
  pageSize?: number
}

/**
 * 目录 JSON 解析结果类型。
 * 作用：让详情页把后端返回的目录 JSON 安全转换成前端目录树结构。
 */
export type SiteArticleToc = ArticleTocItem[]
