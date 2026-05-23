import { renderMarkdown } from '../../app/utils/markdown-renderer'

/**
 * Markdown 渲染接口。
 * 作用：接收 Markdown 正文，返回与后台编辑器完全一致的渲染结果（HTML、目录、纯文本、阅读时长），
 * 供 API 操作方（如 Agent）在调用后台创建/更新文章接口前获取完整的 contentHtml 和 contentTocJson。
 *
 * 请求方式: POST
 * 请求体: { markdown: string }
 * 响应体: { html: string, toc: ArticleTocItem[], tocJson: string, plainText: string, readingMinutes: number }
 *
 * 使用示例:
 *   POST /api/render-markdown
 *   { "markdown": "# 标题\n\n正文" }
 *
 *   → 响应:
 *   {
 *     "html": "<h1 id=\"标题\">标题</h1>\\n<p>正文</p>\\n",
 *     "toc": [{ "id": "标题", "text": "标题", "level": 1 }],
 *     "tocJson": "[{\"id\":\"标题\",\"text\":\"标题\",\"level\":1}]",
 *     "plainText": "标题 正文",
 *     "readingMinutes": 1
 *   }
 */
export default defineEventHandler(async (event) => {
  const body = await readBody<{ markdown?: string }>(event)

  if (typeof body?.markdown !== 'string') {
    throw createError({
      statusCode: 400,
      statusMessage: 'Bad Request',
      message: '请提供 markdown 字段（字符串类型）'
    })
  }

  const result = await renderMarkdown(body.markdown)

  return {
    html: result.html,
    toc: result.toc,
    tocJson: result.tocJson,
    plainText: result.plainText,
    readingMinutes: result.readingMinutes
  }
})
