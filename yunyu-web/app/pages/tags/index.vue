<script setup lang="ts">
/**
 * 前台标签列表页。
 * 作用：集中展示全部标签入口，让用户按更细粒度的主题快速进入内容。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-tags', async () => {
  return await siteContent.listTags()
})

useSeoMeta({
  title: '标签 - 云屿',
  description: '浏览云屿的全部标签入口。'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="rounded-[32px] border border-white/60 bg-white/82 p-6 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 sm:p-8">
        <p class="text-xs font-semibold uppercase tracking-[0.34em] text-emerald-500 dark:text-emerald-300">标签</p>
        <h1 class="mt-3 text-3xl font-semibold">按细分主题快速进入</h1>
      </div>

      <div class="mt-6 flex flex-wrap gap-4">
        <NuxtLink
          v-for="tag in data || []"
          :key="tag.slug"
          :to="`/tags/${tag.slug}`"
          class="min-w-[220px] rounded-[26px] border border-white/60 bg-white/82 px-5 py-4 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900"
        >
          <div class="flex items-center justify-between gap-3">
            <h2 class="text-base font-semibold">{{ tag.name }}</h2>
            <span class="text-xs text-slate-500 dark:text-slate-400">{{ tag.articleCount }} 篇</span>
          </div>
          <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
            {{ tag.description || '从这个标签进入，查看相关主题下的全部文章。' }}
          </p>
        </NuxtLink>
      </div>
    </section>
  </main>
</template>
