<script setup lang="ts">
import { computed } from 'vue'
import MarkdownPreview from './MarkdownPreview.vue'

/**
 * 后台 Markdown 工作台组件。
 * 作用：为文章编辑页提供统一的 Markdown 输入与 HTML 实时预览工作台，
 * 让正文编辑具备整页级双栏体验、固定高度和独立滚动能力。
 */
const props = withDefaults(defineProps<{
  modelValue?: string
  html: string
  isLoading?: boolean
  contentLength?: number
  readingMinutes?: number
  tocCount?: number
}>(), {
  modelValue: '',
  isLoading: false,
  contentLength: 0,
  readingMinutes: 1,
  tocCount: 0
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const viewMode = ref<'edit' | 'preview'>('edit')
const editorTools = [
  { label: '标题', hint: '# / ## / ###' },
  { label: '强调', hint: '**加粗**' },
  { label: '链接', hint: '[文本](url)' },
  { label: '代码', hint: '```language' }
]
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
const hasPreviewContent = computed(() => previewHtml.value.trim().length > 0)
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
</script>

<template>
  <div class="space-y-5">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
      <div class="space-y-1">
        <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">Markdown 内容工作台</p>
        <p class="text-sm text-slate-500 dark:text-slate-400">
          左侧输入 Markdown，右侧实时预览 HTML，适合长文持续写作和排版检查。
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <UBadge color="neutral" variant="soft">正文 {{ currentContentLength }} 字</UBadge>
        <UBadge color="neutral" variant="soft">预估 {{ currentReadingMinutes }} 分钟阅读</UBadge>
        <UBadge color="neutral" variant="soft">目录 {{ currentTocCount }} 项</UBadge>
      </div>
    </div>

    <div class="rounded-[24px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(248,250,252,0.94),rgba(241,245,249,0.72))] p-3 shadow-[0_16px_36px_-28px_rgba(15,23,42,0.28)] dark:border-slate-700 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(15,23,42,0.58))]">
      <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex flex-wrap items-center gap-2">
          <div class="flex items-center gap-1.5 rounded-full border border-slate-200/90 bg-white/90 px-3 py-2 shadow-[0_10px_20px_-18px_rgba(15,23,42,0.28)] dark:border-slate-700 dark:bg-slate-950/80">
            <span class="h-2.5 w-2.5 rounded-full bg-rose-400/90" />
            <span class="h-2.5 w-2.5 rounded-full bg-amber-400/90" />
            <span class="h-2.5 w-2.5 rounded-full bg-emerald-400/90" />
          </div>

          <div
            v-for="tool in editorTools"
            :key="tool.label"
            class="flex min-h-11 items-center gap-2 rounded-full border border-slate-200/90 bg-white/90 px-3 py-2 text-sm text-slate-600 shadow-[0_10px_20px_-18px_rgba(15,23,42,0.24)] dark:border-slate-700 dark:bg-slate-950/80 dark:text-slate-300"
          >
            <span class="font-semibold text-slate-800 dark:text-slate-100">{{ tool.label }}</span>
            <span class="text-slate-400 dark:text-slate-500">{{ tool.hint }}</span>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <span class="inline-flex min-h-11 items-center rounded-full border border-sky-200/80 bg-sky-50/90 px-4 py-2 text-sm font-medium text-sky-700 dark:border-sky-400/30 dark:bg-sky-400/10 dark:text-sky-200">
            Markdown -> HTML 实时编排
          </span>
        </div>
      </div>
    </div>

    <div class="inline-flex rounded-2xl border border-slate-200 bg-transparent p-1 xl:hidden dark:border-slate-700">
      <button
        type="button"
        class="min-h-11 cursor-pointer rounded-xl px-4 text-sm font-medium transition duration-200"
        :class="viewMode === 'edit'
          ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-slate-50'
          : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
        @click="switchViewMode('edit')"
      >
        编辑
      </button>
      <button
        type="button"
        class="min-h-11 cursor-pointer rounded-xl px-4 text-sm font-medium transition duration-200"
        :class="viewMode === 'preview'
          ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-slate-50'
          : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
        @click="switchViewMode('preview')"
      >
        预览
      </button>
    </div>

    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
      <section
        class="flex h-[46rem] flex-col rounded-[28px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(248,250,252,0.86))] p-4 shadow-[0_22px_48px_-34px_rgba(15,23,42,0.32)] dark:border-slate-700 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.9),rgba(15,23,42,0.72))] dark:shadow-[0_24px_50px_-34px_rgba(0,0,0,0.55)] xl:h-[56rem]"
        :class="viewMode === 'preview' ? 'hidden xl:block' : ''"
      >
        <div class="mb-4 flex items-start justify-between gap-3 rounded-[22px] border border-slate-200/80 bg-white/78 px-4 py-3 shadow-[0_16px_32px_-28px_rgba(15,23,42,0.24)] dark:border-slate-700 dark:bg-slate-950/58">
          <div class="min-w-0">
            <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">Markdown 编辑器</p>
            <p class="mt-1 text-sm leading-7 text-slate-500 dark:text-slate-400">支持长文输入、结构调整和持续写作，内容超出后在输入区内部滚动。</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="rounded-full border border-slate-200/90 bg-slate-50 px-3 py-1.5 text-xs font-semibold tracking-[0.14em] text-slate-500 uppercase dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">Editor</span>
            <UBadge color="neutral" variant="soft">源内容</UBadge>
          </div>
        </div>

        <div class="min-h-0 flex-1 overflow-hidden rounded-[1.8rem]">
          <textarea
            :value="contentModel"
            class="h-full min-h-full w-full resize-none overflow-y-auto rounded-[1.8rem] border border-slate-200/80 bg-white/98 px-5 py-5 font-mono text-[1rem] leading-8 text-slate-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7),0_18px_36px_-30px_rgba(15,23,42,0.2)] outline-none transition-[border-color,box-shadow,background-color] duration-200 placeholder:text-slate-400 hover:border-slate-300 focus:border-sky-400 focus:bg-white focus:ring-4 focus:ring-sky-100/90 dark:border-slate-700 dark:bg-slate-950/94 dark:text-slate-100 dark:placeholder:text-slate-500 dark:hover:border-slate-600 dark:focus:border-sky-300 dark:focus:bg-slate-950 dark:focus:ring-sky-400/20 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
            placeholder="请输入 Markdown 正文内容"
            spellcheck="false"
            @input="handleUpdate(($event.target as HTMLTextAreaElement).value)"
          />
        </div>
      </section>

      <section
        class="flex h-[46rem] flex-col rounded-[28px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(248,250,252,0.86))] p-4 shadow-[0_22px_48px_-34px_rgba(15,23,42,0.32)] dark:border-slate-700 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.9),rgba(15,23,42,0.72))] dark:shadow-[0_24px_50px_-34px_rgba(0,0,0,0.55)] xl:h-[56rem]"
        :class="viewMode === 'edit' ? 'hidden xl:block' : ''"
      >
        <div class="mb-4 flex items-start justify-between gap-3 rounded-[22px] border border-slate-200/80 bg-white/78 px-4 py-3 shadow-[0_16px_32px_-28px_rgba(15,23,42,0.24)] dark:border-slate-700 dark:bg-slate-950/58">
          <div class="min-w-0">
            <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">HTML 实时预览</p>
            <p class="mt-1 text-sm leading-7 text-slate-500 dark:text-slate-400">渲染结果实时更新，方便检查标题层级、代码块、段落节奏和最终排版。</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="rounded-full border border-slate-200/90 bg-slate-50 px-3 py-1.5 text-xs font-semibold tracking-[0.14em] text-slate-500 uppercase dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300">Preview</span>
            <UBadge color="info" variant="soft">实时渲染</UBadge>
          </div>
        </div>

        <div class="mb-4 flex items-center justify-between gap-3 rounded-[18px] border border-slate-200/80 bg-slate-50/80 px-4 py-3 dark:border-slate-700 dark:bg-slate-900/70">
          <div class="flex items-center gap-3">
            <span class="relative flex h-3 w-3">
                <span
                  class="absolute inline-flex h-full w-full rounded-full opacity-75"
                  :class="previewSkeletonLoading ? 'animate-ping bg-amber-400/70 dark:bg-amber-300/60' : hasPreviewContent ? 'bg-emerald-400/30 dark:bg-emerald-300/30' : 'bg-slate-300/70 dark:bg-slate-500/40'"
                />
                <span
                  class="relative inline-flex h-3 w-3 rounded-full"
                  :class="previewSkeletonLoading ? 'bg-amber-500 dark:bg-amber-300' : hasPreviewContent ? 'bg-emerald-500 dark:bg-emerald-300' : 'bg-slate-400 dark:bg-slate-500'"
                />
              </span>
            <span class="text-sm font-medium text-slate-700 dark:text-slate-200">{{ previewStatusLabel }}</span>
          </div>

          <span
            class="inline-flex items-center rounded-full border px-3 py-1.5 text-xs font-semibold"
            :class="previewStatusToneClass"
          >
            {{ previewStatusLabel }}
          </span>
        </div>

        <MarkdownPreview
          :html="previewHtml"
          :is-loading="previewSkeletonLoading"
          container-class="h-full flex-1"
          body-class="h-full overflow-y-auto pr-2 whitespace-normal break-words [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.5)_transparent] [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80"
        />
      </section>
    </div>
  </div>
</template>
