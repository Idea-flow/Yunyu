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
  { key: 'finalPreview', label: '最终预览', icon: 'i-lucide-panels-top-left' },
  { key: 'html', label: 'HTML', icon: 'i-lucide-file-code-2' },
  { key: 'toc', label: '目录', icon: 'i-lucide-list-tree' },
  { key: 'plainText', label: '纯文本', icon: 'i-lucide-wrap-text' },
  { key: 'stats', label: '统计', icon: 'i-lucide-chart-column' }
] as const
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
  <div class="space-y-5">
    <div class="rounded-[12px] border border-white/60 bg-white/56 p-3 shadow-[0_12px_24px_-24px_rgba(15,23,42,0.16)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex items-center gap-1.5 rounded-[10px] border border-white/65 bg-white/82 px-3 py-2 shadow-[0_8px_18px_-18px_rgba(15,23,42,0.14)] dark:border-white/10 dark:bg-white/[0.05]">
            <span class="h-2.5 w-2.5 rounded-full bg-rose-400/90" />
            <span class="h-2.5 w-2.5 rounded-full bg-amber-400/90" />
            <span class="h-2.5 w-2.5 rounded-full bg-emerald-400/90" />
          </div>

          <div
            v-for="tool in editorTools"
            :key="tool.label"
            class="flex min-h-9 items-center gap-2 rounded-[10px] border border-white/65 bg-white/82 px-3 py-2 text-sm text-slate-600 shadow-[0_8px_18px_-18px_rgba(15,23,42,0.14)] dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-300"
          >
            <span class="font-semibold text-slate-800 dark:text-slate-100">{{ tool.label }}</span>
            <span class="text-slate-400 dark:text-slate-500">{{ tool.hint }}</span>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UButton
            color="neutral"
            variant="soft"
            icon="i-lucide-code-xml"
            label="预览"
            class="rounded-[10px]"
            @click="openPreviewModal"
          />
        </div>
      </div>
    </div>

    <div class="inline-flex rounded-[10px] border border-white/60 bg-white/45 p-1 backdrop-blur-md xl:hidden dark:border-white/10 dark:bg-white/[0.03]">
      <button
        type="button"
        class="min-h-10 cursor-pointer rounded-[8px] px-4 text-sm font-medium transition duration-200"
        :class="viewMode === 'edit'
          ? 'bg-white/92 text-slate-900 shadow-[0_8px_18px_-18px_rgba(15,23,42,0.18)] dark:bg-white/10 dark:text-slate-50'
          : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
        @click="switchViewMode('edit')"
      >
        编辑
      </button>
      <button
        type="button"
        class="min-h-10 cursor-pointer rounded-[8px] px-4 text-sm font-medium transition duration-200"
        :class="viewMode === 'preview'
          ? 'bg-white/92 text-slate-900 shadow-[0_8px_18px_-18px_rgba(15,23,42,0.18)] dark:bg-white/10 dark:text-slate-50'
          : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
        @click="switchViewMode('preview')"
      >
        预览
      </button>
    </div>

    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
      <section
        class="flex h-[46rem] flex-col rounded-[14px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.84),rgba(255,255,255,0.66))] p-4 shadow-[0_16px_30px_-28px_rgba(15,23,42,0.14)] backdrop-blur-lg dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.78),rgba(15,23,42,0.66))] dark:shadow-[0_18px_34px_-28px_rgba(0,0,0,0.42)] xl:h-[56rem]"
        :class="viewMode === 'preview' ? 'hidden xl:block' : ''"
      >
        <div class="mb-4 flex items-start justify-between gap-3 rounded-[12px] border border-white/65 bg-white/76 px-4 py-3 shadow-[0_10px_20px_-20px_rgba(15,23,42,0.14)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">编辑器</p>
          <span class="text-xs font-medium text-slate-400 dark:text-slate-500">{{ currentContentLength }} 字</span>
        </div>

        <div class="min-h-0 flex-1 overflow-hidden rounded-[10px]">
          <textarea
            :value="contentModel"
            class="h-full min-h-full w-full resize-none overflow-y-auto rounded-[12px] border border-white/70 bg-white/94 px-4 py-4 font-mono text-[1rem] leading-8 text-slate-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7),0_12px_24px_-22px_rgba(15,23,42,0.14)] outline-none transition-[border-color,box-shadow,background-color] duration-200 placeholder:text-slate-400 hover:border-slate-200 focus:border-sky-400 focus:bg-white focus:ring-4 focus:ring-sky-100/90 dark:border-white/10 dark:bg-slate-950/82 dark:text-slate-100 dark:placeholder:text-slate-500 dark:hover:border-white/15 dark:focus:border-sky-300 dark:focus:bg-slate-950 dark:focus:ring-sky-400/20 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
            placeholder="请输入 Markdown 正文内容"
            spellcheck="false"
            @input="handleUpdate(($event.target as HTMLTextAreaElement).value)"
          />
        </div>
      </section>

      <section
        class="flex h-[46rem] min-w-0 overflow-hidden rounded-[14px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.84),rgba(255,255,255,0.66))] p-4 shadow-[0_16px_30px_-28px_rgba(15,23,42,0.14)] backdrop-blur-lg dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.78),rgba(15,23,42,0.66))] dark:shadow-[0_18px_34px_-28px_rgba(0,0,0,0.42)] xl:h-[56rem]"
        :class="viewMode === 'edit' ? 'hidden xl:block' : ''"
      >
        <div class="mb-4 flex items-start justify-between gap-3 rounded-[12px] border border-white/65 bg-white/76 px-4 py-3 shadow-[0_10px_20px_-20px_rgba(15,23,42,0.14)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">预览</p>
          <div class="flex items-center gap-2">
            <button
              type="button"
              class="inline-flex min-h-9 items-center gap-2 rounded-[10px] border border-white/65 bg-white/82 px-3 py-2 text-xs font-semibold text-slate-600 transition duration-200 hover:border-slate-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50"
              :title="`切换内容主题，当前：${currentPreviewContentThemeLabel}`"
              @click="cyclePreviewContentTheme"
            >
              <UIcon name="i-lucide-palette" class="size-3.5" />
              <span>{{ currentPreviewContentThemeLabel }}</span>
            </button>

            <button
              type="button"
              class="inline-flex min-h-9 items-center gap-2 rounded-[10px] border border-white/65 bg-white/82 px-3 py-2 text-xs font-semibold text-slate-600 transition duration-200 hover:border-slate-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50"
              :title="`切换代码主题，当前：${currentPreviewCodeThemeLabel}`"
              @click="cyclePreviewCodeTheme"
            >
              <UIcon name="i-lucide-file-code-2" class="size-3.5" />
              <span>{{ currentPreviewCodeThemeLabel }}</span>
            </button>
          </div>
        </div>

        <MarkdownPreview
          :html="previewHtml"
          :is-loading="previewSkeletonLoading"
          :content-theme="previewContentTheme"
          :code-theme="previewCodeTheme"
          container-class="h-full min-h-0 min-w-0 flex-1 overflow-hidden"
          body-class="h-full min-h-0 min-w-0 overflow-auto pr-2 whitespace-normal break-words [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
        />
      </section>
    </div>

    <UModal
      v-model:open="isPreviewModalOpen"
      title="渲染结果预览"
      :ui="{
        overlay: 'bg-slate-950/40 backdrop-blur-[6px] dark:bg-slate-950/60',
        content: 'w-[calc(100vw-2rem)] max-w-5xl overflow-hidden rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))] dark:shadow-[0_28px_54px_-36px_rgba(0,0,0,0.56)]',
        header: 'border-b border-white/60 px-6 pt-6 pb-4 dark:border-white/10',
        body: 'bg-transparent px-6 py-5',
        footer: 'border-t border-white/60 bg-white/36 px-6 pt-4 pb-6 dark:border-white/10 dark:bg-white/[0.03]'
      }"
    >
      <template #header>
        <div class="flex items-start gap-4">
          <div class="inline-flex size-10 shrink-0 items-center justify-center rounded-[10px] border border-sky-200/80 bg-[linear-gradient(135deg,rgba(240,249,255,0.98),rgba(255,247,237,0.82))] text-sky-600 shadow-[0_10px_20px_-20px_rgba(14,165,233,0.52)] dark:border-sky-400/20 dark:bg-[linear-gradient(135deg,rgba(56,189,248,0.14),rgba(251,146,60,0.08))] dark:text-sky-300">
            <UIcon name="i-lucide-file-code-2" class="size-5" />
          </div>

          <div class="min-w-0 space-y-1">
            <p class="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ currentPreviewPanelTitle }}</p>
          </div>
        </div>
      </template>

      <template #body>
        <div class="space-y-4">
          <div class="flex flex-wrap gap-2">
            <UButton
              v-for="tab in previewTabs"
              :key="tab.key"
              color="neutral"
              :variant="previewPanelTab === tab.key ? 'solid' : 'soft'"
              :icon="tab.icon"
              :label="tab.label"
              class="rounded-[8px]"
              @click="switchPreviewPanelTab(tab.key)"
            />
          </div>

          <div
            v-if="previewPanelTab === 'finalPreview'"
            class="rounded-[12px] border border-white/60 bg-white/55 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]"
          >
            <div class="grid gap-4 lg:grid-cols-2">
              <div class="space-y-3">
                <div class="flex items-center justify-between gap-3">
                  <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">内容主题</p>
                  <UBadge color="neutral" variant="soft">{{ previewContentTheme }}</UBadge>
                </div>
                <div class="flex flex-wrap gap-2">
                  <UButton
                    v-for="theme in contentThemeOptions"
                    :key="theme.value"
                    color="neutral"
                    :variant="previewContentTheme === theme.value ? 'solid' : 'soft'"
                    class="rounded-[8px]"
                    :label="theme.label"
                    @click="switchPreviewContentTheme(theme.value)"
                  />
                </div>
              </div>

              <div class="space-y-3">
                <div class="flex items-center justify-between gap-3">
                  <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">代码主题</p>
                  <UBadge color="neutral" variant="soft">{{ previewCodeTheme }}</UBadge>
                </div>
                <div class="flex flex-wrap gap-2">
                  <UButton
                    v-for="theme in codeThemeOptions"
                    :key="theme.value"
                    color="neutral"
                    :variant="previewCodeTheme === theme.value ? 'solid' : 'soft'"
                    class="rounded-[8px]"
                    :label="theme.label"
                    @click="switchPreviewCodeTheme(theme.value)"
                  />
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-[12px] border border-white/60 bg-white/48 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/[0.03]">
            <div
              v-if="previewPanelTab === 'finalPreview'"
              ref="finalPreviewPanelRef"
              class="overflow-hidden rounded-[12px] border border-white/65 bg-white/84 dark:border-white/10 dark:bg-slate-950/78"
            >
              <MarkdownPreview
                :html="previewHtml"
                :is-loading="previewSkeletonLoading"
                empty-text="当前还没有可预览的正文内容。"
                :content-theme="previewContentTheme"
                :code-theme="previewCodeTheme"
                container-class="min-h-[32rem] border-0 bg-transparent p-0 shadow-none"
                body-class="max-h-[32rem] overflow-auto px-6 py-6 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
              />
            </div>

            <div
              v-else-if="previewPanelTab === 'toc' && tocSource.length"
              class="overflow-hidden rounded-[12px] border border-white/65 bg-white/84 p-3 dark:border-white/10 dark:bg-slate-950/78"
            >
              <div class="mb-3 flex items-center justify-between gap-3 rounded-[12px] border border-white/60 bg-white/56 px-4 py-3 backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]">
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">目录树</p>
                <UBadge color="info" variant="soft">{{ tocSource.length }} 项</UBadge>
              </div>

              <div class="max-h-[28rem] overflow-auto pr-1 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80">
                <ArticleTocTree
                  :items="tocSource"
                  :active-id="activeTocId"
                  @select="jumpToTocItem"
                />
              </div>
            </div>

            <div
              v-else-if="currentPreviewPanelText.trim().length > 0"
              class="overflow-hidden rounded-[12px] border border-slate-900/90 bg-slate-950 dark:border-slate-700"
            >
              <pre class="max-h-[32rem] overflow-auto px-5 py-4 text-sm leading-7 text-slate-100 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-600/80">{{ currentPreviewPanelText }}</pre>
            </div>

            <div
              v-else
              class="flex min-h-48 items-center justify-center rounded-[12px] border border-dashed border-white/70 bg-white/62 px-6 py-8 text-center backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]"
            >
              <p class="text-base font-semibold text-slate-900 dark:text-slate-50">暂无内容</p>
            </div>
          </div>
        </div>
      </template>

      <template #footer>
        <div class="flex w-full justify-end">
          <UButton
            color="neutral"
            variant="soft"
            label="关闭"
            class="rounded-[8px]"
            @click="closePreviewModal"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>
