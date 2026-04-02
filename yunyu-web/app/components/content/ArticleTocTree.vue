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
    paddingLeft: `${Math.max(0, level - 1) * 1.2}rem`
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
    ? 'bg-[linear-gradient(90deg,rgba(14,165,233,0.12),rgba(255,255,255,0.92))] text-sky-800 ring-1 ring-sky-200/80 shadow-[0_18px_40px_-32px_rgba(14,165,233,0.45)] dark:bg-[linear-gradient(90deg,rgba(56,189,248,0.16),rgba(15,23,42,0.28))] dark:text-sky-100 dark:ring-sky-400/20'
    : 'text-slate-600 hover:bg-slate-50/88 hover:text-slate-900 hover:translate-x-0.5 dark:text-slate-300 dark:hover:bg-slate-900/50 dark:hover:text-slate-50'
}

/**
 * 计算目录项左侧圆点样式。
 * 作用：通过不同层级与激活态的圆点颜色，让目录结构更容易快速扫读。
 *
 * @param item 当前目录项
 * @returns 圆点样式类名
 */
function getDotClass(item: ArticleTocItem) {
  if (props.activeId === item.id) {
    return 'bg-sky-500 shadow-[0_0_0_4px_rgba(14,165,233,0.1)] dark:bg-sky-300'
  }

  if (item.level <= 2) {
    return 'bg-slate-400 dark:bg-slate-500'
  }

  return 'bg-slate-300 dark:bg-slate-600'
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
    ? 'text-[0.92rem] font-semibold leading-6 tracking-[-0.02em]'
    : 'text-[0.84rem] font-medium leading-6 text-slate-500 dark:text-slate-400'
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
      class="group relative flex w-full items-start gap-3 overflow-hidden rounded-[1.15rem] px-3 py-2.5 text-left transition duration-300 ease-out"
      :class="getItemClass(item)"
      :style="getIndentStyle(item.level)"
      @click="handleSelect(item)"
    >
      <span
        class="absolute left-0 top-1/2 h-0 w-px -translate-y-1/2 rounded-full bg-sky-500/55 transition-all duration-300 group-hover:h-8"
        :class="props.activeId === item.id ? 'h-9 bg-[linear-gradient(180deg,rgba(14,165,233,0.18),rgba(14,165,233,0.78),rgba(249,115,22,0.42))] dark:bg-[linear-gradient(180deg,rgba(125,211,252,0.18),rgba(125,211,252,0.8),rgba(251,146,60,0.38))]' : 'dark:bg-sky-400/35'"
      />
      <span
        class="mt-1.5 inline-flex h-2.5 w-2.5 shrink-0 rounded-full transition duration-300"
        :class="getDotClass(item)"
      />
      <span class="min-w-0 transition duration-300 group-hover:translate-x-0.5" :class="[getTextClass(item), props.activeId === item.id ? 'translate-x-0.5' : '']">
        {{ item.text }}
      </span>
    </button>
  </div>
</template>
