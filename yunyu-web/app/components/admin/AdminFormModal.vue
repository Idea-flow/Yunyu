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
  eyebrow: '',
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
      overlay: 'bg-slate-950/36 backdrop-blur-[10px] dark:bg-slate-950/60',
      content: ['w-[calc(100vw-2rem)] overflow-hidden rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))] dark:shadow-[0_28px_54px_-36px_rgba(0,0,0,0.56)]', contentWidthClass],
      header: 'border-b border-white/60 px-6 pt-6 pb-4 dark:border-white/10',
      body: 'bg-transparent px-6 py-5',
      footer: 'border-t border-white/60 bg-white/36 px-6 pt-4 pb-6 dark:border-white/10 dark:bg-white/[0.03]',
      close: 'top-5 end-5 rounded-[10px] border border-white/60 bg-white/72 text-slate-500 backdrop-blur-md transition duration-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:text-slate-50',
      title: 'hidden',
      description: 'hidden'
    }"
    @update:open="handleOpenChange"
  >
    <template #header>
      <div class="flex items-start gap-4">
        <div class="inline-flex size-10 shrink-0 items-center justify-center rounded-[10px] border border-sky-200/80 bg-[linear-gradient(135deg,rgba(240,249,255,0.98),rgba(255,247,237,0.82))] text-sky-600 shadow-[0_10px_20px_-20px_rgba(14,165,233,0.52)] dark:border-sky-400/20 dark:bg-[linear-gradient(135deg,rgba(56,189,248,0.14),rgba(251,146,60,0.08))] dark:text-sky-300">
          <UIcon :name="props.icon" class="size-5" />
        </div>

        <div class="min-w-0 space-y-1">
          <p
            v-if="props.eyebrow"
            class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500"
          >
            {{ props.eyebrow }}
          </p>
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
