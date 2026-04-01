<script setup lang="ts">
import FrontFilterBar from '../../components/content/FrontFilterBar.vue'

/**
 * 前台分类列表页。
 * 作用：集中展示全部分类入口，让用户按内容方向进入阅读。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const { data } = await useAsyncData('site-categories', async () => {
  return await siteContent.listCategories()
})

const activeKeyword = computed(() => typeof route.query.keyword === 'string' ? route.query.keyword.trim().toLowerCase() : '')
const filteredCategories = computed(() => {
  const keyword = activeKeyword.value

  if (!keyword) {
    return data.value || []
  }

  return (data.value || []).filter(category => {
    return [category.name, category.description]
      .filter(Boolean)
      .some(value => value.toLowerCase().includes(keyword))
  })
})

const resultText = computed(() => {
  const total = data.value?.length || 0
  const current = filteredCategories.value.length

  if (!activeKeyword.value) {
    return `共 ${total} 个分类入口，可按内容方向进入阅读。`
  }

  return `关键词“${typeof route.query.keyword === 'string' ? route.query.keyword.trim() : ''}”命中 ${current} / ${total} 个分类。`
})

watch(() => route.query.keyword, value => {
  searchKeyword.value = typeof value === 'string' ? value : ''
})

useSeoMeta({
  title: '分类 - 云屿',
  description: '浏览云屿的全部分类入口。'
})

/**
 * 执行分类搜索。
 * 作用：把输入中的关键词同步到路由查询参数，让筛选状态可以被分享和保留。
 */
async function handleSearch() {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/categories',
    query: keyword ? { keyword } : {}
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <FrontFilterBar
        v-model:keyword="searchKeyword"
        eyebrow="分类"
        title="从内容方向进入阅读"
        description="通过统一筛选条快速检索分类入口，再进入对应内容板块浏览相关文章。"
        eyebrow-class="text-orange-500 dark:text-orange-300"
        search-placeholder="搜索分类名称或说明"
        :result-text="resultText"
        show-search
        @search="handleSearch"
      />

      <div class="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <NuxtLink
          v-for="category in filteredCategories"
          :key="category.slug"
          :to="`/categories/${category.slug}`"
          class="overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900"
        >
          <img :src="category.coverUrl" :alt="category.name" class="h-48 w-full object-cover">
          <div class="p-5">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-lg font-semibold">{{ category.name }}</h2>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ category.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ category.description }}</p>
          </div>
        </NuxtLink>
      </div>

      <div
        v-if="!filteredCategories.length"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        没有找到匹配的分类，换个关键词试试看。
      </div>
    </section>
  </main>
</template>
