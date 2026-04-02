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
    class="inline-flex min-h-9 min-w-9 cursor-pointer items-center justify-center rounded-[8px] border bg-white transition duration-200 focus:outline-none focus:ring-4 disabled:cursor-not-allowed disabled:opacity-55 dark:bg-slate-900/85"
    :class="props.tone === 'danger'
      ? 'border-rose-200 text-rose-500 hover:border-rose-300 hover:bg-rose-50 hover:text-rose-600 focus:ring-rose-100 dark:border-rose-500/30 dark:text-rose-300 dark:hover:bg-rose-500/10 dark:hover:text-rose-200 dark:focus:ring-rose-400/20'
      : 'border-slate-200 text-slate-600 hover:border-sky-200 hover:bg-sky-50 hover:text-slate-900 focus:ring-sky-100 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-sky-400/10 dark:hover:text-slate-50 dark:focus:ring-sky-400/20'"
  >
    <UIcon :name="props.icon" class="size-[18px]" />
  </button>
</template>
