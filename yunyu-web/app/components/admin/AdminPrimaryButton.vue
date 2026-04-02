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
    class="group inline-flex min-h-10 min-w-24 cursor-pointer items-center justify-center gap-2 rounded-[8px] border border-sky-500/20 bg-sky-500 px-4 text-sm font-semibold whitespace-nowrap text-white transition duration-200 hover:bg-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-70 dark:border-sky-300/20 dark:bg-sky-400 dark:text-slate-950 dark:hover:bg-sky-300 dark:focus:ring-sky-300 dark:focus:ring-offset-slate-950"
  >
    <UIcon
      v-if="icon && !loading"
      :name="icon"
      class="size-4 transition duration-200 group-hover:translate-x-0.5"
    />
    {{ loading ? loadingLabel : label }}
  </button>
</template>
