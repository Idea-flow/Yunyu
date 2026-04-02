<script setup lang="ts">
/**
 * 前台公共筛选条组件。
 * 作用：统一文章页、分类页、标签页、专题页顶部筛选区的标题、搜索输入和扩展筛选控件布局。
 */
const props = withDefaults(defineProps<{
  eyebrow: string
  title: string
  description?: string
  eyebrowClass?: string
  keyword?: string
  searchPlaceholder?: string
  searchButtonText?: string
  showSearch?: boolean
  pending?: boolean
  resultText?: string
}>(), {
  description: '',
  eyebrowClass: 'text-sky-600 dark:text-sky-300',
  keyword: '',
  searchPlaceholder: '输入关键词筛选',
  searchButtonText: '搜索',
  showSearch: false,
  pending: false,
  resultText: ''
})

const emit = defineEmits<{
  'update:keyword': [value: string]
  search: []
}>()

const slots = useSlots()

/**
 * 生成可双向绑定的关键词模型。
 * 作用：让页面可以通过 `v-model:keyword` 直接接入公共筛选条的输入框。
 */
const keywordModel = computed({
  get: () => props.keyword,
  set: value => emit('update:keyword', value)
})

/**
 * 判断是否存在扩展筛选内容。
 * 作用：在页面未传入额外筛选控件时，自动收起对应布局区域。
 */
const hasFiltersSlot = computed(() => Boolean(slots.filters))

/**
 * 判断是否存在底部补充内容。
 * 作用：让页面按需注入统计说明、快捷入口或辅助筛选提示。
 */
const hasFooterSlot = computed(() => Boolean(slots.footer))

/**
 * 提交筛选动作。
 * 作用：统一处理搜索按钮点击与回车提交事件，对外派发搜索事件。
 */
function handleSearch() {
  emit('search')
}
</script>

<template>
  <div class="rounded-[32px] border border-white/60 bg-white/82 p-6 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 sm:p-8">
    <div class="flex flex-col gap-6">
      <div>
        <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em]" :class="eyebrowClass">{{ eyebrow }}</p>
        <h1 class="mt-3 text-[clamp(2.05rem,1.55rem+1.35vw,3rem)] font-semibold leading-[1.02] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">{{ title }}</h1>
        <p v-if="description" class="mt-4 max-w-3xl text-[clamp(1rem,0.95rem+0.16vw,1.08rem)] leading-[1.95] tracking-[-0.01em] text-slate-600 dark:text-slate-300">
          {{ description }}
        </p>
        <p v-if="resultText" class="mt-5 text-[0.82rem] uppercase tracking-[0.14em] text-slate-500 dark:text-slate-400">
          {{ resultText }}
        </p>
      </div>

      <div v-if="showSearch || hasFiltersSlot" class="flex flex-col gap-3">
        <div v-if="showSearch" class="flex flex-col gap-3 lg:flex-row">
          <UInput
            v-model="keywordModel"
            size="xl"
            :placeholder="searchPlaceholder"
            class="w-full"
            @keyup.enter="handleSearch"
          />
          <UButton
            color="primary"
            size="xl"
            icon="i-lucide-search"
            class="justify-center lg:px-6"
            :loading="pending"
            @click="handleSearch"
          >
            {{ searchButtonText }}
          </UButton>
        </div>

        <div v-if="hasFiltersSlot" class="flex flex-col gap-3">
          <slot name="filters" />
        </div>
      </div>

      <div v-if="hasFooterSlot" class="border-t border-slate-200/70 pt-5 dark:border-white/10">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>
