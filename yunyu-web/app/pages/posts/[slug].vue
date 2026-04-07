<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted } from 'vue'
import { formatChineseDate } from '~/utils/date'
import type { ArticleTocItem } from '../../types/post'
import ArticleContentRenderer from '../../components/content/ArticleContentRenderer.vue'
import ArticleCommentPanel from '../../components/content/ArticleCommentPanel.vue'
import ArticleTocTree from '../../components/content/ArticleTocTree.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'

/**
 * 前台文章详情页。
 * 作用：接入后端真实公开接口，展示文章正文、目录与相关推荐内容。
 */
const route = useRoute()
const siteContent = useSiteContent()
const colorMode = useColorMode()
const activeTocId = ref('')
const readingProgress = ref(0)
const mobileTocOpen = ref(false)
const mobileTocVisible = ref(false)
const lastWindowScrollTop = ref(0)
const articleContentRef = ref<HTMLElement | null>(null)
const tocScrollContainerRef = ref<HTMLElement | null>(null)
let tocObserver: IntersectionObserver | null = null

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
 * 判断文章详情是否存在视频。
 * 作用：当文章配置了视频地址时，在首屏图片下方插入独立的视频播放区。
 */
const hasInlineVideo = computed(() => Boolean(post.value?.videoUrl))

/**
 * 计算文章详情首屏展示标签。
 * 作用：控制文章头部标签数量，避免首屏信息区过于拥挤。
 */
const postTagItems = computed(() => post.value?.tagItems || [])
const heroTags = computed(() => post.value?.tagItems?.slice(0, 4) || [])
const articleCodeTheme = computed(() => colorMode.value === 'dark' ? 'github-dark' : 'github-light')
const relatedLeadPost = computed(() => post.value?.relatedPosts?.[0] || null)
const relatedStreamPosts = computed(() => post.value?.relatedPosts?.slice(1) || [])

/**
 * 计算文章发布时间展示文本。
 * 作用：把后端返回的时间字符串转成更适合前台阅读的展示格式，
 * 避免原始 ISO 时间直接暴露在界面上影响内容气质。
 */
const postPublishedAtLabel = computed(() => {
  return formatChineseDate(post.value?.publishedAt || '')
})

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

/**
 * 判断详情页是否需要显示目录侧栏。
 * 作用：当文章没有目录时隐藏目录模块，避免右侧出现空白容器。
 */
const hasToc = computed(() => tocItems.value.length > 0)

/**
 * 判断详情页是否需要显示标签侧栏。
 * 作用：当文章没有标签时隐藏标签模块，减少无效占位。
 */
const hasPostTags = computed(() => postTagItems.value.length > 0)

/**
 * 判断详情页是否需要显示右侧侧栏。
 * 作用：只有目录或标签任一存在时才保留侧栏布局。
 */
const showArticleSidebar = computed(() => hasToc.value || hasPostTags.value)

watch(tocItems, value => {
  activeTocId.value = value[0]?.id || ''
}, { immediate: true })

watch(activeTocId, async () => {
  if (!import.meta.client) {
    return
  }

  await nextTick()
  syncActiveTocIntoView()
})

watch(
  () => [post.value?.slug, tocItems.value.length],
  async () => {
    if (!import.meta.client) {
      return
    }

    await nextTick()
    observeArticleHeadings()
    syncReadingProgress()
  }
)

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
  mobileTocOpen.value = false

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

/**
 * 打开移动端目录抽屉。
 * 作用：仅在文章存在目录时唤起移动端目录导航，减少小屏幕下的结构打扰。
 */
function openMobileTocDrawer() {
  if (!hasToc.value) {
    return
  }

  mobileTocOpen.value = true
}

/**
 * 关闭移动端目录抽屉。
 * 作用：统一处理遮罩、关闭按钮和目录点击后的抽屉收起行为。
 */
function closeMobileTocDrawer() {
  mobileTocOpen.value = false
}

/**
 * 清理正文目录监听器。
 * 作用：在页面卸载或重新绑定目录监听前移除旧的观察器，避免重复监听。
 */
function cleanupTocObserver() {
  tocObserver?.disconnect()
  tocObserver = null
}

/**
 * 同步目录当前项到可视区域。
 * 作用：当正文滚动导致当前章节变化时，让目录容器自动平滑跟随，
 * 避免激活项跑出目录可视范围后用户失去定位。
 */
function syncActiveTocIntoView() {
  if (!import.meta.client || !tocScrollContainerRef.value || !activeTocId.value) {
    return
  }

  const activeElement = tocScrollContainerRef.value.querySelector<HTMLElement>(`[data-toc-id="${CSS.escape(activeTocId.value)}"]`)

  if (!activeElement) {
    return
  }

  const containerRect = tocScrollContainerRef.value.getBoundingClientRect()
  const activeRect = activeElement.getBoundingClientRect()
  const padding = 20
  const isAboveViewport = activeRect.top < containerRect.top + padding
  const isBelowViewport = activeRect.bottom > containerRect.bottom - padding

  if (!isAboveViewport && !isBelowViewport) {
    return
  }

  const targetScrollTop = activeElement.offsetTop - tocScrollContainerRef.value.clientHeight / 2 + activeElement.clientHeight / 2

  tocScrollContainerRef.value.scrollTo({
    top: Math.max(targetScrollTop, 0),
    behavior: 'smooth'
  })
}

/**
 * 监听正文标题节点。
 * 作用：根据当前滚动位置自动同步激活目录项，让目录和正文保持联动。
 */
function observeArticleHeadings() {
  cleanupTocObserver()

  if (!import.meta.client || !articleContentRef.value || !tocItems.value.length) {
    return
  }

  const headings = tocItems.value
    .map(item => articleContentRef.value?.querySelector<HTMLElement>(`#${CSS.escape(item.id)}`))
    .filter((item): item is HTMLElement => Boolean(item))

  if (!headings.length) {
    return
  }

  tocObserver = new IntersectionObserver((entries) => {
    const visibleEntries = entries
      .filter(entry => entry.isIntersecting)
      .sort((first, second) => first.boundingClientRect.top - second.boundingClientRect.top)

    if (!visibleEntries.length) {
      return
    }

    activeTocId.value = visibleEntries[0].target.id
  }, {
    root: null,
    rootMargin: '-18% 0px -62% 0px',
    threshold: [0, 1]
  })

  headings.forEach(item => tocObserver?.observe(item))
}

/**
 * 同步阅读进度。
 * 作用：根据正文区域在视口中的滚动进度，计算顶部阅读进度条的宽度。
 */
function syncReadingProgress() {
  if (!import.meta.client || !articleContentRef.value) {
    return
  }

  const rect = articleContentRef.value.getBoundingClientRect()
  const viewportHeight = window.innerHeight || 1
  const totalDistance = Math.max(articleContentRef.value.offsetHeight - viewportHeight * 0.55, 1)
  const passedDistance = Math.min(Math.max(-rect.top + viewportHeight * 0.18, 0), totalDistance)

  readingProgress.value = Math.round((passedDistance / totalDistance) * 100)
}

/**
 * 同步移动端目录按钮显隐。
 * 作用：让目录按钮在首屏保持隐藏，用户进入正文阅读后再出现，减少首屏干扰。
 */
function syncMobileTocVisibility() {
  if (!import.meta.client) {
    return
  }

  if (!hasToc.value) {
    mobileTocVisible.value = false
    mobileTocOpen.value = false
    lastWindowScrollTop.value = 0
    return
  }

  const scrollTop = window.scrollY || window.pageYOffset || 0
  const revealOffset = Math.max(Math.round(window.innerHeight * 0.42), 220)
  const nearTopOffset = Math.max(Math.round(window.innerHeight * 0.18), 72)
  const isScrollingDown = scrollTop > lastWindowScrollTop.value

  if (scrollTop <= nearTopOffset) {
    mobileTocVisible.value = false
  } else if (isScrollingDown && scrollTop > revealOffset) {
    mobileTocVisible.value = true
  }

  lastWindowScrollTop.value = scrollTop
}

onMounted(async () => {
  if (!import.meta.client) {
    return
  }

  await nextTick()
  observeArticleHeadings()
  syncReadingProgress()
  syncMobileTocVisibility()
  window.addEventListener('scroll', syncReadingProgress, { passive: true })
  window.addEventListener('scroll', syncMobileTocVisibility, { passive: true })
  window.addEventListener('resize', syncReadingProgress)
  window.addEventListener('resize', syncMobileTocVisibility)
})

onBeforeUnmount(() => {
  cleanupTocObserver()

  if (!import.meta.client) {
    return
  }

  window.removeEventListener('scroll', syncReadingProgress)
  window.removeEventListener('scroll', syncMobileTocVisibility)
  window.removeEventListener('resize', syncReadingProgress)
  window.removeEventListener('resize', syncMobileTocVisibility)
})
</script>

<template>
  <main class="min-h-screen overflow-x-hidden bg-[linear-gradient(180deg,#f4f8ff_0%,#ffffff_34%,#f8fbff_100%)] text-slate-900 dark:bg-[linear-gradient(180deg,#020617_0%,#071120_40%,#020617_100%)] dark:text-slate-100">
    <div class="fixed inset-x-0 top-0 z-40 h-1.5 bg-transparent">
      <div
        class="h-full rounded-r-full bg-[linear-gradient(90deg,rgba(14,165,233,0.95),rgba(249,115,22,0.88))] shadow-[0_10px_24px_-12px_rgba(14,165,233,0.72)] transition-[width] duration-200 ease-out"
        :style="{ width: `${readingProgress}%` }"
      />
    </div>

    <section v-if="post" class="relative overflow-hidden">
      <div class="relative h-[34svh] min-h-[280px] w-full sm:h-[42svh] sm:min-h-[340px] lg:h-[54svh]">
        <div class="absolute inset-0">
          <YunyuImage
            :src="post.coverUrl"
            :alt="post.title"
            wrapper-class="absolute inset-0 h-full w-full"
            image-class="h-full w-full"
            rounded-class="rounded-none"
          />
          <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.18)_0%,rgba(15,23,42,0.18)_18%,rgba(15,23,42,0.22)_42%,rgba(15,23,42,0.62)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.1)_0%,rgba(2,6,23,0.14)_20%,rgba(2,6,23,0.26)_44%,rgba(2,6,23,0.72)_100%)]" />
        </div>
        <div class="absolute inset-x-0 bottom-0 z-10">
          <div class="mx-auto max-w-[1440px] px-5 pb-8 sm:px-8 sm:pb-12 lg:px-10 lg:pb-14">
            <div class="max-w-[44rem]">
              <div class="flex flex-wrap gap-2">
                <NuxtLink :to="`/categories/${post.categorySlug}`">
                  <UBadge color="neutral" variant="soft" size="lg" class="backdrop-blur-md">
                    {{ post.categoryName }}
                  </UBadge>
                </NuxtLink>
                <NuxtLink
                  v-for="topic in post.topicItems"
                  :key="`${post.slug}-${topic.slug}`"
                  :to="`/topics/${topic.slug}`"
                >
                  <UBadge color="primary" variant="soft" size="lg" class="backdrop-blur-md">
                    {{ topic.name }}
                  </UBadge>
                </NuxtLink>
              </div>

              <h1 class="mt-4 max-w-[36rem] text-[clamp(1.16rem,4.9vw,2.08rem)] font-semibold leading-[1.1] tracking-[-0.032em] [font-family:var(--font-display)] [text-wrap:balance] text-white drop-shadow-[0_14px_32px_rgba(15,23,42,0.3)] sm:mt-5 sm:text-[clamp(1.34rem,1.18rem+0.82vw,2.08rem)] sm:leading-[1.08]">
                {{ post.title }}
              </h1>

            </div>
          </div>
        </div>
        <div class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(244,248,255,0.9)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0)_0%,rgba(2,6,23,0.96)_100%)]" />
      </div>
    </section>

    <section v-if="post" class="relative z-10 mx-auto -mt-4 max-w-[1440px] px-4 pb-14 sm:-mt-8 sm:px-8 lg:-mt-10 lg:px-10 lg:pb-24">
      <div class="grid min-w-0 gap-6 sm:gap-8" :class="showArticleSidebar ? 'xl:grid-cols-[minmax(0,1fr)_340px]' : ''">
        <div ref="articleContentRef" class="min-w-0 w-full max-w-full space-y-6 sm:space-y-8">
          <section class="px-1 pt-1 text-[0.74rem] text-slate-500 dark:text-slate-400 sm:pt-2 sm:text-[0.78rem]">
            <div class="flex flex-wrap items-center gap-x-3 gap-y-2 sm:gap-x-4">
              <span class="font-medium text-slate-700 dark:text-slate-200">作者 {{ post.authorName }}</span>
              <span class="h-1 w-1 rounded-full bg-slate-300/90 dark:bg-slate-600" />
              <span>发布 {{ postPublishedAtLabel }}</span>
              <span class="h-1 w-1 rounded-full bg-slate-300/90 dark:bg-slate-600" />
              <span>阅读 {{ post.readingMinutes }} 分钟</span>
              <span class="h-1 w-1 rounded-full bg-slate-300/90 dark:bg-slate-600" />
              <span>热度 {{ post.viewCount }} 次浏览</span>
            </div>
          </section>

          <section
            v-if="hasInlineVideo"
            class="overflow-hidden rounded-[24px] border border-white/60 bg-white/84 p-1.5 shadow-[0_24px_60px_-44px_rgba(15,23,42,0.24)] backdrop-blur-xl sm:rounded-[34px] sm:p-2 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-slate-950/70"
          >
            <video
              :src="post.videoUrl"
              :poster="post.coverUrl || undefined"
              class="aspect-video h-full w-full rounded-[18px] object-cover sm:rounded-[26px]"
              controls
              playsinline
              preload="metadata"
            />
          </section>

          <section v-if="heroTags.length" class="px-1">
            <div class="flex flex-wrap gap-2">
              <NuxtLink
                v-for="tag in heroTags"
                :key="`${post.slug}-${tag.slug}`"
                :to="`/tags/${tag.slug}`"
                class="rounded-full border border-slate-200/80 bg-white/80 px-2.5 py-1 text-[0.72rem] font-medium tracking-[0.01em] text-slate-500 transition hover:border-sky-200 hover:bg-sky-50/70 hover:text-sky-700 dark:border-slate-700/90 dark:bg-slate-900/72 dark:text-slate-400 dark:hover:border-sky-800 dark:hover:bg-slate-900 dark:hover:text-sky-200"
              >
                #{{ tag.name }}
              </NuxtLink>
            </div>
          </section>

          <ArticleContentRenderer
            :html="post.contentHtml"
            content-theme="editorial"
            :code-theme="articleCodeTheme"
            :code-default-expanded="false"
            container-class="relative overflow-hidden rounded-[24px] border border-white/55 bg-white/84 px-1.5 py-4 shadow-[0_24px_60px_-44px_rgba(15,23,42,0.24)] backdrop-blur-xl before:pointer-events-none before:absolute before:inset-x-8 before:top-0 before:h-px before:bg-gradient-to-r before:from-transparent before:via-sky-200/90 before:to-transparent sm:rounded-[30px] sm:px-2 sm:py-5 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] lg:rounded-[38px] dark:border-white/10 dark:bg-slate-950/70 dark:before:via-sky-400/30"
            body-class="px-3.5 sm:px-6 lg:px-12"
          />

          <ArticleCommentPanel
            :post-slug="post.slug"
            :allow-comment="post.allowComment"
          />

          <section class="rounded-[24px] border border-white/60 bg-white/86 p-4 shadow-[0_24px_60px_-44px_rgba(15,23,42,0.24)] dark:border-white/10 dark:bg-slate-950/74 sm:rounded-[36px] sm:p-6 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)]">
            <YunyuSectionTitle
              eyebrow="继续阅读"
              title="沿这条线继续读"
              description="延伸阅读区不只是补充内容，而是让当前主题还能自然向前展开。"
              link-label="返回首页"
              link-to="/"
            />

            <div class="mt-6 space-y-5 sm:mt-8 sm:space-y-6">
              <NuxtLink
                v-if="relatedLeadPost"
                :to="`/posts/${relatedLeadPost.slug}`"
                class="group grid min-w-0 gap-6 border-b border-slate-200/75 pb-6 dark:border-white/10 lg:grid-cols-[minmax(0,1fr)_280px]"
              >
                <div class="min-w-0">
                  <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">延伸阅读</p>
                  <h3 class="mt-3 text-[clamp(1.28rem,5vw,2.3rem)] font-semibold leading-[1.12] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 sm:mt-4 sm:leading-[1.08] dark:text-slate-50 dark:group-hover:text-sky-200">
                    {{ relatedLeadPost.title }}
                  </h3>
                  <p class="mt-3 max-w-3xl text-[0.92rem] leading-7 text-slate-600 sm:mt-4 sm:text-[0.98rem] sm:leading-8 dark:text-slate-300">
                    {{ relatedLeadPost.summary }}
                  </p>
                  <div class="mt-5 flex flex-wrap gap-x-5 gap-y-2 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
                    <span>{{ relatedLeadPost.categoryName }}</span>
                    <span>{{ formatChineseDate(relatedLeadPost.publishedAt) }}</span>
                    <span>{{ relatedLeadPost.readingMinutes }} 分钟阅读</span>
                  </div>
                </div>

                <YunyuImage
                  :src="relatedLeadPost.coverUrl"
                  :alt="relatedLeadPost.title"
                  image-class="h-52 w-full object-cover transition duration-500 group-hover:scale-[1.02] sm:h-64 lg:h-full"
                  rounded-class="rounded-[20px] sm:rounded-[26px]"
                />
              </NuxtLink>

              <div class="grid min-w-0 gap-4 lg:grid-cols-2">
                <NuxtLink
                  v-for="item in relatedStreamPosts"
                  :key="item.slug"
                  :to="`/posts/${item.slug}`"
                  class="group min-w-0 rounded-[20px] border border-slate-200/75 bg-white/88 p-4 transition hover:-translate-y-0.5 hover:border-sky-200 hover:shadow-[0_24px_54px_-40px_rgba(14,165,233,0.34)] sm:rounded-[26px] dark:border-slate-800 dark:bg-slate-900/82 dark:hover:border-sky-900"
                >
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">{{ item.categoryName }}</p>
                  <h3 class="mt-3 text-[clamp(1.02rem,3.9vw,1.35rem)] font-semibold leading-6 tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 transition sm:leading-7 group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                    {{ item.title }}
                  </h3>
                  <p class="mt-2 line-clamp-2 text-[0.88rem] leading-6 text-slate-500 sm:text-sm sm:leading-7 dark:text-slate-400">
                    {{ item.summary }}
                  </p>
                  <div class="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.14em] text-slate-400 dark:text-slate-500">
                    <span>{{ formatChineseDate(item.publishedAt) }}</span>
                    <span>{{ item.readingMinutes }} 分钟阅读</span>
                  </div>
                </NuxtLink>
              </div>
            </div>
          </section>
        </div>

        <aside v-if="showArticleSidebar" class="hidden space-y-5 xl:sticky xl:top-28 xl:block xl:self-start">
          <div v-if="hasToc" class="overflow-hidden rounded-[26px] border border-white/55 bg-white/78 p-4 shadow-[0_18px_52px_-42px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/64">
            <div class="flex items-start justify-between gap-3 border-b border-slate-200/50 pb-3 dark:border-white/10">
              <div>
                <p class="text-[0.58rem] font-semibold uppercase tracking-[0.24em] text-orange-500/85 dark:text-orange-300/85">
                  目录
                </p>
                <h2 class="mt-1 text-[0.98rem] font-semibold tracking-[-0.028em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">阅读导航</h2>
              </div>
              <div class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-[1rem] border border-slate-200/70 bg-white/72 text-sky-600 shadow-[0_10px_22px_-22px_rgba(14,165,233,0.24)] dark:border-white/10 dark:bg-white/5 dark:text-sky-200">
                <UIcon name="i-lucide-book-marked" class="size-4" />
              </div>
            </div>

            <div class="mt-2.5 rounded-[1.15rem] bg-slate-50/66 px-1.5 py-1.5 dark:bg-slate-900/42">
              <div ref="tocScrollContainerRef" class="max-h-[32rem] overflow-auto pr-0.5 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.24)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/50 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/50">
              <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
              </div>
            </div>
          </div>

          <div v-if="hasPostTags" class="overflow-hidden rounded-[30px] border border-white/55 bg-white/82 p-5 shadow-[0_24px_76px_-50px_rgba(15,23,42,0.22)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/68">
            <div class="flex items-start justify-between gap-3 border-b border-slate-200/60 pb-4 dark:border-white/10">
              <div>
                <p class="text-[0.68rem] font-semibold uppercase tracking-[0.3em] text-sky-600 dark:text-sky-300">
                  标签
                </p>
                <h2 class="mt-2 text-[1.12rem] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">内容线索</h2>
              </div>
              <div class="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl border border-slate-200/75 bg-white/80 text-sky-600 shadow-[0_14px_30px_-24px_rgba(14,165,233,0.3)] dark:border-white/10 dark:bg-white/5 dark:text-sky-200">
                <UIcon name="i-lucide-hash" class="size-5" />
              </div>
            </div>

            <div class="mt-4 rounded-[1.4rem] bg-slate-50/74 px-3 py-3 dark:bg-slate-900/50">
              <div class="flex flex-wrap gap-2">
                <NuxtLink
                  v-for="tag in postTagItems"
                  :key="`${post.slug}-${tag.slug}`"
                  :to="`/tags/${tag.slug}`"
                  class="rounded-full border border-slate-200/85 bg-white/92 px-3 py-1.5 text-[0.76rem] font-medium tracking-[0.01em] text-slate-600 transition hover:-translate-y-0.5 hover:border-sky-200 hover:bg-sky-50/70 hover:text-sky-700 dark:border-slate-700 dark:bg-slate-900/84 dark:text-slate-300 dark:hover:border-sky-800 dark:hover:bg-slate-900 dark:hover:text-sky-200"
                >
                  #{{ tag.name }}
                </NuxtLink>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <Transition
      enter-active-class="transition duration-220 ease-out"
      enter-from-class="translate-y-2 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-180 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-2 opacity-0"
    >
      <div
        v-if="hasToc && mobileTocVisible"
        class="fixed bottom-5 right-4 z-40 xl:hidden"
      >
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full border border-white/70 bg-white/88 px-3.5 py-2 text-[0.78rem] font-medium text-slate-700 shadow-[0_18px_42px_-24px_rgba(15,23,42,0.28)] backdrop-blur-xl transition hover:-translate-y-0.5 hover:text-slate-950 dark:border-white/10 dark:bg-slate-950/82 dark:text-slate-200 dark:hover:text-white"
          @click="openMobileTocDrawer"
        >
          <UIcon name="i-lucide-align-left" class="size-4" />
          <span>目录</span>
        </button>
      </div>
    </Transition>

    <Transition
      enter-active-class="transition duration-220 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-180 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="hasToc && mobileTocOpen"
        class="fixed inset-0 z-50 bg-slate-950/36 backdrop-blur-[2px] xl:hidden"
        aria-hidden="true"
        @click="closeMobileTocDrawer"
      />
    </Transition>

    <Transition
      enter-active-class="transition duration-260 ease-out"
      enter-from-class="translate-y-full opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-220 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-full opacity-0"
    >
      <section
        v-if="hasToc && mobileTocOpen"
        class="fixed inset-x-0 bottom-0 z-[60] mx-auto max-w-2xl rounded-t-[28px] border border-white/70 bg-white/96 px-4 pb-[calc(env(safe-area-inset-bottom)+1rem)] pt-3 shadow-[0_-30px_80px_-36px_rgba(15,23,42,0.36)] backdrop-blur-xl xl:hidden dark:border-white/10 dark:bg-slate-950/94"
        aria-label="文章目录"
        aria-modal="true"
        role="dialog"
      >
        <div class="mx-auto mb-3 h-1.5 w-14 rounded-full bg-slate-200 dark:bg-slate-700" />

        <div class="flex items-center justify-between gap-3 border-b border-slate-200/70 pb-3 dark:border-white/10">
          <div>
            <p class="text-[0.66rem] font-semibold uppercase tracking-[0.24em] text-sky-600 dark:text-sky-300">
              目录
            </p>
            <h2 class="mt-1 text-[1rem] font-semibold tracking-[-0.03em] text-slate-950 dark:text-slate-50">
              阅读导航
            </h2>
          </div>

          <button
            type="button"
            class="inline-flex h-9 w-9 items-center justify-center rounded-full border border-slate-200/80 bg-white/82 text-slate-500 transition hover:text-slate-900 dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:text-white"
            aria-label="关闭目录"
            @click="closeMobileTocDrawer"
          >
            <UIcon name="i-lucide-x" class="size-4" />
          </button>
        </div>

        <div class="mt-3 max-h-[60svh] overflow-auto pb-2 pr-1 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.28)_transparent] [&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/60 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/60">
          <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
        </div>
      </section>
    </Transition>
  </main>
</template>
