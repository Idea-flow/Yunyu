<script setup lang="ts">
import type { ArticleTocItem } from '../../types/post'
import ArticleContentRenderer from '../../components/content/ArticleContentRenderer.vue'
import ArticleTocTree from '../../components/content/ArticleTocTree.vue'

/**
 * 前台文章详情页。
 * 作用：接入后端真实公开接口，展示文章正文、目录与相关推荐内容。
 */
const route = useRoute()
const siteContent = useSiteContent()
const activeTocId = ref('')

const { data, error } = await useAsyncData(`site-post-${route.params.slug}`, async () => {
  return await siteContent.getPostDetail(String(route.params.slug || ''))
})

if (error.value) {
  throw createError({
    statusCode: 404,
    statusMessage: error.value.message || '文章不存在'
  })
}

const post = computed(() => data.value)

/**
 * 解析目录 JSON。
 * 作用：把后端返回的目录 JSON 文本安全转换为目录树组件可用的结构。
 */
const tocItems = computed<ArticleTocItem[]>(() => {
  const tocJson = post.value?.contentTocJson

  if (!tocJson) {
    return []
  }

  try {
    const parsed = JSON.parse(tocJson)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

watch(tocItems, value => {
  activeTocId.value = value[0]?.id || ''
}, { immediate: true })

useSeoMeta({
  title: () => post.value?.seoTitle || post.value?.title || '云屿文章',
  description: () => post.value?.seoDescription || post.value?.summary || '云屿文章详情'
})

/**
 * 处理目录点击跳转。
 * 作用：联动正文区滚动到对应标题，并同步当前激活的目录项。
 *
 * @param item 当前目录项
 */
function handleTocSelect(item: ArticleTocItem) {
  activeTocId.value = item.id

  if (!import.meta.client) {
    return
  }

  const target = document.getElementById(item.id)

  if (!target) {
    return
  }

  target.scrollIntoView({
    behavior: 'smooth',
    block: 'start'
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f7fbff_0%,#ffffff_36%,#f8fafc_100%)] text-slate-900 dark:bg-[linear-gradient(180deg,#020617_0%,#081120_42%,#020617_100%)] dark:text-slate-100">
    <section v-if="post" class="mx-auto max-w-[1360px] px-5 pb-8 pt-8 sm:px-8 lg:px-10">
      <div class="overflow-hidden rounded-[34px] border border-white/60 bg-white/80 shadow-[0_32px_90px_-54px_rgba(15,23,42,0.4)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/70">
        <div class="grid gap-0 xl:grid-cols-[minmax(0,1.05fr)_420px]">
          <div class="p-6 sm:p-8 lg:p-10">
            <div class="flex flex-wrap gap-2">
              <NuxtLink :to="`/categories/${post.categorySlug}`">
                <UBadge color="neutral" variant="soft" size="lg">{{ post.categoryName }}</UBadge>
              </NuxtLink>
              <NuxtLink
                v-for="topic in post.topicItems"
                :key="`${post.slug}-${topic.slug}`"
                :to="`/topics/${topic.slug}`"
              >
                <UBadge color="primary" variant="soft" size="lg">
                  {{ topic.name }}
                </UBadge>
              </NuxtLink>
              <NuxtLink
                v-for="tag in post.tagItems"
                :key="`${post.slug}-${tag.slug}`"
                :to="`/tags/${tag.slug}`"
              >
                <UBadge color="success" variant="soft" size="lg">
                  #{{ tag.name }}
                </UBadge>
              </NuxtLink>
            </div>

            <h1 class="mt-6 max-w-4xl text-3xl font-semibold leading-tight sm:text-4xl lg:text-5xl">
              {{ post.title }}
            </h1>

            <p class="mt-5 max-w-3xl text-base leading-8 text-slate-600 dark:text-slate-300">
              {{ post.summary }}
            </p>

            <div class="mt-6 flex flex-wrap items-center gap-5">
              <div class="flex items-center gap-3">
                <img
                  :src="post.authorAvatarUrl"
                  :alt="post.authorName"
                  class="h-12 w-12 rounded-full object-cover ring-2 ring-white/80 dark:ring-slate-800"
                >
                <div>
                  <p class="text-sm font-semibold">{{ post.authorName }}</p>
                  <p class="text-xs text-slate-500 dark:text-slate-400">内容编辑</p>
                </div>
              </div>

              <div class="flex flex-wrap gap-x-5 gap-y-2 text-sm text-slate-500 dark:text-slate-400">
                <span>{{ post.publishedAt }}</span>
                <span>{{ post.readingMinutes }} 分钟阅读</span>
                <span>{{ post.viewCount }} 浏览</span>
                <span>{{ post.likeCount }} 喜欢</span>
              </div>
            </div>
          </div>

          <div class="relative min-h-[280px]">
            <img :src="post.coverUrl" :alt="post.title" class="h-full w-full object-cover">
            <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.04)_0%,rgba(15,23,42,0.38)_100%)]" />
          </div>
        </div>
      </div>
    </section>

    <section v-if="post" class="mx-auto max-w-[1360px] px-5 pb-12 sm:px-8 lg:px-10">
      <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div class="space-y-6">
          <ArticleContentRenderer
            :html="post.contentHtml"
            content-theme="editorial"
            code-theme="github-light"
            :code-default-expanded="false"
            container-class="rounded-[32px] border-white/60 bg-white/84 shadow-[0_32px_90px_-54px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/72"
            body-class="px-1 sm:px-2 lg:px-4"
          />

          <section class="rounded-[32px] border border-white/60 bg-white/84 p-6 shadow-[0_32px_90px_-54px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/72">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
                  继续阅读
                </p>
                <h2 class="mt-3 text-2xl font-semibold">相关文章</h2>
              </div>
            </div>

            <div class="mt-6 grid gap-4 lg:grid-cols-3">
              <NuxtLink
                v-for="item in post.relatedPosts"
                :key="item.slug"
                :to="`/posts/${item.slug}`"
                class="overflow-hidden rounded-[24px] border border-slate-200/75 bg-white/90 transition hover:border-sky-200 dark:border-slate-800 dark:bg-slate-900/85 dark:hover:border-sky-900"
              >
                <img :src="item.coverUrl" :alt="item.title" class="h-40 w-full object-cover">
                <div class="p-4">
                  <p class="text-xs uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">{{ item.categoryName }}</p>
                  <h3 class="mt-3 text-base font-semibold leading-7">{{ item.title }}</h3>
                  <p class="mt-2 line-clamp-2 text-sm leading-6 text-slate-500 dark:text-slate-400">
                    {{ item.summary }}
                  </p>
                </div>
              </NuxtLink>
            </div>
          </section>
        </div>

        <aside class="space-y-4 xl:sticky xl:top-24 xl:self-start">
          <div class="rounded-[28px] border border-white/60 bg-white/84 p-5 shadow-[0_24px_70px_-46px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/72">
            <p class="text-xs font-semibold uppercase tracking-[0.34em] text-orange-500 dark:text-orange-300">
              目录
            </p>
            <h2 class="mt-3 text-xl font-semibold">文章结构</h2>
            <div class="mt-5">
              <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
            </div>
          </div>

          <div class="rounded-[28px] border border-white/60 bg-white/84 p-5 shadow-[0_24px_70px_-46px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/72">
            <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
              内容标签
            </p>
            <div class="mt-4 flex flex-wrap gap-2">
              <NuxtLink
                v-for="tag in post.tagItems"
                :key="`${post.slug}-${tag.slug}`"
                :to="`/tags/${tag.slug}`"
                class="rounded-full border border-slate-200/80 bg-white/90 px-3 py-1.5 text-xs font-medium text-slate-600 transition hover:border-sky-200 hover:text-sky-700 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300 dark:hover:border-sky-800 dark:hover:text-sky-200"
              >
                #{{ tag.name }}
              </NuxtLink>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </main>
</template>
