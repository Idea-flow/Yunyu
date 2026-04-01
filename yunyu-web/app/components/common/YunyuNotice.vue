<script setup lang="ts">
import { computed } from 'vue'

/**
 * 云屿提示组件。
 * 作用：统一承载成功、错误、信息、警告等提示内容的视觉风格，
 * 作为前后台都可复用的轻量级状态提示基础组件。
 */
const props = withDefaults(defineProps<{
  type?: 'success' | 'error' | 'info' | 'warning'
  title: string
  description?: string
  icon?: string
}>(), {
  type: 'info',
  description: '',
  icon: ''
})

/**
 * 计算当前提示组件的默认图标。
 * 作用：当外层未传入自定义图标时，根据提示类型回退到统一图标语义。
 */
const resolvedIcon = computed(() => {
  if (props.icon) {
    return props.icon
  }

  switch (props.type) {
    case 'success':
      return 'i-lucide-badge-check'
    case 'error':
      return 'i-lucide-octagon-alert'
    case 'warning':
      return 'i-lucide-triangle-alert'
    default:
      return 'i-lucide-sparkles'
  }
})

/**
 * 计算组件容器样式。
 * 作用：统一不同提示类型下的边框、背景与文本色板，保证提示风格一致。
 */
const containerClassName = computed(() => {
  switch (props.type) {
    case 'success':
      return 'border-emerald-200/85 bg-emerald-50/92 text-emerald-900 dark:border-emerald-400/25 dark:bg-emerald-400/10 dark:text-emerald-100'
    case 'error':
      return 'border-rose-200/85 bg-rose-50/92 text-rose-900 dark:border-rose-400/25 dark:bg-rose-400/10 dark:text-rose-100'
    case 'warning':
      return 'border-amber-200/85 bg-amber-50/92 text-amber-900 dark:border-amber-400/25 dark:bg-amber-400/10 dark:text-amber-100'
    default:
      return 'border-sky-200/85 bg-sky-50/92 text-sky-900 dark:border-sky-400/25 dark:bg-sky-400/10 dark:text-sky-100'
  }
})

/**
 * 计算图标容器样式。
 * 作用：让不同提示类型在左侧图标区也保持一致的层次与品牌感。
 */
const iconWrapperClassName = computed(() => {
  switch (props.type) {
    case 'success':
      return 'bg-emerald-500/12 text-emerald-600 dark:bg-emerald-400/16 dark:text-emerald-200'
    case 'error':
      return 'bg-rose-500/12 text-rose-600 dark:bg-rose-400/16 dark:text-rose-200'
    case 'warning':
      return 'bg-amber-500/12 text-amber-600 dark:bg-amber-400/16 dark:text-amber-200'
    default:
      return 'bg-sky-500/12 text-sky-600 dark:bg-sky-400/16 dark:text-sky-200'
  }
})
</script>

<template>
  <div
    class="flex items-start gap-3 rounded-[24px] border p-4 shadow-[0_18px_40px_-30px_rgba(15,23,42,0.28)] backdrop-blur-xl"
    :class="containerClassName"
  >
    <div
      class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl"
      :class="iconWrapperClassName"
    >
      <UIcon :name="resolvedIcon" class="size-5" />
    </div>

    <div class="min-w-0 flex-1">
      <p class="text-sm font-semibold leading-6">
        {{ props.title }}
      </p>
      <p
        v-if="props.description"
        class="mt-1 text-sm leading-6 opacity-80"
      >
        {{ props.description }}
      </p>
    </div>
  </div>
</template>
