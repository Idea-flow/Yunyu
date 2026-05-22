import { renderMarkdown } from '../utils/markdown-renderer'
import type { MarkdownRenderResult } from '../utils/markdown-renderer'
import type { Ref } from 'vue'
import type { ArticleTocItem } from '../types/post'

/**
 * Markdown 渲染组合式函数。
 * 作用：监听文章编辑器中的 Markdown 内容，并实时输出预览和目录数据。
 * 底层渲染逻辑复用 app/utils/markdown-renderer.ts 中的纯函数实现，
 * 确保编辑器预览与服务端渲染结果一致。
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
