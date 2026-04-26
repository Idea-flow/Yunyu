<script setup lang="ts">
import type {
  AdminS3ConfigForm,
  AdminS3ProfileForm,
  AdminHomepageConfigForm,
  AdminHomepageHeroStatForm,
  AdminSiteConfigForm
} from '../../types/admin-site-config'
import type { AdminPostItem } from '../../types/post'

/**
 * 后台站点设置页。
 * 作用：提供统一的站点配置与首页展示编辑入口，通过标签分组承载基础信息、SEO、视觉风格与首页首屏配置。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

/**
 * 站点设置标签键类型。
 * 作用：约束后台站点设置页所有配置分组键值，避免标签切换和保存逻辑散落不一致。
 */
type SiteConfigTabKey = 'basic' | 'seo' | 'theme' | 'homepage' | 's3'

/**
 * 站点设置标签项类型。
 * 作用：统一描述配置页标签栏的分组键值和显示信息。
 */
interface SiteConfigTabItem {
  key: SiteConfigTabKey
  label: string
  icon: string
}

const toast = useToast()
const adminSiteConfig = useAdminSiteConfig()
const adminHomepageConfig = useAdminHomepageConfig()
const adminPosts = useAdminPosts()

const isLoading = ref(false)
const isSubmitting = ref(false)
const isSavingS3Config = ref(false)
const isTestingS3Connection = ref(false)
const isActivatingS3Profile = ref(false)
const activeTab = ref<SiteConfigTabKey>('basic')
const lastSavedSiteSnapshot = ref('')
const lastSavedHomepageSnapshot = ref('')
const lastSavedS3Snapshot = ref('')
const heroVisualKeyword = ref('')
const isLoadingHeroVisualOptions = ref(false)
const heroVisualOptions = ref<AdminPostItem[]>([])
const selectedHeroVisualPost = ref<AdminPostItem | null>(null)
const selectedS3ProfileKey = ref('')
let heroVisualSearchTimer: ReturnType<typeof setTimeout> | null = null
const DEFAULT_PRIMARY_COLOR = '#38BDF8'
const DEFAULT_SECONDARY_COLOR = '#FB923C'
const primaryColorPresets = [
  '#38BDF8',
  '#0EA5E9',
  '#22C55E',
  '#14B8A6',
  '#6366F1',
  '#EC4899',
  '#F97316',
  '#F59E0B',
  '#EF4444',
  '#64748B'
] as const
const secondaryColorPresets = [
  '#FB923C',
  '#F97316',
  '#F59E0B',
  '#EAB308',
  '#A855F7',
  '#8B5CF6',
  '#10B981',
  '#06B6D4',
  '#F43F5E',
  '#94A3B8'
] as const

/**
 * 站点设置表单状态。
 * 作用：承载基础信息、SEO 和视觉风格配置字段，并与后台站点配置接口保持一致。
 */
const siteFormState = reactive<AdminSiteConfigForm>({
  siteName: '',
  siteSubTitle: '',
  footerText: '',
  logoUrl: '',
  faviconUrl: '',
  defaultTitle: '',
  defaultDescription: '',
  primaryColor: DEFAULT_PRIMARY_COLOR,
  secondaryColor: DEFAULT_SECONDARY_COLOR,
  wechatAccessCodeEnabled: false,
  wechatAccessCode: '',
  wechatAccessCodeHint: '',
  wechatQrCodeUrl: ''
})

/**
 * 首页展示表单状态。
 * 作用：承载首页无封面首屏、关键词、统计项和首页模块开关配置，并与后台首页配置接口保持一致。
 */
const homepageFormState = reactive<AdminHomepageConfigForm>({
  heroEnabled: true,
  heroLayout: 'brand',
  heroBackgroundMode: 'gradient-grid',
  heroEyebrow: 'Yunyu / 云屿',
  heroTitle: '把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站',
  heroSubtitle: '记录技术、审美、创作与阅读的个人博客与内容网站',
  heroPrimaryButtonText: '查看文章',
  heroPrimaryButtonLink: '/posts',
  heroSecondaryButtonText: '进入专题',
  heroSecondaryButtonLink: '/topics',
  heroVisualPostId: null,
  heroVisualClickable: true,
  heroKeywords: ['写作', '技术', '审美', '长期主义'],
  showHeroKeywords: true,
  showHeroStats: true,
  heroStats: [
    { label: '文章', value: '48' },
    { label: '专题', value: '12' },
    { label: '分类', value: '8' }
  ],
  showFeaturedSection: true,
  featuredSectionTitle: '推荐',
  showLatestSection: true,
  latestSectionTitle: '最新文章',
  showCategorySection: true,
  categorySectionTitle: '分类',
  showTopicSection: true,
  topicSectionTitle: '专题'
})

const homepageBackgroundOptions = [
  { label: '网格渐变', value: 'gradient-grid' },
  { label: '柔光氛围', value: 'soft-glow' },
  { label: '极简线框', value: 'minimal-lines' },
  { label: '关键词气泡', value: 'keyword-cloud' }
] as const

const defaultAllowedContentTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'video/mp4']

/**
 * 创建默认 S3 配置项。
 * 作用：为新增配置和首次进入页面时提供稳定的默认值。
 *
 * @param profileKey 配置键
 * @param enabled 是否启用
 */
function createDefaultS3Profile(profileKey: string, enabled = false): AdminS3ProfileForm {
  return {
    profileKey,
    name: enabled ? '默认配置' : '新配置',
    enabled,
    endpoint: '',
    region: 'auto',
    bucket: '',
    accessKey: '',
    secretKey: '',
    pathStyleAccess: false,
    publicBaseUrl: '',
    presignExpireSeconds: 300,
    maxFileSizeMb: 20,
    allowedContentTypes: [...defaultAllowedContentTypes]
  }
}

/**
 * S3 配置表单状态。
 * 作用：承载后台 S3 多配置列表和当前启用配置键。\n
 */
const s3FormState = reactive<AdminS3ConfigForm>({
  activeProfileKey: '',
  profiles: []
})

const tabItems: SiteConfigTabItem[] = [
  { key: 'basic', label: '基础信息', icon: 'i-lucide-badge-info' },
  { key: 'seo', label: 'SEO 配置', icon: 'i-lucide-search-check' },
  { key: 'theme', label: '视觉风格', icon: 'i-lucide-palette' },
  { key: 'homepage', label: '首页展示', icon: 'i-lucide-layout-template' },
  { key: 's3', label: 'S3 配置', icon: 'i-lucide-hard-drive-upload' }
]

/**
 * 当前是否处于首页展示标签。
 * 作用：统一控制保存行为、文案与页面右侧预览区显隐。
 */
const isHomepageTab = computed(() => activeTab.value === 'homepage')
const isS3Tab = computed(() => activeTab.value === 's3')

/**
 * 当前站点配置是否存在未保存修改。
 * 作用：比较基础信息、SEO 与视觉风格当前表单和最近一次保存快照。
 */
const siteHasUnsavedChanges = computed(() => serializeSiteFormState(siteFormState) !== lastSavedSiteSnapshot.value)

/**
 * 当前首页展示是否存在未保存修改。
 * 作用：比较首页展示当前表单和最近一次保存快照。
 */
const homepageHasUnsavedChanges = computed(() => serializeHomepageFormState(homepageFormState) !== lastSavedHomepageSnapshot.value)
const s3HasUnsavedChanges = computed(() => serializeS3FormState(s3FormState) !== lastSavedS3Snapshot.value)

/**
 * 页面整体是否存在未保存修改。
 * 作用：在页面头部统一提示当前页面是否有待保存内容。
 */
const hasUnsavedChanges = computed(() =>
  siteHasUnsavedChanges.value ||
  homepageHasUnsavedChanges.value ||
  s3HasUnsavedChanges.value
)

/**
 * 当前标签是否存在未保存修改。
 * 作用：让保存按钮和状态文案更贴合当前编辑分组。
 */
const currentTabHasUnsavedChanges = computed(() => {
  if (isHomepageTab.value) {
    return homepageHasUnsavedChanges.value
  }
  if (isS3Tab.value) {
    return s3HasUnsavedChanges.value
  }
  return siteHasUnsavedChanges.value
})

/**
 * 保存按钮文案。
 * 作用：根据当前标签切换为更准确的保存提示，避免用户不清楚保存目标。
 */
const submitButtonLabel = computed(() => {
  if (isHomepageTab.value) {
    return '保存首页展示'
  }
  if (isS3Tab.value) {
    return '保存 S3 配置'
  }
  return '保存站点配置'
})

/**
 * 保存中按钮文案。
 * 作用：根据当前标签切换保存中的状态提示。
 */
const submitLoadingLabel = computed(() => {
  if (isHomepageTab.value) {
    return '首页展示保存中...'
  }
  if (isS3Tab.value) {
    return 'S3 配置保存中...'
  }
  return '站点配置保存中...'
})

/**
 * 首页预览背景样式。
 * 作用：在后台首页展示标签内提供小型可视化预览，帮助判断背景模式和排版气质。
 */
const homepagePreviewBackgroundClass = computed(() => {
  if (homepageFormState.heroBackgroundMode === 'soft-glow') {
    return 'bg-[radial-gradient(circle_at_top,rgba(56,189,248,0.22),transparent_30%),radial-gradient(circle_at_80%_20%,rgba(249,115,22,0.18),transparent_28%),linear-gradient(180deg,#f8fafc_0%,#eef2ff_54%,#f8fafc_100%)] dark:bg-[radial-gradient(circle_at_top,rgba(56,189,248,0.2),transparent_30%),radial-gradient(circle_at_80%_20%,rgba(249,115,22,0.16),transparent_28%),linear-gradient(180deg,#020617_0%,#0f172a_54%,#020617_100%)]'
  }

  if (homepageFormState.heroBackgroundMode === 'minimal-lines') {
    return 'bg-[linear-gradient(180deg,rgba(248,250,252,0.98),rgba(248,250,252,0.98)),linear-gradient(90deg,rgba(148,163,184,0.14)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.14)_1px,transparent_1px)] bg-[size:auto,26px_26px,26px_26px] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.98),rgba(2,6,23,0.98)),linear-gradient(90deg,rgba(71,85,105,0.3)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.3)_1px,transparent_1px)] dark:bg-[size:auto,26px_26px,26px_26px]'
  }

  if (homepageFormState.heroBackgroundMode === 'keyword-cloud') {
    return 'bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.14),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(249,115,22,0.12),transparent_28%),linear-gradient(180deg,#f8fafc_0%,#f1f5f9_100%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(14,165,233,0.16),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(249,115,22,0.12),transparent_28%),linear-gradient(180deg,#020617_0%,#0f172a_100%)]'
  }

  return 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef2ff_48%,#f8fafc_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0f172a_48%,#020617_100%)]'
})

/**
 * 首页预览辅助氛围层样式。
 * 作用：为后台预览补充与前台首屏一致的辅助氛围层，帮助更真实地判断展示效果。
 */
const homepagePreviewAccentClass = computed(() => {
  if (homepageFormState.heroBackgroundMode === 'soft-glow') {
    return 'before:absolute before:-left-16 before:top-8 before:h-40 before:w-40 before:rounded-full before:bg-sky-300/25 before:blur-3xl before:content-[\"\"] after:absolute after:right-[-2rem] after:top-16 after:h-36 after:w-36 after:rounded-full after:bg-orange-300/18 after:blur-3xl after:content-[\"\"] dark:before:bg-sky-500/18 dark:after:bg-orange-400/12'
  }

  if (homepageFormState.heroBackgroundMode === 'minimal-lines') {
    return 'before:absolute before:inset-x-0 before:top-0 before:h-px before:bg-slate-200/90 before:content-[\"\"] after:absolute after:bottom-0 after:left-0 after:h-px after:w-full after:bg-slate-200/90 after:content-[\"\"] dark:before:bg-white/10 dark:after:bg-white/10'
  }

  if (homepageFormState.heroBackgroundMode === 'keyword-cloud') {
    return 'before:absolute before:left-[8%] before:top-[12%] before:text-4xl before:font-semibold before:tracking-[-0.06em] before:text-slate-200/80 before:content-[\"Writing\"] after:absolute after:right-[8%] after:bottom-[16%] after:text-3xl after:font-semibold after:tracking-[-0.06em] after:text-slate-200/60 after:content-[\"Archive\"] dark:before:text-white/6 dark:after:text-white/5'
  }

  return 'before:absolute before:inset-0 before:bg-[linear-gradient(90deg,rgba(148,163,184,0.14)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.14)_1px,transparent_1px)] before:bg-[size:24px_24px] before:content-[\"\"] before:[mask-image:linear-gradient(180deg,rgba(255,255,255,0.72),transparent)] after:absolute after:left-[14%] after:top-12 after:h-32 after:w-32 after:rounded-full after:bg-sky-300/18 after:blur-3xl after:content-[\"\"] dark:before:bg-[linear-gradient(90deg,rgba(71,85,105,0.3)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.3)_1px,transparent_1px)] dark:after:bg-sky-400/10'
})

/**
 * 读取页面全部配置。
 * 作用：在进入页面或点击刷新时同时回填站点配置和首页配置，减少来回切换时的等待。
 */
async function loadAllConfig() {
  isLoading.value = true

  try {
    const [siteResponse, homepageResponse, s3Response] = await Promise.all([
      adminSiteConfig.getSiteConfig(),
      adminHomepageConfig.getHomepageConfig(),
      adminSiteConfig.getS3Config()
    ])

    assignSiteFormState(siteResponse)
    assignHomepageFormState(homepageResponse)
    assignS3FormState(s3Response)
    await syncSelectedHeroVisualPost()
    lastSavedSiteSnapshot.value = serializeSiteFormState(siteResponse)
    lastSavedHomepageSnapshot.value = serializeHomepageFormState(homepageResponse)
    lastSavedS3Snapshot.value = serializeS3FormState(s3Response)
  } catch (error: any) {
    toast.add({
      title: '加载配置失败',
      description: error?.message || '暂时无法获取站点设置或首页展示。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 将站点配置响应同步到表单。
 *
 * @param data 站点配置数据
 */
function assignSiteFormState(data: AdminSiteConfigForm) {
  siteFormState.siteName = data.siteName || ''
  siteFormState.siteSubTitle = data.siteSubTitle || ''
  siteFormState.footerText = data.footerText || ''
  siteFormState.logoUrl = data.logoUrl || ''
  siteFormState.faviconUrl = data.faviconUrl || ''
  siteFormState.defaultTitle = data.defaultTitle || ''
  siteFormState.defaultDescription = data.defaultDescription || ''
  siteFormState.primaryColor = data.primaryColor || DEFAULT_PRIMARY_COLOR
  siteFormState.secondaryColor = data.secondaryColor || DEFAULT_SECONDARY_COLOR
  siteFormState.wechatAccessCodeEnabled = data.wechatAccessCodeEnabled ?? false
  siteFormState.wechatAccessCode = data.wechatAccessCode || ''
  siteFormState.wechatAccessCodeHint = data.wechatAccessCodeHint || ''
  siteFormState.wechatQrCodeUrl = data.wechatQrCodeUrl || ''
}

/**
 * 将首页配置响应同步到表单。
 *
 * @param data 首页配置数据
 */
function assignHomepageFormState(data: AdminHomepageConfigForm) {
  homepageFormState.heroEnabled = data.heroEnabled ?? true
  homepageFormState.heroLayout = 'brand'
  homepageFormState.heroBackgroundMode = data.heroBackgroundMode || 'gradient-grid'
  homepageFormState.heroEyebrow = data.heroEyebrow || 'Yunyu / 云屿'
  homepageFormState.heroTitle = data.heroTitle || ''
  homepageFormState.heroSubtitle = data.heroSubtitle || ''
  homepageFormState.heroPrimaryButtonText = data.heroPrimaryButtonText || '查看文章'
  homepageFormState.heroPrimaryButtonLink = data.heroPrimaryButtonLink || '/posts'
  homepageFormState.heroSecondaryButtonText = data.heroSecondaryButtonText || ''
  homepageFormState.heroSecondaryButtonLink = data.heroSecondaryButtonLink || '/topics'
  homepageFormState.heroVisualPostId = data.heroVisualPostId ?? null
  homepageFormState.heroVisualClickable = data.heroVisualClickable ?? true
  homepageFormState.heroKeywords = (data.heroKeywords || []).filter(Boolean)
  homepageFormState.showHeroKeywords = data.showHeroKeywords ?? true
  homepageFormState.showHeroStats = data.showHeroStats ?? true
  homepageFormState.heroStats = (data.heroStats || []).map(stat => ({
    label: stat.label || '',
    value: stat.value || ''
  }))
  homepageFormState.showFeaturedSection = data.showFeaturedSection ?? true
  homepageFormState.featuredSectionTitle = data.featuredSectionTitle || '推荐'
  homepageFormState.showLatestSection = data.showLatestSection ?? true
  homepageFormState.latestSectionTitle = data.latestSectionTitle || '最新文章'
  homepageFormState.showCategorySection = data.showCategorySection ?? true
  homepageFormState.categorySectionTitle = data.categorySectionTitle || '分类'
  homepageFormState.showTopicSection = data.showTopicSection ?? true
  homepageFormState.topicSectionTitle = data.topicSectionTitle || '专题'
}

/**
 * 将 S3 配置响应同步到表单。
 *
 * @param data S3 配置数据
 */
function assignS3FormState(data: AdminS3ConfigForm) {
  const profileList = (data.profiles || []).map(profile => ({
    profileKey: profile.profileKey || '',
    name: profile.name || '',
    enabled: !!profile.enabled,
    endpoint: profile.endpoint || '',
    region: profile.region || 'auto',
    bucket: profile.bucket || '',
    accessKey: profile.accessKey || '',
    secretKey: profile.secretKey || '',
    pathStyleAccess: !!profile.pathStyleAccess,
    publicBaseUrl: profile.publicBaseUrl || '',
    presignExpireSeconds: profile.presignExpireSeconds || 300,
    maxFileSizeMb: profile.maxFileSizeMb || 20,
    allowedContentTypes: profile.allowedContentTypes?.length
      ? profile.allowedContentTypes.map(item => item.trim()).filter(Boolean)
      : [...defaultAllowedContentTypes]
  }))

  s3FormState.activeProfileKey = data.activeProfileKey || ''
  s3FormState.profiles = profileList

  if (!s3FormState.profiles.length) {
    const defaultKey = `default-${Date.now()}`
    s3FormState.profiles = [createDefaultS3Profile(defaultKey, true)]
    s3FormState.activeProfileKey = defaultKey
  }

  let activeProfile = s3FormState.profiles.find(profile => profile.profileKey === s3FormState.activeProfileKey)
  if (!activeProfile) {
    activeProfile = s3FormState.profiles.find(profile => profile.enabled) || s3FormState.profiles[0]
    s3FormState.activeProfileKey = activeProfile.profileKey
  }

  s3FormState.profiles = s3FormState.profiles.map(profile => ({
    ...profile,
    enabled: profile.profileKey === s3FormState.activeProfileKey
  }))
  selectedS3ProfileKey.value = s3FormState.activeProfileKey
}

/**
 * 序列化站点配置表单状态。
 * 作用：将站点配置表单转换为稳定字符串，用于比较未保存修改状态。
 *
 * @param data 站点配置数据
 * @returns 稳定序列化后的字符串
 */
function serializeSiteFormState(data: AdminSiteConfigForm) {
  return JSON.stringify({
    siteName: data.siteName.trim(),
    siteSubTitle: data.siteSubTitle.trim(),
    footerText: data.footerText.trim(),
    logoUrl: data.logoUrl.trim(),
    faviconUrl: data.faviconUrl.trim(),
    defaultTitle: data.defaultTitle.trim(),
    defaultDescription: data.defaultDescription.trim(),
    primaryColor: data.primaryColor.trim(),
    secondaryColor: data.secondaryColor.trim(),
    wechatAccessCodeEnabled: data.wechatAccessCodeEnabled,
    wechatAccessCode: data.wechatAccessCode.trim(),
    wechatAccessCodeHint: data.wechatAccessCodeHint.trim(),
    wechatQrCodeUrl: data.wechatQrCodeUrl.trim()
  })
}

/**
 * 序列化首页配置表单状态。
 * 作用：将首页配置表单转换为稳定字符串，用于比较未保存修改状态。
 *
 * @param data 首页配置数据
 * @returns 稳定序列化后的字符串
 */
function serializeHomepageFormState(data: AdminHomepageConfigForm) {
  return JSON.stringify({
    heroEnabled: data.heroEnabled,
    heroLayout: data.heroLayout,
    heroBackgroundMode: data.heroBackgroundMode,
    heroEyebrow: data.heroEyebrow.trim(),
    heroTitle: data.heroTitle.trim(),
    heroSubtitle: data.heroSubtitle.trim(),
    heroPrimaryButtonText: data.heroPrimaryButtonText.trim(),
    heroPrimaryButtonLink: data.heroPrimaryButtonLink.trim(),
    heroSecondaryButtonText: data.heroSecondaryButtonText.trim(),
    heroSecondaryButtonLink: data.heroSecondaryButtonLink.trim(),
    heroVisualPostId: data.heroVisualPostId,
    heroVisualClickable: data.heroVisualClickable,
    heroKeywords: data.heroKeywords.map(keyword => keyword.trim()).filter(Boolean),
    showHeroKeywords: data.showHeroKeywords,
    showHeroStats: data.showHeroStats,
    heroStats: data.heroStats.map(stat => ({
      label: stat.label.trim(),
      value: stat.value.trim()
    })).filter(stat => stat.label || stat.value),
    showFeaturedSection: data.showFeaturedSection,
    featuredSectionTitle: data.featuredSectionTitle.trim(),
    showLatestSection: data.showLatestSection,
    latestSectionTitle: data.latestSectionTitle.trim(),
    showCategorySection: data.showCategorySection,
    categorySectionTitle: data.categorySectionTitle.trim(),
    showTopicSection: data.showTopicSection,
    topicSectionTitle: data.topicSectionTitle.trim()
  })
}

/**
 * 序列化 S3 配置表单状态。
 * 作用：将 S3 配置转换为稳定字符串，用于比较当前是否有未保存修改。
 *
 * @param data S3 配置数据
 * @returns 稳定序列化后的字符串
 */
function serializeS3FormState(data: AdminS3ConfigForm) {
  return JSON.stringify({
    activeProfileKey: data.activeProfileKey.trim(),
    profiles: data.profiles.map(profile => ({
      profileKey: profile.profileKey.trim(),
      name: profile.name.trim(),
      enabled: profile.enabled,
      endpoint: profile.endpoint.trim(),
      region: profile.region.trim(),
      bucket: profile.bucket.trim(),
      accessKey: profile.accessKey.trim(),
      secretKey: profile.secretKey.trim(),
      pathStyleAccess: profile.pathStyleAccess,
      publicBaseUrl: profile.publicBaseUrl.trim(),
      presignExpireSeconds: profile.presignExpireSeconds,
      maxFileSizeMb: profile.maxFileSizeMb,
      allowedContentTypes: profile.allowedContentTypes.map(item => item.trim()).filter(Boolean)
    }))
  })
}

/**
 * 切换配置标签。
 * 作用：在不同站点配置分组之间切换当前编辑视图。
 *
 * @param key 标签键值
 */
function switchTab(key: SiteConfigTabKey) {
  activeTab.value = key
  if (key === 's3' && !selectedS3ProfileKey.value && s3FormState.profiles.length) {
    selectedS3ProfileKey.value = s3FormState.activeProfileKey || s3FormState.profiles[0].profileKey
  }
}

/**
 * 校验站点配置表单。
 * 作用：在保存站点配置前进行必要字段和颜色格式校验。
 */
function validateSiteForm() {
  if (!siteFormState.siteName.trim()) {
    activeTab.value = 'basic'
    toast.add({ title: '请输入站点名称', color: 'warning' })
    return false
  }

  if (!siteFormState.siteSubTitle.trim()) {
    activeTab.value = 'basic'
    toast.add({ title: '请输入站点副标题', color: 'warning' })
    return false
  }

  if (!siteFormState.defaultTitle.trim()) {
    activeTab.value = 'seo'
    toast.add({ title: '请输入默认标题', color: 'warning' })
    return false
  }

  if (!siteFormState.defaultDescription.trim()) {
    activeTab.value = 'seo'
    toast.add({ title: '请输入默认描述', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(siteFormState.primaryColor.trim())) {
    activeTab.value = 'theme'
    toast.add({ title: '主色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(siteFormState.secondaryColor.trim())) {
    activeTab.value = 'theme'
    toast.add({ title: '辅助色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  if (siteFormState.wechatAccessCodeEnabled && !siteFormState.wechatAccessCode.trim()) {
    activeTab.value = 'basic'
    toast.add({ title: '启用公众号验证码时，请填写验证码', color: 'warning' })
    return false
  }

  return true
}

/**
 * 恢复视觉风格默认配色。
 * 作用：让站长在试过多套颜色后，可以一键回到当前系统约定的默认品牌色。
 */
function resetThemeColors() {
  siteFormState.primaryColor = DEFAULT_PRIMARY_COLOR
  siteFormState.secondaryColor = DEFAULT_SECONDARY_COLOR
}

/**
 * 校验首页配置表单。
 * 作用：在保存首页配置前进行首屏主信息、按钮和动态列表字段校验。
 */
function validateHomepageForm() {
  if (!homepageFormState.heroTitle.trim()) {
    activeTab.value = 'homepage'
    toast.add({ title: '请输入首页主标题', color: 'warning' })
    return false
  }

  if (!homepageFormState.heroSubtitle.trim()) {
    activeTab.value = 'homepage'
    toast.add({ title: '请输入首页副标题', color: 'warning' })
    return false
  }

  if (!homepageFormState.heroPrimaryButtonText.trim()) {
    activeTab.value = 'homepage'
    toast.add({ title: '请输入主按钮文案', color: 'warning' })
    return false
  }

  if (!homepageFormState.heroPrimaryButtonLink.trim()) {
    activeTab.value = 'homepage'
    toast.add({ title: '请输入主按钮跳转地址', color: 'warning' })
    return false
  }

  if (homepageFormState.heroVisualPostId !== null && homepageFormState.heroVisualPostId <= 0) {
    activeTab.value = 'homepage'
    toast.add({ title: '首屏视觉文章 ID 需大于 0', color: 'warning' })
    return false
  }

  const validKeywords = homepageFormState.heroKeywords
    .map(keyword => keyword.trim())
    .filter(Boolean)

  if (validKeywords.length > 6) {
    activeTab.value = 'homepage'
    toast.add({ title: '首页关键词最多 6 个', color: 'warning' })
    return false
  }

  const validStats = homepageFormState.heroStats.filter(stat => stat.label.trim() || stat.value.trim())
  const invalidStat = validStats.find(stat => !stat.label.trim() || !stat.value.trim())

  if (invalidStat) {
    activeTab.value = 'homepage'
    toast.add({ title: '统计项标题和值需要同时填写', color: 'warning' })
    return false
  }

  if (validStats.length > 4) {
    activeTab.value = 'homepage'
    toast.add({ title: '首页统计项最多 4 个', color: 'warning' })
    return false
  }

  return true
}

/**
 * 校验 S3 配置表单。
 * 作用：在保存 S3 配置前确保配置项完整且仅有一个启用项。
 */
function validateS3Form() {
  if (!s3FormState.profiles.length) {
    toast.add({ title: '至少保留一个 S3 配置项', color: 'warning' })
    return false
  }

  const enabledProfiles = s3FormState.profiles.filter(profile => profile.enabled)
  if (enabledProfiles.length !== 1) {
    toast.add({ title: 'S3 配置必须且只能启用一个', color: 'warning' })
    return false
  }

  if (s3FormState.activeProfileKey !== enabledProfiles[0].profileKey) {
    s3FormState.activeProfileKey = enabledProfiles[0].profileKey
  }

  for (const profile of s3FormState.profiles) {
    if (!profile.profileKey.trim()) {
      toast.add({ title: 'S3 配置键不能为空', color: 'warning' })
      return false
    }
    if (!profile.name.trim()) {
      toast.add({ title: 'S3 配置名称不能为空', color: 'warning' })
      return false
    }
    if (!profile.endpoint.trim()) {
      toast.add({ title: 'S3 Endpoint 不能为空', color: 'warning' })
      return false
    }
    if (!profile.bucket.trim()) {
      toast.add({ title: 'S3 Bucket 不能为空', color: 'warning' })
      return false
    }
    if (!profile.accessKey.trim() || !profile.secretKey.trim()) {
      toast.add({ title: 'S3 AccessKey/SecretKey 不能为空', color: 'warning' })
      return false
    }
    if (profile.presignExpireSeconds < 60 || profile.presignExpireSeconds > 3600) {
      toast.add({ title: '签名有效期需在 60-3600 秒', color: 'warning' })
      return false
    }
    if (profile.maxFileSizeMb < 1 || profile.maxFileSizeMb > 2048) {
      toast.add({ title: '最大文件大小需在 1-2048 MB', color: 'warning' })
      return false
    }
    if (!profile.allowedContentTypes.map(item => item.trim()).filter(Boolean).length) {
      toast.add({ title: '允许的 MIME 类型不能为空', color: 'warning' })
      return false
    }
  }

  const keys = s3FormState.profiles.map(profile => profile.profileKey.trim())
  if (new Set(keys).size !== keys.length) {
    toast.add({ title: 'S3 配置键不能重复', color: 'warning' })
    return false
  }

  return true
}

/**
 * 获取当前正在编辑的 S3 配置项。
 */
const currentS3Profile = computed(() =>
  s3FormState.profiles.find(profile => profile.profileKey === selectedS3ProfileKey.value) || null
)

/**
 * 新增 S3 配置项。
 * 作用：在后台站点设置页追加一个新的 S3 配置页签。
 */
function addS3Profile() {
  const profileKey = `s3-${Date.now()}`
  const profile = createDefaultS3Profile(profileKey, false)
  s3FormState.profiles.push(profile)
  selectedS3ProfileKey.value = profileKey
}

/**
 * 切换当前编辑的 S3 配置项。
 *
 * @param profileKey 配置键
 */
function selectS3Profile(profileKey: string) {
  selectedS3ProfileKey.value = profileKey
}

/**
 * 设为当前启用 S3 配置项。
 *
 * @param profileKey 配置键
 */
async function setActiveS3Profile(profileKey: string) {
  const previousState: AdminS3ConfigForm = {
    activeProfileKey: s3FormState.activeProfileKey,
    profiles: s3FormState.profiles.map(profile => ({
      ...profile,
      allowedContentTypes: [...profile.allowedContentTypes]
    }))
  }

  s3FormState.activeProfileKey = profileKey
  s3FormState.profiles = s3FormState.profiles.map(profile => ({
    ...profile,
    enabled: profile.profileKey === profileKey
  }))
  selectedS3ProfileKey.value = profileKey

  if (!validateS3Form()) {
    assignS3FormState(previousState)
    return
  }

  isActivatingS3Profile.value = true

  try {
    await saveS3Config({ silentSuccessToast: true })
    toast.add({
      title: '启用配置已更新',
      color: 'success'
    })
  } catch (error: any) {
    assignS3FormState(previousState)
    toast.add({
      title: '启用配置失败',
      description: error?.message || '启用配置未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isActivatingS3Profile.value = false
  }
}

/**
 * 删除 S3 配置项。
 *
 * @param profileKey 配置键
 */
function removeS3Profile(profileKey: string) {
  if (s3FormState.profiles.length <= 1) {
    toast.add({ title: '至少保留一个 S3 配置项', color: 'warning' })
    return
  }

  const removingActive = s3FormState.activeProfileKey === profileKey
  s3FormState.profiles = s3FormState.profiles.filter(profile => profile.profileKey !== profileKey)

  if (removingActive) {
    const nextProfile = s3FormState.profiles[0]
    void setActiveS3Profile(nextProfile.profileKey)
    return
  }

  if (selectedS3ProfileKey.value === profileKey) {
    selectedS3ProfileKey.value = s3FormState.profiles[0].profileKey
  }
}

/**
 * 新增首页关键词输入项。
 * 作用：在后台首页配置中追加一个空关键词输入框，便于录入新的首屏关键词。
 */
function addHeroKeyword() {
  if (homepageFormState.heroKeywords.length >= 6) {
    toast.add({ title: '首页关键词最多 6 个', color: 'warning' })
    return
  }

  homepageFormState.heroKeywords.push('')
}

/**
 * 删除首页关键词输入项。
 * 作用：从首页配置中移除指定位置的关键词。
 *
 * @param index 关键词索引
 */
function removeHeroKeyword(index: number) {
  homepageFormState.heroKeywords.splice(index, 1)
}

/**
 * 新增首页统计项。
 * 作用：在首页配置中追加一个空统计项输入组，便于维护首页轻量数据展示。
 */
function addHeroStat() {
  if (homepageFormState.heroStats.length >= 4) {
    toast.add({ title: '首页统计项最多 4 个', color: 'warning' })
    return
  }

  homepageFormState.heroStats.push({
    label: '',
    value: ''
  })
}

/**
 * 删除首页统计项。
 * 作用：从首页配置中移除指定位置的统计项。
 *
 * @param index 统计项索引
 */
function removeHeroStat(index: number) {
  homepageFormState.heroStats.splice(index, 1)
}

/**
 * 同步已选首屏视觉文章。
 * 作用：在加载配置或保存成功后，根据当前视觉文章 ID 拉取文章摘要，便于后台展示已选项。
 */
async function syncSelectedHeroVisualPost() {
  if (!homepageFormState.heroVisualPostId) {
    selectedHeroVisualPost.value = null
    return
  }

  try {
    selectedHeroVisualPost.value = await adminPosts.getPost(homepageFormState.heroVisualPostId)
  } catch {
    selectedHeroVisualPost.value = null
  }
}

/**
 * 搜索首屏视觉候选文章。
 * 作用：根据后台输入的关键词查询已发布文章，供首页视觉块快速选择。
 */
async function searchHeroVisualPosts() {
  const keyword = heroVisualKeyword.value.trim()

  if (!keyword) {
    heroVisualOptions.value = []
    return
  }

  isLoadingHeroVisualOptions.value = true

  try {
    const response = await adminPosts.listPosts({
      keyword,
      status: 'PUBLISHED',
      pageNo: 1,
      pageSize: 8
    })
    heroVisualOptions.value = response.list
  } catch {
    heroVisualOptions.value = []
  } finally {
    isLoadingHeroVisualOptions.value = false
  }
}

/**
 * 选择首屏视觉文章。
 * 作用：将候选文章写入首页配置表单，并同步已选文章展示状态。
 *
 * @param post 文章条目
 */
function selectHeroVisualPost(post: AdminPostItem) {
  homepageFormState.heroVisualPostId = post.id
  selectedHeroVisualPost.value = post
  heroVisualKeyword.value = ''
  heroVisualOptions.value = []
}

/**
 * 清空首屏视觉文章。
 * 作用：移除当前首页右侧视觉文章配置，并恢复为前台默认兜底素材。
 */
function clearHeroVisualPost() {
  homepageFormState.heroVisualPostId = null
  selectedHeroVisualPost.value = null
  heroVisualKeyword.value = ''
  heroVisualOptions.value = []
}

watch(heroVisualKeyword, () => {
  if (heroVisualSearchTimer) {
    clearTimeout(heroVisualSearchTimer)
  }

  heroVisualSearchTimer = setTimeout(() => {
    void searchHeroVisualPosts()
  }, 240)
})

onBeforeUnmount(() => {
  if (heroVisualSearchTimer) {
    clearTimeout(heroVisualSearchTimer)
  }
})

/**
 * 创建首页统计项草稿。
 * 作用：为首页统计项输入区提供稳定的新对象结构。
 *
 * @param stat 原始统计项
 * @returns 规范化后的统计项
 */
function normalizeHeroStat(stat: AdminHomepageHeroStatForm) {
  return {
    label: stat.label.trim(),
    value: stat.value.trim()
  }
}

/**
 * 保存站点配置。
 * 作用：将基础信息、SEO 和视觉风格配置提交到后台接口，并在成功后刷新快照。
 */
async function saveSiteConfig() {
  if (!validateSiteForm()) {
    return
  }

  const response = await adminSiteConfig.updateSiteConfig({
    siteName: siteFormState.siteName.trim(),
    siteSubTitle: siteFormState.siteSubTitle.trim(),
    footerText: siteFormState.footerText.trim(),
    logoUrl: siteFormState.logoUrl.trim(),
    faviconUrl: siteFormState.faviconUrl.trim(),
    defaultTitle: siteFormState.defaultTitle.trim(),
    defaultDescription: siteFormState.defaultDescription.trim(),
    primaryColor: siteFormState.primaryColor.trim(),
    secondaryColor: siteFormState.secondaryColor.trim(),
    wechatAccessCodeEnabled: siteFormState.wechatAccessCodeEnabled,
    wechatAccessCode: siteFormState.wechatAccessCode.trim(),
    wechatAccessCodeHint: siteFormState.wechatAccessCodeHint.trim(),
    wechatQrCodeUrl: siteFormState.wechatQrCodeUrl.trim()
  })

  assignSiteFormState(response)
  lastSavedSiteSnapshot.value = serializeSiteFormState(response)
  toast.add({
    title: '站点配置已保存',
    color: 'success'
  })
}

/**
 * 保存首页配置。
 * 作用：将首页首屏、模块开关和文案配置提交到后台接口，并在成功后刷新快照。
 */
async function saveHomepageConfig() {
  if (!validateHomepageForm()) {
    return
  }

  const response = await adminHomepageConfig.updateHomepageConfig({
    heroEnabled: homepageFormState.heroEnabled,
    heroLayout: 'brand',
    heroBackgroundMode: homepageFormState.heroBackgroundMode,
    heroEyebrow: homepageFormState.heroEyebrow.trim(),
    heroTitle: homepageFormState.heroTitle.trim(),
    heroSubtitle: homepageFormState.heroSubtitle.trim(),
    heroPrimaryButtonText: homepageFormState.heroPrimaryButtonText.trim(),
    heroPrimaryButtonLink: homepageFormState.heroPrimaryButtonLink.trim(),
    heroSecondaryButtonText: homepageFormState.heroSecondaryButtonText.trim(),
    heroSecondaryButtonLink: homepageFormState.heroSecondaryButtonLink.trim(),
    heroVisualPostId: homepageFormState.heroVisualPostId,
    heroVisualClickable: homepageFormState.heroVisualClickable,
    heroKeywords: homepageFormState.heroKeywords.map(keyword => keyword.trim()).filter(Boolean),
    showHeroKeywords: homepageFormState.showHeroKeywords,
    showHeroStats: homepageFormState.showHeroStats,
    heroStats: homepageFormState.heroStats
      .map(normalizeHeroStat)
      .filter(stat => stat.label && stat.value),
    showFeaturedSection: homepageFormState.showFeaturedSection,
    featuredSectionTitle: homepageFormState.featuredSectionTitle.trim(),
    showLatestSection: homepageFormState.showLatestSection,
    latestSectionTitle: homepageFormState.latestSectionTitle.trim(),
    showCategorySection: homepageFormState.showCategorySection,
    categorySectionTitle: homepageFormState.categorySectionTitle.trim(),
    showTopicSection: homepageFormState.showTopicSection,
    topicSectionTitle: homepageFormState.topicSectionTitle.trim()
  })

  assignHomepageFormState(response)
  await syncSelectedHeroVisualPost()
  lastSavedHomepageSnapshot.value = serializeHomepageFormState(response)
  toast.add({
    title: '首页展示已保存',
    color: 'success'
  })
}

/**
 * 构建 S3 配置保存请求体。
 * 作用：统一整理并裁剪表单字段，避免普通保存和启用保存出现字段不一致。
 */
function buildS3ConfigPayload(): AdminS3ConfigForm {
  return {
    activeProfileKey: s3FormState.activeProfileKey.trim(),
    profiles: s3FormState.profiles.map(profile => ({
      profileKey: profile.profileKey.trim(),
      name: profile.name.trim(),
      enabled: profile.enabled,
      endpoint: profile.endpoint.trim(),
      region: profile.region.trim() || 'auto',
      bucket: profile.bucket.trim(),
      accessKey: profile.accessKey.trim(),
      secretKey: profile.secretKey.trim(),
      pathStyleAccess: profile.pathStyleAccess,
      publicBaseUrl: profile.publicBaseUrl.trim(),
      presignExpireSeconds: profile.presignExpireSeconds,
      maxFileSizeMb: profile.maxFileSizeMb,
      allowedContentTypes: profile.allowedContentTypes.map(item => item.trim()).filter(Boolean)
    }))
  }
}

/**
 * 保存 S3 配置。
 * 作用：将后台 S3 多配置提交到后端，并在成功后刷新本地快照。
 */
async function saveS3Config(options?: { silentSuccessToast?: boolean }) {
  if (!validateS3Form()) {
    return
  }

  isSavingS3Config.value = true

  try {
    const response = await adminSiteConfig.updateS3Config(buildS3ConfigPayload())

    assignS3FormState(response)
    lastSavedS3Snapshot.value = serializeS3FormState(response)

    if (!options?.silentSuccessToast) {
      toast.add({
        title: 'S3 配置已保存',
        color: 'success'
      })
    }
  } finally {
    isSavingS3Config.value = false
  }
}

/**
 * 测试当前 S3 配置连接。
 * 作用：实时校验当前编辑配置的 endpoint、bucket 和密钥是否可用。
 */
async function testCurrentS3Connection() {
  if (!currentS3Profile.value) {
    toast.add({ title: '当前没有可测试的 S3 配置', color: 'warning' })
    return
  }

  isTestingS3Connection.value = true

  try {
    const response = await adminSiteConfig.testS3Connection({
      ...currentS3Profile.value,
      profileKey: currentS3Profile.value.profileKey.trim(),
      name: currentS3Profile.value.name.trim(),
      endpoint: currentS3Profile.value.endpoint.trim(),
      region: currentS3Profile.value.region.trim() || 'auto',
      bucket: currentS3Profile.value.bucket.trim(),
      accessKey: currentS3Profile.value.accessKey.trim(),
      secretKey: currentS3Profile.value.secretKey.trim(),
      pathStyleAccess: !!currentS3Profile.value.pathStyleAccess,
      publicBaseUrl: currentS3Profile.value.publicBaseUrl.trim(),
      allowedContentTypes: currentS3Profile.value.allowedContentTypes.map(item => item.trim()).filter(Boolean)
    })

    toast.add({
      title: response.success ? '连接测试成功' : '连接测试失败',
      description: response.message,
      color: response.success ? 'success' : 'error'
    })
  } catch (error: any) {
    toast.add({
      title: '连接测试失败',
      description: error?.message || '连接测试请求失败，请稍后重试。',
      color: 'error'
    })
  } finally {
    isTestingS3Connection.value = false
  }
}

/**
 * 保存当前标签配置。
 * 作用：根据当前所在标签分组调用对应接口，避免不同配置模型互相混写。
 */
async function handleSubmit() {
  isSubmitting.value = true

  try {
    if (isHomepageTab.value) {
      await saveHomepageConfig()
      return
    }

    if (isS3Tab.value) {
      await saveS3Config()
      return
    }

    await saveSiteConfig()
  } catch (error: any) {
    const title = isHomepageTab.value
      ? '保存首页展示失败'
      : (isS3Tab.value
          ? '保存 S3 配置失败'
          : '保存站点配置失败')
    toast.add({
      title,
      description: error?.message || '保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  await loadAllConfig()
})
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">站点设置</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
            {{ currentTabHasUnsavedChanges ? '当前分组待保存' : (hasUnsavedChanges ? '存在其他分组未保存修改' : '已保存') }}
          </p>
        </div>

        <div class="flex w-full flex-wrap items-center justify-end gap-2 sm:w-auto sm:flex-nowrap">
          <AdminButton
            :loading="isLoading"
            icon="i-lucide-refresh-cw"
            label="刷新"
            tone="neutral"
            variant="outline"
            size="sm"
            @click="loadAllConfig"
          />
          <AdminPrimaryButton
            :loading="isSubmitting"
            icon="i-lucide-save"
            :label="submitButtonLabel"
            :loading-label="submitLoadingLabel"
            @click="handleSubmit"
          />
        </div>
      </div>
    </section>

    <div class="space-y-4">
      <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-2.5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
        <div class="overflow-x-auto [scrollbar-width:thin]">
          <div class="flex min-w-max items-center gap-2">
            <button
              v-for="item in tabItems"
              :key="item.key"
              type="button"
              :class="[
                'flex min-w-[168px] items-center gap-2.5 rounded-[12px] border px-3.5 py-2.5 text-left transition duration-200',
                activeTab === item.key
                  ? 'border-sky-100/90 bg-[linear-gradient(135deg,rgba(248,252,255,0.92),rgba(255,250,245,0.58))] text-slate-900 dark:border-sky-400/20 dark:bg-[linear-gradient(135deg,rgba(56,189,248,0.10),rgba(251,146,60,0.05))] dark:text-slate-50'
                  : 'border-transparent text-slate-500 hover:border-white/70 hover:bg-white/68 hover:text-slate-900 dark:text-slate-400 dark:hover:border-white/10 dark:hover:bg-white/5 dark:hover:text-slate-50'
              ]"
              @click="switchTab(item.key)"
            >
              <div class="flex size-8 shrink-0 items-center justify-center rounded-[9px] bg-slate-100/90 text-slate-700 dark:bg-white/5 dark:text-slate-200">
                <UIcon :name="item.icon" class="size-4" />
              </div>
              <span class="whitespace-nowrap text-[13px] font-semibold">{{ item.label }}</span>
            </button>
          </div>
        </div>
      </section>

      <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)] lg:p-5">
        <div v-if="activeTab === 'basic'" class="space-y-4">
          <div class="grid gap-4 md:grid-cols-2">
            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点名称</p>
              <AdminInput v-model="siteFormState.siteName" placeholder="站点名称" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">页脚文案</p>
              <AdminInput v-model="siteFormState.footerText" placeholder="页脚文案" />
            </div>

            <div class="space-y-2 md:col-span-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">站点副标题</p>
              <AdminTextarea v-model="siteFormState.siteSubTitle" :rows="4" placeholder="站点副标题" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Logo 地址</p>
              <AdminInput v-model="siteFormState.logoUrl" placeholder="Logo 地址" />
            </div>

            <div class="space-y-2">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Favicon 地址</p>
              <AdminInput v-model="siteFormState.faviconUrl" placeholder="Favicon 地址" />
            </div>
          </div>

          <div class="rounded-[16px] border border-slate-200/80 bg-white/72 p-4 dark:border-white/10 dark:bg-white/4">
            <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">公众号验证码</h2>
                <p class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400">
                  用于后续文章访问控制和尾部隐藏内容中的“公众号验证码”规则。
                </p>
              </div>

              <AdminSwitchField
                v-model="siteFormState.wechatAccessCodeEnabled"
                label="启用"
                description="开启后，前台可展示公众号验证码相关引导。"
                color="primary"
                active-text="已启用"
                inactive-text="未启用"
              />
            </div>

            <div class="mt-4 grid gap-4 md:grid-cols-2">
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">公众号验证码</p>
                <AdminInput v-model="siteFormState.wechatAccessCode" placeholder="请输入站点级公众号验证码" />
              </div>

              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">公众号二维码地址</p>
                <AdminInput v-model="siteFormState.wechatQrCodeUrl" placeholder="可选，前台弹窗可直接展示二维码" />
              </div>

              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">验证码提示文案</p>
                <AdminTextarea
                  v-model="siteFormState.wechatAccessCodeHint"
                  :rows="3"
                  placeholder="例如：扫码关注公众号后，输入收到的访问验证码"
                />
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeTab === 'seo'" class="grid gap-4">
          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认标题</p>
            <AdminInput v-model="siteFormState.defaultTitle" placeholder="默认标题" />
          </div>

          <div class="space-y-2">
            <p class="text-sm font-medium text-slate-700 dark:text-slate-300">默认描述</p>
            <AdminTextarea v-model="siteFormState.defaultDescription" :rows="5" placeholder="默认描述" />
          </div>

        </div>

        <div v-else-if="activeTab === 'theme'" class="space-y-4">
          <div class="flex flex-col gap-3 rounded-[16px] border border-slate-200/80 bg-white/72 p-4 dark:border-white/10 dark:bg-white/4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">品牌配色</h2>
              <p class="mt-1 text-xs leading-5 text-slate-500 dark:text-slate-400">
                主色与辅助色会同步影响前台全局主题色、导航品牌渐变和站点基础氛围。未配置时仍会自动回退到当前默认配色。
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-3">
              <div class="flex items-center gap-2 rounded-full border border-white/80 bg-white/92 px-3 py-2 shadow-[0_12px_24px_-22px_rgba(15,23,42,0.24)] dark:border-white/10 dark:bg-white/6">
                <span
                  class="h-6 w-6 rounded-full border border-white/90 shadow-sm dark:border-white/10"
                  :style="{ backgroundColor: siteFormState.primaryColor }"
                />
                <span
                  class="-ml-2 h-6 w-6 rounded-full border border-white/90 shadow-sm dark:border-white/10"
                  :style="{ backgroundColor: siteFormState.secondaryColor }"
                />
                <span class="ml-1 text-xs font-medium text-slate-500 dark:text-slate-400">当前配色</span>
              </div>

              <AdminButton
                icon="i-lucide-rotate-ccw"
                label="恢复默认"
                tone="neutral"
                variant="outline"
                @click="resetThemeColors"
              />
            </div>
          </div>

          <div class="grid gap-4 lg:grid-cols-2">
            <AdminColorPicker
              v-model="siteFormState.primaryColor"
              label="主色"
              description="用于前台品牌强调、主操作色和整体主题倾向。"
              :presets="[...primaryColorPresets]"
              :placeholder="DEFAULT_PRIMARY_COLOR"
            />

            <AdminColorPicker
              v-model="siteFormState.secondaryColor"
              label="辅助色"
              description="用于品牌渐变、辅助强调和部分氛围色过渡。"
              :presets="[...secondaryColorPresets]"
              :placeholder="DEFAULT_SECONDARY_COLOR"
            />
          </div>
        </div>

        <div v-else-if="activeTab === 'homepage'" class="grid gap-5 xl:grid-cols-[minmax(0,1.1fr)_360px]">
          <div class="space-y-5">
            <section class="rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">首屏基础</h2>
                  <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">控制无封面首屏是否展示、背景模式和主文案。</p>
                </div>

                <AdminToggleButton
                  v-model="homepageFormState.heroEnabled"
                  tone="success"
                  active-label="首屏已启用"
                  inactive-label="首屏已关闭"
                  show-dot
                />
              </div>

              <div class="mt-4 grid gap-4 md:grid-cols-2">
                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">背景模式</p>
                  <AdminSelect
                    v-model="homepageFormState.heroBackgroundMode"
                    :items="homepageBackgroundOptions"
                    placeholder="选择背景模式"
                  />
                </div>

                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">品牌眉题</p>
                  <AdminInput v-model="homepageFormState.heroEyebrow" placeholder="例如：Yunyu / 云屿" />
                </div>

                <div class="space-y-2 md:col-span-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首页主标题</p>
                  <AdminTextarea
                    v-model="homepageFormState.heroTitle"
                    :rows="3"
                    placeholder="请输入首页主标题"
                  />
                </div>

                <div class="space-y-2 md:col-span-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首页副标题</p>
                  <AdminTextarea
                    v-model="homepageFormState.heroSubtitle"
                    :rows="4"
                    placeholder="请输入首页副标题"
                  />
                </div>
              </div>
            </section>

            <section class="rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
              <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">首屏按钮</h2>
              <div class="mt-4 grid gap-4 md:grid-cols-2">
                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">主按钮文案</p>
                  <AdminInput v-model="homepageFormState.heroPrimaryButtonText" placeholder="查看文章" />
                </div>

                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">主按钮跳转</p>
                  <AdminInput v-model="homepageFormState.heroPrimaryButtonLink" placeholder="/posts" />
                </div>

                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">次按钮文案</p>
                  <AdminInput v-model="homepageFormState.heroSecondaryButtonText" placeholder="进入专题" />
                </div>

                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">次按钮跳转</p>
                  <AdminInput v-model="homepageFormState.heroSecondaryButtonLink" placeholder="/topics" />
                </div>
              </div>
            </section>

            <section class="rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">首屏视觉块</h2>
                  <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
                    配置一篇文章作为右侧主视觉。有视频优先展示视频，没有视频则回退封面图。
                  </p>
                </div>

                <AdminToggleButton
                  v-model="homepageFormState.heroVisualClickable"
                  tone="info"
                  active-label="整块可点击"
                  inactive-label="仅展示不跳转"
                />
              </div>

              <div class="mt-4 grid gap-4 md:grid-cols-2">
                <div class="space-y-2">
                  <p class="text-sm font-medium text-slate-700 dark:text-slate-300">搜索并选择文章</p>
                  <AdminInput
                    v-model="heroVisualKeyword"
                    icon="i-lucide-search"
                    placeholder="输入标题关键词搜索已发布文章"
                  />

                  <div
                    v-if="heroVisualKeyword.trim() || heroVisualOptions.length > 0 || isLoadingHeroVisualOptions"
                    class="overflow-hidden rounded-[14px] border border-slate-200/80 bg-white/92 shadow-[0_16px_34px_-24px_rgba(15,23,42,0.18)] dark:border-white/10 dark:bg-slate-950/80"
                  >
                    <div v-if="isLoadingHeroVisualOptions" class="px-4 py-3 text-sm text-slate-500 dark:text-slate-400">
                      正在搜索文章...
                    </div>

                    <div
                      v-else-if="heroVisualOptions.length === 0"
                      class="px-4 py-3 text-sm text-slate-500 dark:text-slate-400"
                    >
                      没有找到匹配文章
                    </div>

                    <button
                      v-for="post in heroVisualOptions"
                      v-else
                      :key="`hero-visual-option-${post.id}`"
                      type="button"
                      class="flex w-full cursor-pointer items-start justify-between gap-3 border-b border-slate-200/70 px-4 py-3 text-left transition last:border-b-0 hover:bg-slate-50 dark:border-white/10 dark:hover:bg-white/[0.04]"
                      @click="selectHeroVisualPost(post)"
                    >
                      <div class="min-w-0">
                        <p class="truncate text-sm font-medium text-slate-800 dark:text-slate-100">{{ post.title }}</p>
                        <p class="mt-1 text-xs text-slate-400 dark:text-slate-500">
                          #{{ post.id }} · {{ post.slug }}
                        </p>
                      </div>
                      <span class="shrink-0 text-[11px] text-slate-400 dark:text-slate-500">
                        {{ post.publishedAt || '未发布' }}
                      </span>
                    </button>
                  </div>
                </div>

                <div class="rounded-[14px] border border-dashed border-slate-200/80 bg-white/60 px-4 py-3 text-sm text-slate-500 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-400">
                  <p>展示顺序：视频优先，其次封面图，最后使用前台默认兜底视觉。</p>
                  <p class="mt-1">点击逻辑：开启后，右侧整块视觉会跳转到对应文章详情页。</p>
                </div>
              </div>

              <div v-if="selectedHeroVisualPost" class="mt-4 rounded-[14px] border border-slate-200/75 bg-white/70 px-4 py-3 dark:border-white/10 dark:bg-white/[0.04]">
                <div class="flex flex-wrap items-start justify-between gap-3">
                  <div class="min-w-0">
                    <p class="text-xs uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">已选文章</p>
                    <p class="mt-2 truncate text-sm font-medium text-slate-900 dark:text-slate-100">
                      {{ selectedHeroVisualPost.title }}
                    </p>
                    <p class="mt-1 text-xs text-slate-400 dark:text-slate-500">
                      ID {{ selectedHeroVisualPost.id }} · {{ selectedHeroVisualPost.slug }}
                    </p>
                    <div class="mt-3 flex flex-wrap gap-2">
                      <span
                        :class="[
                          'rounded-full border px-2.5 py-1 text-[11px] font-medium',
                          selectedHeroVisualPost.videoReady
                            ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200'
                            : 'border-slate-200 bg-white text-slate-500 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-400'
                        ]"
                      >
                        {{ selectedHeroVisualPost.videoReady ? '有视频' : '无视频' }}
                      </span>
                      <span
                        :class="[
                          'rounded-full border px-2.5 py-1 text-[11px] font-medium',
                          selectedHeroVisualPost.coverReady
                            ? 'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-400/20 dark:bg-sky-400/10 dark:text-sky-200'
                            : 'border-slate-200 bg-white text-slate-500 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-400'
                        ]"
                      >
                        {{ selectedHeroVisualPost.coverReady ? '有封面' : '无封面' }}
                      </span>
                    </div>
                  </div>

                  <AdminButton
                    icon="i-lucide-x"
                    label="清空"
                    tone="neutral"
                    variant="outline"
                    size="xs"
                    @click="clearHeroVisualPost"
                  />
                </div>
              </div>
            </section>

            <section class="rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">关键词与统计</h2>
                  <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">这两块建议保持轻量，只做首屏辅助信息。</p>
                </div>
                <div class="flex flex-wrap items-center gap-2">
                  <AdminToggleButton
                    v-model="homepageFormState.showHeroKeywords"
                    tone="info"
                    active-label="关键词开启"
                    inactive-label="关键词关闭"
                  />
                  <AdminToggleButton
                    v-model="homepageFormState.showHeroStats"
                    tone="warning"
                    active-label="统计开启"
                    inactive-label="统计关闭"
                  />
                </div>
              </div>

              <div class="mt-4 space-y-5">
                <div class="space-y-3">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首屏关键词</p>
                    <AdminButton
                      icon="i-lucide-plus"
                      label="新增关键词"
                      tone="neutral"
                      variant="outline"
                      size="xs"
                      @click="addHeroKeyword"
                    />
                  </div>

                  <div class="grid gap-3 sm:grid-cols-2">
                    <div
                      v-for="(keyword, index) in homepageFormState.heroKeywords"
                      :key="`keyword-${index}`"
                      class="flex items-center gap-2"
                    >
                      <AdminInput
                        v-model="homepageFormState.heroKeywords[index]"
                        :placeholder="`关键词 ${index + 1}`"
                      />
                      <AdminButton
                        icon="i-lucide-trash-2"
                        tone="neutral"
                        variant="outline"
                        size="xs"
                        square
                        @click="removeHeroKeyword(index)"
                      />
                    </div>
                  </div>
                </div>

                <div class="space-y-3">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-700 dark:text-slate-300">首屏统计项</p>
                    <AdminButton
                      icon="i-lucide-plus"
                      label="新增统计项"
                      tone="neutral"
                      variant="outline"
                      size="xs"
                      @click="addHeroStat"
                    />
                  </div>

                  <div class="space-y-3">
                    <div
                      v-for="(stat, index) in homepageFormState.heroStats"
                      :key="`stat-${index}`"
                      class="grid gap-3 rounded-[14px] border border-slate-200/75 bg-white/70 p-3 sm:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto] dark:border-white/10 dark:bg-white/4"
                    >
                      <AdminInput v-model="stat.label" :placeholder="`统计标题 ${index + 1}`" />
                      <AdminInput v-model="stat.value" :placeholder="`统计值 ${index + 1}`" />
                      <AdminButton
                        icon="i-lucide-trash-2"
                        tone="neutral"
                        variant="outline"
                        size="xs"
                        square
                        @click="removeHeroStat(index)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <section class="rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
              <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">首页模块开关</h2>
              <div class="mt-4 grid gap-3 md:grid-cols-2">
                <div class="rounded-[14px] border border-slate-200/75 bg-white/70 p-3 dark:border-white/10 dark:bg-white/4">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-800 dark:text-slate-100">推荐内容区</p>
                    <AdminToggleButton
                      v-model="homepageFormState.showFeaturedSection"
                      tone="success"
                      size="sm"
                      active-label="显示"
                      inactive-label="隐藏"
                    />
                  </div>
                  <div class="mt-3">
                    <AdminInput v-model="homepageFormState.featuredSectionTitle" placeholder="推荐标题" />
                  </div>
                </div>

                <div class="rounded-[14px] border border-slate-200/75 bg-white/70 p-3 dark:border-white/10 dark:bg-white/4">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-800 dark:text-slate-100">最新文章区</p>
                    <AdminToggleButton
                      v-model="homepageFormState.showLatestSection"
                      tone="success"
                      size="sm"
                      active-label="显示"
                      inactive-label="隐藏"
                    />
                  </div>
                  <div class="mt-3">
                    <AdminInput v-model="homepageFormState.latestSectionTitle" placeholder="最新文章标题" />
                  </div>
                </div>

                <div class="rounded-[14px] border border-slate-200/75 bg-white/70 p-3 dark:border-white/10 dark:bg-white/4">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-800 dark:text-slate-100">分类区</p>
                    <AdminToggleButton
                      v-model="homepageFormState.showCategorySection"
                      tone="success"
                      size="sm"
                      active-label="显示"
                      inactive-label="隐藏"
                    />
                  </div>
                  <div class="mt-3">
                    <AdminInput v-model="homepageFormState.categorySectionTitle" placeholder="分类标题" />
                  </div>
                </div>

                <div class="rounded-[14px] border border-slate-200/75 bg-white/70 p-3 dark:border-white/10 dark:bg-white/4">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-medium text-slate-800 dark:text-slate-100">专题区</p>
                    <AdminToggleButton
                      v-model="homepageFormState.showTopicSection"
                      tone="success"
                      size="sm"
                      active-label="显示"
                      inactive-label="隐藏"
                    />
                  </div>
                  <div class="mt-3">
                    <AdminInput v-model="homepageFormState.topicSectionTitle" placeholder="专题标题" />
                  </div>
                </div>
              </div>
            </section>
          </div>

          <aside class="xl:sticky xl:top-6 xl:self-start">
            <section class="overflow-hidden rounded-[18px] border border-slate-200/80 bg-white/80 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-slate-950/55">
              <div class="border-b border-slate-200/80 px-4 py-3 dark:border-white/10">
                <h2 class="text-sm font-semibold text-slate-900 dark:text-slate-50">首屏预览</h2>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">这里展示首页首屏的大致排版气质。</p>
              </div>

              <div
                class="relative isolate min-h-[520px] overflow-hidden px-4 py-5"
                :class="homepagePreviewBackgroundClass"
              >
                <div class="pointer-events-none absolute inset-0" :class="homepagePreviewAccentClass" />

                <div class="relative">
                  <div class="rounded-full border border-slate-200/80 bg-white/72 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.28em] text-slate-500 backdrop-blur-sm dark:border-white/10 dark:bg-white/6 dark:text-slate-400">
                    {{ homepageFormState.heroEyebrow || 'Yunyu / 云屿' }}
                  </div>

                  <h3 class="mt-5 max-w-[11ch] text-[2rem] font-semibold leading-[0.95] tracking-[-0.065em] text-slate-950 [font-family:var(--font-display)] dark:text-white">
                    {{ homepageFormState.heroTitle || '首页主标题' }}
                  </h3>

                  <p class="mt-4 max-w-[17rem] text-sm leading-7 text-slate-600 dark:text-slate-300">
                    {{ homepageFormState.heroSubtitle || '首页副标题' }}
                  </p>

                  <div class="mt-6 flex flex-wrap gap-2">
                    <span class="rounded-full bg-slate-950 px-4 py-2 text-xs font-medium text-white dark:bg-white dark:text-slate-950">
                      {{ homepageFormState.heroPrimaryButtonText || '主按钮' }}
                    </span>
                    <span
                      v-if="homepageFormState.heroSecondaryButtonText.trim()"
                      class="rounded-full border border-slate-300/85 bg-white/72 px-4 py-2 text-xs font-medium text-slate-700 backdrop-blur-sm dark:border-white/14 dark:bg-white/6 dark:text-slate-100"
                    >
                      {{ homepageFormState.heroSecondaryButtonText }}
                    </span>
                  </div>

                  <div class="mt-6 flex flex-wrap items-center gap-2 text-[11px] text-slate-500 dark:text-slate-400">
                    <span class="rounded-full border border-slate-200/80 bg-white/70 px-3 py-1 backdrop-blur-sm dark:border-white/10 dark:bg-white/[0.04]">
                      {{ homepageFormState.heroVisualPostId ? `视觉文章 #${homepageFormState.heroVisualPostId}` : '未配置视觉文章' }}
                    </span>
                    <span class="rounded-full border border-slate-200/80 bg-white/70 px-3 py-1 backdrop-blur-sm dark:border-white/10 dark:bg-white/[0.04]">
                      {{ homepageFormState.heroVisualClickable ? '整块可点击' : '仅展示' }}
                    </span>
                  </div>

                  <div
                    v-if="homepageFormState.showHeroKeywords && homepageFormState.heroKeywords.length > 0"
                    class="mt-6 flex flex-wrap gap-2"
                  >
                    <span
                      v-for="keyword in homepageFormState.heroKeywords.filter(Boolean).slice(0, 6)"
                      :key="keyword"
                      class="rounded-full border border-slate-200/85 bg-white/62 px-3 py-1.5 text-[11px] font-medium text-slate-600 backdrop-blur-sm dark:border-white/10 dark:bg-white/6 dark:text-slate-300"
                    >
                      #{{ keyword }}
                    </span>
                  </div>

                  <div
                    v-if="homepageFormState.showHeroStats && homepageFormState.heroStats.length > 0"
                    class="mt-8 grid grid-cols-2 gap-3 border-t border-slate-200/80 pt-4 dark:border-white/10"
                  >
                    <div
                      v-for="(stat, index) in homepageFormState.heroStats.filter(item => item.label || item.value).slice(0, 4)"
                      :key="`preview-stat-${index}`"
                    >
                      <p class="text-[10px] uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">{{ stat.label || '标题' }}</p>
                      <p class="mt-1 text-base font-semibold tracking-[-0.04em] text-slate-950 dark:text-white">{{ stat.value || '0' }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </section>
          </aside>
        </div>

        <div v-else-if="activeTab === 's3'" class="grid gap-4 lg:grid-cols-[240px_minmax(0,1fr)]">
          <aside class="space-y-3 rounded-[16px] border border-slate-200/80 bg-white/75 p-3 dark:border-white/10 dark:bg-white/4">
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">S3 配置列表</p>
              <AdminButton
                icon="i-lucide-plus"
                label="新增"
                tone="neutral"
                variant="outline"
                size="xs"
                @click="addS3Profile"
              />
            </div>

            <div class="space-y-2">
              <button
                v-for="profile in s3FormState.profiles"
                :key="profile.profileKey"
                type="button"
                class="w-full rounded-[12px] border px-3 py-2 text-left transition"
                :class="selectedS3ProfileKey === profile.profileKey
                  ? 'border-sky-200 bg-sky-50/90 text-sky-700 dark:border-sky-400/30 dark:bg-sky-400/10 dark:text-sky-200'
                  : 'border-slate-200 bg-white/80 text-slate-600 hover:border-slate-300 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-300'"
                @click="selectS3Profile(profile.profileKey)"
              >
                <div class="flex items-center justify-between gap-2">
                  <p class="truncate text-sm font-medium">{{ profile.name || profile.profileKey }}</p>
                  <span
                    class="rounded-full px-2 py-0.5 text-[10px] font-semibold"
                    :class="profile.enabled
                      ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-400/20 dark:text-emerald-200'
                      : 'bg-slate-100 text-slate-500 dark:bg-white/10 dark:text-slate-400'"
                  >
                    {{ profile.enabled ? '启用中' : '未启用' }}
                  </span>
                </div>
                <p class="mt-1 truncate text-xs text-slate-400 dark:text-slate-500">{{ profile.profileKey }}</p>
              </button>
            </div>
          </aside>

          <section v-if="currentS3Profile" class="space-y-4 rounded-[16px] border border-slate-200/80 bg-white/75 p-4 dark:border-white/10 dark:bg-white/4">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">当前配置详情</p>
              <div class="flex items-center gap-2">
                <AdminButton
                  icon="i-lucide-plug-zap"
                  label="连接测试"
                  tone="neutral"
                  variant="outline"
                  size="xs"
                  :loading="isTestingS3Connection"
                  @click="testCurrentS3Connection"
                />
                <AdminButton
                  icon="i-lucide-check"
                  label="设为启用"
                  tone="neutral"
                  variant="outline"
                  size="xs"
                  :loading="isActivatingS3Profile"
                  @click="setActiveS3Profile(currentS3Profile.profileKey)"
                />
                <AdminPrimaryButton
                  icon="i-lucide-save"
                  label="保存配置"
                  loading-label="保存中..."
                  size="xs"
                  :loading="isSavingS3Config"
                  @click="saveS3Config"
                />
                <AdminButton
                  icon="i-lucide-trash-2"
                  label="删除"
                  tone="neutral"
                  variant="outline"
                  size="xs"
                  @click="removeS3Profile(currentS3Profile.profileKey)"
                />
              </div>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">配置键</p>
                <AdminInput
                  :model-value="currentS3Profile.profileKey"
                  placeholder="例如：r2-prod"
                  readonly
                />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">配置名称</p>
                <AdminInput v-model="currentS3Profile.name" placeholder="例如：R2 生产" />
              </div>
              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Endpoint</p>
                <AdminInput v-model="currentS3Profile.endpoint" placeholder="https://s3.example.com" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Region</p>
                <AdminInput v-model="currentS3Profile.region" placeholder="auto" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Bucket</p>
                <AdminInput v-model="currentS3Profile.bucket" placeholder="yunyu-assets" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">AccessKey</p>
                <AdminInput v-model="currentS3Profile.accessKey" placeholder="请输入 AccessKey" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">SecretKey</p>
                <AdminInput v-model="currentS3Profile.secretKey" placeholder="请输入 SecretKey" />
              </div>
              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">访问风格</p>
                <div class="rounded-[12px] border border-slate-200/80 bg-white/80 p-3 dark:border-white/10 dark:bg-white/[0.03]">
                  <AdminToggleButton
                    v-model="currentS3Profile.pathStyleAccess"
                    tone="info"
                    active-label="Path-Style（endpoint/bucket/key）"
                    inactive-label="Virtual-hosted-style（bucket.endpoint/key）"
                  />
                  <p class="mt-2 text-xs text-slate-500 dark:text-slate-400">
                    MinIO、部分私有 S3 和本地域名环境通常需要启用 Path-Style。
                  </p>
                </div>
              </div>
              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">Public Base URL</p>
                <AdminInput v-model="currentS3Profile.publicBaseUrl" placeholder="https://cdn.example.com" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">签名有效期（秒）</p>
                <AdminInput v-model="currentS3Profile.presignExpireSeconds" type="number" placeholder="300" />
              </div>
              <div class="space-y-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">最大文件大小（MB）</p>
                <AdminInput v-model="currentS3Profile.maxFileSizeMb" type="number" placeholder="20" />
              </div>
              <div class="space-y-2 md:col-span-2">
                <p class="text-sm font-medium text-slate-700 dark:text-slate-300">允许 MIME 类型（逗号分隔）</p>
                <AdminInput
                  :model-value="currentS3Profile.allowedContentTypes.join(',')"
                  placeholder="image/jpeg,image/png,image/webp,video/mp4"
                  @update:model-value="value => { currentS3Profile.allowedContentTypes = value.split(',').map(item => item.trim()).filter(Boolean) }"
                />
              </div>
            </div>
          </section>
        </div>
      </section>
    </div>
  </div>
</template>
