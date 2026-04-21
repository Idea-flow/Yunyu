<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import VideoPlayer from '../video/VideoPlayer.vue'
import type { VideoSourceItem } from '../video/player.types'

/**
 * 云屿视频播放器包装组件。
 * 作用：统一承接文章、专题等页面传入的视频地址，
 * 在组件内部完成视频源类型推断、播放器数据结构转换与公共样式封装，
 * 让业务页面只需要提供视频地址和封面地址即可复用同一套播放能力。
 */
const props = withDefaults(defineProps<{
  videoUrl?: string
  posterUrl?: string
}>(), {
  videoUrl: '',
  posterUrl: ''
})

const activeSourceId = ref<number | null>(1)

/**
 * 判断当前组件是否具备可播放视频。
 * 作用：避免外层页面需要重复判断空视频地址，组件内部自行决定是否渲染播放器。
 */
const hasVideo = computed(() => Boolean(props.videoUrl?.trim()))

/**
 * 根据视频地址推断播放器源类型。
 * 作用：让调用方只传一个地址时，组件也能自动匹配 `mp4`、`m3u8`、`iframe` 或 `iframeFull`。
 *
 * @param videoUrl 原始视频地址或 iframe 片段
 * @returns 播放器可识别的视频源类型
 */
function inferVideoSourceType(videoUrl: string): VideoSourceItem['sourceType'] {
  const normalizedVideoUrl = videoUrl.trim()
  const lowerVideoUrl = normalizedVideoUrl.toLowerCase()

  if (lowerVideoUrl.includes('<iframe')) {
    return 'iframeFull'
  }

  if (lowerVideoUrl.includes('.m3u8')) {
    return 'm3u8'
  }

  if (
    lowerVideoUrl.includes('youtube.com')
    || lowerVideoUrl.includes('youtu.be')
    || lowerVideoUrl.includes('player.bilibili.com')
    || lowerVideoUrl.includes('bilibili.com/video')
    || lowerVideoUrl.includes('vimeo.com')
    || lowerVideoUrl.includes('youku.com')
    || lowerVideoUrl.includes('/embed/')
  ) {
    return 'iframe'
  }

  return 'mp4'
}

/**
 * 计算播放器数据源列表。
 * 作用：把单个视频地址转换成 `VideoPlayer` 需要的 `sources` 结构，
 * 同时为不同源类型提供默认标签，便于播放器内部展示与调试。
 */
const videoSources = computed<VideoSourceItem[]>(() => {
  const videoUrl = props.videoUrl?.trim()

  if (!videoUrl) {
    return []
  }

  const sourceType = inferVideoSourceType(videoUrl)
  const sourceLabelMap: Record<VideoSourceItem['sourceType'], string> = {
    mp4: '视频播放',
    m3u8: 'M3U8 播放',
    iframe: '嵌入播放',
    iframeFull: '嵌入播放'
  }

  return [{
    id: 1,
    label: sourceLabelMap[sourceType],
    sourceType,
    sourceUrl: videoUrl
  }]
})

/**
 * 同步当前激活的视频源。
 * 作用：当传入地址变化时，确保播放器始终回到当前唯一可用的那一路视频源。
 */
watch(videoSources, value => {
  activeSourceId.value = value[0]?.id ?? null
}, { immediate: true })
</script>

<template>
  <section
    v-if="hasVideo"
    class="overflow-hidden rounded-[24px] border border-white/60 bg-white/84 p-1.5 shadow-[0_24px_60px_-44px_rgba(15,23,42,0.24)] backdrop-blur-xl sm:rounded-[34px] sm:p-2 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-slate-950/70"
  >
    <VideoPlayer
      v-model:active-source-id="activeSourceId"
      :sources="videoSources"
      :poster-url="props.posterUrl || undefined"
    />
  </section>
</template>
