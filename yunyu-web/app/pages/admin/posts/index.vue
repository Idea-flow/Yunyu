<script setup lang="ts">
import type { AdminPostItem } from '../../../types/post'
import type { AdminTaxonomyItem } from '../../../types/taxonomy'

/**
 * 后台文章列表页。
 * 作用：为站长提供文章筛选、跳转新增页、跳转编辑页与删除管理的统一列表入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const route = useRoute()
const router = useRouter()
const toast = useToast()
const adminPosts = useAdminPosts()
const adminTaxonomy = useAdminTaxonomy()

type PostStatusFilter = 'ALL' | 'DRAFT' | 'PUBLISHED' | 'OFFLINE'
type PostTaxonomyFilter = 'ALL' | number
type PostFlagFilter = 'ALL' | 1 | 0

const isLoading = ref(false)
const isLoadingFilters = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeStatus = ref<PostStatusFilter>('ALL')
const activeCategoryId = ref<PostTaxonomyFilter>('ALL')
const activeTagId = ref<PostTaxonomyFilter>('ALL')
const activeTopicId = ref<PostTaxonomyFilter>('ALL')
const activeIsTop = ref<PostFlagFilter>('ALL')
const activeIsRecommend = ref<PostFlagFilter>('ALL')
const activeAllowComment = ref<PostFlagFilter>('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const isDeleteModalOpen = ref(false)
const deletingPost = ref<AdminPostItem | null>(null)

const posts = ref<AdminPostItem[]>([])
const categoryOptions = ref<Array<{ label: string, value: PostTaxonomyFilter }>>([
  { label: '全部分类', value: 'ALL' }
])
const tagOptions = ref<Array<{ label: string, value: PostTaxonomyFilter }>>([
  { label: '全部标签', value: 'ALL' }
])
const topicOptions = ref<Array<{ label: string, value: PostTaxonomyFilter }>>([
  { label: '全部专题', value: 'ALL' }
])

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下线', value: 'OFFLINE' }
] as const

const topOptions = [
  { label: '全部置顶状态', value: 'ALL' },
  { label: '已置顶', value: 1 },
  { label: '未置顶', value: 0 }
] as const

const recommendOptions = [
  { label: '全部推荐状态', value: 'ALL' },
  { label: '已推荐', value: 1 },
  { label: '未推荐', value: 0 }
] as const

const allowCommentOptions = [
  { label: '全部评论状态', value: 'ALL' },
  { label: '允许评论', value: 1 },
  { label: '禁止评论', value: 0 }
] as const

/**
 * 解析路由中的文章状态筛选值。
 * 作用：让后台文章页支持通过 URL 直接进入草稿、已发布等指定筛选视图。
 *
 * @param value 路由中的状态查询参数
 * @returns 合法的文章状态筛选值
 */
function resolveRouteStatus(value: unknown): PostStatusFilter {
  if (value === 'DRAFT' || value === 'PUBLISHED' || value === 'OFFLINE') {
    return value
  }

  return 'ALL'
}

/**
 * 解析路由中的二元开关筛选值。
 * 作用：统一处理置顶、推荐、评论开关在 URL 中的筛选恢复逻辑。
 *
 * @param value 路由中的查询参数
 * @returns 合法的布尔筛选值
 */
function resolveRouteFlag(value: unknown): PostFlagFilter {
  if (value === '1' || value === 1) {
    return 1
  }

  if (value === '0' || value === 0) {
    return 0
  }

  return 'ALL'
}

/**
 * 解析路由中的内容编排筛选值。
 * 作用：统一恢复分类、标签、专题等数字筛选参数，避免非法值污染列表请求。
 *
 * @param value 路由中的查询参数
 * @returns 合法的内容编排筛选值
 */
function resolveRouteTaxonomyValue(value: unknown): PostTaxonomyFilter {
  if (typeof value !== 'string' || !value.trim()) {
    return 'ALL'
  }

  const parsedValue = Number(value)

  return Number.isFinite(parsedValue) && parsedValue > 0 ? parsedValue : 'ALL'
}

/**
 * 根据当前路由恢复文章筛选条件。
 * 作用：让后台首页或其他入口通过 URL 查询参数直达指定文章筛选视图。
 */
function hydrateFiltersFromRoute() {
  searchKeyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  activeStatus.value = resolveRouteStatus(route.query.status)
  activeCategoryId.value = resolveRouteTaxonomyValue(route.query.categoryId)
  activeTagId.value = resolveRouteTaxonomyValue(route.query.tagId)
  activeTopicId.value = resolveRouteTaxonomyValue(route.query.topicId)
  activeIsTop.value = resolveRouteFlag(route.query.isTop)
  activeIsRecommend.value = resolveRouteFlag(route.query.isRecommend)
  activeAllowComment.value = resolveRouteFlag(route.query.allowComment)

  const routePageNo = Number(route.query.pageNo || 1)
  currentPage.value = Number.isFinite(routePageNo) && routePageNo > 0 ? routePageNo : 1
}

/**
 * 将当前筛选条件同步到路由。
 * 作用：让文章管理页的筛选结果支持刷新保留和入口直达，便于在后台不同页面间往返。
 */
async function syncRouteQuery() {
  const nextQuery: Record<string, string> = {}

  if (searchKeyword.value.trim()) {
    nextQuery.keyword = searchKeyword.value.trim()
  }

  if (activeStatus.value !== 'ALL') {
    nextQuery.status = activeStatus.value
  }

  if (activeCategoryId.value !== 'ALL') {
    nextQuery.categoryId = String(activeCategoryId.value)
  }

  if (activeTagId.value !== 'ALL') {
    nextQuery.tagId = String(activeTagId.value)
  }

  if (activeTopicId.value !== 'ALL') {
    nextQuery.topicId = String(activeTopicId.value)
  }

  if (activeIsTop.value !== 'ALL') {
    nextQuery.isTop = String(activeIsTop.value)
  }

  if (activeIsRecommend.value !== 'ALL') {
    nextQuery.isRecommend = String(activeIsRecommend.value)
  }

  if (activeAllowComment.value !== 'ALL') {
    nextQuery.allowComment = String(activeAllowComment.value)
  }

  if (currentPage.value > 1) {
    nextQuery.pageNo = String(currentPage.value)
  }

  await router.replace({
    path: '/admin/posts',
    query: nextQuery
  })
}

/**
 * 加载文章列表。
 * 会根据当前筛选条件和分页参数请求后台文章接口。
 */
async function loadPosts() {
  isLoading.value = true

  try {
    const response = await adminPosts.listPosts({
      keyword: searchKeyword.value || undefined,
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value,
      categoryId: activeCategoryId.value === 'ALL' ? undefined : activeCategoryId.value,
      tagId: activeTagId.value === 'ALL' ? undefined : activeTagId.value,
      topicId: activeTopicId.value === 'ALL' ? undefined : activeTopicId.value,
      isTop: activeIsTop.value === 'ALL' ? undefined : activeIsTop.value,
      isRecommend: activeIsRecommend.value === 'ALL' ? undefined : activeIsRecommend.value,
      allowComment: activeAllowComment.value === 'ALL' ? undefined : activeAllowComment.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })

    posts.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    const message = error?.message || '暂时无法获取文章列表。'
    toast.add({
      title: '加载文章失败',
      description: message.includes('资源不存在')
        ? '文章管理接口尚未加载，请重启后端服务后再访问。'
        : message,
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 加载文章筛选所需的内容编排选项。
 * 作用：从真实接口读取分类、标签、专题列表，用于文章管理页的筛选区。
 */
async function loadFilterOptions() {
  isLoadingFilters.value = true

  try {
    const [categoryResponse, tagResponse, topicResponse] = await Promise.all([
      adminTaxonomy.listItems('category', { pageNo: 1, pageSize: 100, status: 'ACTIVE' }),
      adminTaxonomy.listItems('tag', { pageNo: 1, pageSize: 100, status: 'ACTIVE' }),
      adminTaxonomy.listItems('topic', { pageNo: 1, pageSize: 100, status: 'ACTIVE' })
    ])

    categoryOptions.value = buildTaxonomyFilterOptions('全部分类', categoryResponse.list)
    tagOptions.value = buildTaxonomyFilterOptions('全部标签', tagResponse.list)
    topicOptions.value = buildTaxonomyFilterOptions('全部专题', topicResponse.list)
  } catch (error: any) {
    toast.add({
      title: '加载筛选项失败',
      description: error?.message || '分类、标签、专题筛选数据暂时无法读取。',
      color: 'error'
    })
  } finally {
    isLoadingFilters.value = false
  }
}

/**
 * 构建内容编排筛选选项。
 * 作用：统一为分类、标签、专题筛选生成带“全部”选项的下拉数据。
 *
 * @param allLabel 全部选项文案
 * @param items 内容编排条目列表
 * @returns 下拉选项
 */
function buildTaxonomyFilterOptions(allLabel: string, items: AdminTaxonomyItem[]) {
  return [
    { label: allLabel, value: 'ALL' as const },
    ...items.map(item => ({
      label: item.name,
      value: item.id as PostTaxonomyFilter
    }))
  ]
}

/**
 * 打开删除确认弹窗。
 *
 * @param post 当前文章数据
 */
function openDeleteModal(post: AdminPostItem) {
  deletingPost.value = post
  isDeleteModalOpen.value = true
}

/**
 * 确认删除当前选中的文章。
 * 仅在二次确认后执行真实删除请求。
 */
async function confirmDelete() {
  if (!deletingPost.value) {
    return
  }

  isDeleteSubmitting.value = true

  try {
    await adminPosts.deletePost(deletingPost.value.id)
    toast.add({ title: '文章已删除', color: 'success' })
    isDeleteModalOpen.value = false
    deletingPost.value = null

    if (posts.value.length === 1 && currentPage.value > 1) {
      currentPage.value -= 1
    }

    await loadPosts()
  } catch (error: any) {
    toast.add({
      title: '删除失败',
      description: error?.message || '当前文章暂时无法删除。',
      color: 'error'
    })
  } finally {
    isDeleteSubmitting.value = false
  }
}

/**
 * 提交搜索。
 * 调整搜索条件后会回到第一页再请求列表。
 */
async function handleSearch() {
  currentPage.value = 1
  await syncRouteQuery()
  await loadPosts()
}

/**
 * 重置筛选条件。
 * 作用：将文章列表页的关键词、状态、分类、标签、专题筛选恢复默认值，并重新加载第一页数据。
 */
async function handleResetFilters() {
  searchKeyword.value = ''
  activeStatus.value = 'ALL'
  activeCategoryId.value = 'ALL'
  activeTagId.value = 'ALL'
  activeTopicId.value = 'ALL'
  activeIsTop.value = 'ALL'
  activeIsRecommend.value = 'ALL'
  activeAllowComment.value = 'ALL'
  currentPage.value = 1
  await syncRouteQuery()
  await loadPosts()
}

/**
 * 处理分页切换。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  if (page === currentPage.value) {
    return
  }

  currentPage.value = page
  await syncRouteQuery()
  await loadPosts()
}

/**
 * 处理每页条数切换。
 *
 * @param nextPageSize 新的每页条数
 */
async function handlePageSizeChange(nextPageSize: number) {
  if (nextPageSize === pageSize.value) {
    return
  }

  pageSize.value = nextPageSize
  currentPage.value = 1
  await syncRouteQuery()
  await loadPosts()
}

/**
 * 跳转到文章新增页。
 * 使用独立页面承载内容编辑流程，替代原有长表单弹窗。
 */
async function goToCreatePage() {
  await navigateTo('/admin/posts/create')
}

/**
 * 跳转到文章编辑页。
 *
 * @param post 当前文章数据
 */
async function goToEditPage(post: AdminPostItem) {
  await navigateTo(`/admin/posts/${post.id}/edit`)
}

/**
 * 解析文章状态文案。
 *
 * @param status 状态值
 * @returns 状态文案
 */
function resolveStatusLabel(status: AdminPostItem['status']) {
  switch (status) {
    case 'PUBLISHED':
      return '已发布'
    case 'OFFLINE':
      return '已下线'
    default:
      return '草稿'
  }
}

/**
 * 解析文章状态徽标样式。
 *
 * @param status 状态值
 * @returns 徽标样式
 */
function resolveStatusBadgeClass(status: AdminPostItem['status']) {
  return status === 'PUBLISHED'
    ? 'border-sky-200/90 bg-sky-100/90 text-sky-700 dark:border-sky-400/25 dark:bg-sky-400/12 dark:text-sky-200'
    : 'border-slate-200/90 bg-slate-100/92 text-slate-600 dark:border-slate-700/80 dark:bg-slate-800/88 dark:text-slate-300'
}

/**
 * 解析文章布尔状态徽标文案。
 * 作用：统一生成置顶、推荐和评论开关在列表中的短标签文案，方便快速扫读。
 *
 * @param flag 状态字段
 * @param enabled 是否开启
 * @returns 徽标文案
 */
function resolvePostFlagLabel(flag: 'isTop' | 'isRecommend' | 'allowComment', enabled: boolean) {
  switch (flag) {
    case 'isTop':
      return enabled ? '已置顶' : '未置顶'
    case 'isRecommend':
      return enabled ? '已推荐' : '未推荐'
    default:
      return enabled ? '可评论' : '已禁评'
  }
}

/**
 * 解析文章布尔状态徽标样式。
 * 作用：统一控制开启态浅蓝、关闭态灰色的视觉反馈，让四个状态保持一致。
 *
 * @param enabled 是否开启
 * @returns 徽标样式
 */
function resolvePostFlagBadgeClass(enabled: boolean) {
  return enabled
    ? 'border-sky-200/90 bg-sky-100/90 text-sky-700 dark:border-sky-400/25 dark:bg-sky-400/12 dark:text-sky-200'
    : 'border-slate-200/90 bg-slate-100/92 text-slate-600 dark:border-slate-700/80 dark:bg-slate-800/88 dark:text-slate-300'
}

/**
 * 解析文章归属预览摘要。
 * 用于在表格中保持单行简洁展示，避免分类、标签和专题过长时挤压列表布局。
 *
 * @param post 当前文章
 * @returns 归属预览文案
 */
function resolvePostMetaSummary(post: AdminPostItem) {
  const sections: string[] = []

  if (post.categoryName) {
    sections.push(`分类 · ${post.categoryName}`)
  }

  if (post.tagNames?.length) {
    sections.push(`标签 · ${post.tagNames.slice(0, 2).join(' / ')}`)
  }

  if (post.topicNames?.length) {
    sections.push(`专题 · ${post.topicNames.slice(0, 2).join(' / ')}`)
  }

  if (!sections.length) {
    return '暂未设置分类、标签或专题'
  }

  return sections.join('  ·  ')
}

/**
 * 解析文章归属完整摘要。
 * 用于鼠标悬浮时展示分类、标签和专题的全部内容，让长归属信息也能完整查看。
 *
 * @param post 当前文章
 * @returns 归属完整文案
 */
function resolvePostMetaFullSummary(post: AdminPostItem) {
  const sections: string[] = []

  if (post.categoryName) {
    sections.push(`分类：${post.categoryName}`)
  }

  if (post.tagNames?.length) {
    sections.push(`标签：${post.tagNames.join(' / ')}`)
  }

  if (post.topicNames?.length) {
    sections.push(`专题：${post.topicNames.join(' / ')}`)
  }

  if (!sections.length) {
    return '暂未设置分类、标签或专题'
  }

  return sections.join('\n')
}

watch(
  () => route.fullPath,
  async () => {
    hydrateFiltersFromRoute()
    await loadPosts()
  }
)

hydrateFiltersFromRoute()

await Promise.all([
  loadFilterOptions(),
  loadPosts()
])
</script>

<template>
  <div class="space-y-4">
    <AdminListPageHeader title="文章管理">
      <template #actions>
        <AdminPrimaryButton label="新增文章" icon="i-lucide-file-plus-2" @click="goToCreatePage" />
      </template>
    </AdminListPageHeader>

    <div class="space-y-4">
      <AdminFilterPanel>
        <template #search>
          <AdminInput
            v-model="searchKeyword"
            icon="i-lucide-search"
            class="w-full"
            placeholder="搜索标题或 Slug"
          />
        </template>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeStatus"
            :items="statusOptions"
            class="w-full"
            placeholder="状态"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeCategoryId"
            :items="categoryOptions"
            class="w-full"
            :disabled="isLoadingFilters"
            placeholder="分类"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeTagId"
            :items="tagOptions"
            class="w-full"
            :disabled="isLoadingFilters"
            placeholder="标签"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeTopicId"
            :items="topicOptions"
            class="w-full"
            :disabled="isLoadingFilters"
            placeholder="专题"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeIsTop"
            :items="topOptions"
            class="w-full"
            placeholder="置顶"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeIsRecommend"
            :items="recommendOptions"
            class="w-full"
            placeholder="推荐"
          />
        </div>

        <div class="min-w-[10rem] flex-1 sm:max-w-[calc(50%-0.375rem)] xl:w-[10rem] xl:flex-none">
          <AdminSelect
            v-model="activeAllowComment"
            :items="allowCommentOptions"
            class="w-full"
            placeholder="评论"
          />
        </div>

        <div class="flex w-full flex-wrap items-center justify-end gap-2 xl:ml-auto xl:w-auto">
          <UButton
            label="重置"
            color="neutral"
            variant="ghost"
            class="cursor-pointer rounded-[10px]"
            @click="handleResetFilters"
          />
          <AdminPrimaryButton label="搜索" icon="i-lucide-search" @click="handleSearch" />
        </div>
      </AdminFilterPanel>

        <AdminTableCard title="文章列表">
          <AdminDataTable
            :is-loading="isLoading"
            :has-data="posts.length > 0"
            min-width="1020px"
            header-class="grid-cols-[3.25rem_5rem_minmax(0,1.58fr)_1fr_1fr_0.8fr_0.7fr]"
            empty-title="没有找到匹配的文章"
            empty-icon="i-lucide-file-search"
          >
            <template #header>
              <p class="text-center">ID</p>
              <p>封面</p>
              <p>标题</p>
              <p>状态</p>
              <p>内容归属</p>
              <p>最近更新</p>
              <p class="text-right">操作</p>
            </template>

          <article
            v-for="post in posts"
            :key="post.id"
            class="grid items-center gap-4 px-4 py-3.5 transition duration-200 hover:bg-white/60 dark:hover:bg-white/5"
            :class="'grid-cols-[3.25rem_5rem_minmax(0,1.58fr)_1fr_1fr_0.8fr_0.7fr]'"
          >
            <div class="min-w-0 text-center">
                <p class="truncate text-sm font-medium text-toned">{{ post.id }}</p>
            </div>

              <div class="flex items-center">
                <div class="flex h-12 w-18 items-center justify-center overflow-hidden rounded-[12px] border border-white/55 bg-slate-100/80 dark:border-white/10 dark:bg-slate-900/70">
                  <img
                    v-if="post.coverUrl"
                    :src="post.coverUrl"
                    :alt="post.title"
                    class="h-full w-full object-cover"
                  >
                  <UIcon
                    v-else
                    name="i-lucide-image"
                    class="size-4 text-slate-400 dark:text-slate-500"
                  />
                </div>
              </div>

              <div class="min-w-0">
                <a
                  :href="`/posts/${post.slug}`"
                  target="_blank"
                  rel="noopener noreferrer"
                  class="block truncate text-[15px] font-semibold text-highlighted transition-colors duration-200 hover:text-sky-600 dark:hover:text-sky-300"
                >
                  {{ post.title }}
                </a>
                <div class="mt-1.5 flex flex-wrap items-center gap-2 text-sm text-muted">
                  <span>{{ post.slug }}</span>
                  <span class="text-border">·</span>
                  <span>{{ post.wordCount }} 字</span>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-1.5">
                <span
                  class="inline-flex items-center justify-center rounded-full border px-2 py-1 text-center text-xs font-medium"
                  :class="resolveStatusBadgeClass(post.status)"
                >
                  {{ resolveStatusLabel(post.status) }}
                </span>
                <span
                  class="inline-flex items-center justify-center rounded-full border px-2 py-1 text-center text-xs font-medium"
                  :class="resolvePostFlagBadgeClass(post.isTop === true)"
                >
                  {{ resolvePostFlagLabel('isTop', post.isTop === true) }}
                </span>
                <span
                  class="inline-flex items-center justify-center rounded-full border px-2 py-1 text-center text-xs font-medium"
                  :class="resolvePostFlagBadgeClass(post.isRecommend === true)"
                >
                  {{ resolvePostFlagLabel('isRecommend', post.isRecommend === true) }}
                </span>
                <span
                  class="inline-flex items-center justify-center rounded-full border px-2 py-1 text-center text-xs font-medium"
                  :class="resolvePostFlagBadgeClass(post.allowComment !== false)"
                >
                  {{ resolvePostFlagLabel('allowComment', post.allowComment !== false) }}
                </span>
              </div>

              <div class="min-w-0">
                <UPopover
                  mode="hover"
                  :arrow="true"
                  :open-delay="20"
                  :close-delay="50"
                  :content="{
                    side: 'top',
                    align: 'start',
                    sideOffset: 10
                  }"
                  :ui="{
                    content: 'max-w-[17rem] rounded-2xl border border-slate-200/80 bg-white/98 px-3 py-2.5 text-xs leading-6 text-slate-700 shadow-[0_18px_40px_-26px_rgba(15,23,42,0.28)] dark:border-slate-700/75 dark:bg-slate-900/98 dark:text-slate-200 dark:shadow-[0_18px_42px_-24px_rgba(0,0,0,0.48)]'
                  }"
                  class="hidden md:block"
                >
                  <button
                    type="button"
                    class="group flex w-full items-center gap-1.5 truncate rounded-xl bg-transparent text-left text-sm text-toned outline-none transition-colors duration-200 hover:text-sky-700 dark:hover:text-sky-300"
                  >
                    <span class="min-w-0 flex-1 truncate">
                      {{ resolvePostMetaSummary(post) }}
                    </span>
                    <UIcon
                      name="i-lucide-chevrons-up"
                      class="size-3.5 shrink-0 text-slate-300 opacity-0 transition duration-200 group-hover:opacity-100 dark:text-slate-600"
                    />
                  </button>

                  <template #content>
                    <p class="whitespace-pre-line text-xs leading-6 text-slate-700 dark:text-slate-200">
                      {{ resolvePostMetaFullSummary(post) }}
                    </p>
                  </template>
                </UPopover>

                <UPopover
                  mode="click"
                  :arrow="true"
                  :content="{
                    side: 'top',
                    align: 'start',
                    sideOffset: 10
                  }"
                  :ui="{
                    content: 'max-w-[17rem] rounded-2xl border border-slate-200/80 bg-white/98 px-3 py-2.5 text-xs leading-6 text-slate-700 shadow-[0_18px_40px_-26px_rgba(15,23,42,0.28)] dark:border-slate-700/75 dark:bg-slate-900/98 dark:text-slate-200 dark:shadow-[0_18px_42px_-24px_rgba(0,0,0,0.48)]'
                  }"
                  class="md:hidden"
                >
                  <button
                    type="button"
                    class="flex w-full items-center gap-1.5 truncate rounded-xl bg-transparent text-left text-sm text-toned outline-none transition-colors duration-200 active:text-sky-700 dark:active:text-sky-300"
                  >
                    <span class="min-w-0 flex-1 truncate">
                      {{ resolvePostMetaSummary(post) }}
                    </span>
                    <UIcon
                      name="i-lucide-chevrons-up"
                      class="size-3.5 shrink-0 text-slate-300 dark:text-slate-600"
                    />
                  </button>

                  <template #content>
                    <p class="whitespace-pre-line text-xs leading-6 text-slate-700 dark:text-slate-200">
                      {{ resolvePostMetaFullSummary(post) }}
                    </p>
                  </template>
                </UPopover>
              </div>

              <div class="text-sm text-toned">
                <p>{{ post.updatedAt }}</p>
              </div>

              <div class="flex items-center justify-end gap-2">
                <AdminActionIconButton
                  icon="i-lucide-pencil-line"
                  label="编辑文章"
                  @click="goToEditPage(post)"
                />
                <AdminActionIconButton
                  icon="i-lucide-trash-2"
                  label="删除文章"
                  tone="danger"
                  @click="openDeleteModal(post)"
                />
              </div>
            </article>
          </AdminDataTable>

          <template #footer>
            <AdminPaginationBar
              :page="currentPage"
              :page-size="pageSize"
              :total="total"
              :total-pages="totalPages"
              @update:page="handlePageChange"
              @update:page-size="handlePageSizeChange"
            />
          </template>
        </AdminTableCard>
      </div>

        <AdminConfirmModal
          v-model:open="isDeleteModalOpen"
          title="确认删除文章"
          :description="deletingPost ? `删除后将无法在后台列表中继续管理“${deletingPost.title}”。` : '请确认是否继续删除当前文章。'"
          confirm-label="确认删除"
          :loading="isDeleteSubmitting"
          @confirm="confirmDelete"
        />
  </div>
</template>
