<script setup lang="ts">
import type {
  AdminCommentStatus,
  AdminCommentThreadGroup,
  AdminCommentThreadReplyItem,
  AdminCommentThreadRootItem
} from '../../types/comment'
import CommentRichContent from '../../components/content/CommentRichContent.vue'

/**
 * 后台评论管理页。
 * 作用：按文章分组展示评论主楼与回复流，为站长提供更清晰的评论审核、驳回和删除入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

type CommentStatusFilter = 'ALL' | AdminCommentStatus

const route = useRoute()
const router = useRouter()
const toast = useToast()
const adminComments = useAdminComments()

const isLoading = ref(false)
const actionCommentId = ref<number | null>(null)
const deletingComment = ref<AdminCommentThreadRootItem | AdminCommentThreadReplyItem | null>(null)
const isDeleteModalOpen = ref(false)
const searchKeyword = ref('')
const searchPostId = ref('')
const activeStatus = ref<CommentStatusFilter>('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const groups = ref<AdminCommentThreadGroup[]>([])

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
] as const

/**
 * 判断当前是否启用了筛选条件。
 * 作用：为重置按钮和筛选提示提供统一状态来源，避免页面出现多处重复判断。
 */
const hasActiveFilters = computed(() => {
  return Boolean(searchKeyword.value.trim() || searchPostId.value || activeStatus.value !== 'ALL')
})

/**
 * 判断当前是否启用了评论级筛选。
 * 作用：控制上下文评论和命中评论的视觉差异，帮助用户快速聚焦命中项。
 */
const hasCommentLevelFilter = computed(() => {
  return Boolean(searchKeyword.value.trim() || activeStatus.value !== 'ALL')
})

/**
 * 统计当前页展示的评论总数。
 * 作用：在页面头部补充当前审核视图的评论体量感知。
 */
const currentPageCommentCount = computed(() => {
  return groups.value.reduce((sum, group) => sum + group.totalCommentCount, 0)
})

/**
 * 当前空态标题。
 * 作用：根据是否启用筛选条件，给出更贴合当前场景的空态提示。
 */
const emptyTitle = computed(() => {
  return hasActiveFilters.value ? '当前筛选条件下暂无评论' : '暂时还没有评论记录'
})

/**
 * 删除确认说明。
 * 作用：明确提示删除评论时会连带隐藏该评论下的回复分支。
 */
const deleteDescription = computed(() => {
  if (!deletingComment.value) {
    return '删除后将无法恢复。'
  }

  return `确认删除「${deletingComment.value.userName}」的这条评论吗？若该评论下存在回复分支，也会一并从前台隐藏。`
})

/**
 * 解析路由中的评论状态筛选值。
 * 作用：让后台评论页支持通过 URL 直接进入“待审核评论”等指定筛选视图。
 *
 * @param value 路由中的状态查询参数
 * @returns 合法的评论状态筛选值
 */
function resolveRouteStatus(value: unknown): CommentStatusFilter {
  if (value === 'PENDING' || value === 'APPROVED' || value === 'REJECTED') {
    return value
  }

  return 'ALL'
}

/**
 * 根据当前路由同步评论筛选条件。
 * 作用：统一承接后台侧边栏、首页入口或手动输入 URL 时带来的查询参数，保持页面状态一致。
 */
function hydrateFiltersFromRoute() {
  searchKeyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  searchPostId.value = typeof route.query.postId === 'string' ? route.query.postId.replace(/[^\d]/g, '') : ''
  activeStatus.value = resolveRouteStatus(route.query.status)

  const routePageNo = Number(route.query.pageNo || 1)
  currentPage.value = Number.isFinite(routePageNo) && routePageNo > 0 ? routePageNo : 1
}

/**
 * 将当前筛选条件同步回路由。
 * 作用：让“待审核评论”等入口在刷新页面后仍保留当前筛选状态，也方便复制链接直达同一视图。
 */
async function syncRouteQuery() {
  const nextQuery: Record<string, string> = {}

  if (searchKeyword.value.trim()) {
    nextQuery.keyword = searchKeyword.value.trim()
  }

  if (searchPostId.value) {
    nextQuery.postId = searchPostId.value
  }

  if (activeStatus.value !== 'ALL') {
    nextQuery.status = activeStatus.value
  }

  if (currentPage.value > 1) {
    nextQuery.pageNo = String(currentPage.value)
  }

  await router.replace({
    path: '/admin/comments',
    query: nextQuery
  })
}

/**
 * 解析文章 ID 筛选值。
 * 作用：把后台筛选栏中的文章 ID 输入统一转换为接口可识别的数字参数。
 *
 * @returns 文章 ID 筛选值
 */
function resolveSearchPostId() {
  if (!searchPostId.value) {
    return undefined
  }

  return Number(searchPostId.value)
}

/**
 * 拉取评论树形审核列表。
 * 作用：根据当前关键词、状态、文章和分页参数刷新按文章分组的评论审核视图。
 */
async function loadCommentGroups() {
  isLoading.value = true

  try {
    const response = await adminComments.listCommentThreadGroups({
      keyword: searchKeyword.value || undefined,
      postId: resolveSearchPostId(),
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })
    groups.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: '加载评论失败',
      description: error?.message || '暂时无法获取评论审核视图，请稍后重试。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 执行评论状态更新。
 * 作用：供站长快速完成评论审核通过或驳回，并在成功后刷新当前审核视图。
 *
 * @param comment 评论条目
 * @param status 目标状态
 */
async function changeCommentStatus(comment: AdminCommentThreadRootItem | AdminCommentThreadReplyItem, status: AdminCommentStatus) {
  actionCommentId.value = comment.id

  try {
    await adminComments.updateCommentStatus(comment.id, status)
    toast.add({
      title: status === 'APPROVED' ? '评论已通过' : status === 'REJECTED' ? '评论已驳回' : '评论状态已更新',
      color: 'success'
    })
    await loadCommentGroups()
  } catch (error: any) {
    toast.add({
      title: '评论状态更新失败',
      description: error?.message || '状态更新未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    actionCommentId.value = null
  }
}

/**
 * 打开删除确认弹窗。
 * 作用：在真正删除评论前提供二次确认，避免误删整条回复分支。
 *
 * @param comment 评论条目
 */
function openDeleteModal(comment: AdminCommentThreadRootItem | AdminCommentThreadReplyItem) {
  deletingComment.value = comment
  isDeleteModalOpen.value = true
}

/**
 * 确认删除评论。
 * 作用：执行评论软删除，并刷新审核视图以同步最新数据。
 */
async function confirmDelete() {
  if (!deletingComment.value) {
    return
  }

  actionCommentId.value = deletingComment.value.id

  try {
    await adminComments.deleteComment(deletingComment.value.id)
    toast.add({
      title: '评论已删除',
      color: 'success'
    })
    isDeleteModalOpen.value = false
    deletingComment.value = null
    await loadCommentGroups()
  } catch (error: any) {
    toast.add({
      title: '评论删除失败',
      description: error?.message || '评论未能删除成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    actionCommentId.value = null
  }
}

/**
 * 处理文章 ID 输入。
 * 作用：将文章 ID 搜索框限制为纯数字输入，避免传入无效查询参数。
 *
 * @param value 当前输入值
 */
function handlePostIdInput(value: string | number | null) {
  searchPostId.value = String(value ?? '').replace(/[^\d]/g, '')
}

/**
 * 处理列表搜索。
 * 作用：在切换搜索条件后回到第一页，避免旧页码导致结果为空。
 */
async function handleSearch() {
  currentPage.value = 1
  await syncRouteQuery()
  await loadCommentGroups()
}

/**
 * 重置评论筛选条件。
 * 作用：一键清空关键词、文章 ID 与状态筛选，并回到第一页重新加载视图。
 */
async function resetFilters() {
  searchKeyword.value = ''
  searchPostId.value = ''
  activeStatus.value = 'ALL'
  currentPage.value = 1
  await syncRouteQuery()
  await loadCommentGroups()
}

/**
 * 处理筛选状态切换。
 * 作用：切换评论审核状态时同步刷新树形审核视图。
 *
 * @param value 当前选择值
 */
async function handleStatusChange(value: string | number | null) {
  activeStatus.value = (value as CommentStatusFilter) || 'ALL'
  currentPage.value = 1
  await syncRouteQuery()
  await loadCommentGroups()
}

/**
 * 处理评论分页切换。
 * 作用：在页码改变后刷新按文章分组的评论审核列表。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  currentPage.value = page
  await syncRouteQuery()
  await loadCommentGroups()
}

/**
 * 处理每页条数切换。
 * 作用：修改每页展示容量后回到第一页重新请求分组列表。
 *
 * @param value 新条数
 */
async function handlePageSizeChange(value: number) {
  pageSize.value = value
  currentPage.value = 1
  await syncRouteQuery()
  await loadCommentGroups()
}

/**
 * 解析评论状态颜色。
 * 作用：让待审、通过、驳回状态在后台审核页中具备稳定的视觉区分。
 *
 * @param status 评论状态
 * @returns 状态颜色
 */
function resolveStatusColor(status: AdminCommentStatus) {
  if (status === 'APPROVED') {
    return 'success'
  }

  if (status === 'REJECTED') {
    return 'error'
  }

  return 'warning'
}

/**
 * 解析评论状态文案。
 * 作用：统一后台审核页中的评论状态展示文本。
 *
 * @param status 评论状态
 * @returns 状态文案
 */
function resolveStatusLabel(status: AdminCommentStatus) {
  if (status === 'APPROVED') {
    return '已通过'
  }

  if (status === 'REJECTED') {
    return '已驳回'
  }

  return '待审核'
}

/**
 * 解析前台可见性说明。
 * 作用：让站长在后台直接理解当前评论是否已经对前台访客可见。
 *
 * @param visible 是否前台可见
 * @returns 可见性说明
 */
function resolveVisibilityLabel(visible: boolean) {
  return visible ? '前台可见' : '前台暂不可见'
}

/**
 * 判断主评论是否仅作为上下文展示。
 * 作用：在筛选命中回复时弱化父级主评论，帮助用户聚焦真正命中的评论。
 *
 * @param root 主评论
 * @returns 是否上下文评论
 */
function isContextRoot(root: AdminCommentThreadRootItem) {
  return hasCommentLevelFilter.value && !root.matchedByFilter && root.hasMatchingDescendant
}

/**
 * 判断回复是否仅作为上下文展示。
 * 作用：在根评论命中搜索时弱化未直接命中的回复，避免审核视图噪声过强。
 *
 * @param reply 回复项
 * @returns 是否上下文回复
 */
function isContextReply(reply: AdminCommentThreadReplyItem) {
  return hasCommentLevelFilter.value && !reply.matchedByFilter
}

watch(
  () => route.fullPath,
  async () => {
    hydrateFiltersFromRoute()
    await loadCommentGroups()
  }
)

hydrateFiltersFromRoute()
await loadCommentGroups()
</script>

<template>
  <div class="space-y-4">
    <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] px-5 py-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-base font-semibold text-slate-900 dark:text-slate-50">评论管理</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            按文章分组查看评论主楼与回复流，集中处理审核、驳回和删除。
          </p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <UBadge color="neutral" variant="soft" class="rounded-[10px] px-3 py-1.5">
            命中 {{ total }} 篇文章
          </UBadge>
          <UBadge color="neutral" variant="soft" class="rounded-[10px] px-3 py-1.5">
            当前页 {{ currentPageCommentCount }} 条评论
          </UBadge>
        </div>
      </div>
    </section>

    <AdminListFilterBar>
      <template #search>
        <div class="grid gap-3 xl:grid-cols-[minmax(0,1fr)_180px]">
          <div>
            <AdminInput
              v-model="searchKeyword"
              icon="i-lucide-search"
              placeholder="搜索评论内容、文章标题、文章 Slug、用户名或邮箱"
              @keydown.enter.prevent="handleSearch"
            />
          </div>

          <div>
            <AdminInput
              :model-value="searchPostId"
              icon="i-lucide-hash"
              placeholder="输入文章 ID"
              @update:model-value="handlePostIdInput"
              @keydown.enter.prevent="handleSearch"
            />
          </div>
        </div>
      </template>

      <template #filters>
        <div>
          <AdminSelect
            :model-value="activeStatus"
            :items="statusOptions"
            placeholder="评论状态"
            class="min-w-36"
            @update:model-value="handleStatusChange"
          />
        </div>
      </template>

      <template #actions>
        <AdminPrimaryButton
          label="查询评论"
          icon="i-lucide-search"
          :loading="isLoading"
          loading-label="查询中..."
          @click="handleSearch"
        />

        <AdminButton
          label="重置"
          tone="neutral"
          variant="ghost"
          :disabled="!hasActiveFilters || isLoading"
          @click="resetFilters"
        />

        <AdminButton
          label="刷新"
          icon="i-lucide-refresh-cw"
          tone="neutral"
          variant="outline"
          :loading="isLoading"
          loading-label="刷新中..."
          @click="loadCommentGroups"
        />
      </template>
    </AdminListFilterBar>

    <AdminTableCard title="文章评论审核视图" :total="total">
      <div v-if="isLoading" class="space-y-4">
        <div
          v-for="index in 3"
          :key="`comment-group-skeleton-${index}`"
          class="rounded-[18px] border border-slate-200/70 bg-white/72 p-4 dark:border-white/10 dark:bg-white/[0.04]"
        >
          <USkeleton class="h-5 w-48 rounded-[8px]" />
          <USkeleton class="mt-3 h-4 w-72 rounded-[8px]" />
          <USkeleton class="mt-5 h-24 rounded-[14px]" />
        </div>
      </div>

      <div v-else-if="groups.length === 0" class="rounded-[18px] border border-dashed border-slate-200/80 bg-white/65 px-6 py-16 text-center dark:border-white/10 dark:bg-white/[0.03]">
        <div class="mx-auto flex size-12 items-center justify-center rounded-full bg-slate-100 text-slate-500 dark:bg-white/[0.05] dark:text-slate-400">
          <UIcon name="i-lucide-message-square-off" class="size-5" />
        </div>
        <p class="mt-4 text-base font-semibold text-slate-900 dark:text-slate-50">{{ emptyTitle }}</p>
        <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
          可以尝试调整关键词、文章 ID 或状态筛选条件后重新查询。
        </p>
      </div>

      <div v-else class="space-y-4">
        <section
          v-for="group in groups"
          :key="`comment-group-${group.postId}`"
          class="overflow-hidden rounded-[18px] border border-slate-200/75 bg-white/72 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-white/[0.04]"
        >
          <div class="border-b border-slate-200/70 px-5 py-4 dark:border-white/10">
            <div class="flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between">
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <NuxtLink
                    :to="`/posts/${group.postSlug}`"
                    target="_blank"
                    class="text-base font-semibold text-slate-900 transition hover:text-sky-600 dark:text-slate-50 dark:hover:text-sky-300"
                  >
                    {{ group.postTitle }}
                  </NuxtLink>
                  <span class="rounded-full border border-slate-200 bg-slate-50 px-2.5 py-1 text-[11px] font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
                    /posts/{{ group.postSlug }}
                  </span>
                </div>

                <div class="mt-3 flex flex-wrap items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                  <span class="rounded-full border border-slate-200 bg-white px-2.5 py-1 dark:border-white/10 dark:bg-white/[0.03]">
                    共 {{ group.totalCommentCount }} 条评论
                  </span>
                  <span class="rounded-full border border-amber-200 bg-amber-50 px-2.5 py-1 text-amber-700 dark:border-amber-400/20 dark:bg-amber-400/10 dark:text-amber-200">
                    待审核 {{ group.pendingCommentCount }}
                  </span>
                  <span class="rounded-full border border-emerald-200 bg-emerald-50 px-2.5 py-1 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200">
                    已通过 {{ group.approvedCommentCount }}
                  </span>
                  <span class="rounded-full border border-rose-200 bg-rose-50 px-2.5 py-1 text-rose-700 dark:border-rose-400/20 dark:bg-rose-400/10 dark:text-rose-200">
                    已驳回 {{ group.rejectedCommentCount }}
                  </span>
                  <span v-if="group.latestCommentTime" class="rounded-full border border-slate-200 bg-white px-2.5 py-1 dark:border-white/10 dark:bg-white/[0.03]">
                    最近评论 {{ group.latestCommentTime }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-4 px-5 py-5">
            <div
              v-for="root in group.roots"
              :key="`comment-root-${root.id}`"
              class="rounded-[18px] border p-4 transition duration-200"
              :class="isContextRoot(root)
                ? 'border-slate-200/80 bg-slate-50/82 opacity-90 dark:border-white/10 dark:bg-white/[0.03]'
                : root.matchedByFilter
                  ? 'border-[var(--admin-primary-border)] bg-[var(--admin-primary-soft-surface)] shadow-[0_16px_32px_-28px_var(--admin-primary-shadow)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,transparent)] dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_10%,transparent)]'
                  : 'border-slate-200/80 bg-white/78 dark:border-white/10 dark:bg-white/[0.03]'"
            >
              <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ root.userName }}</p>
                    <span class="text-xs text-slate-400 dark:text-slate-500">{{ root.userEmail }}</span>
                    <UBadge :color="resolveStatusColor(root.status)" variant="soft">
                      {{ resolveStatusLabel(root.status) }}
                    </UBadge>
                    <span
                      class="rounded-full border px-2 py-0.5 text-[11px] font-medium"
                      :class="root.visibleOnSite
                        ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200'
                        : 'border-slate-200 bg-white text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400'"
                    >
                      {{ resolveVisibilityLabel(root.visibleOnSite) }}
                    </span>
                    <span
                      v-if="isContextRoot(root)"
                      class="rounded-full border border-slate-200 bg-white px-2 py-0.5 text-[11px] font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400"
                    >
                      上下文评论
                    </span>
                  </div>

                  <p class="mt-2 text-xs text-slate-400 dark:text-slate-500">
                    {{ root.createdTime }}
                    <span class="mx-1">·</span>
                    更新 {{ root.updatedTime }}
                    <span v-if="root.ip" class="mx-1">·</span>
                    <span v-if="root.ip">IP {{ root.ip }}</span>
                    <span class="mx-1">·</span>
                    ID #{{ root.id }}
                  </p>

                  <div class="mt-4 text-sm leading-7 text-slate-700 dark:text-slate-300">
                    <CommentRichContent :content="root.content" emoji-size="sm" />
                  </div>
                </div>

                <div class="flex shrink-0 flex-wrap items-start gap-2 xl:justify-end">
                  <AdminActionIconButton
                    icon="i-lucide-badge-check"
                    label="通过评论"
                    :disabled="actionCommentId === root.id || root.status === 'APPROVED'"
                    @click="changeCommentStatus(root, 'APPROVED')"
                  />
                  <AdminActionIconButton
                    icon="i-lucide-circle-off"
                    label="驳回评论"
                    :disabled="actionCommentId === root.id || root.status === 'REJECTED'"
                    @click="changeCommentStatus(root, 'REJECTED')"
                  />
                  <AdminActionIconButton
                    icon="i-lucide-trash-2"
                    label="删除评论"
                    tone="danger"
                    :disabled="actionCommentId === root.id"
                    @click="openDeleteModal(root)"
                  />
                </div>
              </div>

              <div v-if="root.replies.length > 0" class="mt-5 space-y-3 border-t border-slate-200/70 pt-4 dark:border-white/10">
                <div
                  v-for="reply in root.replies"
                  :key="`comment-reply-${reply.id}`"
                  class="rounded-[16px] border px-4 py-4"
                  :class="isContextReply(reply)
                    ? 'border-slate-200/75 bg-slate-50/78 opacity-90 dark:border-white/10 dark:bg-white/[0.03]'
                    : reply.matchedByFilter
                      ? 'border-sky-200/80 bg-sky-50/75 dark:border-sky-400/20 dark:bg-sky-400/10'
                      : 'border-slate-200/75 bg-white/72 dark:border-white/10 dark:bg-white/[0.025]'"
                >
                  <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
                    <div class="min-w-0 flex-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <span class="rounded-full border border-slate-200 bg-white px-2 py-0.5 text-[11px] font-semibold text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
                          回复
                          <template v-if="reply.replyToUserName">
                            @{{ reply.replyToUserName }}
                          </template>
                        </span>
                        <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">{{ reply.userName }}</p>
                        <span class="text-xs text-slate-400 dark:text-slate-500">{{ reply.userEmail }}</span>
                        <UBadge :color="resolveStatusColor(reply.status)" variant="soft">
                          {{ resolveStatusLabel(reply.status) }}
                        </UBadge>
                        <span
                          class="rounded-full border px-2 py-0.5 text-[11px] font-medium"
                          :class="reply.visibleOnSite
                            ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-200'
                            : 'border-slate-200 bg-white text-slate-500 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400'"
                        >
                          {{ resolveVisibilityLabel(reply.visibleOnSite) }}
                        </span>
                      </div>

                      <p class="mt-2 text-xs text-slate-400 dark:text-slate-500">
                        {{ reply.createdTime }}
                        <span class="mx-1">·</span>
                        更新 {{ reply.updatedTime }}
                        <span v-if="reply.ip" class="mx-1">·</span>
                        <span v-if="reply.ip">IP {{ reply.ip }}</span>
                        <span class="mx-1">·</span>
                        ID #{{ reply.id }}
                      </p>

                      <div class="mt-4 text-sm leading-7 text-slate-700 dark:text-slate-300">
                        <CommentRichContent :content="reply.content" emoji-size="sm" />
                      </div>
                    </div>

                    <div class="flex shrink-0 flex-wrap items-start gap-2 xl:justify-end">
                      <AdminActionIconButton
                        icon="i-lucide-badge-check"
                        label="通过评论"
                        :disabled="actionCommentId === reply.id || reply.status === 'APPROVED'"
                        @click="changeCommentStatus(reply, 'APPROVED')"
                      />
                      <AdminActionIconButton
                        icon="i-lucide-circle-off"
                        label="驳回评论"
                        :disabled="actionCommentId === reply.id || reply.status === 'REJECTED'"
                        @click="changeCommentStatus(reply, 'REJECTED')"
                      />
                      <AdminActionIconButton
                        icon="i-lucide-trash-2"
                        label="删除评论"
                        tone="danger"
                        :disabled="actionCommentId === reply.id"
                        @click="openDeleteModal(reply)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

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

    <AdminConfirmModal
      v-model:open="isDeleteModalOpen"
      title="删除评论"
      :description="deleteDescription"
      confirm-label="确认删除"
      :loading="actionCommentId === deletingComment?.id"
      @confirm="confirmDelete"
    />
  </div>
</template>
