<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

/**
 * 主题切换组件。
 * 作用：以前后台共用的单按钮方式切换明亮、暗黑、系统三种主题模式。
 */
interface ThemeModeSwitchProps {
  /**
   * 组件展示风格。
   * 作用：允许在导航栏中使用纯图标样式，在后台继续复用默认按钮样式。
   */
  variant?: 'default' | 'icon'
}

const colorMode = useColorMode()
const isMounted = ref(false)
const props = withDefaults(defineProps<ThemeModeSwitchProps>(), {
  variant: 'default'
})

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
 * 作用：为单按钮顺序切换提供下一站目标。
 */
const nextTheme = computed(() => {
  const currentIndex = themeOptions.findIndex(item => item.value === currentPreference.value)
  const nextIndex = currentIndex >= themeOptions.length - 1 ? 0 : currentIndex + 1
  return themeOptions[nextIndex]
})

/**
 * 计算按钮样式。
 * 作用：根据不同使用场景切换为默认按钮态或纯图标态，避免导航栏额外出现背景块。
 */
const buttonClassName = computed(() => {
  if (props.variant === 'icon') {
    return 'inline-flex size-10 items-center justify-center text-slate-600 transition duration-200 hover:text-slate-900 focus:outline-none focus:ring-0 dark:text-slate-300 dark:hover:text-slate-50'
  }

  return 'inline-flex size-10 items-center justify-center rounded-[8px] border border-slate-200 bg-white text-slate-600 transition duration-200 hover:border-slate-300 hover:text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-400/35 dark:border-slate-700 dark:bg-slate-950/70 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:text-slate-50'
})

/**
 * 顺序切换主题模式。
 * 作用：按照“明亮 -> 暗黑 -> 系统”的顺序循环切换。
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
    :class="buttonClassName"
    :aria-label="`当前主题：${activeTheme.label}，点击切换到${nextTheme.label}`"
    :title="`当前主题：${activeTheme.label}，点击切换到${nextTheme.label}`"
    @click="cycleTheme"
  >
    <UIcon :name="activeTheme.icon" class="size-4" />
  </button>
</template>
