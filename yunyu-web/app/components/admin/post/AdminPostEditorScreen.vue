<script setup lang="ts">
import type { AdminPostForm } from '../../../types/post'
import type { AdminTaxonomyItem } from '../../../types/taxonomy'
import type { AdminAttachmentItem } from '../../../types/attachment'
import AdminMarkdownWorkbench from './AdminMarkdownWorkbench.vue'

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
const adminAttachments = useAdminAttachments()

const isSubmitting = ref(false)
const isLoadingDetail = ref(false)
const isLoadingTaxonomy = ref(false)
const isUploadingVideo = ref(false)
const videoFileInputRef = ref<HTMLInputElement | null>(null)
const isCoverUploadModalOpen = ref(false)
const isCoverSelectModalOpen = ref(false)
const coverSelectorReloadToken = ref(0)
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
  videoUrl: '',
  categoryId: null,
  tagIds: [],
  topicIds: [],
  status: 'DRAFT',
  isTop: false,
  isRecommend: false,
  allowComment: true,
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
const pageTitle = computed(() => isEditing.value ? '编辑文章' : '新增文章')

/**
 * 计算草稿按钮文案。
 * 作用：在新增与编辑场景下统一顶部次操作的命名节奏。
 */
const draftActionLabel = computed(() => isEditing.value ? '保存草稿' : '存为草稿')

/**
 * 计算主按钮文案。
 * 作用：让顶部主操作在编辑模式下更聚焦于保存当前内容，而不是额外强调状态提示。
 */
const primaryActionLabel = computed(() => isEditing.value ? '保存更新' : '发布文章')

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
const estimatedReadingMinutes = computed(() => renderedReadingMinutes.value)
const selectedCategoryLabel = computed(() =>
  categoryOptions.value.find(option => option.value === formState.categoryId)?.label || '暂不设置分类'
)

/**
 * 统一工作台卡片样式。
 * 作用：让文章编辑页的各个功能区共享同一套外层卡片视觉语言。
 */
const workspaceCardClass = 'overflow-hidden rounded-[16px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.82),rgba(255,255,255,0.66))] shadow-[0_18px_34px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.82),rgba(15,23,42,0.68))] dark:shadow-[0_20px_38px_-32px_rgba(0,0,0,0.42)]'

/**
 * 统一工作区内层面板样式。
 * 作用：为基础信息、SEO、内容编排和侧栏摘要提供一致的内层承载容器。
 */
const workspaceSurfaceClass = 'rounded-[12px] border border-white/60 bg-white/62 p-4 shadow-[0_12px_24px_-24px_rgba(15,23,42,0.14)] backdrop-blur-md dark:border-white/10 dark:bg-white/[0.04]'

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
    formState.videoUrl = detail.videoUrl || ''
    formState.categoryId = detail.categoryId ?? null
    formState.tagIds = detail.tagIds || []
    formState.topicIds = detail.topicIds || []
    formState.status = detail.status
    formState.isTop = detail.isTop ?? false
    formState.isRecommend = detail.isRecommend ?? false
    formState.allowComment = detail.allowComment ?? true
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
      videoUrl: formState.videoUrl.trim(),
      categoryId: formState.categoryId,
      tagIds: [...formState.tagIds],
      topicIds: [...formState.topicIds],
      status: formState.status,
      isTop: formState.isTop,
      isRecommend: formState.isRecommend,
      allowComment: formState.allowComment,
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

/**
 * 触发视频文件选择。
 * 作用：打开视频上传文件选择器，准备执行前端直传。
 */
function triggerVideoUpload() {
  videoFileInputRef.value?.click()
}

/**
 * 处理视频文件选择。
 *
 * @param event 文件选择事件
 */
async function onVideoFileSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  await uploadAttachmentAndFill(file, 'video')
  input.value = ''
}

/**
 * 上传附件并回填 URL。
 * 作用：复用附件直传能力，将上传后的访问地址回填到文章表单字段。
 *
 * @param file 文件对象
 * @param target 回填目标
 */
async function uploadAttachmentAndFill(file: File, target: 'cover' | 'video') {
  const isVideoTarget = target === 'video'
  if (isVideoTarget) {
    isUploadingVideo.value = true
  }

  try {
    const uploadResult = await adminAttachments.uploadFile(file)
    if (isVideoTarget) {
      formState.videoUrl = uploadResult.attachment.accessUrl
    } else {
      formState.coverUrl = uploadResult.attachment.accessUrl
    }
    toast.add({
      title: isVideoTarget ? '视频上传成功' : '封面上传成功',
      description: `已回填 ${uploadResult.attachment.fileName}`,
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: isVideoTarget ? '视频上传失败' : '封面上传失败',
      description: error?.message || '附件上传失败，请稍后重试。',
      color: 'error'
    })
  } finally {
    if (isVideoTarget) {
      isUploadingVideo.value = false
    }
  }
}

/**
 * 打开封面上传弹窗。
 */
function openCoverUploadModal() {
  isCoverUploadModalOpen.value = true
}

/**
 * 打开封面附件选择弹窗。
 */
function openCoverSelectorModal() {
  isCoverSelectModalOpen.value = true
}

/**
 * 同步封面上传弹窗开关状态。
 *
 * @param value 是否打开
 */
function handleCoverUploadModalOpenChange(value: boolean) {
  isCoverUploadModalOpen.value = value
}

/**
 * 同步封面附件选择弹窗开关状态。
 *
 * @param value 是否打开
 */
function handleCoverSelectModalOpenChange(value: boolean) {
  isCoverSelectModalOpen.value = value
}

/**
 * 处理封面上传成功回调。
 * 作用：上传完成后回填封面地址并关闭上传弹窗。
 *
 * @param attachment 上传后的附件
 */
function handleCoverUploaded(attachment: AdminAttachmentItem) {
  formState.coverUrl = attachment.accessUrl
  isCoverUploadModalOpen.value = false
  coverSelectorReloadToken.value += 1
  toast.add({
    title: '封面上传成功',
    description: `已回填 ${attachment.fileName}`,
    color: 'success'
  })
}

/**
 * 处理从附件库选择封面。
 * 作用：将所选图片附件地址回填到封面字段并关闭选择弹窗。
 *
 * @param item 附件条目
 */
function handleSelectCoverFromLibrary(item: AdminAttachmentItem) {
  formState.coverUrl = item.accessUrl
  isCoverSelectModalOpen.value = false
  toast.add({
    title: '封面已选择',
    description: item.fileName,
    color: 'success'
  })
}

await Promise.all([
  loadTaxonomyOptions(),
  loadPostDetail()
])
</script>

<template>
  <div class="space-y-4">
    <section :class="workspaceCardClass">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <div class="flex items-center gap-2">
            <button
              type="button"
              class="inline-flex size-9 items-center justify-center rounded-[10px] border border-white/65 bg-white/70 text-slate-600 shadow-[0_10px_18px_-18px_rgba(15,23,42,0.14)] backdrop-blur-md transition duration-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-300 dark:hover:text-slate-50"
              title="返回列表"
              aria-label="返回列表"
              @click="goBackToList"
            >
              <UIcon name="i-lucide-arrow-left" class="size-4" />
            </button>

            <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">
              {{ pageTitle }}
            </h1>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <AdminButton
            :label="draftActionLabel"
            tone="neutral"
            variant="outline"
            :loading="isSubmitting && formState.status === 'DRAFT'"
            @click="handleSubmitWithStatus('DRAFT')"
          />
          <AdminPrimaryButton
            :label="primaryActionLabel"
            loading-label="保存中..."
            :loading="isSubmitting"
            icon="i-lucide-send"
            @click="handleSubmit"
          />
        </div>
      </div>
    </section>

    <div class="space-y-4">
      <section :class="workspaceCardClass">
        <div class="border-b border-white/60 px-5 py-4 dark:border-white/10">
          <p class="text-base font-semibold text-slate-900 dark:text-slate-50">内容信息</p>
        </div>

        <div v-if="isLoadingDetail" class="space-y-4 px-5 py-5">
          <USkeleton class="h-16 rounded-[10px]" />
          <USkeleton class="h-16 rounded-[10px]" />
          <USkeleton class="h-28 rounded-[10px]" />
        </div>

        <form v-else class="space-y-5 px-5 py-5" @submit.prevent="handleSubmit">
          <div class="grid gap-5 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
            <div :class="workspaceSurfaceClass">
              <p class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">主信息</p>

              <div class="space-y-5">
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

                <UFormField name="summary" label="摘要">
                  <AdminTextarea
                    v-model="formState.summary"
                    :rows="4"
                    autoresize
                    placeholder="请输入摘要"
                  />
                </UFormField>

                <UFormField name="coverUrl" label="封面地址">
                  <div class="space-y-2">
                    <AdminInput
                      v-model="formState.coverUrl"
                      placeholder="请输入封面图片 URL"
                    />
                    <div class="flex flex-wrap justify-end gap-2">
                      <AdminButton
                        label="上传图片"
                        tone="neutral"
                        variant="outline"
                        icon="i-lucide-upload"
                        @click="openCoverUploadModal"
                      />
                      <AdminButton
                        label="从附件选择"
                        tone="neutral"
                        variant="outline"
                        icon="i-lucide-images"
                        @click="openCoverSelectorModal"
                      />
                    </div>
                  </div>
                </UFormField>

                <UFormField name="videoUrl" label="视频地址">
                  <div class="space-y-2">
                    <AdminInput
                      v-model="formState.videoUrl"
                      placeholder="请输入视频直链 URL"
                    />
                    <div class="flex justify-end">
                      <AdminButton
                        label="上传视频"
                        tone="neutral"
                        variant="outline"
                        icon="i-lucide-upload"
                        :loading="isUploadingVideo"
                        @click="triggerVideoUpload"
                      />
                    </div>
                    <input
                      ref="videoFileInputRef"
                      type="file"
                      accept="video/mp4,video/webm,video/quicktime"
                      class="hidden"
                      @change="onVideoFileSelected"
                    >
                  </div>
                </UFormField>
              </div>
            </div>

            <div class="space-y-5">
              <div :class="workspaceSurfaceClass">
                <p class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">归属</p>

                <div class="space-y-5">
                  <UFormField name="categoryId" label="分类">
                    <AdminSelect
                      v-model="formState.categoryId"
                      :items="categoryOptions"
                      placeholder="请选择分类"
                    />
                  </UFormField>

                  <div>
                    <p class="mb-3 text-sm font-medium text-slate-700 dark:text-slate-200">标签</p>
                    <div v-if="tagOptions.length" class="flex flex-wrap gap-2">
                      <button
                        v-for="tag in tagOptions"
                        :key="tag.id"
                        type="button"
                        class="cursor-pointer rounded-[10px] border px-3 py-2 text-sm font-medium transition duration-200"
                        :class="formState.tagIds.includes(tag.id)
                          ? 'border-sky-300/90 bg-sky-50/92 text-sky-700 shadow-[0_10px_20px_-22px_rgba(14,165,233,0.4)] dark:border-sky-400/40 dark:bg-sky-400/12 dark:text-sky-200'
                          : 'border-white/65 bg-white/72 text-slate-600 hover:border-slate-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50'"
                        @click="toggleTag(tag.id)"
                      >
                        {{ tag.name }}
                      </button>
                    </div>
                  </div>

                  <div>
                    <p class="mb-3 text-sm font-medium text-slate-700 dark:text-slate-200">专题</p>
                    <div v-if="topicOptions.length" class="flex flex-wrap gap-2">
                      <button
                        v-for="topic in topicOptions"
                        :key="topic.id"
                        type="button"
                        class="cursor-pointer rounded-[10px] border px-3 py-2 text-sm font-medium transition duration-200"
                        :class="formState.topicIds.includes(topic.id)
                          ? 'border-emerald-300/90 bg-emerald-50/92 text-emerald-700 shadow-[0_10px_20px_-22px_rgba(16,185,129,0.4)] dark:border-emerald-400/40 dark:bg-emerald-400/12 dark:text-emerald-200'
                          : 'border-white/65 bg-white/72 text-slate-600 hover:border-slate-200 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-300 dark:hover:border-white/15 dark:hover:text-slate-50'"
                        @click="toggleTopic(topic.id)"
                      >
                        {{ topic.name }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div :class="workspaceSurfaceClass">
                <p class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">SEO</p>

                <div class="space-y-5">
                  <UFormField name="seoTitle" label="SEO 标题">
                    <AdminInput
                      v-model="formState.seoTitle"
                      placeholder="请输入 SEO 标题"
                    />
                  </UFormField>

                  <UFormField name="seoDescription" label="SEO 描述">
                    <AdminTextarea
                      v-model="formState.seoDescription"
                      :rows="4"
                      autoresize
                      placeholder="请输入 SEO 描述"
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
                <p class="mb-4 text-sm font-semibold text-slate-900 dark:text-slate-50">发布设置</p>

                <div class="space-y-3">
                  <AdminSwitchField
                    v-model="formState.isTop"
                    label="置顶展示"
                    description="开启后会在前台列表中优先展示这篇文章。"
                    color="primary"
                    active-text="已置顶"
                    inactive-text="未置顶"
                  />

                  <AdminSwitchField
                    v-model="formState.isRecommend"
                    label="推荐文章"
                    description="开启后可参与前台推荐位和相关文章推荐。"
                    color="primary"
                    active-text="已推荐"
                    inactive-text="未推荐"
                  />

                  <AdminSwitchField
                    v-model="formState.allowComment"
                    label="允许评论"
                    description="关闭后前台详情页将不再开放评论互动。"
                    color="primary"
                    active-text="可评论"
                    inactive-text="已关闭"
                  />
                </div>
              </div>
            </div>
          </div>
        </form>
      </section>

      <section :class="workspaceCardClass">
        <div class="border-b border-white/60 px-5 py-4 dark:border-white/10">
          <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p class="text-base font-semibold text-slate-900 dark:text-slate-50">正文</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">在这里完成正文输入、实时预览和渲染结果检查。</p>
            </div>
            <span class="text-xs font-medium uppercase tracking-[0.16em] text-slate-400 dark:text-slate-500">
              Markdown Workspace
            </span>
          </div>
        </div>

        <div v-if="isLoadingDetail" class="space-y-4 px-5 py-5">
            <USkeleton class="h-[42rem] rounded-[12px]" />
        </div>

        <div v-else class="px-4 py-4 sm:px-5 sm:py-5">
          <AdminMarkdownWorkbench
            v-model="formState.contentMarkdown"
            :html="renderedContentHtml"
            :toc="renderedContentToc"
            :plain-text="renderedPlainText"
            :is-loading="isRenderingMarkdown"
            :content-length="contentLength"
            :reading-minutes="estimatedReadingMinutes"
            :toc-count="renderedContentToc.length"
          />
        </div>
      </section>
    </div>

    <UModal
      :open="isCoverUploadModalOpen"
      title="上传封面图片"
      description="支持点击选择或拖拽上传，上传成功后会自动回填封面地址。"
      :ui="{
        overlay: 'bg-slate-950/36 backdrop-blur-[10px] dark:bg-slate-950/60',
        content: 'w-[calc(100vw-2rem)] max-w-2xl rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))]',
        header: 'px-6 pt-6 pb-4 border-b border-white/60 dark:border-white/10',
        body: 'px-6 py-5'
      }"
      @update:open="handleCoverUploadModalOpenChange"
    >
      <template #body>
        <AdminAttachmentUploader
          accept="image/*"
          upload-button-label="上传并回填封面"
          empty-hint="点击选择图片，或将图片拖拽到此区域"
          @uploaded="handleCoverUploaded"
        />
      </template>
    </UModal>

    <UModal
      :open="isCoverSelectModalOpen"
      title="从附件库选择封面"
      description="复用附件管理列表与查询，选择一个图片后将自动回填封面地址。"
      :ui="{
        overlay: 'bg-slate-950/36 backdrop-blur-[10px] dark:bg-slate-950/60',
        content: 'w-[calc(100vw-2rem)] max-w-[1100px] rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))]',
        header: 'px-6 pt-6 pb-4 border-b border-white/60 dark:border-white/10',
        body: 'px-6 py-5'
      }"
      @update:open="handleCoverSelectModalOpenChange"
    >
      <template #body>
        <AdminAttachmentLibraryPanel
          mode="select"
          default-mime-type="image/"
          :reload-token="coverSelectorReloadToken"
          @select="handleSelectCoverFromLibrary"
        />
      </template>
    </UModal>
  </div>
</template>
