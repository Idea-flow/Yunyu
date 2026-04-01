<script setup lang="ts">
/**
 * 前台默认布局。
 * 作用：统一承接前台页面的顶部导航、主题切换和底部品牌信息，减少首页与列表页重复结构。
 */
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '标签', to: '/tags' },
  { label: '专题', to: '/topics' }
]
</script>

<template>
  <div class="min-h-screen">
    <header class="sticky top-0 z-30 bg-transparent">
      <div class="mx-auto max-w-[1360px] px-5 py-4 sm:px-8 lg:px-10">
        <div class="flex items-center justify-between gap-4 rounded-[28px] border border-white/55 bg-[linear-gradient(180deg,rgba(245,248,252,0.54)_0%,rgba(239,246,255,0.42)_100%)] px-4 py-3 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.1)] backdrop-blur-[18px] dark:border-white/8 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.42)_0%,rgba(15,23,42,0.3)_100%)] sm:px-5">
          <div class="flex min-w-0 items-center gap-3 sm:gap-4">
            <NuxtLink to="/" class="flex shrink-0 items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-sm font-semibold text-white shadow-[0_12px_28px_-16px_rgba(56,189,248,0.7)]">
                云
              </div>
              <div class="min-w-0">
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">云屿</p>
                <p class="text-xs text-slate-500 dark:text-slate-400">Yunyu</p>
              </div>
            </NuxtLink>

            <div class="hidden h-8 w-px bg-slate-200/60 dark:bg-white/10 md:block" />

            <nav class="hidden items-center gap-1 md:flex">
              <NuxtLink
                v-for="item in navigationItems"
                :key="item.to"
                :to="item.to"
                class="rounded-full px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-white/42 hover:text-sky-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-sky-200"
                active-class="bg-white/58 text-sky-700 shadow-[0_8px_20px_-18px_rgba(14,165,233,0.26)] dark:bg-white/10 dark:text-sky-200"
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
