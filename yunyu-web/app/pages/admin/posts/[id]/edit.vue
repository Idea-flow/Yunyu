<script setup lang="ts">
/**
 * 后台文章编辑页。
 * 作用：以独立页面承载文章修改流程，便于处理更复杂的内容编辑场景。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const route = useRoute()

/**
 * 解析当前路由中的文章 ID。
 * 用于为共用编辑器组件提供明确的文章主键。
 */
const postId = computed(() => {
  const value = Number(route.params.id)
  return Number.isFinite(value) ? value : null
})
</script>

<template>
  <AdminPostEditorScreen mode="edit" :post-id="postId" />
</template>
