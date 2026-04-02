<script setup lang="ts">
/**
 * 后台筛选区容器组件。
 * 作用：统一后台列表页搜索框、筛选项和操作按钮的两行布局结构，
 * 保证搜索框独占一行、筛选项支持换行、按钮区可灵活对齐。
 */
</script>

<template>
  <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.76),rgba(255,255,255,0.58))] px-4 py-3 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.74),rgba(15,23,42,0.64))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
    <div class="space-y-3">
      <div>
        <slot name="search" />
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <slot />
      </div>
    </div>
  </section>
</template>
