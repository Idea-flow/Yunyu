<script setup lang="ts">
import type { AdminAttachmentItem } from '../../types/attachment'

/**
 * 后台附件管理页。
 * 作用：提供附件列表查询、删除和测试上传入口，并复用通用附件库面板能力。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const toast = useToast()
const adminAttachments = useAdminAttachments()

const isDeleteSubmitting = ref(false)
const isDeleteModalOpen = ref(false)
const isUploadTesterModalOpen = ref(false)
const deletingAttachment = ref<AdminAttachmentItem | null>(null)
const reloadToken = ref(0)

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
function handleUploaded() {
  reloadToken.value += 1
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
    reloadToken.value += 1
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
            label="刷新"
            tone="neutral"
            variant="outline"
            @click="reloadToken += 1"
          />
        </div>
      </div>
    </section>

    <AdminAttachmentLibraryPanel
      mode="manage"
      :reload-token="reloadToken"
      @request-delete="openDeleteModal"
    />

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
        <AdminAttachmentUploader @uploaded="handleUploaded" />
      </template>
    </UModal>
  </div>
</template>
