<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useAttrs, watch } from 'vue'

/**
 * 云屿通用图片组件。
 * 作用：统一处理前台图片的加载占位、过渡显示与暗色模式视觉表现，
 * 作为后续图片展示场景的基础公共组件。
 */
defineOptions({
  inheritAttrs: false
})

interface YunyuImageProps {
  src: string
  alt?: string
  width?: number | string
  height?: number | string
  loading?: 'lazy' | 'eager'
  decoding?: 'async' | 'auto' | 'sync'
  roundedClass?: string
  objectFitClass?: string
  imageClass?: string
  skeletonClass?: string
  wrapperClass?: string
}

const props = withDefaults(defineProps<YunyuImageProps>(), {
  alt: '',
  width: undefined,
  height: undefined,
  loading: 'lazy',
  decoding: 'async',
  roundedClass: 'rounded-[1.5rem]',
  objectFitClass: 'object-cover',
  imageClass: '',
  skeletonClass: '',
  wrapperClass: ''
})

const attrs = useAttrs()
const imageRef = ref<HTMLImageElement | null>(null)
const isLoaded = ref(false)
const hasError = ref(false)

/**
 * 监听图片地址变化。
 * 当外部切换到新图片时，重置加载状态并重新展示骨架屏。
 */
watch(
  () => props.src,
  async () => {
    isLoaded.value = false
    hasError.value = false
    await nextTick()
    syncImageState()
  }
)

/**
 * 处理图片加载成功事件。
 * 作用：关闭占位层，并触发图片渐显过渡。
 */
function handleLoad() {
  isLoaded.value = true
  hasError.value = false
}

/**
 * 处理图片加载失败事件。
 * 作用：保留占位底板，避免界面出现突兀的空白区域。
 */
function handleError() {
  isLoaded.value = false
  hasError.value = true
}

/**
 * 同步当前图片真实加载状态。
 * 作用：处理浏览器缓存命中场景，避免图片已完成但骨架屏仍停留在界面上。
 */
function syncImageState() {
  const element = imageRef.value

  if (!element || !element.src) {
    return
  }

  if (element.complete && element.naturalWidth > 0) {
    handleLoad()
    return
  }

  if (element.complete && element.naturalWidth === 0) {
    handleError()
  }
}

/**
 * 计算容器样式。
 * 当组件接收到宽高时，统一转成内联样式透传给外层容器。
 */
const wrapperStyle = computed(() => ({
  width: props.width,
  height: props.height
}))

onMounted(() => {
  syncImageState()
})
</script>

<template>
  <div
    class="yunyu-image group relative overflow-hidden bg-slate-200/80 dark:bg-slate-800/70"
    :class="[roundedClass, wrapperClass]"
    :style="wrapperStyle"
  >
    <div
      v-if="!isLoaded"
      class="yunyu-image__skeleton absolute inset-0"
      :class="[roundedClass, skeletonClass, hasError ? 'opacity-80' : 'opacity-100']"
      aria-hidden="true"
    />

    <img
      ref="imageRef"
      v-bind="attrs"
      :src="src"
      :alt="alt"
      :loading="loading"
      :decoding="decoding"
      class="block h-full w-full transition-all duration-500 ease-out dark:brightness-[0.82]"
      :class="[
        roundedClass,
        objectFitClass,
        imageClass,
        isLoaded ? 'scale-100 opacity-100' : 'scale-[1.02] opacity-0'
      ]"
      @load="handleLoad"
      @error="handleError"
    >
  </div>
</template>

<style scoped>
.yunyu-image__skeleton {
  background:
    linear-gradient(
      110deg,
      rgba(255, 255, 255, 0.06) 8%,
      rgba(255, 255, 255, 0.3) 18%,
      rgba(255, 255, 255, 0.06) 33%
    ),
    linear-gradient(180deg, rgba(148, 163, 184, 0.18), rgba(203, 213, 225, 0.4));
  background-size: 200% 100%;
  animation: yunyu-image-shimmer 1.6s linear infinite;
}

.dark .yunyu-image__skeleton {
  background:
    linear-gradient(
      110deg,
      rgba(255, 255, 255, 0.03) 8%,
      rgba(255, 255, 255, 0.12) 18%,
      rgba(255, 255, 255, 0.03) 33%
    ),
    linear-gradient(180deg, rgba(30, 41, 59, 0.88), rgba(51, 65, 85, 0.92));
}

@keyframes yunyu-image-shimmer {
  to {
    background-position-x: -200%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .yunyu-image__skeleton {
    animation: none;
  }
}
</style>
