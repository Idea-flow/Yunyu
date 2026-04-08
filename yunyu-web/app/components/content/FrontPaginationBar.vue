<script setup lang="ts">
/**
 * 前台分页条兼容组件。
 * 作用：兼容旧引用路径，内部统一转发到前台公共分页组件，避免存量页面或历史代码失效。
 */
defineProps<{
  pageNo: number
  totalPages: number
}>()

defineEmits<{
  change: [pageNo: number]
}>()
</script>

<template>
  <YunyuPagination
    :page-no="pageNo"
    :total-pages="totalPages"
    @change="$emit('change', $event)"
  />
</template>
