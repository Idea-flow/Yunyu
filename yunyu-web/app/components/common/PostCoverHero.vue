<script setup lang="ts">
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuStarfieldFlow from '~/components/motion/YunyuStarfieldFlow.vue'

/**
 * 前台文章封面首屏组件。
 * 作用：统一文章详情页、文章列表页等场景的封面首屏布局、遮罩和底部过渡效果，避免重复维护同一套首屏结构。
 */
withDefaults(defineProps<{
  src?: string
  alt?: string
  heightClass?: string
  overlayClass?: string
  contentWrapperClass?: string
  contentContainerClass?: string
  showStarry?: boolean
  starfieldVariant?: 'sky' | 'warm' | 'neutral'
  showHeroMarker?: boolean
}>(), {
  src: '',
  alt: '',
  heightClass: 'h-[34svh] min-h-[280px] w-full sm:h-[42svh] sm:min-h-[340px] lg:h-[54svh]',
  overlayClass: 'bg-[linear-gradient(180deg,rgba(15,23,42,0.18)_0%,rgba(15,23,42,0.18)_18%,rgba(15,23,42,0.22)_42%,rgba(15,23,42,0.62)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.1)_0%,rgba(2,6,23,0.14)_20%,rgba(2,6,23,0.26)_44%,rgba(2,6,23,0.72)_100%)]',
  contentWrapperClass: 'absolute inset-x-0 bottom-0 z-10',
  contentContainerClass: 'mx-auto max-w-[1440px] px-5 pb-8 sm:px-8 sm:pb-12 lg:px-10 lg:pb-14',
  showStarry: true,
  starfieldVariant: 'sky',
  showHeroMarker: true
})
</script>

<template>
  <section
    class="relative overflow-hidden"
    v-bind="showHeroMarker ? { 'data-post-cover-hero': '' } : {}"
  >
    <div class="relative w-full" :class="heightClass">
      <div class="absolute inset-0">
        <template v-if="src">
          <YunyuImage
            :src="src"
            :alt="alt"
            wrapper-class="absolute inset-0 h-full w-full"
            image-class="h-full w-full"
            rounded-class="rounded-none"
          />
        </template>

        <div
          v-else
          class="absolute inset-0 bg-[linear-gradient(135deg,#dbeafe_0%,#f8fbff_45%,#ffffff_100%)] dark:bg-[linear-gradient(135deg,#0f172a_0%,#111827_50%,#020617_100%)]"
        />

        <div class="pointer-events-none absolute inset-0" :class="overlayClass" />
        <YunyuStarfieldFlow
          v-if="showStarry"
          :variant="starfieldVariant"
          density="soft"
        />
      </div>

      <div
        v-if="$slots.center"
        class="pointer-events-none absolute inset-x-0 top-1/2 z-[9] -translate-y-1/2"
      >
        <div class="mx-auto max-w-[1440px] px-5 sm:px-8 lg:px-10">
          <div class="pointer-events-auto mx-auto w-full max-w-4xl">
            <slot name="center" />
          </div>
        </div>
      </div>

      <div :class="contentWrapperClass">
        <div :class="contentContainerClass">
          <slot />
        </div>
      </div>

      <div class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(244,248,255,0.9)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0)_0%,rgba(2,6,23,0.96)_100%)]" />
    </div>
  </section>
</template>
