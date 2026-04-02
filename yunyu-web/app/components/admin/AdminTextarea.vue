<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台文本域组件。
 * 作用：为后台长文本输入场景提供统一的文本域视觉风格与交互反馈。
 */
const props = withDefaults(defineProps<{
  modelValue?: string | number | null
  placeholder?: string
  rows?: number
  autoresize?: boolean
  disabled?: boolean
  textareaClass?: string
}>(), {
  modelValue: '',
  placeholder: '',
  rows: 4,
  autoresize: false,
  disabled: false,
  textareaClass: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
  blur: [event: FocusEvent]
  change: [event: Event]
}>()

/**
 * 统一文本域视觉配置。
 * 作用：沉淀后台长文本输入的圆角、边框、阴影与焦点态，保证与输入框、选择器统一。
 */
const textareaUi = computed(() => ({
  root: 'w-full',
  base: [
    'min-h-28 w-full rounded-[8px] border border-slate-200/80 bg-white/94 px-3.5 py-2.5 leading-7 text-[0.95rem] font-medium text-slate-800',
    'shadow-[0_8px_18px_-18px_rgba(15,23,42,0.22)]',
    'transition-[border-color,box-shadow,background-color,color] duration-200',
    'placeholder:text-slate-400',
    'hover:border-slate-300 hover:bg-white',
    'focus-visible:border-sky-400 focus-visible:bg-white focus-visible:ring-3 focus-visible:ring-sky-100/90',
    'disabled:cursor-not-allowed disabled:opacity-60',
    'dark:border-slate-700/80 dark:bg-slate-950/80 dark:text-slate-100',
    'dark:shadow-[0_14px_30px_-24px_rgba(0,0,0,0.46)] dark:placeholder:text-slate-500',
    'dark:hover:border-slate-600 dark:hover:bg-slate-950/92',
    'dark:focus-visible:border-sky-300 dark:focus-visible:bg-slate-950 dark:focus-visible:ring-sky-400/20',
    props.textareaClass
  ].join(' ')
}))

/**
 * 同步文本域值。
 *
 * @param value 当前输入值
 */
function handleUpdate(value: string | number | null) {
  emit('update:modelValue', value)
}
</script>

<template>
  <UTextarea
    :model-value="props.modelValue"
    :placeholder="props.placeholder"
    :rows="props.rows"
    :autoresize="props.autoresize"
    :disabled="props.disabled"
    size="xl"
    color="neutral"
    variant="outline"
    class="w-full"
    :ui="textareaUi"
    @update:model-value="handleUpdate"
    @blur="emit('blur', $event)"
    @change="emit('change', $event)"
  />
</template>
