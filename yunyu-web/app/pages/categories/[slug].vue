<script setup lang="ts">
/**
 * 前台分类详情页。
 * 作用：展示单个分类的说明和该分类下的文章列表。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, error } = await useAsyncData(
  () => `site-category-${route.fullPath}`,
  async () => {
    return await siteContent.getCategoryDetail(String(route.params.slug || ''), {
      pageNo: pageNo.value,
      pageSize: 10
    })
  },
  {
    watch: [() => route.fullPath]
  }
)

if (error.value) {
  throw createError({
    statusCode: 404,
    statusMessage: error.value.message || '分类不存在'
  })
}

useSeoMeta({
  title: () => `${data.value?.category.name || '分类'} - 云屿`,
  description: () => data.value?.category.description || '云屿分类页'
})

/**
 * 切换分页。
 * 作用：更新当前页码，让分类文章列表继续按真实接口翻页。
 *
 * @param nextPage 目标页码
 */
async function changePage(nextPage: number) {
  await router.push({
    path: `/categories/${route.params.slug}`,
    query: {
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main v-if="data" class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="overflow-hidden rounded-[32px] border border-white/60 bg-white/82 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 md:grid md:grid-cols-[minmax(0,1fr)_360px]">
        <div class="p-6 sm:p-8">
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-orange-500 dark:text-orange-300">分类页</p>
          <h1 class="mt-3 text-3xl font-semibold">{{ data.category.name }}</h1>
          <p class="mt-4 max-w-2xl text-sm leading-7 text-slate-600 dark:text-slate-300">{{ data.category.description }}</p>
          <p class="mt-5 text-sm text-slate-500 dark:text-slate-400">当前共有 {{ data.category.articleCount }} 篇文章</p>
        </div>
        <img :src="data.category.coverUrl" :alt="data.category.name" class="h-full w-full object-cover">
      </div>

      <div class="mt-6 space-y-4">
        <NuxtLink
          v-for="post in data.posts.list"
          :key="post.slug"
          :to="`/posts/${post.slug}`"
          class="grid gap-4 rounded-[28px] border border-white/60 bg-white/82 p-4 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] transition hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/68 dark:hover:border-sky-900 sm:grid-cols-[240px_minmax(0,1fr)]"
        >
          <img :src="post.coverUrl" :alt="post.title" class="h-52 w-full rounded-[22px] object-cover sm:h-full">
          <div class="min-w-0 py-1">
            <h2 class="text-2xl font-semibold leading-9">{{ post.title }}</h2>
            <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ post.summary }}</p>
            <div class="mt-5 flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-500 dark:text-slate-400">
              <span>{{ post.authorName }}</span>
              <span>{{ post.publishedAt }}</span>
              <span>{{ post.readingMinutes }} 分钟阅读</span>
            </div>
          </div>
        </NuxtLink>
      </div>

      <div v-if="data.posts.totalPages > 1" class="mt-8 flex items-center justify-center gap-3">
        <UButton
          variant="outline"
          color="neutral"
          :disabled="data.posts.pageNo <= 1"
          @click="changePage(data.posts.pageNo - 1)"
        >
          上一页
        </UButton>
        <span class="text-sm text-slate-500 dark:text-slate-400">
          第 {{ data.posts.pageNo }} / {{ data.posts.totalPages }} 页
        </span>
        <UButton
          variant="outline"
          color="neutral"
          :disabled="data.posts.pageNo >= data.posts.totalPages"
          @click="changePage(data.posts.pageNo + 1)"
        >
          下一页
        </UButton>
      </div>
    </section>
  </main>
</template>
