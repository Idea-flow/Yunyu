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
    class="inline-flex min-h-9 min-w-9 cursor-pointer items-center justify-center rounded-[10px] border bg-white/76 backdrop-blur-md transition duration-200 focus:outline-none focus:ring-2 disabled:cursor-not-allowed disabled:opacity-55 dark:bg-white/[0.06]"
    :class="props.tone === 'danger'
      ? 'border-rose-200/90 text-rose-500 shadow-[0_10px_18px_-18px_rgba(244,63,94,0.3)] hover:border-rose-300 hover:bg-rose-50/92 hover:text-rose-600 focus:ring-rose-200/35 dark:border-rose-500/20 dark:text-rose-300 dark:hover:bg-rose-500/10 dark:hover:text-rose-200 dark:focus:ring-rose-400/20'
      : 'border-white/75 text-slate-600 shadow-[0_10px_18px_-18px_rgba(15,23,42,0.16)] hover:border-[var(--admin-primary-border)] hover:bg-[var(--admin-primary-soft-surface)] hover:text-slate-900 focus:ring-[var(--admin-primary-ring)] dark:border-white/10 dark:text-slate-300 dark:hover:bg-[color:color-mix(in_srgb,var(--site-primary-color)_12%,transparent)] dark:hover:text-slate-50 dark:focus:ring-[var(--admin-primary-ring)]'"
  >
    <UIcon :name="props.icon" class="size-4" />
  </button>
</template>
