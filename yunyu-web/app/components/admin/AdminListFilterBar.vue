<script setup lang="ts">
/**
 * 后台列表筛选条组件。
 * 作用：为后台列表页提供统一的搜索区、筛选项区和操作按钮区布局，
 * 与数据表格组件配套组成完整的列表工作台。
 */
</script>

<template>
  <section class="rounded-[16px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.76),rgba(255,255,255,0.58))] px-4 py-3 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.74),rgba(15,23,42,0.64))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
      <div class="w-full lg:flex-1">
        <slot name="search" />
      </div>

      <div class="flex flex-wrap items-center gap-3 lg:justify-end">
        <slot name="filters" />

        <div class="flex w-full flex-wrap items-center gap-2 pt-1 sm:ml-auto sm:w-auto sm:pt-0">
          <slot name="actions" />
        </div>
      </div>
    </div>
  </section>
</template>
