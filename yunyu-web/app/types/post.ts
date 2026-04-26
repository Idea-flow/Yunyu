import type { ContentAccessConfig } from './content-access'

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
  isTop?: boolean
  isRecommend?: boolean
  allowComment?: boolean
  seoTitle?: string | null
  seoDescription?: string | null
  coverReady: boolean
  videoReady: boolean
  summaryReady: boolean
  readingMinutes: number
  wordCount: number
  contentMarkdown: string
  contentAccessConfig: ContentAccessConfig
  tailHiddenContentMarkdown: string | null
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
  isTop?: 0 | 1
  isRecommend?: 0 | 1
  allowComment?: 0 | 1
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
  isTop: boolean
  isRecommend: boolean
  allowComment: boolean
  seoTitle: string
  seoDescription: string
  contentMarkdown: string
  contentHtml?: string
  contentToc?: ArticleTocItem[]
  contentTocJson?: string
  contentAccessConfig: ContentAccessConfig
  tailHiddenContentMarkdown: string
  tailHiddenContentHtml?: string
}

/**
 * OpenAI Chat 消息内容片段类型。
 * 作用：兼容 OpenAI Chat 消息中数组内容的文本片段结构。
 */
export interface OpenAiChatMessageContentPart {
  type?: string
  text?: string
  [key: string]: any
}

/**
 * OpenAI Chat 消息类型。
 * 作用：描述 Chat 协议 messages 数组中的单条消息结构。
 */
export interface OpenAiChatMessage {
  role: string
  content: string | OpenAiChatMessageContentPart[]
  name?: string
  tool_call_id?: string
  [key: string]: any
}

/**
 * OpenAI Chat 请求类型。
 * 作用：承接 `/v1/chat/completions` 风格请求字段。
 */
export interface OpenAiChatCompletionRequest {
  model?: string
  messages: OpenAiChatMessage[]
  stream?: boolean
  temperature?: number
  max_tokens?: number
  [key: string]: any
}

/**
 * OpenAI Chat Choice 类型。
 * 作用：描述 Chat 响应 `choices` 数组中的单项结构。
 */
export interface OpenAiChatChoice {
  index: number
  finish_reason?: string | null
  message?: {
    role: string
    content?: string | OpenAiChatMessageContentPart[] | null
    [key: string]: any
  }
  delta?: {
    role?: string
    content?: string | OpenAiChatMessageContentPart[] | null
    [key: string]: any
  }
  [key: string]: any
}

/**
 * OpenAI Chat 响应类型。
 * 作用：承接 `/v1/chat/completions` 的非流式或流式分片数据。
 */
export interface OpenAiChatCompletionResponse {
  id?: string
  object?: string
  created?: number
  model?: string
  choices?: OpenAiChatChoice[]
  [key: string]: any
}

/**
 * 后台文章 AI 元信息生成请求类型。
 * 作用：约束文章元信息生成接口请求字段，直接传递标题、正文和少量可调参数。
 */
export interface AdminPostAiMetaGenerateRequest {
  title?: string
  contentMarkdown?: string
  stream?: boolean
  model?: string
  temperature?: number
  maxTokens?: number
}

/**
 * 后台文章 AI 元信息类型。
 * 作用：承接文章编辑页需要回填的 slug/摘要/SEO 字段。
 */
export interface AdminPostAiGeneratedMeta {
  slug: string
  summary: string
  seoTitle: string
  seoDescription: string
  slugCandidates?: string[]
}

/**
 * 后台文章 AI 流式生成选项。
 * 作用：支持在流式生成过程中订阅每个 OpenAI 分片与文本增量。
 */
export interface AdminPostAiMetaStreamOptions {
  onChunk?: (chunk: OpenAiChatCompletionResponse, deltaText: string) => void
  signal?: AbortSignal
}

/**
 * 后台文章 AI 流式生成结果。
 * 作用：返回最终拼接文本和原始分片，方便页面进行二次解析与回填。
 */
export interface AdminPostAiMetaStreamResult {
  fullText: string
  chunks: OpenAiChatCompletionResponse[]
}
