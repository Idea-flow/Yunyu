<script setup lang="ts">
/**
 * 前台文章列表页。
 * 作用：承接前台公开文章列表浏览、关键词搜索和按页查看能力。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, pending } = await useAsyncData(
  () => `site-posts-${route.fullPath}`,
  async () => {
    return await siteContent.listPosts({
      keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
      pageNo: pageNo.value,
      pageSize: 10
    })
  },
  {
    watch: [() => route.fullPath]
  }
)

const postList = computed(() => data.value)

useSeoMeta({
  title: '文章列表 - 云屿',
  description: '浏览云屿前台文章列表与最新内容。'
})

/**
 * 执行搜索。
 * 作用：把当前关键词同步到路由查询参数，触发服务端列表重新加载。
 */
async function handleSearch() {
  await router.push({
    path: '/posts',
    query: {
      ...(searchKeyword.value ? { keyword: searchKeyword.value } : {}),
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
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="rounded-[32px] border border-white/60 bg-white/82 p-6 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 sm:p-8">
        <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">文章列表</p>
        <h1 class="mt-3 text-3xl font-semibold">按更新顺序浏览全部内容</h1>
        <p class="mt-4 max-w-2xl text-sm leading-7 text-slate-600 dark:text-slate-300">
          这里已经切到真实公开接口，后续首页、专题、分类页也都会共用同一套前台内容查询能力。
        </p>

        <div class="mt-6 flex flex-col gap-3 sm:flex-row">
          <UInput
            v-model="searchKeyword"
            size="xl"
            placeholder="搜索标题或摘要"
            class="flex-1"
            @keyup.enter="handleSearch"
          />
          <UButton color="primary" size="xl" icon="i-lucide-search" @click="handleSearch">
            搜索
          </UButton>
        </div>
      </div>

      <div class="mt-6 space-y-4">
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />

        <NuxtLink
          v-for="post in postList?.list || []"
          :key="post.slug"
          :to="`/posts/${post.slug}`"
          class="grid gap-4 rounded-[28px] border border-white/60 bg-white/82 p-4 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900 sm:grid-cols-[240px_minmax(0,1fr)]"
        >
          <img :src="post.coverUrl" :alt="post.title" class="h-52 w-full rounded-[22px] object-cover sm:h-full">
          <div class="min-w-0 py-1">
            <div class="flex flex-wrap gap-2">
              <UBadge color="neutral" variant="soft">{{ post.categoryName }}</UBadge>
              <UBadge v-for="topicName in post.topicNames.slice(0, 2)" :key="`${post.slug}-${topicName}`" color="primary" variant="soft">
                {{ topicName }}
              </UBadge>
            </div>
            <h2 class="mt-4 text-2xl font-semibold leading-9">{{ post.title }}</h2>
            <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
              {{ post.summary }}
            </p>
            <div class="mt-5 flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-500 dark:text-slate-400">
              <span>{{ post.authorName }}</span>
              <span>{{ post.publishedAt }}</span>
              <span>{{ post.readingMinutes }} 分钟阅读</span>
              <span>{{ post.viewCount }} 浏览</span>
            </div>
          </div>
        </NuxtLink>
      </div>

      <div v-if="postList && postList.totalPages > 1" class="mt-8 flex items-center justify-center gap-3">
        <UButton
          variant="outline"
          color="neutral"
          :disabled="postList.pageNo <= 1"
          @click="changePage(postList.pageNo - 1)"
        >
          上一页
        </UButton>
        <span class="text-sm text-slate-500 dark:text-slate-400">
          第 {{ postList.pageNo }} / {{ postList.totalPages }} 页
        </span>
        <UButton
          variant="outline"
          color="neutral"
          :disabled="postList.pageNo >= postList.totalPages"
          @click="changePage(postList.pageNo + 1)"
        >
          下一页
        </UButton>
      </div>
    </section>
  </main>
</template>
