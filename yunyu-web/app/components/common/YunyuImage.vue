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
  fallbackText?: string
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
  wrapperClass: '',
  fallbackText: ''
})

const attrs = useAttrs()
const imageRef = ref<HTMLImageElement | null>(null)
const isLoaded = ref(false)
const hasError = ref(false)
const normalizedSrc = computed(() => props.src.trim())
const shouldRenderImage = computed(() => Boolean(normalizedSrc.value))

/**
 * 监听图片地址变化。
 * 当外部切换到新图片时，重置加载状态并重新展示骨架屏。
 */
watch(
  () => normalizedSrc.value,
  async () => {
    isLoaded.value = false
    hasError.value = !normalizedSrc.value
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
  if (!normalizedSrc.value) {
    handleError()
    return
  }

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
      v-if="!isLoaded && !hasError"
      class="yunyu-image__skeleton absolute inset-0"
      :class="[roundedClass, skeletonClass, 'opacity-100']"
      aria-hidden="true"
    />

    <div
      v-if="hasError"
      class="absolute inset-0 flex items-center justify-center px-6 text-center"
      :class="roundedClass"
    >
      <div class="absolute inset-0 bg-[linear-gradient(135deg,rgba(219,234,254,0.96)_0%,rgba(224,242,254,0.92)_34%,rgba(236,253,245,0.92)_100%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(71,85,105,0.34)_0%,rgba(71,85,105,0)_34%),radial-gradient(circle_at_bottom_right,rgba(20,184,166,0.16)_0%,rgba(20,184,166,0)_30%),linear-gradient(145deg,rgba(2,6,23,0.98)_0%,rgba(15,23,42,0.97)_46%,rgba(17,24,39,0.95)_100%)]" />
      <div class="relative max-w-[14rem] text-slate-700 dark:text-slate-100">
        <p class="text-center text-[clamp(1rem,0.9rem+0.35vw,1.22rem)] font-semibold leading-7 tracking-[-0.02em] [font-family:var(--font-display)]">
          {{ fallbackText || '遇事不决，可问春风' }}
        </p>
      </div>
    </div>

    <img
      v-if="shouldRenderImage"
      ref="imageRef"
      v-bind="attrs"
      :src="normalizedSrc"
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
