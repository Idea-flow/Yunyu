<script setup lang="ts">
import { formatChineseDateTime } from '../../../utils/date'
import type { AdminAttachmentItem } from '../../../types/attachment'

/**
 * 后台附件库面板组件。
 * 作用：提供统一的附件查询、列表展示与分页能力，支持管理模式和选择模式复用。
 */
const props = withDefaults(defineProps<{
  mode?: 'manage' | 'select'
  defaultMimeType?: string
  reloadToken?: number
}>(), {
  mode: 'manage',
  defaultMimeType: 'ALL',
  reloadToken: 0
})

const emit = defineEmits<{
  select: [item: AdminAttachmentItem]
  requestDelete: [item: AdminAttachmentItem]
}>()

const toast = useToast()
const adminAttachments = useAdminAttachments()

const isLoading = ref(false)
const searchKeyword = ref('')
const activeMimeType = ref(props.defaultMimeType)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(1)
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

const isSelectMode = computed(() => props.mode === 'select')

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
  activeMimeType.value = props.defaultMimeType
  currentPage.value = 1
  await loadAttachments()
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
 * 触发选择附件。
 *
 * @param item 附件条目
 */
function handleSelect(item: AdminAttachmentItem) {
  emit('select', item)
}

/**
 * 触发删除附件请求。
 *
 * @param item 附件条目
 */
function handleDelete(item: AdminAttachmentItem) {
  emit('requestDelete', item)
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
 * 处理页码切换。
 *
 * @param page 新页码
 */
function handlePageChange(page: number) {
  currentPage.value = page
}

/**
 * 处理每页条数切换。
 *
 * @param nextPageSize 新每页条数
 */
function handlePageSizeChange(nextPageSize: number) {
  pageSize.value = nextPageSize
}

watch([currentPage, pageSize], async () => {
  await loadAttachments()
})

watch(() => props.reloadToken, async () => {
  currentPage.value = 1
  await loadAttachments()
})

watch(() => props.defaultMimeType, (value) => {
  activeMimeType.value = value
})

await loadAttachments()
</script>

<template>
  <AdminListWorkbench
    :is-loading="isLoading"
    :has-data="attachments.length > 0"
    min-width="2360px"
    header-class="grid-cols-[110px_140px_minmax(0,3.8fr)_220px_140px_180px_130px_180px_260px_220px_150px]"
    empty-title="暂无附件数据"
    :page="currentPage"
    :page-size="pageSize"
    :total="total"
    :total-pages="totalPages"
    @update:page="handlePageChange"
    @update:page-size="handlePageSizeChange"
  >
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

    <template #header>
      <span>ID</span>
      <span>上传人ID</span>
      <span>链接信息</span>
      <span>文件名</span>
      <span>图片预览</span>
      <span>类型</span>
      <span>大小</span>
      <span>上传时间</span>
      <span>对象键</span>
      <span>SHA-256</span>
      <span>操作</span>
    </template>

    <div
      v-for="item in attachments"
      :key="item.id"
      class="grid grid-cols-[110px_140px_minmax(0,3.8fr)_220px_140px_180px_130px_180px_260px_220px_150px] gap-4 px-4 py-3 text-sm"
    >
      <div class="text-slate-700 dark:text-slate-200">{{ item.id }}</div>
      <div class="text-slate-700 dark:text-slate-200">{{ item.uploaderUserId }}</div>
      <div class="min-w-0 text-slate-600 dark:text-slate-300">
        <AdminTextHoverTip :text="item.accessUrl" />
      </div>
      <div class="min-w-0 text-slate-700 dark:text-slate-200">
        <p class="truncate">{{ item.fileName }}</p>
      </div>
      <AdminAttachmentImagePreview
        :mime-type="item.mimeType"
        :access-url="item.accessUrl"
        :file-name="item.fileName"
      />

      <div class="text-slate-600 dark:text-slate-300">
        <UBadge color="neutral" variant="soft" class="rounded-[8px] px-2 py-0.5">
          {{ resolveMimeGroup(item.mimeType) }}
        </UBadge>
        <p class="mt-1 truncate text-xs">{{ item.mimeType }}</p>
      </div>

      <div class="text-slate-600 dark:text-slate-300">{{ formatFileSize(item.sizeBytes) }}</div>
      <div class="text-slate-600 dark:text-slate-300">{{ formatChineseDateTime(item.createdTime, '-') }}</div>
      <div class="min-w-0 text-slate-600 dark:text-slate-300">
        <AdminTextHoverTip :text="item.objectKey" tooltip-max-width-class="max-w-[460px]" />
      </div>
      <div class="truncate text-xs text-slate-600 dark:text-slate-300">{{ item.sha256 }}</div>

      <div class="flex items-center gap-2">
        <AdminActionIconButton
          icon="i-lucide-copy"
          tooltip="复制链接"
          @click="copyAccessUrl(item)"
        />
        <AdminActionIconButton
          v-if="isSelectMode"
          icon="i-lucide-check"
          tooltip="选择该附件"
          @click="handleSelect(item)"
        />
        <AdminActionIconButton
          v-else
          icon="i-lucide-trash-2"
          tooltip="删除"
          @click="handleDelete(item)"
        />
      </div>
    </div>
  </AdminListWorkbench>
</template>
