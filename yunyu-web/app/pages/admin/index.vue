<script setup lang="ts">
import type { AdminCommentItem } from '../../types/comment'
import type { AdminPostItem } from '../../types/post'

/**
 * 后台首页概览卡片类型。
 * 作用：统一描述工作台顶部统计卡片的标题、数值、说明和颜色信息。
 */
interface AdminDashboardOverviewCard {
  title: string
  value: string
  hint: string
  color: 'primary' | 'warning' | 'success'
}

/**
 * 后台首页待处理卡片类型。
 * 作用：统一描述工作台待办区的标题、说明、跳转地址与图标信息。
 */
interface AdminDashboardPendingItem {
  title: string
  value: string
  description: string
  to: string
  icon: string
}

/**
 * 后台首页聚合数据类型。
 * 作用：集中承接工作台页面所需的真实统计、最新评论与最近文章数据。
 */
interface AdminDashboardPayload {
  publishedPostTotal: number
  draftPostTotal: number
  topicTotal: number
  pendingCommentTotal: number
  recentPosts: AdminPostItem[]
  recentDraftPosts: AdminPostItem[]
  recentPendingComments: AdminCommentItem[]
}

/**
 * 后台首页。
 * 作用：作为后台工作台首页，承载概览数据、快捷入口和待处理事项。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const ACCESS_TOKEN_STORAGE_KEY = 'yunyu_access_token'

const toast = useToast()
const auth = useAuth()
const adminPosts = useAdminPosts()
const adminComments = useAdminComments()
const adminTaxonomy = useAdminTaxonomy()
const runtimeConfig = useRuntimeConfig()
const showAccessToken = ref(false)
const persistedAccessToken = ref('')
const apiBase = computed(() => runtimeConfig.public.apiBase || '')

const quickActions = [
  {
    title: '文章管理',
    description: '查看全部文章与筛选状态',
    to: '/admin/posts',
    icon: 'i-lucide-files'
  },
  {
    title: '评论管理',
    description: '处理评论审核与读者反馈',
    to: '/admin/comments',
    icon: 'i-lucide-messages-square'
  },
  {
    title: '站点设置',
    description: '维护站点信息与首页配置',
    to: '/admin/site',
    icon: 'i-lucide-settings-2'
  }
]

/**
 * 格式化统计数字。
 * 作用：统一将后台首页中的数量值转成更易读的中文数字格式。
 *
 * @param value 原始数值
 * @returns 格式化后的展示文本
 */
function formatCount(value: number) {
  return new Intl.NumberFormat('zh-CN').format(Math.max(0, value || 0))
}

/**
 * 读取本地访问令牌。
 * 作用：仅在后台首页展示当前浏览器保存的访问令牌，便于站长确认连接信息。
 */
function loadPersistedAccessToken() {
  if (!import.meta.client) {
    return
  }

  persistedAccessToken.value = localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) || ''
}

/**
 * 切换访问令牌展示状态。
 * 作用：默认隐藏敏感令牌，仅在站长主动点击后显示真实值。
 */
function toggleAccessTokenVisibility() {
  showAccessToken.value = !showAccessToken.value
}

/**
 * 复制 AI skill 认证信息。
 * 作用：把访问令牌和后台接口地址组合成可直接提供给 AI 的文本，减少重复复制操作。
 */
async function copyAiSkillCredentials() {
  if (!import.meta.client || !persistedAccessToken.value || !apiBase.value) {
    toast.add({
      title: '复制认证信息失败',
      description: '当前访问令牌或后台接口地址不完整。',
      color: 'error'
    })
    return
  }

  const credentialText = `yunyu_access_token:${persistedAccessToken.value}
后台接口地址:${apiBase.value}`

  try {
    await navigator.clipboard.writeText(credentialText)
    toast.add({
      title: 'AI skill 认证信息已复制',
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: '复制认证信息失败',
      description: error?.message || '浏览器暂时无法访问剪贴板。',
      color: 'error'
    })
  }
}

/**
 * 计算访问令牌展示文本。
 * 作用：在默认情况下把令牌完全掩码，避免后台首页直接暴露敏感凭证。
 */
const displayAccessToken = computed(() => {
  if (!persistedAccessToken.value) {
    return '未检测到'
  }

  if (showAccessToken.value) {
    return persistedAccessToken.value
  }

  return '*'.repeat(persistedAccessToken.value.length)
})

/**
 * 读取后台首页聚合数据。
 * 作用：复用现有后台文章、评论和专题接口，组装工作台首页所需的真实统计与待处理信息。
 *
 * @returns 后台首页聚合数据
 */
async function loadDashboardPayload(): Promise<AdminDashboardPayload> {
  const [
    publishedPostsResponse,
    draftPostsResponse,
    topicsResponse,
    pendingCommentsResponse,
    recentPostsResponse,
    recentDraftPostsResponse
  ] = await Promise.all([
    adminPosts.listPosts({ status: 'PUBLISHED', pageNo: 1, pageSize: 1 }),
    adminPosts.listPosts({ status: 'DRAFT', pageNo: 1, pageSize: 1 }),
    adminTaxonomy.listItems('topic', { status: 'ACTIVE', pageNo: 1, pageSize: 1 }),
    adminComments.listComments({ status: 'PENDING', pageNo: 1, pageSize: 3 }),
    adminPosts.listPosts({ pageNo: 1, pageSize: 3 }),
    adminPosts.listPosts({ status: 'DRAFT', pageNo: 1, pageSize: 3 })
  ])

  return {
    publishedPostTotal: publishedPostsResponse.total,
    draftPostTotal: draftPostsResponse.total,
    topicTotal: topicsResponse.total,
    pendingCommentTotal: pendingCommentsResponse.total,
    recentPosts: recentPostsResponse.list,
    recentDraftPosts: recentDraftPostsResponse.list,
    recentPendingComments: pendingCommentsResponse.list
  }
}

const {
  data: dashboardData,
  pending: dashboardPending,
  error: dashboardError,
  refresh: refreshDashboard
} = await useAsyncData('admin-dashboard-home', loadDashboardPayload)

watch(dashboardError, value => {
  if (!value) {
    return
  }

  toast.add({
    title: '后台首页加载失败',
    description: value.message || '工作台数据暂时无法获取，请稍后重试。',
    color: 'error'
  })
}, { immediate: true })

onMounted(() => {
  loadPersistedAccessToken()
})

/**
 * 手动刷新后台首页数据。
 * 作用：供工作台首页顶部刷新按钮复用，便于在不离开当前页面的情况下重新拉取真实统计。
 */
async function handleRefreshDashboard() {
  try {
    await refreshDashboard()
    loadPersistedAccessToken()
    toast.add({
      title: '后台首页已刷新',
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: '刷新失败',
      description: error?.message || '工作台数据暂时无法刷新。',
      color: 'error'
    })
  }
}

const overviewCards = computed<AdminDashboardOverviewCard[]>(() => {
  const payload = dashboardData.value

  return [
    {
      title: '已发布文章',
      value: formatCount(payload?.publishedPostTotal || 0),
      hint: '当前对前台可见的正式内容数量',
      color: 'primary'
    },
    {
      title: '草稿箱',
      value: formatCount(payload?.draftPostTotal || 0),
      hint: '仍待补充和发布的文章数量',
      color: 'warning'
    },
    {
      title: '有效专题',
      value: formatCount(payload?.topicTotal || 0),
      hint: '当前处于启用状态的专题数量',
      color: 'success'
    }
  ]
})

const pendingItems = computed<AdminDashboardPendingItem[]>(() => {
  const payload = dashboardData.value
  const latestPendingComment = payload?.recentPendingComments?.[0]
  const latestDraftPost = payload?.recentDraftPosts?.[0]
  const latestPost = payload?.recentPosts?.[0]

  return [
    {
      title: '待审核评论',
      value: `${formatCount(payload?.pendingCommentTotal || 0)} 条`,
      description: latestPendingComment
        ? `最新一条来自《${latestPendingComment.postTitle}》`
        : '当前没有待审核评论，评论区状态正常。',
      to: '/admin/comments?status=PENDING',
      icon: 'i-lucide-message-circle-more'
    },
    {
      title: '草稿待完善',
      value: `${formatCount(payload?.draftPostTotal || 0)} 篇`,
      description: latestDraftPost
        ? `最近草稿：${latestDraftPost.title}`
        : '当前草稿箱为空，可以直接开始创作新文章。',
      to: '/admin/posts?status=DRAFT',
      icon: 'i-lucide-file-pen-line'
    },
    {
      title: '最近更新',
      value: latestPost?.title || '暂无文章',
      description: latestPost
        ? `最近更新于 ${latestPost.updatedAt}`
        : '当前还没有可展示的文章更新记录。',
      to: latestPost ? `/admin/posts/${latestPost.id}/edit` : '/admin/posts',
      icon: 'i-lucide-sparkles'
    }
  ]
})
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">后台首页</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
            {{ auth.currentUser?.userName || auth.currentUser?.email || '已登录' }}
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UBadge color="success" variant="soft" class="rounded-[8px] px-3 py-1">已登录</UBadge>
          <AdminButton
            icon="i-lucide-refresh-cw"
            :loading="dashboardPending"
            label="刷新数据"
            tone="neutral"
            variant="ghost"
            @click="handleRefreshDashboard"
          />
          <AdminButton
            icon="i-lucide-log-out"
            label="退出登录"
            tone="neutral"
            variant="ghost"
            @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
          />
        </div>
      </div>
    </section>

    <div class="space-y-4">
      <section class="grid gap-4 md:grid-cols-3">
        <div
          v-for="card in overviewCards"
          :key="card.title"
          class="rounded-[16px] border border-white/55 bg-white/70 p-5 shadow-[0_16px_32px_-30px_rgba(15,23,42,0.14)] backdrop-blur-lg dark:border-white/10 dark:bg-white/5 dark:shadow-none"
        >
          <div class="flex items-center justify-between gap-3">
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
            <UBadge :color="card.color" variant="soft" class="rounded-[8px] px-2.5 py-1">{{ card.value }}</UBadge>
          </div>
          <p class="mt-5 text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
          <p class="mt-2 text-xs leading-5 text-slate-500 dark:text-slate-400">{{ card.hint }}</p>
        </div>
      </section>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
          <div class="mb-4 flex items-center justify-between gap-3">
            <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">快捷入口</h2>
            <AdminPrimaryButton label="新建文章" icon="i-lucide-plus" @click="navigateTo('/admin/posts/create')" />
          </div>

          <div class="grid gap-3 md:grid-cols-3">
            <button
              v-for="action in quickActions"
              :key="action.title"
              type="button"
              class="flex items-center gap-3 rounded-[14px] border border-white/60 bg-white/56 p-4 text-left backdrop-blur-md transition duration-200 hover:border-white/80 hover:bg-white/72 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/15 dark:hover:bg-white/8"
              @click="navigateTo(action.to)"
            >
              <div class="flex size-9 shrink-0 items-center justify-center rounded-[9px] bg-slate-100/90 text-slate-700 dark:bg-white/5 dark:text-slate-200">
                <UIcon :name="action.icon" class="size-4" />
              </div>
              <div class="min-w-0">
                <p class="text-sm font-medium text-slate-900 dark:text-slate-50">{{ action.title }}</p>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">{{ action.description }}</p>
              </div>
            </button>
          </div>
        </section>

        <section class="space-y-4">
          <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">AI skill 认证信息</h2>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-[10px] border border-white/60 bg-white/70 px-3 py-2 text-xs font-medium text-slate-700 transition hover:border-white/80 hover:bg-white dark:border-white/10 dark:bg-white/5 dark:text-slate-200 dark:hover:border-white/20 dark:hover:bg-white/10"
                aria-label="复制 AI skill 认证信息"
                @click="copyAiSkillCredentials"
              >
                <UIcon name="i-lucide-copy" class="size-4" />
                <span>复制</span>
              </button>
            </div>

            <div class="mt-4 space-y-3">
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">后端接口域名</p>
                <p class="mt-2 break-all font-mono text-sm text-slate-900 dark:text-slate-50">
                  {{ apiBase || '未配置' }}
                </p>
              </div>

              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0 flex-1">
                    <p class="text-xs font-medium uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">yunyu_access_token</p>
                    <p class="mt-2 break-all font-mono text-sm text-slate-900 dark:text-slate-50">
                      {{ displayAccessToken }}
                    </p>
                  </div>

                  <button
                    type="button"
                    class="inline-flex size-9 items-center justify-center rounded-[10px] border border-white/60 bg-white/70 text-slate-600 transition hover:border-white/80 hover:bg-white dark:border-white/10 dark:bg-white/5 dark:text-slate-300 dark:hover:border-white/20 dark:hover:bg-white/10"
                    :aria-label="showAccessToken ? '隐藏访问令牌' : '显示访问令牌'"
                    @click="toggleAccessTokenVisibility"
                  >
                    <UIcon :name="showAccessToken ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
                  </button>
                </div>
              </div>
            </div>
          </section>
        </section>
      </div>

      <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
        <div class="flex items-center justify-between gap-3">
          <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">待处理</h2>
          <span class="text-xs text-slate-500 dark:text-slate-400">实时取自当前后台数据</span>
        </div>

        <div class="mt-4 grid gap-3 md:grid-cols-3">
          <button
            v-for="item in pendingItems"
            :key="item.title"
            type="button"
            class="rounded-[14px] border border-white/60 bg-white/56 p-4 text-left backdrop-blur-md transition duration-200 hover:border-white/80 hover:bg-white/72 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/15 dark:hover:bg-white/8"
            @click="navigateTo(item.to)"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="flex size-9 shrink-0 items-center justify-center rounded-[9px] bg-slate-100/90 text-slate-700 dark:bg-white/5 dark:text-slate-200">
                <UIcon :name="item.icon" class="size-4" />
              </div>
              <span class="rounded-[8px] bg-sky-50 px-2.5 py-1 text-xs font-medium text-sky-700 dark:bg-sky-500/10 dark:text-sky-200">
                {{ item.value }}
              </span>
            </div>
            <p class="mt-4 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ item.title }}</p>
            <p class="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">{{ item.description }}</p>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>
