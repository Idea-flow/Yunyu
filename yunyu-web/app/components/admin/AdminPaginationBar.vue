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
  <div class="flex flex-col gap-2 pt-1 lg:flex-row lg:items-center lg:justify-between">
    <p class="text-sm font-medium text-slate-500 dark:text-slate-400">
      {{ props.page }} / {{ props.totalPages }} 页
      <span class="ml-2 text-slate-400 dark:text-slate-500">每页 {{ props.pageSize }} 条</span>
    </p>

    <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-end">
      <div class="inline-flex items-center gap-2 px-1 py-0.5">
        <span class="text-xs font-medium text-slate-500 dark:text-slate-400">每页</span>
        <AdminSelect
          :model-value="props.pageSize"
          :items="props.pageSizeOptions"
          class="min-w-28"
          placeholder="每页条数"
          @update:model-value="handlePageSizeChange"
        />
      </div>

      <UPagination
        :key="`${props.pageSize}-${props.total}`"
        :page="props.page"
        :total="props.total"
        :items-per-page="props.pageSize"
        active-color="info"
        active-variant="solid"
        :ui="{
          list: 'flex items-center gap-1 rounded-[12px] border border-white/60 bg-white/44 px-1 py-1 backdrop-blur-md dark:border-white/10 dark:bg-white/[0.03]'
        }"
        @update:page="handlePageChange"
      >
        <template #item="{ item, page }">
          <UButton
            :color="page === item.value ? 'info' : 'neutral'"
            :variant="page === item.value ? 'solid' : 'ghost'"
            :label="String(item.value)"
            class="min-w-9 rounded-[10px] border-0"
            :class="page === item.value
              ? 'shadow-[0_10px_18px_-14px_rgba(14,165,233,0.7)]'
              : 'text-slate-500 hover:bg-white/80 dark:text-slate-300 dark:hover:bg-white/8'"
          />
        </template>
        <template #prev>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevron-left"
            class="rounded-[10px] border-0 text-slate-500 hover:bg-white/80 dark:text-slate-300 dark:hover:bg-white/8"
          />
        </template>
        <template #next>
          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-chevron-right"
            class="rounded-[10px] border-0 text-slate-500 hover:bg-white/80 dark:text-slate-300 dark:hover:bg-white/8"
          />
        </template>
      </UPagination>
    </div>
  </div>
</template>
