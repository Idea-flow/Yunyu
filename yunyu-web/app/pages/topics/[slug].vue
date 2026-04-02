<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'
import FrontPaginationBar from '../../components/content/FrontPaginationBar.vue'
import FrontPostCard from '../../components/content/FrontPostCard.vue'

/**
 * 前台专题详情页。
 * 作用：展示单个专题的说明和专题下的文章列表。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, error } = await useAsyncData(
  () => `site-topic-${route.fullPath}`,
  async () => {
    return await siteContent.getTopicDetail(String(route.params.slug || ''), {
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
    statusMessage: error.value.message || '专题不存在'
  })
}

useSeoMeta({
  title: () => `${data.value?.topic.name || '专题'} - 云屿`,
  description: () => data.value?.topic.summary || '云屿专题页'
})

/**
 * 切换分页。
 * 作用：更新当前页码，让专题文章列表继续按真实接口翻页。
 *
 * @param nextPage 目标页码
 */
async function changePage(nextPage: number) {
  await router.push({
    path: `/topics/${route.params.slug}`,
    query: {
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main v-if="data" class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <YunyuHero
      :src="data.topic.coverUrl"
      :alt="data.topic.name"
      min-height-class="min-h-[42svh] sm:min-h-[48svh] lg:min-h-[54svh]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
      content-width-class="max-w-5xl"
    >
      <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-200">专题页</p>
      <h1 class="mt-4 text-3xl font-bold text-white drop-shadow-lg sm:text-4xl lg:text-5xl">
        {{ data.topic.name }}
      </h1>
      <p class="mt-4 max-w-3xl text-sm leading-7 text-white/86 drop-shadow-md sm:text-base sm:leading-8">
        {{ data.topic.summary }}
      </p>
      <div class="mt-6 inline-flex items-center rounded-full border border-white/18 bg-white/10 px-4 py-2 text-sm font-medium text-white/90 backdrop-blur-sm">
        当前共有 {{ data.topic.articleCount }} 篇文章
      </div>
    </YunyuHero>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
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
