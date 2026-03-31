<script setup lang="ts">
import type { AdminPostForm, AdminPostItem } from '../../types/post'

/**
 * 后台文章管理页。
 * 作用：为站长提供文章查询、创建、编辑、删除与发布状态管理的统一后台工作页面。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminPosts = useAdminPosts()

type PostStatusFilter = 'ALL' | 'DRAFT' | 'PUBLISHED' | 'OFFLINE'

const isLoading = ref(false)
const isSubmitting = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeStatus = ref<PostStatusFilter>('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const editingPostId = ref<number | null>(null)
const isFormModalOpen = ref(false)
const isDeleteModalOpen = ref(false)
const deletingPost = ref<AdminPostItem | null>(null)

const posts = ref<AdminPostItem[]>([])

const formState = reactive<AdminPostForm>({
  title: '',
  slug: '',
  summary: '',
  coverUrl: '',
  status: 'DRAFT',
  contentMarkdown: ''
})

const statusOptions = [
  { label: '全部状态', value: 'ALL' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下线', value: 'OFFLINE' }
] as const

/**
 * 判断当前是否处于编辑状态。
 * 用于切换弹窗标题和提交按钮文案。
 */
const isEditing = computed(() => editingPostId.value !== null)

/**
 * 加载文章列表。
 * 会根据当前搜索条件和分页参数请求后台文章接口。
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
 * 重置文章表单。
 * 在新增或保存完成后恢复为默认状态。
 */
function resetForm() {
  editingPostId.value = null
  formState.title = ''
  formState.slug = ''
  formState.summary = ''
  formState.coverUrl = ''
  formState.status = 'DRAFT'
  formState.contentMarkdown = ''
}

/**
 * 打开新建文章弹窗。
 * 会先重置表单，再展示新增窗口。
 */
function openCreateModal() {
  resetForm()
  isFormModalOpen.value = true
}

/**
 * 打开编辑文章弹窗。
 * 为了避免列表字段不完整，先请求文章详情后再填充表单。
 *
 * @param post 文章数据
 */
async function startEdit(post: AdminPostItem) {
  try {
    const detail = await adminPosts.getPost(post.id)
    editingPostId.value = detail.id
    formState.title = detail.title
    formState.slug = detail.slug
    formState.summary = detail.summary || ''
    formState.coverUrl = detail.coverUrl || ''
    formState.status = detail.status
    formState.contentMarkdown = detail.contentMarkdown || ''
    isFormModalOpen.value = true
  } catch (error: any) {
    toast.add({
      title: '加载文章详情失败',
      description: error?.message || '暂时无法进入编辑状态。',
      color: 'error'
    })
  }
}

/**
 * 校验文章表单。
 * 在提交前给出最基本的表单提示，避免无效请求进入后端。
 */
function validateForm() {
  if (!formState.title.trim()) {
    toast.add({ title: '请输入文章标题', color: 'warning' })
    return false
  }

  if (!formState.slug.trim()) {
    toast.add({ title: '请输入文章 Slug', color: 'warning' })
    return false
  }

  return true
}

/**
 * 提交文章表单。
 * 会根据当前状态自动区分创建和更新动作。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const payload: AdminPostForm = {
      title: formState.title.trim(),
      slug: formState.slug.trim(),
      summary: formState.summary.trim(),
      coverUrl: formState.coverUrl.trim(),
      status: formState.status,
      contentMarkdown: formState.contentMarkdown
    }

    if (editingPostId.value) {
      await adminPosts.updatePost(editingPostId.value, payload)
      toast.add({ title: '文章已更新', color: 'success' })
    } else {
      await adminPosts.createPost(payload)
      toast.add({ title: '文章已创建', color: 'success' })
    }

    resetForm()
    isFormModalOpen.value = false
    await loadPosts()
  } catch (error: any) {
    toast.add({
      title: isEditing.value ? '更新失败' : '创建失败',
      description: error?.message || '文章保存未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}

/**
 * 打开删除确认弹窗。
 *
 * @param post 文章数据
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

    if (editingPostId.value && posts.value.length === 1 && currentPage.value > 1) {
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
  currentPage.value = page
  await loadPosts()
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
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">新增文章内容并管理当前的发布状态</p>
            </div>

            <AdminPrimaryButton label="增加" icon="i-lucide-file-plus-2" @click="openCreateModal" />
          </div>
        </UCard>

        <AdminTableCard
          title="文章列表"
          description="列表最右侧提供编辑和删除操作"
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
                    @click="startEdit(post)"
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
            <div class="flex items-center justify-between gap-4 pt-1">
              <p class="text-sm text-slate-500 dark:text-slate-400">
                第 {{ currentPage }} 页，共 {{ Math.max(1, Math.ceil(total / pageSize)) }} 页
              </p>
              <UPagination
                :model-value="currentPage"
                :total="total"
                :items-per-page="pageSize"
                @update:model-value="handlePageChange"
              />
            </div>
          </template>
        </AdminTableCard>
      </div>

      <AdminFormModal
        v-model:open="isFormModalOpen"
        eyebrow="内容编辑"
        :title="isEditing ? '修改文章' : '增加文章'"
        :description="isEditing ? '修改文章基础信息、状态和正文。' : '创建新的文章内容。'"
        icon="i-lucide-file-pen-line"
        width="editor"
      >
        <template #body>
          <form class="space-y-5" @submit.prevent="handleSubmit">
            <UFormField name="title" label="标题">
              <AdminInput
                v-model="formState.title"
                placeholder="请输入文章标题"
              />
            </UFormField>

            <UFormField name="slug" label="Slug">
              <AdminInput
                v-model="formState.slug"
                placeholder="请输入唯一标识"
              />
            </UFormField>

            <UFormField name="summary" label="摘要">
              <AdminTextarea
                v-model="formState.summary"
                :rows="3"
                autoresize
                placeholder="可选"
              />
            </UFormField>

            <UFormField name="coverUrl" label="封面地址">
              <AdminInput
                v-model="formState.coverUrl"
                placeholder="可选"
              />
            </UFormField>

            <UFormField name="status" label="状态">
              <AdminSelect
                v-model="formState.status"
                :items="statusOptions.slice(1)"
              />
            </UFormField>

            <UFormField name="contentMarkdown" label="正文">
              <AdminTextarea
                v-model="formState.contentMarkdown"
                :rows="10"
                autoresize
                placeholder="请输入 Markdown 正文"
              />
            </UFormField>
          </form>
        </template>

        <template #footer>
          <div class="flex w-full justify-end gap-3">
            <UButton
              label="取消"
              color="neutral"
              variant="ghost"
              @click="isFormModalOpen = false"
            />
            <AdminPrimaryButton
              :label="isEditing ? '保存修改' : '增加文章'"
              loading-label="保存中..."
              :loading="isSubmitting"
              :icon="isEditing ? 'i-lucide-save' : 'i-lucide-file-plus-2'"
              @click="handleSubmit"
            />
          </div>
        </template>
      </AdminFormModal>

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
