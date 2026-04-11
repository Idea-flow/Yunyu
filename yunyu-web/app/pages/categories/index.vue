<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuImage from '~/components/common/YunyuImage.vue'
import YunyuPoetryTypewriter from '~/components/common/YunyuPoetryTypewriter.vue'

/**
 * 前台分类列表页。
 * 作用：直接展示全部分类入口，让用户按内容方向进入阅读，不再额外增加搜索干扰。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-categories', async () => {
  return await siteContent.listCategories()
})

const categories = computed(() => data.value || [])

/**
 * 计算分类列表页首屏主视觉数据。
 * 作用：优先复用当前分类数据中的首个封面作为大图背景，让分类页与专题页保持统一的封面首屏节奏。
 */
const heroCategory = computed(() => categories.value[0] || null)

/**
 * 计算分类页首屏统计信息。
 * 作用：在封面图首屏中补充分类总量与覆盖文章数，让 Hero 承担概览信息而不是只做装饰。
 */
const heroStats = computed(() => {
  const totalCategories = categories.value.length
  const totalArticles = categories.value.reduce((sum, item) => sum + (item.articleCount || 0), 0)

  return [
    { label: '分类总数', value: totalCategories },
    { label: '内容方向', value: totalCategories },
    { label: '覆盖文章', value: totalArticles }
  ]
})

const featuredCategory = computed(() => categories.value[0] || null)
const secondaryCategories = computed(() => categories.value.slice(1))

useSeoMeta({
  title: '分类 - 云屿',
  description: '浏览云屿的全部分类入口。'
})
</script>

<template>
  <main class="min-h-screen bg-[linear-gradient(180deg,#f8fbff_0%,#ffffff_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#081120_100%)]">
    <YunyuHero
      :src="heroCategory?.coverUrl"
      :alt="heroCategory?.name || '云屿分类'"
      min-height-class="h-[min(62svh,32rem)] sm:h-[min(68svh,36rem)] lg:h-[min(74svh,42rem)]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
      content-width-class="max-w-5xl"
      starfield-variant="warm"
    >
      <template #center>
        <YunyuPoetryTypewriter variant="orange" />
      </template>

      <div class="inline-flex items-center rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-semibold uppercase tracking-[0.28em] text-white/90 backdrop-blur-md">
        Category Archive
      </div>

      <div class="mt-5 flex flex-wrap items-center gap-x-5 gap-y-3 text-white/86">
        <div
          v-for="stat in heroStats"
          :key="stat.label"
          class="flex items-baseline gap-2"
        >
          <p class="text-[0.72rem] font-medium tracking-[0.14em] text-white/58">{{ stat.label }}</p>
          <p class="text-base font-semibold text-white sm:text-lg">{{ stat.value }}</p>
        </div>
      </div>
    </YunyuHero>

    <section class="mx-auto max-w-[1360px] px-5 pb-10 pt-8 sm:px-8 lg:px-10">
      <NuxtLink
        v-if="featuredCategory"
        :to="`/categories/${featuredCategory.slug}`"
        class="group mt-8 grid gap-6 border-b border-slate-200/75 pb-8 dark:border-white/10 lg:grid-cols-[minmax(0,1.08fr)_320px]"
      >
        <div class="min-w-0">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-orange-500 dark:text-orange-300">
            Featured Category
          </p>
          <h2 class="mt-4 text-[clamp(2rem,1.6rem+1.2vw,2.8rem)] font-semibold leading-[1.04] tracking-[-0.04em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-orange-600 dark:text-slate-50 dark:group-hover:text-orange-200">
            {{ featuredCategory.name }}
          </h2>
          <p class="mt-4 max-w-3xl text-[1rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ featuredCategory.description }}
          </p>
          <div class="mt-6 flex items-center gap-3 text-[0.72rem] uppercase tracking-[0.16em] text-slate-500 dark:text-slate-400">
            <span>{{ featuredCategory.articleCount }} 篇文章</span>
            <span>按方向持续阅读</span>
          </div>
        </div>

        <YunyuImage
          :src="featuredCategory.coverUrl"
          :alt="featuredCategory.name"
          image-class="h-72 w-full object-cover transition duration-500 group-hover:scale-[1.02] lg:h-full"
          rounded-class="rounded-[28px]"
        />
      </NuxtLink>

      <div class="mt-8 grid gap-x-8 gap-y-6 md:grid-cols-2">
        <NuxtLink
          v-for="category in secondaryCategories"
          :key="category.slug"
          :to="`/categories/${category.slug}`"
          class="group grid gap-4 border-b border-slate-200/75 pb-6 transition hover:border-orange-200 dark:border-white/10 dark:hover:border-orange-900 sm:grid-cols-[180px_minmax(0,1fr)]"
        >
          <YunyuImage
            :src="category.coverUrl"
            :alt="category.name"
            image-class="h-44 w-full object-cover transition duration-500 group-hover:scale-[1.02] sm:h-full"
            rounded-class="rounded-[24px]"
          />
          <div class="min-w-0 py-1">
            <div class="flex items-center justify-between gap-3">
              <h2 class="text-[clamp(1.16rem,1.05rem+0.4vw,1.45rem)] font-semibold leading-[1.16] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-orange-600 dark:text-slate-50 dark:group-hover:text-orange-200">{{ category.name }}</h2>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ category.articleCount }} 篇</span>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ category.description }}</p>
          </div>
        </NuxtLink>
      </div>

      <div
        v-if="!categories.length"
        class="mt-6 rounded-[28px] border border-dashed border-slate-200 bg-white/70 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-slate-950/50 dark:text-slate-400"
      >
        暂时还没有可展示的分类。
      </div>
    </section>
  </main>
</template>
