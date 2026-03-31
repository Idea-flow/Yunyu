<script setup lang="ts">
/**
 * 后台文章新增页。
 * 作用：以独立页面承载文章创建流程，适配更长的内容编辑任务。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})
</script>

<template>
  <AdminPostEditorScreen mode="create" />
</template>
