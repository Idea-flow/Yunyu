<script setup lang="ts">
/**
 * 前台默认布局。
 * 作用：统一承接前台页面的顶部导航、主题切换和底部品牌信息，减少首页与列表页重复结构。
 */
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '专题', to: '/topics' }
]
</script>

<template>
  <div class="min-h-screen">
    <header class="sticky top-0 z-30 border-b border-white/60 bg-white/72 backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/70">
      <div class="mx-auto flex max-w-[1360px] items-center justify-between gap-5 px-5 py-4 sm:px-8 lg:px-10">
        <NuxtLink to="/" class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-sm font-semibold text-white shadow-[0_12px_28px_-16px_rgba(56,189,248,0.7)]">
            云
          </div>
          <div>
            <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">云屿</p>
            <p class="text-xs text-slate-500 dark:text-slate-400">Yunyu</p>
          </div>
        </NuxtLink>

        <nav class="hidden items-center gap-2 md:flex">
          <NuxtLink
            v-for="item in navigationItems"
            :key="item.to"
            :to="item.to"
            class="rounded-full px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-sky-50 hover:text-sky-700 dark:text-slate-300 dark:hover:bg-slate-900 dark:hover:text-sky-200"
            active-class="bg-sky-50 text-sky-700 dark:bg-slate-900 dark:text-sky-200"
          >
            {{ item.label }}
          </NuxtLink>
        </nav>

        <ThemeModeSwitch />
      </div>
    </header>

    <slot />

    <footer class="border-t border-white/60 bg-white/72 dark:border-white/10 dark:bg-slate-950/70">
      <div class="mx-auto flex max-w-[1360px] flex-col gap-3 px-5 py-8 text-sm text-slate-500 sm:px-8 lg:px-10 dark:text-slate-400 md:flex-row md:items-center md:justify-between">
        <p>云屿 Yunyu · 把二次元内容整理成更适合阅读的节奏。</p>
        <div class="flex items-center gap-4">
          <NuxtLink to="/posts" class="hover:text-slate-900 dark:hover:text-slate-50">文章</NuxtLink>
          <NuxtLink to="/categories" class="hover:text-slate-900 dark:hover:text-slate-50">分类</NuxtLink>
          <NuxtLink to="/topics" class="hover:text-slate-900 dark:hover:text-slate-50">专题</NuxtLink>
        </div>
      </div>
    </footer>
  </div>
</template>
