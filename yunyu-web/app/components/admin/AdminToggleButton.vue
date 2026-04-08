<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台开关按钮组件。
 * 作用：为后台中“开启/关闭”“显示/隐藏”“可点击/不可点击”这类轻量状态切换，
 * 提供统一的胶囊按钮样式，避免页面内重复手写一套套切换按钮样式。
 */
const props = withDefaults(defineProps<{
  modelValue?: boolean
  activeLabel: string
  inactiveLabel: string
  tone?: 'primary' | 'success' | 'info' | 'warning' | 'neutral'
  size?: 'sm' | 'md'
  showDot?: boolean
  disabled?: boolean
}>(), {
  modelValue: false,
  tone: 'primary',
  size: 'md',
  showDot: false,
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  click: [event: MouseEvent]
}>()

/**
 * 判断当前按钮是否处于开启态。
 * 作用：统一驱动文案、圆点和按钮高亮状态。
 */
const isActive = computed(() => props.modelValue === true)

/**
 * 解析按钮尺寸样式。
 * 作用：兼容后台头部小胶囊和模块卡片内更紧凑的切换按钮。
 */
const sizeClass = computed(() => {
  if (props.size === 'sm') {
    return 'px-2.5 py-1 text-[11px]'
  }

  return 'px-3 py-1.5 text-xs'
})

/**
 * 解析按钮色调样式。
 * 作用：让开启态可以按语义色区分，同时在关闭态保持统一中性观感。
 */
const toneClass = computed(() => {
  const toneMap = {
    primary: 'border-[var(--admin-primary-border)] bg-[var(--admin-primary-soft-surface)] text-[var(--admin-primary-text)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,transparent)] dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_10%,transparent)] dark:text-[var(--site-primary-color)]',
    success: 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200',
    info: 'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-400/20 dark:bg-sky-400/10 dark:text-sky-200',
    warning: 'border-orange-200 bg-orange-50 text-orange-700 dark:border-orange-400/20 dark:bg-orange-400/10 dark:text-orange-200',
    neutral: 'border-slate-300 bg-slate-100 text-slate-700 dark:border-slate-500/30 dark:bg-slate-700/20 dark:text-slate-200'
  } as const

  return isActive.value
    ? toneMap[props.tone]
    : 'border-slate-200 bg-white text-slate-500 dark:border-white/10 dark:bg-white/5 dark:text-slate-400'
})

/**
 * 解析按钮整体样式。
 * 作用：统一背景、边框、禁用态和过渡效果。
 */
const buttonClass = computed(() => [
  'inline-flex items-center gap-2 rounded-full border font-medium transition duration-200',
  'focus:outline-none focus:ring-2 focus:ring-[var(--admin-primary-ring)] focus:ring-offset-2 focus:ring-offset-white dark:focus:ring-offset-slate-950',
  props.disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer hover:brightness-[1.02]',
  sizeClass.value,
  toneClass.value
].join(' '))

/**
 * 切换当前状态。
 * 作用：点击后向父级回传相反布尔值，并保留原始点击事件。
 *
 * @param event 当前点击事件
 */
function handleClick(event: MouseEvent) {
  if (props.disabled) {
    return
  }

  emit('update:modelValue', !isActive.value)
  emit('click', event)
}
</script>

<template>
  <button
    type="button"
    :disabled="props.disabled"
    :class="buttonClass"
    @click="handleClick"
  >
    <span
      v-if="props.showDot"
      :class="[
        'h-2.5 w-2.5 rounded-full transition duration-200',
        isActive
          ? props.tone === 'warning'
            ? 'bg-orange-500'
            : props.tone === 'success'
              ? 'bg-emerald-500'
              : props.tone === 'info'
                ? 'bg-sky-500'
                : props.tone === 'neutral'
                  ? 'bg-slate-600 dark:bg-slate-200'
                  : 'bg-[var(--site-primary-color)]'
          : 'bg-slate-300 dark:bg-slate-600'
      ]"
    />
    <span>{{ isActive ? props.activeLabel : props.inactiveLabel }}</span>
  </button>
</template>
