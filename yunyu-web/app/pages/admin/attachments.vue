<script setup lang="ts">
import { formatChineseDateTime } from '../../utils/date'
import type { AdminAttachmentItem } from '../../types/attachment'

/**
 * 后台附件管理页。
 * 作用：提供附件列表查询、关键词筛选、MIME 筛选和删除附件的统一管理入口。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminAttachments = useAdminAttachments()

const isLoading = ref(false)
const isDeleteSubmitting = ref(false)
const searchKeyword = ref('')
const activeMimeType = ref('ALL')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
const isDeleteModalOpen = ref(false)
const isUploadTesterModalOpen = ref(false)
const deletingAttachment = ref<AdminAttachmentItem | null>(null)
const attachments = ref<AdminAttachmentItem[]>([])

const mimeTypeOptions = [
  { label: '全部类型', value: 'ALL' },
  { label: '图片', value: 'image/' },
  { label: '视频', value: 'video/' },
  { label: 'PNG', value: 'image/png' },
  { label: 'JPEG', value: 'image/jpeg' },
  { label: 'WEBP', value: 'image/webp' },
  { label: 'MP4', value: 'video/mp4' }
] as const

/**
 * 加载附件列表。
 * 作用：按筛选和分页条件请求后台附件列表接口。
 */
async function loadAttachments() {
  isLoading.value = true

  try {
    const response = await adminAttachments.listAttachments({
      keyword: searchKeyword.value.trim() || undefined,
      mimeType: resolveMimeTypeFilter(),
      pageNo: currentPage.value,
      pageSize: pageSize.value
    })

    attachments.value = response.list
    total.value = response.total
    totalPages.value = response.totalPages
  } catch (error: any) {
    toast.add({
      title: '加载附件失败',
      description: error?.message || '附件列表暂时无法读取。',
      color: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

/**
 * 解析 MIME 筛选值。
 *
 * @returns 传给后端的 MIME 筛选值
 */
function resolveMimeTypeFilter() {
  if (activeMimeType.value === 'ALL') {
    return undefined
  }
  return activeMimeType.value
}

/**
 * 提交筛选。
 * 作用：重置到第一页并按当前筛选项重新拉取附件数据。
 */
async function handleSearch() {
  currentPage.value = 1
  await loadAttachments()
}

/**
 * 重置筛选条件。
 */
async function resetFilters() {
  searchKeyword.value = ''
  activeMimeType.value = 'ALL'
  currentPage.value = 1
  await loadAttachments()
}

/**
 * 打开测试上传弹窗。
 */
function openUploadTesterModal() {
  isUploadTesterModalOpen.value = true
}

/**
 * 处理测试上传成功回调。
 * 作用：上传成功后刷新附件列表，让新记录可立即可见。
 */
async function handleUploaded() {
  currentPage.value = 1
  await loadAttachments()
}

/**
 * 同步测试上传弹窗开关状态。
 *
 * @param value 是否打开
 */
function handleUploadTesterModalOpenChange(value: boolean) {
  isUploadTesterModalOpen.value = value
}

/**
 * 打开删除确认弹窗。
 *
 * @param item 附件条目
 */
function openDeleteModal(item: AdminAttachmentItem) {
  deletingAttachment.value = item
  isDeleteModalOpen.value = true
}

/**
 * 确认删除附件。
 */
async function confirmDelete() {
  if (!deletingAttachment.value) {
    return
  }

  isDeleteSubmitting.value = true

  try {
    await adminAttachments.deleteAttachment(deletingAttachment.value.id)
    toast.add({ title: '附件已删除', color: 'success' })
    isDeleteModalOpen.value = false
    deletingAttachment.value = null
    await loadAttachments()
  } catch (error: any) {
    toast.add({
      title: '删除失败',
      description: error?.message || '附件删除未成功，请稍后重试。',
      color: 'error'
    })
  } finally {
    isDeleteSubmitting.value = false
  }
}

/**
 * 复制附件访问地址。
 *
 * @param item 附件条目
 */
async function copyAccessUrl(item: AdminAttachmentItem) {
  try {
    await navigator.clipboard.writeText(item.accessUrl)
    toast.add({ title: '链接已复制', color: 'success' })
  } catch {
    toast.add({ title: '复制失败', description: '浏览器未授权复制。', color: 'warning' })
  }
}

/**
 * 格式化文件大小。
 *
 * @param sizeBytes 字节数
 * @returns 可读文件大小
 */
function formatFileSize(sizeBytes: number) {
  if (sizeBytes < 1024) {
    return `${sizeBytes} B`
  }
  if (sizeBytes < 1024 * 1024) {
    return `${(sizeBytes / 1024).toFixed(1)} KB`
  }
  if (sizeBytes < 1024 * 1024 * 1024) {
    return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`
  }
  return `${(sizeBytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

/**
 * 解析 MIME 简要类型标签。
 *
 * @param mimeType MIME 类型
 * @returns 类型标签
 */
function resolveMimeGroup(mimeType: string) {
  if (mimeType.startsWith('image/')) {
    return '图片'
  }
  if (mimeType.startsWith('video/')) {
    return '视频'
  }
  return '文件'
}

watch([currentPage, pageSize], async () => {
  await loadAttachments()
})

await loadAttachments()
</script>

<template>
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">附件管理</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">管理上传后的附件资源，支持检索、复制链接和删除。</p>
        </div>

        <div class="flex flex-wrap items-center gap-2">
          <AdminPrimaryButton
            icon="i-lucide-upload"
            label="测试上传"
            @click="openUploadTesterModal"
          />
          <AdminButton
            icon="i-lucide-refresh-cw"
            :loading="isLoading"
            label="刷新"
            tone="neutral"
            variant="outline"
            @click="loadAttachments"
          />
        </div>
      </div>
    </section>

    <AdminListFilterBar>
      <template #search>
        <AdminInput
          v-model="searchKeyword"
          icon="i-lucide-search"
          placeholder="搜索文件名 / objectKey / sha256"
          @keyup.enter="handleSearch"
        />
      </template>

      <template #filters>
        <AdminSelect
          v-model="activeMimeType"
          :items="mimeTypeOptions"
          class="w-[190px]"
          placeholder="全部类型"
          @update:model-value="handleSearch"
        />
      </template>

      <template #actions>
        <AdminButton
          icon="i-lucide-search"
          label="查询"
          tone="neutral"
          variant="outline"
          @click="handleSearch"
        />
        <AdminButton
          icon="i-lucide-rotate-ccw"
          label="重置"
          tone="neutral"
          variant="ghost"
          @click="resetFilters"
        />
      </template>
    </AdminListFilterBar>

    <section class="rounded-[16px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.76),rgba(255,255,255,0.58))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.74),rgba(15,23,42,0.64))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <AdminDataTable
        :is-loading="isLoading"
        :has-data="attachments.length > 0"
        min-width="1200px"
        header-class="grid-cols-[minmax(0,1.5fr)_180px_130px_130px_180px_240px_220px_130px]"
        empty-title="暂无附件数据"
      >
        <template #header>
          <span>附件信息</span>
          <span>类型</span>
          <span>大小</span>
          <span>上传人</span>
          <span>上传时间</span>
          <span>对象键</span>
          <span>SHA-256</span>
          <span>操作</span>
        </template>

        <div
          v-for="item in attachments"
          :key="item.id"
          class="grid grid-cols-[minmax(0,1.5fr)_180px_130px_130px_180px_240px_220px_130px] gap-4 px-4 py-3 text-sm"
        >
          <div class="min-w-0">
            <p class="truncate font-medium text-slate-900 dark:text-slate-50">{{ item.fileName }}</p>
            <p class="mt-1 truncate text-xs text-slate-500 dark:text-slate-400">{{ item.accessUrl }}</p>
          </div>

          <div class="text-slate-600 dark:text-slate-300">
            <UBadge color="neutral" variant="soft" class="rounded-[8px] px-2 py-0.5">
              {{ resolveMimeGroup(item.mimeType) }}
            </UBadge>
            <p class="mt-1 truncate text-xs">{{ item.mimeType }}</p>
          </div>

          <div class="text-slate-600 dark:text-slate-300">{{ formatFileSize(item.sizeBytes) }}</div>
          <div class="text-slate-600 dark:text-slate-300">#{{ item.uploaderUserId }}</div>
          <div class="text-slate-600 dark:text-slate-300">{{ formatChineseDateTime(item.createdTime, '-') }}</div>
          <div class="truncate text-xs text-slate-600 dark:text-slate-300">{{ item.objectKey }}</div>
          <div class="truncate text-xs text-slate-600 dark:text-slate-300">{{ item.sha256 }}</div>

          <div class="flex items-center gap-2">
            <AdminActionIconButton
              icon="i-lucide-copy"
              tooltip="复制链接"
              @click="copyAccessUrl(item)"
            />
            <AdminActionIconButton
              icon="i-lucide-trash-2"
              tooltip="删除"
              @click="openDeleteModal(item)"
            />
          </div>
        </div>
      </AdminDataTable>

      <AdminPaginationBar
        v-model:page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :total-pages="totalPages"
      />
    </section>

    <AdminConfirmModal
      v-model:open="isDeleteModalOpen"
      title="删除附件"
      :description="`确认删除附件《${deletingAttachment?.fileName || ''}》吗？此操作会同时删除远程 S3 文件。`"
      confirm-label="确认删除"
      :loading="isDeleteSubmitting"
      @confirm="confirmDelete"
    />

    <UModal
      :open="isUploadTesterModalOpen"
      title="测试上传"
      description="用于验证当前 S3 配置和前端直传链路是否正常。"
      :ui="{
        overlay: 'bg-slate-950/36 backdrop-blur-[10px] dark:bg-slate-950/60',
        content: 'w-[calc(100vw-2rem)] max-w-2xl rounded-[16px] border border-white/60 bg-[linear-gradient(180deg,rgba(255,255,255,0.92),rgba(255,255,255,0.82))] shadow-[0_24px_48px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.92),rgba(15,23,42,0.88))]',
        header: 'px-6 pt-6 pb-4 border-b border-white/60 dark:border-white/10',
        body: 'px-6 py-5'
      }"
      @update:open="handleUploadTesterModalOpenChange"
    >
      <template #body>
        <AdminAttachmentUploadTester @uploaded="handleUploaded" />
      </template>
    </UModal>
  </div>
</template>
