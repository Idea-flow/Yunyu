<script setup lang="ts">
import type { CSSProperties } from 'vue'

/**
 * 云屿星幕流动组件。
 * 作用：为前台大图封面补充缓慢流动的星点、雾光和空间层次，
 * 让封面在保持干净阅读氛围的同时具有轻微动态感。
 */
interface YunyuStarfieldFlowProps {
  variant?: 'sky' | 'warm' | 'neutral'
  density?: 'soft' | 'medium'
  animated?: boolean
}

const props = withDefaults(defineProps<YunyuStarfieldFlowProps>(), {
  variant: 'sky',
  density: 'soft',
  animated: true
})

/**
 * 计算星幕层的样式变量。
 * 作用：统一不同主题下的星点色温、雾光颜色和漂移幅度，
 * 避免页面层直接维护零散的封面动效样式。
 *
 * @returns 星幕层 CSS 变量
 */
const starfieldStyle = computed<CSSProperties>(() => {
  const baseStyle: CSSProperties = {
    '--yy-star-color': '255, 255, 255',
    '--yy-star-color-soft': '255, 255, 255',
    '--yy-star-glow': '148, 197, 255',
    '--yy-star-glow-soft': '186, 230, 253',
    '--yy-star-drift-x': '28px',
    '--yy-star-drift-y': '-18px'
  }

  if (props.variant === 'warm') {
    return {
      ...baseStyle,
      '--yy-star-glow': '251, 191, 36',
      '--yy-star-glow-soft': '249, 115, 22',
      '--yy-star-drift-x': '24px',
      '--yy-star-drift-y': '-14px'
    }
  }

  if (props.variant === 'neutral') {
    return {
      ...baseStyle,
      '--yy-star-glow': '148, 163, 184',
      '--yy-star-glow-soft': '226, 232, 240',
      '--yy-star-drift-x': '20px',
      '--yy-star-drift-y': '-12px'
    }
  }

  return baseStyle
})
</script>

<template>
  <div
    class="yunyu-starfield-flow"
    :class="[
      density === 'medium' ? 'is-medium-density' : 'is-soft-density',
      animated ? '' : 'is-static'
    ]"
    :style="starfieldStyle"
    aria-hidden="true"
  >
    <div class="yunyu-starfield-flow__layer yunyu-starfield-flow__layer--back" />
    <div class="yunyu-starfield-flow__layer yunyu-starfield-flow__layer--front" />
    <div class="yunyu-starfield-flow__glow yunyu-starfield-flow__glow--primary" />
    <div class="yunyu-starfield-flow__glow yunyu-starfield-flow__glow--secondary" />
    <div class="yunyu-starfield-flow__mist" />
  </div>
</template>

<style scoped>
/**
 * 云屿星幕流动组件样式。
 * 作用：通过双层星点、柔光和雾层建立慢速流动的夜空氛围，
 * 保持背景层有生命感但不干扰正文信息。
 */
.yunyu-starfield-flow {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  opacity: 0.92;
}

.yunyu-starfield-flow__layer,
.yunyu-starfield-flow__glow,
.yunyu-starfield-flow__mist {
  position: absolute;
  inset: -8%;
  transform: translate3d(0, 0, 0);
  will-change: transform, opacity;
}

.yunyu-starfield-flow__layer {
  background-repeat: repeat;
  filter: drop-shadow(0 0 8px rgba(var(--yy-star-color-soft), 0.08));
}

.yunyu-starfield-flow__layer--back {
  opacity: 0.34;
  background-image:
    radial-gradient(circle at 12% 22%, rgba(var(--yy-star-color), 0.68) 0 1px, transparent 1.9px),
    radial-gradient(circle at 78% 16%, rgba(var(--yy-star-color-soft), 0.44) 0 1px, transparent 2px),
    radial-gradient(circle at 26% 72%, rgba(var(--yy-star-color), 0.4) 0 1px, transparent 1.8px),
    radial-gradient(circle at 88% 68%, rgba(var(--yy-star-color-soft), 0.38) 0 1px, transparent 1.8px),
    radial-gradient(circle at 52% 44%, rgba(var(--yy-star-color), 0.28) 0 1px, transparent 1.7px);
  background-size: 240px 240px, 280px 280px, 260px 260px, 300px 300px, 220px 220px;
  animation: yunyu-starfield-drift-back 30s linear infinite alternate;
}

.yunyu-starfield-flow__layer--front {
  opacity: 0.5;
  background-image:
    radial-gradient(circle at 18% 18%, rgba(var(--yy-star-color), 0.96) 0 1.1px, transparent 2px),
    radial-gradient(circle at 64% 28%, rgba(var(--yy-star-color), 0.72) 0 1px, transparent 1.9px),
    radial-gradient(circle at 32% 58%, rgba(var(--yy-star-color-soft), 0.78) 0 1px, transparent 1.8px),
    radial-gradient(circle at 84% 52%, rgba(var(--yy-star-color), 0.62) 0 1.1px, transparent 2px),
    radial-gradient(circle at 48% 82%, rgba(var(--yy-star-color-soft), 0.56) 0 1px, transparent 1.8px),
    radial-gradient(circle at 8% 86%, rgba(var(--yy-star-color), 0.4) 0 1px, transparent 1.7px);
  background-size: 210px 210px, 240px 240px, 230px 230px, 260px 260px, 250px 250px, 280px 280px;
  animation: yunyu-starfield-drift-front 22s linear infinite alternate;
}

.yunyu-starfield-flow__glow {
  border-radius: 9999px;
  filter: blur(48px);
}

.yunyu-starfield-flow__glow--primary {
  left: -10%;
  top: 8%;
  right: auto;
  bottom: auto;
  width: 34%;
  height: 36%;
  opacity: 0.24;
  background: radial-gradient(circle, rgba(var(--yy-star-glow), 0.34) 0%, rgba(var(--yy-star-glow), 0) 72%);
  animation: yunyu-starfield-glow-primary 18s ease-in-out infinite;
}

.yunyu-starfield-flow__glow--secondary {
  left: auto;
  top: auto;
  right: -4%;
  bottom: 2%;
  width: 32%;
  height: 28%;
  opacity: 0.18;
  background: radial-gradient(circle, rgba(var(--yy-star-glow-soft), 0.3) 0%, rgba(var(--yy-star-glow-soft), 0) 70%);
  animation: yunyu-starfield-glow-secondary 24s ease-in-out infinite;
}

.yunyu-starfield-flow__mist {
  inset: 0;
  background:
    radial-gradient(circle at 18% 26%, rgba(var(--yy-star-glow), 0.12) 0%, rgba(var(--yy-star-glow), 0) 34%),
    radial-gradient(circle at 72% 68%, rgba(var(--yy-star-glow-soft), 0.08) 0%, rgba(var(--yy-star-glow-soft), 0) 30%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.03) 0%, rgba(2, 6, 23, 0.08) 100%);
  animation: yunyu-starfield-mist 20s ease-in-out infinite;
}

.is-soft-density .yunyu-starfield-flow__layer--back {
  opacity: 0.26;
}

.is-soft-density .yunyu-starfield-flow__layer--front {
  opacity: 0.42;
}

.is-medium-density .yunyu-starfield-flow__layer--back {
  opacity: 0.4;
  background-size: 200px 200px, 220px 220px, 210px 210px, 240px 240px, 190px 190px;
}

.is-medium-density .yunyu-starfield-flow__layer--front {
  opacity: 0.58;
  background-size: 180px 180px, 210px 210px, 200px 200px, 220px 220px, 210px 210px, 240px 240px;
}

.is-static .yunyu-starfield-flow__layer,
.is-static .yunyu-starfield-flow__glow,
.is-static .yunyu-starfield-flow__mist {
  animation: none;
}

@keyframes yunyu-starfield-drift-back {
  0% {
    transform: translate3d(0, 0, 0) scale(1.04);
  }

  100% {
    transform: translate3d(var(--yy-star-drift-x), var(--yy-star-drift-y), 0) scale(1.08);
  }
}

@keyframes yunyu-starfield-drift-front {
  0% {
    transform: translate3d(0, 0, 0) scale(1.02);
  }

  100% {
    transform: translate3d(calc(var(--yy-star-drift-x) * -0.5), calc(var(--yy-star-drift-y) * 0.35), 0) scale(1.1);
  }
}

@keyframes yunyu-starfield-glow-primary {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.2;
  }

  50% {
    transform: translate3d(18px, 10px, 0) scale(1.08);
    opacity: 0.28;
  }
}

@keyframes yunyu-starfield-glow-secondary {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
    opacity: 0.14;
  }

  50% {
    transform: translate3d(-12px, -16px, 0) scale(1.06);
    opacity: 0.24;
  }
}

@keyframes yunyu-starfield-mist {
  0%,
  100% {
    transform: translate3d(0, 0, 0);
    opacity: 0.72;
  }

  50% {
    transform: translate3d(0, -8px, 0);
    opacity: 0.94;
  }
}

@media (prefers-reduced-motion: reduce) {
  .yunyu-starfield-flow__layer,
  .yunyu-starfield-flow__glow,
  .yunyu-starfield-flow__mist {
    animation: none !important;
    transform: none !important;
  }
}
</style>
