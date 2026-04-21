<script setup lang="ts">
import type { AdminAttachmentItem } from '../../../types/attachment'

/**
 * 附件测试上传组件。
 * 作用：在后台附件管理弹窗中提供单文件上传测试能力，展示上传结果并支持复制访问链接。
 */
const emit = defineEmits<{
  uploaded: [attachment: AdminAttachmentItem]
}>()

const toast = useToast()
const adminAttachments = useAdminAttachments()

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const isUploading = ref(false)
const uploadResult = ref<AdminAttachmentItem | null>(null)

/**
 * 触发文件选择。
 * 作用：打开系统文件选择窗口，供用户选择测试上传文件。
 */
function triggerFileSelect() {
  fileInputRef.value?.click()
}

/**
 * 处理文件选择事件。
 *
 * @param event 文件选择事件
 */
function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] || null
  selectedFile.value = file
  uploadResult.value = null
}

/**
 * 执行测试上传。
 * 作用：调用附件直传能力完成一次真实上传，并将结果回传给父页面。
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
      title: '测试上传成功',
      description: result.attachment.fileName,
      color: 'success'
    })
  } catch (error: any) {
    toast.add({
      title: '测试上传失败',
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

        <div class="flex flex-wrap items-center gap-2">
          <AdminButton
            icon="i-lucide-paperclip"
            label="选择文件"
            tone="neutral"
            variant="outline"
            @click="triggerFileSelect"
          />

          <span v-if="selectedFile" class="text-sm text-slate-600 dark:text-slate-300">
            {{ selectedFile.name }}（{{ formatFileSize(selectedFile.size) }}）
          </span>
          <span v-else class="text-sm text-slate-500 dark:text-slate-400">尚未选择文件</span>
        </div>

        <input
          ref="fileInputRef"
          type="file"
          class="hidden"
          @change="handleFileChange"
        >

        <div class="flex justify-end">
          <AdminPrimaryButton
            icon="i-lucide-upload"
            label="开始上传"
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
