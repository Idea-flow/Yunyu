<script setup lang="ts">
/**
 * 前台标签列表页。
 * 作用：直接展示全部标签入口，让用户按更细粒度的主题快速进入内容，不再额外增加搜索干扰。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-tags', async () => {
  return await siteContent.listTags()
})

const tags = computed(() => data.value || [])
const tagsCountText = computed(() => `共 ${tags.value.length} 个标签入口，适合按更细粒度的话题直接进入内容。`)

useSeoMeta({
  title: '标签 - 云屿',
  description: '浏览云屿的全部标签入口。'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="max-w-3xl">
        <p class="text-[0.72rem] font-semibold uppercase tracking-[0.32em] text-emerald-500 dark:text-emerald-300">
          标签
        </p>
        <h1 class="mt-4 text-[clamp(2.1rem,1.7rem+1.4vw,3rem)] font-semibold leading-[1.04] tracking-[-0.045em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
          按细分主题快速进入
        </h1>
        <p class="mt-4 max-w-3xl text-[1rem] leading-8 text-slate-600 dark:text-slate-300">
          标签数量不多时，完整展示更直观，也更适合一眼扫过后直接进入感兴趣的话题。
        </p>
        <p class="mt-5 text-sm text-slate-500 dark:text-slate-400">
          {{ tagsCountText }}
        </p>
      </div>

      <div class="mt-6 flex flex-wrap gap-4">
        <NuxtLink
          v-for="tag in tags"
          :key="tag.slug"
          :to="`/tags/${tag.slug}`"
          class="min-w-[220px] rounded-[26px] border border-white/60 bg-white/82 px-5 py-4 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900"
        >
          <div class="flex items-center justify-between gap-3">
            <h2 class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">{{ tag.name }}</h2>
            <span class="text-xs text-slate-500 dark:text-slate-400">{{ tag.articleCount }} 篇</span>
          </div>
          <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
            {{ tag.description || '从这个标签进入，查看相关主题下的全部文章。' }}
          </p>
        </NuxtLink>
      </div>

      <div
        v-if="!tags.length"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        暂时还没有可展示的标签。
      </div>
    </section>
  </main>
</template>
