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
      overlay: 'bg-slate-950/36 backdrop-blur-[10px] dark:bg-slate-950/60',
      content: 'w-[calc(100vw-2rem)] max-w-lg rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))] dark:shadow-[0_28px_54px_-36px_rgba(0,0,0,0.56)]',
      header: 'hidden',
      body: 'px-6 pt-6 pb-5',
      footer: 'border-t border-white/60 bg-white/36 px-6 pt-4 pb-6 dark:border-white/10 dark:bg-white/[0.03]'
    }"
    @update:open="handleOpenChange"
  >
    <template #body>
      <div class="flex items-start gap-4 rounded-[12px] border border-white/60 bg-white/52 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]">
        <div class="flex size-10 shrink-0 items-center justify-center rounded-[10px] border border-rose-200/80 bg-rose-500/10 text-rose-500 dark:border-rose-400/18 dark:bg-rose-400/14 dark:text-rose-300">
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
          class="rounded-[10px]"
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
