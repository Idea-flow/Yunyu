<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted } from 'vue'
import QRCode from 'qrcode'
import { formatChineseDate } from '~/utils/date'
import type { ArticleTocItem } from '../../types/post'
import type { ContentAccessRuleType } from '../../types/content-access'
import ArticleContentRenderer from '../../components/content/ArticleContentRenderer.vue'
import ArticleCommentPanel from '../../components/content/ArticleCommentPanel.vue'
import ArticleTocTree from '../../components/content/ArticleTocTree.vue'
import ShareQrCard from '../../components/content/ShareQrCard.vue'
import YunyuVideo from '../../components/common/YunyuVideo.vue'
import PostCoverHero from '~/components/common/PostCoverHero.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuPoetryTypewriter from '~/components/common/YunyuPoetryTypewriter.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'

type ArticleContentTheme = 'editorial' | 'documentation' | 'minimal'
type ArticleCodeTheme = 'github-light' | 'github-dark'

/**
 * 前台文章详情页。
 * 作用：接入后端真实公开接口，展示文章正文、目录与相关推荐内容。
 */
const route = useRoute()
const siteContent = useSiteContent()
const auth = useAuth()
const colorMode = useColorMode()
const yunyuToast = useYunyuToast()
const activeTocId = ref('')
const readingProgress = ref(0)
const mobileTocOpen = ref(false)
const mobileTocVisible = ref(false)
const lastWindowScrollTop = ref(0)
const isTocManualScrolling = ref(false)
const isSharingArticle = ref(false)
const isSharePanelOpen = ref(false)
const isWechatShareModalOpen = ref(false)
const isAccessModalOpen = ref(false)
const wechatShareQrCodeDataUrl = ref('')
const activeAccessScope = ref<'ARTICLE' | 'TAIL_HIDDEN'>('ARTICLE')
const articleAccessCodeInput = ref('')
const wechatAccessCodeInput = ref('')
const isAccessSubmitting = ref(false)
const articleContentRef = ref<HTMLElement | null>(null)
const tocScrollContainerRef = ref<HTMLElement | null>(null)
const hasReportedViewCount = ref(false)
let tocObserver: IntersectionObserver | null = null
let tocManualScrollTimer: number | null = null
let viewCountReportTimer: ReturnType<typeof setTimeout> | null = null
const ARTICLE_CONTENT_THEME_STORAGE_KEY = 'yunyu-post-content-theme'
const articleContentThemeOptions: Array<{ value: ArticleContentTheme, label: string, hint: string }> = [
  { value: 'editorial', label: '杂志感', hint: '更有内容阅读氛围' },
  { value: 'documentation', label: '文档感', hint: '结构更规整清楚' },
  { value: 'minimal', label: '极简', hint: '更轻更紧凑' }
]
const selectedArticleContentTheme = ref<ArticleContentTheme>('editorial')

const { data, error, refresh } = await useAsyncData(`site-post-${route.params.slug}`, async () => {
  return await siteContent.getPostDetail(String(route.params.slug || ''))
})

if (error.value) {
  throw createError({
    statusCode: 404,
    statusMessage: error.value.message || '文章不存在'
  })
}

const post = computed(() => data.value)
const isLoggedIn = computed(() => Boolean(auth.currentUser.value || post.value?.contentAccessState?.loggedIn))

/**
 * 计算文章正文是否已解锁。
 * 作用：统一控制正文、评论区以及整篇文章解锁引导的显示逻辑。
 */
const articleAccessAllowed = computed(() => Boolean(post.value?.contentAccessState?.articleAccessAllowed))

/**
 * 计算文章级未完成规则列表。
 * 作用：为整篇文章未解锁时的按钮、提示和弹窗入口提供统一数据源。
 */
const articlePendingRuleTypes = computed<ContentAccessRuleType[]>(() => {
  return (post.value?.contentAccessState?.articleAccessPendingRuleTypes || []) as ContentAccessRuleType[]
})

/**
 * 计算尾部隐藏内容未完成规则列表。
 * 作用：为尾部隐藏内容模块的独立提示与校验入口提供统一数据源。
 */
const tailHiddenPendingRuleTypes = computed<ContentAccessRuleType[]>(() => {
  return (post.value?.contentAccessState?.tailHiddenAccessPendingRuleTypes || []) as ContentAccessRuleType[]
})

/**
 * 获取规则优先级。
 * 作用：让详情页按“先登录，再其他校验”的节奏逐步提示，不一次抛出全部限制条件。
 *
 * @param ruleType 规则类型
 * @returns 优先级数值，数值越小优先级越高
 */
function getRulePriority(ruleType: ContentAccessRuleType) {
  if (ruleType === 'LOGIN') {
    return 1
  }
  if (ruleType === 'ACCESS_CODE') {
    return 2
  }
  if (ruleType === 'WECHAT_ACCESS_CODE') {
    return 3
  }
  return 99
}

/**
 * 获取当前需要先处理的规则。
 * 作用：从未完成规则中挑出优先级最高的一条，作为当前界面唯一提示步骤。
 *
 * @param ruleTypes 未完成规则列表
 * @returns 当前优先规则
 */
function getPrimaryPendingRule(ruleTypes: ContentAccessRuleType[]) {
  if (!ruleTypes.length) {
    return null
  }

  return [...ruleTypes].sort((first, second) => getRulePriority(first) - getRulePriority(second))[0] || null
}

/**
 * 计算文章当前主规则。
 * 作用：整篇文章访问控制只展示当前最该完成的一步。
 */
const articlePrimaryPendingRule = computed(() => getPrimaryPendingRule(articlePendingRuleTypes.value))

/**
 * 计算尾部隐藏内容当前主规则。
 * 作用：尾部隐藏内容拥有独立规则时，也按单步方式提示。
 */
const tailHiddenPrimaryPendingRule = computed(() => getPrimaryPendingRule(tailHiddenPendingRuleTypes.value))

/**
 * 判断文章当前是否优先要求登录。
 * 作用：只在当前主规则是登录时展示登录按钮。
 */
const articleRequiresLogin = computed(() => articlePrimaryPendingRule.value === 'LOGIN')

/**
 * 判断文章当前是否优先要求文章访问码。
 * 作用：只在当前主规则是文章访问码时展示访问码入口。
 */
const articleRequiresAccessCode = computed(() => articlePrimaryPendingRule.value === 'ACCESS_CODE')

/**
 * 判断文章当前是否优先要求公众号验证码。
 * 作用：只在当前主规则是公众号验证码时展示公众号校验入口。
 */
const articleRequiresWechatCode = computed(() => articlePrimaryPendingRule.value === 'WECHAT_ACCESS_CODE')

/**
 * 判断尾部隐藏内容当前是否优先要求登录。
 * 作用：只在尾部隐藏内容当前主规则为登录时展示登录按钮。
 */
const tailHiddenRequiresLogin = computed(() => tailHiddenPrimaryPendingRule.value === 'LOGIN')

/**
 * 判断尾部隐藏内容当前是否优先要求访问码。
 * 作用：尾部隐藏内容独立配置文章访问码时，也复用统一访问码输入弹窗。
 */
const tailHiddenRequiresAccessCode = computed(() => tailHiddenPrimaryPendingRule.value === 'ACCESS_CODE')

/**
 * 判断尾部隐藏内容当前是否优先要求公众号验证码。
 * 作用：只在尾部隐藏内容当前主规则为公众号验证码时展示校验入口。
 */
const tailHiddenRequiresWechatCode = computed(() => tailHiddenPrimaryPendingRule.value === 'WECHAT_ACCESS_CODE')

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
  return colorMode.value === 'dark' ? 'github-dark' : 'github-light'
})
const relatedCompactPosts = computed(() => post.value?.relatedPosts?.slice(0, 2) || [])

/**
 * 判断详情页是否存在延伸阅读。
 * 作用：当后端未返回相关推荐时隐藏“继续阅读”区块，避免正文后出现空白导览容器。
 */
const hasRelatedPosts = computed(() => (post.value?.relatedPosts?.length || 0) > 0)
const hasTailHiddenBlock = computed(() => Boolean(post.value?.contentAccessConfig?.tailHiddenAccess?.enabled))
const tailHiddenAccessAllowed = computed(() => Boolean(post.value?.contentAccessState?.tailHiddenAccessAllowed))
const hasTailHiddenContent = computed(() => Boolean(post.value?.tailHiddenContentHtml))

/**
 * 计算当前弹窗对应的待校验规则列表。
 * 作用：在同一个弹窗中同时支持文章级与尾部隐藏内容两类场景，避免重复维护两套界面。
 */
const activeAccessPendingRuleTypes = computed<ContentAccessRuleType[]>(() => {
  const activeRule = activeAccessScope.value === 'ARTICLE'
    ? articlePrimaryPendingRule.value
    : tailHiddenPrimaryPendingRule.value
  return activeRule ? [activeRule] : []
})

/**
 * 计算当前弹窗标题。
 * 作用：根据当前解锁范围切换弹窗文案，让用户更清楚自己正在解锁哪一层内容。
 */
const accessModalTitle = computed(() => {
  return activeAccessScope.value === 'ARTICLE' ? '解锁文章内容' : `解锁${post.value?.tailHiddenTitle || '隐藏内容'}`
})

/**
 * 判断弹窗内是否需要文章访问码输入框。
 * 作用：仅在文章级场景且待校验规则包含 `ACCESS_CODE` 时展示对应输入区域。
 */
const accessModalRequiresArticleCode = computed(() => {
  return activeAccessPendingRuleTypes.value.includes('ACCESS_CODE')
})

/**
 * 判断弹窗内是否需要公众号验证码输入框。
 * 作用：仅在当前范围仍待校验公众号验证码时展示对应输入区域。
 */
const accessModalRequiresWechatCode = computed(() => {
  return activeAccessPendingRuleTypes.value.includes('WECHAT_ACCESS_CODE')
})

/**
 * 计算文章访问码提示文案。
 * 作用：在文章启用了访问码规则时，把后台配置的提示语展示给读者。
 */
const articleAccessCodeHint = computed(() => {
  return post.value?.contentAccessConfig?.articleAccess?.articleAccessCodeHint || '请输入文章访问验证码'
})

/**
 * 计算当前文章分享标题。
 * 作用：统一组织原生分享与复制链接时使用的标题文本，避免不同分享入口出现不一致文案。
 */
const shareTitle = computed(() => post.value?.title || '云屿文章')

/**
 * 计算当前文章分享描述。
 * 作用：为支持原生分享描述字段的环境补充一段简短摘要，没有摘要时回退站点默认文案。
 */
const shareDescription = computed(() => post.value?.summary || '来自云屿的文章分享')

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
 * 转换访问规则展示文案。
 * 作用：把服务端规则标识映射为前台可直接展示的中文说明。
 *
 * @param ruleType 规则类型
 * @returns 中文标签
 */
function getRuleLabel(ruleType: ContentAccessRuleType) {
  if (ruleType === 'LOGIN') {
    return '登录后可见'
  }
  if (ruleType === 'WECHAT_ACCESS_CODE') {
    return '公众号验证码'
  }
  if (ruleType === 'ACCESS_CODE') {
    return '文章访问码'
  }
  return ruleType
}

/**
 * 获取当前主规则的简短说明。
 * 作用：把解锁提示压缩成一句话，降低页面噪音。
 *
 * @param ruleType 规则类型
 * @returns 简短提示文案
 */
function getRuleHint(ruleType: ContentAccessRuleType | null) {
  if (ruleType === 'LOGIN') {
    return '请先登录'
  }
  if (ruleType === 'ACCESS_CODE') {
    return articleAccessCodeHint.value
  }
  if (ruleType === 'WECHAT_ACCESS_CODE') {
    return post.value?.contentAccessState?.wechatAccessCodeHint || '请输入公众号验证码'
  }
  return '当前内容暂不可见'
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

watch(() => post.value?.id, () => {
  hasReportedViewCount.value = false
  clearViewCountReportTimer()
  scheduleViewCountReport()
})

watch(isWechatShareModalOpen, async value => {
  if (!value) {
    return
  }

  await ensureWechatShareQrCode()
})

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
 * 跳转到登录页继续解锁。
 * 作用：保留当前文章地址作为回跳参数，让用户登录后能直接回到当前详情页。
 */
async function goToLoginForAccess() {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

/**
 * 打开内容访问校验弹窗。
 * 作用：根据当前解锁范围切换弹窗上下文，复用同一套验证码输入交互。
 *
 * @param scopeType 解锁范围
 */
function openAccessModal(scopeType: 'ARTICLE' | 'TAIL_HIDDEN') {
  activeAccessScope.value = scopeType
  articleAccessCodeInput.value = ''
  wechatAccessCodeInput.value = ''
  isAccessModalOpen.value = true
}

/**
 * 提交内容访问校验。
 * 作用：向后端提交文章访问码或公众号验证码，校验成功后刷新详情页状态并关闭弹窗。
 *
 * @param ruleType 规则类型
 */
async function submitAccessVerification(ruleType: 'ACCESS_CODE' | 'WECHAT_ACCESS_CODE') {
  if (!post.value?.slug || isAccessSubmitting.value) {
    return
  }

  const accessCode = (ruleType === 'ACCESS_CODE' ? articleAccessCodeInput.value : wechatAccessCodeInput.value).trim()

  if (!accessCode) {
    yunyuToast.warning(ruleType === 'ACCESS_CODE' ? '请输入文章访问验证码' : '请输入公众号验证码')
    return
  }

  isAccessSubmitting.value = true

  try {
    await siteContent.verifyPostAccess(post.value.slug, {
      scopeType: activeAccessScope.value,
      ruleType,
      accessCode
    })
    await refresh()
    isAccessModalOpen.value = false
    articleAccessCodeInput.value = ''
    wechatAccessCodeInput.value = ''
    yunyuToast.success('校验通过，内容已解锁')
  } catch (error: any) {
    yunyuToast.error('校验失败', error?.message || '访问验证码校验失败，请稍后重试。')
  } finally {
    isAccessSubmitting.value = false
  }
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
 * 恢复详情页主题偏好。
 * 作用：首次进入文章详情页时，从本地缓存中读取用户上一次选择的正文排版主题。
 */
function hydrateArticleThemePreference() {
  if (!import.meta.client) {
    return
  }

  const savedContentTheme = window.localStorage.getItem(ARTICLE_CONTENT_THEME_STORAGE_KEY)

  if (savedContentTheme === 'editorial' || savedContentTheme === 'documentation' || savedContentTheme === 'minimal') {
    selectedArticleContentTheme.value = savedContentTheme
  }
}

/**
 * 清理浏览量上报定时器。
 * 作用：在文章切换或页面卸载时取消尚未执行的浏览量上报，
 * 避免旧文章详情页残留的延迟任务误记到新页面。
 */
function clearViewCountReportTimer() {
  if (!import.meta.client || viewCountReportTimer === null) {
    return
  }

  window.clearTimeout(viewCountReportTimer)
  viewCountReportTimer = null
}

/**
 * 上报当前文章浏览量。
 * 作用：在前台正文完成客户端挂载后，异步调用后端独立浏览量接口，
 * 不阻塞文章详情首屏渲染，也避免同一页面重复上报。
 */
async function reportViewCount() {
  if (!import.meta.client || hasReportedViewCount.value || !post.value?.id) {
    return
  }

  hasReportedViewCount.value = true

  try {
    await siteContent.increasePostViewCount(post.value.id)
  } catch {
    hasReportedViewCount.value = false
  }
}

/**
 * 延迟安排浏览量上报。
 * 作用：让文章内容先完成渲染，再在客户端延迟一次轻量上报，
 * 过滤掉刚进入详情页就立即离开的访问。
 */
function scheduleViewCountReport() {
  if (!import.meta.client || hasReportedViewCount.value || !post.value?.id) {
    return
  }

  clearViewCountReportTimer()
  viewCountReportTimer = window.setTimeout(() => {
    viewCountReportTimer = null
    void reportViewCount()
  }, 1000)
}

/**
 * 读取当前文章分享地址。
 * 作用：优先复用浏览器当前地址，确保复制与原生分享都指向用户正在阅读的真实详情页地址。
 */
function getShareUrl() {
  if (!import.meta.client) {
    return ''
  }

  return window.location.href
}

/**
 * 复制当前文章链接。
 * 作用：在原生分享不可用时回退为复制链接，保证桌面端和部分受限环境仍能完成分享动作。
 */
async function copyShareUrl() {
  await copyText(getShareUrl())
}

/**
 * 复制指定文本。
 * 作用：统一处理不同平台的分享文案复制逻辑，避免各平台按钮重复操作剪贴板。
 *
 * @param text 需要复制的文本
 */
async function copyText(text: string) {
  if (!text) {
    throw new Error('当前分享内容不可用')
  }

  if (!navigator.clipboard?.writeText) {
    throw new Error('当前环境不支持剪贴板')
  }

  await navigator.clipboard.writeText(text)
}

/**
 * 生成小红书分享文案。
 * 作用：组织适合复制到小红书的标题、摘要和链接内容，降低用户二次整理成本。
 */
function buildXiaohongshuShareText() {
  return [
    shareTitle.value,
    shareDescription.value,
    '',
    `原文链接：${getShareUrl()}`
  ].filter(Boolean).join('\n')
}

/**
 * 生成技术社区分享文案。
 * 作用：为 `CSDN` 和掘金生成 Markdown 风格内容，便于直接粘贴到编辑器中。
 *
 * @param platformName 平台名称
 * @returns 平台分享文案
 */
function buildCommunityShareText(platformName: string) {
  return [
    `# ${shareTitle.value}`,
    '',
    shareDescription.value,
    '',
    `原文链接：${getShareUrl()}`,
    '',
    `转载到${platformName}时可按需补充你的导读或使用感受。`
  ].filter(Boolean).join('\n')
}

/**
 * 打开外部平台页面。
 * 作用：在复制平台文案后顺手打开目标站点，减少用户再手动寻找入口的步骤。
 *
 * @param url 平台地址
 */
function openExternalPlatform(url: string) {
  if (!import.meta.client || !url) {
    return
  }

  window.open(url, '_blank', 'noopener,noreferrer')
}

/**
 * 生成微信分享二维码。
 * 作用：在前端本地把当前文章链接编码为二维码图片，避免依赖外部二维码服务。
 */
async function ensureWechatShareQrCode() {
  if (!import.meta.client || wechatShareQrCodeDataUrl.value) {
    return
  }

  const shareUrl = getShareUrl()

  if (!shareUrl) {
    return
  }

  try {
    wechatShareQrCodeDataUrl.value = await QRCode.toDataURL(shareUrl, {
      margin: 1,
      width: 240,
      color: {
        dark: colorMode.value === 'dark' ? '#F8FAFC' : '#0F172A',
        light: colorMode.value === 'dark' ? '#1F2937' : '#FFFFFF'
      }
    })
  } catch {
    wechatShareQrCodeDataUrl.value = ''
    yunyuToast.error('二维码生成失败', '暂时无法生成微信分享二维码。')
  }
}

/**
 * 处理系统分享。
 * 作用：在分享面板里提供系统级分享能力，不支持时自动回退为复制链接。
 */
async function handleNativeShare() {
  isSharePanelOpen.value = false

  if (!import.meta.client || isSharingArticle.value) {
    return
  }

  isSharingArticle.value = true

  try {
    const shareUrl = getShareUrl()

    if (!shareUrl) {
      throw new Error('当前分享地址不可用')
    }

    if (typeof navigator.share === 'function') {
      await navigator.share({
        title: shareTitle.value,
        text: shareDescription.value,
        url: shareUrl
      })
      return
    }

    await copyShareUrl()
    yunyuToast.success('文章链接已复制')
  } catch (error: any) {
    if (error?.name === 'AbortError') {
      return
    }

    try {
      await copyShareUrl()
      yunyuToast.success('文章链接已复制')
    } catch {
      yunyuToast.error('分享失败', '当前环境暂时无法分享或复制链接。')
    }
  } finally {
    isSharingArticle.value = false
  }
}

/**
 * 打开微信分享弹窗。
 * 作用：在分享面板中展示二维码，方便用户用微信扫码打开当前文章。
 */
function handleWechatShare() {
  isSharePanelOpen.value = false
  isWechatShareModalOpen.value = true
}

/**
 * 处理小红书分享。
 * 作用：复制适合小红书的分享文案，并打开小红书首页供用户继续发布。
 */
async function handleXiaohongshuShare() {
  isSharePanelOpen.value = false

  try {
    await copyText(buildXiaohongshuShareText())
    openExternalPlatform('https://www.xiaohongshu.com/')
    yunyuToast.success('小红书分享文案已复制')
  } catch {
    yunyuToast.error('复制失败', '暂时无法复制小红书分享文案。')
  }
}

/**
 * 处理技术社区分享。
 * 作用：复制适合技术社区编辑器的 Markdown 文案，并打开对应平台。
 *
 * @param platformName 平台名称
 * @param platformUrl 平台地址
 */
async function handleCommunityShare(platformName: string, platformUrl: string) {
  isSharePanelOpen.value = false

  try {
    await copyText(buildCommunityShareText(platformName))
    openExternalPlatform(platformUrl)
    yunyuToast.success(`${platformName}分享文案已复制`)
  } catch {
    yunyuToast.error('复制失败', `暂时无法复制${platformName}分享文案。`)
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

  auth.hydratePersistedUser()
  const previousLoggedIn = Boolean(post.value?.contentAccessState?.loggedIn)
  const currentUser = await auth.fetchCurrentUser()
  if (Boolean(currentUser) !== previousLoggedIn) {
    await refresh()
  }

  await nextTick()
  hydrateArticleThemePreference()
  observeArticleHeadings()
  syncReadingProgress()
  syncMobileTocVisibility()
  scheduleViewCountReport()
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

  clearViewCountReportTimer()

  window.removeEventListener('scroll', syncReadingProgress)
  window.removeEventListener('scroll', syncMobileTocVisibility)
  window.removeEventListener('resize', syncReadingProgress)
  window.removeEventListener('resize', syncMobileTocVisibility)
})
</script>

<template>
  <main class="min-h-screen overflow-x-clip bg-white text-slate-900 dark:bg-[#020617] dark:text-slate-100">
    <div class="fixed inset-x-0 top-0 z-40 h-1.5 bg-transparent">
      <div
        class="h-full rounded-r-full bg-[linear-gradient(90deg,rgba(125,211,252,0.96)_0%,rgba(96,165,250,0.92)_52%,rgba(196,181,253,0.88)_100%)] shadow-[0_10px_24px_-14px_rgba(96,165,250,0.42)] transition-[width] duration-300 ease-out dark:bg-[linear-gradient(90deg,rgba(56,189,248,0.9)_0%,rgba(96,165,250,0.86)_54%,rgba(129,140,248,0.74)_100%)] dark:shadow-[0_10px_24px_-14px_rgba(56,189,248,0.32)]"
        :style="{ width: `${readingProgress}%` }"
      />
    </div>

    <template v-if="post">
      <PostCoverHero
        :src="post.coverUrl"
        :alt="post.title"
        motion-mode="clouds"
      >
        <template #center>
          <YunyuPoetryTypewriter variant="sky" />
        </template>

        <div class="max-w-[52rem] min-w-0">
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

            <div class="mt-5 flex flex-wrap items-center gap-x-3 gap-y-2 text-[0.74rem] text-white/82 sm:gap-x-4 sm:text-[0.78rem]">
              <span class="font-medium text-white">作者 {{ post.authorName }}</span>
              <span class="h-1 w-1 rounded-full bg-white/35" />
              <span>发布 {{ postPublishedAtLabel }}</span>
              <span class="h-1 w-1 rounded-full bg-white/35" />
              <span>阅读 {{ post.readingMinutes }} 分钟</span>
              <span class="h-1 w-1 rounded-full bg-white/35" />
              <span>热度 {{ post.viewCount }} 次浏览</span>
            </div>

            <div class="mt-4 flex flex-wrap items-center gap-2.5 sm:mt-5">
              <div class="flex items-center gap-1 rounded-full">
                <button
                  v-for="theme in articleContentThemeOptions"
                  :key="theme.value"
                  type="button"
                  class="inline-flex cursor-pointer items-center rounded-full px-3 py-1.5 text-[0.72rem] font-medium transition sm:px-3.5 sm:text-[0.76rem]"
                  :class="selectedArticleContentTheme === theme.value
                    ? 'bg-white/18 text-white shadow-[0_14px_32px_-24px_rgba(15,23,42,0.5)] backdrop-blur-md'
                    : 'bg-transparent text-white/66 hover:text-white'"
                  :title="theme.hint"
                  @click="switchArticleContentTheme(theme.value)"
                >
                  {{ theme.label }}
                </button>
              </div>

              <UPopover
                v-model:open="isSharePanelOpen"
                mode="click"
                :arrow="true"
                :content="{
                  side: 'bottom',
                  align: 'start',
                  sideOffset: 12
                }"
                :ui="{
                  content: 'w-[13rem] max-w-[calc(100vw-2rem)] rounded-[18px] border border-slate-200/85 bg-white/97 p-2 text-slate-900 shadow-[0_22px_54px_-30px_rgba(15,23,42,0.22)] backdrop-blur-xl dark:border-white/10 dark:bg-[rgba(7,14,26,0.95)] dark:text-white dark:shadow-[0_24px_60px_-34px_rgba(0,0,0,0.55)] sm:w-[13.5rem]'
                }"
              >
                <button
                  type="button"
                  class="inline-flex h-9 w-9 cursor-pointer items-center justify-center rounded-full bg-transparent text-white/84 transition hover:text-white disabled:cursor-not-allowed disabled:opacity-70 sm:h-10 sm:w-10"
                  :disabled="isSharingArticle"
                  :aria-label="isSharingArticle ? '分享中' : '分享这篇文章'"
                  :title="isSharingArticle ? '分享中...' : '分享这篇文章'"
                >
                  <UIcon
                    :name="isSharingArticle ? 'i-lucide-loader-circle' : 'i-lucide-share-2'"
                    class="size-4"
                    :class="isSharingArticle ? 'animate-spin' : ''"
                  />
                </button>

                <template #content>
                  <div>
                    <div class="space-y-1">
                    <button
                      type="button"
                      class="flex min-h-10 w-full items-center gap-2.5 rounded-[12px] border border-transparent px-2.5 py-2 text-left text-[0.82rem] text-slate-700 transition hover:border-slate-200/90 hover:bg-slate-50 hover:text-slate-950 dark:text-white/90 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-white sm:min-h-9 sm:text-[0.8rem]"
                      @click="handleNativeShare"
                    >
                      <span class="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-[10px] bg-[linear-gradient(135deg,#0f172a,#334155)] text-white shadow-[0_14px_26px_-18px_rgba(15,23,42,0.55)] dark:bg-[linear-gradient(135deg,#f8fafc,#cbd5e1)] dark:text-slate-900">
                        <UIcon name="i-lucide-share-2" class="size-3.5" />
                      </span>
                      <span class="min-w-0 truncate font-medium">发送到设备</span>
                    </button>

                    <button
                      type="button"
                      class="flex min-h-10 w-full items-center gap-2.5 rounded-[12px] border border-transparent px-2.5 py-2 text-left text-[0.82rem] text-slate-700 transition hover:border-slate-200/90 hover:bg-slate-50 hover:text-slate-950 dark:text-white/90 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-white sm:min-h-9 sm:text-[0.8rem]"
                      @click="handleWechatShare"
                    >
                      <span class="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-[10px] bg-[linear-gradient(135deg,#f7fff9,#d9fbe7)] text-[#07C160] shadow-[0_14px_26px_-18px_rgba(34,197,94,0.38)] ring-1 ring-[#07C160]/10 dark:bg-[linear-gradient(135deg,#18251f,#101915)] dark:text-[#3ddc84] dark:ring-white/8">
                        <Icon name="social:wechat" class="size-[0.9rem]" />
                      </span>
                      <span class="min-w-0 truncate font-medium">微信</span>
                    </button>

                    <button
                      type="button"
                      class="flex min-h-10 w-full items-center gap-2.5 rounded-[12px] border border-transparent px-2.5 py-2 text-left text-[0.82rem] text-slate-700 transition hover:border-slate-200/90 hover:bg-slate-50 hover:text-slate-950 dark:text-white/90 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-white sm:min-h-9 sm:text-[0.8rem]"
                      @click="handleXiaohongshuShare"
                    >
                      <span class="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-[10px] bg-[linear-gradient(135deg,#fff5f7,#ffe0e6)] text-[#FF2442] shadow-[0_14px_26px_-18px_rgba(255,36,66,0.3)] ring-1 ring-[#FF2442]/10 dark:bg-[linear-gradient(135deg,#2a151b,#180d12)] dark:text-[#ff6b83] dark:ring-white/8">
                        <Icon name="social:xiaohongshu" class="size-[0.9rem]" />
                      </span>
                      <span class="min-w-0 truncate font-medium">小红书</span>
                    </button>

                    <button
                      type="button"
                      class="flex min-h-10 w-full items-center gap-2.5 rounded-[12px] border border-transparent px-2.5 py-2 text-left text-[0.82rem] text-slate-700 transition hover:border-slate-200/90 hover:bg-slate-50 hover:text-slate-950 dark:text-white/90 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-white sm:min-h-9 sm:text-[0.8rem]"
                      @click="handleCommunityShare('CSDN', 'https://www.csdn.net/')"
                    >
                      <span class="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-[10px] bg-[linear-gradient(135deg,#fff6f2,#ffe2da)] text-[#FC5531] shadow-[0_14px_26px_-18px_rgba(252,85,49,0.3)] ring-1 ring-[#FC5531]/10 dark:bg-[linear-gradient(135deg,#2d1814,#1a0f0d)] dark:text-[#ff8c72] dark:ring-white/8">
                        <Icon name="social:csdn" class="size-[0.92rem]" />
                      </span>
                      <span class="min-w-0 truncate font-medium">CSDN</span>
                    </button>

                    <button
                      type="button"
                      class="flex min-h-10 w-full items-center gap-2.5 rounded-[12px] border border-transparent px-2.5 py-2 text-left text-[0.82rem] text-slate-700 transition hover:border-slate-200/90 hover:bg-slate-50 hover:text-slate-950 dark:text-white/90 dark:hover:border-white/10 dark:hover:bg-white/6 dark:hover:text-white sm:min-h-9 sm:text-[0.8rem]"
                      @click="handleCommunityShare('掘金', 'https://juejin.cn/')"
                    >
                      <span class="inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-[10px] bg-[linear-gradient(135deg,#f3f8ff,#ddeaff)] text-[#007FFF] shadow-[0_14px_26px_-18px_rgba(0,127,255,0.3)] ring-1 ring-[#007FFF]/10 dark:bg-[linear-gradient(135deg,#102033,#0a1524)] dark:text-[#58a6ff] dark:ring-white/8">
                        <Icon name="social:juejin" class="size-[0.9rem]" />
                      </span>
                      <span class="min-w-0 truncate font-medium">掘金</span>
                    </button>
                    </div>
                  </div>
                </template>
              </UPopover>
            </div>
          </div>
        </div>
      </PostCoverHero>

      <div class="pointer-events-none relative z-10 -mt-24 h-24 bg-[linear-gradient(180deg,rgba(255,255,255,0)_0%,rgba(255,255,255,0.96)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0)_0%,rgba(2,6,23,0.96)_100%)]" />
    </template>

    <section v-if="post" class="relative z-10 mx-auto -mt-4 max-w-[1440px] px-4 pb-14 sm:-mt-8 sm:px-8 lg:-mt-10 lg:px-10 lg:pb-24">
      <div class="grid min-w-0 gap-6 sm:gap-8 lg:items-start" :class="showArticleSidebar ? 'lg:grid-cols-[minmax(0,1fr)_340px]' : ''">
        <div ref="articleContentRef" class="min-w-0 w-full max-w-full space-y-6 sm:space-y-8">
          <YunyuVideo
            v-if="hasInlineVideo"
            :video-url="post.videoUrl || ''"
            :poster-url="post.coverUrl || ''"
          />

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

          <template v-if="articleAccessAllowed">
            <ArticleContentRenderer
              :html="post.contentHtml"
              :content-theme="articleContentTheme"
              :code-theme="articleCodeTheme"
              :code-default-expanded="false"
              container-class="min-h-0 border-0 bg-transparent p-0 shadow-none"
              body-class="px-3.5 sm:px-6 lg:px-12"
            />
          </template>

          <section
            v-else
            class="overflow-hidden rounded-[28px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98),rgba(248,250,252,0.94))] p-6 shadow-[0_22px_52px_-36px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.84),rgba(2,6,23,0.92))]"
          >
            <div class="border-b border-slate-200/70 pb-5 dark:border-white/10">
              <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                内容访问控制
              </p>
              <h2 class="mt-3 text-[1.24rem] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                当前文章暂不可见
              </h2>
              <p class="mt-3 text-sm leading-7 text-slate-500 dark:text-slate-400">
                {{ getRuleHint(articlePrimaryPendingRule) }}
              </p>
            </div>

            <div class="grid gap-4 pt-6 lg:grid-cols-[minmax(0,1fr)_19rem]">
              <div class="rounded-[20px] border border-slate-200/75 bg-white/88 p-5 dark:border-white/10 dark:bg-white/[0.03]">
                <div class="flex flex-wrap gap-3">
                  <button
                    v-if="articleRequiresLogin"
                    type="button"
                    class="inline-flex min-h-11 items-center justify-center rounded-full bg-slate-950 px-5 text-sm font-medium text-white transition hover:bg-slate-800 dark:bg-sky-400 dark:text-slate-950 dark:hover:bg-sky-300"
                    @click="goToLoginForAccess"
                  >
                    {{ isLoggedIn ? '刷新登录状态' : '登录后继续' }}
                  </button>

                  <button
                    v-if="articleRequiresAccessCode || articleRequiresWechatCode"
                    type="button"
                    class="inline-flex min-h-11 items-center justify-center rounded-full border border-slate-300 bg-white px-5 text-sm font-medium text-slate-700 transition hover:border-sky-300 hover:text-sky-700 dark:border-white/12 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-sky-400/30 dark:hover:text-sky-200"
                    @click="openAccessModal('ARTICLE')"
                  >
                    {{ articleRequiresAccessCode ? '输入访问码' : '输入公众号验证码' }}
                  </button>
                </div>
              </div>

              <div
                v-if="articleRequiresWechatCode && post.contentAccessState?.wechatQrCodeUrl"
                class="rounded-[24px] border border-slate-200/75 bg-white/88 p-5 dark:border-white/10 dark:bg-white/[0.03]"
              >
                <p class="text-sm font-medium text-slate-900 dark:text-slate-50">扫码获取公众号验证码</p>
                <img
                  :src="post.contentAccessState.wechatQrCodeUrl"
                  alt="公众号二维码"
                  class="mt-4 h-48 w-48 rounded-[18px] border border-slate-200/80 object-cover dark:border-white/10"
                >
              </div>
            </div>
          </section>

          <section
            v-if="articleAccessAllowed && hasTailHiddenBlock"
            class="overflow-hidden rounded-[28px] border border-slate-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.96),rgba(248,250,252,0.92))] p-6 shadow-[0_22px_52px_-36px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(2,6,23,0.88))]"
          >
            <div>
              <h2 class="text-[1.22rem] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                {{ post.tailHiddenTitle || '隐藏内容' }}
              </h2>
            </div>

            <div v-if="tailHiddenAccessAllowed && hasTailHiddenContent" class="pt-6">
              <div class="h-[32rem] overflow-hidden rounded-[22px] border border-slate-200/70 bg-white/70 p-3 shadow-[inset_0_1px_0_rgba(255,255,255,0.55)] dark:border-white/10 dark:bg-slate-950/36">
                <div class="h-full overflow-auto">
                  <ArticleContentRenderer
                    :html="post.tailHiddenContentHtml"
                    :content-theme="articleContentTheme"
                    :code-theme="articleCodeTheme"
                    :code-default-expanded="false"
                    container-class="h-full min-h-0 overflow-visible border-0 bg-transparent p-0 shadow-none"
                    body-class="min-w-max px-0"
                  />
                </div>
              </div>
            </div>

            <div v-else-if="!tailHiddenAccessAllowed" class="pt-5">
              <div class="rounded-[18px] border border-dashed border-slate-300/80 bg-slate-50/80 px-4 py-4 dark:border-white/12 dark:bg-white/[0.03]">
                <p class="text-sm text-slate-500 dark:text-slate-400">
                  {{ getRuleHint(tailHiddenPrimaryPendingRule) }}
                </p>

                <div class="mt-4 flex flex-wrap gap-3">
                  <button
                    v-if="tailHiddenRequiresLogin"
                    type="button"
                    class="inline-flex min-h-10 items-center justify-center rounded-full bg-slate-950 px-4 text-sm font-medium text-white transition hover:bg-slate-800 dark:bg-sky-400 dark:text-slate-950 dark:hover:bg-sky-300"
                    @click="goToLoginForAccess"
                  >
                    {{ isLoggedIn ? '刷新登录状态' : '登录后继续' }}
                  </button>

                  <button
                    v-if="tailHiddenRequiresAccessCode || tailHiddenRequiresWechatCode"
                    type="button"
                    class="inline-flex min-h-10 items-center justify-center rounded-full border border-slate-300 bg-white px-4 text-sm font-medium text-slate-700 transition hover:border-sky-300 hover:text-sky-700 dark:border-white/12 dark:bg-white/[0.04] dark:text-slate-200 dark:hover:border-sky-400/30 dark:hover:text-sky-200"
                    @click="openAccessModal('TAIL_HIDDEN')"
                  >
                    {{ tailHiddenRequiresAccessCode ? '输入访问码' : '输入公众号验证码' }}
                  </button>
                </div>
              </div>
            </div>
          </section>

          <ArticleCommentPanel
            v-if="articleAccessAllowed"
            :post-slug="post.slug"
            :allow-comment="post.allowComment"
            :initial-comment-count="post.commentCount"
          />

          <section
            v-if="hasRelatedPosts"
            class="relative"
          >
            <div class="border-b border-slate-200/70 pb-4 dark:border-white/10 sm:pb-5">
              <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                继续阅读
              </p>
            </div>

            <div class="mt-5 grid min-w-0 gap-5 sm:mt-6 lg:grid-cols-2 lg:gap-6">
              <NuxtLink
                v-for="item in relatedCompactPosts"
                :key="item.slug"
                :to="`/posts/${item.slug}`"
                class="group relative min-w-0 cursor-pointer rounded-[20px] p-1.5 transition duration-300 hover:bg-slate-50/78 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/80 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:hover:bg-white/[0.03] dark:focus-visible:ring-sky-400/70 dark:focus-visible:ring-offset-slate-950 sm:rounded-[24px] sm:p-2"
              >
                <div
                  v-if="item.coverUrl"
                  class="relative overflow-hidden rounded-[18px] border border-slate-200/75 bg-slate-100/80 dark:border-white/10 dark:bg-slate-900/78 sm:rounded-[22px]"
                >
                  <YunyuImage
                    :src="item.coverUrl"
                    :alt="item.title"
                    wrapper-class="aspect-[16/9] h-full w-full"
                    image-class="h-full w-full object-cover transition duration-500 ease-out group-hover:scale-[1.02]"
                    rounded-class="rounded-none"
                  />
                  <div class="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.04)_0%,rgba(15,23,42,0.02)_40%,rgba(15,23,42,0.18)_100%)] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.02)_0%,rgba(2,6,23,0.06)_38%,rgba(2,6,23,0.3)_100%)]" />
                </div>

                <div class="mt-4 flex items-start justify-between gap-3">
                  <div>
                    <p class="text-[0.72rem] font-medium text-slate-500 dark:text-slate-400">{{ item.categoryName }}</p>
                  </div>

                  <div class="inline-flex h-9 w-9 shrink-0 items-center justify-center text-slate-400 transition group-hover:text-sky-700 dark:text-slate-500 dark:group-hover:text-sky-200">
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
                  <span class="inline-flex items-center gap-1.5 rounded-full border border-slate-200/75 bg-slate-50/88 px-3 py-1.5 dark:border-white/10 dark:bg-white/[0.04]">
                    <UIcon name="i-lucide-calendar-days" class="size-3.5" />
                    <span>{{ formatChineseDate(item.publishedAt) }}</span>
                  </span>
                  <span class="inline-flex items-center gap-1.5 rounded-full border border-slate-200/75 bg-slate-50/88 px-3 py-1.5 dark:border-white/10 dark:bg-white/[0.04]">
                    <UIcon name="i-lucide-clock-3" class="size-3.5" />
                    <span>{{ item.readingMinutes }} 分钟阅读</span>
                  </span>
                </div>
              </NuxtLink>
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

    <UModal
      v-model:open="isAccessModalOpen"
      :ui="{ content: 'sm:max-w-[34rem] overflow-hidden rounded-[30px] border border-slate-200/42 bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(248,250,252,0.86))] p-0 text-slate-900 shadow-[0_24px_68px_-40px_rgba(15,23,42,0.16)] backdrop-blur-[28px] dark:border-white/6 dark:bg-[linear-gradient(180deg,rgba(10,18,34,0.84),rgba(8,14,28,0.76))] dark:text-white dark:shadow-[0_24px_68px_-40px_rgba(0,0,0,0.42)]' }"
    >
      <template #content>
        <div class="p-6 sm:p-7">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-sky-600 dark:text-sky-300">
                内容访问控制
              </p>
              <h3 class="mt-3 text-[1.24rem] font-semibold tracking-[-0.03em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                {{ accessModalTitle }}
              </h3>
              <p class="mt-3 text-sm leading-7 text-slate-500 dark:text-slate-400">
                {{ getRuleHint(activeAccessPendingRuleTypes[0] || null) }}
              </p>
            </div>

            <button
              type="button"
              class="inline-flex h-10 w-10 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-700 dark:hover:bg-white/8 dark:hover:text-white"
              aria-label="关闭弹窗"
              @click="isAccessModalOpen = false"
            >
              <UIcon name="i-lucide-x" class="size-4" />
            </button>
          </div>

          <div class="mt-6 space-y-5">
            <div
              v-if="accessModalRequiresArticleCode"
              class="rounded-[22px] border border-slate-200/75 bg-white/88 p-5 dark:border-white/10 dark:bg-white/[0.03]"
            >
              <p class="text-sm font-medium text-slate-900 dark:text-slate-50">文章访问码</p>
              <input
                v-model="articleAccessCodeInput"
                type="text"
                maxlength="128"
                placeholder="请输入文章访问验证码"
                class="mt-4 h-11 w-full rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-300 focus:ring-2 focus:ring-sky-100 dark:border-white/12 dark:bg-slate-950/70 dark:text-slate-50 dark:placeholder:text-slate-500 dark:focus:border-sky-400/40 dark:focus:ring-sky-400/10"
              >
              <button
                type="button"
                class="mt-4 inline-flex min-h-11 items-center justify-center rounded-full bg-slate-950 px-5 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-sky-400 dark:text-slate-950 dark:hover:bg-sky-300"
                :disabled="isAccessSubmitting"
                @click="submitAccessVerification('ACCESS_CODE')"
              >
                {{ isAccessSubmitting ? '校验中...' : '确认' }}
              </button>
            </div>

            <div
              v-if="accessModalRequiresWechatCode"
              class="rounded-[22px] border border-slate-200/75 bg-white/88 p-5 dark:border-white/10 dark:bg-white/[0.03]"
            >
              <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_12rem]">
                <div>
                  <p class="text-sm font-medium text-slate-900 dark:text-slate-50">公众号验证码</p>
                  <input
                    v-model="wechatAccessCodeInput"
                    type="text"
                    maxlength="128"
                    placeholder="请输入公众号验证码"
                    class="mt-4 h-11 w-full rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-900 outline-none transition placeholder:text-slate-400 focus:border-sky-300 focus:ring-2 focus:ring-sky-100 dark:border-white/12 dark:bg-slate-950/70 dark:text-slate-50 dark:placeholder:text-slate-500 dark:focus:border-sky-400/40 dark:focus:ring-sky-400/10"
                  >
                  <button
                    type="button"
                    class="mt-4 inline-flex min-h-11 items-center justify-center rounded-full border border-slate-300 bg-white px-5 text-sm font-medium text-slate-700 transition hover:border-sky-300 hover:text-sky-700 disabled:cursor-not-allowed disabled:opacity-60 dark:border-white/12 dark:bg-white/[0.04] dark:text-slate-100 dark:hover:border-sky-400/30 dark:hover:text-sky-200"
                    :disabled="isAccessSubmitting"
                    @click="submitAccessVerification('WECHAT_ACCESS_CODE')"
                  >
                    {{ isAccessSubmitting ? '校验中...' : '确认' }}
                  </button>
                </div>

                <div
                  v-if="post?.contentAccessState?.wechatQrCodeUrl"
                  class="rounded-[20px] border border-slate-200/75 bg-slate-50/80 p-3 dark:border-white/10 dark:bg-white/[0.03]"
                >
                  <img
                    :src="post.contentAccessState.wechatQrCodeUrl"
                    alt="公众号二维码"
                    class="h-40 w-40 rounded-[16px] object-cover"
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </UModal>

    <UModal
      v-model:open="isWechatShareModalOpen"
      :ui="{ content: 'sm:max-w-[24rem] overflow-hidden rounded-[30px] border border-slate-200/42 bg-[linear-gradient(180deg,rgba(255,255,255,0.9),rgba(248,250,252,0.82))] p-0 text-slate-900 shadow-[0_24px_68px_-40px_rgba(15,23,42,0.14)] backdrop-blur-[28px] dark:border-white/6 dark:bg-[linear-gradient(180deg,rgba(10,18,34,0.8),rgba(8,14,28,0.72))] dark:text-white dark:shadow-[0_24px_68px_-40px_rgba(0,0,0,0.4)]' }"
    >
      <template #content>
        <ShareQrCard
          title="扫码分享"
          brand-label="WeChat"
          :poster-title="post?.title || ''"
          :qr-code-url="wechatShareQrCodeDataUrl"
          qr-code-alt="微信分享二维码"
          :share-url="getShareUrl()"
          :download-file-name="post?.title || ''"
          @close="isWechatShareModalOpen = false"
        />
      </template>
    </UModal>
  </main>
</template>
