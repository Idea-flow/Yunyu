<script setup lang="ts">
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuStarfieldFlow from '~/components/motion/YunyuStarfieldFlow.vue'

/**
 * 云屿通用 Hero 组件。
 * 作用：统一承接首页、文章详情页等大图首屏的背景图、遮罩、星空氛围与底部信息区布局，
 * 作为前台大图 Hero 视觉语言的公共基础组件。
 */
interface YunyuHeroProps {
  src: string
  alt?: string
  minHeightClass?: string
  overlayClass?: string
  contentWidthClass?: string
  contentPaddingClass?: string
  showStarry?: boolean
  starfieldVariant?: 'sky' | 'warm' | 'neutral'
  showHeroMarker?: boolean
}

const props = withDefaults(defineProps<YunyuHeroProps>(), {
  alt: '',
  minHeightClass: 'min-h-[74svh] sm:min-h-[80svh] lg:min-h-[88svh]',
  overlayClass: 'bg-[linear-gradient(180deg,rgba(2,6,23,0.18)_0%,rgba(2,6,23,0.26)_24%,rgba(2,6,23,0.5)_56%,rgba(2,6,23,0.88)_100%)]',
  contentWidthClass: 'max-w-4xl',
  contentPaddingClass: 'px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-14',
  showStarry: true,
  starfieldVariant: 'sky',
  showHeroMarker: true
})

/**
 * 计算首屏容器样式。
 * 作用：统一不同页面的大图高度策略，避免首页和详情页各自定义一套高度规则。
 */
const heroClassName = computed(() => ['relative', props.minHeightClass].join(' '))
</script>

<template>
  <section
    class="relative overflow-hidden"
    v-bind="showHeroMarker ? { 'data-post-cover-hero': '' } : {}"
  >
    <div :class="heroClassName">
      <YunyuImage
        :src="src"
        :alt="alt"
        wrapper-class="absolute inset-0 h-full w-full"
        image-class="h-full w-full"
        rounded-class="rounded-none"
      />
      <div class="pointer-events-none absolute inset-0" :class="overlayClass" />

      <YunyuStarfieldFlow
        v-if="showStarry"
        :variant="starfieldVariant"
        density="soft"
      />

      <div
        v-if="$slots.center"
        class="pointer-events-none absolute inset-x-0 top-1/2 z-10 -translate-y-1/2"
      >
        <div class="mx-auto max-w-[1440px] px-5 sm:px-8 lg:px-10">
          <div class="pointer-events-auto mx-auto w-full max-w-4xl">
            <slot name="center" />
          </div>
        </div>
      </div>

      <div class="absolute inset-x-0 bottom-0 z-10">
        <div class="mx-auto max-w-[1440px]" :class="contentPaddingClass">
          <div :class="contentWidthClass">
            <slot />
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
