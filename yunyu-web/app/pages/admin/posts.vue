<script setup lang="ts">
/**
 * 后台文章管理页。
 * 作用：为站长提供文章检索、状态筛选、内容巡检和进入创作流程的统一入口，
 * 作为后台内容运营的核心工作页面。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

type PostStatus = 'PUBLISHED' | 'DRAFT' | 'REVIEW'
type StatusFilter = 'ALL' | PostStatus
type SortMode = 'UPDATED' | 'PUBLISHED' | 'READING'

interface AdminPostRecord {
  id: number
  title: string
  slug: string
  topic: string
  status: PostStatus
  coverReady: boolean
  summaryReady: boolean
  readingMinutes: number
  wordCount: number
  updatedAt: string
  publishedAt: string | null
}

interface OverviewCard {
  title: string
  value: string
  description: string
  color: 'primary' | 'neutral' | 'warning'
}

interface SortOption {
  value: SortMode
  label: string
}

interface StatusOption {
  value: StatusFilter
  label: string
}

/**
 * 页面顶部概览数据。
 * 当前阶段先使用静态数据建立版式和信息层级，后续再接真实文章接口。
 */
const overviewCards: OverviewCard[] = [
  {
    title: '已发布',
    value: '24',
    description: '本周新增 3 篇',
    color: 'primary'
  },
  {
    title: '草稿箱',
    value: '08',
    description: '其中 2 篇等待封面',
    color: 'neutral'
  },
  {
    title: '待校对',
    value: '05',
    description: '建议今天内完成复核',
    color: 'warning'
  }
]

/**
 * 文章列表数据。
 * 用于表现后台文章管理页的完整信息结构，包括状态、专题和内容完整度。
 */
const allPosts = ref<AdminPostRecord[]>([
  {
    id: 1,
    title: '把内容站做成作品，而不是把作品塞进模板里',
    slug: 'build-content-site-like-a-work',
    topic: '品牌与设计',
    status: 'PUBLISHED',
    coverReady: true,
    summaryReady: true,
    readingMinutes: 8,
    wordCount: 2640,
    updatedAt: '2026-03-30 21:18',
    publishedAt: '2026-03-28 10:00'
  },
  {
    id: 2,
    title: '编辑感首页的留白应该如何被计算',
    slug: 'editorial-homepage-whitespace',
    topic: '界面系统',
    status: 'REVIEW',
    coverReady: true,
    summaryReady: false,
    readingMinutes: 6,
    wordCount: 1980,
    updatedAt: '2026-03-31 09:40',
    publishedAt: null
  },
  {
    id: 3,
    title: 'Nuxt 内容网站的主题系统不该后补',
    slug: 'nuxt-theme-system-first',
    topic: '前端工程',
    status: 'DRAFT',
    coverReady: false,
    summaryReady: true,
    readingMinutes: 10,
    wordCount: 3260,
    updatedAt: '2026-03-31 08:12',
    publishedAt: null
  },
  {
    id: 4,
    title: '后台工作台如何保持秩序感，而不是控制台感',
    slug: 'admin-workspace-order',
    topic: '后台体验',
    status: 'PUBLISHED',
    coverReady: true,
    summaryReady: true,
    readingMinutes: 5,
    wordCount: 1740,
    updatedAt: '2026-03-29 18:06',
    publishedAt: '2026-03-27 14:30'
  },
  {
    id: 5,
    title: '内容推荐位的更新节奏与专题节奏应该怎么配合',
    slug: 'editorial-promo-rhythm',
    topic: '内容运营',
    status: 'REVIEW',
    coverReady: false,
    summaryReady: false,
    readingMinutes: 7,
    wordCount: 2410,
    updatedAt: '2026-03-31 07:28',
    publishedAt: null
  }
])

/**
 * 状态筛选选项。
 * 用于快速限定文章列表的展示范围。
 */
const statusOptions: StatusOption[] = [
  { value: 'ALL', label: '全部' },
  { value: 'PUBLISHED', label: '已发布' },
  { value: 'DRAFT', label: '草稿' },
  { value: 'REVIEW', label: '待校对' }
]

/**
 * 排序选项。
 * 用于在不同内容管理场景下切换查看优先级。
 */
const sortOptions: SortOption[] = [
  { value: 'UPDATED', label: '最近更新' },
  { value: 'PUBLISHED', label: '发布时间' },
  { value: 'READING', label: '阅读时长' }
]

const searchKeyword = ref('')
const activeStatus = ref<StatusFilter>('ALL')
const activeSort = ref<SortMode>('UPDATED')

/**
 * 计算筛选后的文章列表。
 * 会同时处理关键词搜索、状态筛选和排序规则。
 */
const filteredPosts = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return [...allPosts.value]
    .filter((post) => {
      if (activeStatus.value !== 'ALL' && post.status !== activeStatus.value) {
        return false
      }

      if (!keyword) {
        return true
      }

      return [
        post.title,
        post.slug,
        post.topic
      ].some((field) => field.toLowerCase().includes(keyword))
    })
    .sort((left, right) => {
      if (activeSort.value === 'READING') {
        return right.readingMinutes - left.readingMinutes
      }

      if (activeSort.value === 'PUBLISHED') {
        return (right.publishedAt || '').localeCompare(left.publishedAt || '')
      }

      return right.updatedAt.localeCompare(left.updatedAt)
    })
})

/**
 * 计算当前筛选结果中的内容质量问题总数。
 * 用于在侧栏中展示本轮整理优先项。
 */
const contentIssueCount = computed(() =>
  filteredPosts.value.filter((post) => !post.coverReady || !post.summaryReady).length
)

/**
 * 计算当前筛选结果中的已发布文章数量。
 * 用于辅助运营侧栏呈现发布状态。
 */
const publishedCount = computed(() =>
  filteredPosts.value.filter((post) => post.status === 'PUBLISHED').length
)

/**
 * 将文章状态转换为 Nuxt UI 颜色。
 * 让列表状态在明暗模式下仍保持清晰一致的语义反馈。
 */
function resolveStatusColor(status: PostStatus) {
  switch (status) {
    case 'PUBLISHED':
      return 'primary' as const
    case 'REVIEW':
      return 'warning' as const
    default:
      return 'neutral' as const
  }
}

/**
 * 将文章状态转换为用户可读文案。
 * 保持列表中的状态表达简洁统一。
 */
function resolveStatusLabel(status: PostStatus) {
  switch (status) {
    case 'PUBLISHED':
      return '已发布'
    case 'REVIEW':
      return '待校对'
    default:
      return '草稿'
  }
}
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="文章管理">
        <template #right>
          <div class="flex items-center gap-3">
            <UButton
              icon="i-lucide-square-pen"
              label="新建文章"
              color="primary"
              class="rounded-2xl"
            />
          </div>
        </template>
      </UDashboardNavbar>

      <UDashboardToolbar>
        <template #left>
          <div>
            <p class="text-sm font-medium text-highlighted">内容中心</p>
            <p class="text-xs text-muted">集中处理文章搜索、筛选、校对与发布节奏</p>
          </div>
        </template>
      </UDashboardToolbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <div class="grid gap-4 md:grid-cols-3">
          <UCard
            v-for="card in overviewCards"
            :key="card.title"
            class="overflow-hidden rounded-[28px] border border-default/70 bg-default/95 shadow-sm"
          >
            <div class="space-y-3">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-medium text-muted">{{ card.title }}</p>
                <UBadge :color="card.color" variant="soft">{{ card.description }}</UBadge>
              </div>
              <p class="text-4xl font-semibold tracking-tight text-highlighted">{{ card.value }}</p>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[minmax(0,1.6fr)_21rem]">
          <div class="space-y-6">
            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <div class="flex flex-col gap-5">
                <div class="flex flex-wrap items-center gap-3">
                  <button
                    v-for="option in statusOptions"
                    :key="option.value"
                    type="button"
                    class="min-h-11 rounded-full border px-4 text-sm font-medium transition"
                    :class="activeStatus === option.value
                      ? 'border-primary/30 bg-primary text-inverted shadow-sm'
                      : 'border-default bg-default text-highlighted hover:border-primary/30 hover:bg-muted/70'"
                    @click="activeStatus = option.value"
                  >
                    {{ option.label }}
                  </button>
                </div>

                <div class="flex flex-col gap-4 xl:flex-row xl:items-center">
                  <UInput
                    v-model="searchKeyword"
                    icon="i-lucide-search"
                    size="xl"
                    class="w-full"
                    placeholder="搜索标题、Slug 或专题"
                    :ui="{
                      base: 'min-h-13 rounded-2xl'
                    }"
                  />

                  <div class="flex flex-wrap items-center gap-3">
                    <span class="text-sm text-muted">排序</span>
                    <button
                      v-for="option in sortOptions"
                      :key="option.value"
                      type="button"
                      class="min-h-11 rounded-full border px-4 text-sm font-medium transition"
                      :class="activeSort === option.value
                        ? 'border-default bg-muted text-highlighted'
                        : 'border-default bg-default text-toned hover:bg-muted/70'"
                      @click="activeSort = option.value"
                    >
                      {{ option.label }}
                    </button>
                  </div>
                </div>
              </div>
            </UCard>

            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <template #header>
                <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p class="text-base font-semibold text-highlighted">文章列表</p>
                    <p class="text-sm text-muted">当前共 {{ filteredPosts.length }} 篇文章符合筛选条件</p>
                  </div>

                  <UBadge color="neutral" variant="soft">
                    {{ activeStatus === 'ALL' ? '全部状态' : statusOptions.find((option) => option.value === activeStatus)?.label }}
                  </UBadge>
                </div>
              </template>

              <div class="overflow-hidden rounded-[24px] border border-default/70">
                <div class="hidden grid-cols-[minmax(0,1.8fr)_0.8fr_0.8fr_0.8fr_0.7fr] gap-4 border-b border-default/70 bg-muted/40 px-5 py-4 text-xs font-semibold uppercase tracking-[0.14em] text-muted lg:grid">
                  <p>文章</p>
                  <p>状态</p>
                  <p>专题</p>
                  <p>最近更新</p>
                  <p class="text-right">操作</p>
                </div>

                <div class="divide-y divide-default/70">
                  <article
                    v-for="post in filteredPosts"
                    :key="post.id"
                    class="grid gap-4 px-5 py-5 transition hover:bg-muted/30 lg:grid-cols-[minmax(0,1.8fr)_0.8fr_0.8fr_0.8fr_0.7fr] lg:items-center"
                  >
                    <div class="min-w-0">
                      <p class="truncate text-base font-semibold text-highlighted">{{ post.title }}</p>
                      <div class="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted">
                        <span>{{ post.slug }}</span>
                        <span class="text-border">·</span>
                        <span>{{ post.wordCount }} 字</span>
                        <span class="text-border">·</span>
                        <span>{{ post.readingMinutes }} 分钟阅读</span>
                      </div>
                    </div>

                    <div class="flex items-center lg:block">
                      <UBadge :color="resolveStatusColor(post.status)" variant="soft">
                        {{ resolveStatusLabel(post.status) }}
                      </UBadge>
                    </div>

                    <div class="text-sm text-toned">
                      {{ post.topic }}
                    </div>

                    <div class="space-y-1 text-sm text-toned">
                      <p>{{ post.updatedAt }}</p>
                      <p v-if="post.publishedAt" class="text-xs text-muted">
                        发布于 {{ post.publishedAt }}
                      </p>
                    </div>

                    <div class="flex items-center justify-start gap-2 lg:justify-end">
                      <UButton
                        icon="i-lucide-eye"
                        color="neutral"
                        variant="ghost"
                        aria-label="查看文章"
                      />
                      <UButton
                        icon="i-lucide-pencil-line"
                        color="neutral"
                        variant="ghost"
                        aria-label="编辑文章"
                      />
                      <UDropdownMenu
                        :items="[
                          [{ label: '预览', icon: 'i-lucide-eye' }],
                          [{ label: '复制链接', icon: 'i-lucide-link' }],
                          [{ label: '归档', icon: 'i-lucide-archive' }]
                        ]"
                      >
                        <UButton
                          icon="i-lucide-ellipsis"
                          color="neutral"
                          variant="ghost"
                          aria-label="更多操作"
                        />
                      </UDropdownMenu>
                    </div>

                    <div class="flex flex-wrap items-center gap-2 lg:col-span-5">
                      <UBadge
                        :color="post.coverReady ? 'success' : 'warning'"
                        variant="subtle"
                      >
                        {{ post.coverReady ? '封面已准备' : '缺少封面' }}
                      </UBadge>
                      <UBadge
                        :color="post.summaryReady ? 'success' : 'warning'"
                        variant="subtle"
                      >
                        {{ post.summaryReady ? '摘要完整' : '缺少摘要' }}
                      </UBadge>
                    </div>
                  </article>

                  <div
                    v-if="filteredPosts.length === 0"
                    class="px-6 py-14 text-center"
                  >
                    <p class="text-base font-medium text-highlighted">没有找到匹配的文章</p>
                    <p class="mt-2 text-sm text-muted">可以尝试调整关键词或切换状态筛选。</p>
                  </div>
                </div>
              </div>
            </UCard>
          </div>

          <div class="space-y-6">
            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <template #header>
                <div>
                  <p class="text-base font-semibold text-highlighted">本轮整理重点</p>
                  <p class="text-sm text-muted">先处理会影响发布质量的内容项</p>
                </div>
              </template>

              <div class="space-y-4">
                <div class="rounded-[24px] border border-default/70 bg-muted/35 p-4">
                  <p class="text-xs uppercase tracking-[0.16em] text-muted">内容问题</p>
                  <p class="mt-3 text-3xl font-semibold text-highlighted">{{ contentIssueCount }}</p>
                  <p class="mt-2 text-sm leading-7 text-toned">
                    当前筛选结果中，仍有 {{ contentIssueCount }} 篇文章需要补全封面或摘要。
                  </p>
                </div>

                <div class="rounded-[24px] border border-default/70 bg-muted/35 p-4">
                  <p class="text-xs uppercase tracking-[0.16em] text-muted">已发布数量</p>
                  <p class="mt-3 text-3xl font-semibold text-highlighted">{{ publishedCount }}</p>
                  <p class="mt-2 text-sm leading-7 text-toned">
                    已进入线上展示的内容会优先维持稳定，不建议频繁改动核心结构。
                  </p>
                </div>
              </div>
            </UCard>

            <UCard class="rounded-[30px] border border-default/70 bg-default/95 shadow-sm">
              <template #header>
                <div>
                  <p class="text-base font-semibold text-highlighted">内容检查清单</p>
                  <p class="text-sm text-muted">保持每篇文章在展示层面足够完整</p>
                </div>
              </template>

              <ul class="space-y-3">
                <li class="rounded-[22px] border border-default/70 bg-muted/30 px-4 py-4 text-sm leading-7 text-toned">
                  发布前确认封面、摘要、专题归属与标题节奏保持一致。
                </li>
                <li class="rounded-[22px] border border-default/70 bg-muted/30 px-4 py-4 text-sm leading-7 text-toned">
                  长文优先检查阅读时长、段落密度与首屏信息层级。
                </li>
                <li class="rounded-[22px] border border-default/70 bg-muted/30 px-4 py-4 text-sm leading-7 text-toned">
                  已发布内容调整时，优先维护链接稳定性与推荐位一致性。
                </li>
              </ul>
            </UCard>
          </div>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
