<script setup lang="ts">
import { computed, nextTick } from 'vue'
import MarkdownPreview from './MarkdownPreview.vue'
import ArticleTocTree from '../../content/ArticleTocTree.vue'
import type { ArticleTocItem } from '../../../types/post'

/**
 * 后台 Markdown 工作台组件。
 * 作用：为文章编辑页提供统一的 Markdown 输入与 HTML 实时预览工作台，
 * 让正文编辑具备整页级双栏体验、固定高度和独立滚动能力。
 */
const props = withDefaults(defineProps<{
  modelValue?: string
  html: string
  toc?: ArticleTocItem[]
  plainText?: string
  isLoading?: boolean
  contentLength?: number
  readingMinutes?: number
  tocCount?: number
}>(), {
  modelValue: '',
  toc: () => [],
  plainText: '',
  isLoading: false,
  contentLength: 0,
  readingMinutes: 1,
  tocCount: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const viewMode = ref<'edit' | 'preview'>('edit')
const previewPanelTab = ref<'finalPreview' | 'html' | 'toc' | 'plainText' | 'stats'>('finalPreview')
const isPreviewModalOpen = ref(false)
const activeTocId = ref('')
const finalPreviewPanelRef = ref<HTMLElement | null>(null)
const previewContentTheme = ref<'editorial' | 'documentation' | 'minimal'>('editorial')
const previewCodeTheme = ref<'github-light' | 'github-dark' | 'vitesse-light' | 'vitesse-dark'>('github-light')
const editorTools = [
  { label: '标题', hint: '# / ## / ###' },
  { label: '强调', hint: '**加粗**' },
  { label: '链接', hint: '[文本](url)' },
  { label: '代码', hint: '```language' }
]
const contentThemeOptions = [
  { value: 'editorial', label: '杂志感', hint: '阅读氛围更强' },
  { value: 'documentation', label: '文档感', hint: '结构更规整' },
  { value: 'minimal', label: '极简', hint: '更轻更紧凑' }
] as const
const codeThemeOptions = [
  { value: 'github-light', label: 'GitHub Light', hint: '浅色稳定' },
  { value: 'github-dark', label: 'GitHub Dark', hint: '深色稳定' },
  { value: 'vitesse-light', label: 'Vitesse Light', hint: '技术感更强' },
  { value: 'vitesse-dark', label: 'Vitesse Dark', hint: '深色技术感' }
] as const
const currentPreviewContentThemeLabel = computed(() =>
  contentThemeOptions.find(item => item.value === previewContentTheme.value)?.label || '杂志感'
)
const currentPreviewCodeThemeLabel = computed(() =>
  codeThemeOptions.find(item => item.value === previewCodeTheme.value)?.label || 'GitHub Light'
)
const contentModel = computed({
  get: () => props.modelValue ?? '',
  set: value => emit('update:modelValue', value)
})
const previewHtml = computed(() => props.html || '')
const previewLoading = computed(() => props.isLoading)
const previewSkeletonLoading = computed(() => previewLoading.value && !hasPreviewContent.value)
const currentContentLength = computed(() => props.contentLength)
const currentReadingMinutes = computed(() => props.readingMinutes)
const currentTocCount = computed(() => props.tocCount)
const tocSource = computed(() => props.toc || [])
const plainTextSource = computed(() => props.plainText || '')
const hasPreviewContent = computed(() => previewHtml.value.trim().length > 0)
const previewTabs = [
  { key: 'finalPreview', label: '最终预览', icon: 'i-lucide-panels-top-left', hint: '按前台样式查看成品排版' },
  { key: 'html', label: 'HTML', icon: 'i-lucide-file-code-2', hint: '检查最终渲染后的结构输出' },
  { key: 'toc', label: '目录', icon: 'i-lucide-list-tree', hint: '确认标题层级与锚点结构' },
  { key: 'plainText', label: '纯文本', icon: 'i-lucide-wrap-text', hint: '确认提取后的正文文本内容' },
  { key: 'stats', label: '统计', icon: 'i-lucide-chart-column', hint: '查看字数、阅读时长与状态' }
] as const
const currentPreviewTabMeta = computed(() =>
  previewTabs.find(item => item.key === previewPanelTab.value) || previewTabs[0]
)
const previewSummaryItems = computed(() => [
  {
    key: 'words',
    label: '字数',
    value: `${currentContentLength.value}`,
    tone: 'text-slate-900 dark:text-slate-50'
  },
  {
    key: 'reading',
    label: '阅读',
    value: `${currentReadingMinutes.value} 分钟`,
    tone: 'text-slate-900 dark:text-slate-50'
  },
  {
    key: 'toc',
    label: '目录',
    value: `${currentTocCount.value} 项`,
    tone: 'text-slate-900 dark:text-slate-50'
  }
])
const tocSourceText = computed(() => JSON.stringify(tocSource.value, null, 2))
const statsSourceText = computed(() => JSON.stringify({
  contentLength: currentContentLength.value,
  readingMinutes: currentReadingMinutes.value,
  tocCount: currentTocCount.value,
  hasHtml: hasPreviewContent.value,
  isLoading: previewLoading.value
}, null, 2))
const currentPreviewPanelText = computed(() => {
  switch (previewPanelTab.value) {
    case 'finalPreview':
      return previewHtml.value
    case 'toc':
      return tocSourceText.value
    case 'plainText':
      return plainTextSource.value
    case 'stats':
      return statsSourceText.value
    default:
      return previewHtml.value
  }
})
const currentPreviewPanelTitle = computed(() => {
  switch (previewPanelTab.value) {
    case 'finalPreview':
      return '最终文章预览'
    case 'toc':
      return '当前目录结构'
    case 'plainText':
      return '当前纯文本内容'
    case 'stats':
      return '当前渲染统计'
    default:
      return '当前渲染 HTML'
  }
})
const currentPreviewPanelDescription = computed(() => {
  switch (previewPanelTab.value) {
    case 'finalPreview':
      return '这里按文章最终展示样式渲染当前正文内容，便于直接确认排版、段落节奏和阅读观感。'
    case 'toc':
      return '这里展示当前正文解析后的目录数据，便于确认标题层级和目录生成结果。'
    case 'plainText':
      return '这里展示当前正文提取后的纯文本内容，便于确认字数统计和摘要基础文本。'
    case 'stats':
      return '这里展示当前正文渲染过程中的核心统计数据，便于定位内容指标是否正常。'
    default:
      return '这里展示当前正文生成的 HTML 原文，便于直接检查预览内容与渲染结果。'
  }
})
const currentPreviewCodePanelClass = computed(() => {
  switch (previewPanelTab.value) {
    case 'html':
      return 'border-slate-200/80 bg-[linear-gradient(180deg,rgba(248,250,252,0.98),rgba(241,245,249,0.96))] dark:border-slate-700/80 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.96),rgba(2,6,23,0.98))]'
    case 'plainText':
      return 'border-stone-200/80 bg-[linear-gradient(180deg,rgba(255,252,248,0.98),rgba(250,245,235,0.96))] dark:border-stone-700/70 dark:bg-[linear-gradient(180deg,rgba(41,37,36,0.96),rgba(28,25,23,0.98))]'
    case 'stats':
      return 'border-sky-200/80 bg-[linear-gradient(180deg,rgba(240,249,255,0.98),rgba(239,246,255,0.94))] dark:border-sky-900/70 dark:bg-[linear-gradient(180deg,rgba(12,74,110,0.24),rgba(8,47,73,0.4))]'
    default:
      return 'border-slate-200/80 bg-[linear-gradient(180deg,rgba(248,250,252,0.98),rgba(241,245,249,0.96))] dark:border-slate-700/80 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.96),rgba(2,6,23,0.98))]'
  }
})
const currentPreviewCodeTextClass = computed(() => (
  previewPanelTab.value === 'plainText'
    ? 'whitespace-pre-wrap break-words text-slate-700 dark:text-stone-100'
    : 'whitespace-pre text-slate-700 dark:text-slate-100'
))
const previewStatusLabel = computed(() => {
  if (previewLoading.value && !hasPreviewContent.value) {
    return '正在渲染'
  }

  if (previewLoading.value && hasPreviewContent.value) {
    return '已更新预览'
  }

  return hasPreviewContent.value ? '渲染完成' : '等待内容'
})
const previewStatusToneClass = computed(() =>
  previewSkeletonLoading.value
    ? 'border-amber-200/80 bg-amber-50/90 text-amber-700 dark:border-amber-400/30 dark:bg-amber-400/10 dark:text-amber-200'
    : hasPreviewContent.value
      ? 'border-emerald-200/80 bg-emerald-50/90 text-emerald-700 dark:border-emerald-400/30 dark:bg-emerald-400/10 dark:text-emerald-200'
      : 'border-slate-200/80 bg-slate-50/90 text-slate-600 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300'
)

/**
 * 同步 Markdown 文本内容。
 * 用于把左侧编辑区输入内容持续回传给外层文章表单。
 *
 * @param value 当前输入的 Markdown 文本
 */
function handleUpdate(value: string | number | null) {
  emit('update:modelValue', String(value ?? ''))
}

/**
 * 切换移动端工作台视图。
 * 用于在小屏环境下切换编辑区与预览区，保证阅读和输入都更清晰。
 *
 * @param mode 目标视图模式
 */
function switchViewMode(mode: 'edit' | 'preview') {
  viewMode.value = mode
}

/**
 * 打开渲染结果预览弹窗。
 * 用于集中查看 HTML、目录、纯文本与统计等原始渲染结果，辅助排查预览区异常问题。
 */
function openPreviewModal() {
  isPreviewModalOpen.value = true
}

/**
 * 切换预览面板标签。
 * 用于在弹窗内查看不同类型的原始渲染结果。
 *
 * @param tab 当前选中的面板标识
 */
function switchPreviewPanelTab(tab: 'finalPreview' | 'html' | 'toc' | 'plainText' | 'stats') {
  previewPanelTab.value = tab
}

/**
 * 关闭渲染结果预览弹窗。
 * 用于在查看完当前原始渲染结果后收起预览弹窗。
 */
function closePreviewModal() {
  isPreviewModalOpen.value = false
}

/**
 * 切换最终预览内容主题。
 * 作用：在预览弹窗里快速比较不同正文主题的排版与阅读观感。
 *
 * @param theme 目标内容主题
 */
function switchPreviewContentTheme(theme: 'editorial' | 'documentation' | 'minimal') {
  previewContentTheme.value = theme
}

/**
 * 切换最终预览代码主题。
 * 作用：在预览弹窗里快速比较不同代码高亮主题的视觉表现。
 *
 * @param theme 目标代码主题
 */
function switchPreviewCodeTheme(theme: 'github-light' | 'github-dark' | 'vitesse-light' | 'vitesse-dark') {
  previewCodeTheme.value = theme
}

/**
 * 顺序切换内容主题。
 * 作用：在实时预览区提供更轻量的主题切换入口，不占用过多工作台空间。
 */
function cyclePreviewContentTheme() {
  const currentIndex = contentThemeOptions.findIndex(item => item.value === previewContentTheme.value)
  const nextIndex = currentIndex >= contentThemeOptions.length - 1 ? 0 : currentIndex + 1
  previewContentTheme.value = contentThemeOptions[nextIndex].value
}

/**
 * 顺序切换代码主题。
 * 作用：在实时预览区提供更轻量的代码高亮切换入口，便于快速比较效果。
 */
function cyclePreviewCodeTheme() {
  const currentIndex = codeThemeOptions.findIndex(item => item.value === previewCodeTheme.value)
  const nextIndex = currentIndex >= codeThemeOptions.length - 1 ? 0 : currentIndex + 1
  previewCodeTheme.value = codeThemeOptions[nextIndex].value
}

/**
 * 跳转到最终预览中的指定标题。
 * 作用：在目录树中点击某个标题后，切换到最终预览并滚动到对应锚点位置。
 *
 * @param item 当前目录项
 */
async function jumpToTocItem(item: ArticleTocItem) {
  activeTocId.value = item.id
  previewPanelTab.value = 'finalPreview'
  await nextTick()

  const target = finalPreviewPanelRef.value?.querySelector<HTMLElement>(`#${CSS.escape(item.id)}`)
  if (!target) {
    return
  }

  target.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}
</script>

<template>
  <div class="space-y-4">
    <div class="rounded-[16px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(248,250,252,0.9))] px-4 py-3.5 shadow-[0_16px_30px_-30px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.04),rgba(255,255,255,0.02))]">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0 space-y-2">
          <div class="flex flex-wrap items-center gap-2.5">
            <div class="flex items-center gap-1.5 rounded-full border border-slate-200/80 bg-white px-2.5 py-1.5 shadow-[0_10px_20px_-22px_rgba(15,23,42,0.12)] dark:border-white/10 dark:bg-slate-950/70">
              <span class="h-2.5 w-2.5 rounded-full bg-rose-400/90" />
              <span class="h-2.5 w-2.5 rounded-full bg-amber-400/90" />
              <span class="h-2.5 w-2.5 rounded-full bg-emerald-400/90" />
            </div>

            <span class="text-sm font-semibold tracking-[-0.02em] text-slate-900 dark:text-slate-50">正文工作区</span>
            <span class="inline-flex min-h-7 items-center rounded-full border border-slate-200/80 bg-white/86 px-2.5 text-[0.72rem] font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
              {{ currentContentLength }} 字
            </span>
            <span class="inline-flex min-h-7 items-center rounded-full border border-slate-200/80 bg-white/86 px-2.5 text-[0.72rem] font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
              {{ currentReadingMinutes }} 分钟阅读
            </span>
          </div>

        </div>

        <div class="flex flex-wrap items-center gap-2">
          <div class="inline-flex rounded-[12px] border border-slate-200/80 bg-white/88 p-1 shadow-[0_12px_22px_-22px_rgba(15,23,42,0.12)] dark:border-white/10 dark:bg-white/[0.04] xl:hidden">
            <button
              type="button"
              class="min-h-9 cursor-pointer rounded-[10px] px-3 text-sm font-medium transition duration-200"
              :class="viewMode === 'edit'
                ? 'bg-slate-900 text-white shadow-[0_12px_20px_-18px_rgba(15,23,42,0.28)] dark:bg-white dark:text-slate-950'
                : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
              @click="switchViewMode('edit')"
            >
              编辑
            </button>
            <button
              type="button"
              class="min-h-9 cursor-pointer rounded-[10px] px-3 text-sm font-medium transition duration-200"
              :class="viewMode === 'preview'
                ? 'bg-slate-900 text-white shadow-[0_12px_20px_-18px_rgba(15,23,42,0.28)] dark:bg-white dark:text-slate-950'
                : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
              @click="switchViewMode('preview')"
            >
              预览
            </button>
          </div>

          <div
            class="inline-flex min-h-9 items-center rounded-full border px-3 text-xs font-semibold"
            :class="previewStatusToneClass"
          >
            {{ previewStatusLabel }}
          </div>

          <button
            type="button"
            class="inline-flex min-h-9 cursor-pointer items-center gap-2 rounded-[12px] border border-slate-200/80 bg-white px-3 py-2 text-xs font-semibold text-slate-600 transition duration-200 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:bg-slate-950/70 dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50"
            :title="`切换内容主题，当前：${currentPreviewContentThemeLabel}`"
            @click="cyclePreviewContentTheme"
          >
            <UIcon name="i-lucide-palette" class="size-3.5" />
            <span>{{ currentPreviewContentThemeLabel }}</span>
          </button>

          <button
            type="button"
            class="inline-flex min-h-9 cursor-pointer items-center gap-2 rounded-[12px] border border-slate-200/80 bg-white px-3 py-2 text-xs font-semibold text-slate-600 transition duration-200 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:bg-slate-950/70 dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50"
            :title="`切换代码主题，当前：${currentPreviewCodeThemeLabel}`"
            @click="cyclePreviewCodeTheme"
          >
            <UIcon name="i-lucide-file-code-2" class="size-3.5" />
            <span>{{ currentPreviewCodeThemeLabel }}</span>
          </button>

          <AdminButton
            tone="neutral"
            variant="outline"
            icon="i-lucide-code-xml"
            label="查看结果"
            @click="openPreviewModal"
          />
        </div>
      </div>
    </div>

    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
      <section
        class="flex h-[44rem] flex-col rounded-[18px] border border-slate-200/80 bg-white/84 p-3 shadow-[0_18px_34px_-30px_rgba(15,23,42,0.12)] dark:border-white/10 dark:bg-slate-950/72 xl:h-[52rem]"
        :class="viewMode === 'preview' ? 'hidden xl:block' : ''"
      >
        <div class="min-h-0 flex-1 overflow-hidden rounded-[14px] bg-transparent">
          <textarea
            :value="contentModel"
            class="h-full min-h-full w-full resize-none overflow-y-auto rounded-[14px] border-0 bg-transparent px-5 py-4 font-mono text-[0.96rem] leading-[1.95] text-slate-900 outline-none transition-[background-color] duration-200 placeholder:text-slate-400 dark:text-slate-100 dark:placeholder:text-slate-500 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
            placeholder="请输入 Markdown 正文内容"
            spellcheck="false"
            @input="handleUpdate(($event.target as HTMLTextAreaElement).value)"
          />
        </div>
      </section>

      <section
        class="flex h-[44rem] min-w-0 overflow-hidden rounded-[18px] border border-slate-200/80 bg-white/84 p-3 shadow-[0_18px_34px_-30px_rgba(15,23,42,0.12)] dark:border-white/10 dark:bg-slate-950/72 xl:h-[52rem]"
        :class="viewMode === 'edit' ? 'hidden xl:block' : ''"
      >
        <MarkdownPreview
          :html="previewHtml"
          :is-loading="previewSkeletonLoading"
          :content-theme="previewContentTheme"
          :code-theme="previewCodeTheme"
          container-class="h-full min-h-0 min-w-0 flex-1 overflow-hidden rounded-[14px] border-0 bg-transparent p-0 shadow-none"
          body-class="h-full min-h-0 min-w-0 overflow-auto px-2 py-1 whitespace-normal break-words [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
        />
      </section>
    </div>

    <UModal
      v-model:open="isPreviewModalOpen"
      title="渲染结果预览"
      :ui="{
        overlay: 'bg-slate-950/40 backdrop-blur-[6px] dark:bg-slate-950/60',
        content: 'w-[calc(100vw-2rem)] max-w-6xl overflow-hidden rounded-[20px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(248,250,252,0.92))] shadow-[0_32px_72px_-36px_rgba(15,23,42,0.3)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.96),rgba(15,23,42,0.92))] dark:shadow-[0_32px_72px_-36px_rgba(0,0,0,0.62)]',
        header: 'border-b border-slate-200/80 px-6 pt-6 pb-5 dark:border-white/10',
        body: 'bg-transparent px-6 py-6',
        footer: 'border-t border-slate-200/80 bg-white/55 px-6 pt-4 pb-5 dark:border-white/10 dark:bg-white/[0.03]'
      }"
    >
      <template #header>
        <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div class="flex min-w-0 items-start gap-4">
            <div class="inline-flex size-11 shrink-0 items-center justify-center rounded-[14px] border border-sky-200/80 bg-[linear-gradient(135deg,rgba(240,249,255,0.98),rgba(255,255,255,0.82))] text-sky-600 shadow-[0_14px_28px_-24px_rgba(14,165,233,0.38)] dark:border-sky-400/20 dark:bg-[linear-gradient(135deg,rgba(56,189,248,0.14),rgba(255,255,255,0.03))] dark:text-sky-300">
              <UIcon :name="currentPreviewTabMeta.icon" class="size-5" />
            </div>

            <div class="min-w-0 space-y-1.5">
              <div class="flex flex-wrap items-center gap-2">
                <p class="text-[1.05rem] font-semibold tracking-[-0.02em] text-slate-900 dark:text-slate-50">{{ currentPreviewPanelTitle }}</p>
                <span
                  class="inline-flex min-h-6 items-center rounded-full border px-2.5 text-[0.68rem] font-semibold tracking-[0.08em]"
                  :class="previewStatusToneClass"
                >
                  {{ previewStatusLabel }}
                </span>
              </div>
              <p class="max-w-2xl text-sm leading-6 text-slate-500 dark:text-slate-400">
                {{ currentPreviewTabMeta.hint }}
              </p>
            </div>
          </div>

          <div class="grid grid-cols-3 gap-2 sm:gap-3">
            <div
              v-for="item in previewSummaryItems"
              :key="item.key"
              class="min-w-[5.5rem] rounded-[14px] border border-slate-200/80 bg-white/78 px-3 py-2.5 shadow-[0_10px_24px_-24px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-white/[0.04]"
            >
              <p class="text-[0.68rem] font-medium uppercase tracking-[0.14em] text-slate-400 dark:text-slate-500">{{ item.label }}</p>
              <p class="mt-1 text-sm font-semibold tracking-[-0.01em]" :class="item.tone">{{ item.value }}</p>
            </div>
          </div>
        </div>
      </template>

      <template #body>
        <div class="grid gap-5 lg:grid-cols-[15rem_minmax(0,1fr)]">
          <aside class="space-y-4">
            <div class="rounded-[18px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(248,250,252,0.9))] p-3 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.2)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.03))]">
              <div class="mb-2 px-2 pt-1">
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">结果视图</p>
              </div>
              <div class="space-y-1.5">
                <button
                  v-for="tab in previewTabs"
                  :key="tab.key"
                  type="button"
                  class="group flex w-full cursor-pointer items-start gap-3 rounded-[14px] border px-3 py-3 text-left transition duration-200"
                  :class="previewPanelTab === tab.key
                    ? 'border-sky-200/80 bg-sky-50/80 shadow-[0_14px_28px_-24px_rgba(14,165,233,0.32)] dark:border-sky-400/20 dark:bg-sky-400/10'
                    : 'border-transparent bg-transparent hover:border-slate-200/80 hover:bg-white/80 dark:hover:border-white/10 dark:hover:bg-white/[0.04]'"
                  @click="switchPreviewPanelTab(tab.key)"
                >
                  <span
                    class="mt-0.5 inline-flex size-8 shrink-0 items-center justify-center rounded-[10px] border transition duration-200"
                    :class="previewPanelTab === tab.key
                      ? 'border-sky-200/80 bg-white text-sky-600 dark:border-sky-400/20 dark:bg-slate-950/70 dark:text-sky-300'
                      : 'border-slate-200/80 bg-white/75 text-slate-400 group-hover:text-slate-700 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-500 dark:group-hover:text-slate-300'"
                  >
                    <UIcon :name="tab.icon" class="size-4" />
                  </span>
                  <span class="min-w-0">
                    <span
                      class="block text-sm font-semibold tracking-[-0.01em]"
                      :class="previewPanelTab === tab.key ? 'text-slate-950 dark:text-slate-50' : 'text-slate-700 dark:text-slate-200'"
                    >
                      {{ tab.label }}
                    </span>
                    <span class="mt-1 block text-xs leading-5 text-slate-400 dark:text-slate-500">
                      {{ tab.hint }}
                    </span>
                  </span>
                </button>
              </div>
            </div>

            <div
              v-if="previewPanelTab === 'finalPreview'"
              class="rounded-[18px] border border-slate-200/80 bg-white/82 p-3.5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-white/[0.04]"
            >
              <div class="mb-3 px-1">
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">预览主题</p>
              </div>

              <div class="space-y-3.5">
                <div class="space-y-2">
                  <div class="flex items-center justify-between gap-3 px-1">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">内容样式</p>
                    <UBadge color="neutral" variant="soft">{{ previewContentTheme }}</UBadge>
                  </div>
                  <div class="space-y-1.5">
                    <button
                      v-for="theme in contentThemeOptions"
                      :key="theme.value"
                      type="button"
                      class="flex min-h-11 w-full cursor-pointer items-center justify-between rounded-[12px] border px-3 py-2.5 text-left transition duration-200"
                      :class="previewContentTheme === theme.value
                        ? 'border-sky-200/80 bg-sky-50/82 text-slate-950 dark:border-sky-400/20 dark:bg-sky-400/10 dark:text-slate-50'
                        : 'border-slate-200/80 bg-slate-50/72 text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:bg-slate-950/48 dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50'"
                      @click="switchPreviewContentTheme(theme.value)"
                    >
                      <span>
                        <span class="block text-sm font-semibold">{{ theme.label }}</span>
                        <span class="mt-0.5 block text-xs text-slate-400 dark:text-slate-500">{{ theme.hint }}</span>
                      </span>
                      <UIcon
                        :name="previewContentTheme === theme.value ? 'i-lucide-check' : 'i-lucide-chevron-right'"
                        class="size-4 shrink-0"
                      />
                    </button>
                  </div>
                </div>

                <div class="space-y-2">
                  <div class="flex items-center justify-between gap-3 px-1">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">代码主题</p>
                    <UBadge color="neutral" variant="soft">{{ previewCodeTheme }}</UBadge>
                  </div>
                  <div class="space-y-1.5">
                    <button
                      v-for="theme in codeThemeOptions"
                      :key="theme.value"
                      type="button"
                      class="flex min-h-11 w-full cursor-pointer items-center justify-between rounded-[12px] border px-3 py-2.5 text-left transition duration-200"
                      :class="previewCodeTheme === theme.value
                        ? 'border-sky-200/80 bg-sky-50/82 text-slate-950 dark:border-sky-400/20 dark:bg-sky-400/10 dark:text-slate-50'
                        : 'border-slate-200/80 bg-slate-50/72 text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:bg-slate-950/48 dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50'"
                      @click="switchPreviewCodeTheme(theme.value)"
                    >
                      <span>
                        <span class="block text-sm font-semibold">{{ theme.label }}</span>
                        <span class="mt-0.5 block text-xs text-slate-400 dark:text-slate-500">{{ theme.hint }}</span>
                      </span>
                      <UIcon
                        :name="previewCodeTheme === theme.value ? 'i-lucide-check' : 'i-lucide-chevron-right'"
                        class="size-4 shrink-0"
                      />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div
              v-else
              class="rounded-[18px] border border-slate-200/80 bg-white/82 p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-white/[0.04]"
            >
              <p class="text-[0.68rem] font-semibold uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">当前说明</p>
              <p class="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">
                {{ currentPreviewPanelDescription }}
              </p>
              <div class="mt-4 rounded-[14px] border border-dashed border-slate-200/80 bg-slate-50/72 px-3 py-3 dark:border-white/10 dark:bg-slate-950/48">
                <p class="text-xs leading-5 text-slate-400 dark:text-slate-500">
                  左侧切换结果视图，右侧展示对应内容。目录项支持直接跳转到最终预览中的标题位置。
                </p>
              </div>
            </div>
          </aside>

          <section class="min-w-0 space-y-4">
            <div class="flex flex-col gap-3 rounded-[18px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.88),rgba(248,250,252,0.84))] px-4 py-3.5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.03))] sm:flex-row sm:items-center sm:justify-between">
              <div class="min-w-0">
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ currentPreviewPanelTitle }}</p>
                <p class="mt-1 text-xs leading-5 text-slate-400 dark:text-slate-500">
                  {{ currentPreviewPanelDescription }}
                </p>
              </div>
              <div class="flex items-center gap-2">
                <span
                  class="inline-flex min-h-8 items-center rounded-full border px-3 text-xs font-semibold"
                  :class="previewStatusToneClass"
                >
                  {{ previewStatusLabel }}
                </span>
                <span class="inline-flex min-h-8 items-center rounded-full border border-slate-200/80 bg-white/78 px-3 text-xs font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
                  {{ currentPreviewTabMeta.label }}
                </span>
              </div>
            </div>

            <div class="rounded-[20px] border border-slate-200/80 bg-white/72 p-4 shadow-[0_20px_42px_-32px_rgba(15,23,42,0.22)] dark:border-white/10 dark:bg-white/[0.03]">
              <div
                v-if="previewPanelTab === 'finalPreview'"
                ref="finalPreviewPanelRef"
                class="overflow-hidden rounded-[18px] border border-slate-200/80 bg-white dark:border-white/10 dark:bg-slate-950/82"
              >
                <MarkdownPreview
                  :html="previewHtml"
                  :is-loading="previewSkeletonLoading"
                  empty-text="当前还没有可预览的正文内容。"
                  :content-theme="previewContentTheme"
                  :code-theme="previewCodeTheme"
                  container-class="min-h-[38rem] border-0 bg-transparent p-0 shadow-none"
                  body-class="max-h-[38rem] overflow-auto px-7 py-7 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
                />
              </div>

              <div
                v-else-if="previewPanelTab === 'toc' && tocSource.length"
                class="overflow-hidden rounded-[18px] border border-slate-200/80 bg-white dark:border-white/10 dark:bg-slate-950/82"
              >
                <div class="border-b border-slate-200/80 px-5 py-4 dark:border-white/10">
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">目录树</p>
                      <p class="mt-1 text-xs leading-5 text-slate-400 dark:text-slate-500">点击条目后会自动切换到最终预览并滚动到对应标题。</p>
                    </div>
                    <UBadge color="info" variant="soft">{{ tocSource.length }} 项</UBadge>
                  </div>
                </div>

                <div class="max-h-[38rem] overflow-auto px-4 py-4 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80">
                  <ArticleTocTree
                    :items="tocSource"
                    :active-id="activeTocId"
                    @select="jumpToTocItem"
                  />
                </div>
              </div>

              <div
                v-else-if="currentPreviewPanelText.trim().length > 0"
                class="overflow-hidden rounded-[18px] border"
                :class="currentPreviewCodePanelClass"
              >
                <div class="border-b border-black/6 px-5 py-3.5 dark:border-white/8">
                  <div class="flex items-center gap-2">
                    <span class="h-2.5 w-2.5 rounded-full bg-rose-400/95" />
                    <span class="h-2.5 w-2.5 rounded-full bg-amber-400/95" />
                    <span class="h-2.5 w-2.5 rounded-full bg-emerald-400/95" />
                    <span class="ml-2 text-[0.7rem] font-medium uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">
                      {{ currentPreviewTabMeta.label }}
                    </span>
                  </div>
                </div>
                <pre
                  class="max-h-[38rem] overflow-auto px-5 py-4 text-sm leading-7 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
                  :class="currentPreviewCodeTextClass"
                >{{ currentPreviewPanelText }}</pre>
              </div>

              <div
                v-else
                class="flex min-h-[22rem] items-center justify-center rounded-[18px] border border-dashed border-slate-200/80 bg-[linear-gradient(180deg,rgba(248,250,252,0.82),rgba(255,255,255,0.62))] px-6 py-8 text-center dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.42),rgba(2,6,23,0.56))]"
              >
                <div class="max-w-sm">
                  <div class="mx-auto inline-flex size-12 items-center justify-center rounded-[14px] border border-slate-200/80 bg-white text-slate-400 shadow-[0_12px_24px_-20px_rgba(15,23,42,0.14)] dark:border-white/10 dark:bg-slate-950/80 dark:text-slate-500">
                    <UIcon :name="currentPreviewTabMeta.icon" class="size-5" />
                  </div>
                  <p class="mt-4 text-base font-semibold text-slate-900 dark:text-slate-50">当前还没有可展示的内容</p>
                  <p class="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">
                    可以先在左侧输入 Markdown 内容，或切换到其他结果视图查看现有数据。
                  </p>
                </div>
              </div>
            </div>
          </section>
        </div>
      </template>

      <template #footer>
        <div class="flex w-full justify-end">
          <AdminButton
            tone="neutral"
            variant="outline"
            label="关闭"
            @click="closePreviewModal"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>
