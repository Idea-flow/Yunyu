<script setup lang="ts">
/**
 * 后台筛选区容器组件。
 * 作用：统一后台列表页搜索框、筛选项和操作按钮的两行布局结构，
 * 保证搜索框独占一行、筛选项支持换行、按钮区可灵活对齐。
 */
</script>

<template>
  <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
    <div class="space-y-4">
      <div>
        <slot name="search" />
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <slot />
      </div>
    </div>
  </UCard>
</template>
