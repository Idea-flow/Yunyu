<script setup lang="ts">
import type { AdminCommentItem, AdminCommentStatus } from '../../types/comment'
import CommentRichContent from '../../components/content/CommentRichContent.vue'

/**
 * 后台评论管理页。
 * 作用：为站长提供评论列表查询、审核通过、驳回和删除的统一管理入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

type CommentStatusFilter = 'ALL' | AdminCommentStatus

const toast = useToast()
const adminComments = useAdminComments()

const isLoading = ref(false)
const actionCommentId = ref<number | null>(null)
const deletingComment = ref<AdminCommentItem | null>(null)
const isDeleteModalOpen = ref(false)
const searchKeyword = ref('')
const searchPostId = ref('')
const activeStatus = ref<CommentStatusFilter>('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const comments = ref<AdminCommentItem[]>([])

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
 * 拉取评论列表。
 * 作用：根据当前关键词、状态和分页参数刷新评论管理页数据。
 */
async function loadComments() {
  isLoading.value = true

  try {
    const response = await adminComments.listComments({
      keyword: searchKeyword.value || undefined,
      postId: resolveSearchPostId(),
      status: activeStatus.value === 'ALL' ? undefined : activeStatus.value,
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })
    comments.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: '加载评论失败',
      description: error?.message || '暂时无法获取评论列表，请稍后重试。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 执行评论状态更新。
 * 作用：供站长快速完成评论审核通过或驳回，并在成功后刷新当前列表。
 *
 * @param comment 评论条目
 * @param status 目标状态
 */
async function changeCommentStatus(comment: AdminCommentItem, status: AdminCommentStatus) {
  actionCommentId.value = comment.id

  try {
    await adminComments.updateCommentStatus(comment.id, status)
    toast.add({
      title: status === 'APPROVED' ? '评论已通过' : status === 'REJECTED' ? '评论已驳回' : '评论已退回待审',
      color: 'success'
    })
    await loadComments()
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
function openDeleteModal(comment: AdminCommentItem) {
  deletingComment.value = comment
  isDeleteModalOpen.value = true
}

/**
 * 确认删除评论。
 * 作用：执行评论软删除，并刷新列表以同步最新审核队列状态。
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
    await loadComments()
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
  await loadComments()
}

/**
 * 重置评论筛选条件。
 * 作用：一键清空关键词、文章 ID 与状态筛选，并回到第一页重新加载列表。
 */
async function resetFilters() {
  searchKeyword.value = ''
  searchPostId.value = ''
  activeStatus.value = 'ALL'
  currentPage.value = 1
  await loadComments()
}

/**
 * 处理筛选状态切换。
 * 作用：切换评论审核状态时同步刷新列表。
 *
 * @param value 当前选择值
 */
async function handleStatusChange(value: string | number | null) {
  activeStatus.value = (value as CommentStatusFilter) || 'ALL'
  currentPage.value = 1
  await loadComments()
}

/**
 * 处理评论分页切换。
 * 作用：在页码改变后刷新评论列表。
 *
 * @param page 新页码
 */
async function handlePageChange(page: number) {
  currentPage.value = page
  await loadComments()
}

/**
 * 处理每页条数切换。
 * 作用：修改每页展示容量后回到第一页重新请求列表。
 *
 * @param value 新条数
 */
async function handlePageSizeChange(value: number) {
  pageSize.value = value
  currentPage.value = 1
  await loadComments()
}

/**
 * 解析评论状态标签颜色。
 * 作用：让待审、通过、驳回状态在后台列表中具备稳定的视觉区分。
 *
 * @param status 评论状态
 * @returns Nuxt UI 颜色标识
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
 * 生成删除确认说明。
 * 作用：明确提示删除评论时会连带隐藏该评论下的回复分支。
 */
const deleteDescription = computed(() => {
  if (!deletingComment.value) {
    return '删除后将无法恢复。'
  }

  return `确认删除「${deletingComment.value.userName}」的这条评论吗？若该评论下存在回复分支，也会一并从前台隐藏。`
})

await loadComments()
</script>

<template>
  <div class="space-y-4">
    <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] px-5 py-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-base font-semibold text-slate-900 dark:text-slate-50">评论管理</h1>
          <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
            集中处理前台评论审核、异常内容清理和互动质量维护。
          </p>
        </div>

        <UBadge color="neutral" variant="soft" class="rounded-[10px] px-3 py-1.5">
          当前共 {{ total }} 条评论记录
        </UBadge>
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

        <button
          type="button"
          class="inline-flex min-h-9 min-w-20 items-center justify-center rounded-[10px] border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 transition hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900 disabled:cursor-not-allowed disabled:opacity-55 dark:border-white/10 dark:bg-slate-950/60 dark:text-slate-300 dark:hover:border-white/15 dark:hover:bg-slate-900/80 dark:hover:text-slate-100"
          :disabled="!hasActiveFilters || isLoading"
          @click="resetFilters"
        >
          重置
        </button>

        <AdminPrimaryButton
          label="刷新列表"
          icon="i-lucide-refresh-cw"
          :loading="isLoading"
          loading-label="刷新中..."
          @click="loadComments"
        />
      </template>
    </AdminListFilterBar>

    <AdminTableCard title="评论列表" :total="total">
      <AdminDataTable
        :is-loading="isLoading"
        :has-data="comments.length > 0"
        min-width="1440px"
        header-class="grid-cols-[110px_220px_180px_minmax(320px,1fr)_120px_150px_160px_170px]"
        empty-title="当前筛选条件下暂无评论"
        empty-icon="i-lucide-message-square-off"
      >
        <template #header>
          <span>评论 ID</span>
          <span>文章</span>
          <span>评论作者</span>
          <span>评论内容</span>
          <span>状态</span>
          <span>回复信息</span>
          <span>创建</span>
          <span>操作</span>
        </template>

        <div
          v-for="comment in comments"
          :key="comment.id"
          class="grid grid-cols-[110px_220px_180px_minmax(320px,1fr)_120px_150px_160px_170px] gap-4 px-4 py-4 text-sm text-slate-600 dark:text-slate-300"
        >
          <div class="min-w-0">
            <p class="font-semibold text-slate-900 dark:text-slate-50">#{{ comment.id }}</p>
            <p class="mt-1 text-xs text-slate-400 dark:text-slate-500">
              {{ comment.replyCommentId ? '回复评论' : '主评论' }}
            </p>
          </div>

          <div class="min-w-0">
            <NuxtLink
              :to="`/posts/${comment.postSlug}`"
              target="_blank"
              class="line-clamp-2 font-medium text-slate-900 transition hover:text-sky-600 dark:text-slate-50 dark:hover:text-sky-300"
            >
              {{ comment.postTitle }}
            </NuxtLink>
            <p class="mt-1 text-xs text-slate-400 dark:text-slate-500">ID {{ comment.postId }}</p>
          </div>

          <div class="min-w-0">
            <p class="truncate font-medium text-slate-900 dark:text-slate-50">{{ comment.userName }}</p>
            <p class="mt-1 truncate text-xs text-slate-400 dark:text-slate-500">{{ comment.userEmail }}</p>
          </div>

          <div class="min-w-0">
            <CommentRichContent
              :content="comment.content"
              emoji-size="sm"
              class="leading-7 text-slate-600 dark:text-slate-300"
            />
            <p v-if="comment.ip" class="mt-2 text-xs text-slate-400 dark:text-slate-500">IP：{{ comment.ip }}</p>
          </div>

          <div class="flex items-start">
            <UBadge :color="resolveStatusColor(comment.status)" variant="soft">
              {{ comment.status === 'APPROVED' ? '已通过' : comment.status === 'REJECTED' ? '已驳回' : '待审核' }}
            </UBadge>
          </div>

          <div class="min-w-0 text-xs leading-6 text-slate-500 dark:text-slate-400">
            <p v-if="comment.replyCommentId">回复 ID：{{ comment.replyCommentId }}</p>
            <p v-if="comment.replyToUserName">回复对象：{{ comment.replyToUserName }}</p>
            <p v-if="comment.rootId">楼层根 ID：{{ comment.rootId }}</p>
          </div>

          <div class="text-xs leading-6 text-slate-500 dark:text-slate-400">
            <p>{{ comment.createdTime }}</p>
            <p class="mt-1">更新 {{ comment.updatedTime }}</p>
          </div>

          <div class="flex flex-wrap items-start gap-2">
            <AdminActionIconButton
              icon="i-lucide-badge-check"
              label="通过评论"
              :disabled="actionCommentId === comment.id || comment.status === 'APPROVED'"
              @click="changeCommentStatus(comment, 'APPROVED')"
            />
            <AdminActionIconButton
              icon="i-lucide-circle-off"
              label="驳回评论"
              :disabled="actionCommentId === comment.id || comment.status === 'REJECTED'"
              @click="changeCommentStatus(comment, 'REJECTED')"
            />
            <AdminActionIconButton
              icon="i-lucide-trash-2"
              label="删除评论"
              tone="danger"
              :disabled="actionCommentId === comment.id"
              @click="openDeleteModal(comment)"
            />
          </div>
        </div>
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
