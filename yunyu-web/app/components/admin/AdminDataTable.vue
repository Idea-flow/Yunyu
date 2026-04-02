<script setup lang="ts">
/**
 * 后台数据表格组件。
 * 作用：为后台各类列表页提供统一的横向滚动、表头、骨架屏和空状态容器，
 * 让文章、用户、分类、标签、专题等列表保持同一套表格工作台结构。
 */
withDefaults(defineProps<{
  isLoading?: boolean
  hasData: boolean
  minWidth?: string
  headerClass: string
  emptyTitle: string
  emptyIcon?: string
  skeletonCount?: number
}>(), {
  isLoading: false,
  minWidth: '980px',
  emptyIcon: 'i-lucide-search-x',
  skeletonCount: 3
})
</script>

<template>
  <div class="overflow-hidden rounded-[14px] border border-white/60 bg-white/64 dark:border-white/10 dark:bg-white/4">
    <div v-if="isLoading" class="space-y-3 p-4">
      <USkeleton
        v-for="index in skeletonCount"
        :key="index"
        class="h-[4.5rem] rounded-[10px]"
      />
    </div>

    <div v-else class="overflow-x-auto">
      <div :style="{ minWidth }">
        <div
          class="grid gap-4 border-b border-white/60 px-4 py-3 text-[0.68rem] font-semibold tracking-[0.14em] text-slate-400 uppercase dark:border-white/10 dark:text-slate-500"
          :class="headerClass"
        >
          <slot name="header" />
        </div>

        <div v-if="hasData" class="divide-y divide-white/60 dark:divide-white/10">
          <slot />
        </div>

        <div
          v-else
          class="flex flex-col items-center justify-center gap-3 px-6 py-12 text-center"
          :style="{ minWidth }"
        >
          <div class="inline-flex size-14 items-center justify-center rounded-[12px] bg-sky-50 text-sky-600 dark:bg-sky-400/12 dark:text-sky-300">
            <UIcon :name="emptyIcon" class="size-5" />
          </div>
          <p class="text-base font-medium text-slate-900 dark:text-slate-50">{{ emptyTitle }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
