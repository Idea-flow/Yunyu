<script setup lang="ts">
import type { AdminAttachmentItem } from '../../../types/attachment'

/**
 * 后台通用附件上传组件。
 * 作用：封装附件前端直传能力，支持点击选择与拖拽上传，并在上传完成后向父组件回传附件信息。
 */
const props = withDefaults(defineProps<{
  accept?: string
  uploadButtonLabel?: string
  emptyHint?: string
}>(), {
  accept: '',
  uploadButtonLabel: '开始上传',
  emptyHint: '点击选择文件，或将文件拖拽到此区域'
})

const emit = defineEmits<{
  uploaded: [attachment: AdminAttachmentItem]
}>()

const toast = useToast()
const adminAttachments = useAdminAttachments()

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isUploading = ref(false)
const uploadResult = ref<AdminAttachmentItem | null>(null)
const isDragOver = ref(false)

/**
 * 触发文件选择。
 * 作用：打开系统文件选择窗口，供用户手动选择待上传文件。
 */
function triggerFileSelect() {
  fileInputRef.value?.click()
}

/**
 * 根据文件列表设置当前选中文件。
 *
 * @param fileList 文件列表
 */
function setSelectedFile(fileList: FileList | null) {
  const file = fileList?.[0] || null
  selectedFile.value = file
  uploadResult.value = null
}

/**
 * 处理文件选择事件。
 *
 * @param event 文件选择事件
 */
function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  setSelectedFile(input.files)
}

/**
 * 处理拖拽进入事件。
 *
 * @param event 拖拽事件
 */
function handleDragOver(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = true
}

/**
 * 处理拖拽离开事件。
 */
function handleDragLeave() {
  isDragOver.value = false
}

/**
 * 处理拖拽放置事件。
 *
 * @param event 拖拽事件
 */
function handleDrop(event: DragEvent) {
  event.preventDefault()
  isDragOver.value = false
  setSelectedFile(event.dataTransfer?.files || null)
}

/**
 * 执行上传。
 * 作用：调用附件直传并把上传后的附件结果回传给父组件。
 */
async function handleUpload() {
  if (!selectedFile.value) {
    toast.add({ title: '请先选择文件', color: 'warning' })
    return
  }

  isUploading.value = true

  try {
    const result = await adminAttachments.uploadFile(selectedFile.value)
    uploadResult.value = result.attachment
    emit('uploaded', result.attachment)
    toast.add({
      title: '上传成功',
      description: result.attachment.fileName,
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: '上传失败',
      description: error?.message || '上传过程中发生异常。',
      color: 'error'
    })
  } finally {
    isUploading.value = false
  }
}

/**
 * 复制上传结果访问链接。
 */
async function copyResultUrl() {
  if (!uploadResult.value) {
    return
  }

  try {
    await navigator.clipboard.writeText(uploadResult.value.accessUrl)
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
</script>

<template>
  <div class="space-y-4">
    <section class="rounded-[14px] border border-slate-200/80 bg-white/78 p-4 dark:border-white/10 dark:bg-white/[0.04]">
      <div class="space-y-3">
        <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">选择上传文件</p>

        <div
          class="cursor-pointer rounded-[12px] border border-dashed px-4 py-6 text-center transition"
          :class="isDragOver
            ? 'border-sky-300 bg-sky-50/80 dark:border-sky-400/40 dark:bg-sky-400/10'
            : 'border-slate-300/80 bg-white/80 hover:border-slate-400/80 dark:border-white/15 dark:bg-white/[0.03] dark:hover:border-white/25'"
          @click="triggerFileSelect"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
          @drop="handleDrop"
        >
          <p class="text-sm text-slate-600 dark:text-slate-300">{{ emptyHint }}</p>
          <p v-if="selectedFile" class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">
            {{ selectedFile.name }}（{{ formatFileSize(selectedFile.size) }}）
          </p>
          <p v-else class="mt-2 text-xs text-slate-500 dark:text-slate-400">支持单文件上传</p>
        </div>

        <input
          ref="fileInputRef"
          type="file"
          :accept="accept"
          class="hidden"
          @change="handleFileChange"
        >

        <div class="flex justify-end">
          <AdminPrimaryButton
            icon="i-lucide-upload"
            :label="uploadButtonLabel"
            loading-label="上传中..."
            :loading="isUploading"
            @click="handleUpload"
          />
        </div>
      </div>
    </section>

    <section
      v-if="uploadResult"
      class="rounded-[14px] border border-emerald-200/80 bg-emerald-50/70 p-4 dark:border-emerald-400/20 dark:bg-emerald-400/10"
    >
      <div class="flex flex-col gap-2 text-sm">
        <p class="font-semibold text-emerald-700 dark:text-emerald-200">上传结果</p>
        <p class="text-slate-700 dark:text-slate-200">文件：{{ uploadResult.fileName }}</p>
        <p class="break-all text-xs text-slate-600 dark:text-slate-300">URL：{{ uploadResult.accessUrl }}</p>

        <div class="flex justify-end">
          <AdminButton
            icon="i-lucide-copy"
            label="复制链接"
            tone="neutral"
            variant="outline"
            @click="copyResultUrl"
          />
        </div>
      </div>
    </section>
  </div>
</template>
