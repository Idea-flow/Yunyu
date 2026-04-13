<script setup lang="ts">
import VideoIcon from '../icons/VideoIcon.vue'

/**
 * 这个组件负责提供播放器目录内部统一按钮壳层，
 * 用纯 Tailwind 方式替代外部 UI 组件库按钮，保持目录自包含。
 */
const props = withDefaults(defineProps<{
  label?: string
  icon?:
    | 'copy'
    | 'expand'
    | 'minimize'
    | 'picture-in-picture'
    | 'play'
    | 'rotate-ccw'
    | 'rotate-cw'
    | 'skip-forward'
    | 'volume'
    | 'volume-off'
    | 'close'
    | 'check'
    | 'chevron-right'
    | 'list-video'
    | 'loader'
  variant?: 'ghost' | 'soft'
  tone?: 'neutral' | 'warning' | 'success'
  size?: 'xs' | 'sm' | 'md'
  ariaLabel?: string
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
}>(), {
  label: '',
  icon: undefined,
  variant: 'ghost',
  tone: 'neutral',
  size: 'md',
  ariaLabel: '',
  disabled: false,
  type: 'button'
})

/**
 * 计算按钮尺寸样式，兼容仅图标与图标加文案两种模式。
 */
const sizeClass = computed(() => {
  const iconOnly = !props.label

  if (props.size === 'xs') {
    return iconOnly ? 'size-8 text-xs sm:size-[2.1rem]' : 'min-h-8 px-3 text-xs sm:min-h-[2.1rem]'
  }

  if (props.size === 'sm') {
    return iconOnly ? 'size-[2.125rem] text-[13px] sm:size-9 sm:text-sm' : 'min-h-[2.125rem] px-3 text-[13px] sm:min-h-9 sm:px-3.5 sm:text-sm'
  }

  return iconOnly ? 'size-9 text-sm sm:size-10' : 'min-h-9 px-3.5 text-sm sm:min-h-10 sm:px-4'
})

/**
 * 计算按钮主题样式，统一收口不同语义下的边框、背景与文字颜色。
 */
const toneClass = computed(() => {
  if (props.tone === 'warning') {
    return props.variant === 'soft'
      ? 'border-amber-300/24 bg-amber-400/14 text-amber-50 hover:bg-amber-400/18'
      : 'border-amber-300/20 bg-amber-400/8 text-amber-50 hover:bg-amber-400/14'
  }

  if (props.tone === 'success') {
    return props.variant === 'soft'
      ? 'border-emerald-300/24 bg-emerald-400/14 text-emerald-50 hover:bg-emerald-400/18'
      : 'border-emerald-300/20 bg-emerald-400/8 text-emerald-50 hover:bg-emerald-400/14'
  }

  return props.variant === 'soft'
    ? 'border-white/12 bg-white/10 text-[var(--text-0)] hover:bg-white/14'
    : 'border-white/10 bg-white/5 text-[var(--text-1)] hover:bg-white/9'
})

/**
 * 根据按钮尺寸选择更合适的图标尺寸，
 * 避免按钮本体与内部图标比例失衡。
 */
const iconSize = computed<'compact' | 'default' | 'action'>(() => {
  if (props.size === 'xs') {
    return 'compact'
  }

  if (props.size === 'sm') {
    return 'default'
  }

  return 'action'
})
</script>

<template>
  <button
    :type="props.type"
    :aria-label="props.ariaLabel || props.label || undefined"
    :disabled="props.disabled"
    class="inline-flex shrink-0 items-center justify-center gap-2 rounded-full border backdrop-blur-[18px] transition duration-200 disabled:cursor-not-allowed disabled:opacity-50"
    :class="[sizeClass, toneClass]"
  >
    <VideoIcon v-if="props.icon" :name="props.icon" :size="iconSize" class="shrink-0" />
    <span v-if="props.label" class="truncate leading-none">{{ props.label }}</span>
    <slot />
  </button>
</template>
