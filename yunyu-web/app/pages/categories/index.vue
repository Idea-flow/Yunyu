<script setup lang="ts">
import YunyuImage from '~/components/common/YunyuImage.vue'
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
const featuredCategory = computed(() => filteredCategories.value[0] || null)
const secondaryCategories = computed(() => filteredCategories.value.slice(1))

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

      <NuxtLink
        v-if="featuredCategory"
        :to="`/categories/${featuredCategory.slug}`"
        class="group mt-8 grid gap-6 border-b border-slate-200/75 pb-8 dark:border-white/10 lg:grid-cols-[minmax(0,1.08fr)_320px]"
      >
        <div class="min-w-0">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-orange-500 dark:text-orange-300">
            Featured Category
          </p>
          <h2 class="mt-4 text-[clamp(2rem,1.6rem+1.2vw,2.8rem)] font-semibold leading-[1.04] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-orange-600 dark:text-slate-50 dark:group-hover:text-orange-200">
            {{ featuredCategory.name }}
          </h2>
          <p class="mt-4 max-w-3xl text-[1rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ featuredCategory.description }}
          </p>
          <div class="mt-6 flex items-center gap-3 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
            <span>{{ featuredCategory.articleCount }} 篇文章</span>
            <span>按方向持续阅读</span>
          </div>
        </div>

        <YunyuImage
          :src="featuredCategory.coverUrl"
          :alt="featuredCategory.name"
          image-class="h-72 w-full object-cover transition duration-500 group-hover:scale-[1.02] lg:h-full"
          rounded-class="rounded-[28px]"
        />
      </NuxtLink>

      <div class="mt-8 grid gap-x-8 gap-y-6 md:grid-cols-2">
        <NuxtLink
          v-for="category in secondaryCategories"
          :key="category.slug"
          :to="`/categories/${category.slug}`"
          class="group grid gap-4 border-b border-slate-200/75 pb-6 transition hover:border-orange-200 dark:border-white/10 dark:hover:border-orange-900 sm:grid-cols-[180px_minmax(0,1fr)]"
        >
          <YunyuImage
            :src="category.coverUrl"
            :alt="category.name"
            image-class="h-44 w-full object-cover transition duration-500 group-hover:scale-[1.02] sm:h-full"
            rounded-class="rounded-[24px]"
          />
          <div class="min-w-0 py-1">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-orange-600 dark:text-slate-50 dark:group-hover:text-orange-200">{{ category.name }}</h2>
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
