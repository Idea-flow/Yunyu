<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台选择器组件。
 * 作用：为后台筛选区、分页区与表单区提供统一的简洁软润型选择器样式与下拉面板表现。
 */
const props = withDefaults(defineProps<{
  modelValue?: string | number | null
  items: any[]
  placeholder?: string
  disabled?: boolean
  icon?: string
}>(), {
  modelValue: null,
  placeholder: '',
  disabled: false,
  icon: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
}>()

/**
 * 统一选择器视觉配置。
 * 作用：沉淀后台通用下拉框的圆角、阴影、焦点态和下拉面板风格，避免页面各自重复定义。
 */
const selectUi = computed(() => ({
  base: [
    'w-full min-h-12 rounded-[1.1rem] border border-slate-200/80 bg-white/94 px-4 py-3 text-slate-800',
    'shadow-[0_10px_24px_-22px_rgba(15,23,42,0.28)]',
    'transition-[border-color,box-shadow,background-color,color] duration-200',
    'placeholder:text-slate-400',
    'hover:border-slate-300 hover:bg-white',
    'focus-visible:border-sky-400 focus-visible:bg-white focus-visible:ring-3 focus-visible:ring-sky-100/90',
    'disabled:cursor-not-allowed disabled:opacity-60',
    'dark:border-slate-700/80 dark:bg-slate-950/80 dark:text-slate-100',
    'dark:shadow-[0_14px_30px_-24px_rgba(0,0,0,0.46)] dark:placeholder:text-slate-500',
    'dark:hover:border-slate-600 dark:hover:bg-slate-950/92',
    'dark:focus-visible:border-sky-300 dark:focus-visible:bg-slate-950 dark:focus-visible:ring-sky-400/20'
  ].join(' '),
  leadingIcon: 'text-slate-400 dark:text-slate-500',
  value: 'truncate pr-2 text-[0.95rem] font-medium text-slate-800 dark:text-slate-100',
  placeholder: 'truncate pr-2 text-[0.95rem] font-medium text-slate-400 dark:text-slate-500',
  trailingIcon: 'text-slate-400 transition-transform duration-200 group-data-[state=open]:rotate-180 dark:text-slate-500',
  content: [
    'min-w-[var(--reka-select-trigger-width)] max-w-[min(22rem,calc(100vw-2rem))] overflow-hidden rounded-[1.2rem] border border-slate-200/90 bg-white/98 p-1.5',
    'shadow-[0_18px_42px_-24px_rgba(15,23,42,0.3)] backdrop-blur-xl',
    'dark:border-slate-700 dark:bg-slate-900/96 dark:shadow-[0_22px_46px_-26px_rgba(0,0,0,0.58)]'
  ].join(' '),
  viewport: [
    'max-h-72 space-y-1 overflow-y-auto p-0.5',
    '[scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.45)_transparent]',
    '[&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent',
    '[&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80',
    'dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80'
  ].join(' '),
  item: [
    'min-h-10 cursor-pointer rounded-[0.95rem] px-3 py-2.5 text-slate-600',
    'transition-[background-color,color] duration-150',
    'data-highlighted:bg-slate-100 data-highlighted:text-slate-900',
    'data-[state=checked]:bg-sky-50 data-[state=checked]:text-sky-700',
    'dark:text-slate-300 dark:data-highlighted:bg-slate-800 dark:data-highlighted:text-slate-50',
    'dark:data-[state=checked]:bg-sky-400/12 dark:data-[state=checked]:text-sky-200'
  ].join(' '),
  itemLeadingIcon: 'text-slate-400 dark:text-slate-500',
  itemLabel: 'truncate text-sm font-medium',
  itemDescription: 'text-xs text-slate-400 dark:text-slate-500',
  empty: 'px-3 py-3 text-sm text-slate-400 dark:text-slate-500'
}))

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
    :icon="props.icon"
    size="xl"
    color="neutral"
    variant="outline"
    class="group w-full"
    :ui="selectUi"
    @update:model-value="handleUpdate"
  />
</template>
