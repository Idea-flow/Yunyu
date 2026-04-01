<script setup lang="ts">
/**
 * 前台专题列表页。
 * 作用：展示全部专题入口，帮助用户按主题组织进入文章内容。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-topics', async () => {
  return await siteContent.listTopics()
})

useSeoMeta({
  title: '专题 - 云屿',
  description: '浏览云屿的全部专题入口。'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="rounded-[32px] border border-white/60 bg-white/82 p-6 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 sm:p-8">
        <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">专题</p>
        <h1 class="mt-3 text-3xl font-semibold">按主题连续阅读</h1>
      </div>

      <div class="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <NuxtLink
          v-for="topic in data || []"
          :key="topic.slug"
          :to="`/topics/${topic.slug}`"
          class="overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900"
        >
          <img :src="topic.coverUrl" :alt="topic.name" class="h-52 w-full object-cover">
          <div class="p-5">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-lg font-semibold">{{ topic.name }}</h2>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ topic.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ topic.summary }}</p>
          </div>
        </NuxtLink>
      </div>
    </section>
  </main>
</template>
