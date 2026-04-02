<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 前台默认布局。
 * 作用：统一承接前台页面的顶部导航、主题切换和底部品牌信息，减少首页与列表页重复结构。
 */
const route = useRoute()
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '标签', to: '/tags' },
  { label: '专题', to: '/topics' }
]

const isScrolled = ref(false)
const isPostDetailPage = computed(() => route.path.startsWith('/posts/'))

/**
 * 判断当前页面是否需要导航栏覆盖首屏区域。
 * 作用：让首页与文章详情页的导航栏直接叠在首屏大图之上，避免导航区和首屏内容被硬切开。
 */
const isOverlayPage = computed(() => {
  return route.path === '/'
    || route.path === '/topics'
    || route.path.startsWith('/posts/')
    || route.path.startsWith('/topics/')
    || route.path.startsWith('/categories/')
})

/**
 * 判断导航栏当前是否应进入实底玻璃态。
 * 作用：在覆盖页首屏顶部保持更轻的覆盖状态，滚动后再切换为可读性更强的玻璃容器。
 */
const isSolidNav = computed(() => !isOverlayPage.value || isScrolled.value)

/**
 * 计算头部容器定位样式。
 * 作用：覆盖页使用 fixed 覆盖在首屏上方，普通页面继续使用 sticky 导航。
 */
const headerClassName = computed(() => {
  return isOverlayPage.value
    ? 'fixed inset-x-0 top-0 z-40'
    : 'sticky top-0 z-30 bg-transparent'
})

/**
 * 计算导航面板样式。
 * 作用：统一控制覆盖态与滚动态下导航条的背景、边框和阴影层级。
 */
const navPanelClassName = computed(() => {
  if (!isSolidNav.value) {
    if (isPostDetailPage.value) {
      return 'border border-white/10 bg-[linear-gradient(180deg,rgba(15,23,42,0.18)_0%,rgba(15,23,42,0.08)_100%)] shadow-[0_12px_34px_-28px_rgba(15,23,42,0.2)] backdrop-blur-[10px]'
    }

    return 'border-white/12 bg-[linear-gradient(180deg,rgba(15,23,42,0.14)_0%,rgba(15,23,42,0.1)_100%)] shadow-none backdrop-blur-[14px]'
  }

  return 'border border-white/55 bg-[linear-gradient(180deg,rgba(245,248,252,0.54)_0%,rgba(239,246,255,0.42)_100%)] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.1)] backdrop-blur-[18px] dark:border-white/8 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.42)_0%,rgba(15,23,42,0.3)_100%)]'
})

/**
 * 计算品牌标题文字样式。
 * 作用：覆盖页在首屏顶部默认使用浅色文案，滚动后切回常规导航配色。
 */
const brandTitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white'
    : 'text-slate-900 dark:text-slate-50'
})

/**
 * 计算品牌副标题文字样式。
 * 作用：让覆盖态与普通态都保持合适的对比度。
 */
const brandSubtitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white/72'
    : 'text-slate-500 dark:text-slate-400'
})

/**
 * 计算导航链接基础样式。
 * 作用：统一覆盖态与普通态下导航文字的可读性与交互反馈。
 */
const navLinkClassName = computed(() => {
  return !isSolidNav.value
    ? 'rounded-full px-4 py-2 text-sm font-medium text-white/88 transition hover:bg-white/12 hover:text-white'
    : 'rounded-full px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-white/42 hover:text-sky-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-sky-200'
})

/**
 * 计算导航链接激活样式。
 * 作用：让当前页面入口在覆盖态和普通态下都保持明确的激活反馈。
 */
const navLinkActiveClassName = computed(() => {
  return !isSolidNav.value
    ? 'bg-white/14 text-white'
    : 'bg-white/58 text-sky-700 shadow-[0_8px_20px_-18px_rgba(14,165,233,0.26)] dark:bg-white/10 dark:text-sky-200'
})

/**
 * 计算导航外层留白。
 * 作用：文章详情页首屏改为全宽顶图后，导航需要更贴近浏览器顶部，
 * 避免顶部留白破坏整张封面的连续感。
 */
const headerInnerClassName = computed(() => {
  if (isOverlayPage.value && isPostDetailPage.value) {
    return 'mx-auto max-w-[1360px] px-5 pt-2 pb-3 sm:px-8 sm:pt-3 lg:px-10'
  }

  return 'mx-auto max-w-[1360px] px-5 py-4 sm:px-8 lg:px-10'
})

/**
 * 计算导航面板尺寸节奏。
 * 作用：文章详情页在首屏阶段使用更轻更薄的导航条，
 * 让导航像浮在封面上的一层细玻璃，而不是独立的大块容器。
 */
const navPanelLayoutClassName = computed(() => {
  if (isOverlayPage.value && isPostDetailPage.value && !isSolidNav.value) {
    return 'rounded-[24px] px-4 py-2.5 sm:px-5'
  }

  return 'rounded-[28px] px-4 py-3 sm:px-5'
})

/**
 * 同步页面滚动状态。
 * 作用：根据滚动距离切换导航栏是否进入实底玻璃态。
 */
function syncScrollState() {
  if (!import.meta.client) {
    return
  }

  isScrolled.value = window.scrollY > 36
}

onMounted(() => {
  if (!import.meta.client) {
    return
  }

  syncScrollState()
  window.addEventListener('scroll', syncScrollState, { passive: true })
})

watch(() => route.fullPath, () => {
  syncScrollState()
})

onBeforeUnmount(() => {
  if (!import.meta.client) {
    return
  }

  window.removeEventListener('scroll', syncScrollState)
})
</script>

<template>
  <div class="min-h-screen">
    <header :class="headerClassName">
      <div :class="headerInnerClassName">
        <div
          class="flex items-center justify-between gap-4 transition-all duration-300"
          :class="[navPanelClassName, navPanelLayoutClassName]"
        >
          <div class="flex min-w-0 items-center gap-3 sm:gap-4">
            <NuxtLink to="/" class="flex shrink-0 items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-sm font-semibold text-white shadow-[0_12px_28px_-16px_rgba(56,189,248,0.7)]">
                云
              </div>
              <div class="min-w-0">
                <p class="text-[clamp(1.25rem,1.05rem+0.75vw,1.7rem)] font-semibold leading-[0.96] tracking-[-0.05em] [font-family:var(--font-display)]" :class="brandTitleClassName">云屿</p>
                <p class="mt-1 text-[0.66rem] uppercase tracking-[0.14em]" :class="brandSubtitleClassName">Yunyu</p>
              </div>
            </NuxtLink>

            <div
              class="hidden h-8 w-px md:block"
              :class="!isSolidNav ? 'bg-white/14' : 'bg-slate-200/60 dark:bg-white/10'"
            />

            <nav class="hidden items-center gap-1 md:flex">
              <NuxtLink
                v-for="item in navigationItems"
                :key="item.to"
                :to="item.to"
                :class="navLinkClassName"
                :active-class="navLinkActiveClassName"
              >
                {{ item.label }}
              </NuxtLink>
            </nav>
          </div>

          <div class="flex shrink-0 items-center gap-2">
            <ThemeModeSwitch />
          </div>
        </div>
      </div>
    </header>

    <slot />

    <footer class="border-t border-white/60 bg-white/72 dark:border-white/10 dark:bg-slate-950/70">
      <div class="mx-auto flex max-w-[1360px] flex-col gap-3 px-5 py-8 text-sm text-slate-500 sm:px-8 lg:px-10 dark:text-slate-400 md:flex-row md:items-center md:justify-between">
        <p>云屿 Yunyu · 把二次元内容整理成更适合阅读的节奏。</p>
        <div class="flex items-center gap-4">
          <NuxtLink to="/posts" class="hover:text-slate-900 dark:hover:text-slate-50">文章</NuxtLink>
          <NuxtLink to="/categories" class="hover:text-slate-900 dark:hover:text-slate-50">分类</NuxtLink>
          <NuxtLink to="/tags" class="hover:text-slate-900 dark:hover:text-slate-50">标签</NuxtLink>
          <NuxtLink to="/topics" class="hover:text-slate-900 dark:hover:text-slate-50">专题</NuxtLink>
        </div>
      </div>
    </footer>
  </div>
</template>
