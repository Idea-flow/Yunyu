<script setup lang="ts">
/**
 * 后台筛选区容器组件。
 * 作用：统一后台列表页搜索框、筛选项和操作按钮的两行布局结构，
 * 保证搜索框独占一行、筛选项支持换行、按钮区可灵活对齐。
 */
</script>

<template>
  <section class="admin-surface-soft px-4 py-4 md:px-5">
    <div class="space-y-4">
      <div>
        <slot name="search" />
      </div>

      <div class="flex flex-wrap items-center gap-3">
        <slot />
      </div>
    </div>
  </section>
</template>
