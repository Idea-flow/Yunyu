<script setup lang="ts">
/**
 * 前台应用根组件。
 * 作用：统一承接全局布局、页面切换和 Nuxt UI 通用能力配置，
 * 这里集中定义全站 toast 的展示位置与基础体验。
 */
const globalToaster = {
  position: 'top-center' as const,
  duration: 2000,
  progress: false,
  expand: false,
  max: 3,
  ui: {
    viewport: 'fixed left-1/2 top-5 z-[120] flex w-[min(calc(100vw-1.5rem),26rem)] -translate-x-1/2 flex-col px-0 sm:top-6',
    base: 'pointer-events-auto absolute inset-x-0 z-(--index) transform-(--transform) data-[expanded=false]:data-[front=false]:h-(--front-height) data-[expanded=false]:data-[front=false]:*:opacity-0 data-[front=false]:*:transition-opacity data-[front=false]:*:duration-100 data-[state=closed]:animate-[toast-closed_180ms_ease-in-out] data-[state=closed]:data-[expanded=false]:data-[front=false]:animate-[toast-collapsed-closed_180ms_ease-in-out] data-[state=open]:data-[swipe=move]:transition-none transition-[transform,translate,height,opacity] duration-200 ease-out'
  }
}
</script>

<template>
  <UApp :toaster="globalToaster">
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
