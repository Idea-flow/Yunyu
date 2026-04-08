<script setup lang="ts">
import { computed } from 'vue'

/**
 * 后台开关字段组件。
 * 作用：为后台表单场景提供统一的布尔开关样式、开启态色彩和说明文案布局，
 * 让文章、分类或系统设置等页面都能复用同一套轻量交互表现。
 */
const props = withDefaults(defineProps<{
  modelValue?: boolean
  label: string
  description?: string
  disabled?: boolean
  color?: 'primary' | 'secondary' | 'success' | 'info' | 'warning' | 'error' | 'neutral'
  activeText?: string
  inactiveText?: string
}>(), {
  modelValue: false,
  description: '',
  disabled: false,
  color: 'primary',
  activeText: '已开启',
  inactiveText: '已关闭'
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

/**
 * 判断当前开关是否处于开启状态。
 * 作用：统一驱动容器高亮、状态文案和开关本体的视觉反馈。
 */
const isChecked = computed(() => props.modelValue === true)

/**
 * 解析当前色彩方案。
 * 作用：为开启态提供更清晰的轨道色、弱化背景和暗黑模式适配，而不是依赖默认样式。
 */
const toneStyle = computed(() => {
  const toneMap = {
    primary: {
      panel: 'border-[var(--admin-primary-border)] bg-[var(--admin-primary-soft-surface)] shadow-[0_12px_28px_-26px_var(--admin-primary-shadow)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,transparent)] dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_10%,transparent)]',
      track: 'data-[state=checked]:border-[var(--site-primary-color)] data-[state=checked]:bg-[var(--site-primary-color)] dark:data-[state=checked]:border-[var(--site-primary-color)] dark:data-[state=checked]:bg-[var(--site-primary-color)]',
      badge: 'border-[var(--admin-primary-border)] bg-[var(--admin-primary-soft-strong)] text-[var(--admin-primary-text)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,transparent)] dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_14%,transparent)] dark:text-[var(--site-primary-color)]'
    },
    secondary: {
      panel: 'border-indigo-200/90 bg-indigo-50/80 shadow-[0_12px_28px_-26px_rgba(99,102,241,0.42)] dark:border-indigo-400/30 dark:bg-indigo-400/[0.08]',
      track: 'data-[state=checked]:border-indigo-500 data-[state=checked]:bg-indigo-500 dark:data-[state=checked]:border-indigo-400 dark:data-[state=checked]:bg-indigo-400',
      badge: 'border-indigo-200/90 bg-indigo-100/88 text-indigo-700 dark:border-indigo-400/25 dark:bg-indigo-400/12 dark:text-indigo-200'
    },
    success: {
      panel: 'border-emerald-200/90 bg-emerald-50/80 shadow-[0_12px_28px_-26px_rgba(16,185,129,0.42)] dark:border-emerald-400/30 dark:bg-emerald-400/[0.08]',
      track: 'data-[state=checked]:border-emerald-500 data-[state=checked]:bg-emerald-500 dark:data-[state=checked]:border-emerald-400 dark:data-[state=checked]:bg-emerald-400',
      badge: 'border-emerald-200/90 bg-emerald-100/88 text-emerald-700 dark:border-emerald-400/25 dark:bg-emerald-400/12 dark:text-emerald-200'
    },
    info: {
      panel: 'border-cyan-200/90 bg-cyan-50/80 shadow-[0_12px_28px_-26px_rgba(6,182,212,0.42)] dark:border-cyan-400/30 dark:bg-cyan-400/[0.08]',
      track: 'data-[state=checked]:border-cyan-500 data-[state=checked]:bg-cyan-500 dark:data-[state=checked]:border-cyan-400 dark:data-[state=checked]:bg-cyan-400',
      badge: 'border-cyan-200/90 bg-cyan-100/88 text-cyan-700 dark:border-cyan-400/25 dark:bg-cyan-400/12 dark:text-cyan-200'
    },
    warning: {
      panel: 'border-amber-200/90 bg-amber-50/80 shadow-[0_12px_28px_-26px_rgba(245,158,11,0.42)] dark:border-amber-400/30 dark:bg-amber-400/[0.08]',
      track: 'data-[state=checked]:border-amber-500 data-[state=checked]:bg-amber-500 dark:data-[state=checked]:border-amber-400 dark:data-[state=checked]:bg-amber-400',
      badge: 'border-amber-200/90 bg-amber-100/88 text-amber-700 dark:border-amber-400/25 dark:bg-amber-400/12 dark:text-amber-200'
    },
    error: {
      panel: 'border-rose-200/90 bg-rose-50/80 shadow-[0_12px_28px_-26px_rgba(244,63,94,0.42)] dark:border-rose-400/30 dark:bg-rose-400/[0.08]',
      track: 'data-[state=checked]:border-rose-500 data-[state=checked]:bg-rose-500 dark:data-[state=checked]:border-rose-400 dark:data-[state=checked]:bg-rose-400',
      badge: 'border-rose-200/90 bg-rose-100/88 text-rose-700 dark:border-rose-400/25 dark:bg-rose-400/12 dark:text-rose-200'
    },
    neutral: {
      panel: 'border-slate-300/90 bg-slate-100/75 shadow-[0_12px_28px_-26px_rgba(51,65,85,0.28)] dark:border-slate-500/30 dark:bg-slate-700/20',
      track: 'data-[state=checked]:border-slate-700 data-[state=checked]:bg-slate-700 dark:data-[state=checked]:border-slate-200 dark:data-[state=checked]:bg-slate-200',
      badge: 'border-slate-300/90 bg-slate-200/88 text-slate-700 dark:border-slate-500/30 dark:bg-slate-700/35 dark:text-slate-200'
    }
  } as const

  return toneMap[props.color]
})

/**
 * 计算字段容器样式。
 * 作用：让开启态除了开关本体之外，还有一层很轻的面板反馈，提升可识别性。
 */
const fieldClass = computed(() => [
    'flex items-center justify-between gap-4 rounded-[12px] border px-4 py-3 transition-[border-color,background-color,box-shadow,transform] duration-200',
    'dark:shadow-none',
    props.disabled
      ? 'cursor-not-allowed opacity-60'
      : 'hover:border-slate-200 hover:bg-white/82 dark:hover:border-white/12 dark:hover:bg-white/[0.05]',
    isChecked.value
    ? toneStyle.value.panel
    : 'border-white/60 bg-white/72 dark:border-white/10 dark:bg-white/[0.04]'
].join(' '))

/**
 * 计算开关内部样式。
 * 作用：增强轨道和焦点反馈，让开启态在明亮与暗黑模式下都保持清晰。
 */
const switchUi = computed(() => ({
  root: 'shrink-0',
  base: [
    'h-6 w-11 cursor-pointer rounded-full border shadow-[0_10px_24px_-18px_rgba(15,23,42,0.32)]',
    'transition-[background-color,border-color,box-shadow,transform] duration-200',
    'data-[state=unchecked]:border-slate-300 data-[state=unchecked]:bg-slate-200/95',
    'hover:scale-[1.02] focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-[var(--admin-primary-ring)]',
    'dark:data-[state=unchecked]:border-slate-600 dark:data-[state=unchecked]:bg-slate-700/90',
    'dark:focus-visible:ring-[var(--admin-primary-ring)]',
    toneStyle.value.track
  ].join(' '),
  thumb: [
    'size-5 rounded-full bg-white shadow-[0_8px_18px_-14px_rgba(15,23,42,0.38)]',
    'transition-transform duration-200 data-[state=checked]:translate-x-5 data-[state=unchecked]:translate-x-0',
    'dark:bg-slate-950 dark:shadow-[0_10px_24px_-18px_rgba(0,0,0,0.55)]'
  ].join(' ')
}))

/**
 * 同步开关值。
 *
 * @param value 当前开关值
 */
function handleUpdate(value: boolean) {
  emit('update:modelValue', value)
}
</script>

<template>
  <div :class="fieldClass">
    <div class="min-w-0">
      <div class="flex items-center gap-2">
        <p class="text-sm font-medium text-slate-800 dark:text-slate-100">
          {{ props.label }}
        </p>
        <span
          class="inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-semibold tracking-[0.02em] transition-[border-color,background-color,color] duration-200"
          :class="isChecked
            ? toneStyle.badge
            : 'border-slate-200/90 bg-slate-100/88 text-slate-500 dark:border-slate-700/80 dark:bg-slate-800/88 dark:text-slate-400'"
        >
          {{ isChecked ? props.activeText : props.inactiveText }}
        </span>
      </div>

      <p
        v-if="props.description"
        class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400"
      >
        {{ props.description }}
      </p>
    </div>

    <USwitch
      :model-value="props.modelValue"
      :disabled="props.disabled"
      color="primary"
      size="xl"
      :ui="switchUi"
      @update:model-value="handleUpdate"
    />
  </div>
</template>
