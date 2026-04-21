<script setup lang="ts">
/**
 * 后台列表工作台组件。
 * 作用：统一封装筛选条、数据表格和分页区，减少后台各列表页面的重复结构代码。
 */
const props = withDefaults(defineProps<{
  isLoading?: boolean
  hasData: boolean
  minWidth?: string
  headerClass: string
  emptyTitle: string
  emptyIcon?: string
  skeletonCount?: number
  page: number
  pageSize: number
  total: number
  totalPages: number
  showFilterBar?: boolean
  showPagination?: boolean
}>(), {
  isLoading: false,
  minWidth: '980px',
  emptyIcon: 'i-lucide-search-x',
  skeletonCount: 3,
  showFilterBar: true,
  showPagination: true
})

const emit = defineEmits<{
  'update:page': [page: number]
  'update:pageSize': [pageSize: number]
}>()

/**
 * 同步页码。
 *
 * @param page 新页码
 */
function handlePageChange(page: number) {
  emit('update:page', page)
}

/**
 * 同步每页条数。
 *
 * @param pageSize 新每页条数
 */
function handlePageSizeChange(pageSize: number) {
  emit('update:pageSize', pageSize)
}
</script>

<template>
  <div class="space-y-4">
    <AdminListFilterBar v-if="showFilterBar">
      <template #search>
        <slot name="search" />
      </template>

      <template #filters>
        <slot name="filters" />
      </template>

      <template #actions>
        <slot name="actions" />
      </template>
    </AdminListFilterBar>

    <section class="rounded-[16px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.76),rgba(255,255,255,0.58))] p-4 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.74),rgba(15,23,42,0.64))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <AdminDataTable
        :is-loading="isLoading"
        :has-data="hasData"
        :min-width="minWidth"
        :header-class="headerClass"
        :empty-title="emptyTitle"
        :empty-icon="emptyIcon"
        :skeleton-count="skeletonCount"
      >
        <template #header>
          <slot name="header" />
        </template>

        <slot />
      </AdminDataTable>

      <AdminPaginationBar
        v-if="showPagination"
        :page="page"
        :page-size="pageSize"
        :total="total"
        :total-pages="totalPages"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </section>
  </div>
</template>
