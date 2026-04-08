<script setup lang="ts">
/**
 * 后台主操作按钮组件。
 * 作用：统一后台页面中搜索、新增、保存等主按钮的视觉样式，
 * 当前作为通用后台按钮组件的主按钮兼容包装层，避免旧页面调用方式失效。
 */
withDefaults(defineProps<{
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  loading?: boolean
  label: string
  loadingLabel?: string
  icon?: string
}>(), {
  type: 'button',
  disabled: false,
  loading: false,
  loadingLabel: '处理中...'
})
</script>

<template>
  <AdminButton
    :type="type"
    :disabled="disabled || loading"
    :loading="loading"
    :label="label"
    :loading-label="loadingLabel"
    :icon="icon"
    tone="primary"
    variant="solid"
  >
    {{ loading ? loadingLabel : label }}
  </AdminButton>
</template>
