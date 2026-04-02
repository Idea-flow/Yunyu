<script setup lang="ts">
/**
 * 后台分页条组件。
 * 作用：统一后台列表页的分页文案、每页条数切换和当前页高亮样式，
 * 便于用户、文章、分类、标签、专题等后台页面复用一致的分页交互。
 */
const props = withDefaults(defineProps<{
  page: number
  pageSize: number
  total: number
  totalPages: number
  pageSizeOptions?: Array<{ label: string, value: number }>
}>(), {
  pageSizeOptions: () => [
    { label: '2 条/页', value: 2 },
    { label: '5 条/页', value: 5 },
    { label: '10 条/页', value: 10 },
    { label: '20 条/页', value: 20 },
    { label: '50 条/页', value: 50 },
    { label: '100 条/页', value: 100 }
  ]
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [pageSize: number]
}>()

const jumpPageCount = 5

/**
 * 处理页码切换。
 * 作用：将分页组件选中的页码向父级透传，由具体页面决定何时刷新列表。
 *
 * @param page 新页码
 */
function handlePageChange(page: number) {
  emit('update:page', page)
}

/**
 * 向前跳转固定页数。
 * 作用：替代首尾页图标，提供更自然的快速翻页能力。
 */
function handleJumpBackward() {
  handlePageChange(Math.max(1, props.page - jumpPageCount))
}

/**
 * 向后跳转固定页数。
 * 作用：替代首尾页图标，提供更自然的快速翻页能力。
 */
function handleJumpForward() {
  handlePageChange(Math.min(props.totalPages, props.page + jumpPageCount))
}

/**
 * 处理每页条数切换。
 * 作用：将选择器中的条数转成数字后回传给父级，统一驱动分页容量变更。
 *
 * @param value 当前选择值
 */
function handlePageSizeChange(value: string | number | null) {
  const nextPageSize = Number(value)

  if (!Number.isFinite(nextPageSize) || nextPageSize < 1) {
    return
  }

  emit('update:pageSize', nextPageSize)
}
</script>

<template>
  <div class="flex flex-col gap-3 pt-1 lg:flex-row lg:items-center lg:justify-between">
    <div class="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
      <span class="font-medium text-slate-600 dark:text-slate-300">
        {{ props.page }} / {{ props.totalPages }} 页
      </span>
      <span class="text-slate-400 dark:text-slate-500">共 {{ props.total }} 条</span>
      <span class="text-slate-400 dark:text-slate-500">每页 {{ props.pageSize }} 条</span>
    </div>

    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
      <div class="inline-flex items-center gap-1.5">
        <span class="text-[11px] font-medium text-slate-400 dark:text-slate-500">每页</span>
        <AdminSelect
          :model-value="props.pageSize"
          :items="props.pageSizeOptions"
          compact
          class="min-w-18"
          placeholder="每页条数"
          @update:model-value="handlePageSizeChange"
        />
      </div>

      <UPagination
        :key="`${props.pageSize}-${props.total}`"
        :page="props.page"
        :total="props.total"
        :items-per-page="props.pageSize"
        active-color="neutral"
        active-variant="soft"
        :ui="{
          list: 'flex items-center gap-1 rounded-[16px] border border-white/70 bg-white/62 p-1 shadow-[0_18px_38px_-32px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-white/10 dark:bg-white/[0.045]'
        }"
        @update:page="handlePageChange"
      >
        <template #item="{ item, page }">
          <UButton
            color="neutral"
            :variant="page === item.value ? 'soft' : 'ghost'"
            :label="String(item.value)"
            class="inline-flex h-9 w-9 items-center justify-center rounded-[12px] border-0 px-0 text-center text-sm font-medium leading-none transition-all duration-200"
            :class="page === item.value
              ? 'bg-slate-900/[0.06] text-slate-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.72),0_10px_22px_-20px_rgba(15,23,42,0.24)] dark:bg-white/[0.09] dark:text-slate-100'
              : 'text-slate-500 hover:bg-white/78 hover:text-slate-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-slate-100'"
          />
        </template>
        <template #prev>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevron-left"
            class="min-h-9 min-w-9 rounded-[12px] border-0 text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          />
        </template>
        <template #next>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevron-right"
            class="min-h-9 min-w-9 rounded-[12px] border-0 text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          />
        </template>
        <template #first>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevrons-left"
            :disabled="props.page <= 1"
            :title="`向前跳 ${jumpPageCount} 页`"
            class="min-h-9 min-w-9 rounded-[12px] border-0 text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 disabled:opacity-35 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
            @click="handleJumpBackward"
          />
        </template>
        <template #last>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevrons-right"
            :disabled="props.page >= props.totalPages"
            :title="`向后跳 ${jumpPageCount} 页`"
            class="min-h-9 min-w-9 rounded-[12px] border-0 text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 disabled:opacity-35 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
            @click="handleJumpForward"
          />
        </template>
      </UPagination>
    </div>
  </div>
</template>
