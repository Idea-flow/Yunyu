<script setup lang="ts">
import FrontPostCard from '../components/content/FrontPostCard.vue'
/**
 * 前台首页。
 * 作用：承接前台首页的品牌头图、推荐文章、最新内容、分类入口和专题内容展示。
 */
const siteContent = useSiteContent()

const { data, pending, error } = await useAsyncData('site-home', async () => {
  return await siteContent.getHome()
})

const homeData = computed(() => data.value)
const featuredPost = computed(() => homeData.value?.featuredPosts?.[0] || null)
const recommendedPosts = computed(() => homeData.value?.featuredPosts?.slice(1, 4) || [])
const latestPosts = computed(() => homeData.value?.latestPosts || [])
const categories = computed(() => homeData.value?.categories || [])
const topics = computed(() => homeData.value?.topics || [])
const siteInfo = computed(() => homeData.value?.siteInfo)

useSeoMeta({
  title: () => siteInfo.value?.defaultTitle || '云屿 Yunyu',
  description: () => siteInfo.value?.defaultDescription || '云屿前台首页'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f7fbff_0%,#eff6ff_35%,#ffffff_100%)] text-slate-900 transition-colors duration-300 dark:bg-[linear-gradient(180deg,#020617_0%,#081120_42%,#020617_100%)] dark:text-slate-100">
    <section class="relative overflow-hidden">
      <div class="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(56,189,248,0.22),transparent_28rem),radial-gradient(circle_at_80%_12%,rgba(251,146,60,0.18),transparent_22rem)]" />
      <div class="relative mx-auto max-w-[1360px] px-5 pb-12 pt-8 sm:px-8 lg:px-10 lg:pb-16 lg:pt-10">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.42em] text-sky-600 dark:text-sky-300">
            Yunyu / 云屿
          </p>
          <h1 class="mt-3 text-2xl font-semibold tracking-tight sm:text-3xl">
            {{ siteInfo?.siteSubTitle || '在二次元场景与情绪里漫游的内容站' }}
          </h1>
        </div>

        <div v-if="pending" class="mt-8 grid gap-6 xl:grid-cols-[minmax(0,1.18fr)_392px]">
          <USkeleton class="h-[520px] rounded-[32px]" />
          <div class="grid gap-4">
            <USkeleton class="h-[252px] rounded-[28px]" />
            <USkeleton class="h-[252px] rounded-[28px]" />
          </div>
        </div>

        <div
          v-else-if="featuredPost"
          class="mt-8 grid gap-6 xl:grid-cols-[minmax(0,1.18fr)_392px]"
        >
          <NuxtLink
            :to="`/posts/${featuredPost.slug}`"
            class="group relative overflow-hidden rounded-[32px] border border-white/60 bg-white/78 shadow-[0_30px_80px_-40px_rgba(15,23,42,0.35)] backdrop-blur-xl transition duration-300 hover:-translate-y-0.5 dark:border-white/10 dark:bg-slate-950/68"
          >
            <div class="grid min-h-[520px] lg:grid-cols-[minmax(0,1.05fr)_0.95fr]">
              <div class="flex flex-col justify-between p-7 sm:p-8 lg:p-10">
                <div>
                  <div class="flex flex-wrap gap-2">
                    <UBadge color="primary" variant="soft" size="lg">主打推荐</UBadge>
                    <UBadge color="neutral" variant="soft" size="lg">{{ featuredPost.categoryName }}</UBadge>
                  </div>
                  <h2 class="mt-6 max-w-3xl text-3xl font-semibold leading-tight sm:text-4xl lg:text-5xl">
                    {{ featuredPost.title }}
                  </h2>
                  <p class="mt-5 max-w-2xl text-sm leading-8 text-slate-600 sm:text-base dark:text-slate-300">
                    {{ featuredPost.summary }}
                  </p>
                </div>

                <div class="mt-8 flex flex-wrap items-end justify-between gap-5">
                  <div class="flex items-center gap-3">
                    <img
                      :src="featuredPost.authorAvatarUrl"
                      :alt="featuredPost.authorName"
                      class="h-12 w-12 rounded-full object-cover ring-2 ring-white/70 dark:ring-slate-800"
                    >
                    <div>
                      <p class="text-sm font-semibold">{{ featuredPost.authorName }}</p>
                      <p class="text-xs text-slate-500 dark:text-slate-400">
                        内容编辑 · {{ featuredPost.publishedAt }} · {{ featuredPost.readingMinutes }} 分钟
                      </p>
                    </div>
                  </div>

                  <div class="flex flex-wrap gap-2">
                    <NuxtLink
                      v-for="tag in featuredPost.tagItems"
                      :key="`${featuredPost.slug}-${tag.slug}`"
                      :to="`/tags/${tag.slug}`"
                      class="relative z-10 rounded-full border border-slate-200/80 bg-white/80 px-3 py-1 text-xs font-medium text-slate-600 transition hover:border-sky-200 hover:text-sky-700 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300 dark:hover:border-sky-800 dark:hover:text-sky-200"
                      @click.stop
                    >
                      #{{ tag.name }}
                    </NuxtLink>
                  </div>
                </div>
              </div>

              <div class="relative min-h-[320px] overflow-hidden">
                <img
                  :src="featuredPost.coverUrl"
                  :alt="featuredPost.title"
                  class="h-full w-full object-cover transition duration-500 group-hover:scale-[1.03]"
                >
                <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.02)_0%,rgba(15,23,42,0.3)_100%)]" />
              </div>
            </div>
          </NuxtLink>

          <aside class="flex flex-col gap-4">
            <div class="rounded-[28px] border border-white/60 bg-white/74 p-6 shadow-[0_24px_70px_-44px_rgba(15,23,42,0.36)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/68">
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
                    站点速览
                  </p>
                  <p class="mt-4 text-sm leading-7 text-slate-600 dark:text-slate-300">
                    {{ siteInfo?.defaultDescription || '聚焦新番观察、场景美学与专题化阅读体验。' }}
                  </p>
                </div>
                <div class="rounded-2xl bg-sky-50 p-3 text-sky-600 dark:bg-sky-400/10 dark:text-sky-300">
                  <UIcon name="i-lucide-sparkles" class="size-5" />
                </div>
              </div>

              <div class="mt-6 grid grid-cols-3 gap-3">
                <div
                  v-for="stat in [
                    { label: '推荐', value: String(homeData?.featuredPosts?.length || 0).padStart(2, '0') },
                    { label: '最新', value: String(homeData?.latestPosts?.length || 0).padStart(2, '0') },
                    { label: '分类', value: String(homeData?.categories?.length || 0).padStart(2, '0') }
                  ]"
                  :key="stat.label"
                  class="rounded-[22px] border border-slate-200/75 bg-white/88 px-4 py-4 dark:border-slate-800 dark:bg-slate-900/88"
                >
                  <p class="text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400 dark:text-slate-500">
                    {{ stat.label }}
                  </p>
                  <p class="mt-2 text-3xl font-semibold leading-none text-slate-900 dark:text-slate-50">
                    {{ stat.value }}
                  </p>
                </div>
              </div>
            </div>

            <div class="rounded-[28px] border border-white/60 bg-white/74 p-6 shadow-[0_24px_70px_-44px_rgba(15,23,42,0.36)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/68">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-xs font-semibold uppercase tracking-[0.34em] text-orange-500 dark:text-orange-300">
                    本周专题
                  </p>
                  <h3 class="mt-3 text-lg font-semibold leading-8">从主题进入，会更沉浸</h3>
                </div>
                <div class="rounded-2xl bg-orange-50 p-3 text-orange-500 dark:bg-orange-400/10 dark:text-orange-300">
                  <UIcon name="i-lucide-book-open-text" class="size-5" />
                </div>
              </div>

              <div class="mt-5 space-y-3">
                <NuxtLink
                  v-for="topic in topics.slice(0, 3)"
                  :key="topic.slug"
                  :to="`/topics/${topic.slug}`"
                  class="flex items-center gap-4 rounded-[22px] border border-slate-200/75 bg-white/90 p-4 transition hover:border-sky-200 hover:bg-sky-50/70 dark:border-slate-800 dark:bg-slate-900/88 dark:hover:border-sky-900 dark:hover:bg-slate-900"
                >
                  <img :src="topic.coverUrl" :alt="topic.name" class="h-[4.5rem] w-[4.5rem] shrink-0 rounded-[20px] object-cover">
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center justify-between gap-3">
                      <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">{{ topic.name }}</p>
                      <span class="shrink-0 text-[11px] font-medium text-slate-400 dark:text-slate-500">{{ topic.articleCount }} 篇</span>
                    </div>
                    <p class="mt-2 line-clamp-2 text-xs leading-6 text-slate-500 dark:text-slate-400">
                      {{ topic.summary }}
                    </p>
                  </div>
                </NuxtLink>
              </div>
            </div>
          </aside>
        </div>

        <div
          v-else-if="error"
          class="mt-8 rounded-[28px] border border-rose-200 bg-rose-50/80 p-6 text-sm text-rose-600 dark:border-rose-900/60 dark:bg-rose-950/30 dark:text-rose-300"
        >
          {{ error.message }}
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="flex items-end justify-between gap-5">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
            推荐阅读
          </p>
          <h2 class="mt-3 text-2xl font-semibold">先从这三篇进入云屿</h2>
        </div>
      </div>

      <div class="mt-6 grid gap-5 lg:grid-cols-3">
        <FrontPostCard
          v-for="post in recommendedPosts"
          :key="post.slug"
          :post="post"
          layout="stack"
          :topic-limit="1"
          image-height-class="h-56"
        />
      </div>
    </section>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="flex items-end justify-between gap-5">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-orange-500 dark:text-orange-300">
            分类入口
          </p>
          <h2 class="mt-3 text-2xl font-semibold">先按兴趣，再按情绪进入</h2>
        </div>
      </div>

      <div class="mt-6 grid gap-5 lg:grid-cols-2 xl:grid-cols-4">
        <NuxtLink
          v-for="category in categories"
          :key="category.slug"
          :to="`/categories/${category.slug}`"
          class="overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/68"
        >
          <img :src="category.coverUrl" :alt="category.name" class="h-44 w-full object-cover">
          <div class="p-5">
            <div class="flex items-center justify-between gap-3">
              <h3 class="text-lg font-semibold">{{ category.name }}</h3>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ category.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
              {{ category.description }}
            </p>
          </div>
        </NuxtLink>
      </div>
    </section>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">
      <div class="flex items-end justify-between gap-5">
        <div>
          <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
            最新内容
          </p>
          <h2 class="mt-3 text-2xl font-semibold">把内容做成可以慢慢逛的阅读流</h2>
        </div>
      </div>

      <div class="mt-6 grid gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div class="space-y-4">
          <FrontPostCard
            v-for="post in latestPosts"
            :key="post.slug"
            :post="post"
            :topic-limit="1"
            :show-tags="true"
            image-height-class="h-48"
            root-class="sm:grid-cols-[220px_minmax(0,1fr)]"
          />
        </div>

        <aside class="space-y-4">
          <div
            v-for="topic in topics"
            :key="topic.slug"
            class="overflow-hidden rounded-[28px] border border-white/60 bg-white/82 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.36)] dark:border-white/10 dark:bg-slate-950/68"
          >
            <img :src="topic.coverUrl" :alt="topic.name" class="h-44 w-full object-cover">
            <div class="p-5">
              <div class="flex items-center justify-between gap-3">
                <NuxtLink :to="`/topics/${topic.slug}`" class="text-lg font-semibold hover:text-sky-700 dark:hover:text-sky-200">{{ topic.name }}</NuxtLink>
                <span class="text-xs text-slate-500 dark:text-slate-400">{{ topic.articleCount }} 篇</span>
              </div>
              <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                {{ topic.summary }}
              </p>
            </div>
          </div>
        </aside>
      </div>
    </section>
  </main>
</template>
