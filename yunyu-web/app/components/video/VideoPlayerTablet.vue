<script setup lang="ts">
import type { VideoPlayerEmits, VideoPlayerProps } from './player.types'
import VideoBadge from './base/VideoBadge.vue'
import VideoButton from './base/VideoButton.vue'
import { useVideoPlayerController } from './composables/useVideoPlayerController'
import VideoIcon from './icons/VideoIcon.vue'

const props = withDefaults(defineProps<VideoPlayerProps>(), {
  immersivePoster: true
})

const emit = defineEmits<VideoPlayerEmits>()

/**
 * 这个组件负责 iPad 与平板端播放器视图，
 * 在保留较完整播放能力的同时，收紧控件密度与悬浮区域，让平板手感更贴近触控设备。
 */
const playerController = useVideoPlayerController(props, (event, value) => emit(event, value))
const controller = reactive(playerController)
const videoElement = playerController.videoElement
const iframeLoaded = playerController.iframeLoaded
const iframeAttributes = computed(() => controller.buildIframeAttributes(controller.activeSource))

/**
 * 为平板端沉浸式封面色层提供统一背景图来源，
 * 让加载态氛围继续跟随封面主色变化。
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
 * 生成平板端沉浸式封面文案，
 * 只在播放器建立连接与缓冲时提示当前状态。
 */
const immersivePosterText = computed(() => {
  return controller.loadingText || '正在准备播放...'
})

/**
 * 处理平板端沉浸式封面点击，
 * 让用户在封面态也能直接开始或恢复播放。
 */
function handleImmersivePosterClick() {
  void controller.togglePlayback()
}
</script>

<template>
  <section class="space-y-4">
    <div v-if="controls !== false" class="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
      <button
        v-for="source in controller.sortedSources"
        :key="source.id"
        type="button"
        class="player-chip shrink-0 rounded-full px-3.5 py-2 text-sm transition"
        :class="{ 'is-active': controller.activeSource?.id === source.id }"
        @click="controller.switchSource(source.id)"
      >
        {{ source.label }}
      </button>

      <VideoButton
        v-if="controller.activeSource"
        tone="neutral"
        variant="ghost"
        icon="copy"
        label="复制当前源"
        class="player-ghost-button shrink-0"
        @click="controller.copySourceUrl"
      />
    </div>

    <div v-if="controls !== false" class="player-info-panel grid gap-3 px-4 py-3 md:grid-cols-[1fr_auto] md:items-center">
      <div class="flex min-w-0 flex-wrap items-center gap-2 text-sm">
        <VideoBadge tone="primary">
          {{ controller.activeSource ? controller.getSourceTypeLabel(controller.activeSource.normalizedSourceType) : '暂无线路' }}
        </VideoBadge>
        <span class="truncate text-[var(--text-1)]">{{ controller.activeSource?.label || '未选择线路' }}</span>
        <span v-if="controller.nextFallbackSource" class="text-[var(--text-2)]">失败时将尝试切到：{{ controller.nextFallbackSource.label }}</span>
      </div>

      <p class="text-sm text-[var(--text-2)]">{{ controller.playerHint }}</p>
    </div>

    <div
      class="video-player-shell group overflow-hidden rounded-[1.2rem] border border-white/8 bg-black shadow-[0_20px_56px_rgb(0_0_0_/_0.18)]"
      data-player-shell
      @touchstart.passive="controller.revealControls"
      @click="controller.revealControls"
      @mousemove="controller.revealControls"
      @mouseenter="controller.revealControls"
      @mouseleave="controller.hideControls"
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
          class="video-player-media aspect-video w-full bg-black"
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
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.36)] backdrop-blur-[2px]"
        >
          <div class="mx-4 flex max-w-[82%] items-center gap-3 rounded-full border border-white/10 bg-[rgb(13_17_24_/_0.74)] px-4 py-2.5 text-sm text-[var(--text-0)] shadow-[0_10px_30px_rgba(0,0,0,0.22)]">
            <VideoIcon name="loader" size="default" class="shrink-0 animate-spin text-white/90" />
            <span class="truncate">{{ controller.loadingText }}</span>
          </div>
        </div>

        <div
          v-if="controller.sourceErrorMessage && !controller.isSwitching"
          class="absolute inset-x-4 bottom-4 z-20 rounded-[1rem] border border-amber-300/18 bg-[rgb(24_18_12_/_0.82)] p-3 text-white shadow-[0_12px_30px_rgba(0,0,0,0.24)] backdrop-blur-xl"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="text-sm font-medium text-amber-100">当前线路加载失败</p>
              <p class="mt-1 text-xs leading-5 text-amber-50/82">{{ controller.sourceErrorMessage }}</p>
            </div>

            <button
              type="button"
              class="flex size-8 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/8 text-white transition md:size-[2.1rem]"
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
              size="sm"
              icon="skip-forward"
              :label="`切换到 ${controller.nextFallbackSource.label}`"
              @click="controller.switchToNextFallback"
            />
          </div>
        </div>

        <div
          v-if="!controller.shouldShowImmersivePoster"
          class="absolute inset-0 z-10 transition-opacity duration-300"
          :class="controller.shouldShowControlsOverlay ? 'opacity-100' : 'pointer-events-none opacity-0'"
        >
          <div class="absolute inset-x-0 top-0 z-20 flex items-start justify-between bg-gradient-to-b from-black/38 via-black/14 to-transparent px-4 pb-14 pt-4">
            <div class="flex items-center gap-2">
              <button
                v-if="controller.canUseFullscreen"
                type="button"
                class="flex size-9 items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.46)] text-white shadow-[0_8px_20px_rgba(0,0,0,0.18)] backdrop-blur-xl md:size-10"
                aria-label="切换全屏"
                @pointerup.stop.prevent="controller.toggleFullscreen"
              >
                <VideoIcon :name="controller.isFullscreen ? 'minimize' : 'expand'" size="default" />
              </button>

              <button
                v-if="controller.canUsePictureInPicture"
                type="button"
                class="flex size-9 items-center justify-center rounded-full border border-white/10 bg-[rgba(21,26,34,0.46)] text-white shadow-[0_8px_20px_rgba(0,0,0,0.18)] backdrop-blur-xl md:size-10"
                aria-label="切换画中画"
                @pointerup.stop.prevent="controller.togglePictureInPicture"
              >
                <VideoIcon name="picture-in-picture" size="default" />
              </button>
            </div>

            <div class="flex items-center gap-2 rounded-full border border-white/10 bg-[rgba(18,22,30,0.34)] px-3 py-2 text-white shadow-[0_10px_24px_rgba(0,0,0,0.18)] backdrop-blur-xl">
              <button
                type="button"
                class="flex size-8 items-center justify-center rounded-full border border-white/10 bg-white/8 text-white md:size-[2.1rem]"
                aria-label="静音切换"
                @pointerup.stop.prevent="controller.toggleMute"
              >
                <VideoIcon :name="controller.isMuted || controller.volumePercent === 0 ? 'volume-off' : 'volume'" size="default" />
              </button>

              <input
                type="range"
                min="0"
                max="100"
                step="1"
                :value="controller.volumePercent"
                class="video-player-slider w-28 accent-white"
                aria-label="调节音量"
                @input="controller.updateVolume(($event.target as HTMLInputElement).value)"
              >
            </div>
          </div>

          <div class="pointer-events-none absolute inset-0 z-20 flex flex-col justify-end">
            <div class="pointer-events-auto relative z-30 px-4 pb-4 pt-16">
              <div class="mx-auto max-w-[34rem] rounded-[1.35rem] border border-white/10 bg-[rgba(15,19,28,0.30)] p-4 shadow-[0_14px_34px_rgba(0,0,0,0.22)] backdrop-blur-[22px]">
                <div class="flex items-center justify-center gap-3">
                  <button
                    type="button"
                    class="relative flex size-10 items-center justify-center rounded-full border border-white/10 bg-[rgba(18,22,30,0.34)] text-white shadow-[0_10px_24px_rgba(0,0,0,0.16)] backdrop-blur-xl md:size-11"
                    aria-label="后退 15 秒"
                    @pointerup.stop.prevent="controller.seekBy(-15)"
                  >
                    <VideoIcon name="rotate-ccw" size="action" />
                    <span class="pointer-events-none absolute left-1/2 top-1/2 ml-[0.02rem] mt-[0.52rem] -translate-x-1/2 -translate-y-1/2 text-[8px] font-medium leading-none text-white/88">15</span>
                  </button>

                  <button
                    type="button"
                    class="flex size-[3.1rem] items-center justify-center rounded-full border border-white/16 bg-[rgba(255,255,255,0.10)] text-white shadow-[0_12px_28px_rgba(0,0,0,0.20)] backdrop-blur-xl md:size-[3.4rem]"
                    aria-label="播放或暂停"
                    @pointerup.stop.prevent="controller.togglePlayback"
                  >
                    <div v-if="controller.isPlaying" class="flex items-center gap-[0.24rem]">
                      <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                      <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                    </div>
                    <VideoIcon v-else name="play" size="hero" class="translate-x-[0.06rem]" />
                  </button>

                  <button
                    type="button"
                    class="relative flex size-10 items-center justify-center rounded-full border border-white/10 bg-[rgba(18,22,30,0.34)] text-white shadow-[0_10px_24px_rgba(0,0,0,0.16)] backdrop-blur-xl md:size-11"
                    aria-label="前进 15 秒"
                    @pointerup.stop.prevent="controller.seekBy(15)"
                  >
                    <VideoIcon name="rotate-cw" size="action" />
                    <span class="pointer-events-none absolute left-1/2 top-1/2 ml-[0.02rem] mt-[0.52rem] -translate-x-1/2 -translate-y-1/2 text-[8px] font-medium leading-none text-white/88">15</span>
                  </button>
                </div>

                <div class="mt-3 space-y-1.5">
                  <div class="flex items-center gap-3">
                    <span class="min-w-[3.1rem] text-left text-[12px] font-medium tracking-[0.01em] text-white/88">
                      {{ controller.formatTime(controller.currentTime) }}
                    </span>

                    <input
                      type="range"
                      min="0"
                      :max="controller.duration || 0"
                      step="0.1"
                      :value="controller.currentTime"
                      class="video-player-slider h-1.5 flex-1 accent-white"
                      aria-label="调节播放进度"
                      @input="controller.updateSeekPosition(($event.target as HTMLInputElement).value)"
                    >

                    <span class="min-w-[3.1rem] text-right text-[12px] font-medium tracking-[0.01em] text-white/88">
                      {{ controller.formatTime(controller.duration) }}
                    </span>
                  </div>

                  <p
                    v-if="controller.progressFeedbackText"
                    class="text-center text-[11px] leading-4 text-white/72"
                  >
                    {{ controller.progressFeedbackText }}
                  </p>
                </div>
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
          class="flex aspect-video items-center justify-center bg-[rgb(8_10_15_/_0.92)] px-6 text-center text-sm text-[var(--text-2)]"
        >
          当前 iframe 播放源格式不正确，请检查后台填写内容。
        </div>
        <div
          v-if="controller.isSwitching"
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.7)] text-sm text-[var(--text-1)] backdrop-blur-sm"
        >
          正在切换播放源...
        </div>
        <div
          v-if="!iframeLoaded"
          class="absolute inset-0 flex items-center justify-center bg-black/60 px-6 text-center text-sm text-[var(--text-1)]"
        >
          {{ controller.iframeLoadingText }}
        </div>
      </div>

      <div v-else class="flex aspect-video items-center justify-center bg-[rgb(8_10_15_/_0.92)] text-sm text-[var(--text-2)]">
        当前视频暂无可用播放源。
      </div>
    </div>
  </section>
</template>
