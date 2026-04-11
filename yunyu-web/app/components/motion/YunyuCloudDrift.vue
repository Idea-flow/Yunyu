<script setup lang="ts">
import type { CSSProperties } from 'vue'

/**
 * 云屿云层流动组件。
 * 作用：为文章封面提供浅色、克制的横向云层流动氛围，
 * 用更轻的动画替代星空效果，营造类似手绘动画天空的柔和背景感。
 */
interface YunyuCloudDriftProps {
  variant?: 'sky' | 'warm' | 'neutral'
  density?: 'soft' | 'medium'
  animated?: boolean
}

const props = withDefaults(defineProps<YunyuCloudDriftProps>(), {
  variant: 'sky',
  density: 'soft',
  animated: true
})

/**
 * 计算云层组件样式变量。
 * 作用：统一不同主题下的云层亮度、透明度和移动速度，
 * 让页面可以在不增加复杂度的前提下切换不同天空氛围。
 *
 * @returns 云层组件 CSS 变量
 */
const cloudStyle = computed<CSSProperties>(() => {
  const isMediumDensity = props.density === 'medium'

  const baseStyle: CSSProperties = {
    '--yy-cloud-color': '255, 255, 255',
    '--yy-cloud-soft': '248, 252, 255',
    '--yy-cloud-haze': '219, 234, 254',
    '--yy-cloud-layer-opacity': isMediumDensity ? '0.18' : '0.12',
    '--yy-cloud-front-opacity': isMediumDensity ? '0.28' : '0.2',
    '--yy-cloud-haze-opacity': isMediumDensity ? '0.12' : '0.08',
    '--yy-cloud-duration': isMediumDensity ? '42s' : '50s',
    '--yy-cloud-front-duration': isMediumDensity ? '34s' : '40s'
  }

  if (props.variant === 'warm') {
    return {
      ...baseStyle,
      '--yy-cloud-soft': '255, 247, 237',
      '--yy-cloud-haze': '254, 215, 170'
    }
  }

  if (props.variant === 'neutral') {
    return {
      ...baseStyle,
      '--yy-cloud-soft': '241, 245, 249',
      '--yy-cloud-haze': '203, 213, 225'
    }
  }

  return baseStyle
})
</script>

<template>
  <div
    class="yunyu-cloud-drift"
    :class="animated ? '' : 'is-static'"
    :style="cloudStyle"
    aria-hidden="true"
  >
    <div class="yunyu-cloud-drift__haze" />
    <div class="yunyu-cloud-drift__layer yunyu-cloud-drift__layer--back" />
    <div class="yunyu-cloud-drift__layer yunyu-cloud-drift__layer--front" />
  </div>
</template>

<style scoped>
/**
 * 云层根容器。
 * 作用：限制云层只在背景区域中缓慢流动，不影响内容层点击与排版。
 */
.yunyu-cloud-drift {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

/**
 * 云层装饰层。
 * 作用：统一装饰层的定位与渲染性能提示，保证动画稳定平滑。
 */
.yunyu-cloud-drift__haze,
.yunyu-cloud-drift__layer {
  position: absolute;
  inset: 0;
  will-change: transform, opacity;
}

/**
 * 云雾底层。
 * 作用：给天空加一点极浅的空气感，避免云朵单独出现时过于突兀。
 */
.yunyu-cloud-drift__haze {
  opacity: var(--yy-cloud-haze-opacity);
  background:
    radial-gradient(circle at 22% 18%, rgba(var(--yy-cloud-haze), 0.22) 0%, rgba(var(--yy-cloud-haze), 0) 24%),
    radial-gradient(circle at 74% 24%, rgba(var(--yy-cloud-soft), 0.18) 0%, rgba(var(--yy-cloud-soft), 0) 20%),
    linear-gradient(180deg, rgba(var(--yy-cloud-color), 0.06) 0%, rgba(var(--yy-cloud-color), 0.015) 48%, rgba(var(--yy-cloud-color), 0) 100%);
  filter: blur(42px);
}

/**
 * 云层主体。
 * 作用：承载横向移动的手绘感云团，让画面呈现轻柔的左进右出效果。
 */
.yunyu-cloud-drift__layer {
  inset: 0;
  opacity: 0;
  background-repeat: no-repeat;
}

/**
 * 远景云层。
 * 作用：提供更淡、更宽的云团，让整体天空看起来更安静。
 */
.yunyu-cloud-drift__layer--back {
  opacity: var(--yy-cloud-layer-opacity);
  background-image:
    radial-gradient(ellipse 22% 7% at 10% 22%, rgba(var(--yy-cloud-soft), 0.66) 0%, rgba(var(--yy-cloud-soft), 0.34) 54%, rgba(var(--yy-cloud-soft), 0) 100%),
    radial-gradient(ellipse 18% 6% at 28% 26%, rgba(var(--yy-cloud-color), 0.54) 0%, rgba(var(--yy-cloud-color), 0.26) 56%, rgba(var(--yy-cloud-color), 0) 100%),
    radial-gradient(ellipse 24% 7.5% at 48% 18%, rgba(var(--yy-cloud-soft), 0.62) 0%, rgba(var(--yy-cloud-soft), 0.3) 54%, rgba(var(--yy-cloud-soft), 0) 100%),
    radial-gradient(ellipse 20% 6.5% at 68% 22%, rgba(var(--yy-cloud-color), 0.5) 0%, rgba(var(--yy-cloud-color), 0.22) 56%, rgba(var(--yy-cloud-color), 0) 100%),
    radial-gradient(ellipse 23% 7% at 88% 28%, rgba(var(--yy-cloud-soft), 0.64) 0%, rgba(var(--yy-cloud-soft), 0.28) 54%, rgba(var(--yy-cloud-soft), 0) 100%);
  filter: blur(18px);
  animation: yunyu-cloud-drift-back var(--yy-cloud-duration) linear infinite;
}

/**
 * 前景云层。
 * 作用：用更小一点的亮色云团补足层次，让画面更像轻轻飘过的动画云。
 */
.yunyu-cloud-drift__layer--front {
  opacity: var(--yy-cloud-front-opacity);
  background-image:
    radial-gradient(ellipse 16% 5.5% at 6% 42%, rgba(var(--yy-cloud-color), 0.74) 0%, rgba(var(--yy-cloud-color), 0.3) 50%, rgba(var(--yy-cloud-color), 0) 100%),
    radial-gradient(ellipse 14% 5% at 24% 36%, rgba(var(--yy-cloud-soft), 0.68) 0%, rgba(var(--yy-cloud-soft), 0.24) 50%, rgba(var(--yy-cloud-soft), 0) 100%),
    radial-gradient(ellipse 18% 6% at 46% 30%, rgba(var(--yy-cloud-color), 0.72) 0%, rgba(var(--yy-cloud-color), 0.28) 50%, rgba(var(--yy-cloud-color), 0) 100%),
    radial-gradient(ellipse 15% 5% at 68% 38%, rgba(var(--yy-cloud-soft), 0.62) 0%, rgba(var(--yy-cloud-soft), 0.2) 50%, rgba(var(--yy-cloud-soft), 0) 100%),
    radial-gradient(ellipse 17% 5.5% at 88% 32%, rgba(var(--yy-cloud-color), 0.7) 0%, rgba(var(--yy-cloud-color), 0.24) 50%, rgba(var(--yy-cloud-color), 0) 100%);
  filter: blur(12px);
  animation: yunyu-cloud-drift-front var(--yy-cloud-front-duration) linear infinite;
}

/**
 * 静态模式。
 * 作用：在关闭动画时保留浅云层氛围，但停止所有位移动效。
 */
.is-static .yunyu-cloud-drift__layer {
  animation: none;
  transform: none;
  opacity: 0.22;
}

/**
 * 远景云层动画。
 * 作用：让大块浅云从左侧缓慢滑入，经过画面后在右侧淡出。
 */
@keyframes yunyu-cloud-drift-back {
  0% {
    transform: translate3d(-14%, 0, 0);
    opacity: 0;
  }

  15% {
    opacity: var(--yy-cloud-layer-opacity);
  }

  82% {
    opacity: var(--yy-cloud-layer-opacity);
  }

  100% {
    transform: translate3d(12%, 0, 0);
    opacity: 0;
  }
}

/**
 * 前景云层动画。
 * 作用：让较亮的云团稍快一点经过视野，增强轻盈流动感。
 */
@keyframes yunyu-cloud-drift-front {
  0% {
    transform: translate3d(-18%, 0, 0);
    opacity: 0;
  }

  12% {
    opacity: var(--yy-cloud-front-opacity);
  }

  78% {
    opacity: var(--yy-cloud-front-opacity);
  }

  100% {
    transform: translate3d(14%, 0, 0);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .yunyu-cloud-drift__layer {
    animation: none !important;
    transform: none !important;
    opacity: 0.2;
  }
}
</style>
