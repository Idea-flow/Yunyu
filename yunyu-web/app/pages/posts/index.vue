<script setup lang="ts">
import FrontFilterBar from '../../components/content/FrontFilterBar.vue'
import FrontPostCard from '../../components/content/FrontPostCard.vue'
import FrontPaginationBar from '../../components/content/FrontPaginationBar.vue'

/**
 * 前台文章列表页。
 * 作用：承接前台公开文章列表浏览、关键词搜索、标签筛选和按页查看能力。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const selectedTagSlug = computed(() => typeof route.query.tagSlug === 'string' ? route.query.tagSlug : '')

const { data: tagOptions } = await useAsyncData('site-tags-options', async () => {
  return await siteContent.listTags()
})

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, pending } = await useAsyncData(
  () => `site-posts-${route.fullPath}`,
  async () => {
    return await siteContent.listPosts({
      keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
      tagSlug: selectedTagSlug.value || undefined,
      pageNo: pageNo.value,
      pageSize: 10
    })
  },
  {
    watch: [() => route.fullPath]
  }
)

const postList = computed(() => data.value)
const currentTag = computed(() => tagOptions.value?.find(tag => tag.slug === selectedTagSlug.value) || null)
const resultText = computed(() => {
  if (!postList.value) {
    return currentTag.value?.description || ''
  }

  const conditions: string[] = []

  if (typeof route.query.keyword === 'string' && route.query.keyword.trim()) {
    conditions.push(`关键词“${route.query.keyword.trim()}”`)
  }

  if (currentTag.value) {
    conditions.push(`标签“${currentTag.value.name}”`)
  }

  if (!conditions.length) {
    return `共 ${postList.value.total} 篇公开文章，按发布时间倒序浏览。`
  }

  return `当前按${conditions.join('、')}筛选，共找到 ${postList.value.total} 篇文章。`
})

watch(() => route.query.keyword, value => {
  searchKeyword.value = typeof value === 'string' ? value : ''
})

useSeoMeta({
  title: () => currentTag.value ? `${currentTag.value.name} - 文章列表 - 云屿` : '文章列表 - 云屿',
  description: () => currentTag.value?.description || '浏览云屿前台文章列表与最新内容。'
})

/**
 * 执行搜索。
 * 作用：把当前关键词同步到路由查询参数，触发服务端列表重新加载。
 */
async function handleSearch() {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/posts',
    query: {
      ...(keyword ? { keyword } : {}),
      ...(selectedTagSlug.value ? { tagSlug: selectedTagSlug.value } : {}),
      pageNo: 1
    }
  })
}

/**
 * 切换标签筛选。
 * 作用：把标签筛选同步到路由参数，并让文章列表回到第一页重新查询。
 *
 * @param tagSlug 标签 slug
 */
async function handleTagChange(tagSlug: string) {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/posts',
    query: {
      ...(keyword ? { keyword } : {}),
      ...(tagSlug ? { tagSlug } : {}),
      pageNo: 1
    }
  })
}

/**
 * 切换分页。
 * 作用：更新当前页码并保持现有搜索条件不丢失。
 *
 * @param nextPage 目标页码
 */
async function changePage(nextPage: number) {
  await router.push({
    path: '/posts',
    query: {
      ...(typeof route.query.keyword === 'string' && route.query.keyword ? { keyword: route.query.keyword } : {}),
      ...(selectedTagSlug.value ? { tagSlug: selectedTagSlug.value } : {}),
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <FrontFilterBar
        v-model:keyword="searchKeyword"
        eyebrow="文章列表"
        :title="currentTag ? `${currentTag.name} · 文章列表` : '按更新顺序浏览全部内容'"
        :description="currentTag?.description || '通过关键词和标签快速缩小公开文章范围，统一浏览云屿全部前台内容。'"
        eyebrow-class="text-sky-600 dark:text-sky-300"
        search-placeholder="搜索标题或摘要"
        :pending="pending"
        :result-text="resultText"
        show-search
        @search="handleSearch"
      >
        <template #filters>
          <div class="flex flex-col gap-3 lg:flex-row">
            <USelect
              :model-value="selectedTagSlug || undefined"
              :items="[
                { label: '全部标签', value: '' },
                ...((tagOptions || []).map(tag => ({ label: `${tag.name} (${tag.articleCount})`, value: tag.slug })))
              ]"
              size="xl"
              placeholder="按标签筛选"
              class="w-full lg:max-w-[320px]"
              @update:model-value="value => handleTagChange(String(value || ''))"
            />
          </div>
        </template>
      </FrontFilterBar>

      <div class="mt-6 space-y-4">
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />

        <FrontPostCard
          v-for="post in postList?.list || []"
          :key="post.slug"
          :post="post"
        />

        <div
          v-if="!pending && !(postList?.list?.length)"
          class="rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
        >
          当前筛选条件下暂无文章，换个关键词或标签试试看。
        </div>
      </div>

      <FrontPaginationBar
        v-if="postList"
        class="mt-8"
        :page-no="postList.pageNo"
        :total-pages="postList.totalPages"
        @change="changePage"
      />
    </section>
  </main>
</template>
