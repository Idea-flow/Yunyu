<script setup lang="ts">
import FrontPostCard from '../components/content/FrontPostCard.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'

/**
 * 前台首页。
 * 作用：重构首页为更清晰的内容首页结构，统一承接首屏品牌表达、精选阅读、阅读地图与最新内容流。
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
 * 计算首页首屏统计项。
 * 作用：把首页内容规模收口成 3 个最关键的摘要，避免首屏信息分散。
 */
const heroStats = computed(() => ([
  { label: '主打', value: String(homeData.value?.featuredPosts?.length || 0).padStart(2, '0') },
  { label: '专题', value: String(homeData.value?.topics?.length || 0).padStart(2, '0') },
  { label: '分类', value: String(homeData.value?.categories?.length || 0).padStart(2, '0') }
]))

/**
 * 计算首页主打文章标签。
 * 作用：控制首屏标签数量，让 Hero 保持辨识度而不是变成标签墙。
 */
const featuredTags = computed(() => featuredPost.value?.tagItems?.slice(0, 3) || [])

/**
 * 计算首页精选阅读数据。
 * 作用：优先使用推荐内容承接首页第二屏，不足时再回退到最新内容。
 */
const editorialPosts = computed(() => {
  const featuredList = homeData.value?.featuredPosts?.slice(1, 4) || []

  if (featuredList.length >= 3) {
    return featuredList
  }

  const fallbackPosts = latestPosts.value.filter(post => post.slug !== featuredPost.value?.slug)
  return [...featuredList, ...fallbackPosts].slice(0, 3)
})

/**
 * 计算精选区主文章。
 * 作用：为首页第二屏提供一个更强的主视觉入口。
 */
const editorialLeadPost = computed(() => editorialPosts.value[0] || null)

/**
 * 计算精选区辅助文章列表。
 * 作用：将剩余精选文章整理为右侧轻量导流卡片，减少同权重堆叠。
 */
const editorialSidePosts = computed(() => editorialPosts.value.slice(1))

/**
 * 计算阅读地图分类入口。
 * 作用：分类区只保留最值得首页承接的两个入口，降低视觉噪音。
 */
const atlasCategories = computed(() => categories.value.slice(0, 2))

/**
 * 计算阅读地图专题入口。
 * 作用：专题区保留两个最聚焦的入口，让阅读地图承担“怎么逛”的职责。
 */
const atlasTopics = computed(() => topics.value.slice(0, 2))

/**
 * 计算最新内容主条目。
 * 作用：让最新内容区先展示一个清晰的主入口，而不是所有内容同时竞争注意力。
 */
const latestLeadPost = computed(() => latestPosts.value[0] || null)

/**
 * 计算最新内容流列表。
 * 作用：保留足够更新感，同时避免首页尾段再次变成过密内容墙。
 */
const latestStreamPosts = computed(() => latestPosts.value.slice(1, 4))

/**
 * 阅读地图卡片色板。
 * 作用：让阅读地图中的入口拥有统一但有区分的柔和底色层级。
 */
const atlasToneClassNames = [
  'front-atlas-card--sky',
  'front-atlas-card--orange',
  'front-atlas-card--rose',
  'front-atlas-card--slate'
] as const

/**
 * 获取阅读地图分类卡片色调类名。
 * 作用：根据分类顺序分配柔和背景色，帮助用户更快区分入口。
 *
 * @param index 分类索引
 * @returns 对应的色调类名
 */
function getAtlasCategoryToneClass(index: number) {
  return atlasToneClassNames[index % atlasToneClassNames.length]
}

/**
 * 获取阅读地图专题卡片色调类名。
 * 作用：为专题卡分配与分类卡错开的色调，形成更平衡的首页色彩节奏。
 *
 * @param index 专题索引
 * @returns 对应的色调类名
 */
function getAtlasTopicToneClass(index: number) {
  return atlasToneClassNames[(index + 2) % atlasToneClassNames.length]
}

useSeoMeta({
  title: () => siteInfo.value?.defaultTitle || '云屿 Yunyu',
  description: () => siteInfo.value?.defaultDescription || '云屿前台首页'
})
</script>

<template>
  <main class="min-h-screen overflow-hidden bg-[linear-gradient(180deg,#f8fbff_0%,#f4f7fb_36%,#ffffff_100%)] text-slate-900 transition-colors duration-300 dark:bg-[linear-gradient(180deg,#030712_0%,#071120_36%,#020617_100%)] dark:text-slate-100">
    <section class="relative overflow-hidden">
      <div v-if="pending" class="space-y-8">
        <USkeleton class="h-[78svh] min-h-[540px] w-full rounded-none" />
      </div>

      <div
        v-else-if="featuredPost"
        class="space-y-10 pb-14 lg:space-y-16 lg:pb-20"
      >
        <YunyuHero :src="featuredPost.coverUrl" :alt="featuredPost.title">
          <p class="text-xs font-semibold uppercase tracking-[0.46em] text-white/76 drop-shadow-md">
            {{ siteInfo?.siteName || 'Yunyu / 云屿' }}
          </p>

          <h1 class="mt-4 max-w-4xl text-[clamp(2.35rem,1.6rem+3vw,4.35rem)] font-semibold leading-[1] tracking-[-0.045em] [font-family:var(--font-display)] [text-wrap:balance] text-white drop-shadow-lg">
            把热爱、情绪与阅读节奏，
            <br class="hidden sm:block" />
            整理成一条可以慢慢逛的阅读路径。
          </h1>

          <p class="mt-4 max-w-[34rem] text-[0.95rem] leading-7 text-white/82 drop-shadow-md sm:text-[1rem]">
            {{ siteInfo?.siteSubTitle || '在二次元场景与情绪里漫游的内容站' }}。首页不再同时解释所有东西，而是先给你一条清晰入口，再带你继续往下读。
          </p>

          <div class="mt-5 flex flex-wrap gap-2">
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
              class="rounded-full border border-white/18 bg-white/10 px-3 py-2 text-xs font-medium text-white/88 backdrop-blur-sm transition duration-200 hover:bg-white/18"
            >
              #{{ tag.name }}
            </NuxtLink>
          </div>

          <div class="mt-6 flex flex-wrap items-center gap-4 text-white/90">
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
                <p class="text-[11px] text-white/66">{{ featuredPost.publishedAt }} · {{ featuredPost.readingMinutes }} 分钟阅读</p>
              </div>
            </div>

            <div class="flex flex-wrap gap-3">
              <span
                v-for="stat in heroStats"
                :key="stat.label"
                class="rounded-full border border-white/16 bg-white/10 px-3 py-1.5 text-[13px] text-white/84 backdrop-blur-sm"
              >
                {{ stat.label }} {{ stat.value }}
              </span>
            </div>
          </div>

          <div class="mt-7 flex flex-wrap gap-3">
            <NuxtLink
              :to="`/posts/${featuredPost.slug}`"
              class="inline-flex items-center gap-2 rounded-full bg-white/92 px-5 py-2.5 text-[13px] font-medium text-slate-900 transition duration-200 hover:bg-white"
            >
              <span>进入主打内容</span>
              <UIcon name="i-lucide-arrow-right" class="size-4" />
            </NuxtLink>
            <NuxtLink
              to="#atlas"
              class="inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-5 py-2.5 text-[13px] font-medium text-white/90 backdrop-blur-sm transition duration-200 hover:bg-white/18"
            >
              <span>先看阅读地图</span>
              <UIcon name="i-lucide-map" class="size-4" />
            </NuxtLink>
          </div>
        </YunyuHero>

        <section id="curated" class="mx-auto max-w-[1360px] px-5 sm:px-8 lg:px-10">
          <YunyuSectionTitle
            eyebrow="精选阅读"
            title="先读这三篇，就能理解云屿现在的气质"
            description="第二屏只负责承接首屏后的第一步点击，不再重复分类、专题和说明模块。"
            size="compact"
            link-label="查看全部文章"
            link-to="/posts"
          />

          <div
            v-if="editorialLeadPost"
            class="mt-7 grid gap-4 lg:grid-cols-[minmax(0,1.05fr)_minmax(0,0.95fr)]"
          >
            <NuxtLink
              :to="`/posts/${editorialLeadPost.slug}`"
              class="group overflow-hidden rounded-[32px] border border-white/60 bg-white/82 shadow-[0_28px_80px_-50px_rgba(15,23,42,0.34)] transition duration-300 hover:-translate-y-0.5 hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/66 dark:hover:border-sky-900"
            >
              <div class="grid h-full gap-0 lg:grid-cols-[0.96fr_1.04fr]">
                <YunyuImage
                  :src="editorialLeadPost.coverUrl"
                  :alt="editorialLeadPost.title"
                  image-class="h-72 w-full object-cover transition duration-500 group-hover:scale-[1.03] lg:h-full"
                  rounded-class="rounded-t-[32px] rounded-b-none lg:rounded-l-[32px] lg:rounded-r-none"
                />

                <div class="flex h-full flex-col justify-center p-5 sm:p-7">
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.26em] text-sky-600 dark:text-sky-300">
                    Editorial Lead
                  </p>
                  <h3 class="mt-3 text-[clamp(1.7rem,1.45rem+0.8vw,2.25rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
                    {{ editorialLeadPost.title }}
                  </h3>
                  <p class="mt-3 text-[0.95rem] leading-7 text-slate-600 dark:text-slate-300">
                    {{ editorialLeadPost.summary }}
                  </p>
                  <div class="mt-4 flex flex-wrap gap-2">
                    <UBadge color="neutral" variant="soft">{{ editorialLeadPost.categoryName }}</UBadge>
                    <NuxtLink
                      v-for="topic in editorialLeadPost.topicItems.slice(0, 2)"
                      :key="`${editorialLeadPost.slug}-${topic.slug}`"
                      :to="`/topics/${topic.slug}`"
                      class="relative z-10"
                      @click.stop
                    >
                      <UBadge color="primary" variant="soft">{{ topic.name }}</UBadge>
                    </NuxtLink>
                  </div>
                  <div class="mt-5 flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.14em] text-slate-500 dark:text-slate-400">
                    <span>{{ editorialLeadPost.authorName }}</span>
                    <span>{{ editorialLeadPost.publishedAt }}</span>
                    <span>{{ editorialLeadPost.readingMinutes }} 分钟阅读</span>
                  </div>
                </div>
              </div>
            </NuxtLink>

            <div class="grid gap-5">
              <NuxtLink
                v-for="post in editorialSidePosts"
                :key="post.slug"
                :to="`/posts/${post.slug}`"
                class="group grid gap-4 rounded-[28px] border border-white/60 bg-white/82 p-4 shadow-[0_22px_60px_-46px_rgba(15,23,42,0.28)] transition duration-300 hover:-translate-y-0.5 hover:border-sky-200 dark:border-white/10 dark:bg-slate-950/66 dark:hover:border-sky-900 sm:grid-cols-[200px_minmax(0,1fr)]"
              >
                <YunyuImage
                  :src="post.coverUrl"
                  :alt="post.title"
                  image-class="h-48 w-full object-cover transition duration-500 group-hover:scale-[1.03] sm:h-full"
                  rounded-class="rounded-[22px]"
                />
                <div class="min-w-0 py-1 sm:pr-2">
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.22em] text-slate-500 dark:text-slate-400">
                    {{ post.categoryName }}
                  </p>
                  <h3 class="mt-2.5 text-[clamp(1.18rem,1.08rem+0.32vw,1.48rem)] font-semibold leading-[1.18] tracking-[-0.025em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
                    {{ post.title }}
                  </h3>
                  <p class="mt-2.5 line-clamp-3 text-[0.92rem] leading-6 text-slate-600 dark:text-slate-300">
                    {{ post.summary }}
                  </p>
                  <div class="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.14em] text-slate-500 dark:text-slate-400">
                    <span>{{ post.authorName }}</span>
                    <span>{{ post.publishedAt }}</span>
                  </div>
                </div>
              </NuxtLink>
            </div>
          </div>
        </section>

        <section id="atlas" class="mx-auto max-w-[1360px] px-5 sm:px-8 lg:px-10">
          <div class="front-atlas-shell p-5 sm:p-6 lg:p-8">
            <div class="relative z-10 grid gap-5 xl:grid-cols-[minmax(0,0.88fr)_minmax(0,1.12fr)]">
              <div class="front-atlas-note-card p-6 sm:p-7">
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.3em] text-orange-600 dark:text-orange-300">
                  Reading Atlas
                </p>
                <h2 class="mt-3 text-[clamp(1.75rem,1.55rem+0.95vw,2.5rem)] font-semibold leading-[1.12] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
                  首页现在只回答一件事：
                  <br class="hidden sm:block" />
                  你应该从哪里开始逛。
                </h2>
                <p class="mt-4 text-[0.95rem] leading-7 text-slate-600 dark:text-slate-300">
                  分类负责理解内容边界，专题负责进入连续阅读。这一屏不再重复堆很多卡片，而是把“怎么进入网站”讲清楚。
                </p>

                <div class="mt-6 rounded-[28px] border border-white/75 bg-white/72 p-5 shadow-[0_20px_50px_-40px_rgba(148,163,184,0.32)] backdrop-blur-md dark:border-white/10 dark:bg-slate-950/54">
                  <p class="text-xs font-semibold uppercase tracking-[0.28em] text-slate-500 dark:text-slate-400">
                    首页节奏
                  </p>
                  <ol class="mt-4 space-y-3 text-sm leading-7 text-slate-700 dark:text-slate-300">
                    <li>1. 首屏给出品牌和主打内容</li>
                    <li>2. 精选阅读承接第一次点击</li>
                    <li>3. 阅读地图告诉用户怎么继续逛</li>
                    <li>4. 最新内容流保留持续更新感</li>
                  </ol>
                </div>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <NuxtLink
                  v-for="(category, index) in atlasCategories"
                  :key="category.slug"
                  :to="`/categories/${category.slug}`"
                  class="front-atlas-card group p-5 transition duration-300 hover:-translate-y-0.5 hover:shadow-[0_28px_68px_-46px_rgba(148,163,184,0.46)]"
                  :class="getAtlasCategoryToneClass(index)"
                >
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-slate-500 dark:text-slate-400">
                    {{ String(category.articleCount).padStart(2, '0') }} 篇内容
                  </p>
                  <h3 class="mt-4 text-[clamp(1.55rem,1.38rem+0.45vw,1.95rem)] font-semibold leading-[1.12] tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                    {{ category.name }}
                  </h3>
                  <p class="mt-3 text-[0.92rem] leading-7 text-slate-600 dark:text-slate-300">
                    {{ category.description }}
                  </p>
                </NuxtLink>

                <NuxtLink
                  v-for="(topic, index) in atlasTopics"
                  :key="topic.slug"
                  :to="`/topics/${topic.slug}`"
                  class="front-atlas-card group p-5 transition duration-300 hover:-translate-y-0.5 hover:shadow-[0_28px_68px_-46px_rgba(148,163,184,0.46)]"
                  :class="getAtlasTopicToneClass(index)"
                >
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-slate-500 dark:text-slate-400">
                    {{ String(topic.articleCount).padStart(2, '0') }} 条路线
                  </p>
                  <h3 class="mt-4 text-[clamp(1.55rem,1.38rem+0.45vw,1.95rem)] font-semibold leading-[1.12] tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                    {{ topic.name }}
                  </h3>
                  <p class="mt-3 text-[0.92rem] leading-7 text-slate-600 dark:text-slate-300">
                    {{ topic.summary }}
                  </p>
                </NuxtLink>
              </div>
            </div>
          </div>
        </section>

        <section id="stream" class="mx-auto max-w-[1360px] px-5 sm:px-8 lg:px-10">
          <YunyuSectionTitle
            eyebrow="最新内容"
            title="把更新做成一条继续延伸的内容流"
            description="首页尾段只保留一个主条目和少量最新内容，让阅读在首屏之后继续，而不是再次进入信息堆叠。"
            size="compact"
            link-label="查看全部文章"
            link-to="/posts"
          />

          <div
            v-if="latestLeadPost"
            class="mt-7 grid gap-8 xl:grid-cols-[minmax(0,1.06fr)_minmax(0,0.94fr)]"
          >
            <NuxtLink
              :to="`/posts/${latestLeadPost.slug}`"
              class="group grid gap-6 border-b border-slate-200/75 pb-8 transition duration-300 dark:border-white/10 lg:grid-cols-[minmax(0,1.02fr)_320px]"
            >
              <div class="min-w-0">
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-sky-600 dark:text-sky-300">
                  Latest Feature
                </p>
                <h3 class="mt-3 text-[clamp(1.8rem,1.55rem+0.95vw,2.45rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition duration-200 group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                  {{ latestLeadPost.title }}
                </h3>
                <p class="mt-3 max-w-3xl text-[0.95rem] leading-7 text-slate-600 dark:text-slate-300">
                  {{ latestLeadPost.summary }}
                </p>
                <div class="mt-4 flex flex-wrap gap-2">
                  <UBadge color="neutral" variant="soft">{{ latestLeadPost.categoryName }}</UBadge>
                  <NuxtLink
                    v-for="topic in latestLeadPost.topicItems.slice(0, 2)"
                    :key="`${latestLeadPost.slug}-${topic.slug}`"
                    :to="`/topics/${topic.slug}`"
                    class="relative z-10"
                    @click.stop
                  >
                    <UBadge color="primary" variant="soft">{{ topic.name }}</UBadge>
                  </NuxtLink>
                </div>
                <div class="mt-5 flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.14em] text-slate-500 dark:text-slate-400">
                  <span>{{ latestLeadPost.authorName }}</span>
                  <span>{{ latestLeadPost.publishedAt }}</span>
                  <span>{{ latestLeadPost.readingMinutes }} 分钟阅读</span>
                  <span>{{ latestLeadPost.viewCount }} 浏览</span>
                </div>
              </div>

              <YunyuImage
                :src="latestLeadPost.coverUrl"
                :alt="latestLeadPost.title"
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
                :show-tags="false"
                title-class="text-[clamp(1.3rem,1.15rem+0.45vw,1.65rem)] leading-[1.14] tracking-[-0.028em]"
                summary-class="text-[0.92rem] leading-7"
                image-height-class="h-52"
                root-class="sm:grid-cols-[200px_minmax(0,1fr)]"
              />
            </div>
          </div>
        </section>
      </div>

      <div
        v-else-if="error"
        class="mx-auto max-w-[1360px] px-5 py-20 sm:px-8 lg:px-10"
      >
        <div class="rounded-[28px] border border-rose-200 bg-rose-50/80 p-6 text-sm text-rose-600 dark:border-rose-900/60 dark:bg-rose-950/30 dark:text-rose-300">
          {{ error.message }}
        </div>
      </div>
    </section>
  </main>
</template>
