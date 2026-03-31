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
      base: 'admin-select-base',
      value: 'text-[color:var(--admin-text-strong)]',
      placeholder: 'text-[color:var(--admin-text-muted)]',
      trailingIcon: 'text-[color:var(--admin-text-muted)]',
      content: 'admin-select-content',
      item: 'admin-select-item',
      itemLabel: 'text-sm',
      itemDescription: 'text-[color:var(--admin-text-muted)]'
    }"
    @update:model-value="handleUpdate"
  />
</template>
