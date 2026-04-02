<script setup lang="ts">
import FrontPostCard from '../components/content/FrontPostCard.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'

/**
 * 前台首页。
 * 作用：以最基础、最简洁的内容首页结构承接主打文章、最新文章、分类入口与专题入口。
 */
const siteContent = useSiteContent()

const { data, pending, error } = await useAsyncData('site-home', async () => {
  return await siteContent.getHome()
})

const homeData = computed(() => data.value)
const featuredPost = computed(() => homeData.value?.featuredPosts?.[0] || null)
const latestPosts = computed(() => homeData.value?.latestPosts || [])
const categories = computed(() => homeData.value?.categories || [])
const topics = computed(() => homeData.value?.topics || [])
const siteInfo = computed(() => homeData.value?.siteInfo)

/**
 * 计算首页主打文章标签。
 * 作用：限制主打文章标签数量，保证首页首屏信息保持简洁。
 */
const featuredTags = computed(() => featuredPost.value?.tagItems?.slice(0, 2) || [])

/**
 * 计算首页最新文章列表。
 * 作用：首页正文只保留少量最新文章，避免内容过多导致首页显得杂乱。
 */
const latestDisplayPosts = computed(() => latestPosts.value.slice(0, 4))

/**
 * 计算首页分类入口。
 * 作用：首页分类区仅展示少量高频入口，保持版面清爽。
 */
const categoryEntries = computed(() => categories.value.slice(0, 4))

/**
 * 计算首页专题入口。
 * 作用：首页专题区仅展示少量入口，避免与文章流互相争夺注意力。
 */
const topicEntries = computed(() => topics.value.slice(0, 4))

useSeoMeta({
  title: () => siteInfo.value?.defaultTitle || '云屿 Yunyu',
  description: () => siteInfo.value?.defaultDescription || '云屿前台首页'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fafc_0%,#f8fafc_100%)] text-slate-900 transition-colors duration-300 dark:bg-[linear-gradient(180deg,#020617_0%,#020617_100%)] dark:text-slate-100">
    <section class="relative overflow-hidden">
      <div v-if="pending" class="space-y-8">
        <USkeleton class="h-[72svh] min-h-[520px] w-full rounded-none" />
      </div>

      <div
        v-else-if="featuredPost"
        class="space-y-12 pb-14 lg:space-y-14 lg:pb-20"
      >
        <YunyuHero
          :src="featuredPost.coverUrl"
          :alt="featuredPost.title"
          min-height-class="min-h-[68svh] sm:min-h-[72svh] lg:min-h-[78svh]"
          content-width-class="max-w-3xl"
          content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
          :show-starry="false"
        >
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-white/72">
            {{ siteInfo?.siteName || 'Yunyu / 云屿' }}
          </p>

          <h1 class="mt-4 text-[clamp(2.2rem,1.65rem+2.7vw,4rem)] font-semibold leading-[1.02] tracking-[-0.04em] text-white [font-family:var(--font-display)] [text-wrap:balance]">
            {{ featuredPost.title }}
          </h1>

          <p class="mt-4 max-w-[34rem] text-[0.98rem] leading-7 text-white/80">
            {{ featuredPost.summary }}
          </p>

          <div class="mt-5 flex flex-wrap gap-2">
            <UBadge color="neutral" variant="soft">{{ featuredPost.categoryName }}</UBadge>
            <NuxtLink
              v-for="tag in featuredTags"
              :key="`${featuredPost.slug}-${tag.slug}`"
              :to="`/tags/${tag.slug}`"
              class="rounded-full border border-white/18 bg-white/10 px-3 py-1.5 text-xs text-white/84 backdrop-blur-sm transition duration-200 hover:bg-white/16"
            >
              #{{ tag.name }}
            </NuxtLink>
          </div>

          <div class="mt-6 flex flex-wrap gap-3">
            <NuxtLink
              :to="`/posts/${featuredPost.slug}`"
              class="inline-flex items-center gap-2 rounded-full bg-white px-5 py-2.5 text-sm font-medium text-slate-900 transition duration-200 hover:bg-slate-100"
            >
              <span>阅读文章</span>
              <UIcon name="i-lucide-arrow-right" class="size-4" />
            </NuxtLink>
            <NuxtLink
              to="/posts"
              class="inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-5 py-2.5 text-sm font-medium text-white/90 backdrop-blur-sm transition duration-200 hover:bg-white/16"
            >
              <span>全部文章</span>
              <UIcon name="i-lucide-book-open" class="size-4" />
            </NuxtLink>
          </div>
        </YunyuHero>

        <section class="mx-auto max-w-[1240px] px-5 sm:px-8 lg:px-10">
          <YunyuSectionTitle
            eyebrow="最新文章"
            title="最新文章"
            link-label="查看全部"
            link-to="/posts"
          />

          <div class="mt-8 space-y-6">
            <FrontPostCard
              v-for="post in latestDisplayPosts"
              :key="post.slug"
              :post="post"
              :topic-limit="1"
              :show-tags="false"
              image-height-class="h-48"
              root-class="sm:grid-cols-[220px_minmax(0,1fr)]"
            />
          </div>
        </section>

        <section class="mx-auto max-w-[1240px] px-5 sm:px-8 lg:px-10">
          <div class="grid gap-10 lg:grid-cols-2">
            <div>
              <YunyuSectionTitle
                eyebrow="分类"
                title="分类"
                link-label="查看全部"
                link-to="/categories"
              />

              <div class="mt-8 grid gap-4 sm:grid-cols-2">
                <NuxtLink
                  v-for="category in categoryEntries"
                  :key="category.slug"
                  :to="`/categories/${category.slug}`"
                  class="rounded-[24px] border border-slate-200/80 bg-white p-5 transition duration-200 hover:border-sky-200 hover:shadow-[0_18px_40px_-34px_rgba(15,23,42,0.2)] dark:border-white/10 dark:bg-slate-950/60 dark:hover:border-sky-900"
                >
                  <p class="text-sm font-semibold text-slate-950 dark:text-slate-50">{{ category.name }}</p>
                  <p class="mt-2 text-xs text-slate-500 dark:text-slate-400">{{ category.articleCount }} 篇</p>
                </NuxtLink>
              </div>
            </div>

            <div>
              <YunyuSectionTitle
                eyebrow="专题"
                title="专题"
                link-label="查看全部"
                link-to="/topics"
              />

              <div class="mt-8 grid gap-4 sm:grid-cols-2">
                <NuxtLink
                  v-for="topic in topicEntries"
                  :key="topic.slug"
                  :to="`/topics/${topic.slug}`"
                  class="rounded-[24px] border border-slate-200/80 bg-white p-5 transition duration-200 hover:border-sky-200 hover:shadow-[0_18px_40px_-34px_rgba(15,23,42,0.2)] dark:border-white/10 dark:bg-slate-950/60 dark:hover:border-sky-900"
                >
                  <p class="text-sm font-semibold text-slate-950 dark:text-slate-50">{{ topic.name }}</p>
                  <p class="mt-2 text-xs text-slate-500 dark:text-slate-400">{{ topic.articleCount }} 篇</p>
                </NuxtLink>
              </div>
            </div>
          </div>
        </section>
      </div>

      <div
        v-else-if="error"
        class="mx-auto max-w-[1240px] px-5 py-20 sm:px-8 lg:px-10"
      >
        <div class="rounded-[24px] border border-rose-200 bg-rose-50/80 p-6 text-sm text-rose-600 dark:border-rose-900/60 dark:bg-rose-950/30 dark:text-rose-300">
          {{ error.message }}
        </div>
      </div>
    </section>
  </main>
</template>
