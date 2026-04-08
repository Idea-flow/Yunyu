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
const pageJumpValue = ref('')

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

/**
 * 跳转到指定页码。
 * 作用：读取输入框中的页码值，并在有效范围内执行跳转。
 */
function handlePageJump() {
  const nextPage = Number(pageJumpValue.value)

  if (!Number.isFinite(nextPage)) {
    pageJumpValue.value = ''
    return
  }

  handlePageChange(Math.min(props.totalPages, Math.max(1, Math.trunc(nextPage))))
  pageJumpValue.value = ''
}

/**
 * 同步跳页输入值。
 * 作用：只保留手动输入的数字字符，避免出现浏览器数字输入控件。
 *
 * @param value 当前输入值
 */
function handlePageJumpInput(value: string | number | null) {
  pageJumpValue.value = String(value ?? '').replace(/\D/g, '')
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

      <div class="inline-flex items-center gap-1.5">
        <span class="text-[11px] font-medium text-slate-400 dark:text-slate-500">跳至</span>
        <UInput
          :model-value="pageJumpValue"
          type="text"
          inputmode="numeric"
          placeholder="页码"
          size="lg"
          color="neutral"
          variant="outline"
          class="w-18"
          :ui="{
            base: 'h-8 rounded-[10px] border border-white/70 bg-white/62 px-2.5 text-center text-sm font-medium text-slate-700 shadow-[0_12px_28px_-26px_rgba(15,23,42,0.24)] backdrop-blur-xl transition-[border-color,box-shadow,background-color,color] duration-200 placeholder:text-slate-400 hover:border-slate-200/90 hover:bg-white/72 focus-visible:border-slate-300 focus-visible:ring-2 focus-visible:ring-slate-200/60 dark:border-white/10 dark:bg-white/[0.045] dark:text-slate-100 dark:placeholder:text-slate-500 dark:hover:bg-white/[0.06] dark:focus-visible:border-white/15 dark:focus-visible:ring-white/10'
          }"
          @update:model-value="handlePageJumpInput"
          @keyup.enter="handlePageJump"
          @blur="handlePageJump"
        />
      </div>

      <UPagination
        :key="`${props.pageSize}-${props.total}`"
        :page="props.page"
        :total="props.total"
        :items-per-page="props.pageSize"
        :show-controls="false"
        :show-edges="false"
        active-color="neutral"
        active-variant="soft"
        :ui="{
          list: 'flex items-center gap-1 rounded-[16px] border border-white/70 bg-white/62 p-1 shadow-[0_18px_38px_-32px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-white/10 dark:bg-white/[0.045]'
        }"
        @update:page="handlePageChange"
      >
        <template #item="{ item, page }">
          <AdminButton
            tone="neutral"
            :variant="page === item.value ? 'outline' : 'ghost'"
            :label="String(item.value)"
            square
            :class="page === item.value
              ? 'border-slate-200/80 bg-slate-900/[0.06] text-slate-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.72),0_10px_22px_-20px_rgba(15,23,42,0.24)] dark:border-white/10 dark:bg-white/[0.09] dark:text-slate-100'
              : 'border-transparent text-slate-500 hover:bg-white/78 hover:text-slate-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-slate-100'"
          />
        </template>
        <template #prev>
          <AdminButton
            tone="neutral"
            variant="ghost"
            icon="i-lucide-chevron-left"
            square
            class="border-transparent text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          />
        </template>
        <template #next>
          <AdminButton
            tone="neutral"
            variant="ghost"
            icon="i-lucide-chevron-right"
            square
            class="border-transparent text-slate-400 transition-colors duration-200 hover:bg-white/78 hover:text-slate-700 dark:text-slate-400 dark:hover:bg-white/8 dark:hover:text-slate-100"
          />
        </template>
      </UPagination>
    </div>
  </div>
</template>
