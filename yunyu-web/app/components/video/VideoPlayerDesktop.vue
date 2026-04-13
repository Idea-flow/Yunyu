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
 * 这个组件负责桌面端播放器视图，
 * 保留完整的音量、画中画和较宽的控制区布局。
 */
const playerController = useVideoPlayerController(props, (event, value) => emit(event, value))
const controller = reactive(playerController)
const videoElement = playerController.videoElement
const iframeLoaded = playerController.iframeLoaded
const iframeAttributes = computed(() => controller.buildIframeAttributes(controller.activeSource))

/**
 * 为沉浸式封面色层提供统一的背景图来源，
 * 让氛围色块直接从当前封面图本身派生颜色，而不是使用固定色板。
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
 * 生成沉浸式封面态的状态文案，
 * 只在加载线路时展示连接反馈，不再承担暂停提示。
 */
const immersivePosterText = computed(() => {
  return controller.loadingText || '正在准备播放...'
})

/**
 * 处理沉浸式封面层点击，
 * 让封面态可以直接承担播放与恢复播放的入口。
 */
function handleImmersivePosterClick() {
  void controller.togglePlayback()
}
</script>

<template>
  <section class="space-y-5">
    <div v-if="controls !== false" class="flex flex-wrap items-center gap-2">
      <button
        v-for="source in controller.sortedSources"
        :key="source.id"
        type="button"
        class="player-chip rounded-full px-4 py-2 text-sm transition"
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
        class="player-ghost-button"
        @click="controller.copySourceUrl"
      />
    </div>

    <div v-if="controls !== false" class="player-info-panel grid gap-4 px-4 py-4 lg:grid-cols-[1fr_auto] lg:items-center">
      <div class="flex flex-wrap items-center gap-2 text-sm">
        <VideoBadge tone="primary">
          {{ controller.activeSource ? controller.getSourceTypeLabel(controller.activeSource.normalizedSourceType) : '暂无线路' }}
        </VideoBadge>
        <span class="text-[var(--text-1)]">{{ controller.activeSource?.label || '未选择线路' }}</span>
        <span v-if="controller.nextFallbackSource" class="text-[var(--text-2)]">失败时将尝试切到：{{ controller.nextFallbackSource.label }}</span>
      </div>

      <p class="text-sm text-[var(--text-2)]">{{ controller.playerHint }}</p>
    </div>

    <div
      class="video-player-shell group overflow-hidden rounded-[1.25rem] border border-white/8 bg-black shadow-[0_20px_56px_rgb(0_0_0_/_0.16)]"
      data-player-shell
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
          @click="handleImmersivePosterClick"
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
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.38)] backdrop-blur-[2px]"
        >
          <div class="flex items-center gap-3 rounded-full border border-white/10 bg-[rgb(13_17_24_/_0.7)] px-4 py-2.5 text-sm text-[var(--text-0)] shadow-[0_10px_30px_rgba(0,0,0,0.22)]">
            <VideoIcon name="loader" size="default" class="animate-spin text-white/90" />
            <span>{{ controller.loadingText }}</span>
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
              class="flex size-8 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/8 text-white transition hover:bg-white/12"
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
          class="video-player-overlay"
          :class="{ 'is-visible': controller.shouldShowControlsOverlay }"
        >
          <div class="video-player-toolbar">
            <div class="video-player-toolbar-left">
              <button
                v-if="controller.canUseFullscreen"
                type="button"
                class="video-player-icon-button"
                aria-label="切换全屏"
                @click="controller.toggleFullscreen"
              >
                <VideoIcon :name="controller.isFullscreen ? 'minimize' : 'expand'" size="default" />
              </button>

              <button
                v-if="controller.canUsePictureInPicture"
                type="button"
                class="video-player-icon-button"
                aria-label="切换画中画"
                @click="controller.togglePictureInPicture"
              >
                <VideoIcon name="picture-in-picture" size="default" />
              </button>
            </div>

            <div class="video-player-volume">
              <button
                type="button"
                class="video-player-icon-button"
                aria-label="静音切换"
                @click="controller.toggleMute"
              >
                <VideoIcon :name="controller.isMuted || controller.volumePercent === 0 ? 'volume-off' : 'volume'" size="default" />
              </button>

              <input
                type="range"
                min="0"
                max="100"
                step="1"
                :value="controller.volumePercent"
                class="video-player-slider video-player-volume-slider"
                aria-label="调节音量"
                @input="controller.updateVolume(($event.target as HTMLInputElement).value)"
              >
            </div>
          </div>

          <div class="video-player-controls">
            <div class="video-player-controls-left">
              <button
                type="button"
                class="video-player-icon-button"
                aria-label="后退 15 秒"
                @click="controller.seekBy(-15)"
              >
                <VideoIcon name="rotate-ccw" size="action" />
                <span class="video-player-badge">15</span>
              </button>

              <button
                type="button"
                class="video-player-play-button"
                aria-label="播放或暂停"
                @click="controller.togglePlayback"
              >
                <div v-if="controller.isPlaying" class="flex items-center gap-[0.24rem]">
                  <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                  <span class="block h-4.5 w-[0.24rem] rounded-full bg-white/95" />
                </div>
                <VideoIcon v-else name="play" size="hero" class="translate-x-[0.06rem]" />
              </button>

              <button
                type="button"
                class="video-player-icon-button"
                aria-label="前进 15 秒"
                @click="controller.seekBy(15)"
              >
                <VideoIcon name="rotate-cw" size="action" />
                <span class="video-player-badge">15</span>
              </button>
            </div>

            <div class="video-player-progress-wrap">
              <span class="video-player-time">{{ controller.formatTime(controller.currentTime) }}</span>
              <div class="space-y-1">
                <input
                  type="range"
                  min="0"
                  :max="controller.duration || 0"
                  step="0.1"
                  :value="controller.currentTime"
                  class="video-player-slider video-player-progress"
                  aria-label="调节播放进度"
                  @input="controller.updateSeekPosition(($event.target as HTMLInputElement).value)"
                >
                <p
                  v-if="controller.progressFeedbackText"
                  class="text-center text-[11px] leading-4 text-white/72"
                >
                  {{ controller.progressFeedbackText }}
                </p>
              </div>
              <span class="video-player-time">{{ controller.formatTime(controller.duration) }}</span>
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
          class="absolute inset-0 z-10 flex items-center justify-center bg-[rgb(8_10_15_/_0.72)] text-sm text-[var(--text-1)] backdrop-blur-sm"
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
