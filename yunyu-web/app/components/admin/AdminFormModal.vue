<script setup lang="ts">
/**
 * 后台表单弹窗组件。
 * 作用：统一后台新增与编辑弹窗的尺寸、头部层级、正文留白和底部操作区结构。
 */
const props = withDefaults(defineProps<{
  open: boolean
  title: string
  description?: string
  eyebrow?: string
  icon?: string
  width?: 'default' | 'wide' | 'editor'
}>(), {
  description: '',
  eyebrow: '表单操作',
  icon: 'i-lucide-pen-square',
  width: 'wide'
})

const emit = defineEmits<{
  'update:open': [value: boolean]
}>()

/**
 * 解析弹窗宽度类名。
 * 根据不同表单复杂度切换更合适的最大宽度。
 */
const contentWidthClass = computed(() => {
  switch (props.width) {
    case 'editor':
      return 'max-w-4xl'
    case 'wide':
      return 'max-w-2xl'
    default:
      return 'max-w-xl'
  }
})

/**
 * 同步弹窗开关状态。
 *
 * @param value 是否打开
 */
function handleOpenChange(value: boolean) {
  emit('update:open', value)
}
</script>

<template>
  <UModal
    :open="props.open"
    scrollable
    :ui="{
      overlay: 'bg-slate-950/35 backdrop-blur-[6px] dark:bg-slate-950/55',
      content: ['w-[calc(100vw-2rem)] overflow-hidden rounded-[2rem] border border-slate-200/80 bg-white/95 shadow-[0_40px_80px_-42px_rgba(15,23,42,0.42)] backdrop-blur-2xl dark:border-slate-700 dark:bg-slate-950/92 dark:shadow-[0_42px_80px_-42px_rgba(0,0,0,0.72)]', contentWidthClass],
      header: 'border-b border-slate-200 px-6 pt-6 pb-4 dark:border-slate-800',
      body: 'bg-white/50 px-6 py-5 dark:bg-slate-950/40',
      footer: 'border-t border-slate-200 bg-slate-50/80 px-6 pt-4 pb-6 dark:border-slate-800 dark:bg-slate-900/65',
      close: 'top-5 end-5',
      title: 'hidden',
      description: 'hidden'
    }"
    @update:open="handleOpenChange"
  >
    <template #header>
      <div class="flex items-start gap-4">
        <div class="inline-flex size-12 shrink-0 items-center justify-center rounded-[1.1rem] border border-sky-200 bg-sky-50 text-sky-600 shadow-[0_14px_28px_-24px_rgba(14,165,233,0.55)] dark:border-sky-400/25 dark:bg-sky-400/10 dark:text-sky-300">
          <UIcon :name="props.icon" class="size-5" />
        </div>

        <div class="min-w-0 space-y-1">
          <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">{{ props.eyebrow }}</p>
          <p class="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">
            {{ props.title }}
          </p>
          <p
            v-if="props.description"
            class="max-w-2xl text-sm leading-7 text-slate-600 dark:text-slate-300"
          >
            {{ props.description }}
          </p>
        </div>
      </div>
    </template>

    <template #body="{ close }">
      <slot name="body" :close="close" />
    </template>

    <template #footer="{ close }">
      <slot name="footer" :close="close" />
    </template>
  </UModal>
</template>
