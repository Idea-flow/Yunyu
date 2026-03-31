<script setup lang="ts">
import type { AdminPostItem } from '../../../types/post'

/**
 * 后台文章列表页。
 * 作用：为站长提供文章筛选、跳转新增页、跳转编辑页与删除管理的统一列表入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminPosts = useAdminPosts()

type PostStatusFilter = 'ALL' | 'DRAFT' | 'PUBLISHED' | 'OFFLINE'

const isLoading = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeStatus = ref<PostStatusFilter>('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const isDeleteModalOpen = ref(false)
const deletingPost = ref<AdminPostItem | null>(null)

const posts = ref<AdminPostItem[]>([])

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下线', value: 'OFFLINE' }
] as const

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
 * 解析文章状态颜色。
 *
 * @param status 状态值
 * @returns Nuxt UI 颜色名
 */
function resolveStatusColor(status: AdminPostItem['status']) {
  switch (status) {
    case 'PUBLISHED':
      return 'primary' as const
    case 'OFFLINE':
      return 'warning' as const
    default:
      return 'neutral' as const
  }
}

await loadPosts()
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="文章管理" />
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
          <div class="flex flex-col gap-4 xl:flex-row xl:items-center">
            <AdminInput
              v-model="searchKeyword"
              icon="i-lucide-search"
              placeholder="搜索标题或 Slug"
            />

            <AdminSelect
              v-model="activeStatus"
              :items="statusOptions"
              class="min-w-40"
              placeholder="状态"
            />

            <AdminPrimaryButton label="搜索" icon="i-lucide-search" @click="handleSearch" />
          </div>
        </UCard>

        <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">操作</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">文章操作区</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">新增和修改都改为独立页面，适合承载长内容编辑流程。</p>
            </div>

            <AdminPrimaryButton label="增加" icon="i-lucide-file-plus-2" @click="goToCreatePage" />
          </div>
        </UCard>

        <AdminTableCard
          title="文章列表"
          description="列表最右侧提供跳转编辑页和删除操作"
          :total="total"
        >
          <div v-if="isLoading" class="space-y-3">
            <USkeleton class="h-[4.5rem] rounded-2xl" />
            <USkeleton class="h-[4.5rem] rounded-2xl" />
            <USkeleton class="h-[4.5rem] rounded-2xl" />
          </div>

          <div v-else class="overflow-hidden rounded-[1.55rem] border border-slate-200/80 bg-white/85 dark:border-slate-800 dark:bg-slate-950/60">
            <div class="hidden grid-cols-[minmax(0,1.7fr)_0.8fr_0.8fr_0.8fr_0.8fr] gap-4 border-b border-slate-200/80 bg-slate-50/85 px-5 py-4 text-xs font-semibold tracking-[0.14em] text-slate-400 uppercase dark:border-slate-800 dark:bg-slate-900/80 dark:text-slate-500 lg:grid">
              <p>文章</p>
              <p>状态</p>
              <p>专题</p>
              <p>最近更新</p>
              <p class="text-right">操作</p>
            </div>

            <div class="divide-y divide-default/70">
              <article
                v-for="post in posts"
                :key="post.id"
                class="grid gap-4 px-5 py-5 transition duration-200 hover:bg-sky-50/80 dark:hover:bg-sky-400/8 lg:grid-cols-[minmax(0,1.7fr)_0.8fr_0.8fr_0.8fr_0.8fr] lg:items-center"
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

                <div>
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

                <div class="flex flex-wrap items-center gap-2 lg:col-span-5">
                  <UBadge :color="post.coverReady ? 'success' : 'warning'" variant="subtle">
                    {{ post.coverReady ? '封面已准备' : '缺少封面' }}
                  </UBadge>
                  <UBadge :color="post.summaryReady ? 'success' : 'warning'" variant="subtle">
                    {{ post.summaryReady ? '摘要完整' : '缺少摘要' }}
                  </UBadge>
                </div>
              </article>

              <div v-if="!posts.length" class="flex flex-col items-center justify-center gap-3 px-6 py-12 text-center">
                <div class="inline-flex size-14 items-center justify-center rounded-[1.2rem] bg-sky-50 text-sky-600 dark:bg-sky-400/12 dark:text-sky-300">
                  <UIcon name="i-lucide-file-search" class="size-5" />
                </div>
                <p class="text-base font-medium text-slate-900 dark:text-slate-50">没有找到匹配的文章</p>
                <p class="max-w-md text-sm text-slate-500 dark:text-slate-400">可以尝试调整关键词或筛选条件。</p>
              </div>
            </div>
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
      </div>

      <AdminConfirmModal
        v-model:open="isDeleteModalOpen"
        title="确认删除文章"
        :description="deletingPost ? `删除后将无法在后台列表中继续管理“${deletingPost.title}”。` : '请确认是否继续删除当前文章。'"
        confirm-label="确认删除"
        :loading="isDeleteSubmitting"
        @confirm="confirmDelete"
      />
    </template>
  </UDashboardPanel>
</template>
