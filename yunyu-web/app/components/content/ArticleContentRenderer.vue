<script setup lang="ts">
import { computed, createVNode, nextTick, onBeforeUnmount, onMounted, ref, render, resolveComponent, watch } from 'vue'

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
const NuxtIcon = resolveComponent('Icon')

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
 * 作用：将代码块中通过 `v-html` 注入的按钮占位节点，升级为真实的 Nuxt Icon 组件渲染结果。
 *
 * @param target 图标挂载目标节点
 * @param name 图标名称
 */
function mountActionIcon(target: HTMLElement, name: string) {
  render(createVNode(NuxtIcon, {
    name,
    class: 'yy-md-code-action-icon-glyph'
  }), target)

  cleanupCallbacks.push(() => {
    render(null, target)
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
  } catch (error) {
    console.error('[ArticleContentRenderer] Copy code failed.', error)
    button.setAttribute('aria-label', '复制失败')
    button.setAttribute('title', '复制失败')
  }

  window.setTimeout(() => {
    button.setAttribute('aria-label', originalLabel)
    button.setAttribute('title', originalLabel)
  }, 1600)
}

/**
 * 切换代码块折叠状态。
 * 作用：当代码内容超出阈值时，允许用户在收起和展开之间切换。
 *
 * @param block 代码块外层容器
 * @param button 折叠按钮
 */
function toggleCodeBlock(block: HTMLElement, button: HTMLButtonElement) {
  const isCollapsed = block.dataset.codeCollapsed !== 'false'
  block.dataset.codeCollapsed = isCollapsed ? 'false' : 'true'
  syncToggleButtonState(button, !isCollapsed)
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
    block.dataset.codeCollapsed = isOverflowing && !props.codeDefaultExpanded ? 'true' : 'false'

    if (!isOverflowing) {
      toggleButton.hidden = true
      continue
    }

    toggleButton.hidden = false
    syncToggleButtonState(toggleButton, props.codeDefaultExpanded)

    const handleToggle = () => {
      toggleCodeBlock(block, toggleButton)
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
  await nextTick()
  enhanceCodeBlocks()
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
