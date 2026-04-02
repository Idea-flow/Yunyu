<script setup lang="ts">
/**
 * 后台主操作按钮组件。
 * 作用：统一后台页面中搜索、新增、保存等主按钮的视觉样式，
 * 确保在明亮与暗黑模式下都具备足够清晰的可见性。
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
  <button
    :type="type"
    :disabled="disabled || loading"
    class="group inline-flex min-h-9 min-w-24 cursor-pointer items-center justify-center gap-2 rounded-[10px] border border-sky-400/20 bg-[linear-gradient(135deg,#38bdf8,#0ea5e9)] px-4 py-2 text-sm font-semibold whitespace-nowrap text-white shadow-[0_12px_24px_-20px_rgba(14,165,233,0.56)] transition duration-200 hover:border-sky-400/25 hover:bg-[linear-gradient(135deg,#22c3ee,#0284c7)] hover:shadow-[0_16px_26px_-20px_rgba(14,165,233,0.62)] focus:outline-none focus:ring-2 focus:ring-sky-400/30 focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-70 dark:border-sky-300/15 dark:bg-[linear-gradient(135deg,#38bdf8,#7dd3fc)] dark:text-slate-950 dark:hover:bg-[linear-gradient(135deg,#67e8f9,#38bdf8)] dark:focus:ring-sky-300/30 dark:focus:ring-offset-slate-950"
  >
    <UIcon
      v-if="icon && !loading"
      :name="icon"
      class="size-4 transition duration-200 group-hover:translate-x-0.5"
    />
    {{ loading ? loadingLabel : label }}
  </button>
</template>
