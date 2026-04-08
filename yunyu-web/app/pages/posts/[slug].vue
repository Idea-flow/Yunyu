<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted } from 'vue'
import { formatChineseDate } from '~/utils/date'
import type { ArticleTocItem } from '../../types/post'
import ArticleContentRenderer from '../../components/content/ArticleContentRenderer.vue'
import ArticleCommentPanel from '../../components/content/ArticleCommentPanel.vue'
import ArticleTocTree from '../../components/content/ArticleTocTree.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'

type ArticleContentTheme = 'editorial' | 'documentation' | 'minimal'
type ArticleCodeThemeFamily = 'github' | 'vitesse'
type ArticleCodeTheme = 'github-light' | 'github-dark' | 'vitesse-light' | 'vitesse-dark'

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
const isTocManualScrolling = ref(false)
const articleContentRef = ref<HTMLElement | null>(null)
const tocScrollContainerRef = ref<HTMLElement | null>(null)
let tocObserver: IntersectionObserver | null = null
let tocManualScrollTimer: number | null = null
const ARTICLE_CONTENT_THEME_STORAGE_KEY = 'yunyu-post-content-theme'
const ARTICLE_CODE_THEME_STORAGE_KEY = 'yunyu-post-code-theme-family'
const articleContentThemeOptions: Array<{ value: ArticleContentTheme, label: string, hint: string }> = [
  { value: 'editorial', label: '杂志感', hint: '更有内容阅读氛围' },
  { value: 'documentation', label: '文档感', hint: '结构更规整清楚' },
  { value: 'minimal', label: '极简', hint: '更轻更紧凑' }
]
const articleCodeThemeFamilyOptions: Array<{ value: ArticleCodeThemeFamily, label: string, hint: string }> = [
  { value: 'github', label: 'GitHub', hint: '稳定克制' },
  { value: 'vitesse', label: 'Vitesse', hint: '技术感更强' }
]
const selectedArticleContentTheme = ref<ArticleContentTheme>('editorial')
const selectedArticleCodeThemeFamily = ref<ArticleCodeThemeFamily>('github')

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
const articleContentTheme = computed(() => selectedArticleContentTheme.value)
const articleCodeTheme = computed<ArticleCodeTheme>(() => {
  if (selectedArticleCodeThemeFamily.value === 'vitesse') {
    return colorMode.value === 'dark' ? 'vitesse-dark' : 'vitesse-light'
  }

  return colorMode.value === 'dark' ? 'github-dark' : 'github-light'
})
const relatedCompactPosts = computed(() => post.value?.relatedPosts?.slice(0, 2) || [])

/**
 * 判断详情页是否存在延伸阅读。
 * 作用：当后端未返回相关推荐时隐藏“继续阅读”区块，避免正文后出现空白导览容器。
 */
const hasRelatedPosts = computed(() => (post.value?.relatedPosts?.length || 0) > 0)

const currentArticleContentThemeLabel = computed(() => {
  return articleContentThemeOptions.find(item => item.value === selectedArticleContentTheme.value)?.label || '杂志感'
})
const currentArticleCodeThemeLabel = computed(() => {
  const familyLabel = articleCodeThemeFamilyOptions.find(item => item.value === selectedArticleCodeThemeFamily.value)?.label || 'GitHub'
  return `${familyLabel} · ${colorMode.value === 'dark' ? '暗色' : '亮色'}`
})

/**
 * 解析标题内部纯文本。
 * 作用：从 HTML 标题片段中提取可读文本，供前台目录展示使用，
 * 避免目录里残留标签、换行和多余空白字符。
 *
 * @param html 标题内部 HTML
 * @returns 清洗后的纯文本
 */
function extractHeadingText(html: string) {
  return html
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&amp;/gi, '&')
    .replace(/&lt;/gi, '<')
    .replace(/&gt;/gi, '>')
    .replace(/&quot;/gi, '"')
    .replace(/&#39;/gi, '\'')
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * 从正文 HTML 中提取目录结构。
 * 作用：当前台接口没有返回 `contentTocJson` 时，仍然可以根据正文中的标题标签生成目录，
 * 保证详情页右侧目录和移动端目录抽屉都能正常显示。
 *
 * @param contentHtml 文章正文 HTML
 * @returns 可供目录树组件直接使用的目录数组
 */
function extractTocItemsFromHtml(contentHtml: string) {
  const headingPattern = /<h([1-6])\b([^>]*)>([\s\S]*?)<\/h\1>/gi
  const items: ArticleTocItem[] = []
  let match: RegExpExecArray | null

  while ((match = headingPattern.exec(contentHtml)) !== null) {
    const level = Number.parseInt(match[1] || '0', 10)
    const attrs = match[2] || ''
    const innerHtml = match[3] || ''
    const text = extractHeadingText(innerHtml)

    if (!text) {
      continue
    }

    const idMatch = attrs.match(/\sid=(['"])(.*?)\1/i)
    const id = idMatch?.[2]?.trim() || `article-heading-${items.length + 1}`

    items.push({
      id,
      text,
      level
    })
  }

  return items
}

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

  if (tocJson) {
    try {
      const parsed = JSON.parse(tocJson)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return extractTocItemsFromHtml(post.value?.contentHtml || '')
    }
  }

  return extractTocItemsFromHtml(post.value?.contentHtml || '')
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

watch(selectedArticleContentTheme, value => {
  if (!import.meta.client) {
    return
  }

  window.localStorage.setItem(ARTICLE_CONTENT_THEME_STORAGE_KEY, value)
})

watch(selectedArticleCodeThemeFamily, value => {
  if (!import.meta.client) {
    return
  }

  window.localStorage.setItem(ARTICLE_CODE_THEME_STORAGE_KEY, value)
})

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

  const articleBody = getArticleBodyElement()
  const target = articleBody?.querySelector<HTMLElement>(`#${CSS.escape(item.id)}`) || document.getElementById(item.id)

  if (!target) {
    return
  }

  startTocManualScrollLock()
  const offsetTop = getTocScrollOffset()
  const targetScrollTop = window.scrollY + target.getBoundingClientRect().top - offsetTop

  window.scrollTo({
    top: Math.max(targetScrollTop, 0),
    behavior: 'smooth',
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
 * 切换详情页正文主题。
 * 作用：允许读者根据当前阅读内容切换正文排版风格，并记住个人偏好。
 *
 * @param theme 目标正文主题
 */
function switchArticleContentTheme(theme: ArticleContentTheme) {
  selectedArticleContentTheme.value = theme
}

/**
 * 切换详情页代码主题家族。
 * 作用：只切换代码主题风格家族，具体明暗版本由当前系统主题自动映射。
 *
 * @param family 目标代码主题家族
 */
function switchArticleCodeThemeFamily(family: ArticleCodeThemeFamily) {
  selectedArticleCodeThemeFamily.value = family
}

/**
 * 恢复详情页主题偏好。
 * 作用：首次进入文章详情页时，从本地缓存中读取用户上一次选择的正文主题和代码主题。
 */
function hydrateArticleThemePreference() {
  if (!import.meta.client) {
    return
  }

  const savedContentTheme = window.localStorage.getItem(ARTICLE_CONTENT_THEME_STORAGE_KEY)
  const savedCodeThemeFamily = window.localStorage.getItem(ARTICLE_CODE_THEME_STORAGE_KEY)

  if (savedContentTheme === 'editorial' || savedContentTheme === 'documentation' || savedContentTheme === 'minimal') {
    selectedArticleContentTheme.value = savedContentTheme
  }

  if (savedCodeThemeFamily === 'github' || savedCodeThemeFamily === 'vitesse') {
    selectedArticleCodeThemeFamily.value = savedCodeThemeFamily
  }
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
 * 计算目录点击滚动偏移量。
 * 作用：让目录跳转时自动避开顶部固定导航和进度条，避免标题被遮挡导致“跳到附近章节”的错觉。
 */
function getTocScrollOffset() {
  if (!import.meta.client) {
    return 96
  }

  const headerElement = document.querySelector('header')
  const headerHeight = headerElement instanceof HTMLElement ? headerElement.getBoundingClientRect().height : 0
  const readingProgressHeight = 6
  const spacing = 12

  return Math.round(headerHeight + readingProgressHeight + spacing)
}

/**
 * 标记目录手动滚动状态。
 * 作用：目录点击触发平滑滚动时，短暂冻结自动激活逻辑，避免观察器在滚动途中切到相邻标题。
 */
function startTocManualScrollLock() {
  if (!import.meta.client) {
    return
  }

  isTocManualScrolling.value = true

  if (tocManualScrollTimer !== null) {
    window.clearTimeout(tocManualScrollTimer)
  }

  tocManualScrollTimer = window.setTimeout(() => {
    isTocManualScrolling.value = false
    tocManualScrollTimer = null
  }, 950)
}

/**
 * 获取正文渲染根节点。
 * 作用：把目录联动范围限制在文章正文 `.yy-md` 内部，
 * 避免把评论区、相关推荐等区域的标题错误算进目录监听。
 */
function getArticleBodyElement() {
  if (!articleContentRef.value) {
    return null
  }

  return articleContentRef.value.querySelector<HTMLElement>('.yy-md')
}

/**
 * 同步正文标题锚点。
 * 作用：当前端兜底从 HTML 标题生成目录时，为未携带 `id` 的标题节点补齐锚点，
 * 让目录点击跳转和滚动监听都能稳定工作。
 */
function syncArticleHeadingIds() {
  const articleBody = getArticleBodyElement()

  if (!articleBody) {
    return []
  }

  const headings = Array.from(articleBody.querySelectorAll<HTMLElement>('h1, h2, h3, h4, h5, h6'))
  const scrollMarginTop = getTocScrollOffset()

  headings.forEach((heading, index) => {
    const tocItem = tocItems.value[index]

    if (!tocItem) {
      return
    }

    if (!heading.id) {
      heading.id = tocItem.id
    }

    heading.style.scrollMarginTop = `${scrollMarginTop}px`
  })

  return headings
}

/**
 * 监听正文标题节点。
 * 作用：根据当前滚动位置自动同步激活目录项，让目录和正文保持联动。
 */
function observeArticleHeadings() {
  cleanupTocObserver()

  if (!import.meta.client || !tocItems.value.length) {
    return
  }

  syncArticleHeadingIds()

  const articleBody = getArticleBodyElement()

  if (!articleBody) {
    return
  }

  const headings = tocItems.value
    .map(item => articleBody.querySelector<HTMLElement>(`#${CSS.escape(item.id)}`))
    .filter((item): item is HTMLElement => Boolean(item))

  if (!headings.length) {
    return
  }

  tocObserver = new IntersectionObserver((entries) => {
    if (isTocManualScrolling.value) {
      return
    }

    const visibleEntries = entries
      .filter(entry => entry.isIntersecting)
      .sort((first, second) => first.boundingClientRect.top - second.boundingClientRect.top)

    if (!visibleEntries.length) {
      return
    }

    activeTocId.value = visibleEntries[0].target.id
  }, {
    root: null,
    rootMargin: `-${getTocScrollOffset()}px 0px -62% 0px`,
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
  hydrateArticleThemePreference()
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

  if (tocManualScrollTimer !== null) {
    window.clearTimeout(tocManualScrollTimer)
    tocManualScrollTimer = null
  }

  window.removeEventListener('scroll', syncReadingProgress)
  window.removeEventListener('scroll', syncMobileTocVisibility)
  window.removeEventListener('resize', syncReadingProgress)
  window.removeEventListener('resize', syncMobileTocVisibility)
})
</script>

<template>
  <main class="min-h-screen overflow-x-clip bg-[linear-gradient(180deg,#f4f8ff_0%,#ffffff_34%,#f8fbff_100%)] text-slate-900 dark:bg-[linear-gradient(180deg,#020617_0%,#071120_40%,#020617_100%)] dark:text-slate-100">
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
            <div class="lg:grid lg:grid-cols-[minmax(0,1fr)_320px] lg:items-end lg:gap-8">
              <div class="max-w-[44rem] min-w-0">
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

              <section class="hidden justify-self-end lg:block lg:w-full lg:max-w-[320px]">
                <div class="rounded-[18px] border border-white/12 bg-black/12 px-4 py-3 text-white/88 shadow-[0_18px_42px_-34px_rgba(15,23,42,0.34)] backdrop-blur-[14px]">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-[0.6rem] font-semibold uppercase tracking-[0.22em] text-white/56">阅读主题</p>
                    <span class="text-[0.66rem] text-white/58">
                      {{ colorMode.value === 'dark' ? '暗色模式' : '亮色模式' }}
                    </span>
                  </div>

                  <div class="mt-3 space-y-3">
                    <div>
                      <div class="flex items-center justify-between gap-3">
                        <p class="text-[0.68rem] font-medium text-white/74">排版</p>
                        <span class="text-[0.66rem] text-white/52">{{ currentArticleContentThemeLabel }}</span>
                      </div>
                      <div class="mt-1.5 flex flex-wrap gap-1.5">
                        <button
                          v-for="theme in articleContentThemeOptions"
                          :key="theme.value"
                          type="button"
                          class="inline-flex cursor-pointer items-center rounded-full border px-2.5 py-1 text-[0.68rem] font-medium transition"
                          :class="selectedArticleContentTheme === theme.value
                            ? 'border-white/22 bg-white/14 text-white'
                            : 'border-white/8 bg-transparent text-white/64 hover:border-white/16 hover:bg-white/[0.06] hover:text-white/88'"
                          :title="theme.hint"
                          @click="switchArticleContentTheme(theme.value)"
                        >
                          {{ theme.label }}
                        </button>
                      </div>
                    </div>

                    <div>
                      <div class="flex items-center justify-between gap-3">
                        <p class="text-[0.68rem] font-medium text-white/74">代码</p>
                        <span class="text-[0.66rem] text-white/52">{{ currentArticleCodeThemeLabel }}</span>
                      </div>
                      <div class="mt-1.5 flex flex-wrap gap-1.5">
                        <button
                          v-for="theme in articleCodeThemeFamilyOptions"
                          :key="theme.value"
                          type="button"
                          class="inline-flex cursor-pointer items-center rounded-full border px-2.5 py-1 text-[0.68rem] font-medium transition"
                          :class="selectedArticleCodeThemeFamily === theme.value
                            ? 'border-sky-300/22 bg-sky-300/12 text-white'
                            : 'border-white/8 bg-transparent text-white/64 hover:border-white/16 hover:bg-white/[0.06] hover:text-white/88'"
                          :title="`${theme.hint}，会随当前明暗模式自动切换`"
                          @click="switchArticleCodeThemeFamily(theme.value)"
                        >
                          {{ theme.label }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </div>
        </div>
        <div class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(244,248,255,0.9)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0)_0%,rgba(2,6,23,0.96)_100%)]" />
      </div>
    </section>

    <section v-if="post" class="relative z-10 mx-auto -mt-4 max-w-[1440px] px-4 pb-14 sm:-mt-8 sm:px-8 lg:-mt-10 lg:px-10 lg:pb-24">
      <div class="grid min-w-0 gap-6 sm:gap-8 lg:items-start" :class="showArticleSidebar ? 'lg:grid-cols-[minmax(0,1fr)_340px]' : ''">
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

          <section class="rounded-[16px] border border-slate-200/70 bg-white/76 px-3.5 py-3 backdrop-blur-md dark:border-white/10 dark:bg-slate-950/56 lg:hidden">
            <div class="flex items-center justify-between gap-3">
              <p class="text-[0.6rem] font-semibold uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">阅读主题</p>
              <span class="text-[0.66rem] text-slate-400 dark:text-slate-500">
                {{ colorMode.value === 'dark' ? '暗色模式' : '亮色模式' }}
              </span>
            </div>

            <div class="mt-3 space-y-3">
              <div>
                <div class="flex items-center justify-between gap-3">
                  <p class="text-[0.7rem] font-medium text-slate-600 dark:text-slate-300">排版</p>
                  <span class="text-[0.66rem] text-slate-400 dark:text-slate-500">{{ currentArticleContentThemeLabel }}</span>
                </div>
                <div class="mt-1.5 flex flex-wrap gap-1.5">
                  <button
                    v-for="theme in articleContentThemeOptions"
                    :key="theme.value"
                    type="button"
                    class="inline-flex cursor-pointer items-center rounded-full border px-2.5 py-1 text-[0.68rem] font-medium transition"
                    :class="selectedArticleContentTheme === theme.value
                      ? 'border-slate-300 bg-slate-900 text-white dark:border-white/12 dark:bg-white dark:text-slate-950'
                      : 'border-slate-200/80 bg-transparent text-slate-500 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:text-slate-300 dark:hover:border-white/14 dark:hover:text-slate-50'"
                    :title="theme.hint"
                    @click="switchArticleContentTheme(theme.value)"
                  >
                    {{ theme.label }}
                  </button>
                </div>
              </div>

              <div>
                <div class="flex items-center justify-between gap-3">
                  <p class="text-[0.7rem] font-medium text-slate-600 dark:text-slate-300">代码</p>
                  <span class="text-[0.66rem] text-slate-400 dark:text-slate-500">{{ currentArticleCodeThemeLabel }}</span>
                </div>
                <div class="mt-1.5 flex flex-wrap gap-1.5">
                  <button
                    v-for="theme in articleCodeThemeFamilyOptions"
                    :key="theme.value"
                    type="button"
                    class="inline-flex cursor-pointer items-center rounded-full border px-2.5 py-1 text-[0.68rem] font-medium transition"
                    :class="selectedArticleCodeThemeFamily === theme.value
                      ? 'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-400/20 dark:bg-sky-400/12 dark:text-sky-200'
                      : 'border-slate-200/80 bg-transparent text-slate-500 hover:border-slate-300 hover:text-slate-900 dark:border-white/10 dark:text-slate-300 dark:hover:border-white/14 dark:hover:text-slate-50'"
                    :title="`${theme.hint}，会随当前明暗模式自动切换`"
                    @click="switchArticleCodeThemeFamily(theme.value)"
                  >
                    {{ theme.label }}
                  </button>
                </div>
              </div>
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
            :content-theme="articleContentTheme"
            :code-theme="articleCodeTheme"
            :code-default-expanded="false"
            container-class="relative overflow-hidden rounded-[24px] border border-white/55 bg-white/84 px-1.5 py-4 shadow-[0_24px_60px_-44px_rgba(15,23,42,0.24)] backdrop-blur-xl before:pointer-events-none before:absolute before:inset-x-8 before:top-0 before:h-px before:bg-gradient-to-r before:from-transparent before:via-sky-200/90 before:to-transparent sm:rounded-[30px] sm:px-2 sm:py-5 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)] lg:rounded-[38px] dark:border-white/10 dark:bg-slate-950/70 dark:before:via-sky-400/30"
            body-class="px-3.5 sm:px-6 lg:px-12"
          />

          <ArticleCommentPanel
            :post-slug="post.slug"
            :allow-comment="post.allowComment"
          />

          <section
            v-if="hasRelatedPosts"
            class="relative overflow-hidden rounded-[28px] border border-white/65 bg-white/88 p-4 shadow-[0_28px_76px_-48px_rgba(15,23,42,0.22)] before:pointer-events-none before:absolute before:-left-16 before:top-8 before:h-40 before:w-40 before:rounded-full before:bg-sky-200/26 before:blur-3xl after:pointer-events-none after:absolute after:-right-12 after:bottom-0 after:h-44 after:w-44 after:rounded-full after:bg-orange-200/26 after:blur-3xl dark:border-white/10 dark:bg-slate-950/76 dark:before:bg-sky-500/12 dark:after:bg-orange-400/10 sm:rounded-[36px] sm:p-6 sm:shadow-[0_34px_94px_-58px_rgba(15,23,42,0.28)]"
          >
            <div class="relative">
              <div class="border-b border-slate-200/70 pb-4 dark:border-white/10 sm:pb-5">
                <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                  继续阅读
                </p>
              </div>

              <div class="mt-5 grid min-w-0 gap-4 sm:mt-6 lg:grid-cols-2">
                <NuxtLink
                  v-for="item in relatedCompactPosts"
                  :key="item.slug"
                  :to="`/posts/${item.slug}`"
                  class="group relative min-w-0 cursor-pointer overflow-hidden rounded-[22px] border border-slate-200/80 bg-white/90 p-4 transition duration-300 hover:-translate-y-0.5 hover:border-sky-200 hover:shadow-[0_24px_54px_-40px_rgba(14,165,233,0.34)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/80 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:border-slate-800 dark:bg-slate-900/82 dark:hover:border-sky-900 dark:focus-visible:ring-sky-400/70 dark:focus-visible:ring-offset-slate-950 sm:rounded-[26px] sm:p-5"
                >
                  <div class="pointer-events-none absolute inset-y-5 left-0 w-px bg-gradient-to-b from-transparent via-sky-200 to-transparent dark:via-sky-400/35" />

                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="text-[0.72rem] font-medium text-slate-500 dark:text-slate-400">{{ item.categoryName }}</p>
                    </div>

                    <div class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-slate-200/80 bg-slate-50 text-slate-400 transition group-hover:border-sky-200 group-hover:text-sky-700 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-500 dark:group-hover:border-sky-800 dark:group-hover:text-sky-200">
                      <UIcon name="i-lucide-arrow-up-right" class="size-4" />
                    </div>
                  </div>

                  <h3 class="mt-4 text-[clamp(1.06rem,3.8vw,1.38rem)] font-semibold leading-7 tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-sky-700 dark:text-slate-50 dark:group-hover:text-sky-200">
                    {{ item.title }}
                  </h3>

                  <p class="mt-3 line-clamp-3 text-[0.9rem] leading-7 text-slate-500 dark:text-slate-400">
                    {{ item.summary }}
                  </p>

                  <div class="mt-5 flex flex-wrap gap-2.5 text-[0.72rem] text-slate-400 dark:text-slate-500">
                    <span class="inline-flex items-center gap-1.5 rounded-full border border-slate-200/80 bg-slate-50/92 px-3 py-1.5 dark:border-white/10 dark:bg-white/[0.04]">
                      <UIcon name="i-lucide-calendar-days" class="size-3.5" />
                      <span>{{ formatChineseDate(item.publishedAt) }}</span>
                    </span>
                    <span class="inline-flex items-center gap-1.5 rounded-full border border-slate-200/80 bg-slate-50/92 px-3 py-1.5 dark:border-white/10 dark:bg-white/[0.04]">
                      <UIcon name="i-lucide-clock-3" class="size-3.5" />
                      <span>{{ item.readingMinutes }} 分钟阅读</span>
                    </span>
                  </div>
                </NuxtLink>
              </div>
            </div>
          </section>
        </div>

        <aside v-if="showArticleSidebar" class="hidden space-y-5 lg:block lg:h-fit lg:self-start lg:sticky lg:top-24">
          <div
            v-if="hasToc"
            class="relative overflow-hidden rounded-[24px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.86),rgba(255,255,255,0.72))] p-4 shadow-[0_22px_48px_-40px_rgba(15,23,42,0.22)] backdrop-blur-xl before:pointer-events-none before:absolute before:inset-x-8 before:top-0 before:h-px before:bg-gradient-to-r before:from-transparent before:via-sky-200/85 before:to-transparent dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.82),rgba(15,23,42,0.72))] dark:before:via-sky-400/30"
          >
            <div class="relative">
              <div class="border-b border-slate-200/70 pb-3 dark:border-white/10">
                <div class="min-w-0">
                  <p class="text-[0.76rem] font-medium tracking-[0.18em] text-slate-400 dark:text-slate-500">
                    目录
                  </p>
                </div>
              </div>

              <div class="mt-3">
                <div ref="tocScrollContainerRef" class="max-h-[32rem] overflow-auto pr-1 lg:max-h-[calc(100svh-8rem)] [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.22)_transparent] [&::-webkit-scrollbar]:h-2 [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/45 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/38">
                  <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
                </div>
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
        class="fixed bottom-5 right-4 z-40 lg:hidden"
      >
        <button
          type="button"
          class="inline-flex min-h-11 items-center gap-2 rounded-full border border-white/65 bg-[linear-gradient(180deg,rgba(255,255,255,0.9),rgba(255,255,255,0.78))] px-3.5 py-2 text-[0.78rem] font-medium text-slate-600 shadow-[0_16px_34px_-28px_rgba(15,23,42,0.22)] backdrop-blur-xl transition duration-200 hover:border-sky-200/80 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.88),rgba(15,23,42,0.8))] dark:text-slate-300 dark:hover:border-sky-400/20 dark:hover:text-white dark:focus-visible:ring-sky-400/25 dark:focus-visible:ring-offset-slate-950"
          @click="openMobileTocDrawer"
        >
          <UIcon name="i-lucide-align-left" class="size-4 text-sky-500/70 dark:text-sky-300/70" />
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
        class="fixed inset-0 z-50 bg-slate-950/36 backdrop-blur-[2px] lg:hidden"
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
        class="fixed inset-x-0 bottom-0 z-[60] mx-auto max-w-2xl rounded-t-[26px] border border-white/65 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(255,255,255,0.88))] px-4 pb-[calc(env(safe-area-inset-bottom)+1rem)] pt-3 shadow-[0_-24px_60px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl lg:hidden dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.94),rgba(15,23,42,0.9))]"
        aria-label="文章目录"
        aria-modal="true"
        role="dialog"
      >
        <div class="mx-auto mb-3 h-1.5 w-14 rounded-full bg-slate-200/90 dark:bg-slate-700/80" />

        <div class="flex items-start justify-between gap-3 border-b border-slate-200/70 pb-3 dark:border-white/10">
          <div class="min-w-0">
            <p class="text-[0.76rem] font-medium tracking-[0.18em] text-slate-400 dark:text-slate-500">
              目录
            </p>
          </div>

          <button
            type="button"
            class="inline-flex h-10 w-10 items-center justify-center text-slate-400 transition hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:text-slate-500 dark:hover:text-white dark:focus-visible:ring-sky-400/25 dark:focus-visible:ring-offset-slate-950"
            aria-label="关闭目录"
            @click="closeMobileTocDrawer"
          >
            <UIcon name="i-lucide-x" class="size-4" />
          </button>
        </div>

        <div class="mt-3">
          <div class="max-h-[60svh] overflow-auto pb-2 pr-1 [scrollbar-width:thin] [scrollbar-color:rgba(148,163,184,0.22)_transparent] [&::-webkit-scrollbar]:w-1 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/50 dark:[&::-webkit-scrollbar-thumb]:bg-slate-500/45">
            <ArticleTocTree :items="tocItems" :active-id="activeTocId" @select="handleTocSelect" />
          </div>
        </div>
      </section>
    </Transition>
  </main>
</template>
