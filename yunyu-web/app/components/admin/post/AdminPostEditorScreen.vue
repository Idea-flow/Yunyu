<script setup lang="ts">
import type { AdminPostForm } from '../../../types/post'

/**
 * 后台文章编辑页组件。
 * 作用：统一承载文章新增与编辑场景的表单、提交逻辑和页面布局，
 * 让创建与修改文章共用一套稳定的后台编辑工作流。
 */
const props = withDefaults(defineProps<{
  mode: 'create' | 'edit'
  postId?: number | null
}>(), {
  postId: null
})

const toast = useToast()
const adminPosts = useAdminPosts()

const isSubmitting = ref(false)
const isLoadingDetail = ref(false)
const contentViewMode = ref<'edit' | 'preview'>('edit')

const topicTags = ref(['内容策划待接入', '专题模块待接入'])
const keywordTags = ref(['SEO关键词待接入', '标签模块待接入'])

const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下线', value: 'OFFLINE' }
] as const

const formState = reactive<AdminPostForm>({
  title: '',
  slug: '',
  summary: '',
  coverUrl: '',
  status: 'DRAFT',
  seoTitle: '',
  seoDescription: '',
  contentMarkdown: ''
})

/**
 * 判断当前是否为编辑模式。
 * 用于切换页面标题、提示文案与提交行为。
 */
const isEditing = computed(() => props.mode === 'edit')

/**
 * 计算页面标题。
 * 让新增和编辑场景在视觉层面有明确区分。
 */
const pageTitle = computed(() => isEditing.value ? '修改文章' : '新增文章')

/**
 * 计算页面说明文案。
 * 帮助用户快速理解当前页面的操作目标。
 */
const pageDescription = computed(() =>
  isEditing.value
    ? '调整文章标题、摘要、发布状态与正文内容，完成后统一保存。'
    : '从这里创建一篇新的文章内容，先整理基础信息，再填写正文。'
)

/**
 * 计算主按钮文案。
 * 根据当前模式动态反馈提交动作。
 */
const submitLabel = computed(() => isEditing.value ? '保存修改' : '创建文章')

/**
 * 计算状态提示文案。
 * 用于在页面右侧摘要卡片展示当前内容的编辑状态。
 */
const statusHint = computed(() => {
  if (formState.status === 'PUBLISHED') {
    return '当前将以已发布状态保存'
  }

  if (formState.status === 'OFFLINE') {
    return '当前将以下线状态保存'
  }

  return '当前会保存为草稿'
})

/**
 * 计算正文字符数。
 * 用于在编辑页提供即时内容量反馈。
 */
const contentLength = computed(() => formState.contentMarkdown.trim().length)
const seoTitleLength = computed(() => formState.seoTitle.trim().length)
const seoDescriptionLength = computed(() => formState.seoDescription.trim().length)
const estimatedReadingMinutes = computed(() => Math.max(1, Math.ceil(contentLength.value / 500)))

/**
 * 计算 Markdown 预览内容。
 * 当前阶段先提供轻量级预览，保留段落和换行结构，便于作者快速检查内容节奏。
 */
const markdownPreview = computed(() => {
  const source = formState.contentMarkdown.trim()

  if (!source) {
    return ''
  }

  return source
    .split(/\n{2,}/)
    .map(section => section.trim())
    .filter(Boolean)
    .join('\n\n')
})

/**
 * 校验文章表单。
 * 在提交前给出最基础的字段反馈，避免无效请求进入后端。
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

  if (!formState.contentMarkdown.trim()) {
    toast.add({ title: '请输入文章正文', color: 'warning' })
    return false
  }

  return true
}

/**
 * 以指定状态提交文章。
 * 用于支持保存草稿、发布文章和下线保存等明确动作。
 *
 * @param status 目标状态
 */
async function handleSubmitWithStatus(status: AdminPostForm['status']) {
  formState.status = status
  await handleSubmit()
}

/**
 * 回填文章详情。
 * 在编辑模式下根据文章详情初始化表单内容。
 */
async function loadPostDetail() {
  if (!isEditing.value) {
    return
  }

  if (!props.postId) {
    toast.add({
      title: '文章标识无效',
      description: '当前编辑地址缺少有效的文章 ID。',
      color: 'error'
    })
    await navigateTo('/admin/posts')
    return
  }

  isLoadingDetail.value = true

  try {
    const detail = await adminPosts.getPost(props.postId)
    formState.title = detail.title
    formState.slug = detail.slug
    formState.summary = detail.summary || ''
    formState.coverUrl = detail.coverUrl || ''
    formState.status = detail.status
    formState.seoTitle = detail.seoTitle || ''
    formState.seoDescription = detail.seoDescription || ''
    formState.contentMarkdown = detail.contentMarkdown || ''
  } catch (error: any) {
    toast.add({
      title: '加载文章详情失败',
      description: error?.message || '当前文章暂时无法进入编辑状态。',
      color: 'error'
    })
    await navigateTo('/admin/posts')
  } finally {
    isLoadingDetail.value = false
  }
}

/**
 * 提交当前文章表单。
 * 会根据页面模式自动选择创建或更新接口。
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
      seoTitle: formState.seoTitle.trim(),
      seoDescription: formState.seoDescription.trim(),
      contentMarkdown: formState.contentMarkdown
    }

    if (isEditing.value && props.postId) {
      await adminPosts.updatePost(props.postId, payload)
      toast.add({ title: '文章已更新', color: 'success' })
    } else {
      await adminPosts.createPost(payload)
      toast.add({ title: '文章已创建', color: 'success' })
    }

    await navigateTo('/admin/posts')
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
 * 返回文章列表页。
 * 在用户放弃当前编辑任务时回到列表视图。
 */
async function goBackToList() {
  await navigateTo('/admin/posts')
}

await loadPostDetail()
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar :title="pageTitle">
        <template #leading>
          <UButton
            icon="i-lucide-arrow-left"
            color="neutral"
            variant="ghost"
            class="rounded-2xl"
            @click="goBackToList"
          />
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <div class="grid gap-6 xl:grid-cols-[minmax(0,1.4fr)_22rem]">
          <section class="space-y-6">
            <UCard class="overflow-hidden rounded-[34px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
              <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
                <div class="space-y-3">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Editorial Workspace</p>
                  <div class="space-y-2">
                    <h1 class="text-2xl font-semibold tracking-tight text-slate-900 dark:text-slate-50 lg:text-[2rem]">
                      {{ pageTitle }}
                    </h1>
                    <p class="max-w-2xl text-sm leading-7 text-slate-600 dark:text-slate-300">
                      {{ pageDescription }}
                    </p>
                  </div>
                </div>

                <div class="flex flex-wrap items-center gap-3">
                  <UButton
                    label="返回列表"
                    color="neutral"
                    variant="outline"
                    class="cursor-pointer rounded-2xl"
                    @click="goBackToList"
                  />
                  <UButton
                    label="保存草稿"
                    color="neutral"
                    variant="outline"
                    class="cursor-pointer rounded-2xl"
                    :loading="isSubmitting && formState.status === 'DRAFT'"
                    @click="handleSubmitWithStatus('DRAFT')"
                  />
                  <UButton
                    v-if="isEditing"
                    label="下线保存"
                    color="warning"
                    variant="soft"
                    class="cursor-pointer rounded-2xl"
                    :loading="isSubmitting && formState.status === 'OFFLINE'"
                    @click="handleSubmitWithStatus('OFFLINE')"
                  />
                  <AdminPrimaryButton
                    :label="formState.status === 'PUBLISHED' ? '更新已发布内容' : '发布文章'"
                    loading-label="保存中..."
                    :loading="isSubmitting"
                    icon="i-lucide-send"
                    @click="handleSubmitWithStatus('PUBLISHED')"
                  />
                </div>
              </div>
            </UCard>

            <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">基础信息</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">文章元信息</p>
                </div>
              </template>

              <div v-if="isLoadingDetail" class="space-y-4">
                <USkeleton class="h-16 rounded-2xl" />
                <USkeleton class="h-16 rounded-2xl" />
                <USkeleton class="h-28 rounded-2xl" />
              </div>

              <form v-else class="space-y-5" @submit.prevent="handleSubmit">
                <div class="grid gap-5 lg:grid-cols-2">
                  <UFormField name="title" label="标题" required>
                    <AdminInput
                      v-model="formState.title"
                      placeholder="请输入文章标题"
                    />
                  </UFormField>

                  <UFormField name="slug" label="Slug" required>
                    <AdminInput
                      v-model="formState.slug"
                      placeholder="请输入唯一标识"
                    />
                  </UFormField>
                </div>

                <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_13rem]">
                  <UFormField name="summary" label="摘要">
                    <AdminTextarea
                      v-model="formState.summary"
                      :rows="4"
                      autoresize
                      placeholder="用于列表与详情摘要展示，可选"
                    />
                  </UFormField>

                  <UFormField name="status" label="状态">
                    <AdminSelect
                      v-model="formState.status"
                      :items="statusOptions"
                    />
                  </UFormField>
                </div>

                <UFormField name="coverUrl" label="封面地址">
                  <AdminInput
                    v-model="formState.coverUrl"
                    placeholder="请输入封面图片 URL，可选"
                  />
                </UFormField>

                <div class="grid gap-5 lg:grid-cols-2">
                  <UFormField name="seoTitle" label="SEO 标题">
                    <AdminInput
                      v-model="formState.seoTitle"
                      placeholder="用于搜索结果标题展示，可选"
                    />
                  </UFormField>

                  <UFormField name="seoDescription" label="SEO 描述">
                    <AdminTextarea
                      v-model="formState.seoDescription"
                      :rows="4"
                      autoresize
                      placeholder="用于搜索摘要展示，可选"
                    />
                  </UFormField>
                </div>
              </form>
            </UCard>

            <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Structure</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">内容编排区</p>
                </div>
              </template>

              <div class="grid gap-5 lg:grid-cols-3">
                <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">分类</p>
                    <UBadge color="warning" variant="soft">待接口</UBadge>
                  </div>
                  <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                    分类模块尚未接入后台接口，当前工作台先预留结构区，后续可直接挂接真实分类选择器。
                  </p>
                </div>

                <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">标签</p>
                    <UBadge color="warning" variant="soft">待接口</UBadge>
                  </div>
                  <div class="mt-3 flex flex-wrap gap-2">
                    <UBadge
                      v-for="keyword in keywordTags"
                      :key="keyword"
                      color="neutral"
                      variant="subtle"
                    >
                      {{ keyword }}
                    </UBadge>
                  </div>
                </div>

                <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <div class="flex items-center justify-between gap-3">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">专题</p>
                    <UBadge color="warning" variant="soft">待接口</UBadge>
                  </div>
                  <div class="mt-3 flex flex-wrap gap-2">
                    <UBadge
                      v-for="topic in topicTags"
                      :key="topic"
                      color="neutral"
                      variant="subtle"
                    >
                      {{ topic }}
                    </UBadge>
                  </div>
                </div>
              </div>
            </UCard>

            <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
              <template #header>
                <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
                  <div class="space-y-1">
                    <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Content</p>
                    <p class="text-base font-semibold text-slate-900 dark:text-slate-50">正文编辑区</p>
                  </div>

                  <div class="inline-flex rounded-2xl border border-slate-200 bg-slate-50 p-1 dark:border-slate-700 dark:bg-slate-900">
                    <button
                      type="button"
                      class="min-h-11 cursor-pointer rounded-xl px-4 text-sm font-medium transition duration-200"
                      :class="contentViewMode === 'edit'
                        ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-slate-50'
                        : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
                      @click="contentViewMode = 'edit'"
                    >
                      编辑
                    </button>
                    <button
                      type="button"
                      class="min-h-11 cursor-pointer rounded-xl px-4 text-sm font-medium transition duration-200"
                      :class="contentViewMode === 'preview'
                        ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-800 dark:text-slate-50'
                        : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-50'"
                      @click="contentViewMode = 'preview'"
                    >
                      预览
                    </button>
                  </div>
                </div>
              </template>

              <div v-if="isLoadingDetail" class="space-y-4">
                <USkeleton class="h-72 rounded-[28px]" />
              </div>

              <UFormField v-else-if="contentViewMode === 'edit'" name="contentMarkdown" label="Markdown 正文" required>
                <AdminTextarea
                  v-model="formState.contentMarkdown"
                  :rows="18"
                  autoresize
                  placeholder="请输入 Markdown 正文内容"
                />
              </UFormField>

              <div
                v-else
                class="min-h-72 rounded-[28px] border border-slate-200/80 bg-slate-50/80 p-6 dark:border-slate-700 dark:bg-slate-900/70"
              >
                <div v-if="markdownPreview" class="space-y-4 whitespace-pre-wrap text-sm leading-8 text-slate-700 dark:text-slate-200">
                  {{ markdownPreview }}
                </div>
                <div v-else class="flex h-full min-h-60 items-center justify-center text-sm text-slate-400 dark:text-slate-500">
                  这里会显示正文预览，先在左侧输入 Markdown 内容。
                </div>
              </div>
            </UCard>
          </section>

          <aside class="space-y-6">
            <UCard class="rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Overview</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">编辑摘要</p>
                </div>
              </template>

              <div class="space-y-4">
                <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">当前状态</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ statusHint }}</p>
                </div>

                <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                  <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">标题长度</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ formState.title.trim().length }}</p>
                  </div>

                  <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">正文字符数</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ contentLength }}</p>
                  </div>
                </div>

                <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                  <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">SEO 标题长度</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ seoTitleLength }}</p>
                  </div>

                  <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">预估阅读</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ estimatedReadingMinutes }} 分钟</p>
                  </div>
                </div>

                <div
                  v-if="formState.coverUrl.trim()"
                  class="overflow-hidden rounded-[24px] border border-slate-200/80 bg-slate-50/80 dark:border-slate-700 dark:bg-slate-900/70"
                >
                  <div class="flex items-center justify-between px-4 pt-4">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">封面预览</p>
                    <UBadge color="success" variant="soft">已填写</UBadge>
                  </div>
                  <img
                    :src="formState.coverUrl"
                    alt="文章封面预览"
                    class="mt-4 h-44 w-full object-cover"
                  >
                </div>

                <div class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-4 dark:border-slate-700 dark:bg-slate-900/70">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">编辑建议</p>
                  <ul class="mt-3 space-y-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                    <li>标题尽量清晰直接，避免在列表里被截断。</li>
                    <li>摘要建议控制在 60 到 120 字，方便列表概览。</li>
                    <li>SEO 标题建议控制在 60 字以内，SEO 描述尽量控制在 160 字以内。</li>
                    <li>正文较长时，优先在这个页面完成完整编辑，不再用弹窗承载。</li>
                  </ul>
                </div>
              </div>
            </UCard>
          </aside>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
