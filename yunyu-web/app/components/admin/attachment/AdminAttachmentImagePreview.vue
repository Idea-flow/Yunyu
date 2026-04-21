<script setup lang="ts">
/**
 * 后台附件图片预览组件。
 * 作用：在列表单元格中展示图片缩略图，并支持点击后全屏查看原图。
 */
const props = withDefaults(defineProps<{
  mimeType: string
  accessUrl: string
  fileName?: string
}>(), {
  fileName: ''
})

const isPreviewOpen = ref(false)

/**
 * 是否为图片附件。
 * 作用：仅在图片 MIME 类型下展示缩略图和预览能力。
 */
const isImage = computed(() => props.mimeType.startsWith('image/'))

/**
 * 打开全屏预览弹窗。
 */
function openPreview() {
  if (!isImage.value) {
    return
  }
  isPreviewOpen.value = true
}

/**
 * 同步预览弹窗开关状态。
 *
 * @param value 是否打开
 */
function handlePreviewOpenChange(value: boolean) {
  isPreviewOpen.value = value
}
</script>

<template>
  <div class="flex items-center">
    <button
      v-if="isImage"
      type="button"
      class="overflow-hidden rounded-[10px] border border-slate-200/80 bg-white/80 transition hover:border-slate-300 dark:border-white/15 dark:bg-white/[0.04] dark:hover:border-white/25"
      @click="openPreview"
    >
      <img
        :src="accessUrl"
        :alt="fileName || '图片预览'"
        class="h-12 w-12 object-cover"
        loading="lazy"
      >
    </button>

    <span v-else class="text-xs text-slate-400 dark:text-slate-500">--</span>
  </div>

  <UModal
    :open="isPreviewOpen"
    :title="fileName || '图片预览'"
    description="点击遮罩或按 ESC 关闭预览。"
    :ui="{
      overlay: 'bg-slate-950/80',
      content: 'w-screen max-w-none h-screen rounded-none border-0 bg-slate-950/95',
      header: 'px-6 py-4 border-b border-white/10',
      body: 'px-2 py-2'
    }"
    @update:open="handlePreviewOpenChange"
  >
    <template #body>
      <div class="flex h-[calc(100vh-6.5rem)] items-center justify-center">
        <img
          :src="accessUrl"
          :alt="fileName || '图片预览'"
          class="max-h-full max-w-full object-contain"
        >
      </div>
    </template>
  </UModal>
</template>
