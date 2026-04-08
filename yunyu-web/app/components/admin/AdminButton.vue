<script setup lang="ts">
import { computed, useSlots } from 'vue'

/**
 * 后台通用按钮组件。
 * 作用：为后台页面提供统一的主按钮、描边按钮和幽灵按钮样式，
 * 并让品牌主色跟随站点主题配置变化，避免后台各处重复维护按钮视觉。
 */
defineOptions({
  inheritAttrs: false
})

const props = withDefaults(defineProps<{
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  loading?: boolean
  label?: string
  loadingLabel?: string
  icon?: string
  tone?: 'primary' | 'neutral' | 'danger'
  variant?: 'solid' | 'outline' | 'ghost'
  size?: 'xs' | 'sm' | 'md'
  square?: boolean
}>(), {
  type: 'button',
  disabled: false,
  loading: false,
  label: '',
  loadingLabel: '处理中...',
  icon: undefined,
  tone: 'primary',
  variant: 'solid',
  size: 'md',
  square: false
})
const slots = useSlots()

/**
 * 判断是否为纯图标按钮。
 * 作用：为删除、左右翻页等小操作按钮自动切换为紧凑方形样式。
 */
const isIconOnly = computed(() => !props.loading && !!props.icon && !props.label && !slots.default)

/**
 * 解析按钮尺寸样式。
 * 作用：统一后台不同操作区的按钮高度、内边距和字号节奏。
 */
const sizeClass = computed(() => {
  if (props.square || isIconOnly.value) {
    if (props.size === 'xs') {
      return 'h-7 w-7 rounded-[10px] p-0 text-xs'
    }

    if (props.size === 'sm') {
      return 'h-8 w-8 rounded-[10px] p-0 text-sm'
    }

    return 'h-9 w-9 rounded-[10px] p-0 text-sm'
  }

  if (props.size === 'xs') {
    return 'min-h-7 px-2.5 py-1 text-xs rounded-[10px]'
  }

  if (props.size === 'sm') {
    return 'min-h-8 px-3 py-1.5 text-sm rounded-[10px]'
  }

  return 'min-h-9 px-4 py-2 text-sm rounded-[10px]'
})

/**
 * 解析按钮视觉样式。
 * 作用：根据语义色和变体统一生成按钮样式，确保后台按钮体系一致。
 */
const buttonClass = computed(() => {
  const toneClassMap = {
    primary: {
      solid: 'border-[var(--site-primary-color)] bg-[var(--site-primary-color)] text-white shadow-[0_12px_24px_-20px_var(--admin-primary-shadow)] hover:brightness-[1.03] hover:shadow-[0_16px_26px_-20px_var(--admin-primary-shadow)] focus:ring-[var(--admin-primary-ring)] dark:text-slate-950',
      outline: 'border-[var(--admin-primary-border)] bg-white/88 text-[var(--admin-primary-text)] shadow-[0_10px_22px_-22px_var(--admin-primary-shadow)] hover:bg-[var(--admin-primary-soft-surface)] hover:text-slate-900 focus:ring-[var(--admin-primary-ring)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_24%,transparent)] dark:bg-white/[0.05] dark:text-[var(--site-primary-color)] dark:hover:bg-[color:color-mix(in_srgb,var(--site-primary-color)_12%,transparent)] dark:hover:text-slate-50',
      ghost: 'border-transparent bg-transparent text-[var(--admin-primary-text)] hover:bg-[var(--admin-primary-soft-surface)] hover:text-slate-900 focus:ring-[var(--admin-primary-ring)] dark:text-[var(--site-primary-color)] dark:hover:bg-[color:color-mix(in_srgb,var(--site-primary-color)_12%,transparent)] dark:hover:text-slate-50'
    },
    neutral: {
      solid: 'border-slate-900 bg-slate-900 text-white shadow-[0_12px_24px_-20px_rgba(15,23,42,0.34)] hover:bg-slate-800 hover:border-slate-800 focus:ring-slate-200/70 dark:border-slate-100 dark:bg-slate-100 dark:text-slate-950 dark:hover:bg-white dark:hover:border-white dark:focus:ring-white/20',
      outline: 'border-slate-200/85 bg-white/92 text-slate-700 shadow-[0_10px_22px_-22px_rgba(15,23,42,0.18)] hover:border-slate-300 hover:bg-white hover:text-slate-900 focus:ring-slate-200/70 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-200 dark:hover:border-white/16 dark:hover:bg-white/[0.08] dark:hover:text-slate-50 dark:focus:ring-white/12',
      ghost: 'border-transparent bg-transparent text-slate-500 hover:bg-slate-100/90 hover:text-slate-900 focus:ring-slate-200/70 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-slate-50 dark:focus:ring-white/12'
    },
    danger: {
      solid: 'border-rose-500 bg-rose-500 text-white shadow-[0_12px_24px_-20px_rgba(244,63,94,0.38)] hover:border-rose-600 hover:bg-rose-600 focus:ring-rose-200/40 dark:border-rose-400 dark:bg-rose-400 dark:text-slate-950 dark:hover:border-rose-300 dark:hover:bg-rose-300 dark:focus:ring-rose-400/20',
      outline: 'border-rose-200/90 bg-white/92 text-rose-600 shadow-[0_10px_22px_-22px_rgba(244,63,94,0.24)] hover:border-rose-300 hover:bg-rose-50/92 hover:text-rose-700 focus:ring-rose-200/40 dark:border-rose-400/20 dark:bg-white/[0.05] dark:text-rose-300 dark:hover:bg-rose-400/10 dark:hover:text-rose-200 dark:focus:ring-rose-400/20',
      ghost: 'border-transparent bg-transparent text-rose-500 hover:bg-rose-50/92 hover:text-rose-700 focus:ring-rose-200/40 dark:text-rose-300 dark:hover:bg-rose-400/10 dark:hover:text-rose-200 dark:focus:ring-rose-400/20'
    }
  } as const

  return [
    'group inline-flex cursor-pointer items-center justify-center gap-2 border font-semibold whitespace-nowrap transition duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-70 dark:focus:ring-offset-slate-950',
    props.square || isIconOnly.value ? '' : 'min-w-24',
    sizeClass.value,
    toneClassMap[props.tone][props.variant]
  ].join(' ')
})
</script>

<template>
  <button
    v-bind="$attrs"
    :type="props.type"
    :disabled="props.disabled || props.loading"
    :class="buttonClass"
  >
    <UIcon
      v-if="props.icon && !props.loading"
      :name="props.icon"
      class="size-4 transition duration-200 group-hover:translate-x-0.5"
    />
    <slot>{{ props.loading ? props.loadingLabel : props.label }}</slot>
  </button>
</template>
