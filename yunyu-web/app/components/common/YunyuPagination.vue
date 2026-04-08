<script setup lang="ts">
/**
 * 前台通用分页组件。
 * 作用：统一前台文章、分类、专题、标签等列表页的分页交互与样式，避免页面内重复实现分页逻辑。
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
 * 作用：保留首页、末页和当前页附近的页码，让分页导航既紧凑又具备可跳转性。
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
 * 计算最终展示项。
 * 作用：在不连续页码之间补充省略号，减少分页条宽度占用。
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
 * 作用：统一拦截越界页码和重复点击，并向父级派发合法页码。
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
    class="flex flex-col items-center gap-3 text-sm text-slate-500 dark:text-slate-400"
  >
    <div class="text-xs font-medium text-slate-400 dark:text-slate-500 sm:hidden">
      第 {{ pageNo }} / {{ totalPages }} 页
    </div>

    <div class="flex w-full items-center justify-center gap-2 sm:hidden">
      <UButton
        variant="ghost"
        color="neutral"
        class="min-w-[92px] rounded-full px-3 py-2 text-sm text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-300"
        :disabled="pageNo <= 1"
        @click="changePage(pageNo - 1)"
      >
        上一页
      </UButton>

      <div class="inline-flex min-w-[88px] items-center justify-center rounded-full border border-slate-200/80 bg-white/80 px-4 py-2 text-sm font-semibold text-slate-700 shadow-[0_14px_30px_-24px_rgba(15,23,42,0.28)] dark:border-white/10 dark:bg-slate-950/60 dark:text-slate-100">
        {{ pageNo }}
      </div>

      <UButton
        variant="ghost"
        color="neutral"
        class="min-w-[92px] rounded-full px-3 py-2 text-sm text-slate-500 hover:text-sky-600 dark:text-slate-400 dark:hover:text-sky-300"
        :disabled="pageNo >= totalPages"
        @click="changePage(pageNo + 1)"
      >
        下一页
      </UButton>
    </div>

    <div class="hidden flex-wrap items-center justify-center gap-2 sm:flex">
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
  </div>
</template>
