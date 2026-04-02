import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import { getSingletonHighlighter } from 'shiki'
import type { Ref } from 'vue'
import type { ArticleTocItem } from '../types/post'

/**
 * Markdown 渲染结果类型。
 * 作用：统一承接文章编辑页预览所需的 HTML、目录、纯文本和阅读时长信息。
 */
interface MarkdownRenderResult {
  html: string
  toc: ArticleTocItem[]
  plainText: string
  readingMinutes: number
}

/**
 * 代码块渲染元信息类型。
 * 作用：统一描述单个 Markdown 代码块的语言、展示标签和折叠能力，供 HTML 组装时复用。
 */
interface CodeBlockRenderMeta {
  language: string
  displayLanguage: string
  isCollapsible: boolean
}

const markdownRenderer = createMarkdownRenderer()
const shikiHighlighterPromise = getSingletonHighlighter({
  themes: ['github-light', 'github-dark-default'],
  langs: [
    'plaintext',
    'text',
    'bash',
    'shell',
    'json',
    'javascript',
    'typescript',
    'ts',
    'js',
    'jsx',
    'tsx',
    'java',
    'vue',
    'xml',
    'html',
    'css',
    'scss',
    'sql',
    'yaml',
    'yml',
    'markdown'
  ]
})

/**
 * 创建 Markdown 解析器。
 * 作用：为后台文章编辑页提供统一的 Markdown 解析、标题锚点和基础排版能力。
 */
function createMarkdownRenderer() {
  return new MarkdownIt({
    html: false,
    linkify: true,
    breaks: true
  }).use(markdownItAnchor, {
    slugify: createSlugifyFactory(),
    permalink: (markdownItAnchor as any).permalink.linkInsideHeader({
      symbol: '#',
      placement: 'after',
      class: 'yy-md-heading-anchor',
      assistiveText: '复制标题链接',
      visuallyHiddenClass: 'yy-md-visually-hidden'
    })
  })
}

/**
 * 创建标题 slug 生成器。
 * 作用：为标题锚点和目录项生成稳定、可重复的唯一标识。
 */
function createSlugifyFactory() {
  const counters = new Map<string, number>()

  return (value: string) => {
    const normalized = value
      .normalize('NFKD')
      .toLowerCase()
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^\p{Letter}\p{Number}\s-]/gu, '')
      .trim()
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-')
      .replace(/^-|-$/g, '')

    const base = normalized || 'section'
    const current = counters.get(base) || 0
    counters.set(base, current + 1)
    return current === 0 ? base : `${base}-${current + 1}`
  }
}

/**
 * 提取目录数据。
 * 作用：从 Markdown token 中读取标题层级，生成前端与后端共用的目录结构。
 *
 * @param tokens Markdown token 列表
 * @returns 目录项列表
 */
function extractToc(tokens: any[]): ArticleTocItem[] {
  const toc: ArticleTocItem[] = []

  for (let index = 0; index < tokens.length; index += 1) {
    const token = tokens[index]

    if (!token || token.type !== 'heading_open') {
      continue
    }

    const level = Number(token.tag?.replace('h', '') || 0)
    const nextToken = tokens[index + 1]
    const text = nextToken?.content?.trim()

    if (!text || level < 1 || level > 6) {
      continue
    }

    const attrs = typeof token.attrGet === 'function' ? token.attrGet('id') : null
    toc.push({
      id: attrs || 'section',
      text,
      level
    })
  }

  return toc
}

/**
 * 计算纯文本内容。
 * 作用：用于阅读时长与字数统计，避免 HTML 标签影响统计结果。
 *
 * @param markdown Markdown 内容
 * @returns 纯文本内容
 */
function extractPlainText(markdown: string) {
  return markdown
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*]\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*]\([^)]*\)/g, ' ')
    .replace(/[#>*_\-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * 转义 HTML 属性值。
 * 作用：避免代码语言名等动态文本插入 HTML 属性时破坏结构。
 *
 * @param value 原始文本
 * @returns 可安全写入 HTML 属性的文本
 */
function escapeHtmlAttribute(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/"/g, '&quot;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

/**
 * 解析代码块语言信息。
 * 作用：从 Markdown fence 信息中提取语言名，并生成更适合展示的标签文本。
 *
 * @param info fence info 原文
 * @returns 代码块元信息
 */
function resolveCodeBlockMeta(info: string | undefined): CodeBlockRenderMeta {
  const rawLanguage = info?.trim().split(/\s+/)[0] || 'plaintext'
  const normalizedLanguage = rawLanguage.toLowerCase()
  const displayMap: Record<string, string> = {
    ts: 'TypeScript',
    js: 'JavaScript',
    tsx: 'TSX',
    jsx: 'JSX',
    bash: 'Bash',
    shell: 'Shell',
    yml: 'YAML',
    md: 'Markdown',
    vue: 'Vue',
    html: 'HTML',
    css: 'CSS',
    scss: 'SCSS',
    sql: 'SQL',
    xml: 'XML',
    json: 'JSON',
    java: 'Java',
    plaintext: 'Plain Text',
    text: 'Plain Text'
  }

  return {
    language: normalizedLanguage,
    displayLanguage: displayMap[normalizedLanguage] || rawLanguage.toUpperCase(),
    isCollapsible: true
  }
}

/**
 * 将 Markdown token 渲染为带代码高亮的 HTML。
 * 作用：让代码块通过 Shiki 输出更高质量的高亮结果。
 *
 * @param tokens Markdown token 列表
 * @returns HTML 字符串
 */
async function renderTokensToHtml(tokens: any[]) {
  const highlighter = await shikiHighlighterPromise
  const fenceHtmlByIndex = new Map<number, string>()

  await Promise.all(tokens.map(async (token, index) => {
    if (!token || token.type !== 'fence') {
      return
    }

    const meta = resolveCodeBlockMeta(token.info)
    const code = token.content || ''

    let renderedHtml = ''

    try {
      renderedHtml = await highlighter.codeToHtml(code, {
        lang: meta.language,
        themes: {
          light: 'github-light',
          dark: 'github-dark-default'
        },
        defaultColor: false
      })
    } catch (error) {
      console.warn(`[MarkdownRenderer] Shiki language "${meta.language}" not found, fallback to plaintext.`, error)
      renderedHtml = await highlighter.codeToHtml(code, {
        lang: 'plaintext',
        themes: {
          light: 'github-light',
          dark: 'github-dark-default'
        },
        defaultColor: false
      })
    }

    const languageAttr = escapeHtmlAttribute(meta.language)
    const displayLanguageAttr = escapeHtmlAttribute(meta.displayLanguage)

    fenceHtmlByIndex.set(index, `
<div
  class="yy-md-code-block"
  data-code-language="${languageAttr}"
  data-code-language-label="${displayLanguageAttr}"
  data-code-collapsible="${meta.isCollapsible ? 'true' : 'false'}"
>
  <div class="yy-md-code-toolbar">
    <div class="yy-md-code-toolbar-meta">
      <span class="yy-md-code-window-controls" aria-hidden="true">
        <span class="yy-md-code-window-dot yy-md-code-window-dot-close"></span>
        <span class="yy-md-code-window-dot yy-md-code-window-dot-minimize"></span>
        <span class="yy-md-code-window-dot yy-md-code-window-dot-expand"></span>
      </span>
      <span class="yy-md-code-language-pill">${displayLanguageAttr}</span>
    </div>
    <div class="yy-md-code-toolbar-actions">
      <button type="button" class="yy-md-code-action yy-md-code-action-icon" data-code-toggle data-state="collapsed" aria-label="展开代码" title="展开代码" hidden>
        <span data-code-icon-host aria-hidden="true"></span>
      </button>
      <button type="button" class="yy-md-code-action yy-md-code-action-icon" data-code-copy aria-label="复制代码" title="复制代码">
        <span data-code-icon-host aria-hidden="true"></span>
      </button>
    </div>
  </div>
  <div class="yy-md-code-body">
    ${renderedHtml}
  </div>
</div>`.trim())
  }))

  const originalFenceRule = markdownRenderer.renderer.rules.fence
  markdownRenderer.renderer.rules.fence = (tokens, index, options, env, self) => {
    return fenceHtmlByIndex.get(index) || originalFenceRule?.(tokens, index, options, env, self) || self.renderToken(tokens, index, options)
  }

  try {
    return markdownRenderer.renderer.render(tokens, markdownRenderer.options, {})
  } finally {
    markdownRenderer.renderer.rules.fence = originalFenceRule
  }
}

/**
 * 渲染 Markdown 内容。
 * 作用：统一生成文章编辑页预览所需的 HTML、目录、纯文本与阅读时长。
 *
 * @param markdown Markdown 内容
 * @returns 渲染结果
 */
async function renderMarkdown(markdown: string): Promise<MarkdownRenderResult> {
  const source = markdown.trim()

  if (!source) {
    return {
      html: '',
      toc: [],
      plainText: '',
      readingMinutes: 1
    }
  }

  const tokens = markdownRenderer.parse(source, {})
  const toc = extractToc(tokens)
  const html = await renderTokensToHtml(tokens)
  const plainText = extractPlainText(source)
  const readingMinutes = Math.max(1, Math.ceil(plainText.length / 500))

  return {
    html,
    toc,
    plainText,
    readingMinutes
  }
}

/**
 * Markdown 渲染组合式函数。
 * 作用：监听文章编辑器中的 Markdown 内容，并实时输出预览和目录数据。
 *
 * @param markdownRef Markdown 响应式引用
 * @returns 渲染状态
 */
export function useMarkdownRenderer(markdownRef: Ref<string>) {
  const html = ref('')
  const toc = ref<ArticleTocItem[]>([])
  const plainText = ref('')
  const readingMinutes = ref(1)
  const isRendering = ref(false)

  /**
   * 刷新渲染结果。
   * 作用：在正文内容变更后重新计算 HTML、目录、纯文本和阅读时长。
   */
  async function refresh() {
    isRendering.value = true

    try {
      const result = await renderMarkdown(markdownRef.value || '')
      html.value = result.html
      toc.value = result.toc
      plainText.value = result.plainText
      readingMinutes.value = result.readingMinutes
    } catch (error) {
      console.error('[MarkdownRenderer] Markdown render failed.', error)
      html.value = ''
      toc.value = []
      plainText.value = ''
      readingMinutes.value = 1
    } finally {
      isRendering.value = false
    }
  }

  watch(markdownRef, async () => {
    await refresh()
  }, { immediate: true })

  return {
    html,
    toc,
    plainText,
    readingMinutes,
    isRendering,
    refresh
  }
}
