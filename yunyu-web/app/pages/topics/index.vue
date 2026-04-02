<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import FrontFilterBar from '../../components/content/FrontFilterBar.vue'

/**
 * 前台专题列表页。
 * 作用：展示全部专题入口，帮助用户按主题组织进入文章内容。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const { data } = await useAsyncData('site-topics', async () => {
  return await siteContent.listTopics()
})

/**
 * 计算专题列表页首屏主视觉数据。
 * 作用：优先复用当前专题数据中的首个封面作为大图背景，让专题页也进入统一的 Hero 视觉模式。
 */
const heroTopic = computed(() => data.value?.[0] || null)

const activeKeyword = computed(() => typeof route.query.keyword === 'string' ? route.query.keyword.trim().toLowerCase() : '')
const filteredTopics = computed(() => {
  const keyword = activeKeyword.value

  if (!keyword) {
    return data.value || []
  }

  return (data.value || []).filter(topic => {
    return [topic.name, topic.summary]
      .filter(Boolean)
      .some(value => value.toLowerCase().includes(keyword))
  })
})

const resultText = computed(() => {
  const total = data.value?.length || 0
  const current = filteredTopics.value.length

  if (!activeKeyword.value) {
    return `共 ${total} 个专题入口，适合按主题连续阅读一组相关文章。`
  }

  return `关键词“${typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''}”命中 ${current} / ${total} 个专题。`
})

/**
 * 计算专题页首屏统计信息。
 * 作用：在大图首屏中补充专题总量与当前检索结果，让 Hero 不只是装饰图。
 */
const heroStats = computed(() => {
  const totalTopics = data.value?.length || 0
  const visibleTopics = filteredTopics.value.length
  const totalArticles = (data.value || []).reduce((sum, item) => sum + (item.articleCount || 0), 0)

  return [
    { label: '专题总数', value: totalTopics },
    { label: '当前结果', value: visibleTopics },
    { label: '覆盖文章', value: totalArticles }
  ]
})

watch(() => route.query.keyword, value => {
  searchKeyword.value = typeof value === 'string' ? value : ''
})

useSeoMeta({
  title: '专题 - 云屿',
  description: '浏览云屿的全部专题入口。'
})

/**
 * 执行专题搜索。
 * 作用：把筛选关键词写入路由查询参数，统一专题页的可回放筛选体验。
 */
async function handleSearch() {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/topics',
    query: keyword ? { keyword } : {}
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <YunyuHero
      :src="heroTopic?.coverUrl"
      :alt="heroTopic?.name || '云屿专题'"
      min-height-class="min-h-[48svh] sm:min-h-[56svh] lg:min-h-[62svh]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
      content-width-class="max-w-5xl"
    >
      <div class="inline-flex items-center rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-semibold uppercase tracking-[0.28em] text-white/90 backdrop-blur-md">
        Topic Archive
      </div>

      <h1 class="mt-5 text-3xl font-bold text-white drop-shadow-lg sm:text-4xl lg:text-5xl">
        专题不是标签堆叠，而是成组阅读入口
      </h1>

      <p class="mt-4 max-w-3xl text-sm leading-7 text-white/86 drop-shadow-md sm:text-base sm:leading-8">
        在云屿里，专题页负责把同一主题下的文章串成连续阅读路径，让内容更像一条可以慢慢展开的线。
      </p>

      <div class="mt-6 flex flex-wrap gap-3">
        <div
          v-for="stat in heroStats"
          :key="stat.label"
          class="rounded-2xl border border-white/16 bg-white/10 px-4 py-3 backdrop-blur-md"
        >
          <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-white/62">{{ stat.label }}</p>
          <p class="mt-2 text-lg font-semibold text-white">{{ stat.value }}</p>
        </div>
      </div>
    </YunyuHero>

    <section class="mx-auto max-w-[1360px] px-5 pb-10 pt-8 sm:px-8 lg:px-10">
      <FrontFilterBar
        v-model:keyword="searchKeyword"
        eyebrow="专题"
        title="按主题连续阅读"
        description="通过公共筛选条检索专题入口，更快进入同一主题下的成组文章。"
        eyebrow-class="text-sky-600 dark:text-sky-300"
        search-placeholder="搜索专题名称或摘要"
        :result-text="resultText"
        show-search
        @search="handleSearch"
      />

      <div class="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        <NuxtLink
          v-for="topic in filteredTopics"
          :key="topic.slug"
          :to="`/topics/${topic.slug}`"
          class="overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900"
        >
          <YunyuImage
            :src="topic.coverUrl"
            :alt="topic.name"
            image-class="h-52 w-full"
            rounded-class="rounded-t-[28px] rounded-b-none"
          />
          <div class="p-5">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-lg font-semibold">{{ topic.name }}</h2>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ topic.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ topic.summary }}</p>
          </div>
        </NuxtLink>
      </div>

      <div
        v-if="!filteredTopics.length"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        没有找到匹配的专题，换个关键词试试看。
      </div>
    </section>
  </main>
</template>
