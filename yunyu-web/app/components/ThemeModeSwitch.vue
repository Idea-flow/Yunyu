<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

/**
 * 主题切换组件。
 * 作用：以单按钮顺序切换的方式承接明亮、暗黑、系统三种主题模式，
 * 让导航栏中的主题入口保持更简洁的占位和更轻的视觉干扰。
 */
const colorMode = useColorMode()
const isMounted = ref(false)

const themeOptions = [
  {
    label: '明亮',
    value: 'light' as const,
    icon: 'i-lucide-sun-medium'
  },
  {
    label: '暗黑',
    value: 'dark' as const,
    icon: 'i-lucide-moon-star'
  },
  {
    label: '系统',
    value: 'system' as const,
    icon: 'i-lucide-laptop-minimal'
  }
] as const

/**
 * 获取当前用于界面展示的主题偏好。
 * 作用：在 SSR 与客户端首帧阶段统一按 `system` 渲染，避免 hydration 不一致。
 */
const currentPreference = computed<'light' | 'dark' | 'system'>(() => {
  if (!isMounted.value) {
    return 'system'
  }

  return colorMode.preference as 'light' | 'dark' | 'system'
})

/**
 * 获取当前激活主题配置。
 * 作用：让按钮可以同步展示当前主题的图标和提示文案。
 */
const activeTheme = computed(() =>
  themeOptions.find(item => item.value === currentPreference.value) ?? themeOptions[2]
)

/**
 * 获取下一个主题配置。
 * 作用：为顺序切换提供明确的下一站提示，减少用户猜测。
 */
const nextTheme = computed(() => {
  const currentIndex = themeOptions.findIndex(item => item.value === currentPreference.value)
  const nextIndex = currentIndex >= themeOptions.length - 1 ? 0 : currentIndex + 1
  return themeOptions[nextIndex]
})

/**
 * 顺序切换主题模式。
 * 作用：按照“明亮 -> 暗黑 -> 系统”的顺序循环切换，保持操作简单直接。
 */
function cycleTheme() {
  colorMode.preference = nextTheme.value.value
}

onMounted(() => {
  isMounted.value = true
})
</script>

<template>
  <button
    type="button"
    class="inline-flex h-11 min-w-11 cursor-pointer items-center justify-center rounded-full px-2 text-slate-600 transition duration-200 hover:text-sky-700 focus:outline-none focus:ring-2 focus:ring-sky-400/35 dark:text-slate-300 dark:hover:text-sky-200"
    :aria-label="`当前主题：${activeTheme.label}，点击切换到${nextTheme.label}`"
    :title="`当前主题：${activeTheme.label}，点击切换到${nextTheme.label}`"
    @click="cycleTheme"
  >
    <UIcon :name="activeTheme.icon" class="size-5" />
  </button>
</template>
