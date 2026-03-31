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
    <template #body>
      <div class="admin-muted-panel flex items-start gap-4 rounded-[24px] p-4">
        <div class="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-rose-500/12 text-rose-500 dark:bg-rose-400/14 dark:text-rose-300">
          <UIcon name="i-lucide-shield-alert" class="size-5" />
        </div>
        <div class="space-y-1">
          <p class="text-sm font-semibold text-[color:var(--admin-text-strong)]">{{ props.title }}</p>
          <p class="text-sm leading-7 text-[color:var(--admin-text-body)]">{{ props.description }}</p>
        </div>
      </div>
    </template>

    <template #footer>
      <div class="flex w-full justify-end gap-3">
        <UButton
          :label="props.cancelLabel"
          color="neutral"
          variant="ghost"
          @click="handleOpenChange(false)"
        />
        <AdminPrimaryButton
          :label="props.confirmLabel"
          loading-label="处理中..."
          :loading="props.loading"
          icon="i-lucide-trash-2"
          @click="handleConfirm"
        />
      </div>
    </template>
  </UModal>
</template>
