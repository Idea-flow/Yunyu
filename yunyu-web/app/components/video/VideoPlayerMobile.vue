<script setup lang="ts">
import type { VideoPlayerEmits, VideoPlayerProps } from './player.types'
import VideoButton from './base/VideoButton.vue'
import VideoIcon from './icons/VideoIcon.vue'
import { useVideoPlayerController } from './composables/useVideoPlayerController'
import { useVideoToast } from './composables/useVideoToast'

const props = withDefaults(defineProps<VideoPlayerProps>(), {
  immersivePoster: true
})

const emit = defineEmits<VideoPlayerEmits>()
const videoToast = useVideoToast()

/**
 * 这个组件负责手机端播放器视图，
 * 采用更接近 Telegram iOS 的轻量玻璃面板，减少控件对画面的遮挡。
 */
const playerController = useVideoPlayerController(props, (event, value) => emit(event, value))
const controller = reactive(playerController)
const videoElement = playerController.videoElement
const iframeLoaded = playerController.iframeLoaded
const iframeAttributes = computed(() => controller.buildIframeAttributes(controller.activeSource))
const isSourceDrawerOpen = ref(false)

/**
 * 为手机端沉浸式封面色层提供统一背景图，
 * 让动态氛围直接跟随封面主色而变化。
 */
const immersivePosterColorStyle = computed(() => {
  if (!props.posterUrl) {
    return undefined
  }

  return {
    backgroundImage: `url("${props.posterUrl}")`
  }
})

/**
 * 生成手机端沉浸式封面层文案，
 * 保持与桌面端一致，只在加载线路时输出状态文本。
 */
const immersivePosterText = computed(() => {
  return controller.loadingText || '正在准备播放...'
})

/**
 * 打开手机端线路抽屉，便于在不占用详情页空间的情况下切换播放源。
 */
function openSourceDrawer() {
  isSourceDrawerOpen.value = true
}

/**
 * 关闭手机端线路抽屉，避免遮挡视频和详情信息。
 */
function closeSourceDrawer() {
  isSourceDrawerOpen.value = false
}

/**
 * 切换线路后立即关闭抽屉，让用户回到播放画面。
 */
function handleSelectSource(sourceId: number) {
  controller.switchSource(sourceId)
  closeSourceDrawer()
}

/**
 * 处理手机端沉浸式封面点击，
 * 让用户在封面态直接开始或恢复播放。
 */
function handleImmersivePosterClick() {
  debugTouchEvent('封面层收到点击', immersivePosterText.value)
  void controller.togglePlayback()
}

/**
 * 弹出手机端播放器调试提示，
 * 用来确认按钮事件是否真的触发到了组件层。
 */
function debugTouchEvent(title: string, description?: string) {
  if (!props.debug) {
    return
  }

  console.info(`[VideoPlayerMobile Debug] ${title}`, {
    description,
    activeSource: controller.activeSource?.label,
    sourceType: controller.activeSource?.normalizedSourceType,
    isPlaying: controller.isPlaying,
    currentTime: controller.currentTime,
    duration: controller.duration
  })

  videoToast.add({
    title,
    description,
    tone: 'warning'
  })
}

/**
 * 调试手机端播放按钮点击链路，便于区分“没点到”还是“点到了但没播放”。
 */
function handleDebugTogglePlayback() {
  debugTouchEvent('播放按钮收到事件', controller.isPlaying ? '当前准备暂停' : '当前准备播放')
  void controller.togglePlayback()
}

/**
 * 调试手机端快进快退按钮点击链路。
 */
function handleDebugSeek(offsetSeconds: number) {
  debugTouchEvent(offsetSeconds > 0 ? '快进按钮收到事件' : '后退按钮收到事件', `offset=${offsetSeconds}`)
  controller.seekBy(offsetSeconds)
}

/**
 * 兼容部分移动端浏览器对 pointer 事件支持不稳定的情况，
 * 在 touch/click 两条链路上统一触发播放调试逻辑。
 */
function handleDebugTogglePlaybackByEvent(eventName: string) {
  debugTouchEvent(`播放按钮 ${eventName}`, controller.isPlaying ? '当前准备暂停' : '当前准备播放')
  void controller.togglePlayback()
}

/**
 * 兼容部分移动端浏览器对 pointer 事件支持不稳定的情况，
 * 在 touch/click 两条链路上统一触发快进快退调试逻辑。
 */
function handleDebugSeekByEvent(offsetSeconds: number, eventName: string) {
  debugTouchEvent(`${offsetSeconds > 0 ? '快进' : '后退'}按钮 ${eventName}`, `offset=${offsetSeconds}`)
  controller.seekBy(offsetSeconds)
}
</script>

<template>
  <section class="space-y-4">
    <div v-if="controls !== false" class="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
      <button
        v-for="source in controller.sortedSources"
        :key="source.id"
        type="button"
        class="player-chip shrink-0 rounded-full px-3 py-2 text-sm transition"
        :class="{ 'is-active': controller.activeSource?.id === source.id }"
        @click="controller.switchSource(source.id)"
      >
        {{ source.label }}
      </button>
    </div>

    <div
      class="overflow-hidden rounded-[1.15rem] border border-white/8 bg-black shadow-[0_18px_46px_rgb(0_0_0_/_0.2)]"
      data-player-shell
      @touchstart.passive="controller.revealControls"
      @click="controller.revealControls"
    >
      <div
        v-if="controller.activeSource?.normalizedSourceType === 'mp4' || controller.activeSource?.normalizedSourceType === 'm3u8'"
        class="video-player-stage"
      >
        <video
          ref="videoElement"
          :src="controller.activeSource.normalizedSourceType === 'mp4' ? controller.activeSource.sourceUrl : undefined"
          :poster="props.posterUrl"
          playsinline
          preload="auto"
          class="video-player-media aspect-video w-full bg-black object-cover"
          :class="{ 'is-dimmed': props.immersivePoster && controller.shouldShowImmersivePoster }"
          @click="controller.togglePlayback"
          @error="controller.handleSourceError"
        />

        <button
          v-if="props.immersivePoster && props.posterUrl && controller.isVideoSource"
          type="button"
          class="video-player-immersive-layer"
          :class="{ 'is-visible': controller.shouldShowImmersivePoster, 'is-hidden': !controller.shouldShowImmersivePoster }"
          aria-label="切换播放状态"
          @pointerup.stop.prevent="handleImmersivePosterClick"
          @click.stop.prevent="handleImmersivePosterClick"
        >
          <img
            v-if="props.posterUrl"
            :src="props.posterUrl"
            alt=""
            aria-hidden="true"
            class="video-player-immersive-backdrop"
          >
          <div class="video-player-immersive-mask" />
          <div class="video-player-immersive-color video-player-immersive-color-a" :style="immersivePosterColorStyle" />
          <div class="video-player-immersive-color video-player-immersive-color-b" :style="immersivePosterColorStyle" />
          <div class="video-player-immersive-color video-player-immersive-color-c" :style="immersivePosterColorStyle" />

          <div class="video-player-immersive-status">
            <span>{{ immersivePosterText }}</span>
          </div>
        </button>

        <div
          v-if="!controller.shouldShowImmersivePoster && controller.shouldShowLoadingOverlay"
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.34)] backdrop-blur-[2px]"
        >
          <div class="mx-4 flex max-w-[84%] items-center gap-2.5 rounded-full border border-white/10 bg-[rgb(13_17_24_/_0.74)] px-3.5 py-2 text-[13px] text-[var(--text-0)] shadow-[0_10px_28px_rgba(0,0,0,0.22)]">
            <VideoIcon name="loader" size="default" class="shrink-0 animate-spin text-white/90" />
            <span class="truncate">{{ controller.loadingText }}</span>
          </div>
        </div>

        <div
          v-if="controller.sourceErrorMessage && !controller.isSwitching"
          class="absolute inset-x-3 bottom-3 z-20 rounded-[1rem] border border-amber-300/18 bg-[rgb(24_18_12_/_0.82)] p-3 text-white shadow-[0_12px_30px_rgba(0,0,0,0.24)] backdrop-blur-xl"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="text-sm font-medium text-amber-100">当前线路加载失败</p>
              <p class="mt-1 text-xs leading-5 text-amber-50/82">{{ controller.sourceErrorMessage }}</p>
            </div>

            <button
              type="button"
              class="flex size-8 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/8 text-white sm:size-[2.1rem]"
              aria-label="关闭提示"
              @click="controller.sourceErrorMessage = ''"
            >
              <VideoIcon name="close" size="compact" />
            </button>
          </div>

          <div v-if="controller.nextFallbackSource" class="mt-3 flex justify-end">
            <VideoButton
              tone="warning"
              variant="soft"
              size="xs"
              icon="skip-forward"
              :label="`切到 ${controller.nextFallbackSource.label}`"
              @click="controller.switchToNextFallback"
            />
          </div>
        </div>

        <div
          v-if="!controller.shouldShowImmersivePoster"
          class="absolute inset-0 z-10 transition-opacity duration-300"
          :class="controller.shouldShowControlsOverlay ? 'opacity-100' : 'pointer-events-none opacity-0'"
        >
          <div class="absolute inset-x-0 top-0 z-20 flex items-start justify-between bg-gradient-to-b from-black/36 via-black/10 to-transparent px-3.5 pb-12 pt-4">
            <div class="flex items-center gap-2">
              <button
                v-if="controls !== false"
                type="button"
                class="flex size-[2.125rem] touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.44)] text-white shadow-[0_6px_18px_rgba(0,0,0,0.18)] backdrop-blur-xl sm:size-9"
                aria-label="打开线路列表"
                @pointerup.stop.prevent="openSourceDrawer"
              >
                <VideoIcon name="list-video" size="default" />
              </button>

              <button
                v-if="controller.canUseFullscreen"
                type="button"
                class="flex size-[2.125rem] touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.44)] text-white shadow-[0_6px_18px_rgba(0,0,0,0.18)] backdrop-blur-xl sm:size-9"
                aria-label="切换全屏"
                @pointerup.stop.prevent="controller.toggleFullscreen"
              >
                <VideoIcon :name="controller.isFullscreen ? 'minimize' : 'expand'" size="default" />
              </button>

              <button
                v-if="controller.canUsePictureInPicture"
                type="button"
                class="flex size-[2.125rem] touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.44)] text-white shadow-[0_6px_18px_rgba(0,0,0,0.18)] backdrop-blur-xl sm:size-9"
                aria-label="切换画中画"
                @pointerup.stop.prevent="controller.togglePictureInPicture"
              >
                <VideoIcon name="picture-in-picture" size="default" />
              </button>
            </div>

            <button
              type="button"
              class="flex size-[2.125rem] touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.44)] text-white shadow-[0_6px_18px_rgba(0,0,0,0.18)] backdrop-blur-xl sm:size-9"
              aria-label="静音切换"
              @pointerup.stop.prevent="controller.toggleMute"
            >
              <VideoIcon :name="controller.isMuted || controller.volumePercent === 0 ? 'volume-off' : 'volume'" size="default" />
            </button>
          </div>

          <div class="pointer-events-none absolute inset-0 z-20 flex flex-col justify-between">
            <div class="flex-1" />

            <div class="pointer-events-auto relative z-30 flex items-center justify-center px-4">
              <div class="flex items-center justify-center gap-2">
                <button
                  type="button"
                  class="relative flex size-10 touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(18,22,30,0.30)] text-white shadow-[0_10px_24px_rgba(0,0,0,0.16)] backdrop-blur-xl sm:size-11"
                  aria-label="后退 15 秒"
                  @touchstart.stop.prevent="debugTouchEvent('后退按钮 touchstart', '等待执行 seekBy(-15)')"
                  @touchend.stop.prevent="handleDebugSeekByEvent(-15, 'touchend')"
                  @click.stop.prevent="handleDebugSeekByEvent(-15, 'click')"
                >
                  <VideoIcon name="rotate-ccw" size="action" />
                  <span class="pointer-events-none absolute left-1/2 top-1/2 ml-[0.02rem] mt-[0.52rem] -translate-x-1/2 -translate-y-1/2 text-[8px] font-medium leading-none text-white/88">15</span>
                </button>

                <button
                  type="button"
                  class="flex size-[3rem] touch-manipulation items-center justify-center rounded-full border border-white/16 bg-[rgba(255,255,255,0.10)] text-white shadow-[0_12px_26px_rgba(0,0,0,0.20)] backdrop-blur-xl sm:size-[3.25rem]"
                  aria-label="播放或暂停"
                  @touchstart.stop.prevent="debugTouchEvent('播放按钮 touchstart', '等待执行 togglePlayback')"
                  @touchend.stop.prevent="handleDebugTogglePlaybackByEvent('touchend')"
                  @click.stop.prevent="handleDebugTogglePlaybackByEvent('click')"
                >
                  <div v-if="controller.isPlaying" class="flex items-center gap-[0.24rem]">
                    <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                    <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                  </div>
                  <VideoIcon v-else name="play" size="hero" class="translate-x-[0.06rem]" />
                </button>

                <button
                  type="button"
                  class="relative flex size-10 touch-manipulation items-center justify-center rounded-full border border-white/10 bg-[rgba(18,22,30,0.30)] text-white shadow-[0_10px_24px_rgba(0,0,0,0.16)] backdrop-blur-xl sm:size-11"
                  aria-label="前进 15 秒"
                  @touchstart.stop.prevent="debugTouchEvent('快进按钮 touchstart', '等待执行 seekBy(15)')"
                  @touchend.stop.prevent="handleDebugSeekByEvent(15, 'touchend')"
                  @click.stop.prevent="handleDebugSeekByEvent(15, 'click')"
                >
                  <VideoIcon name="rotate-cw" size="action" />
                  <span class="pointer-events-none absolute left-1/2 top-1/2 ml-[0.02rem] mt-[0.52rem] -translate-x-1/2 -translate-y-1/2 text-[8px] font-medium leading-none text-white/88">15</span>
                </button>
              </div>
            </div>

            <div class="pointer-events-auto relative z-30 bg-gradient-to-t from-black/42 via-black/24 to-transparent px-4 pb-4 pt-12">
              <div class="mx-auto max-w-[16rem] space-y-1.5">
                <div class="flex items-center gap-3 rounded-full border border-white/8 bg-[rgba(18,22,30,0.26)] px-3 py-2 shadow-[0_10px_24px_rgba(0,0,0,0.16)] backdrop-blur-xl">
                  <span class="min-w-[2.6rem] text-left text-[12px] font-medium tracking-[0.01em] text-white/88">
                    {{ controller.formatTime(controller.currentTime) }}
                  </span>

                  <input
                    type="range"
                    min="0"
                    :max="controller.duration || 0"
                    step="0.1"
                    :value="controller.currentTime"
                    class="video-player-slider h-1 w-[7.5rem] flex-1 accent-white"
                    aria-label="调节播放进度"
                    @input="controller.updateSeekPosition(($event.target as HTMLInputElement).value)"
                  >

                  <span class="min-w-[2.6rem] text-right text-[12px] font-medium tracking-[0.01em] text-white/88">
                    {{ controller.formatTime(controller.duration) }}
                  </span>
                </div>

                <p
                  v-if="controller.progressFeedbackText"
                  class="text-center text-[11px] leading-4 text-white/70"
                >
                  {{ controller.progressFeedbackText }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-else-if="controller.activeSource?.normalizedSourceType === 'iframe' || controller.activeSource?.normalizedSourceType === 'iframeFull'"
        class="video-player-stage"
      >
        <iframe
          v-if="iframeAttributes"
          :src="iframeAttributes.src"
          class="video-player-frame aspect-video w-full bg-black"
          :title="iframeAttributes.title || controller.activeSource?.label || '嵌入视频播放器'"
          :allow="iframeAttributes.allow || 'autoplay; fullscreen; picture-in-picture'"
          :referrerpolicy="iframeAttributes.referrerpolicy"
          :sandbox="iframeAttributes.sandbox ?? controller.getIframeSandboxValue(controller.activeSource)"
          :frameborder="iframeAttributes.frameborder"
          :scrolling="iframeAttributes.scrolling"
          :loading="iframeAttributes.loading || 'lazy'"
          :allowfullscreen="iframeAttributes.allowfullscreen || undefined"
          @load="iframeLoaded = true; controller.handleIframeLoad()"
        />
        <div
          v-else
          class="flex aspect-video items-center justify-center bg-[rgb(8_10_15_/_0.92)] px-5 text-center text-sm text-[var(--text-2)]"
        >
          当前 iframe 播放源格式不正确，请检查后台填写内容。
        </div>
        <div
          v-if="controller.isSwitching"
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.68)] text-sm text-[var(--text-1)] backdrop-blur-sm"
        >
          正在切换播放源...
        </div>
        <div
          v-if="!iframeLoaded"
          class="absolute inset-0 flex items-center justify-center bg-black/60 px-5 text-center text-sm text-[var(--text-1)]"
        >
          {{ controller.iframeLoadingText }}
        </div>
      </div>

      <div v-else class="flex aspect-video items-center justify-center bg-[rgb(8_10_15_/_0.92)] text-sm text-[var(--text-2)]">
        当前视频暂无可用播放源。
      </div>
    </div>

    <div v-if="controls !== false" class="player-info-panel space-y-3 px-4 py-3">
      <div class="flex items-center justify-between gap-3">
        <div class="min-w-0">
          <p class="truncate text-sm font-medium text-[var(--text-0)]">{{ controller.activeSource?.label || '未选择线路' }}</p>
          <p class="mt-1 text-xs text-[var(--text-2)]">
            {{ controller.activeSource ? controller.getSourceTypeLabel(controller.activeSource.normalizedSourceType) : '暂无线路' }}
          </p>
        </div>

        <VideoButton
          v-if="controller.activeSource"
          tone="neutral"
          variant="ghost"
          icon="copy"
          size="sm"
          class="player-ghost-button shrink-0"
          @click="controller.copySourceUrl"
        />
      </div>

      <p class="text-xs leading-5 text-[var(--text-2)]">{{ controller.playerHint }}</p>
    </div>

    <Transition
      enter-active-class="transition duration-260 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isSourceDrawerOpen"
        class="fixed inset-0 z-[70]"
      >
        <div
          class="absolute inset-0 bg-black/48 backdrop-blur-sm"
          @click="closeSourceDrawer"
        />

        <div
          class="absolute inset-x-0 bottom-0 rounded-t-[1.6rem] border border-white/10 bg-[rgb(13_17_24_/_0.96)] px-4 pb-[calc(1rem+env(safe-area-inset-bottom))] pt-3 shadow-[0_-18px_40px_rgba(0,0,0,0.28)] backdrop-blur-xl transition duration-260 ease-out"
          :class="isSourceDrawerOpen ? 'translate-y-0' : 'translate-y-full'"
          @click.stop
        >
          <div class="mx-auto mb-3 h-1.5 w-12 rounded-full bg-white/14" />

          <div class="mb-4 flex items-center justify-between gap-3">
            <div>
              <p class="text-sm font-medium text-[var(--text-0)]">切换线路</p>
              <p class="mt-1 text-xs text-[var(--text-2)]">选择更稳定或更适合当前设备的播放源</p>
            </div>

            <button
              type="button"
              class="flex size-[2.125rem] items-center justify-center rounded-full border border-white/10 bg-white/6 text-white sm:size-9"
              aria-label="关闭线路抽屉"
              @click="closeSourceDrawer"
            >
              <VideoIcon name="close" size="compact" />
            </button>
          </div>

          <div class="space-y-2">
            <button
              v-for="source in controller.sortedSources"
              :key="source.id"
              type="button"
              class="flex w-full items-center justify-between gap-3 rounded-[1rem] border px-4 py-3 text-left transition"
              :class="controller.activeSource?.id === source.id
                ? 'border-primary-400/40 bg-primary-500/12 text-[var(--text-0)]'
                : 'border-white/8 bg-white/4 text-[var(--text-1)]'"
              @click="handleSelectSource(source.id)"
            >
              <div class="min-w-0">
                <p class="truncate text-sm font-medium">{{ source.label }}</p>
                <p class="mt-1 text-xs text-[var(--text-2)]">
                  {{ controller.getSourceTypeLabel(source.normalizedSourceType) }}
                </p>
              </div>

              <VideoIcon
                :name="controller.activeSource?.id === source.id ? 'check' : 'chevron-right'"
                size="default"
                class="shrink-0"
              />
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </section>
</template>
