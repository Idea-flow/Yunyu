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
  linkLabel?: string
  linkTo?: string
}

const props = withDefaults(defineProps<YunyuSectionTitleProps>(), {
  eyebrow: '',
  description: '',
  align: 'start',
  tone: 'sky',
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
</script>

<template>
  <div class="flex flex-wrap items-end justify-between gap-5">
    <div :class="rootClassName">
      <p
        v-if="eyebrow"
        class="text-xs font-semibold uppercase tracking-[0.34em]"
        :class="eyebrowClassName"
      >
        {{ eyebrow }}
      </p>
      <h2 class="mt-3 text-[1.85rem] font-semibold tracking-tight text-slate-950 sm:text-[2.1rem] dark:text-slate-50">
        {{ title }}
      </h2>
      <p
        v-if="description"
        class="mt-3 max-w-[44rem] text-sm leading-7 text-slate-600 dark:text-slate-300"
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
