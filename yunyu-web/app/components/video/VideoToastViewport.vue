<script setup lang="ts">
import { useVideoToast } from './composables/useVideoToast'
import VideoIcon from './icons/VideoIcon.vue'

/**
 * 这个组件负责承载播放器目录内部专用 toast 视图，
 * 让播放反馈在不依赖外部通知组件库时仍可见、可关闭。
 */
const videoToast = useVideoToast()

/**
 * 根据 toast 语义返回对应图标，帮助用户快速识别反馈类型。
 */
function getToastIconName(tone: 'info' | 'success' | 'warning' | 'error') {
  if (tone === 'success') {
    return 'check'
  }

  if (tone === 'warning') {
    return 'skip-forward'
  }

  if (tone === 'error') {
    return 'close'
  }

  return 'loader'
}

/**
 * 根据 toast 语义返回配色类名，保持不同反馈的视觉层级清晰。
 */
function getToastToneClass(tone: 'info' | 'success' | 'warning' | 'error') {
  if (tone === 'success') {
    return 'border-emerald-300/22 bg-[rgb(8_23_18_/_0.88)] text-emerald-50'
  }

  if (tone === 'warning') {
    return 'border-amber-300/22 bg-[rgb(28_21_9_/_0.9)] text-amber-50'
  }

  if (tone === 'error') {
    return 'border-rose-300/22 bg-[rgb(30_12_16_/_0.9)] text-rose-50'
  }

  return 'border-white/12 bg-[rgb(13_17_24_/_0.88)] text-[var(--text-0)]'
}
</script>

<template>
  <div class="pointer-events-none fixed inset-x-0 top-4 z-[90] flex justify-center px-4">
    <TransitionGroup
      tag="div"
      enter-active-class="transition duration-220 ease-out"
      enter-from-class="translate-y-2 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-180 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="-translate-y-2 opacity-0"
      class="flex w-full max-w-md flex-col gap-2"
    >
      <div
        v-for="item in videoToast.items.value"
        :key="item.id"
        class="pointer-events-auto rounded-[1rem] border px-3.5 py-3 shadow-[0_16px_34px_rgba(0,0,0,0.2)] backdrop-blur-xl"
        :class="getToastToneClass(item.tone || 'info')"
      >
        <div class="flex items-start gap-3">
          <span class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full border border-current/12 bg-white/8 sm:size-[2.1rem]">
            <VideoIcon :name="getToastIconName(item.tone || 'info')" size="compact" :class="item.tone === 'info' ? 'animate-spin' : ''" />
          </span>

          <div class="min-w-0 flex-1">
            <p class="text-sm font-medium leading-5">{{ item.title }}</p>
            <p v-if="item.description" class="mt-1 text-xs leading-5 text-current/78">{{ item.description }}</p>
          </div>

          <button
            type="button"
            class="flex size-8 shrink-0 items-center justify-center rounded-full border border-current/12 bg-white/8 text-current/82 transition hover:bg-white/12 sm:size-[2.1rem]"
            aria-label="关闭提示"
            @click="videoToast.remove(item.id)"
          >
            <VideoIcon name="close" size="compact" />
          </button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>
