<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台输入框组件。
 * 作用：为后台搜索区和表单区提供统一的输入框视觉样式、焦点反馈与尺寸规范。
 */
const props = withDefaults(defineProps<{
  modelValue?: string | number | null
  placeholder?: string
  icon?: string
  type?: string
  disabled?: boolean
}>(), {
  modelValue: '',
  placeholder: '',
  icon: undefined,
  type: 'text',
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
  blur: [event: FocusEvent]
  change: [event: Event]
}>()

/**
 * 统一输入框视觉配置。
 * 作用：沉淀后台通用输入框的圆角、边框、阴影与焦点态，保证与选择器、文本域保持一致。
 */
const inputUi = computed(() => ({
  root: 'w-full',
  base: [
    'w-full min-h-12 rounded-[1.1rem] border border-slate-200/80 bg-white/94 px-4 py-3 text-[0.95rem] font-medium text-slate-800',
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
  leading: 'ps-4',
  leadingIcon: 'size-5 text-slate-400 dark:text-slate-500',
  trailing: 'pe-4',
  trailingIcon: 'size-5 text-slate-400 dark:text-slate-500'
}))

/**
 * 同步输入框值。
 *
 * @param value 当前输入值
 */
function handleUpdate(value: string | number | null) {
  emit('update:modelValue', value)
}
</script>

<template>
  <UInput
    :model-value="props.modelValue"
    :placeholder="props.placeholder"
    :icon="props.icon"
    :type="props.type"
    :disabled="props.disabled"
    size="xl"
    color="neutral"
    variant="outline"
    class="w-full"
    :ui="inputUi"
    @update:model-value="handleUpdate"
    @blur="emit('blur', $event)"
    @change="emit('change', $event)"
  />
</template>
