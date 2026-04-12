<script setup lang="ts">
import type { CSSProperties } from 'vue'

/**
 * 云屿亮色云朵组件。
 * 作用：在首页首屏左侧提供一朵轻量的浅色云团，
 * 用缓慢飘出的方式替代树影，让明亮模式更干净、更轻盈。
 */
interface YunyuParticleTreeLightProps {
  treeAreaRatio?: number
  maxTreeWidth?: number
  minTreeWidth?: number
  anchorOffsetX?: number
}

interface LightCloudLayout {
  width: number
  height: number
  canvasLeft: number
  canvasWidth: number
}

const rootRef = ref<HTMLDivElement | null>(null)
const prefersReducedMotion = ref(false)
const props = withDefaults(defineProps<YunyuParticleTreeLightProps>(), {
  treeAreaRatio: 0.42,
  maxTreeWidth: 760,
  minTreeWidth: 320,
  anchorOffsetX: 0
})

const layoutState = ref<LightCloudLayout>({
  width: 0,
  height: 0,
  canvasLeft: 0,
  canvasWidth: 520
})

let resizeObserver: ResizeObserver | null = null
let reducedMotionMediaQuery: MediaQueryList | null = null

/**
 * 将数值限制在指定区间。
 * 作用：统一控制亮色云朵的宽度，让装饰在不同屏幕下保持轻盈比例。
 *
 * @param value 当前值
 * @param min 最小值
 * @param max 最大值
 * @returns 限制后的结果
 */
function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max)
}

/**
 * 同步系统“减少动态效果”偏好。
 * 作用：在用户偏好静态时关闭云朵漂移动画，只保留静态浅云轮廓。
 */
function syncReducedMotionPreference() {
  prefersReducedMotion.value = Boolean(reducedMotionMediaQuery?.matches)
}

/**
 * 根据容器尺寸计算亮色云朵布局。
 * 作用：让云朵只占据首页左上局部区域，避免明亮模式下装饰过满。
 *
 * @param width 首屏容器宽度
 * @param height 首屏容器高度
 * @returns 当前云朵布局信息
 */
function createLightCloudLayout(width: number, height: number): LightCloudLayout {
  const stageWidth = clamp(
    width * props.treeAreaRatio * 0.72,
    Math.min(props.minTreeWidth, width * 0.68),
    Math.min(props.maxTreeWidth * 0.66, width * 0.56)
  )
  const paddingLeft = 36

  return {
    width,
    height,
    canvasLeft: width * 0.02 + props.anchorOffsetX - paddingLeft,
    canvasWidth: stageWidth + paddingLeft + 88
  }
}

/**
 * 同步云朵布局。
 * 作用：在首页尺寸变化时重新计算云朵位置，保证明亮模式下的留白关系稳定。
 */
function syncLayout() {
  if (!import.meta.client || !rootRef.value) {
    return
  }

  const { width, height } = rootRef.value.getBoundingClientRect()

  if (!width || !height) {
    return
  }

  layoutState.value = createLightCloudLayout(width, height)
}

/**
 * 处理系统动态偏好变化。
 * 作用：当系统切换为减少动态时，立即让云层改为静态展示。
 */
function handleReducedMotionChange() {
  syncReducedMotionPreference()
}

/**
 * 计算亮色云朵容器样式。
 * 作用：将云朵限制在首屏左侧区域，避免飘到正文中间造成干扰。
 *
 * @returns 云朵外层样式
 */
const cloudStyle = computed<CSSProperties>(() => {
  return {
    left: `${layoutState.value.canvasLeft}px`,
    width: `${layoutState.value.canvasWidth}px`,
    height: `${layoutState.value.height}px`
  }
})

onMounted(() => {
  if (!import.meta.client || !rootRef.value) {
    return
  }

  reducedMotionMediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  reducedMotionMediaQuery.addEventListener('change', handleReducedMotionChange)
  syncReducedMotionPreference()

  resizeObserver = new window.ResizeObserver(syncLayout)
  resizeObserver.observe(rootRef.value)
  syncLayout()
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  reducedMotionMediaQuery?.removeEventListener('change', handleReducedMotionChange)
})
</script>

<template>
  <div ref="rootRef" class="yunyu-particle-tree-light" aria-hidden="true">
    <div
      class="yunyu-particle-tree-light__cloud"
      :class="prefersReducedMotion ? 'is-static' : ''"
      :style="cloudStyle"
    >
      <div class="yunyu-particle-tree-light__haze" />
      <div class="yunyu-particle-tree-light__body" />
      <div class="yunyu-particle-tree-light__tail" />
    </div>
  </div>
</template>

<style scoped>
/**
 * 云屿亮色云朵组件样式。
 * 作用：为明亮模式提供一层极简、轻量的云朵飘移装饰，
 * 让首页保持干净而不过度空白。
 */
.yunyu-particle-tree-light {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  contain: layout paint;
}

.yunyu-particle-tree-light__cloud {
  position: absolute;
  inset: 0 auto 0 0;
  display: block;
}

.yunyu-particle-tree-light__haze,
.yunyu-particle-tree-light__body,
.yunyu-particle-tree-light__tail {
  position: absolute;
  left: 0;
  border-radius: 999px;
  will-change: transform, opacity;
}

.yunyu-particle-tree-light__haze {
  top: 16%;
  width: 66%;
  height: 21%;
  background: radial-gradient(circle at 35% 50%, rgba(191, 219, 254, 0.18) 0%, rgba(191, 219, 254, 0.06) 42%, rgba(191, 219, 254, 0) 76%);
  filter: blur(34px);
  opacity: 0.7;
}

.yunyu-particle-tree-light__body {
  top: 18%;
  width: 58%;
  height: 13%;
  background:
    radial-gradient(ellipse 26% 54% at 20% 55%, rgba(255, 255, 255, 0.84) 0%, rgba(255, 255, 255, 0.54) 56%, rgba(255, 255, 255, 0) 100%),
    radial-gradient(ellipse 28% 58% at 46% 50%, rgba(255, 255, 255, 0.8) 0%, rgba(248, 250, 252, 0.48) 56%, rgba(248, 250, 252, 0) 100%),
    radial-gradient(ellipse 24% 52% at 72% 56%, rgba(255, 255, 255, 0.74) 0%, rgba(241, 245, 249, 0.42) 56%, rgba(241, 245, 249, 0) 100%);
  filter: blur(10px);
  opacity: 0.88;
  animation: yunyu-light-cloud-drift 22s ease-in-out infinite;
}

.yunyu-particle-tree-light__tail {
  top: 22%;
  left: 34%;
  width: 26%;
  height: 6%;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.16) 0%, rgba(255, 255, 255, 0.04) 50%, rgba(255, 255, 255, 0) 100%);
  filter: blur(12px);
  opacity: 0.5;
  animation: yunyu-light-cloud-tail 22s ease-in-out infinite;
}

.is-static .yunyu-particle-tree-light__body,
.is-static .yunyu-particle-tree-light__tail {
  animation: none;
  transform: none;
}

@keyframes yunyu-light-cloud-drift {
  0% {
    transform: translate3d(-8%, 0, 0);
    opacity: 0;
  }

  18% {
    opacity: 0.88;
  }

  72% {
    opacity: 0.84;
  }

  100% {
    transform: translate3d(14%, -4%, 0);
    opacity: 0;
  }
}

@keyframes yunyu-light-cloud-tail {
  0% {
    transform: translate3d(-12%, 0, 0) scaleX(0.88);
    opacity: 0;
  }

  20% {
    opacity: 0.48;
  }

  74% {
    opacity: 0.42;
  }

  100% {
    transform: translate3d(18%, -3%, 0) scaleX(1.06);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .yunyu-particle-tree-light__body,
  .yunyu-particle-tree-light__tail {
    animation: none !important;
    transform: none !important;
  }
}
</style>
