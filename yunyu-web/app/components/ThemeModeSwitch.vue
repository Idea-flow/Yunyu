<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

/**
 * 主题切换组件。
 * 负责在明亮、暗黑、系统三种模式之间切换，
 * 作为前后台统一主题机制的基础交互入口。
 */
const colorMode = useColorMode()
const isMounted = ref(false)

/**
 * 设置主题偏好。
 * 统一通过 color-mode 管理主题状态，避免组件各自维护主题逻辑。
 */
function setTheme(preference: 'light' | 'dark' | 'system') {
  colorMode.preference = preference
}

/**
 * 判断当前按钮是否为激活状态。
 * 用于在界面上直观表达用户当前选择的主题模式。
 */
function isActive(preference: 'light' | 'dark' | 'system') {
  return currentPreference.value === preference
}

const themeOptions = [
  {
    label: '明亮',
    description: '适合白天和高亮环境',
    value: 'light' as const,
    icon: 'i-lucide-sun-medium'
  },
  {
    label: '暗黑',
    description: '适合夜间和低刺激阅读',
    value: 'dark' as const,
    icon: 'i-lucide-moon-star'
  },
  {
    label: '系统',
    description: '跟随设备当前主题',
    value: 'system' as const,
    icon: 'i-lucide-laptop-minimal'
  }
]

/**
 * 获取当前用于界面展示的主题偏好。
 * 作用：在 SSR 和客户端首帧阶段统一按 `system` 渲染，避免 hydration 时因真实主题已切换而产生不一致。
 */
const currentPreference = computed<'light' | 'dark' | 'system'>(() => {
  if (!isMounted.value) {
    return 'system'
  }

  return colorMode.preference as 'light' | 'dark' | 'system'
})

/**
 * 获取当前激活主题的配置。
 * 用于在触发按钮上展示当前主题名称和图标。
 */
const activeTheme = computed(() =>
  themeOptions.find(item => item.value === currentPreference.value) ?? themeOptions[2]
)

/**
 * 构建主题菜单项。
 * 将主题切换逻辑统一收敛到下拉菜单中，便于后续扩展更多主题方案。
 */
const themeMenuItems = computed(() => [
  themeOptions.map(item => ({
    label: item.label,
    description: item.description,
    icon: item.icon,
    color: isActive(item.value) ? 'primary' as const : 'neutral' as const,
    onSelect: () => setTheme(item.value)
  }))
])

onMounted(() => {
  isMounted.value = true
})
</script>

<template>
  <UDropdownMenu
    :items="themeMenuItems"
    :content="{ side: 'bottom', align: 'end', sideOffset: 12 }"
    :ui="{
      content: 'min-w-64 rounded-[22px] border border-slate-200 bg-white/95 p-1 shadow-[0_24px_48px_-28px_rgba(15,23,42,0.3)] backdrop-blur-xl dark:border-slate-700 dark:bg-slate-950/95',
      item: 'rounded-2xl text-slate-600 transition-colors duration-200 data-highlighted:bg-sky-50 data-highlighted:text-slate-900 dark:text-slate-300 dark:data-highlighted:bg-sky-400/15 dark:data-highlighted:text-slate-50',
      itemLabel: 'text-sm font-medium',
      itemDescription: 'text-xs text-slate-400 dark:text-slate-500',
      itemLeadingIcon: 'size-4 text-slate-400 dark:text-slate-500',
      separator: 'bg-slate-200 dark:bg-slate-700'
    }"
  >
    <button
      type="button"
      class="inline-flex size-11 cursor-pointer items-center justify-center rounded-2xl border border-slate-200/80 bg-white/86 text-slate-600 shadow-[0_14px_30px_-24px_rgba(15,23,42,0.22)] transition duration-200 hover:-translate-y-0.5 hover:border-sky-200 hover:text-sky-700 hover:shadow-[0_18px_32px_-24px_rgba(15,23,42,0.3)] focus:outline-none focus:ring-2 focus:ring-sky-400/70 dark:border-slate-700 dark:bg-slate-900/82 dark:text-slate-300 dark:hover:border-sky-400/40 dark:hover:text-sky-200"
      :aria-label="`当前主题：${activeTheme.label}`"
      :title="`当前主题：${activeTheme.label}`"
    >
      <UIcon :name="activeTheme.icon" class="size-5" />
    </button>
  </UDropdownMenu>
</template>
