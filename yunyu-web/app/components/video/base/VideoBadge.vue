<script setup lang="ts">
/**
 * 这个组件负责提供播放器目录内部统一徽标样式，
 * 以纯 Tailwind 方式替代外部 UI 徽标组件。
 */
const props = withDefaults(defineProps<{
  tone?: 'primary' | 'neutral' | 'warning'
}>(), {
  tone: 'primary'
})

/**
 * 计算徽标配色，保持不同语义下的背景与边框表现统一。
 */
const toneClass = computed(() => {
  if (props.tone === 'warning') {
    return 'border-amber-300/24 bg-amber-400/12 text-amber-50'
  }

  if (props.tone === 'neutral') {
    return 'border-white/12 bg-white/8 text-[var(--text-1)]'
  }

  return 'border-primary-400/24 bg-primary-500/12 text-primary-100'
})
</script>

<template>
  <span
    class="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium leading-none"
    :class="toneClass"
  >
    <slot />
  </span>
</template>
