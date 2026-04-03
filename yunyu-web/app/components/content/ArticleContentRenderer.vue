<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 文章内容渲染组件。
 * 作用：统一承接后台预览与前台正文的 HTML 展示能力，
 * 通过内容主题和代码主题控制标题、段落、链接、引用、代码块等元素的最终视觉效果。
 */
const props = withDefaults(defineProps<{
  html: string
  isLoading?: boolean
  emptyText?: string
  containerClass?: string
  bodyClass?: string
  contentTheme?: 'editorial' | 'documentation' | 'minimal'
  codeTheme?: 'github-light' | 'github-dark' | 'vitesse-light' | 'vitesse-dark'
  codeDefaultExpanded?: boolean
}>(), {
  isLoading: false,
  emptyText: '这里会显示正文预览，先在左侧输入 Markdown 内容。',
  containerClass: '',
  bodyClass: '',
  contentTheme: 'editorial',
  codeTheme: 'github-light',
  codeDefaultExpanded: false
})

const normalizedHtml = computed(() => props.html?.trim() || '')
const hasContent = computed(() => normalizedHtml.value.length > 0)
const containerRef = ref<HTMLElement | null>(null)
const cleanupCallbacks: Array<() => void> = []
const collapseHeight = 152
const iframeWarningDelay = 5200
const yunyuToast = useYunyuToast()
const actionIconMap: Record<string, string> = {
  'lucide:copy': `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <rect x="9" y="9" width="13" height="13" rx="2"></rect>
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
    </svg>
  `,
  'lucide:chevron-down': `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="m6 9 6 6 6-6"></path>
    </svg>
  `,
  'lucide:chevron-up': `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="m18 15-6-6-6 6"></path>
    </svg>
  `
}

/**
 * 清理已注册的代码块事件。
 * 作用：在内容重新渲染或组件卸载时移除旧按钮监听，避免重复绑定。
 */
function cleanupCodeEnhancements() {
  while (cleanupCallbacks.length) {
    cleanupCallbacks.pop()?.()
  }
}

/**
 * 挂载代码块工具条图标。
 * 作用：为通过 `v-html` 注入的按钮占位节点填充静态 SVG，
 * 避免在 SSR 和 hydration 阶段依赖运行时组件解析造成结构不一致。
 *
 * @param target 图标挂载目标节点
 * @param name 图标名称
 */
function mountActionIcon(target: HTMLElement, name: string) {
  target.innerHTML = actionIconMap[name] || ''
  target.classList.add('yy-md-code-action-icon-glyph')

  cleanupCallbacks.push(() => {
    target.innerHTML = ''
    target.classList.remove('yy-md-code-action-icon-glyph')
  })
}

/**
 * 复制指定代码块内容。
 * 作用：为代码块头部的复制按钮提供复制能力，并在成功后反馈按钮状态。
 *
 * @param text 代码文本
 * @param button 当前触发的按钮元素
 */
async function copyCode(text: string, button: HTMLButtonElement) {
  const originalLabel = button.getAttribute('aria-label') || '复制代码'

  try {
    await navigator.clipboard.writeText(text)
    button.setAttribute('aria-label', '复制成功')
    button.setAttribute('title', '复制成功')
    yunyuToast.success('代码已复制', '可以直接粘贴到编辑器或终端中使用。')
  } catch (error) {
    console.error('[ArticleContentRenderer] Copy code failed.', error)
    button.setAttribute('aria-label', '复制失败')
    button.setAttribute('title', '复制失败')
    yunyuToast.error('复制失败', '当前环境暂时无法访问剪贴板，请手动复制代码。')
  }

  window.setTimeout(() => {
    button.setAttribute('aria-label', originalLabel)
    button.setAttribute('title', originalLabel)
  }, 1600)
}

/**
 * 设置代码块内容容器高度。
 * 作用：统一维护代码块当前展示高度，避免展开和收起时直接切换状态造成闪烁。
 *
 * @param codeBody 代码块内容容器
 * @param height 目标高度
 */
function setCodeBodyHeight(codeBody: HTMLElement, height: number | 'none') {
  codeBody.style.maxHeight = height === 'none' ? 'none' : `${Math.max(height, collapseHeight)}px`
}

/**
 * 同步代码块高度状态。
 * 作用：在初始化或切换折叠状态时，根据真实内容高度驱动平滑过渡。
 *
 * @param codeBody 代码块内容容器
 * @param isExpanded 当前是否展开
 * @param animate 是否启用动画
 */
function syncCodeBodyHeight(codeBody: HTMLElement, isExpanded: boolean, animate: boolean) {
  const fullHeight = codeBody.scrollHeight

  if (!animate) {
    setCodeBodyHeight(codeBody, isExpanded ? 'none' : collapseHeight)
    return
  }

  const startHeight = Math.max(codeBody.getBoundingClientRect().height, collapseHeight)
  setCodeBodyHeight(codeBody, startHeight)

  requestAnimationFrame(() => {
    setCodeBodyHeight(codeBody, isExpanded ? fullHeight : collapseHeight)
  })

  const handleTransitionEnd = (event: TransitionEvent) => {
    if (event.propertyName !== 'max-height') {
      return
    }

    codeBody.removeEventListener('transitionend', handleTransitionEnd)
    setCodeBodyHeight(codeBody, isExpanded ? 'none' : collapseHeight)
  }

  codeBody.addEventListener('transitionend', handleTransitionEnd)
  cleanupCallbacks.push(() => codeBody.removeEventListener('transitionend', handleTransitionEnd))
}

/**
 * 切换代码块折叠状态。
 * 作用：当代码内容超出阈值时，允许用户在收起和展开之间切换。
 *
 * @param block 代码块外层容器
 * @param codeBody 代码块内容容器
 * @param button 折叠按钮
 */
function toggleCodeBlock(block: HTMLElement, codeBody: HTMLElement, button: HTMLButtonElement) {
  const isCollapsed = block.dataset.codeCollapsed !== 'false'
  const isExpanded = isCollapsed
  block.dataset.codeCollapsed = isExpanded ? 'false' : 'true'
  syncToggleButtonState(button, isExpanded)
  syncCodeBodyHeight(codeBody, isExpanded, true)
}

/**
 * 同步折叠按钮状态。
 * 作用：根据代码块当前是否展开，统一更新按钮图标语义与悬浮提示。
 *
 * @param button 折叠按钮
 * @param isExpanded 当前是否展开
 */
function syncToggleButtonState(button: HTMLButtonElement, isExpanded: boolean) {
  const label = isExpanded ? '收起代码' : '展开代码'
  const iconHost = button.querySelector<HTMLElement>('[data-code-icon-host]')
  button.dataset.state = isExpanded ? 'expanded' : 'collapsed'
  button.setAttribute('aria-label', label)
  button.setAttribute('title', label)

  if (iconHost) {
    mountActionIcon(iconHost, isExpanded ? 'lucide:chevron-up' : 'lucide:chevron-down')
  }
}

/**
 * 确保代码块工具条具备窗口控制点。
 * 作用：兼容历史文章已存储的旧版 HTML，在前台渲染时自动补齐 macOS 风格的三色圆点。
 *
 * @param block 代码块外层容器
 */
function ensureWindowControls(block: HTMLElement) {
  const toolbarMeta = block.querySelector<HTMLElement>('.yy-md-code-toolbar-meta')

  if (!toolbarMeta || toolbarMeta.querySelector('.yy-md-code-window-controls')) {
    return
  }

  const controls = document.createElement('span')
  controls.className = 'yy-md-code-window-controls'
  controls.setAttribute('aria-hidden', 'true')
  controls.innerHTML = `
    <span class="yy-md-code-window-dot yy-md-code-window-dot-close"></span>
    <span class="yy-md-code-window-dot yy-md-code-window-dot-minimize"></span>
    <span class="yy-md-code-window-dot yy-md-code-window-dot-expand"></span>
  `.trim()

  toolbarMeta.insertBefore(controls, toolbarMeta.firstChild)
}

/**
 * 创建 iframe 加载异常提示节点。
 * 作用：当第三方嵌入长时间空白时，给用户一个明确反馈，
 * 提示可能是目标站禁止嵌入或当前网络不可达，并提供源链接直达验证。
 *
 * @param src iframe 源地址
 * @returns 可插入 DOM 的提示节点
 */
function createIframeFallbackNotice(src: string) {
  const notice = document.createElement('div')
  notice.className = 'mt-3 rounded-[16px] border border-amber-200/80 bg-amber-50/88 px-4 py-3 text-sm text-amber-900 shadow-[0_16px_30px_-26px_rgba(217,119,6,0.35)] dark:border-amber-400/20 dark:bg-amber-400/10 dark:text-amber-100'
  notice.hidden = true

  const title = document.createElement('p')
  title.className = 'font-semibold tracking-[-0.01em]'
  title.textContent = '嵌入内容加载异常'

  const description = document.createElement('p')
  description.className = 'mt-1 leading-6 text-amber-800/90 dark:text-amber-100/80'
  description.textContent = '该区域长时间未正常显示，可能是目标站禁止嵌入，或当前网络暂时不可达。'

  notice.appendChild(title)
  notice.appendChild(description)

  if (src) {
    const actionRow = document.createElement('div')
    actionRow.className = 'mt-2 flex flex-wrap items-center gap-2'

    const link = document.createElement('a')
    link.className = 'inline-flex items-center rounded-full border border-amber-300/80 bg-white/88 px-3 py-1.5 text-xs font-semibold text-amber-900 transition duration-200 hover:-translate-y-0.5 hover:border-amber-400 hover:bg-white dark:border-amber-300/20 dark:bg-slate-950/55 dark:text-amber-100 dark:hover:border-amber-300/35'
    link.href = src
    link.target = '_blank'
    link.rel = 'noopener noreferrer'
    link.textContent = '打开源地址验证'

    actionRow.appendChild(link)
    notice.appendChild(actionRow)
  }

  return notice
}

/**
 * 增强 iframe 嵌入反馈。
 * 作用：为正文中的第三方嵌入补充超时提示，
 * 当播放器或页面长期空白时，提醒用户排查网络或目标站嵌入策略。
 */
function enhanceEmbeddedIframes() {
  if (!containerRef.value) {
    return
  }

  const iframes = containerRef.value.querySelectorAll<HTMLIFrameElement>('iframe')

  for (const iframe of iframes) {
    const src = iframe.getAttribute('src')?.trim() || ''
    const notice = createIframeFallbackNotice(src)
    let resolved = false

    iframe.insertAdjacentElement('afterend', notice)

    const revealNotice = () => {
      if (resolved) {
        return
      }

      notice.hidden = false
    }

    const hideNotice = () => {
      resolved = true
      notice.hidden = true
    }

    const handleLoad = () => {
      hideNotice()
    }

    const handleError = () => {
      revealNotice()
    }

    const warningTimer = window.setTimeout(() => {
      revealNotice()
    }, iframeWarningDelay)

    iframe.addEventListener('load', handleLoad)
    iframe.addEventListener('error', handleError)

    cleanupCallbacks.push(() => {
      window.clearTimeout(warningTimer)
      iframe.removeEventListener('load', handleLoad)
      iframe.removeEventListener('error', handleError)
      notice.remove()
    })
  }
}

/**
 * 增强代码块交互。
 * 作用：为通过 `v-html` 注入的代码块补充复制按钮、折叠能力和运行时状态。
 */
function enhanceCodeBlocks() {
  cleanupCodeEnhancements()

  if (!containerRef.value) {
    return
  }

  const codeBlocks = containerRef.value.querySelectorAll<HTMLElement>('.yy-md-code-block')

  for (const block of codeBlocks) {
    ensureWindowControls(block)

    const codeBody = block.querySelector<HTMLElement>('.yy-md-code-body')
    const shikiCode = block.querySelector<HTMLElement>('.shiki code')
    const copyButton = block.querySelector<HTMLButtonElement>('[data-code-copy]')
    const toggleButton = block.querySelector<HTMLButtonElement>('[data-code-toggle]')
    const copyIconHost = copyButton?.querySelector<HTMLElement>('[data-code-icon-host]')

    if (copyIconHost) {
      mountActionIcon(copyIconHost, 'lucide:copy')
    }

    if (copyButton && shikiCode) {
      const handleCopy = async () => {
        await copyCode(shikiCode.textContent || '', copyButton)
      }

      copyButton.addEventListener('click', handleCopy)
      cleanupCallbacks.push(() => copyButton.removeEventListener('click', handleCopy))
    }

    if (!codeBody || !toggleButton) {
      continue
    }

    const isOverflowing = codeBody.scrollHeight > collapseHeight
    block.dataset.codeOverflowing = isOverflowing ? 'true' : 'false'
    block.dataset.codeCollapsed = isOverflowing && !props.codeDefaultExpanded ? 'true' : 'false'

    if (!isOverflowing) {
      setCodeBodyHeight(codeBody, 'none')
      toggleButton.hidden = true
      continue
    }

    toggleButton.hidden = false
    syncToggleButtonState(toggleButton, props.codeDefaultExpanded)
    syncCodeBodyHeight(codeBody, props.codeDefaultExpanded, false)

    const handleToggle = () => {
      toggleCodeBlock(block, codeBody, toggleButton)
    }

    toggleButton.addEventListener('click', handleToggle)
    cleanupCallbacks.push(() => toggleButton.removeEventListener('click', handleToggle))
  }
}

/**
 * 刷新正文运行时增强效果。
 * 作用：在 HTML 内容或加载状态变化后，等待 DOM 更新完成再增强代码块交互。
 */
async function refreshEnhancements() {
  if (import.meta.server) {
    return
  }

  await nextTick()
  enhanceCodeBlocks()
  enhanceEmbeddedIframes()
}

watch(() => [props.html, props.isLoading, props.codeDefaultExpanded] as const, async () => {
  await refreshEnhancements()
})

onMounted(async () => {
  await refreshEnhancements()
})

onBeforeUnmount(() => {
  cleanupCodeEnhancements()
})
</script>

<template>
  <div
    class="yy-md-shell min-h-72 min-w-0 overflow-hidden rounded-[24px] border border-slate-200/80 bg-white/78 p-6 shadow-[inset_0_1px_0_rgba(255,255,255,0.6)] dark:border-slate-700 dark:bg-slate-950/52"
    :class="props.containerClass"
  >
    <div v-if="props.isLoading" class="space-y-3">
      <USkeleton class="h-5 w-2/3 rounded-lg" />
      <USkeleton class="h-5 w-full rounded-lg" />
      <USkeleton class="h-5 w-5/6 rounded-lg" />
      <USkeleton class="h-32 w-full rounded-2xl" />
    </div>

    <article
      v-else-if="hasContent"
      ref="containerRef"
      class="yy-md min-w-0 max-w-none"
      :class="props.bodyClass"
      :data-content-theme="props.contentTheme"
      :data-code-theme="props.codeTheme"
      v-html="normalizedHtml"
    />

    <div
      v-else
      class="flex h-full min-h-60 items-center justify-center"
      :class="props.bodyClass"
    >
      <div class="flex max-w-sm flex-col items-center text-center">
        <div class="flex h-16 w-16 items-center justify-center rounded-[1.4rem] border border-slate-200/90 bg-slate-50 text-slate-500 shadow-[0_16px_34px_-26px_rgba(15,23,42,0.35)] dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300">
          <UIcon name="i-lucide-file-code-2" class="size-7" />
        </div>
        <p class="mt-5 text-base font-semibold text-slate-800 dark:text-slate-100">预览区等待渲染</p>
        <p class="mt-2 text-sm leading-7 text-slate-500 dark:text-slate-400">
          {{ props.emptyText }}
        </p>
      </div>
    </div>
  </div>
</template>
