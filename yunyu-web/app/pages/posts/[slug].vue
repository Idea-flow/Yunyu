<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted } from 'vue'
import type { ArticleTocItem } from '../../types/post'
import ArticleContentRenderer from '../../components/content/ArticleContentRenderer.vue'
import ArticleTocTree from '../../components/content/ArticleTocTree.vue'
import YunyuHero from '~/components/common/YunyuHero.vue'
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
const articleContentRef = ref<HTMLElement | null>(null)
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
 * 计算文章详情首屏展示标签。
 * 作用：控制文章头部标签数量，避免首屏信息区过于拥挤。
 */
const heroTags = computed(() => post.value?.tagItems?.slice(0, 4) || [])
const articleCodeTheme = computed(() => colorMode.value === 'dark' ? 'github-dark' : 'github-light')
const relatedLeadPost = computed(() => post.value?.relatedPosts?.[0] || null)
const relatedStreamPosts = computed(() => post.value?.relatedPosts?.slice(1) || [])

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
 * 清理正文目录监听器。
 * 作用：在页面卸载或重新绑定目录监听前移除旧的观察器，避免重复监听。
 */
function cleanupTocObserver() {
  tocObserver?.disconnect()
  tocObserver = null
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

onMounted(async () => {
  if (!import.meta.client) {
    return
  }

  await nextTick()
  observeArticleHeadings()
  syncReadingProgress()
  window.addEventListener('scroll', syncReadingProgress, { passive: true })
  window.addEventListener('resize', syncReadingProgress)
})

onBeforeUnmount(() => {
  cleanupTocObserver()

  if (!import.meta.client) {
    return
  }

  window.removeEventListener('scroll', syncReadingProgress)
  window.removeEventListener('resize', syncReadingProgress)
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#ffffff_34%,#f8fbff_100%)] text-slate-900 dark:bg-[linear-gradient(180deg,#020617_0%,#071120_40%,#020617_100%)] dark:text-slate-100">
    <div class="fixed inset-x-0 top-0 z-40 h-1.5 bg-transparent">
      <div
        class="h-full rounded-r-full bg-[linear-gradient(90deg,rgba(14,165,233,0.95),rgba(249,115,22,0.88))] shadow-[0_10px_24px_-12px_rgba(14,165,233,0.72)] transition-[width] duration-200 ease-out"
        :style="{ width: `${readingProgress}%` }"
      />
    </div>

    <YunyuHero
      v-if="post"
      :src="post.coverUrl"
      :alt="post.title"
      min-height-class="min-h-[60svh] sm:min-h-[66svh] lg:min-h-[72svh]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
    >
      <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-white/72 drop-shadow-md">
        Editorial Story
      </p>

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

      <h1 class="mt-5 line-clamp-2 text-[clamp(2.55rem,1.85rem+2.5vw,4.8rem)] font-semibold leading-[0.98] tracking-[-0.045em] [font-family:var(--font-display)] [text-wrap:balance] text-white drop-shadow-lg">
        {{ post.title }}
      </h1>

      <p class="mt-5 max-w-[42rem] text-[1.02rem] leading-8 tracking-[-0.01em] text-white/86 drop-shadow-md sm:text-[1.08rem] sm:leading-9">
        {{ post.summary }}
      </p>

      <div class="mt-7 inline-flex max-w-full flex-wrap items-center gap-x-5 gap-y-3 rounded-[1.75rem] border border-white/14 bg-black/15 px-5 py-4 text-white/90 backdrop-blur-md">
        <div class="flex items-center gap-2.5">
          <span class="inline-flex size-5 items-center justify-center">
            <svg class="size-5" fill="url(#yunyuAuthorGradient)" viewBox="0 0 24 24" aria-hidden="true">
              <defs>
                <linearGradient id="yunyuAuthorGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#fb7185" />
                  <stop offset="100%" style="stop-color:#fb923c" />
                </linearGradient>
              </defs>
              <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </span>
          <div>
            <p class="text-[0.68rem] uppercase tracking-[0.18em] text-white/55">作者</p>
            <span class="text-sm drop-shadow-md">{{ post.authorName }}</span>
          </div>
        </div>

        <div class="hidden h-8 w-px bg-white/12 sm:block" />

        <div class="flex items-center gap-2.5">
          <UIcon name="i-lucide-clock-3" class="size-5 text-sky-300" />
          <div>
            <p class="text-[0.68rem] uppercase tracking-[0.18em] text-white/55">发布</p>
            <span class="text-sm drop-shadow-md">{{ post.publishedAt }}</span>
          </div>
        </div>

        <div class="hidden h-8 w-px bg-white/12 sm:block" />

        <div class="flex items-center gap-2.5">
          <UIcon name="i-lucide-book-open" class="size-5 text-orange-300" />
          <div>
            <p class="text-[0.68rem] uppercase tracking-[0.18em] text-white/55">阅读</p>
            <span class="text-sm drop-shadow-md">{{ post.readingMinutes }} 分钟阅读</span>
          </div>
        </div>

        <div class="hidden h-8 w-px bg-white/12 sm:block" />

        <div class="flex items-center gap-2.5">
          <UIcon name="i-lucide-eye" class="size-5 text-emerald-300" />
          <div>
            <p class="text-[0.68rem] uppercase tracking-[0.18em] text-white/55">热度</p>
            <span class="text-sm drop-shadow-md">{{ post.viewCount }} 次浏览</span>
          </div>
        </div>
      </div>

      <div class="mt-5 flex flex-wrap gap-2">
        <NuxtLink
          v-for="tag in heroTags"
          :key="`${post.slug}-${tag.slug}`"
          :to="`/tags/${tag.slug}`"
          class="rounded-full border border-white/16 bg-white/10 px-3 py-1.5 text-xs font-medium text-white/88 backdrop-blur-sm transition hover:bg-white/18"
        >
          #{{ tag.name }}
        </NuxtLink>
      </div>
    </YunyuHero>

    <section v-if="post" class="mx-auto max-w-[1440px] px-5 pb-16 sm:px-8 lg:px-10 lg:pb-24">
      <div class="grid gap-8 xl:grid-cols-[minmax(0,1fr)_340px]">
        <div ref="articleContentRef" class="space-y-8">
          <section class="rounded-[34px] border border-white/55 bg-white/78 px-6 py-6 shadow-[0_30px_90px_-56px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/62 sm:px-8">
            <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_220px] lg:items-end">
              <div>
                <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
                  Reading Note
                </p>
                <p class="mt-4 text-[clamp(1.6rem,1.35rem+0.65vw,2.05rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
                  这篇文章适合以更慢一点的节奏阅读，把重点留给正文本身。
                </p>
                <p class="mt-4 max-w-3xl text-[0.98rem] leading-8 text-slate-600 dark:text-slate-300">
                  目录、标签和延伸阅读只承担导览作用。真正的主角应该是段落、章节与内容之间的推进关系。
                </p>
              </div>

              <div class="grid grid-cols-2 gap-3 lg:grid-cols-1">
                <div class="rounded-[24px] border border-slate-200/75 bg-white/72 px-4 py-4 dark:border-white/10 dark:bg-slate-900/72">
                  <p class="text-[0.68rem] uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">段落节奏</p>
                  <p class="mt-2 text-sm font-medium text-slate-700 dark:text-slate-200">Editorial</p>
                </div>
                <div class="rounded-[24px] border border-slate-200/75 bg-white/72 px-4 py-4 dark:border-white/10 dark:bg-slate-900/72">
                  <p class="text-[0.68rem] uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">正文主题</p>
                  <p class="mt-2 text-sm font-medium text-slate-700 dark:text-slate-200">Long-form Reading</p>
                </div>
              </div>
            </div>
          </section>

          <ArticleContentRenderer
            :html="post.contentHtml"
            content-theme="editorial"
            :code-theme="articleCodeTheme"
            :code-default-expanded="false"
            container-class="rounded-[38px] border border-white/55 bg-white/84 px-2 py-4 shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/70"
            body-class="px-4 sm:px-6 lg:px-12"
          />

          <section class="rounded-[36px] border border-white/60 bg-white/86 p-5 shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-slate-950/74 sm:p-6">
            <YunyuSectionTitle
              eyebrow="继续阅读"
              title="沿这条线继续读"
              description="延伸阅读区不只是补充内容，而是让当前主题还能自然向前展开。"
              link-label="返回首页"
              link-to="/"
            />

            <div class="mt-8 space-y-6">
              <NuxtLink
                v-if="relatedLeadPost"
                :to="`/posts/${relatedLeadPost.slug}`"
                class="group grid gap-6 border-b border-slate-200/75 pb-6 dark:border-white/10 lg:grid-cols-[minmax(0,1fr)_280px]"
              >
                <div class="min-w-0">
                  <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                    Editor's Pick
                  </p>
                  <h3 class="mt-4 text-[clamp(1.7rem,1.4rem+0.8vw,2.3rem)] font-semibold leading-[1.08] tracking-[-0.035em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                    {{ relatedLeadPost.title }}
                  </h3>
                  <p class="mt-4 max-w-3xl text-[0.98rem] leading-8 text-slate-600 dark:text-slate-300">
                    {{ relatedLeadPost.summary }}
                  </p>
                  <div class="mt-5 flex flex-wrap gap-x-5 gap-y-2 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
                    <span>{{ relatedLeadPost.categoryName }}</span>
                    <span>{{ relatedLeadPost.publishedAt }}</span>
                    <span>{{ relatedLeadPost.readingMinutes }} 分钟阅读</span>
                  </div>
                </div>

                <YunyuImage
                  :src="relatedLeadPost.coverUrl"
                  :alt="relatedLeadPost.title"
                  image-class="h-64 w-full object-cover transition duration-500 group-hover:scale-[1.02] lg:h-full"
                  rounded-class="rounded-[26px]"
                />
              </NuxtLink>

              <div class="grid gap-4 lg:grid-cols-2">
                <NuxtLink
                  v-for="item in relatedStreamPosts"
                  :key="item.slug"
                  :to="`/posts/${item.slug}`"
                  class="group rounded-[26px] border border-slate-200/75 bg-white/88 p-4 transition hover:-translate-y-0.5 hover:border-sky-200 hover:shadow-[0_24px_54px_-40px_rgba(14,165,233,0.34)] dark:border-slate-800 dark:bg-slate-900/82 dark:hover:border-sky-900"
                >
                  <p class="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">From {{ item.categoryName }}</p>
                  <h3 class="mt-3 text-[clamp(1.12rem,1.02rem+0.32vw,1.35rem)] font-semibold leading-7 tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                    {{ item.title }}
                  </h3>
                  <p class="mt-2 line-clamp-2 text-sm leading-7 text-slate-500 dark:text-slate-400">
                    {{ item.summary }}
                  </p>
                  <div class="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.14em] text-slate-400 dark:text-slate-500">
                    <span>{{ item.publishedAt }}</span>
                    <span>{{ item.readingMinutes }} 分钟阅读</span>
                  </div>
                </NuxtLink>
              </div>
            </div>
          </section>
        </div>

        <aside class="space-y-5 xl:sticky xl:top-28 xl:self-start">
          <div class="rounded-[30px] border border-white/55 bg-white/82 p-5 shadow-[0_24px_76px_-50px_rgba(15,23,42,0.22)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/68">
            <div class="flex items-center justify-between gap-3 border-b border-slate-200/60 pb-4 dark:border-white/10">
              <div>
                <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-orange-500 dark:text-orange-300">
                  目录
                </p>
                <h2 class="mt-2 text-[1.2rem] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">文章结构</h2>
              </div>
              <div class="inline-flex h-11 w-11 items-center justify-center rounded-2xl border border-slate-200/75 bg-white/80 text-sky-600 dark:border-white/10 dark:bg-white/5 dark:text-sky-200">
                <UIcon name="i-lucide-book-marked" class="size-5" />
              </div>
            </div>

            <div class="mt-4 max-h-[32rem] overflow-auto pr-1 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.3)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/60 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/60">
              <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
            </div>
          </div>

          <div class="rounded-[30px] border border-white/55 bg-white/82 p-5 shadow-[0_24px_76px_-50px_rgba(15,23,42,0.22)] backdrop-blur-xl dark:border-white/10 dark:bg-slate-950/68">
            <p class="text-xs font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
              内容标签
            </p>
            <p class="mt-3 text-sm leading-7 text-slate-500 dark:text-slate-400">
              从标签继续走，可以更快回到相近主题与相近语境的内容里。
            </p>
            <div class="mt-4 flex flex-wrap gap-2">
              <NuxtLink
                v-for="tag in post.tagItems"
                :key="`${post.slug}-${tag.slug}`"
                :to="`/tags/${tag.slug}`"
                class="rounded-full border border-slate-200/80 bg-white/90 px-3 py-1.5 text-xs font-medium text-slate-600 transition hover:border-sky-200 hover:bg-sky-50/70 hover:text-sky-700 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-300 dark:hover:border-sky-800 dark:hover:bg-slate-900 dark:hover:text-sky-200"
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
