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
      content: ['admin-modal-content w-[calc(100vw-2rem)]', contentWidthClass],
      header: 'admin-modal-header',
      body: 'admin-modal-body',
      footer: 'admin-modal-footer',
      close: 'top-5 end-5',
      title: 'hidden',
      description: 'hidden'
    }"
    @update:open="handleOpenChange"
  >
    <template #header>
      <div class="flex items-start gap-4">
        <div class="admin-modal-icon-wrap">
          <UIcon :name="props.icon" class="size-5" />
        </div>

        <div class="min-w-0 space-y-1">
          <p class="admin-kicker">{{ props.eyebrow }}</p>
          <p class="text-xl font-semibold tracking-tight text-[color:var(--admin-text-strong)]">
            {{ props.title }}
          </p>
          <p
            v-if="props.description"
            class="max-w-2xl text-sm leading-7 text-[color:var(--admin-text-body)]"
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
