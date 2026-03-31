<script setup lang="ts">
import type { AdminPostForm } from '../../../types/post'
import type { AdminTaxonomyItem } from '../../../types/taxonomy'
import AdminMarkdownWorkbench from './AdminMarkdownWorkbench.vue'
import ArticleTocPanel from './ArticleTocPanel.vue'

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
const adminTaxonomy = useAdminTaxonomy()

const isSubmitting = ref(false)
const isLoadingDetail = ref(false)
const isLoadingTaxonomy = ref(false)
const categoryOptions = ref<Array<{ label: string, value: number | null }>>([
  { label: '暂不设置分类', value: null }
])
const tagOptions = ref<AdminTaxonomyItem[]>([])
const topicOptions = ref<AdminTaxonomyItem[]>([])

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
  categoryId: null,
  tagIds: [],
  topicIds: [],
  status: 'DRAFT',
  seoTitle: '',
  seoDescription: '',
  contentMarkdown: '',
  contentHtml: '',
  contentToc: []
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
const {
  html: renderedContentHtml,
  toc: renderedContentToc,
  plainText: renderedPlainText,
  readingMinutes: renderedReadingMinutes,
  isRendering: isRenderingMarkdown
} = useMarkdownRenderer(toRef(formState, 'contentMarkdown'))

const contentLength = computed(() => renderedPlainText.value.length)
const seoTitleLength = computed(() => formState.seoTitle.trim().length)
const seoDescriptionLength = computed(() => formState.seoDescription.trim().length)
const estimatedReadingMinutes = computed(() => renderedReadingMinutes.value)
const selectedCategoryLabel = computed(() =>
  categoryOptions.value.find(option => option.value === formState.categoryId)?.label || '暂不设置分类'
)
const selectedTagItems = computed(() =>
  tagOptions.value.filter(item => formState.tagIds.includes(item.id))
)
const selectedTopicItems = computed(() =>
  topicOptions.value.filter(item => formState.topicIds.includes(item.id))
)

/**
 * 统一工作台卡片样式。
 * 作用：让文章编辑页的各个功能区共享同一套外层卡片视觉语言。
 */
const workspaceCardClass = 'rounded-[30px] border border-slate-200/80 bg-white/85 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-slate-800 dark:bg-slate-950/70 dark:shadow-[0_22px_48px_-30px_rgba(0,0,0,0.55)]'

/**
 * 统一工作区内层面板样式。
 * 作用：为基础信息、SEO、内容编排和侧栏摘要提供一致的内层承载容器。
 */
const workspaceSurfaceClass = 'rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-5 dark:border-slate-700 dark:bg-slate-900/70'

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
    formState.categoryId = detail.categoryId ?? null
    formState.tagIds = detail.tagIds || []
    formState.topicIds = detail.topicIds || []
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
 * 加载内容编排选项。
 * 作用：从真实后台接口读取分类、标签、专题列表，为文章编辑页提供真实选择数据。
 */
async function loadTaxonomyOptions() {
  isLoadingTaxonomy.value = true

  try {
    const [categoryResponse, tagResponse, topicResponse] = await Promise.all([
      adminTaxonomy.listItems('category', {
        pageNo: 1,
        pageSize: 100,
        status: 'ACTIVE'
      }),
      adminTaxonomy.listItems('tag', {
        pageNo: 1,
        pageSize: 100,
        status: 'ACTIVE'
      }),
      adminTaxonomy.listItems('topic', {
        pageNo: 1,
        pageSize: 100,
        status: 'ACTIVE'
      })
    ])

    categoryOptions.value = [
      { label: '暂不设置分类', value: null },
      ...categoryResponse.list.map(item => ({
        label: item.name,
        value: item.id
      }))
    ]
    tagOptions.value = tagResponse.list
    topicOptions.value = topicResponse.list
  } catch (error: any) {
    toast.add({
      title: '加载内容编排数据失败',
      description: error?.message || '分类、标签、专题暂时无法读取。',
      color: 'error'
    })
  } finally {
    isLoadingTaxonomy.value = false
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
    formState.contentHtml = renderedContentHtml.value
    formState.contentToc = renderedContentToc.value

    const payload: AdminPostForm = {
      title: formState.title.trim(),
      slug: formState.slug.trim(),
      summary: formState.summary.trim(),
      coverUrl: formState.coverUrl.trim(),
      categoryId: formState.categoryId,
      tagIds: [...formState.tagIds],
      topicIds: [...formState.topicIds],
      status: formState.status,
      seoTitle: formState.seoTitle.trim(),
      seoDescription: formState.seoDescription.trim(),
      contentMarkdown: formState.contentMarkdown,
      contentHtml: renderedContentHtml.value,
      contentToc: renderedContentToc.value,
      contentTocJson: JSON.stringify(renderedContentToc.value)
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

/**
 * 切换标签选中状态。
 * 作用：支持文章与多个标签建立关联，并保持当前选择顺序稳定。
 *
 * @param tagId 标签ID
 */
function toggleTag(tagId: number) {
  formState.tagIds = formState.tagIds.includes(tagId)
    ? formState.tagIds.filter(id => id !== tagId)
    : [...formState.tagIds, tagId]
}

/**
 * 切换专题选中状态。
 * 作用：支持文章与多个专题建立关联，便于后续专题页聚合同一篇文章。
 *
 * @param topicId 专题ID
 */
function toggleTopic(topicId: number) {
  formState.topicIds = formState.topicIds.includes(topicId)
    ? formState.topicIds.filter(id => id !== topicId)
    : [...formState.topicIds, topicId]
}

await Promise.all([
  loadTaxonomyOptions(),
  loadPostDetail()
])
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

            <UCard :class="workspaceCardClass">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">基础信息</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">文章基础信息</p>
                </div>
              </template>

              <div v-if="isLoadingDetail" class="space-y-4">
                <USkeleton class="h-16 rounded-2xl" />
                <USkeleton class="h-16 rounded-2xl" />
                <USkeleton class="h-28 rounded-2xl" />
              </div>

              <form v-else class="space-y-5" @submit.prevent="handleSubmit">
                <div :class="workspaceSurfaceClass">
                  <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
                    <div class="space-y-1">
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">主信息</p>
                      <p class="text-sm text-slate-500 dark:text-slate-400">标题、Slug 与状态决定文章在后台与前台的基础识别方式。</p>
                    </div>
                    <UBadge color="neutral" variant="soft">标题 {{ formState.title.trim().length }} 字</UBadge>
                  </div>

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
                </div>

                <div :class="workspaceSurfaceClass">
                  <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
                    <div class="space-y-1">
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">摘要与状态</p>
                      <p class="text-sm text-slate-500 dark:text-slate-400">摘要用于列表概览，状态决定这次保存将如何进入内容流程。</p>
                    </div>
                    <UBadge color="info" variant="soft">{{ statusHint }}</UBadge>
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
                </div>

                <div :class="workspaceSurfaceClass">
                  <div class="mb-4 space-y-1">
                    <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">封面资源</p>
                    <p class="text-sm text-slate-500 dark:text-slate-400">封面地址会影响列表首屏观感，建议优先使用稳定可访问的图片链接。</p>
                  </div>

                  <UFormField name="coverUrl" label="封面地址">
                    <AdminInput
                      v-model="formState.coverUrl"
                      placeholder="请输入封面图片 URL，可选"
                    />
                  </UFormField>
                </div>
              </form>
            </UCard>

            <UCard :class="workspaceCardClass">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">SEO</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">搜索展示信息</p>
                </div>
              </template>

              <div v-if="isLoadingDetail" class="space-y-4">
                <USkeleton class="h-16 rounded-2xl" />
                <USkeleton class="h-28 rounded-2xl" />
              </div>

                  <div v-else class="space-y-5">
                <div :class="workspaceSurfaceClass">
                  <div class="mb-4 flex flex-wrap items-center gap-3">
                    <UBadge color="neutral" variant="soft">SEO 标题 {{ seoTitleLength }} 字</UBadge>
                    <UBadge color="neutral" variant="soft">SEO 描述 {{ seoDescriptionLength }} 字</UBadge>
                  </div>

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
                </div>

                <div class="grid gap-3 lg:grid-cols-3">
                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">搜索建议</p>
                    <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">SEO 标题建议控制在 60 字以内，避免搜索结果里被截断。</p>
                  </div>

                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">描述建议</p>
                    <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">SEO 描述尽量控制在 160 字以内，突出文章核心价值。</p>
                  </div>

                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">摘要关系</p>
                    <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">如果没有单独 SEO 描述，可先从摘要中提炼一版再微调。</p>
                  </div>
                </div>
              </div>
            </UCard>

            <UCard :class="workspaceCardClass">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Structure</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">分类 / 标签 / 专题</p>
                </div>
              </template>

              <div v-if="isLoadingTaxonomy" class="grid gap-5 lg:grid-cols-3">
                <USkeleton class="h-36 rounded-[24px]" />
                <USkeleton class="h-36 rounded-[24px]" />
                <USkeleton class="h-36 rounded-[24px]" />
              </div>

              <div v-else class="grid gap-5 lg:grid-cols-3">
                <div :class="workspaceSurfaceClass">
                  <div class="flex items-center justify-between gap-3">
                    <div class="space-y-1">
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">分类</p>
                      <p class="text-sm text-slate-500 dark:text-slate-400">单选，确定文章主归档。</p>
                    </div>
                    <UBadge color="info" variant="soft">{{ selectedCategoryLabel }}</UBadge>
                  </div>
                  <div class="mt-4 space-y-3">
                    <AdminSelect
                      v-model="formState.categoryId"
                      :items="categoryOptions"
                      placeholder="请选择分类"
                    />
                    <p class="text-sm leading-7 text-slate-600 dark:text-slate-300">
                      分类主要用于文章主归档和列表组织。
                    </p>
                  </div>
                </div>

                <div :class="workspaceSurfaceClass">
                  <div class="flex items-center justify-between gap-3">
                    <div class="space-y-1">
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">标签</p>
                      <p class="text-sm text-slate-500 dark:text-slate-400">多选，补充文章主题关键词。</p>
                    </div>
                    <UBadge color="info" variant="soft">已选 {{ formState.tagIds.length }}</UBadge>
                  </div>
                  <div v-if="tagOptions.length" class="mt-4 flex flex-wrap gap-2">
                    <button
                      v-for="tag in tagOptions"
                      :key="tag.id"
                      type="button"
                      class="cursor-pointer rounded-full border px-3 py-2 text-sm font-medium transition duration-200"
                      :class="formState.tagIds.includes(tag.id)
                        ? 'border-sky-300 bg-sky-50 text-sky-700 shadow-[0_10px_24px_-20px_rgba(14,165,233,0.45)] dark:border-sky-400/40 dark:bg-sky-400/12 dark:text-sky-200'
                        : 'border-slate-200/90 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-950/60 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:text-slate-50'"
                      @click="toggleTag(tag.id)"
                    >
                      {{ tag.name }}
                    </button>
                  </div>
                  <div v-else class="mt-4 text-sm leading-7 text-slate-500 dark:text-slate-400">
                    当前没有可用标签，请先到标签管理页创建数据。
                  </div>
                </div>

                <div :class="workspaceSurfaceClass">
                  <div class="flex items-center justify-between gap-3">
                    <div class="space-y-1">
                      <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">专题</p>
                      <p class="text-sm text-slate-500 dark:text-slate-400">多选，适合归入系列内容。</p>
                    </div>
                    <UBadge color="info" variant="soft">已选 {{ formState.topicIds.length }}</UBadge>
                  </div>
                  <div v-if="topicOptions.length" class="mt-4 flex flex-wrap gap-2">
                    <button
                      v-for="topic in topicOptions"
                      :key="topic.id"
                      type="button"
                      class="cursor-pointer rounded-full border px-3 py-2 text-sm font-medium transition duration-200"
                      :class="formState.topicIds.includes(topic.id)
                        ? 'border-emerald-300 bg-emerald-50 text-emerald-700 shadow-[0_10px_24px_-20px_rgba(16,185,129,0.42)] dark:border-emerald-400/40 dark:bg-emerald-400/12 dark:text-emerald-200'
                        : 'border-slate-200/90 bg-white text-slate-600 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-950/60 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:text-slate-50'"
                      @click="toggleTopic(topic.id)"
                    >
                      {{ topic.name }}
                    </button>
                  </div>
                  <div v-else class="mt-4 text-sm leading-7 text-slate-500 dark:text-slate-400">
                    当前没有可用专题，请先到专题管理页创建数据。
                  </div>
                </div>
              </div>
            </UCard>

          </section>

          <aside class="space-y-6">
            <UCard :class="workspaceCardClass">
              <template #header>
                <div class="space-y-1">
                  <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Overview</p>
                  <p class="text-base font-semibold text-slate-900 dark:text-slate-50">编辑摘要</p>
                </div>
              </template>

              <div class="space-y-4">
                <div :class="workspaceSurfaceClass">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">当前状态</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ statusHint }}</p>
                </div>

                <div :class="workspaceSurfaceClass">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">内容归属</p>
                  <p class="mt-2 text-sm font-semibold text-slate-900 dark:text-slate-50">{{ selectedCategoryLabel }}</p>
                  <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                    标签 {{ selectedTagItems.length }} 个，专题 {{ selectedTopicItems.length }} 个
                  </p>
                </div>

                <div :class="workspaceSurfaceClass">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">正文目录</p>
                  <div class="mt-3">
                    <ArticleTocPanel :items="renderedContentToc" />
                  </div>
                </div>

                <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">标题长度</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ formState.title.trim().length }}</p>
                  </div>

                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">正文字符数</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ contentLength }}</p>
                  </div>
                </div>

                <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-1">
                  <div :class="workspaceSurfaceClass">
                    <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">SEO 标题长度</p>
                    <p class="mt-2 text-2xl font-semibold text-slate-900 dark:text-slate-50">{{ seoTitleLength }}</p>
                  </div>

                  <div :class="workspaceSurfaceClass">
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

                <div :class="workspaceSurfaceClass">
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

        <UCard :class="workspaceCardClass">
          <template #header>
            <div class="space-y-1">
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Content</p>
              <p class="text-base font-semibold text-slate-900 dark:text-slate-50">正文双栏工作台</p>
            </div>
          </template>

          <div v-if="isLoadingDetail" class="space-y-4">
            <USkeleton class="h-[42rem] rounded-[28px]" />
          </div>

          <div v-else class="space-y-5">
            <AdminMarkdownWorkbench
              v-model="formState.contentMarkdown"
              :html="renderedContentHtml"
              :is-loading="isRenderingMarkdown"
              :content-length="contentLength"
              :reading-minutes="estimatedReadingMinutes"
              :toc-count="renderedContentToc.length"
            />

            <div class="grid gap-3 lg:grid-cols-3">
              <div :class="workspaceSurfaceClass">
                <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">写作建议</p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">正文较长时优先在这个页面完成完整编辑，不再使用弹窗承载。</p>
              </div>

              <div :class="workspaceSurfaceClass">
                <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">结构建议</p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">长内容建议通过二级标题和段落分组，让预览检查更直观。</p>
              </div>

              <div :class="workspaceSurfaceClass">
                <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">发布建议</p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">准备发布前请结合右侧摘要卡，检查封面、SEO 和内容归属是否完整。</p>
              </div>
            </div>
          </div>
        </UCard>
      </div>
    </template>
  </UDashboardPanel>
</template>
