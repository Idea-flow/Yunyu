<script setup lang="ts">
import { computed } from 'vue'

/**
 * Markdown 预览组件。
 * 作用：统一承接后台文章编辑页的 HTML 预览展示，避免在页面中重复拼装预览样式。
 */
const props = withDefaults(defineProps<{
  html: string
  isLoading?: boolean
  emptyText?: string
  containerClass?: string
  bodyClass?: string
}>(), {
  isLoading: false,
  emptyText: '这里会显示正文预览，先在左侧输入 Markdown 内容。',
  containerClass: '',
  bodyClass: ''
})

const normalizedHtml = computed(() => props.html?.trim() || '')
</script>

<template>
  <div
    class="min-h-72 rounded-[24px] border border-slate-200/80 bg-white/78 p-6 shadow-[inset_0_1px_0_rgba(255,255,255,0.6)] dark:border-slate-700 dark:bg-slate-950/52"
    :class="props.containerClass"
  >
    <div v-if="props.isLoading" class="space-y-3">
      <USkeleton class="h-5 w-2/3 rounded-lg" />
      <USkeleton class="h-5 w-full rounded-lg" />
      <USkeleton class="h-5 w-5/6 rounded-lg" />
      <USkeleton class="h-32 w-full rounded-2xl" />
    </div>

    <div
      v-else-if="normalizedHtml"
      class="prose prose-slate max-w-none whitespace-normal leading-8 dark:prose-invert prose-headings:scroll-mt-24 prose-pre:overflow-x-auto prose-pre:rounded-[1.1rem] prose-pre:border prose-pre:border-slate-200/80 prose-pre:bg-slate-950 prose-pre:p-0 dark:prose-pre:border-slate-700 prose-code:before:hidden prose-code:after:hidden"
      :class="props.bodyClass"
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
        <div class="mt-5 flex flex-wrap justify-center gap-2">
          <span class="rounded-full border border-slate-200/90 bg-white px-3 py-1.5 text-xs font-medium text-slate-500 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300">支持标题与段落</span>
          <span class="rounded-full border border-slate-200/90 bg-white px-3 py-1.5 text-xs font-medium text-slate-500 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300">支持代码块高亮</span>
          <span class="rounded-full border border-slate-200/90 bg-white px-3 py-1.5 text-xs font-medium text-slate-500 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300">支持目录生成</span>
        </div>
      </div>
    </div>
  </div>
</template>
