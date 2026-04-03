/**
 * 后台文章条目类型。
 * 作用：统一描述后台文章管理页中单篇文章的数据结构，供列表和表单复用。
 */
export interface AdminPostItem {
  id: number
  title: string
  slug: string
  summary: string | null
  coverUrl: string | null
  videoUrl: string | null
  categoryId?: number | null
  categoryName?: string | null
  tagIds?: number[]
  tagNames?: string[]
  topicIds?: number[]
  topicNames?: string[]
  topic: string
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  seoTitle?: string | null
  seoDescription?: string | null
  coverReady: boolean
  videoReady: boolean
  summaryReady: boolean
  readingMinutes: number
  wordCount: number
  contentMarkdown: string
  updatedAt: string
  publishedAt: string | null
}

/**
 * 后台文章列表响应类型。
 * 作用：匹配后端文章列表接口的返回结构，统一承接列表和分页信息。
 */
export interface AdminPostListResponse {
  list: AdminPostItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台文章查询参数类型。
 * 作用：统一描述文章列表的搜索、状态筛选和分页参数。
 */
export interface AdminPostQuery {
  keyword?: string
  status?: '' | 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  categoryId?: number
  tagId?: number
  topicId?: number
  pageNo?: number
  pageSize?: number
}

/**
 * 后台文章表单类型。
 * 作用：承接后台新增和编辑文章时提交的表单字段。
 */
export interface ArticleTocItem {
  id: string
  text: string
  level: number
}

/**
 * 后台文章表单类型。
 * 作用：承接后台新增和编辑文章时提交的主内容与派生内容字段。
 */
export interface AdminPostForm {
  title: string
  slug: string
  summary: string
  coverUrl: string
  videoUrl: string
  categoryId: number | null
  tagIds: number[]
  topicIds: number[]
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
  seoTitle: string
  seoDescription: string
  contentMarkdown: string
  contentHtml?: string
  contentToc?: ArticleTocItem[]
  contentTocJson?: string
}
