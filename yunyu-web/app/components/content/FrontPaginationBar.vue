<script setup lang="ts">
/**
 * 前台分页条组件。
 * 作用：统一前台公开页的轻量分页交互样式，避免文章、分类、专题和标签页重复散写分页控件。
 */
const props = withDefaults(defineProps<{
  pageNo: number
  totalPages: number
}>(), {
  pageNo: 1,
  totalPages: 1
})

const emit = defineEmits<{
  change: [pageNo: number]
}>()

/**
 * 计算需要展示的页码列表。
 * 作用：在保持分页简洁的同时，让当前页附近的跳转入口始终可见。
 */
const visiblePages = computed(() => {
  const pages = new Set<number>()
  const maxPage = Math.max(props.totalPages, 1)

  pages.add(1)
  pages.add(maxPage)

  for (let page = props.pageNo - 2; page <= props.pageNo + 2; page += 1) {
    if (page >= 1 && page <= maxPage) {
      pages.add(page)
    }
  }

  return Array.from(pages).sort((left, right) => left - right)
})

/**
 * 计算带省略号的展示项。
 * 作用：把不连续页码折叠成省略展示，减少分页横向占用。
 */
const pageItems = computed<(number | string)[]>(() => {
  const items: (number | string)[] = []

  visiblePages.value.forEach((page, index) => {
    const previousPage = visiblePages.value[index - 1]
    if (previousPage && page - previousPage > 1) {
      items.push(`ellipsis-${previousPage}-${page}`)
    }
    items.push(page)
  })

  return items
})

/**
 * 切换到指定页。
 * 作用：对外派发页码变化事件，并拦截无效页码点击。
 *
 * @param targetPage 目标页码
 */
function changePage(targetPage: number) {
  if (targetPage < 1 || targetPage > props.totalPages || targetPage === props.pageNo) {
    return
  }

  emit('change', targetPage)
}
</script>

<template>
  <div
    v-if="totalPages > 1"
    class="flex flex-wrap items-center justify-center gap-2 text-sm text-slate-500 dark:text-slate-400"
  >
    <UButton
      variant="ghost"
      color="neutral"
      class="rounded-full px-3 text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-300"
      :disabled="pageNo <= 1"
      @click="changePage(pageNo - 1)"
    >
      上一页
    </UButton>

    <template v-for="item in pageItems" :key="String(item)">
      <span
        v-if="typeof item === 'string'"
        class="px-1 text-slate-300 dark:text-slate-600"
      >
        ...
      </span>

      <button
        v-else
        type="button"
        class="min-w-9 rounded-full px-3 py-2 text-sm font-medium transition"
        :class="item === pageNo
          ? 'bg-slate-900 text-white shadow-[0_16px_30px_-18px_rgba(15,23,42,0.45)] dark:bg-slate-100 dark:text-slate-900'
          : 'text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-300'"
        @click="changePage(item)"
      >
        {{ item }}
      </button>
    </template>

    <UButton
      variant="ghost"
      color="neutral"
      class="rounded-full px-3 text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-300"
      :disabled="pageNo >= totalPages"
      @click="changePage(pageNo + 1)"
    >
      下一页
    </UButton>
  </div>
</template>
