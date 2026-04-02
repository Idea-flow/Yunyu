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
    paddingLeft: `${Math.max(0, level - 1) * 0.72}rem`
  }
}

/**
 * 计算目录项容器样式。
 * 作用：统一目录项在普通态、悬停态和激活态下的背景、位移和阴影表现。
 *
 * @param item 当前目录项
 * @returns 目录项样式类名
 */
function getItemClass(item: ArticleTocItem) {
  return props.activeId === item.id
    ? 'bg-[linear-gradient(90deg,rgba(14,165,233,0.08),rgba(255,255,255,0.96))] text-slate-950 ring-1 ring-sky-200/70 shadow-[0_18px_30px_-28px_rgba(14,165,233,0.24)] dark:bg-[linear-gradient(90deg,rgba(56,189,248,0.14),rgba(15,23,42,0.8))] dark:text-slate-50 dark:ring-sky-400/20'
    : 'text-slate-500 hover:bg-slate-50/72 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-900/28 dark:hover:text-slate-50'
}

/**
 * 计算目录项文字样式。
 * 作用：让高层级标题更突出，低层级标题更克制，维持目录节奏感。
 *
 * @param item 当前目录项
 * @returns 文字样式类名
 */
function getTextClass(item: ArticleTocItem) {
  return item.level <= 2
    ? 'text-[0.85rem] font-semibold leading-6 tracking-[-0.02em]'
    : 'text-[0.78rem] font-medium leading-[1.6] text-slate-400 dark:text-slate-500'
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
  <div class="space-y-1">
    <button
      v-for="item in props.items"
      :key="item.id"
      type="button"
      :data-toc-id="item.id"
      class="group relative flex w-full items-start gap-2.5 overflow-hidden rounded-[0.95rem] px-2.5 py-2 text-left transition duration-200 ease-out cursor-pointer"
      :class="getItemClass(item)"
      :style="getIndentStyle(item.level)"
      @click="handleSelect(item)"
    >
      <span
        class="absolute left-0 top-1/2 h-0 w-[2px] -translate-y-1/2 rounded-full bg-sky-500/80 transition-all duration-200 dark:bg-sky-300/80"
        :class="props.activeId === item.id ? 'h-5.5' : 'h-0 group-hover:h-4 group-hover:bg-slate-300 dark:group-hover:bg-slate-600'"
      />
      <span class="min-w-0 pr-1 transition duration-200" :class="[getTextClass(item), props.activeId === item.id ? 'translate-x-0.5 text-slate-950 dark:text-slate-50' : '']">
        {{ item.text }}
      </span>
    </button>
  </div>
</template>
