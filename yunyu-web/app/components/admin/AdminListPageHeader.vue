<script setup lang="ts">
/**
 * 后台列表页头组件。
 * 作用：统一后台列表页顶部标题区和主操作按钮区的布局，
 * 与筛选条和数据表格配套组成完整的列表工作台结构。
 */
defineProps<{
  title: string
}>()
</script>

<template>
  <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
    <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
      <div class="min-w-0">
        <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">{{ title }}</h1>
      </div>

      <div class="flex items-center gap-2">
        <slot name="actions" />
      </div>
    </div>
  </section>
</template>
