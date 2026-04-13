<script setup lang="ts">
import { buildVideoPath } from '~/utils/seo'

/**
 * 前台视频卡片组件。
 * 负责统一展示视频封面、标题、时长与标签入口，
 * 作为首页、标签页与相关推荐列表的核心卡片样式。
 */
interface TagItem {
  id: number
  name: string
  slug: string
}

const coverLoaded = ref(false)
const coverFailed = ref(false)
const coverImageElement = ref<HTMLImageElement | null>(null)

defineProps<{
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
 * 获取封面左下角优先展示的标签文案。
 * 优先展示第一个标签，若当前视频没有标签则回退为 ASMR。
 */
function getPrimaryTagLabel(tags: TagItem[]) {
  return tags[0]?.name || 'ASMR'
}

/**
 * 标记封面图已经完成加载，用于切换骨架占位和图片显隐。
 */
function handleImageLoad() {
  coverLoaded.value = true
  coverFailed.value = false
}

/**
 * 标记封面图加载失败，切换到站点统一的渐变封面兜底样式。
 */
function handleImageError() {
  coverLoaded.value = true
  coverFailed.value = true
}

/**
 * 在客户端挂载后兜底检查封面图当前状态。
 * 避免 SSR 首屏阶段图片已命中缓存但 load 事件早于 Vue 监听绑定，导致图片始终保持透明。
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
  <article class="group grid gap-2.5 lg:gap-2">
    <NuxtLink :to="buildVideoPath(video)" :prefetch="false" class="grid gap-2.5 lg:gap-2">
      <div class="relative overflow-hidden rounded-[1.5rem] border border-[var(--line-soft)] bg-[linear-gradient(180deg,color-mix(in_srgb,var(--panel-strong)_92%,transparent),color-mix(in_srgb,var(--panel-bg)_94%,transparent))] shadow-[var(--shadow-panel)] backdrop-blur-xl transition duration-300 group-hover:border-[color:var(--accent-ring)] group-hover:shadow-[var(--shadow-soft)]">
        <div
          v-if="!coverLoaded"
          class="absolute inset-0 animate-[shimmerSlide_1.35s_linear_infinite] bg-[linear-gradient(90deg,rgba(255,255,255,0.02)_0%,rgba(255,255,255,0.08)_22%,rgba(255,255,255,0.14)_48%,rgba(255,255,255,0.08)_72%,rgba(255,255,255,0.02)_100%),linear-gradient(180deg,rgba(20,28,38,0.96),rgba(13,18,27,0.9))] [background-size:220%_100%,100%_100%]"
        />
        <div
          v-if="coverFailed"
          class="absolute inset-0 aspect-video bg-[radial-gradient(circle_at_top_left,rgba(116,185,214,0.16),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(255,206,168,0.22),transparent_34%),linear-gradient(180deg,rgba(247,251,255,0.98),rgba(232,241,248,0.96))] dark:bg-[radial-gradient(circle_at_top_left,rgba(121,199,190,0.22),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(74,122,209,0.18),transparent_32%),linear-gradient(180deg,rgba(17,27,38,0.96),rgba(10,17,28,0.98))]"
        >
          <div class="absolute inset-x-0 top-0 h-24 bg-[linear-gradient(180deg,rgba(255,255,255,0.52),transparent)] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.06),transparent)]" />
          <div class="absolute -left-8 bottom-6 h-24 w-24 rounded-full bg-[rgba(130,202,215,0.18)] blur-2xl dark:bg-[rgba(121,199,190,0.1)]" />
          <div class="absolute right-4 top-5 h-16 w-16 rounded-full bg-[rgba(255,216,180,0.22)] blur-2xl dark:bg-[rgba(168,188,255,0.08)]" />
          <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.02),rgba(255,255,255,0)_42%,rgba(21,33,52,0.12))] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.02),rgba(255,255,255,0)_42%,rgba(4,8,16,0.3))]" />
        </div>
        <img
          ref="coverImageElement"
          :src="video.coverUrl"
          :alt="video.title"
          width="1280"
          height="720"
          loading="lazy"
          class="block aspect-video h-full w-full object-cover transition duration-500 group-hover:scale-[1.02] group-hover:brightness-[1.04]"
          :class="coverLoaded && !coverFailed ? 'opacity-100' : 'opacity-0'"
          @load="handleImageLoad"
          @error="handleImageError"
        />
        <div class="absolute inset-x-0 bottom-0 flex items-end justify-between gap-3 bg-gradient-to-t from-black/70 via-black/15 to-transparent px-4 pb-4 pt-12">
          <span class="inline-flex max-w-[8.5rem] items-center truncate rounded-full border border-white/15 bg-black/30 px-3 py-1 text-xs text-white/85 backdrop-blur-md">
            {{ getPrimaryTagLabel(video.tags) }}
          </span>
          <span v-if="video.duration" class="inline-flex items-center rounded-full border border-white/15 bg-black/30 px-3 py-1 text-xs text-white/85 backdrop-blur-md">
            {{ video.duration }}
          </span>
        </div>
      </div>

      <div class="space-y-1.5 px-1">
        <h3 class="line-clamp-2 min-w-0 text-[1.04rem] font-medium leading-6 text-[var(--text-0)] transition group-hover:text-[var(--accent-strong)] lg:text-[1.14rem] lg:leading-7 2xl:text-[1.18rem]">
          {{ video.title }}
        </h3>
        <p v-if="video.summary" class="line-clamp-2 text-sm leading-6 text-[var(--text-2)]">
          {{ video.summary }}
        </p>
      </div>
    </NuxtLink>
  </article>
</template>
