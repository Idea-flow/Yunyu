<script setup lang="ts">
import './video-player.css'

import VideoPlayerDesktop from './VideoPlayerDesktop.vue'
import VideoPlayerMobile from './VideoPlayerMobile.vue'
import VideoPlayerTablet from './VideoPlayerTablet.vue'
import { provideVideoToast } from './composables/useVideoToast'
import type { VideoPlayerEmits, VideoPlayerProps } from './player.types'
import VideoToastViewport from './VideoToastViewport.vue'

const props = withDefaults(defineProps<VideoPlayerProps>(), {
  immersivePoster: true
})

const emit = defineEmits<VideoPlayerEmits>()
provideVideoToast()

const isMobileViewport = ref(false)
const isTabletViewport = ref(false)
const isViewportResolved = ref(false)
let mobileMediaQueryList: MediaQueryList | null = null
let tabletMediaQueryList: MediaQueryList | null = null

interface LegacyMediaQueryList extends MediaQueryList {
  addListener: (listener: (event: MediaQueryListEvent) => void) => void
  removeListener: (listener: (event: MediaQueryListEvent) => void) => void
}

/**
 * 这个组件是播放器外层分发器，
 * 负责根据设备形态在手机、平板和桌面播放器之间切换。
 */
const currentPlayerComponent = computed(() => {
  if (isMobileViewport.value) {
    return VideoPlayerMobile
  }

  if (isTabletViewport.value) {
    return VideoPlayerTablet
  }

  return VideoPlayerDesktop
})

/**
 * 判断当前设备是否具备平板触控特征，
 * 兼容 iPadOS 将自身伪装成 Mac 的场景。
 */
function isTouchTabletDevice() {
  if (!import.meta.client) {
    return false
  }

  const currentUserAgent = navigator.userAgent || ''
  const isIpadUserAgent = /iPad/i.test(currentUserAgent)
  const isIpadOsDesktopMode = navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1
  const isTouchCapable = navigator.maxTouchPoints > 0
  const hasCoarsePointer = window.matchMedia('(pointer: coarse)').matches || window.matchMedia('(hover: none)').matches

  return isIpadUserAgent || isIpadOsDesktopMode || (isTouchCapable && hasCoarsePointer)
}

/**
 * 同步当前视口模式，
 * 让 iPad 进入平板专用播放器，同时避免普通桌面端被错误归类。
 */
function syncViewportMode() {
  const isMobile = Boolean(mobileMediaQueryList?.matches)
  const isTabletRange = Boolean(tabletMediaQueryList?.matches)
  const isTabletDevice = isTouchTabletDevice()

  isMobileViewport.value = isMobile
  isTabletViewport.value = !isMobile && isTabletRange && isTabletDevice
  isViewportResolved.value = true
}

onMounted(() => {
  mobileMediaQueryList = window.matchMedia('(max-width: 767px)')
  tabletMediaQueryList = window.matchMedia('(min-width: 768px) and (max-width: 1366px)')
  syncViewportMode()

  if (typeof mobileMediaQueryList.addEventListener === 'function') {
    mobileMediaQueryList.addEventListener('change', syncViewportMode)
    tabletMediaQueryList?.addEventListener('change', syncViewportMode)
    return
  }

  ;(mobileMediaQueryList as LegacyMediaQueryList).addListener(syncViewportMode)
  ;(tabletMediaQueryList as LegacyMediaQueryList).addListener(syncViewportMode)
})

onBeforeUnmount(() => {
  if (!mobileMediaQueryList || !tabletMediaQueryList) {
    return
  }

  if (typeof mobileMediaQueryList.removeEventListener === 'function') {
    mobileMediaQueryList.removeEventListener('change', syncViewportMode)
    tabletMediaQueryList.removeEventListener('change', syncViewportMode)
    return
  }

  ;(mobileMediaQueryList as LegacyMediaQueryList).removeListener(syncViewportMode)
  ;(tabletMediaQueryList as LegacyMediaQueryList).removeListener(syncViewportMode)
})
</script>

<template>
  <VideoToastViewport />

  <div
    v-if="!isViewportResolved"
    class="relative overflow-hidden rounded-[1.25rem] border border-white/8 bg-black shadow-[0_20px_56px_rgb(0_0_0_/_0.16)]"
  >
    <div class="aspect-video w-full bg-[rgb(10_12_18_/_0.96)]" />

    <img
      v-if="props.posterUrl"
      :src="props.posterUrl"
      alt=""
      aria-hidden="true"
      class="absolute inset-0 h-full w-full scale-[1.04] object-cover opacity-52 blur-xl"
    >

    <div class="absolute inset-0 bg-gradient-to-b from-black/28 via-black/16 to-black/54" />

  </div>

  <component
    v-else
    :is="currentPlayerComponent"
    :sources="props.sources"
    :controls="props.controls"
    :active-source-id="props.activeSourceId"
    :poster-url="props.posterUrl"
    :debug="props.debug"
    :immersive-poster="props.immersivePoster"
    @update:active-source-id="emit('update:activeSourceId', $event)"
  />
</template>
