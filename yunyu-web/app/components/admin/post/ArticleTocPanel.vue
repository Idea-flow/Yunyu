<script setup lang="ts">
import type { ArticleTocItem } from '../../../types/post'

/**
 * 文章目录面板组件。
 * 作用：在后台文章编辑页中展示当前 Markdown 的标题结构，帮助作者检查内容层级。
 */
const props = defineProps<{
  items: ArticleTocItem[]
}>()
</script>

<template>
  <div class="space-y-2">
    <template v-if="props.items.length">
      <div
        v-for="item in props.items"
        :key="item.id"
        class="rounded-[8px] px-3 py-2 text-sm transition-colors duration-200"
        :class="[
          item.level === 1 ? 'bg-slate-100/90 font-semibold text-slate-900 dark:bg-slate-800/90 dark:text-slate-50' : '',
          item.level === 2 ? 'bg-white/75 text-slate-700 dark:bg-slate-950/30 dark:text-slate-200' : '',
          item.level >= 3 ? 'bg-transparent text-slate-500 dark:text-slate-400' : ''
        ]"
        :style="{ paddingLeft: `${Math.max(0, item.level - 1) * 14 + 12}px` }"
      >
        {{ item.text }}
      </div>
    </template>

    <div v-else class="text-sm text-slate-500 dark:text-slate-400">暂无目录</div>
  </div>
</template>
