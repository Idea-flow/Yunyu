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
    paddingLeft: `${Math.max(0, level - 1) * 0.78}rem`
  }
}

/**
 * 计算目录项容器样式。
 * 作用：统一目录项在普通态、悬停态和激活态下的边线、文字和焦点表现，
 * 让目录更接近站内前台的轻玻璃阅读场景，减少按钮感但保留精致层次。
 *
 * @param item 当前目录项
 * @returns 目录项样式类名
 */
function getItemClass(item: ArticleTocItem) {
  return props.activeId === item.id
    ? 'rounded-[999px] bg-[linear-gradient(90deg,rgba(224,242,254,0.98),rgba(240,249,255,0.74))] text-sky-950 shadow-[inset_0_1px_0_rgba(255,255,255,0.72)] ring-1 ring-sky-100/80 dark:bg-[linear-gradient(90deg,rgba(14,116,144,0.4),rgba(8,47,73,0.22))] dark:text-sky-50 dark:ring-sky-400/18 dark:shadow-none'
    : 'text-slate-500 hover:text-slate-900 dark:text-slate-400/82 dark:hover:text-slate-100'
}

/**
 * 计算目录项文字样式。
 * 作用：让高层级标题更突出，低层级标题更克制，维持目录节奏感。
 *
 * @param item 当前目录项
 * @returns 文字样式类名
 */
function getTextClass(item: ArticleTocItem) {
  if (item.level <= 1) {
    return 'text-[0.84rem] font-medium leading-6 tracking-[-0.015em]'
  }

  if (item.level === 2) {
    return 'text-[0.8rem] font-normal leading-[1.62] text-slate-500 dark:text-slate-300/82'
  }

  return 'text-[0.78rem] font-normal leading-[1.58] text-slate-400 dark:text-slate-400/72'
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
  <div class="space-y-0.5">
    <button
      v-for="item in props.items"
      :key="item.id"
      type="button"
      :data-toc-id="item.id"
      class="group relative flex min-h-11 w-full items-start overflow-hidden px-3 py-2 text-left transition duration-200 ease-out cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/45 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:focus-visible:ring-sky-400/25 dark:focus-visible:ring-offset-slate-950"
      :class="getItemClass(item)"
      :style="getIndentStyle(item.level)"
      @click="handleSelect(item)"
    >
      <span class="min-w-0 flex-1 pr-1 transition duration-200">
        <span class="block min-w-0 transition duration-200" :class="[getTextClass(item), props.activeId === item.id ? 'text-sky-950 dark:text-sky-50' : '']">
          {{ item.text }}
        </span>
      </span>
    </button>
  </div>
</template>
