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
    :ui="{
      overlay: 'bg-slate-950/35 backdrop-blur-[6px] dark:bg-slate-950/55',
      content: 'w-[calc(100vw-2rem)] max-w-lg rounded-[2rem] border border-slate-200/80 bg-white/95 shadow-[0_40px_80px_-42px_rgba(15,23,42,0.42)] backdrop-blur-2xl dark:border-slate-700 dark:bg-slate-950/92 dark:shadow-[0_42px_80px_-42px_rgba(0,0,0,0.72)]',
      header: 'hidden',
      body: 'px-6 pt-6 pb-5',
      footer: 'border-t border-slate-200 bg-slate-50/80 px-6 pt-4 pb-6 dark:border-slate-800 dark:bg-slate-900/65'
    }"
    @update:open="handleOpenChange"
  >
    <template #body>
      <div class="flex items-start gap-4 rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
        <div class="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-rose-500/12 text-rose-500 dark:bg-rose-400/14 dark:text-rose-300">
          <UIcon name="i-lucide-shield-alert" class="size-5" />
        </div>
        <div class="space-y-1">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ props.title }}</p>
          <p class="text-sm leading-7 text-slate-600 dark:text-slate-300">{{ props.description }}</p>
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
