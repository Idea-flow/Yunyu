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
      base: 'admin-input-base',
      leading: 'ps-4',
      leadingIcon: 'size-5 text-[color:var(--admin-text-muted)]',
      trailing: 'pe-4',
      trailingIcon: 'size-5 text-[color:var(--admin-text-muted)]'
    }"
    @update:model-value="handleUpdate"
    @blur="emit('blur', $event)"
    @change="emit('change', $event)"
  />
</template>
