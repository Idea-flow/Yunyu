<script setup lang="ts">
/**
 * 云屿通用分区标题组件。
 * 作用：统一前台分区的眉标、标题、说明文案与辅助跳转入口，
 * 作为首页和后续内容入口页的基础标题组件。
 */
interface YunyuSectionTitleProps {
  eyebrow?: string
  title: string
  description?: string
  align?: 'start' | 'center'
  tone?: 'sky' | 'orange' | 'slate'
  size?: 'default' | 'compact'
  linkLabel?: string
  linkTo?: string
}

const props = withDefaults(defineProps<YunyuSectionTitleProps>(), {
  eyebrow: '',
  description: '',
  align: 'start',
  tone: 'sky',
  size: 'default',
  linkLabel: '',
  linkTo: ''
})

/**
 * 计算分区标题容器样式。
 * 作用：统一左对齐与居中两种分区标题排版模式。
 */
const rootClassName = computed(() => {
  return props.align === 'center'
    ? 'mx-auto max-w-3xl text-center'
    : 'max-w-2xl'
})

/**
 * 计算眉标颜色样式。
 * 作用：根据分区气质统一切换蓝系、橙系与中性色表现。
 */
const eyebrowClassName = computed(() => {
  const toneClassMap = {
    sky: 'text-sky-600 dark:text-sky-300',
    orange: 'text-orange-500 dark:text-orange-300',
    slate: 'text-slate-500 dark:text-slate-400'
  }

  return toneClassMap[props.tone]
})

/**
 * 计算标题文字样式。
 * 作用：支持默认版和紧凑版标题尺寸，便于首页等需要更克制版式的场景复用。
 */
const titleClassName = computed(() => {
  if (props.size === 'compact') {
    return 'mt-3 text-[clamp(1.7rem,1.4rem+0.9vw,2.3rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50'
  }

  return 'mt-3 text-[clamp(1.95rem,1.5rem+1.15vw,2.7rem)] font-semibold leading-[1.04] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50'
})

/**
 * 计算描述文字样式。
 * 作用：支持默认版和紧凑版说明文案密度，避免首页说明文字过大影响阅读节奏。
 */
const descriptionClassName = computed(() => {
  if (props.size === 'compact') {
    return 'mt-3 max-w-[40rem] text-[0.96rem] leading-[1.85] tracking-[-0.01em] text-slate-600 dark:text-slate-300'
  }

  return 'mt-3 max-w-[44rem] text-[clamp(1rem,0.95rem+0.16vw,1.08rem)] leading-[1.95] tracking-[-0.01em] text-slate-600 dark:text-slate-300'
})
</script>

<template>
  <div class="flex flex-wrap items-end justify-between gap-5">
    <div :class="rootClassName">
      <p
        v-if="eyebrow"
        class="text-[0.72rem] font-semibold uppercase tracking-[0.34em]"
        :class="eyebrowClassName"
      >
        {{ eyebrow }}
      </p>
      <h2 :class="titleClassName">
        {{ title }}
      </h2>
      <p
        v-if="description"
        :class="descriptionClassName"
      >
        {{ description }}
      </p>
    </div>

    <NuxtLink
      v-if="linkLabel && linkTo"
      :to="linkTo"
      class="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-sky-700 dark:text-slate-400 dark:hover:text-sky-200"
    >
      <span>{{ linkLabel }}</span>
      <UIcon name="i-lucide-arrow-up-right" class="size-4" />
    </NuxtLink>
  </div>
</template>
