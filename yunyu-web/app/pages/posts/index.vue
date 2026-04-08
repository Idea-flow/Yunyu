<script setup lang="ts">
import PostCoverHero from '~/components/common/PostCoverHero.vue'
import FrontPostCard from '../../components/content/FrontPostCard.vue'
import YunyuPagination from '../../components/common/YunyuPagination.vue'

/**
 * 前台文章列表页。
 * 作用：承接前台公开文章列表浏览、关键词搜索、标签筛选和按页查看能力。
 */
const route = useRoute()
const router = useRouter()
const siteContent = useSiteContent()
const ALL_TAG_VALUE = '__ALL_TAGS__'
const isSearchModalOpen = ref(false)
const searchKeyword = ref(typeof route.query.keyword === 'string' ? route.query.keyword : '')
const draftKeyword = ref(searchKeyword.value)
const selectedTagSlug = computed(() => typeof route.query.tagSlug === 'string' ? route.query.tagSlug : '')
const draftTagValue = ref(selectedTagSlug.value || ALL_TAG_VALUE)

const { data: tagOptions } = await useAsyncData('site-tags-options', async () => {
  return await siteContent.listTags()
})

const pageNo = computed(() => {
  const value = Number(route.query.pageNo || 1)
  return Number.isNaN(value) || value < 1 ? 1 : value
})

const { data, pending } = await useAsyncData(
  () => `site-posts-${route.fullPath}`,
  async () => {
    return await siteContent.listPosts({
      keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
      tagSlug: selectedTagSlug.value || undefined,
      pageNo: pageNo.value,
      pageSize: 10
    })
  },
  {
    watch: [() => route.fullPath]
  }
)

const postList = computed(() => data.value)
const currentTag = computed(() => tagOptions.value?.find(tag => tag.slug === selectedTagSlug.value) || null)
const heroCoverUrl = computed(() => {
  const list = postList.value?.list || []
  const firstCoverPost = list.find(post => typeof post.coverUrl === 'string' && post.coverUrl.trim())
  return firstCoverPost?.coverUrl?.trim() || ''
})

watch(() => route.query.keyword, value => {
  searchKeyword.value = typeof value === 'string' ? value : ''
  draftKeyword.value = searchKeyword.value
})

watch(selectedTagSlug, value => {
  draftTagValue.value = value || ALL_TAG_VALUE
})

useSeoMeta({
  title: () => currentTag.value ? `${currentTag.value.name} - 文章列表 - 云屿` : '文章列表 - 云屿',
  description: () => currentTag.value?.description || '浏览云屿前台文章列表与最新内容。'
})

/**
 * 执行搜索。
 * 作用：把当前关键词同步到路由查询参数，触发服务端列表重新加载。
 */
async function handleSearch() {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/posts',
    query: {
      ...(keyword ? { keyword } : {}),
      ...(selectedTagSlug.value ? { tagSlug: selectedTagSlug.value } : {}),
      pageNo: 1
    }
  })
}

/**
 * 打开搜索弹窗。
 * 作用：在不占用文章列表首屏空间的前提下，为用户提供关键词和标签筛选入口。
 */
function openSearchModal() {
  draftKeyword.value = searchKeyword.value
  draftTagValue.value = selectedTagSlug.value || ALL_TAG_VALUE
  isSearchModalOpen.value = true
}

/**
 * 应用弹窗筛选条件。
 * 作用：把弹窗中的关键词与标签同步到路由参数，再关闭弹窗并刷新文章列表。
 */
async function applySearchFilters() {
  searchKeyword.value = draftKeyword.value
  await handleTagChange(draftTagValue.value === ALL_TAG_VALUE ? '' : draftTagValue.value)
  isSearchModalOpen.value = false
}

/**
 * 清空当前筛选。
 * 作用：快速恢复文章列表为“全部文章”状态，并重置弹窗中的输入项。
 */
async function clearSearchFilters() {
  draftKeyword.value = ''
  draftTagValue.value = ALL_TAG_VALUE
  searchKeyword.value = ''

  await router.push({
    path: '/posts',
    query: {
      pageNo: 1
    }
  })

  isSearchModalOpen.value = false
}

/**
 * 切换标签筛选。
 * 作用：把标签筛选同步到路由参数，并让文章列表回到第一页重新查询。
 *
 * @param tagSlug 标签 slug
 */
async function handleTagChange(tagSlug: string) {
  const keyword = searchKeyword.value.trim()

  await router.push({
    path: '/posts',
    query: {
      ...(keyword ? { keyword } : {}),
      ...(tagSlug ? { tagSlug } : {}),
      pageNo: 1
    }
  })
}

/**
 * 切换分页。
 * 作用：更新当前页码并保持现有搜索条件不丢失。
 *
 * @param nextPage 目标页码
 */
async function changePage(nextPage: number) {
  await router.push({
    path: '/posts',
    query: {
      ...(typeof route.query.keyword === 'string' && route.query.keyword ? { keyword: route.query.keyword } : {}),
      ...(selectedTagSlug.value ? { tagSlug: selectedTagSlug.value } : {}),
      pageNo: nextPage
    }
  })
}
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <PostCoverHero
      :src="heroCoverUrl"
      alt="文章列表封面"
      content-wrapper-class="absolute inset-x-0 bottom-0 z-10 flex h-full items-center justify-center"
      content-container-class="w-full max-w-[1440px] px-5 py-10 sm:px-8 sm:py-14 lg:px-10"
    >
      <div class="mx-auto w-full max-w-3xl">
        <div class="mx-auto flex w-full max-w-2xl items-center gap-2 rounded-[24px] bg-black/12 p-2 shadow-[0_18px_42px_-34px_rgba(15,23,42,0.34)] backdrop-blur-[14px]">
          <UInput
            v-model="searchKeyword"
            variant="none"
            size="xl"
            placeholder="搜索标题、摘要或关键词"
            class="w-full"
            :ui="{
              base: 'h-12 border-0 bg-transparent px-4 text-base text-white shadow-none placeholder:text-white/60 focus-visible:ring-0 dark:text-white dark:placeholder:text-white/54'
            }"
            @keyup.enter="handleSearch"
          />

          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-sliders-horizontal"
            aria-label="打开高级筛选"
            class="rounded-full text-white hover:bg-white/10 hover:text-white"
            @click="openSearchModal"
          />

          <UButton
            color="neutral"
            variant="ghost"
            icon="i-lucide-search"
            aria-label="搜索文章"
            class="rounded-full text-white hover:bg-white/10 hover:text-white"
            :loading="pending"
            @click="handleSearch"
          />
        </div>

        <div v-if="pending" class="mt-4 flex justify-center">
          <span class="rounded-full border border-white/16 bg-black/16 px-4 py-1.5 text-xs font-medium text-white/90 backdrop-blur-[12px]">
            正在更新文章列表...
          </span>
        </div>
      </div>
    </PostCoverHero>

    <section class="mx-auto max-w-[1360px] px-5 py-8 sm:px-8 lg:px-10">

      <div class="mt-6 space-y-4">
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />
        <USkeleton v-if="pending" class="h-48 rounded-[28px]" />

        <FrontPostCard
          v-for="post in postList?.list || []"
          :key="post.slug"
          :post="post"
        />

        <div
          v-if="!pending && !(postList?.list?.length)"
          class="rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
        >
          当前筛选条件下暂无文章，换个关键词或标签试试看。
        </div>
      </div>

      <YunyuPagination
        v-if="postList"
        class="mt-8"
        :page-no="postList.pageNo"
        :total-pages="postList.totalPages"
        @change="changePage"
      />
    </section>

    <UModal
      v-model:open="isSearchModalOpen"
      :ui="{ content: 'sm:max-w-lg rounded-[24px] border border-white/60 bg-white/95 dark:border-white/10 dark:bg-slate-950/95' }"
    >
      <template #content>
        <div class="p-5 sm:p-6">
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">高级筛选</p>
              <h2 class="mt-2 text-2xl font-semibold tracking-[-0.04em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
                关键词与标签筛选
              </h2>
              <p class="mt-3 text-sm leading-7 text-slate-500 dark:text-slate-400">
                通过关键词和标签快速缩小文章范围。
              </p>
            </div>

            <UButton
              color="neutral"
              variant="ghost"
              icon="i-lucide-x"
              class="rounded-full"
              @click="isSearchModalOpen = false"
            />
          </div>

          <div class="mt-6 space-y-4">
            <div>
              <p class="mb-2 text-sm font-medium text-slate-700 dark:text-slate-200">关键词</p>
              <UInput
                v-model="draftKeyword"
                size="xl"
                placeholder="搜索标题或摘要"
                class="w-full"
                @keyup.enter="applySearchFilters"
              />
            </div>

            <div>
              <p class="mb-2 text-sm font-medium text-slate-700 dark:text-slate-200">标签</p>
              <USelect
                v-model="draftTagValue"
                :items="[
                  { label: '全部标签', value: ALL_TAG_VALUE },
                  ...((tagOptions || []).map(tag => ({ label: `${tag.name} (${tag.articleCount})`, value: tag.slug })))
                ]"
                size="xl"
                placeholder="按标签筛选"
                class="w-full"
              />
            </div>
          </div>

          <div class="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
            <UButton
              color="neutral"
              variant="ghost"
              class="justify-center rounded-full px-5"
              @click="clearSearchFilters"
            >
              清空筛选
            </UButton>
            <UButton
              color="primary"
              icon="i-lucide-search"
              class="justify-center rounded-full px-5"
              :loading="pending"
              @click="applySearchFilters"
            >
              应用筛选
            </UButton>
          </div>
        </div>
      </template>
    </UModal>
  </main>
</template>
