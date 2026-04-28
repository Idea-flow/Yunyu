<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuPoetryTypewriter from '~/components/common/YunyuPoetryTypewriter.vue'

/**
 * 前台专题列表页。
 * 作用：展示全部专题入口，帮助用户按主题组织进入文章内容。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-topics', async () => {
  return await siteContent.listTopics()
})

/**
 * 计算专题列表页首屏主视觉数据。
 * 作用：优先查找首个带封面的专题作为 Hero 背景；若前几个专题没有封面，则继续向后回退，保持与文章列表页和分类页一致的封面取值逻辑。
 */
const heroTopic = computed(() => {
  const list = data.value || []
  const firstCoverTopic = list.find(item => typeof item.coverUrl === 'string' && item.coverUrl.trim())
  return firstCoverTopic || list[0] || null
})
const featuredTopic = computed(() => data.value?.[0] || null)
const secondaryTopics = computed(() => (data.value || []).slice(1))

/**
 * 计算专题页首屏统计信息。
 * 作用：在大图首屏中补充专题总量与当前检索结果，让 Hero 不只是装饰图。
 */
const heroStats = computed(() => {
  const totalTopics = data.value?.length || 0
  const totalArticles = (data.value || []).reduce((sum, item) => sum + (item.articleCount || 0), 0)

  return [
    { label: '专题总数', value: totalTopics },
    { label: '专题分组', value: totalTopics },
    { label: '覆盖文章', value: totalArticles }
  ]
})

useSeoMeta({
  title: '专题 - 云屿',
  description: '浏览云屿的全部专题入口。'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">

    <YunyuHero
      :src="heroTopic?.coverUrl"
      :alt="heroTopic?.name || '云屿专题'"
      min-height-class="h-[min(62svh,32rem)] sm:h-[min(68svh,36rem)] lg:h-[min(74svh,42rem)]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
      content-width-class="max-w-5xl"
    >
      <template #center>
        <YunyuPoetryTypewriter variant="sky" />
      </template>

      <div class="inline-flex items-center rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-semibold uppercase tracking-[0.28em] text-white/90 backdrop-blur-md">
        Topic Archive
      </div>

      <div class="mt-5 flex flex-wrap items-center gap-x-5 gap-y-3 text-white/86">
        <div
          v-for="stat in heroStats"
          :key="stat.label"
          class="flex items-baseline gap-2"
        >
          <p class="text-[0.72rem] font-medium tracking-[0.14em] text-white/58">{{ stat.label }}</p>
          <p class="text-base font-semibold text-white sm:text-lg">{{ stat.value }}</p>
        </div>
      </div>
    </YunyuHero>

    <section class="mx-auto max-w-[1360px] px-5 pb-10 pt-8 sm:px-8 lg:px-10">
      <NuxtLink
        v-if="featuredTopic"
        :to="`/topics/${featuredTopic.slug}`"
        class="group grid gap-6 border-b border-slate-200/75 pb-8 dark:border-white/10 lg:grid-cols-[minmax(0,1.08fr)_320px]"
      >
        <div class="min-w-0">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
            Featured Topic
          </p>
          <h2 class="mt-4 text-[clamp(2rem,1.6rem+1.2vw,2.8rem)] font-semibold leading-[1.04] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
            {{ featuredTopic.name }}
          </h2>
          <p class="mt-4 max-w-3xl text-[1rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ featuredTopic.summary }}
          </p>
          <div class="mt-6 flex items-center gap-3 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
            <span>{{ featuredTopic.articleCount }} 篇文章</span>
            <span>连续主题阅读</span>
          </div>
        </div>

        <YunyuImage
          :src="featuredTopic.coverUrl"
          :alt="featuredTopic.name"
          image-class="h-72 w-full object-cover transition duration-500 group-hover:scale-[1.02] lg:h-full"
          rounded-class="rounded-[28px]"
        />
      </NuxtLink>

      <div class="mt-8 grid gap-x-8 gap-y-6 md:grid-cols-2">
        <NuxtLink
          v-for="topic in secondaryTopics"
          :key="topic.slug"
          :to="`/topics/${topic.slug}`"
          class="group grid gap-4 border-b border-slate-200/75 pb-6 transition hover:border-sky-200 dark:border-white/10 dark:hover:border-sky-900 sm:grid-cols-[180px_minmax(0,1fr)]"
        >
          <YunyuImage
            :src="topic.coverUrl"
            :alt="topic.name"
            image-class="h-44 w-full object-cover transition duration-500 group-hover:scale-[1.02] sm:h-full"
            rounded-class="rounded-[24px]"
          />
          <div class="min-w-0 py-1">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">{{ topic.name }}</h2>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ topic.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ topic.summary }}</p>
          </div>
        </NuxtLink>
      </div>

      <div
        v-if="!(data?.length)"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        暂时还没有可展示的专题内容。
      </div>
    </section>
  </main>
</template>
