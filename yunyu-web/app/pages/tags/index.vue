<script setup lang="ts">
import FrontFilterBar from '../../components/content/FrontFilterBar.vue'

/**
 * 前台标签列表页。
 * 作用：集中展示全部标签入口，让用户按更细粒度的主题快速进入内容。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const { data } = await useAsyncData('site-tags', async () => {
  return await siteContent.listTags()
})

const activeKeyword = computed(() => typeof route.query.keyword === 'string' ? route.query.keyword.trim().toLowerCase() : '')
const filteredTags = computed(() => {
  const keyword = activeKeyword.value

  if (!keyword) {
    return data.value || []
  }

  return (data.value || []).filter(tag => {
    return [tag.name, tag.description]
      .filter(Boolean)
      .some(value => value.toLowerCase().includes(keyword))
  })
})

const resultText = computed(() => {
  const total = data.value?.length || 0
  const current = filteredTags.value.length

  if (!activeKeyword.value) {
    return `共 ${total} 个标签入口，适合按更细粒度的话题快速进入内容。`
  }

  return `关键词“${typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''}”命中 ${current} / ${total} 个标签。`
})

watch(() => route.query.keyword, value => {
  searchKeyword.value = typeof value === 'string' ? value : ''
})

useSeoMeta({
  title: '标签 - 云屿',
  description: '浏览云屿的全部标签入口。'
})

/**
 * 执行标签搜索。
 * 作用：把输入中的关键词同步到路由查询参数，让标签筛选结果可被复用和回访。
 */
async function handleSearch() {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/tags',
    query: keyword ? { keyword } : {}
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <FrontFilterBar
        v-model:keyword="searchKeyword"
        eyebrow="标签"
        title="按细分主题快速进入"
        description="统一使用公共筛选条，在大量标签中快速收敛到你真正关心的话题。"
        eyebrow-class="text-emerald-500 dark:text-emerald-300"
        search-placeholder="搜索标签名称或说明"
        :result-text="resultText"
        show-search
        @search="handleSearch"
      />

      <div class="mt-6 flex flex-wrap gap-4">
        <NuxtLink
          v-for="tag in filteredTags"
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
        v-if="!filteredTags.length"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        没有找到匹配的标签，换个关键词试试看。
      </div>
    </section>
  </main>
</template>
