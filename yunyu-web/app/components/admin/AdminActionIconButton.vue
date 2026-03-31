<script setup lang="ts">
/**
 * 后台表格操作按钮组件。
 * 作用：统一列表右侧编辑、删除等图标按钮的尺寸、边界、状态反馈和可访问性表现。
 */
const props = withDefaults(defineProps<{
  icon: string
  label: string
  tone?: 'neutral' | 'danger'
  disabled?: boolean
}>(), {
  tone: 'neutral',
  disabled: false
})
</script>

<template>
  <button
    type="button"
    :disabled="props.disabled"
    :aria-label="props.label"
    :title="props.label"
    class="admin-action-icon-button"
    :class="props.tone === 'danger' ? 'admin-action-icon-button-danger' : 'admin-action-icon-button-neutral'"
  >
    <UIcon :name="props.icon" class="size-[18px]" />
  </button>
</template>
