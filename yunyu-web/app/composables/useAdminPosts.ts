import { formatChineseDateTime } from '~/utils/date'
import type {
  AdminPostAiMetaGenerateRequest,
  AdminPostAiMetaStreamOptions,
  AdminPostAiMetaStreamResult,
  AdminPostForm,
  AdminPostItem,
  AdminPostListResponse,
  AdminPostQuery,
  OpenAiChatCompletionResponse
} from '../types/post'

/**
 * 转换后台文章条目。
 * 作用：统一格式化后台文章管理中可见的时间字段，避免页面层直接展示原始时间字符串。
 *
 * @param item 后端文章条目
 * @returns 前端可直接使用的文章条目
 */
function toAdminPostItem(item: AdminPostItem): AdminPostItem {
  return {
    ...item,
    updatedAt: formatChineseDateTime(item.updatedAt, '-'),
    publishedAt: item.publishedAt ? formatChineseDateTime(item.publishedAt, '-') : null
  }
}

/**
 * 提取 Chat 流式分片中的文本增量。
 * 作用：兼容 delta.content 为字符串或数组结构，统一返回本次分片新增文本。
 *
 * @param chunk OpenAI Chat 流式分片
 * @returns 当前分片增量文本
 */
function extractChatDeltaText(chunk: OpenAiChatCompletionResponse) {
  const firstChoice = Array.isArray(chunk?.choices) ? chunk.choices[0] : null
  const deltaContent = firstChoice?.delta?.content
  if (typeof deltaContent === 'string') {
    return deltaContent
  }

  if (Array.isArray(deltaContent)) {
    return deltaContent
      .map(item => {
        if (typeof item === 'string') {
          return item
        }
        return typeof item?.text === 'string' ? item.text : ''
      })
      .join('')
  }

  return ''
}

/**
 * 后台文章管理组合式函数。
 * 作用：统一封装后台文章增删改查接口，避免页面中散落接口路径和请求细节。
 */
export function useAdminPosts() {
  const apiClient = useApiClient()
  const runtimeConfig = useRuntimeConfig()

  /**
   * 查询后台文章列表。
   *
   * @param query 查询参数
   * @returns 文章列表响应
   */
  async function listPosts(query: AdminPostQuery = {}) {
    const response = await apiClient.request<AdminPostListResponse>('/api/admin/posts', {
      method: 'GET',
      query
    })

    return {
      ...response,
      list: response.list.map(item => toAdminPostItem(item))
    }
  }

  /**
   * 查询单篇文章详情。
   *
   * @param postId 文章ID
   * @returns 文章详情
   */
  async function getPost(postId: number) {
    const response = await apiClient.request<AdminPostItem>(`/api/admin/posts/${postId}`)
    return toAdminPostItem(response)
  }

  /**
   * 创建文章。
   *
   * @param payload 文章表单
   * @returns 创建后的文章
   */
  async function createPost(payload: AdminPostForm) {
    const response = await apiClient.request<AdminPostItem>('/api/admin/posts', {
      method: 'POST',
      body: payload
    })

    return toAdminPostItem(response)
  }

  /**
   * 更新文章。
   *
   * @param postId 文章ID
   * @param payload 文章表单
   * @returns 更新后的文章
   */
  async function updatePost(postId: number, payload: AdminPostForm) {
    const response = await apiClient.request<AdminPostItem>(`/api/admin/posts/${postId}`, {
      method: 'PUT',
      body: payload
    })

    return toAdminPostItem(response)
  }

  /**
   * 删除文章。
   *
   * @param postId 文章ID
   */
  async function deletePost(postId: number) {
    await apiClient.request<void>(`/api/admin/posts/${postId}`, {
      method: 'DELETE'
    })
  }

  /**
   * 非流式生成文章元信息。
   * 作用：调用后台文章 AI 元信息接口并返回 OpenAI Chat 风格响应体。
   *
   * @param payload OpenAI Chat 风格请求体
   * @returns OpenAI Chat 风格响应体
   */
  async function generatePostMeta(payload: AdminPostAiMetaGenerateRequest) {
    return await apiClient.rawRequest<OpenAiChatCompletionResponse>('/api/admin/posts/ai/meta/generate', {
      method: 'POST',
      body: {
        ...payload,
        stream: false
      }
    })
  }

  /**
   * 流式生成文章元信息。
   * 作用：通过原生 Fetch 读取 SSE 数据流，解析 OpenAI Chat 分片并回调增量文本。
   *
   * @param payload OpenAI Chat 风格请求体
   * @param options 流式回调与中断选项
   * @returns 流式聚合结果
   */
  async function streamGeneratePostMeta(
    payload: AdminPostAiMetaGenerateRequest,
    options: AdminPostAiMetaStreamOptions = {}
  ): Promise<AdminPostAiMetaStreamResult> {
    const authToken = apiClient.hydratePersistedAccessToken() || apiClient.accessToken.value || ''
    const response = await fetch(`${runtimeConfig.public.apiBase}/api/admin/posts/ai/meta/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(authToken ? { Authorization: `Bearer ${authToken}` } : {})
      },
      body: JSON.stringify({
        ...payload,
        stream: true
      }),
      signal: options.signal
    })

    if (!response.ok) {
      let message = `流式生成失败，HTTP 状态码：${response.status}`
      try {
        const errorBody = await response.json() as any
        message = errorBody?.error?.message || errorBody?.message || message
      } catch {
        // 忽略 JSON 解析失败，沿用默认提示。
      }
      throw new Error(message)
    }

    if (!response.body) {
      throw new Error('流式响应体为空，无法解析 AI 生成结果')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let fullText = ''
    const chunks: OpenAiChatCompletionResponse[] = []

    while (true) {
      const { value, done } = await reader.read()
      if (done) {
        break
      }

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split(/\r?\n/)
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) {
          continue
        }

        const dataLine = trimmedLine.startsWith('data:')
          ? trimmedLine.slice(5).trim()
          : trimmedLine

        if (!dataLine) {
          continue
        }

        if (dataLine === '[DONE]') {
          return {
            fullText,
            chunks
          }
        }

        let chunk: OpenAiChatCompletionResponse
        try {
          chunk = JSON.parse(dataLine) as OpenAiChatCompletionResponse
        } catch {
          continue
        }

        const errorMessage = (chunk as any)?.error?.message
        if (typeof errorMessage === 'string' && errorMessage.trim()) {
          throw new Error(errorMessage)
        }

        chunks.push(chunk)
        const deltaText = extractChatDeltaText(chunk)
        if (deltaText) {
          fullText += deltaText
        }
        options.onChunk?.(chunk, deltaText)
      }
    }

    buffer += decoder.decode()
    if (buffer.trim()) {
      const trailingLine = buffer.trim()
      const dataLine = trailingLine.startsWith('data:') ? trailingLine.slice(5).trim() : trailingLine
      if (dataLine && dataLine !== '[DONE]') {
        let trailingChunk: OpenAiChatCompletionResponse | null = null
        try {
          trailingChunk = JSON.parse(dataLine) as OpenAiChatCompletionResponse
        } catch {
          trailingChunk = null
        }

        if (trailingChunk) {
          const trailingErrorMessage = (trailingChunk as any)?.error?.message
          if (typeof trailingErrorMessage === 'string' && trailingErrorMessage.trim()) {
            throw new Error(trailingErrorMessage)
          }

          chunks.push(trailingChunk)
          const deltaText = extractChatDeltaText(trailingChunk)
          if (deltaText) {
            fullText += deltaText
          }
          options.onChunk?.(trailingChunk, deltaText)
        }
      }
    }

    return {
      fullText,
      chunks
    }
  }

  return {
    listPosts,
    getPost,
    createPost,
    updatePost,
    deletePost,
    generatePostMeta,
    streamGeneratePostMeta
  }
}
