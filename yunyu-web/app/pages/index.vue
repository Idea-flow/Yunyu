<script setup lang="ts">
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'
import { formatChineseDate } from '~/utils/date'
import type { HomePageConfig, HomePageHeroVisual, SitePostSummary } from '../types/site'

/**
 * 前台首页。
 * 作用：以更克制的品牌首屏承接首页主张，再用杂志化的内容编排展示最新文章与探索入口，
 * 让个人博客首页更像作品首页，而不是信息堆叠页。
 */
const siteContent = useSiteContent()

const { data, pending, error } = await useAsyncData('site-home', async () => {
  return await siteContent.getHome()
})

const homeData = computed(() => data.value)
const featuredPosts = computed(() => homeData.value?.featuredPosts || [])
const latestPosts = computed(() => homeData.value?.latestPosts || [])
const categories = computed(() => homeData.value?.categories || [])
const topics = computed(() => homeData.value?.topics || [])
const siteInfo = computed(() => homeData.value?.siteInfo)
const homepageConfig = computed<HomePageConfig | null>(() => homeData.value?.homepageConfig || null)
const heroVisual = computed<HomePageHeroVisual | null>(() => homeData.value?.heroVisual || null)

/**
 * 首页首屏眉题。
 * 作用：优先读取后台首页配置中的品牌眉题，未配置时回退到站点名称。
 */
const heroEyebrow = computed(() => homepageConfig.value?.heroEyebrow || siteInfo.value?.siteName || 'Yunyu / 云屿')

/**
 * 首页首屏标题。
 * 作用：统一读取首页品牌首屏主标题，保证首屏不再依赖具体文章封面。
 */
const heroTitle = computed(() => homepageConfig.value?.heroTitle || '把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站')

/**
 * 首页首屏副标题。
 * 作用：统一读取首页品牌首屏说明文案，作为标题下方的补充说明。
 */
const heroSubtitle = computed(() => homepageConfig.value?.heroSubtitle || siteInfo.value?.siteSubTitle || '')

/**
 * 首页首屏品牌大字。
 * 作用：生成首屏背景中的低透明度品牌文字，让首屏在无封面图的情况下仍有记忆点。
 */
const heroBrandMark = computed(() => {
  return (siteInfo.value?.siteName || 'YUNYU').slice(0, 12)
})

/**
 * 首页首屏关键词。
 * 作用：当后台开启关键词展示时，在首屏中展示少量主题方向。
 */
const heroKeywords = computed(() => {
  if (!homepageConfig.value?.showHeroKeywords) {
    return []
  }

  return (homepageConfig.value?.heroKeywords || []).slice(0, 5)
})

/**
 * 首页首屏统计项。
 * 作用：当后台开启轻量统计时，在首屏底部展示少量站点规模信息。
 */
const heroStats = computed(() => {
  if (!homepageConfig.value?.showHeroStats) {
    return []
  }

  return (homepageConfig.value?.heroStats || []).slice(0, 4)
})

/**
 * 首页首屏最近更新。
 * 作用：在首屏按钮下方补充少量最新文章入口，让首页在不堆卡片的前提下更有内容感。
 */
const heroRecentPosts = computed(() => {
  const preferredPosts = latestDisplayPosts.value.length > 0 ? latestDisplayPosts.value : featuredPosts.value

  return preferredPosts.slice(0, 2)
})

/**
 * 首页首屏统计展示项。
 * 作用：统一承接后台配置的首屏统计数据，避免模板层直接依赖原始配置字段。
 */
const heroStatItems = computed(() => {
  return heroStats.value
})

/**
 * 首页最新文章展示列表。
 * 作用：首页正文只保留少量最新文章，避免内容过多导致首页显得杂乱。
 */
const latestDisplayPosts = computed(() => latestPosts.value.slice(0, 4))

/**
 * 首页编辑主文章。
 * 作用：将最新内容中的第一篇文章作为正文主入口，承担首页进入阅读的第一视觉焦点。
 */
const editorialLeadPost = computed<SitePostSummary | null>(() => {
  return latestDisplayPosts.value[0] || featuredPosts.value[0] || null
})

/**
 * 首页编辑次文章列表。
 * 作用：承接主文章右侧的阅读列表，让首页正文保持紧凑但不空。
 */
const editorialSidePosts = computed(() => {
  if (latestDisplayPosts.value.length > 1) {
    return latestDisplayPosts.value.slice(1, 4)
  }

  return featuredPosts.value
    .filter(post => post.slug !== editorialLeadPost.value?.slug)
    .slice(0, 3)
})

/**
 * 首页精选文章列表。
 * 作用：在正文下半部分保留少量“继续阅读”入口，补充更具主题性的内容。
 */
const selectedPosts = computed(() => {
  const excludedSlugs = new Set([
    editorialLeadPost.value?.slug,
    ...editorialSidePosts.value.map(post => post.slug)
  ].filter(Boolean))

  return featuredPosts.value.filter(post => !excludedSlugs.has(post.slug)).slice(0, 3)
})

/**
 * 首页首屏默认视频地址。
 * 作用：当后台未配置视觉文章，或目标文章既无视频也无封面图时，回退到默认视觉素材。
 */
const defaultHeroVideoUrl = 'https://s3.hi168.com/hi168-29272-3320gqns/%E2%80%9C%E8%B6%81%E5%A4%A9%E7%A9%BA%E4%B8%8D%E6%B3%A8%E6%84%8F%EF%BC%8C%E5%81%B7%E4%B8%80%E7%82%B9%E4%BA%91%E6%9C%B5%E9%80%81%E7%BB%99%E4%BD%A0%EF%BD%9E%E2%80%9D.mp4'

/**
 * 首页首屏是否使用配置视频。
 * 作用：控制右侧主视觉优先展示后台配置文章的视频资源。
 */
const heroVisualHasVideo = computed(() => {
  return heroVisual.value?.mediaType === 'video' && Boolean(heroVisual.value.videoUrl)
})

/**
 * 首页首屏是否使用配置图片。
 * 作用：在没有视频时回退为文章封面图展示。
 */
const heroVisualHasImage = computed(() => {
  return heroVisual.value?.mediaType === 'image' && Boolean(heroVisual.value.imageUrl)
})

/**
 * 首页首屏视频地址。
 * 作用：统一返回首页右侧主视觉视频地址，优先取配置视频，兜底使用默认素材。
 */
const heroVideoUrl = computed(() => {
  if (heroVisualHasVideo.value) {
    return heroVisual.value?.videoUrl || defaultHeroVideoUrl
  }

  return defaultHeroVideoUrl
})

/**
 * 首页首屏图片地址。
 * 作用：当配置文章只有封面图时，为右侧主视觉返回图片地址。
 */
const heroImageUrl = computed(() => {
  return heroVisualHasImage.value ? heroVisual.value?.imageUrl || '' : ''
})

/**
 * 首页首屏视觉块跳转地址。
 * 作用：当后台开启整块点击时，为右侧主视觉提供文章详情跳转。
 */
const heroVisualLink = computed(() => {
  if (!heroVisual.value?.clickable || !heroVisual.value?.postSlug) {
    return ''
  }

  return getPostLink(heroVisual.value.postSlug)
})

/**
 * 首页首屏视觉块辅助文案。
 * 作用：在右侧主视觉下方保留一条轻量说明，优先显示视觉文章标题。
 */
const heroVisualCaption = computed(() => {
  return heroVisual.value?.postTitle || heroKeywords.value.slice(0, 2).join(' / ') || '写作 / 观察'
})

/**
 * 计算首页分类入口。
 * 作用：首页探索区仅展示少量高频分类入口，保持版面清爽。
 */
const categoryEntries = computed(() => categories.value.slice(0, 5))

/**
 * 计算首页专题入口。
 * 作用：首页探索区仅展示少量专题入口，避免与文章流互相争夺注意力。
 */
const topicEntries = computed(() => topics.value.slice(0, 5))

/**
 * 是否显示最新文章区。
 * 作用：根据后台首页配置控制正文主阅读区显隐，并避免空数据时出现空白区块。
 */
const showLatestSection = computed(() => {
  return homepageConfig.value?.showLatestSection !== false
    && Boolean(editorialLeadPost.value || editorialSidePosts.value.length)
})

/**
 * 是否显示精选文章区。
 * 作用：根据后台首页配置控制继续阅读区显隐，并避免与最新文章重复展示。
 */
const showFeaturedSection = computed(() => {
  return homepageConfig.value?.showFeaturedSection !== false && selectedPosts.value.length > 0
})

/**
 * 是否显示分类区。
 * 作用：根据后台首页配置控制分类区显隐，并避免空数据时出现空白区块。
 */
const showCategorySection = computed(() => {
  return homepageConfig.value?.showCategorySection !== false && categoryEntries.value.length > 0
})

/**
 * 是否显示专题区。
 * 作用：根据后台首页配置控制专题区显隐，并避免空数据时出现空白区块。
 */
const showTopicSection = computed(() => {
  return homepageConfig.value?.showTopicSection !== false && topicEntries.value.length > 0
})

/**
 * 首页首屏背景样式。
 * 作用：根据后台配置切换首屏背景模式，形成稳定但可切换的品牌氛围。
 */
const heroBackgroundClass = computed(() => {
  const backgroundMode = homepageConfig.value?.heroBackgroundMode || 'gradient-grid'

  if (backgroundMode === 'soft-glow') {
    return 'bg-[radial-gradient(circle_at_16%_20%,rgba(56,189,248,0.18),transparent_24%),radial-gradient(circle_at_84%_22%,rgba(249,115,22,0.16),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef2ff_48%,#f8fafc_100%)] dark:bg-[radial-gradient(circle_at_16%_20%,rgba(56,189,248,0.16),transparent_24%),radial-gradient(circle_at_84%_22%,rgba(249,115,22,0.12),transparent_24%),linear-gradient(180deg,#020617_0%,#0f172a_52%,#020617_100%)]'
  }

  if (backgroundMode === 'minimal-lines') {
    return 'bg-[linear-gradient(180deg,rgba(250,250,249,0.98),rgba(248,250,252,0.98)),linear-gradient(90deg,rgba(148,163,184,0.11)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.11)_1px,transparent_1px)] bg-[size:auto,34px_34px,34px_34px] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.99),rgba(2,6,23,0.99)),linear-gradient(90deg,rgba(71,85,105,0.2)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.2)_1px,transparent_1px)] dark:bg-[size:auto,34px_34px,34px_34px]'
  }

  if (backgroundMode === 'keyword-cloud') {
    return 'bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.14),transparent_24%),radial-gradient(circle_at_bottom_right,rgba(249,115,22,0.12),transparent_26%),linear-gradient(180deg,#f8fafc_0%,#f1f5f9_100%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.12),transparent_24%),radial-gradient(circle_at_bottom_right,rgba(249,115,22,0.1),transparent_26%),linear-gradient(180deg,#020617_0%,#0f172a_100%)]'
  }

  return 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef2ff_42%,#f8fafc_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0f172a_46%,#020617_100%)]'
})

/**
 * 首页首屏辅助氛围层样式。
 * 作用：为不同背景模式补充柔和的发光、网格或关键词层次，让首屏不需要封面图也有辨识度。
 */
const heroAccentClass = computed(() => {
  const backgroundMode = homepageConfig.value?.heroBackgroundMode || 'gradient-grid'

  if (backgroundMode === 'soft-glow') {
    return 'before:absolute before:-left-12 before:top-20 before:h-56 before:w-56 before:rounded-full before:bg-sky-300/20 before:blur-3xl before:content-[\"\"] after:absolute after:right-0 after:top-28 after:h-48 after:w-48 after:rounded-full after:bg-orange-300/18 after:blur-3xl after:content-[\"\"] dark:before:bg-sky-500/14 dark:after:bg-orange-400/10'
  }

  if (backgroundMode === 'minimal-lines') {
    return 'before:absolute before:inset-x-0 before:top-0 before:h-px before:bg-slate-200/70 before:content-[\"\"] after:absolute after:bottom-0 after:left-0 after:h-px after:w-full after:bg-slate-200/70 after:content-[\"\"] dark:before:bg-white/10 dark:after:bg-white/10'
  }

  if (backgroundMode === 'keyword-cloud') {
    return 'before:absolute before:left-[8%] before:top-[18%] before:text-[clamp(2.6rem,4vw,5rem)] before:font-semibold before:tracking-[-0.06em] before:text-slate-200/65 before:content-[\"Writing\"] after:absolute after:right-[8%] after:bottom-[18%] after:text-[clamp(2.2rem,3.5vw,4rem)] after:font-semibold after:tracking-[-0.06em] after:text-slate-200/55 after:content-[\"Archive\"] dark:before:text-white/6 dark:after:text-white/5'
  }

  return 'before:absolute before:inset-0 before:bg-[linear-gradient(90deg,rgba(148,163,184,0.14)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.14)_1px,transparent_1px)] before:bg-[size:30px_30px] before:content-[\"\"] before:[mask-image:linear-gradient(180deg,rgba(255,255,255,0.84),transparent)] after:absolute after:left-[10%] after:top-20 after:h-44 after:w-44 after:rounded-full after:bg-sky-300/16 after:blur-3xl after:content-[\"\"] dark:before:bg-[linear-gradient(90deg,rgba(71,85,105,0.25)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.25)_1px,transparent_1px)] dark:after:bg-sky-400/10'
})

/**
 * 首页首屏主按钮文案。
 * 作用：统一读取后台首页配置主按钮文案。
 */
const primaryActionText = computed(() => homepageConfig.value?.heroPrimaryButtonText || '查看文章')

/**
 * 首页首屏主按钮地址。
 * 作用：统一读取后台首页配置主按钮跳转地址。
 */
const primaryActionLink = computed(() => homepageConfig.value?.heroPrimaryButtonLink || '/posts')

/**
 * 首页首屏次按钮文案。
 * 作用：统一读取后台首页配置次按钮文案，未配置时自动隐藏次按钮。
 */
const secondaryActionText = computed(() => homepageConfig.value?.heroSecondaryButtonText || '')

/**
 * 首页首屏次按钮地址。
 * 作用：统一读取后台首页配置次按钮跳转地址。
 */
const secondaryActionLink = computed(() => homepageConfig.value?.heroSecondaryButtonLink || '/topics')

/**
 * 首屏是否显示次按钮。
 * 作用：保证后台未配置次按钮时页面不出现空按钮。
 */
const showSecondaryAction = computed(() => Boolean(secondaryActionText.value && secondaryActionLink.value))

useSeoMeta({
  title: () => siteInfo.value?.defaultTitle || '云屿 Yunyu',
  description: () => siteInfo.value?.defaultDescription || '云屿前台首页'
})

/**
 * 返回文章详情页链接。
 * 作用：统一生成文章详情页地址。
 *
 * @param slug 文章 slug
 * @returns 文章详情页地址
 */
function getPostLink(slug: string) {
  return `/posts/${slug}`
}

/**
 * 返回分类详情页链接。
 * 作用：统一生成分类详情页地址。
 *
 * @param slug 分类 slug
 * @returns 分类详情页地址
 */
function getCategoryLink(slug: string) {
  return `/categories/${slug}`
}

/**
 * 返回专题详情页链接。
 * 作用：统一生成专题详情页地址。
 *
 * @param slug 专题 slug
 * @returns 专题详情页地址
 */
function getTopicLink(slug: string) {
  return `/topics/${slug}`
}

</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fafc_0%,#f8fafc_100%)] text-slate-900 transition-colors duration-300 dark:bg-[linear-gradient(180deg,#020617_0%,#020617_100%)] dark:text-slate-100">
    <section v-if="pending" class="space-y-8">
      <USkeleton class="h-[72svh] min-h-[520px] w-full rounded-none" />
    </section>

    <section
      v-else-if="homeData && homepageConfig?.heroEnabled !== false"
      class="relative isolate min-h-screen overflow-hidden"
      :class="heroBackgroundClass"
    >
      <div class="pointer-events-none absolute inset-0" :class="heroAccentClass" />

      <div
        aria-hidden="true"
        class="pointer-events-none absolute inset-x-0 top-1/2 hidden -translate-y-1/2 lg:block"
      >
        <div class="mx-auto max-w-[1320px] px-10 text-right text-[clamp(7rem,17vw,16rem)] font-semibold leading-none tracking-[-0.08em] text-slate-950/[0.04] [font-family:var(--font-display)] dark:text-white/[0.03]">
          {{ heroBrandMark }}
        </div>
      </div>

      <div class="relative mx-auto flex min-h-screen max-w-[1320px] items-stretch px-5 pb-8 pt-[5.5rem] sm:px-8 sm:pt-24 lg:px-10 lg:pb-10 lg:pt-[6.5rem]">
        <div class="flex w-full flex-col justify-between gap-10">
          <div class="grid gap-10 lg:grid-cols-[minmax(0,1fr)_minmax(420px,520px)] lg:items-center lg:gap-12">
            <div class="max-w-[720px]">
              <p class="text-[0.67rem] font-semibold uppercase tracking-[0.38em] text-slate-500 dark:text-slate-400">
                {{ heroEyebrow }}
              </p>

              <h1 class="mt-4 max-w-[11ch] text-[clamp(1.92rem,1.48rem+1.36vw,2.95rem)] font-semibold leading-[0.98] tracking-[-0.055em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] dark:text-white">
                {{ heroTitle }}
              </h1>

              <p class="mt-5 max-w-[33rem] text-[0.9rem] leading-8 tracking-[-0.01em] text-slate-600 dark:text-slate-300">
                {{ heroSubtitle }}
              </p>

              <div class="mt-6 flex flex-wrap gap-3">
                <NuxtLink
                  :to="primaryActionLink"
                  class="inline-flex items-center gap-2 rounded-full bg-slate-950 px-5 py-2.5 text-sm font-medium text-white transition duration-200 hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
                >
                  <span>{{ primaryActionText }}</span>
                  <UIcon name="i-lucide-arrow-right" class="size-4" />
                </NuxtLink>

                <NuxtLink
                  v-if="showSecondaryAction"
                  :to="secondaryActionLink"
                  class="inline-flex items-center gap-2 rounded-full border border-slate-300/85 bg-white/72 px-5 py-2.5 text-sm font-medium text-slate-700 backdrop-blur-sm transition duration-200 hover:border-slate-400 hover:bg-white dark:border-white/14 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10"
                >
                  <span>{{ secondaryActionText }}</span>
                  <UIcon name="i-lucide-arrow-up-right" class="size-4" />
                </NuxtLink>
              </div>

              <div
                v-if="heroRecentPosts.length > 0"
                class="mt-7 max-w-[34rem]"
              >
                <div class="flex items-center gap-4">
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.28em] text-slate-400 dark:text-slate-500">
                    最近更新
                  </p>
                  <NuxtLink
                    to="/posts"
                    class="text-[0.72rem] text-slate-400 transition hover:text-slate-700 dark:text-slate-500 dark:hover:text-slate-300"
                  >
                    查看全部
                  </NuxtLink>
                </div>

                <div class="mt-3 space-y-2.5">
                  <NuxtLink
                    v-for="post in heroRecentPosts"
                    :key="`hero-${post.slug}`"
                    :to="getPostLink(post.slug)"
                    class="group flex items-center justify-between gap-3 border-b border-slate-200/70 py-2 last:border-b-0 dark:border-white/10"
                  >
                    <div class="min-w-0">
                      <p class="truncate text-sm font-medium text-slate-700 transition group-hover:text-slate-950 dark:text-slate-200 dark:group-hover:text-white">
                        {{ post.title }}
                      </p>
                    </div>

                    <div class="flex shrink-0 items-center gap-3 text-[0.72rem] text-slate-400 dark:text-slate-500">
                      <span>{{ formatChineseDate(post.publishedAt) }}</span>
                      <UIcon
                        name="i-lucide-arrow-up-right"
                        class="size-3.5 transition group-hover:text-slate-700 dark:group-hover:text-slate-300"
                      />
                    </div>
                  </NuxtLink>
                </div>
              </div>
            </div>

            <div class="w-full max-w-[520px] justify-self-start lg:justify-self-end">
              <NuxtLink
                v-if="heroVisualLink"
                :to="heroVisualLink"
                class="group block cursor-pointer rounded-[36px] border border-white/70 bg-white/52 p-2 shadow-[0_34px_90px_-54px_rgba(15,23,42,0.38)] backdrop-blur-md transition duration-300 hover:-translate-y-0.5 hover:shadow-[0_38px_98px_-54px_rgba(15,23,42,0.42)] dark:border-white/10 dark:bg-white/[0.04]"
              >
                <div class="overflow-hidden rounded-[30px] bg-slate-100 dark:bg-slate-900">
                  <video
                    v-if="heroVisualHasVideo"
                    :src="heroVideoUrl"
                    :poster="heroVisual?.imageUrl || undefined"
                    class="aspect-[16/11] h-full w-full object-cover transition duration-500 group-hover:scale-[1.015]"
                    autoplay
                    muted
                    loop
                    playsinline
                    preload="metadata"
                  />
                  <img
                    v-else-if="heroVisualHasImage"
                    :src="heroImageUrl"
                    :alt="heroVisual?.postTitle || heroTitle"
                    class="aspect-[16/11] h-full w-full object-cover transition duration-500 group-hover:scale-[1.015]"
                  >
                  <video
                    v-else
                    :src="heroVideoUrl"
                    class="aspect-[16/11] h-full w-full object-cover transition duration-500 group-hover:scale-[1.015]"
                    autoplay
                    muted
                    loop
                    playsinline
                    preload="metadata"
                  />
                </div>
              </NuxtLink>

              <div
                v-else
                class="rounded-[36px] border border-white/70 bg-white/52 p-2 shadow-[0_34px_90px_-54px_rgba(15,23,42,0.38)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]"
              >
                <div class="overflow-hidden rounded-[30px] bg-slate-100 dark:bg-slate-900">
                  <video
                    v-if="heroVisualHasVideo"
                    :src="heroVideoUrl"
                    :poster="heroVisual?.imageUrl || undefined"
                    class="aspect-[16/11] h-full w-full object-cover"
                    autoplay
                    muted
                    loop
                    playsinline
                    preload="metadata"
                  />
                  <img
                    v-else-if="heroVisualHasImage"
                    :src="heroImageUrl"
                    :alt="heroVisual?.postTitle || heroTitle"
                    class="aspect-[16/11] h-full w-full object-cover"
                  >
                  <video
                    v-else
                    :src="heroVideoUrl"
                    class="aspect-[16/11] h-full w-full object-cover"
                    autoplay
                    muted
                    loop
                    playsinline
                    preload="metadata"
                  />
                </div>
              </div>

              <div class="mt-3 flex items-center justify-between gap-3 px-1 text-[0.68rem] uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
                <span>视觉片段</span>
                <span class="truncate text-right">{{ heroVisualCaption }}</span>
              </div>
            </div>
          </div>

          <div v-if="heroStatItems.length > 0" class="mt-10 lg:mt-12">
            <div class="flex justify-start lg:justify-end">
              <div>
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.28em] text-slate-400 dark:text-slate-500">
                  站点体量
                </p>
                <dl class="mt-3 flex flex-wrap gap-x-6 gap-y-3">
                  <div
                    v-for="stat in heroStatItems"
                    :key="`${stat.label}-${stat.value}`"
                    class="min-w-[4.5rem]"
                  >
                    <dt class="text-[0.68rem] uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">
                      {{ stat.label }}
                    </dt>
                    <dd class="mt-1.5 text-[1.05rem] font-semibold tracking-[-0.04em] text-slate-950 dark:text-white">
                      {{ stat.value }}
                    </dd>
                  </div>
                </dl>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section
      v-else-if="homeData"
      class="mx-auto max-w-[1240px] px-5 py-20 sm:px-8 lg:px-10"
    >
      <div class="max-w-[760px]">
        <p class="text-[0.69rem] font-semibold uppercase tracking-[0.36em] text-slate-500 dark:text-slate-400">
          {{ heroEyebrow }}
        </p>
        <h1 class="mt-5 text-[clamp(2.6rem,1.9rem+2.6vw,4.8rem)] font-semibold leading-[0.95] tracking-[-0.065em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] dark:text-white">
          {{ heroTitle }}
        </h1>
        <p class="mt-5 max-w-[38rem] text-[0.94rem] leading-8 text-slate-600 dark:text-slate-300">
          {{ heroSubtitle }}
        </p>
      </div>
    </section>

    <section
      v-if="homeData"
      class="mx-auto max-w-[1240px] space-y-16 px-5 py-12 sm:px-8 lg:space-y-20 lg:px-10 lg:py-16"
    >
      <section v-if="showLatestSection" id="latest">
        <YunyuSectionTitle
          eyebrow="Latest"
          :title="homepageConfig?.latestSectionTitle || '最新文章'"
          description="从最近一次更新开始进入阅读，首页只保留最值得先看到的几篇。"
          link-label="查看全部"
          link-to="/posts"
          size="compact"
          tone="slate"
        />

        <div class="mt-10 grid gap-10 lg:grid-cols-[minmax(0,1.04fr)_minmax(320px,0.88fr)]">
          <NuxtLink
            v-if="editorialLeadPost"
            :to="getPostLink(editorialLeadPost.slug)"
            class="group block border-b border-slate-200/80 pb-8 transition dark:border-white/10 lg:border-b-0 lg:pb-0"
          >
            <YunyuImage
              :src="editorialLeadPost.coverUrl"
              :alt="editorialLeadPost.title"
              wrapper-class="aspect-video"
              image-class="h-full w-full object-cover transition duration-500 group-hover:scale-[1.02]"
              rounded-class="rounded-[30px]"
            />

            <div class="min-w-0 mt-6">
              <div class="flex flex-wrap items-center gap-2">
                <UBadge color="neutral" variant="soft">{{ editorialLeadPost.categoryName }}</UBadge>
                <UBadge
                  v-for="topic in editorialLeadPost.topicItems.slice(0, 1)"
                  :key="`${editorialLeadPost.slug}-${topic.slug}`"
                  color="primary"
                  variant="soft"
                >
                  {{ topic.name }}
                </UBadge>
              </div>

              <h2 class="mt-5 max-w-[14ch] text-[clamp(2rem,1.5rem+1.2vw,2.9rem)] font-semibold leading-[1.02] tracking-[-0.05em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
                {{ editorialLeadPost.title }}
              </h2>

              <p class="mt-4 max-w-[40rem] text-[0.95rem] leading-8 text-slate-600 dark:text-slate-300">
                {{ editorialLeadPost.summary }}
              </p>

              <div class="mt-6 flex flex-wrap gap-x-5 gap-y-2 text-[0.7rem] uppercase tracking-[0.18em] text-slate-500 dark:text-slate-400">
                <span>{{ editorialLeadPost.authorName }}</span>
                <span>{{ formatChineseDate(editorialLeadPost.publishedAt) }}</span>
                <span>{{ editorialLeadPost.readingMinutes }} 分钟阅读</span>
                <span>{{ editorialLeadPost.viewCount }} 浏览</span>
              </div>
            </div>
          </NuxtLink>

          <div class="space-y-5 border-t border-slate-200/80 pt-1 dark:border-white/10 lg:border-t-0 lg:pt-0">
            <NuxtLink
              v-for="post in editorialSidePosts"
              :key="post.slug"
              :to="getPostLink(post.slug)"
              class="group grid gap-4 border-b border-slate-200/75 pb-5 transition last:border-b-0 last:pb-0 dark:border-white/10 sm:grid-cols-[160px_minmax(0,1fr)]"
            >
              <YunyuImage
                :src="post.coverUrl"
                :alt="post.title"
                wrapper-class="aspect-video"
                image-class="h-full w-full object-cover transition duration-500 group-hover:scale-[1.02]"
                rounded-class="rounded-[22px]"
              />

              <div class="min-w-0">
                <div class="flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
                  <span>{{ post.categoryName }}</span>
                  <span>{{ formatChineseDate(post.publishedAt) }}</span>
                </div>

                <h3 class="mt-3 text-[1.12rem] font-semibold leading-7 tracking-[-0.03em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
                  {{ post.title }}
                </h3>

                <p class="mt-2 line-clamp-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                  {{ post.summary }}
                </p>

                <div class="mt-4 flex items-center gap-4 text-[0.72rem] text-slate-500 dark:text-slate-400">
                  <span>{{ post.readingMinutes }} 分钟阅读</span>
                  <span>{{ post.viewCount }} 浏览</span>
                </div>
              </div>
            </NuxtLink>
          </div>
        </div>
      </section>

      <section v-if="showCategorySection || showTopicSection" id="explore" class="border-t border-slate-200/80 pt-16 dark:border-white/10">
        <YunyuSectionTitle
          eyebrow="Explore"
          title="从结构继续进入"
          description="当你不想按时间浏览时，可以直接从分类与专题进入更稳定的内容线索。"
          size="compact"
          tone="slate"
        />

        <div class="mt-10 grid gap-12 lg:grid-cols-2">
          <div v-if="showCategorySection">
            <div class="flex items-center justify-between gap-4">
              <h3 class="text-[1.08rem] font-semibold tracking-[-0.03em] text-slate-950 [font-family:var(--font-display)] dark:text-slate-50">
                {{ homepageConfig?.categorySectionTitle || '分类' }}
              </h3>
              <NuxtLink to="/categories" class="text-sm text-slate-500 transition hover:text-slate-950 dark:text-slate-400 dark:hover:text-white">
                查看全部
              </NuxtLink>
            </div>

            <div class="mt-5 space-y-4">
              <NuxtLink
                v-for="category in categoryEntries"
                :key="category.slug"
                :to="getCategoryLink(category.slug)"
                class="group block border-b border-slate-200/75 pb-4 transition last:border-b-0 last:pb-0 dark:border-white/10"
              >
                <div class="flex items-start justify-between gap-4">
                  <h4 class="text-[1rem] font-medium tracking-[-0.02em] text-slate-950 transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
                    {{ category.name }}
                  </h4>
                  <span class="shrink-0 text-[0.72rem] uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">
                    {{ category.articleCount }} 篇
                  </span>
                </div>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                  {{ category.description || '从这个分类进入，浏览同一方向下的全部内容。' }}
                </p>
              </NuxtLink>
            </div>
          </div>

          <div v-if="showTopicSection">
            <div class="flex items-center justify-between gap-4">
              <h3 class="text-[1.08rem] font-semibold tracking-[-0.03em] text-slate-950 [font-family:var(--font-display)] dark:text-slate-50">
                {{ homepageConfig?.topicSectionTitle || '专题' }}
              </h3>
              <NuxtLink to="/topics" class="text-sm text-slate-500 transition hover:text-slate-950 dark:text-slate-400 dark:hover:text-white">
                查看全部
              </NuxtLink>
            </div>

            <div class="mt-5 space-y-4">
              <NuxtLink
                v-for="topic in topicEntries"
                :key="topic.slug"
                :to="getTopicLink(topic.slug)"
                class="group block border-b border-slate-200/75 pb-4 transition last:border-b-0 last:pb-0 dark:border-white/10"
              >
                <div class="flex items-start justify-between gap-4">
                  <h4 class="text-[1rem] font-medium tracking-[-0.02em] text-slate-950 transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
                    {{ topic.name }}
                  </h4>
                  <span class="shrink-0 text-[0.72rem] uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">
                    {{ topic.articleCount }} 篇
                  </span>
                </div>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                  {{ topic.summary || '围绕同一主题持续整理的专题内容。' }}
                </p>
              </NuxtLink>
            </div>
          </div>
        </div>
      </section>

      <section v-if="showFeaturedSection" class="border-t border-slate-200/80 pt-16 dark:border-white/10">
        <YunyuSectionTitle
          eyebrow="Selected"
          :title="homepageConfig?.featuredSectionTitle || '继续阅读'"
          description="首页不再堆太多内容，只留下几篇适合继续读下去的文章。"
          link-label="查看全部"
          link-to="/posts"
          size="compact"
          tone="slate"
        />

        <div class="mt-10 grid gap-8 lg:grid-cols-3">
          <NuxtLink
            v-for="post in selectedPosts"
            :key="post.slug"
            :to="getPostLink(post.slug)"
            class="group block border-t border-slate-200/80 pt-5 transition dark:border-white/10"
          >
            <div class="flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
              <span>{{ post.categoryName }}</span>
              <span>{{ formatChineseDate(post.publishedAt) }}</span>
            </div>

            <h3 class="mt-4 text-[1.22rem] font-semibold leading-7 tracking-[-0.035em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
              {{ post.title }}
            </h3>

            <p class="mt-3 line-clamp-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
              {{ post.summary }}
            </p>

            <div class="mt-4 flex items-center gap-4 text-[0.72rem] text-slate-500 dark:text-slate-400">
              <span>{{ post.readingMinutes }} 分钟阅读</span>
              <span>{{ post.viewCount }} 浏览</span>
            </div>
          </NuxtLink>
        </div>
      </section>
    </section>

    <section
      v-else-if="error"
      class="mx-auto max-w-[1240px] px-5 py-20 sm:px-8 lg:px-10"
    >
      <div class="rounded-[24px] border border-rose-200 bg-rose-50/80 p-6 text-sm text-rose-600 dark:border-rose-900/60 dark:bg-rose-950/30 dark:text-rose-300">
        {{ error.message }}
      </div>
    </section>
  </main>
</template>
