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
 * 作用：统一目录项在普通态、悬停态和激活态下的背景、位移和阴影表现。
 *
 * @param item 当前目录项
 * @returns 目录项样式类名
 */
function getItemClass(item: ArticleTocItem) {
  return props.activeId === item.id
    ? 'bg-stone-950/[0.045] text-stone-950 ring-1 ring-stone-200/80 dark:bg-white/[0.06] dark:text-stone-50 dark:ring-white/10'
    : 'text-stone-500 hover:bg-stone-900/[0.03] hover:text-stone-900 dark:text-stone-300/72 dark:hover:bg-white/[0.04] dark:hover:text-stone-50'
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
    return 'text-[0.84rem] font-semibold leading-6 tracking-[-0.02em]'
  }

  if (item.level === 2) {
    return 'text-[0.81rem] font-medium leading-[1.62] text-stone-600 dark:text-stone-300/88'
  }

  return 'text-[0.78rem] font-medium leading-[1.58] text-stone-400 dark:text-stone-400/80'
}

/**
 * 计算目录项节点样式。
 * 作用：通过节点颜色和边框变化强化当前章节定位，同时保留普通目录的安静阅读感。
 *
 * @param item 当前目录项
 * @returns 节点样式类名
 */
function getDotClass(item: ArticleTocItem) {
  return props.activeId === item.id
    ? 'bg-stone-900 dark:bg-stone-100'
    : 'bg-stone-300 group-hover:bg-stone-500 dark:bg-stone-600 dark:group-hover:bg-stone-400'
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
      class="group relative flex min-h-11 w-full items-start gap-3 overflow-hidden rounded-[0.95rem] px-3 py-2.5 text-left transition duration-200 ease-out cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stone-300/70 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:focus-visible:ring-stone-500/55 dark:focus-visible:ring-offset-slate-950"
      :class="getItemClass(item)"
      :style="getIndentStyle(item.level)"
      @click="handleSelect(item)"
    >
      <span class="flex min-w-0 flex-1 items-start gap-3">
        <span
          class="relative mt-[0.7rem] inline-flex h-1.5 w-1.5 shrink-0 rounded-full transition duration-200"
          :class="getDotClass(item)"
        />

        <span class="min-w-0 flex-1 pr-1 transition duration-200">
          <span class="block min-w-0 transition duration-200" :class="[getTextClass(item), props.activeId === item.id ? 'text-stone-950 dark:text-stone-50' : '']">
            {{ item.text }}
          </span>
        </span>
      </span>
    </button>
  </div>
</template>
