<script setup lang="ts">
/**
 * 主题切换组件。
 * 负责在明亮、暗黑、系统三种模式之间切换，
 * 作为前后台统一主题机制的基础交互入口。
 */
const colorMode = useColorMode()

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
  return colorMode.preference === preference
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
 * 获取当前激活主题的配置。
 * 用于在触发按钮上展示当前主题名称和图标。
 */
const activeTheme = computed(() =>
  themeOptions.find(item => item.value === colorMode.preference) ?? themeOptions[2]
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
</script>

<template>
  <UDropdownMenu
    :items="themeMenuItems"
    :content="{ side: 'top', align: 'start', sideOffset: 12 }"
    :ui="{
      content: 'min-w-72 rounded-[22px] border border-slate-200 bg-white/95 p-1 shadow-[0_24px_48px_-28px_rgba(15,23,42,0.3)] backdrop-blur-xl dark:border-slate-700 dark:bg-slate-950/95',
      item: 'rounded-2xl text-slate-600 transition-colors duration-200 data-highlighted:bg-sky-50 data-highlighted:text-slate-900 dark:text-slate-300 dark:data-highlighted:bg-sky-400/15 dark:data-highlighted:text-slate-50',
      itemLabel: 'text-sm font-medium',
      itemDescription: 'text-xs text-slate-400 dark:text-slate-500',
      itemLeadingIcon: 'size-4 text-slate-400 dark:text-slate-500',
      separator: 'bg-slate-200 dark:bg-slate-700'
    }"
  >
    <button
      type="button"
      class="flex min-h-14 w-full cursor-pointer items-center justify-between gap-3 rounded-[20px] border border-slate-200 bg-white/90 px-4 py-3 shadow-[0_14px_30px_-24px_rgba(15,23,42,0.28)] transition duration-200 hover:-translate-y-0.5 hover:border-sky-200 hover:shadow-[0_20px_32px_-24px_rgba(15,23,42,0.34)] focus:outline-none focus:ring-2 focus:ring-sky-400/70 dark:border-slate-700 dark:bg-slate-900/85 dark:hover:border-sky-400/40"
      :aria-label="`当前主题：${activeTheme.label}`"
    >
      <div class="flex min-w-0 items-center gap-3">
        <div class="inline-flex size-9 shrink-0 items-center justify-center rounded-2xl bg-sky-50 text-sky-600 dark:bg-sky-400/12 dark:text-sky-300">
          <UIcon :name="activeTheme.icon" class="size-4" />
        </div>

        <div class="min-w-0 text-left">
          <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ activeTheme.label }}</p>
          <p class="truncate text-xs text-slate-400 dark:text-slate-500">{{ activeTheme.description }}</p>
        </div>
      </div>

      <UIcon name="i-lucide-chevrons-up-down" class="size-4 text-slate-400 dark:text-slate-500" />
    </button>
  </UDropdownMenu>
</template>
