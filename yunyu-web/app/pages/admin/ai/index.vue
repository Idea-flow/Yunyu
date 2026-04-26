<script setup lang="ts">
/**
 * 后台 AI 模块默认入口页。
 * 作用：统一把 `/admin/ai` 重定向到 AI 配置页，避免直接访问目录路由时出现空白。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

await navigateTo('/admin/ai/config', { replace: true })
</script>

<template>
  <div class="sr-only">正在跳转到 AI 配置页...</div>
</template>
