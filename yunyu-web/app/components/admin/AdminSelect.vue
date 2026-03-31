<script setup lang="ts">
/**
 * 后台选择器组件。
 * 作用：为后台筛选区和表单区提供统一的选择器样式与下拉面板表现。
 */
const props = withDefaults(defineProps<{
  modelValue?: string | number | null
  items: any[]
  placeholder?: string
  disabled?: boolean
}>(), {
  modelValue: null,
  placeholder: '',
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
}>()

/**
 * 同步选择器值。
 *
 * @param value 当前选中值
 */
function handleUpdate(value: string | number | null) {
  emit('update:modelValue', value)
}
</script>

<template>
  <USelect
    :model-value="props.modelValue"
    :items="props.items"
    :placeholder="props.placeholder"
    :disabled="props.disabled"
    size="xl"
    color="neutral"
    variant="outline"
    class="w-full"
    :ui="{
      base: 'w-full rounded-[1.15rem] border border-slate-200/80 bg-white/90 px-4 py-3 text-slate-800 shadow-[0_10px_22px_-18px_rgba(15,23,42,0.3)] transition duration-200 placeholder:text-slate-400 focus-visible:border-sky-400 focus-visible:ring-4 focus-visible:ring-sky-100 dark:border-slate-700 dark:bg-slate-900/85 dark:text-slate-100 dark:shadow-[0_14px_30px_-24px_rgba(0,0,0,0.55)] dark:placeholder:text-slate-500 dark:focus-visible:border-sky-300 dark:focus-visible:ring-sky-400/20',
      value: 'text-slate-800 dark:text-slate-100',
      placeholder: 'text-slate-400 dark:text-slate-500',
      trailingIcon: 'text-slate-400 dark:text-slate-500',
      content: 'rounded-[1.15rem] border border-slate-200 bg-white/95 p-1 shadow-[0_18px_42px_-24px_rgba(15,23,42,0.35)] backdrop-blur-xl dark:border-slate-700 dark:bg-slate-900/95',
      item: 'rounded-2xl text-slate-600 transition-colors duration-200 data-highlighted:bg-sky-50 data-highlighted:text-slate-900 dark:text-slate-300 dark:data-highlighted:bg-sky-400/15 dark:data-highlighted:text-slate-50',
      itemLabel: 'text-sm',
      itemDescription: 'text-slate-400 dark:text-slate-500'
    }"
    @update:model-value="handleUpdate"
  />
</template>
