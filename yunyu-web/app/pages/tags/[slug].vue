<script setup lang="ts">
import FrontPaginationBar from '../../components/content/FrontPaginationBar.vue'
import FrontPostCard from '../../components/content/FrontPostCard.vue'

/**
 * 前台标签详情页。
 * 作用：展示单个标签说明和该标签下的文章列表。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, error } = await useAsyncData(
  () => `site-tag-${route.fullPath}`,
  async () => {
    return await siteContent.getTagDetail(String(route.params.slug || ''), {
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
    statusMessage: error.value.message || '标签不存在'
  })
}

useSeoMeta({
  title: () => `${data.value?.tag.name || '标签'} - 云屿`,
  description: () => data.value?.tag.description || '云屿标签页'
})

/**
 * 切换分页。
 * 作用：更新当前页码，让标签文章列表继续按真实接口翻页。
 *
 * @param nextPage 目标页码
 */
async function changePage(nextPage: number) {
  await router.push({
    path: `/tags/${route.params.slug}`,
    query: {
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main v-if="data" class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="rounded-[32px] border border-white/60 bg-white/82 p-6 shadow-[0_26px_70px_-46px_rgba(15,23,42,0.35)] dark:border-white/10 dark:bg-slate-950/72 sm:p-8">
        <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-emerald-500 dark:text-emerald-300">标签页</p>
        <h1 class="mt-3 text-[clamp(2.05rem,1.55rem+1.35vw,3rem)] font-semibold leading-[1.02] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">{{ data.tag.name }}</h1>
        <p class="mt-4 max-w-2xl text-[clamp(1rem,0.95rem+0.16vw,1.08rem)] leading-[1.95] tracking-[-0.01em] text-slate-600 dark:text-slate-300">
          {{ data.tag.description || '从这个标签进入，查看相同主题标签下的全部公开文章。' }}
        </p>
        <p class="mt-5 text-[0.82rem] uppercase tracking-[0.14em] text-slate-500 dark:text-slate-400">当前共有 {{ data.tag.articleCount }} 篇文章</p>
      </div>

      <div class="mt-6 space-y-4">
        <FrontPostCard
          v-for="post in data.posts.list"
          :key="post.slug"
          :post="post"
        />
      </div>

      <FrontPaginationBar
        class="mt-8"
        :page-no="data.posts.pageNo"
        :total-pages="data.posts.totalPages"
        @change="changePage"
      />
    </section>
  </main>
</template>
