<script setup lang="ts">
import type { ArticleTocItem } from '../../types/post'

/**
 * 文章目录树组件。
 * 作用：统一渲染文章标题层级结构，供编辑预览和详情页侧栏复用，
 * 并支持点击目录项后跳转到对应正文锚点。
 */
const props = withDefaults(defineProps<{
  items?: ArticleTocItem[]
  activeId?: string
}>(), {
  items: () => [],
  activeId: ''
})

const emit = defineEmits<{
  select: [item: ArticleTocItem]
}>()

/**
 * 计算目录项缩进样式。
 * 作用：根据标题层级生成更清晰的视觉缩进，避免目录结构难读。
 *
 * @param level 标题层级
 * @returns 缩进样式对象
 */
function getIndentStyle(level: number) {
  return {
    paddingLeft: `${Math.max(0, level - 1) * 1.05}rem`
  }
}

/**
 * 选中目录项。
 * 作用：将当前点击的目录节点回传给外层，以便联动正文预览区跳转。
 *
 * @param item 当前目录项
 */
function handleSelect(item: ArticleTocItem) {
  emit('select', item)
}
</script>

<template>
  <div class="space-y-1.5">
    <button
      v-for="item in props.items"
      :key="item.id"
      type="button"
      class="flex w-full items-start gap-3 rounded-2xl px-3 py-2.5 text-left transition duration-200"
      :class="props.activeId === item.id
        ? 'bg-sky-50 text-sky-700 shadow-[0_12px_24px_-20px_rgba(14,165,233,0.42)] dark:bg-sky-400/12 dark:text-sky-200'
        : 'text-slate-600 hover:bg-white/90 hover:text-slate-900 dark:text-slate-300 dark:hover:bg-slate-950/70 dark:hover:text-slate-50'"
      :style="getIndentStyle(item.level)"
      @click="handleSelect(item)"
    >
      <span
        class="mt-1 inline-flex h-2.5 w-2.5 shrink-0 rounded-full"
        :class="props.activeId === item.id
          ? 'bg-sky-500 dark:bg-sky-300'
          : item.level <= 2
            ? 'bg-slate-400 dark:bg-slate-500'
            : 'bg-slate-300 dark:bg-slate-600'"
      />
      <span class="min-w-0 text-sm font-medium leading-6">{{ item.text }}</span>
    </button>
  </div>
</template>
