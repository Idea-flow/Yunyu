<script setup lang="ts">
import FrontPostCard from '../components/content/FrontPostCard.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'
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
const leadLatestPost = computed(() => latestPosts.value[0] || null)
const latestStreamPosts = computed(() => latestPosts.value.slice(1))
const categories = computed(() => homeData.value?.categories || [])
const topics = computed(() => homeData.value?.topics || [])
const siteInfo = computed(() => homeData.value?.siteInfo)

/**
 * 计算首页首屏统计项。
 * 作用：把首页已有内容数据转成统一的品牌信息摘要，服务首屏展示。
 */
const heroStats = computed(() => ([
  { label: '推荐', value: String(homeData.value?.featuredPosts?.length || 0).padStart(2, '0') },
  { label: '最新', value: String(homeData.value?.latestPosts?.length || 0).padStart(2, '0') },
  { label: '分类', value: String(homeData.value?.categories?.length || 0).padStart(2, '0') }
]))

/**
 * 计算首页主打文章标签。
 * 作用：控制首屏标签数量，避免主视觉区域信息过满。
 */
const featuredTags = computed(() => featuredPost.value?.tagItems?.slice(0, 3) || [])

/**
 * 计算首页推荐专题。
 * 作用：为首页中段提供更聚焦的专题入口，而不是一次性摊开全部内容。
 */
const spotlightTopics = computed(() => topics.value.slice(0, 4))

/**
 * 计算首页重点分类。
 * 作用：保留最适合首屏后段承接的分类数量，提升阅读地图的聚焦感。
 */
const spotlightCategories = computed(() => categories.value.slice(0, 4))

useSeoMeta({
  title: () => siteInfo.value?.defaultTitle || '云屿 Yunyu',
  description: () => siteInfo.value?.defaultDescription || '云屿前台首页'
})
</script>

<template>
  <main class="min-h-screen overflow-hidden bg-[linear-gradient(180deg,#f7fbff_0%,#edf5ff_36%,#ffffff_100%)] text-slate-900 transition-colors duration-300 dark:bg-[linear-gradient(180deg,#030712_0%,#071120_38%,#020617_100%)] dark:text-slate-100">
    <section class="relative overflow-hidden">
      <div class="relative">
        <div v-if="pending" class="space-y-8">
          <USkeleton class="h-[78svh] min-h-[540px] w-full rounded-none" />
        </div>

        <div
          v-else-if="featuredPost"
          class="space-y-16"
        >
          <YunyuHero :src="featuredPost.coverUrl" :alt="featuredPost.title">
            <p class="text-xs font-semibold uppercase tracking-[0.46em] text-white/76 drop-shadow-md">
              {{ siteInfo?.siteName || 'Yunyu / 云屿' }}
            </p>

            <h1 class="mt-5 text-[clamp(3rem,1.9rem+4.3vw,5.7rem)] font-semibold leading-[0.95] tracking-[-0.05em] [font-family:var(--font-display)] [text-wrap:balance] text-white drop-shadow-lg">
              把热爱、情绪与阅读节奏，做成一座可以慢慢逛的个人内容站。
            </h1>

            <p class="mt-5 max-w-[36rem] text-base leading-8 text-white/84 drop-shadow-md sm:text-[1.05rem]">
              {{ siteInfo?.siteSubTitle || '在二次元场景与情绪里漫游的内容站' }}。这里不只展示文章，更把专题、分类与主打内容整理成一条更有沉浸感的阅读路径。
            </p>

            <div class="mt-6 flex flex-wrap gap-2">
              <div class="rounded-full border border-white/18 bg-white/10 px-4 py-2 text-[11px] font-semibold uppercase tracking-[0.28em] text-white/90 backdrop-blur-md">
                Editor's Pick
              </div>
              <div class="rounded-full border border-white/18 bg-white/10 px-4 py-2 text-xs font-medium text-white/88 backdrop-blur-md">
                {{ featuredPost.categoryName }}
              </div>
              <NuxtLink
                v-for="tag in featuredTags"
                :key="`${featuredPost.slug}-${tag.slug}`"
                :to="`/tags/${tag.slug}`"
                class="rounded-full border border-white/18 bg-white/10 px-3 py-2 text-xs font-medium text-white/88 backdrop-blur-sm transition hover:bg-white/18"
              >
                #{{ tag.name }}
              </NuxtLink>
            </div>

            <div class="mt-7 flex flex-wrap items-center gap-5 text-white/90">
              <div class="flex items-center gap-3">
                <YunyuImage
                  :src="featuredPost.authorAvatarUrl"
                  :alt="featuredPost.authorName"
                  wrapper-class="h-11 w-11 ring-2 ring-white/26"
                  image-class="h-full w-full"
                  rounded-class="rounded-full"
                />
                <div>
                  <p class="text-sm font-medium text-white drop-shadow-md">{{ featuredPost.authorName }}</p>
                  <p class="text-xs text-white/66">{{ featuredPost.publishedAt }} · {{ featuredPost.readingMinutes }} 分钟阅读</p>
                </div>
              </div>

              <div class="flex flex-wrap gap-3">
                <span
                  v-for="stat in heroStats"
                  :key="stat.label"
                  class="rounded-full border border-white/16 bg-white/10 px-3 py-1.5 text-sm text-white/84 backdrop-blur-sm"
                >
                  {{ stat.label }} {{ stat.value }}
                </span>
              </div>
            </div>

            <div class="mt-8 flex flex-wrap gap-3">
              <NuxtLink
                :to="`/posts/${featuredPost.slug}`"
                class="inline-flex items-center gap-2 rounded-full bg-white/92 px-6 py-3 text-sm font-medium text-slate-900 transition hover:bg-white"
              >
                <span>进入主打内容</span>
                <UIcon name="i-lucide-arrow-right" class="size-4" />
              </NuxtLink>
              <NuxtLink
                to="/topics"
                class="inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-6 py-3 text-sm font-medium text-white/90 backdrop-blur-sm transition hover:bg-white/18"
              >
                <span>按专题继续阅读</span>
                <UIcon name="i-lucide-book-open-text" class="size-4" />
              </NuxtLink>
            </div>
          </YunyuHero>

          <section class="mx-auto max-w-[1440px] px-5 sm:px-8 lg:px-10">
            <div class="grid gap-8 xl:grid-cols-[minmax(0,0.95fr)_minmax(0,1.05fr)]">
            <div class="rounded-[34px] border border-white/60 bg-white/70 p-7 shadow-[0_30px_80px_-50px_rgba(15,23,42,0.34)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/62">
              <p class="text-xs font-semibold uppercase tracking-[0.34em] text-slate-500 dark:text-slate-400">
                Reading Note
              </p>
              <p class="mt-5 text-[clamp(1.75rem,1.45rem+0.9vw,2.35rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
                云屿不是内容仓库，而是一条有顺序、有情绪、有节奏的阅读路径。
              </p>
              <p class="mt-5 text-[clamp(1rem,0.95rem+0.16vw,1.08rem)] leading-[1.95] tracking-[-0.01em] text-slate-600 dark:text-slate-300">
                {{ siteInfo?.defaultDescription || '聚焦新番观察、场景美学与专题化阅读体验。' }}
                首页后续会持续把“主打推荐、推荐阅读、分类地图、专题路线、最新内容”整理成可连续进入的内容流。
              </p>
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <NuxtLink
                v-for="topic in spotlightTopics"
                :key="topic.slug"
                :to="`/topics/${topic.slug}`"
                class="group overflow-hidden rounded-[30px] border border-white/60 bg-white/76 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.34)] transition hover:-translate-y-0.5 hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/62 dark:hover:border-sky-900"
              >
                <YunyuImage
                  :src="topic.coverUrl"
                  :alt="topic.name"
                  image-class="h-44 w-full transition duration-500 group-hover:scale-[1.03]"
                  rounded-class="rounded-t-[30px] rounded-b-none"
                />
                <div class="p-5">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">{{ topic.name }}</p>
                    <span class="text-[11px] font-medium text-slate-400 dark:text-slate-500">{{ topic.articleCount }} 篇</span>
                  </div>
                  <p class="mt-3 line-clamp-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                    {{ topic.summary }}
                  </p>
                </div>
              </NuxtLink>
            </div>
            </div>
          </section>
        </div>

        <div
          v-else-if="error"
          class="rounded-[28px] border border-rose-200 bg-rose-50/80 p-6 text-sm text-rose-600 dark:border-rose-900/60 dark:bg-rose-950/30 dark:text-rose-300"
        >
          {{ error.message }}
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1440px] px-5 py-10 sm:px-8 lg:px-10">
      <YunyuSectionTitle
        eyebrow="推荐阅读"
        title="先从这三篇进入云屿"
        description="推荐区负责承接首页首屏后的第一段阅读路径，让用户先进入最能代表当前气质的内容。"
        link-label="查看全部文章"
        link-to="/posts"
      />

      <div class="mt-8 grid gap-5 lg:grid-cols-3">
        <FrontPostCard
          v-for="post in recommendedPosts"
          :key="post.slug"
          :post="post"
          layout="stack"
          :topic-limit="1"
          image-height-class="h-64"
        />
      </div>
    </section>

    <section class="mx-auto max-w-[1440px] px-5 py-10 sm:px-8 lg:px-10">
      <YunyuSectionTitle
        eyebrow="分类地图"
        title="先按兴趣，再按情绪进入"
        description="分类区后续承担首页的阅读地图功能，不只是展示入口，而是告诉用户可以从哪条方向开始逛。"
        tone="orange"
        link-label="查看全部分类"
        link-to="/categories"
      />

      <div class="mt-8 grid gap-5 xl:grid-cols-[minmax(0,1.08fr)_minmax(0,0.92fr)]">
        <div class="grid gap-4 sm:grid-cols-2">
          <NuxtLink
            v-for="category in spotlightCategories"
            :key="category.slug"
            :to="`/categories/${category.slug}`"
            class="group grid gap-4 rounded-[30px] border border-white/60 bg-white/74 p-4 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.34)] transition hover:-translate-y-0.5 hover:border-orange-200 dark:border-white/10 dark:bg-slate-950/62 dark:hover:border-orange-900"
          >
            <YunyuImage
              :src="category.coverUrl"
              :alt="category.name"
              image-class="h-48 w-full transition duration-500 group-hover:scale-[1.03]"
              rounded-class="rounded-[24px]"
            />
            <div class="px-1 pb-1">
              <div class="flex items-center justify-between gap-3">
                <h3 class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50 sm:text-[1.35rem]">{{ category.name }}</h3>
                <span class="text-xs text-slate-500 dark:text-slate-400">{{ category.articleCount }} 篇</span>
              </div>
              <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                {{ category.description }}
              </p>
            </div>
          </NuxtLink>
        </div>

        <div class="rounded-[34px] border border-white/60 bg-white/70 p-6 shadow-[0_30px_80px_-50px_rgba(15,23,42,0.34)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/62">
          <YunyuSectionTitle
            eyebrow="专题路线"
            title="从主题进入，会更沉浸"
            description="专题承担连续阅读的组织能力，让同一主题下的文章不再散落，而是形成可持续进入的阅读流。"
            tone="orange"
            link-label="查看全部专题"
            link-to="/topics"
          />

          <div class="mt-8 space-y-4">
            <NuxtLink
              v-for="topic in spotlightTopics"
              :key="topic.slug"
              :to="`/topics/${topic.slug}`"
              class="flex items-center gap-4 rounded-[24px] border border-slate-200/75 bg-white/88 p-4 transition hover:border-sky-200 hover:bg-sky-50/70 dark:border-slate-800 dark:bg-slate-900/82 dark:hover:border-sky-900 dark:hover:bg-slate-900"
            >
              <YunyuImage
                :src="topic.coverUrl"
                :alt="topic.name"
                wrapper-class="h-[4.75rem] w-[4.75rem] shrink-0"
                image-class="h-full w-full"
                rounded-class="rounded-[20px]"
              />
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-3">
                  <p class="truncate text-sm font-semibold text-slate-950 dark:text-slate-50">{{ topic.name }}</p>
                  <span class="shrink-0 text-[11px] font-medium text-slate-400 dark:text-slate-500">{{ topic.articleCount }} 篇</span>
                </div>
                <p class="mt-2 line-clamp-2 text-xs leading-6 text-slate-500 dark:text-slate-400">
                  {{ topic.summary }}
                </p>
              </div>
            </NuxtLink>
          </div>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1440px] px-5 py-10 pb-16 sm:px-8 lg:px-10 lg:pb-24">
      <YunyuSectionTitle
        eyebrow="最新内容"
        title="把内容做成可以慢慢逛的阅读流"
        description="最新区不是简单倒序列表，而是首页最后一段持续更新的阅读流，让用户在主打内容之外继续延展。"
        link-label="查看全部专题"
        link-to="/topics"
      />

      <div class="mt-8 grid gap-8 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div class="space-y-8">
          <NuxtLink
            v-if="leadLatestPost"
            :to="`/posts/${leadLatestPost.slug}`"
            class="group grid gap-6 border-b border-slate-200/75 pb-8 dark:border-white/10 lg:grid-cols-[minmax(0,1.05fr)_300px]"
          >
            <div class="min-w-0">
              <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                Latest Feature
              </p>
              <h3 class="mt-4 text-[clamp(2rem,1.6rem+1.2vw,2.9rem)] font-semibold leading-[1.04] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                {{ leadLatestPost.title }}
              </h3>
              <p class="mt-4 max-w-3xl text-[1rem] leading-8 text-slate-600 dark:text-slate-300">
                {{ leadLatestPost.summary }}
              </p>
              <div class="mt-5 flex flex-wrap gap-2">
                <UBadge color="neutral" variant="soft">{{ leadLatestPost.categoryName }}</UBadge>
                <NuxtLink
                  v-for="topic in leadLatestPost.topicItems.slice(0, 2)"
                  :key="`${leadLatestPost.slug}-${topic.slug}`"
                  :to="`/topics/${topic.slug}`"
                  class="relative z-10"
                  @click.stop
                >
                  <UBadge color="primary" variant="soft">{{ topic.name }}</UBadge>
                </NuxtLink>
              </div>
              <div class="mt-6 flex flex-wrap gap-x-5 gap-y-2 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
                <span>{{ leadLatestPost.authorName }}</span>
                <span>{{ leadLatestPost.publishedAt }}</span>
                <span>{{ leadLatestPost.readingMinutes }} 分钟阅读</span>
                <span>{{ leadLatestPost.viewCount }} 浏览</span>
              </div>
            </div>

            <YunyuImage
              :src="leadLatestPost.coverUrl"
              :alt="leadLatestPost.title"
              image-class="h-72 w-full object-cover transition duration-500 group-hover:scale-[1.02] lg:h-full"
              rounded-class="rounded-[28px]"
            />
          </NuxtLink>

          <div class="space-y-6">
            <FrontPostCard
              v-for="post in latestStreamPosts"
              :key="post.slug"
              :post="post"
              :topic-limit="1"
              :show-tags="true"
              image-height-class="h-52"
              root-class="sm:grid-cols-[240px_minmax(0,1fr)]"
            />
          </div>
        </div>

        <aside class="space-y-4">
          <div class="rounded-[34px] border border-white/60 bg-white/72 p-6 shadow-[0_28px_80px_-52px_rgba(15,23,42,0.34)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/62">
            <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
              Reading Atlas
            </p>
            <p class="mt-4 text-[1.7rem] font-semibold leading-tight text-slate-950 dark:text-slate-50">
              首页后段保留“继续阅读”的动力，而不是让浏览在首屏后断掉。
            </p>
            <p class="mt-4 text-sm leading-7 text-slate-600 dark:text-slate-300">
              这里后续会继续增加系列文章、延伸阅读、阅读路线与更完整的内容索引，让云屿真正具备“逛”的感觉。
            </p>
          </div>

          <div
            v-for="topic in spotlightTopics"
            :key="topic.slug"
            class="overflow-hidden rounded-[30px] border border-white/60 bg-white/76 shadow-[0_24px_70px_-48px_rgba(15,23,42,0.34)] transition hover:-translate-y-0.5 hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/62 dark:hover:border-sky-900"
          >
            <YunyuImage
              :src="topic.coverUrl"
              :alt="topic.name"
              image-class="h-48 w-full"
              rounded-class="rounded-t-[30px] rounded-b-none"
            />
            <div class="p-5">
              <div class="flex items-center justify-between gap-3">
                <NuxtLink :to="`/topics/${topic.slug}`" class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition hover:text-sky-700 dark:text-slate-50 dark:hover:text-sky-200">{{ topic.name }}</NuxtLink>
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
