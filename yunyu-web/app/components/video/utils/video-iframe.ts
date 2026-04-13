import type { VideoSourceItem } from '../player.types'

/**
 * 这个接口负责描述 iframe 渲染所需的可控属性，
 * 让播放器与详情页都能复用同一套解析结果。
 */
export interface ParsedIframeAttributes {
  src: string
  title?: string
  allow?: string
  referrerpolicy?: HTMLAttributeReferrerPolicy
  sandbox?: string
  frameborder?: string
  scrolling?: string
  loading?: 'lazy' | 'eager'
  allowfullscreen?: boolean
}

/**
 * 从 iframe HTML 片段中提取指定属性值，
 * 统一兼容单引号、双引号与属性名前后空白。
 */
function extractIframeAttribute(html: string, attributeName: string) {
  const matcher = new RegExp(`${attributeName}\\s*=\\s*(['"])(.*?)\\1`, 'i')
  const match = html.match(matcher)

  return match?.[2]?.trim() || ''
}

/**
 * 将完整 iframe HTML 解析成安全可控的属性对象，
 * 避免直接把任意 HTML 原样注入到页面里。
 */
export function parseIframeAttributes(iframeHtml: string): ParsedIframeAttributes | null {
  const normalizedHtml = iframeHtml.trim()

  if (!/^<iframe[\s\S]*<\/iframe>$/i.test(normalizedHtml)) {
    return null
  }

  const openingTagMatch = normalizedHtml.match(/<iframe\b[^>]*>/i)

  if (!openingTagMatch) {
    return null
  }

  const openingTag = openingTagMatch[0]
  const src = extractIframeAttribute(openingTag, 'src')

  if (!src) {
    return null
  }

  const loadingValue = extractIframeAttribute(openingTag, 'loading').toLowerCase()
  const referrerPolicyValue = extractIframeAttribute(openingTag, 'referrerpolicy')

  return {
    src,
    title: extractIframeAttribute(openingTag, 'title') || undefined,
    allow: extractIframeAttribute(openingTag, 'allow') || undefined,
    referrerpolicy: referrerPolicyValue
      ? referrerPolicyValue as HTMLAttributeReferrerPolicy
      : undefined,
    sandbox: extractIframeAttribute(openingTag, 'sandbox') || undefined,
    frameborder: extractIframeAttribute(openingTag, 'frameborder') || undefined,
    scrolling: extractIframeAttribute(openingTag, 'scrolling') || undefined,
    loading: loadingValue === 'eager' ? 'eager' : loadingValue === 'lazy' ? 'lazy' : undefined,
    allowfullscreen: /\ballowfullscreen\b/i.test(openingTag)
  }
}

/**
 * 根据播放源生成 iframe 渲染配置，
 * 普通 iframe 只使用 src，iframeFull 则会解析完整嵌入参数。
 */
export function buildIframeRenderAttributes(source: Pick<VideoSourceItem, 'sourceType' | 'sourceUrl' | 'label'> | null | undefined) {
  if (!source) {
    return null
  }

  if (source.sourceType === 'iframe') {
    return {
      src: source.sourceUrl,
      title: source.label || '嵌入视频播放器'
    } satisfies ParsedIframeAttributes
  }

  if (source.sourceType === 'iframeFull') {
    return parseIframeAttributes(source.sourceUrl)
  }

  return null
}

/**
 * 提取可用于 SEO `embedUrl` 的真实 iframe 地址，
 * 避免 `iframeFull` 场景把整段 HTML 写进结构化数据。
 */
export function extractEmbedUrl(source: Pick<VideoSourceItem, 'sourceType' | 'sourceUrl' | 'label'> | null | undefined) {
  return buildIframeRenderAttributes(source)?.src || undefined
}
