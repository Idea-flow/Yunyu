<script setup lang="ts">
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
}>(), {
  modelValue: '',
  placeholder: '',
  rows: 4,
  autoresize: false,
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
  blur: [event: FocusEvent]
  change: [event: Event]
}>()

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
    :ui="{
      root: 'w-full',
      base: 'admin-textarea-base'
    }"
    @update:model-value="handleUpdate"
    @blur="emit('blur', $event)"
    @change="emit('change', $event)"
  />
</template>
