<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuPagination from '../../components/common/YunyuPagination.vue'
import FrontPostCard from '../../components/content/FrontPostCard.vue'

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
    <YunyuHero
      :src="data.category.coverUrl"
      :alt="data.category.name"
      min-height-class="min-h-[42svh] sm:min-h-[48svh] lg:min-h-[54svh]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
      content-width-class="max-w-5xl"
    >
      <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-orange-200">分类页</p>
      <h1 class="mt-4 text-[clamp(2.55rem,1.85rem+2.5vw,4.8rem)] font-semibold leading-[0.98] tracking-[-0.045em] [font-family:var(--font-display)] [text-wrap:balance] text-white drop-shadow-lg">
        {{ data.category.name }}
      </h1>
      <p class="mt-4 max-w-3xl text-sm leading-7 text-white/86 drop-shadow-md sm:text-base sm:leading-8">
        {{ data.category.description }}
      </p>
      <div class="mt-6 inline-flex items-center rounded-full border border-white/18 bg-white/10 px-4 py-2 text-sm font-medium text-white/90 backdrop-blur-sm">
        当前共有 {{ data.category.articleCount }} 篇文章
      </div>
    </YunyuHero>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="mt-6 space-y-4">
        <FrontPostCard
          v-for="post in data.posts.list"
          :key="post.slug"
          :post="post"
          :show-category="false"
          title-class="mt-0"
        />
      </div>

      <YunyuPagination
        class="mt-8"
        :page-no="data.posts.pageNo"
        :total-pages="data.posts.totalPages"
        @change="changePage"
      />
    </section>
  </main>
</template>
