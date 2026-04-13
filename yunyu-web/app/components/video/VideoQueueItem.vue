<script setup lang="ts">
import { buildVideoPath } from '~/utils/seo'

/**
 * 右侧相似视频紧凑队列项组件。
 * 负责在详情页侧栏里以更高信息密度展示封面、标题、时长与标签，形成更接近播放列表的浏览节奏。
 */
interface TagItem {
  id: number
  name: string
  slug: string
}

const coverLoaded = ref(false)
const coverFailed = ref(false)
const coverImageElement = ref<HTMLImageElement | null>(null)

const props = defineProps<{
  video: {
    id: number
    title: string
    coverUrl: string
    summary: string
    duration: string
    tags: TagItem[]
  }
}>()

/**
 * 标记缩略图已加载完成，
 * 用于结束骨架占位并显示真实封面。
 */
function handleImageLoad() {
  coverLoaded.value = true
  coverFailed.value = false
}

/**
 * 标记缩略图加载失败，
 * 让侧栏卡片回退到统一的渐变占位视觉。
 */
function handleImageError() {
  coverLoaded.value = true
  coverFailed.value = true
}

/**
 * 生成侧栏列表项的摘要文案，
 * 优先展示简介，没有简介时回退到首个标签，保持右侧列表信息密度稳定。
 */
const secondaryText = computed(() => {
  if (props.video.summary?.trim()) {
    return props.video.summary.trim()
  }

  return props.video.tags.slice(0, 2).map((item) => item.name).join(' · ')
})

/**
 * 在客户端挂载后兜底同步图片状态，
 * 避免浏览器命中缓存时错过 load 事件导致图片一直不显示。
 */
onMounted(() => {
  if (!coverImageElement.value) {
    return
  }

  if (coverImageElement.value.complete) {
    coverLoaded.value = true
    coverFailed.value = coverImageElement.value.naturalWidth === 0
  }
})
</script>

<template>
  <NuxtLink
    :to="buildVideoPath(props.video)"
    :prefetch="false"
    class="group flex items-start gap-2.5 py-2 transition"
  >
    <div class="relative w-[10.5rem] shrink-0 overflow-hidden rounded-[0.8rem] bg-[rgba(15,23,42,0.06)] shadow-[0_8px_20px_rgba(0,0,0,0.08)] transition duration-300 group-hover:shadow-[0_14px_24px_rgba(0,0,0,0.12)] dark:bg-[linear-gradient(180deg,rgba(20,28,38,0.96),rgba(13,18,27,0.9))]">
      <div
        v-if="!coverLoaded"
        class="absolute inset-0 animate-[shimmerSlide_1.35s_linear_infinite] bg-[linear-gradient(90deg,rgba(255,255,255,0.08)_0%,rgba(255,255,255,0.22)_22%,rgba(255,255,255,0.32)_48%,rgba(255,255,255,0.22)_72%,rgba(255,255,255,0.08)_100%),linear-gradient(180deg,rgba(241,245,249,0.92),rgba(226,232,240,0.92))] [background-size:220%_100%,100%_100%] dark:bg-[linear-gradient(90deg,rgba(255,255,255,0.02)_0%,rgba(255,255,255,0.08)_22%,rgba(255,255,255,0.14)_48%,rgba(255,255,255,0.08)_72%,rgba(255,255,255,0.02)_100%),linear-gradient(180deg,rgba(20,28,38,0.96),rgba(13,18,27,0.9))]"
      />
      <div
        v-if="coverFailed"
        class="absolute inset-0 aspect-video bg-[radial-gradient(circle_at_top_left,rgba(116,185,214,0.16),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(255,206,168,0.22),transparent_34%),linear-gradient(180deg,rgba(247,251,255,0.98),rgba(232,241,248,0.96))] dark:bg-[radial-gradient(circle_at_top_left,rgba(121,199,190,0.22),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(74,122,209,0.18),transparent_32%),linear-gradient(180deg,rgba(17,27,38,0.96),rgba(10,17,28,0.98))]"
      />
      <img
        ref="coverImageElement"
        :src="props.video.coverUrl"
        :alt="props.video.title"
        width="320"
        height="180"
        loading="lazy"
        class="aspect-[16/9] w-full object-cover transition duration-300 group-hover:scale-[1.04]"
        :class="coverLoaded && !coverFailed ? 'opacity-100' : 'opacity-0'"
        @load="handleImageLoad"
        @error="handleImageError"
      >
      <div class="absolute inset-x-0 bottom-0 flex justify-end bg-gradient-to-t from-black/72 via-black/18 to-transparent px-2 pb-2 pt-8">
        <span
          v-if="props.video.duration"
          class="rounded-md border border-white/12 bg-black/34 px-1.5 py-0.5 text-[10px] leading-none text-white/88 backdrop-blur-md"
        >
          {{ props.video.duration }}
        </span>
      </div>
    </div>

    <div class="min-w-0 flex-1 self-stretch py-0.5">
      <div class="flex h-full items-start justify-between gap-3">
        <div class="min-w-0">
          <p class="line-clamp-2 text-[13.5px] font-medium leading-5 text-[var(--text-0)] transition group-hover:text-[var(--accent-strong)]">
            {{ props.video.title }}
          </p>
          <p v-if="secondaryText" class="mt-1 line-clamp-2 text-[11px] leading-4.5 text-[var(--text-2)]">
            {{ secondaryText }}
          </p>
        </div>

        <span class="mt-0.5 shrink-0 text-[10px] tracking-[0.16em] text-[var(--text-3)] opacity-0 transition duration-200 group-hover:translate-x-0.5 group-hover:opacity-100">
          NEXT
        </span>
      </div>
    </div>
  </NuxtLink>
</template>
