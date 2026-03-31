<script setup lang="ts">
/**
 * 后台确认弹窗组件。
 * 作用：为后台危险操作提供统一的二次确认弹窗，避免各页面重复拼装确认交互。
 */
const props = withDefaults(defineProps<{
  open: boolean
  title: string
  description: string
  confirmLabel?: string
  cancelLabel?: string
  loading?: boolean
}>(), {
  confirmLabel: '确认',
  cancelLabel: '取消',
  loading: false
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  confirm: []
}>()

/**
 * 同步弹窗开关状态。
 *
 * @param value 是否打开
 */
function handleOpenChange(value: boolean) {
  emit('update:open', value)
}

/**
 * 执行确认操作。
 * 点击后由父组件真正处理删除或其他危险动作。
 */
function handleConfirm() {
  emit('confirm')
}
</script>

<template>
  <UModal
    :open="props.open"
    :title="props.title"
    :description="props.description"
    @update:open="handleOpenChange"
  >
    <template #footer>
      <div class="flex w-full justify-end gap-3">
        <UButton
          :label="props.cancelLabel"
          color="neutral"
          variant="ghost"
          @click="handleOpenChange(false)"
        />
        <UButton
          :label="props.confirmLabel"
          color="error"
          :loading="props.loading"
          @click="handleConfirm"
        />
      </div>
    </template>
  </UModal>
</template>
