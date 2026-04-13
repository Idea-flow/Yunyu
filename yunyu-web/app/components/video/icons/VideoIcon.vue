<script setup lang="ts">
import { videoIconMap, type VideoIconName } from './icon-map'

/**
 * 这个组件负责统一包装播放器目录内的本地 SVG 图标文件，
 * 让业务层继续按名称调用图标，同时底层资源改为独立 svg 文件便于维护与替换。
 */
type VideoIconSize = 'compact' | 'default' | 'action' | 'hero'

const props = withDefaults(defineProps<{
  name: VideoIconName
  title?: string
  size?: VideoIconSize
}>(), {
  title: '',
  size: 'default'
})

/**
 * 对原始 SVG 字符串进行最小增强，
 * 补齐尺寸、无障碍属性与可选 title，保证图标在 Tailwind 控制下稳定渲染。
 */
const renderedSvg = computed(() => {
  const rawSvg = videoIconMap[props.name]
  const normalizedSvg = rawSvg.replace(
    '<svg ',
    `<svg class="block h-full w-full" width="100%" height="100%" focusable="false" aria-hidden="${props.title ? 'false' : 'true'}" `
  )

  if (!props.title) {
    return normalizedSvg
  }

  const escapedTitle = props.title
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')

  return normalizedSvg.replace('>', `><title>${escapedTitle}</title>`)
})

/**
 * 统一收口播放器内图标的响应式尺寸，
 * 让手机端更克制、平板与桌面端逐步放大，避免每个视图散落写死 size 类。
 */
const sizeClass = computed(() => {
  if (props.size === 'compact') {
    return 'size-[0.8rem] sm:size-[0.85rem] md:size-[0.9rem]'
  }

  if (props.size === 'action') {
    return 'size-[0.92rem] sm:size-[0.98rem] md:size-[1.05rem]'
  }

  if (props.size === 'hero') {
    return 'size-[1rem] sm:size-[1.12rem] md:size-[1.22rem]'
  }

  return 'size-[0.88rem] sm:size-[0.94rem] md:size-[1rem]'
})
</script>

<template>
  <span
    class="inline-flex items-center justify-center"
    :class="sizeClass"
    v-html="renderedSvg"
  />
</template>
