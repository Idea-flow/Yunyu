<script setup lang="ts">
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
    :ui="{
      root: 'w-full',
      base: 'w-full rounded-[1.15rem] border border-slate-200/80 bg-white/90 px-4 py-3 text-slate-800 shadow-[0_10px_22px_-18px_rgba(15,23,42,0.3)] transition duration-200 placeholder:text-slate-400 focus-visible:border-sky-400 focus-visible:ring-4 focus-visible:ring-sky-100 dark:border-slate-700 dark:bg-slate-900/85 dark:text-slate-100 dark:shadow-[0_14px_30px_-24px_rgba(0,0,0,0.55)] dark:placeholder:text-slate-500 dark:focus-visible:border-sky-300 dark:focus-visible:ring-sky-400/20',
      leading: 'ps-4',
      leadingIcon: 'size-5 text-slate-400 dark:text-slate-500',
      trailing: 'pe-4',
      trailingIcon: 'size-5 text-slate-400 dark:text-slate-500'
    }"
    @update:model-value="handleUpdate"
    @blur="emit('blur', $event)"
    @change="emit('change', $event)"
  />
</template>
