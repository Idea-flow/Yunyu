<script setup lang="ts">
import YunyuHero from '~/components/common/YunyuHero.vue'

/**
 * 前台标签列表页。
 * 作用：直接展示全部标签入口，让用户按更细粒度的主题快速进入内容，不再额外增加搜索干扰。
 */
const siteContent = useSiteContent()

const { data } = await useAsyncData('site-tags', async () => {
  return await siteContent.listTags()
})

const tags = computed(() => data.value || [])

/**
 * 计算标签页覆盖文章数。
 * 作用：在首屏中补充标签所覆盖的内容规模，让 Hero 承担概览信息而不是只做装饰。
 */
const totalArticles = computed(() => {
  return tags.value.reduce((sum, tag) => sum + (tag.articleCount || 0), 0)
})

/**
 * 计算标签页首屏统计项。
 * 作用：把列表规模与浏览方式收口成统一数据源，避免模板内散落拼接逻辑。
 */
const heroStats = computed(() => {
  return [
    { label: '标签总数', value: tags.value.length },
    { label: '覆盖文章', value: totalArticles.value },
    { label: '浏览方式', value: '细分主题' }
  ]
})

/**
 * 计算首屏辅助提示文案。
 * 作用：在大字背景旁补一行代表性标签名称，让首屏气质与正文列表建立联系。
 */
const featuredTagNames = computed(() => {
  return tags.value.slice(0, 4).map(tag => tag.name).join(' · ') || 'Tag Archive'
})

/**
 * 计算标签页首屏描述文案。
 * 作用：把页面定位与当前标签规模整合成一句概览，避免模板层再做条件拼接。
 */
const heroDescription = computed(() => {
  if (tags.value.length > 0) {
    return `共整理了 ${tags.value.length} 个标签入口，适合先按细分主题扫一眼，再直接进入感兴趣的内容。`
  }

  return '标签数量不多时，完整展示会比搜索更直观，也更适合按兴趣快速进入内容。'
})

/**
 * 计算正文区概览文案。
 * 作用：让 Hero 以下的首个内容区继续承接同一套语气，而不是重新起一套说明块。
 */
const tagsCountText = computed(() => {
  return `共 ${tags.value.length} 个标签入口，适合按更细粒度的话题直接进入内容。`
})

const tagsPageBackgroundClass = 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef5ff_28%,#ffffff_76%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0b1220_44%,#020617_100%)]'
const tagsHeroBackgroundClass = 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef6ff_36%,#f8fafc_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0b1220_42%,#020617_100%)]'
const tagsHeroOverlayClass = 'bg-[linear-gradient(180deg,rgba(255,255,255,0.16)_0%,rgba(255,255,255,0.06)_18%,rgba(255,255,255,0)_36%),radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.14),transparent_24%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.12),transparent_24%),linear-gradient(90deg,rgba(148,163,184,0.1)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.1)_1px,transparent_1px)] bg-[size:auto,auto,auto,32px_32px,32px_32px] [mask-image:linear-gradient(180deg,rgba(255,255,255,1),rgba(255,255,255,0.96)_58%,rgba(255,255,255,0.56)_100%)] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.04)_0%,rgba(255,255,255,0.015)_20%,rgba(255,255,255,0)_42%),radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.12),transparent_24%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.08),transparent_24%),linear-gradient(90deg,rgba(71,85,105,0.18)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.18)_1px,transparent_1px)] dark:bg-[size:auto,auto,auto,32px_32px,32px_32px]'

useSeoMeta({
  title: '标签 - 云屿',
  description: '浏览云屿的全部标签入口。'
})
</script>

<template>
  <main class="min-h-screen" :class="tagsPageBackgroundClass">
    <YunyuHero
      :show-starry="false"
      :background-class="tagsHeroBackgroundClass"
      :overlay-class="tagsHeroOverlayClass"
      min-height-class="h-[min(72svh,40rem)] sm:h-[min(78svh,44rem)] lg:h-[min(84svh,48rem)]"
      content-width-class="max-w-[1360px]"
      center-width-class="max-w-[1360px]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
    >
      <template #center>
        <div class="hidden lg:block">
          <div class="ml-auto max-w-[34rem] text-right">
            <p class="text-[clamp(5.4rem,11vw,9.4rem)] font-semibold leading-none tracking-[-0.08em] text-slate-950/[0.08] [font-family:var(--font-display)] dark:text-white/[0.06]">
              TAGS
            </p>
            <p class="mt-4 text-[0.72rem] font-semibold uppercase tracking-[0.32em] text-slate-500 dark:text-slate-400">
              {{ featuredTagNames }}
            </p>
          </div>
        </div>
      </template>

      <div class="grid gap-10 lg:grid-cols-[minmax(0,0.94fr)_minmax(0,0.72fr)] lg:items-end">
        <div class="max-w-[760px]">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-emerald-600 dark:text-emerald-300">
            Tag Archive
          </p>
          <h1 class="mt-4 text-[clamp(2.3rem,1.8rem+1.9vw,4.4rem)] font-semibold leading-[0.98] tracking-[-0.055em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
            按更细的主题，直接进入内容
          </h1>
          <p class="mt-5 max-w-[39rem] text-[0.96rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ heroDescription }}
          </p>

          <div class="mt-7 flex flex-wrap gap-3">
            <a
              href="#tag-list"
              class="inline-flex items-center gap-2 rounded-full bg-slate-950 px-5 py-2.5 text-sm font-medium text-white transition duration-200 hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
            >
              <span>查看标签列表</span>
              <UIcon name="i-lucide-arrow-down" class="size-4" />
            </a>
            <NuxtLink
              to="/topics"
              class="inline-flex items-center gap-2 rounded-full border border-slate-200/80 bg-white/72 px-5 py-2.5 text-sm font-medium text-slate-700 transition duration-200 hover:border-slate-300 hover:bg-white hover:text-slate-950 dark:border-white/12 dark:bg-white/[0.05] dark:text-slate-200 dark:hover:bg-white/[0.08] dark:hover:text-white"
            >
              <span>浏览专题</span>
            </NuxtLink>
          </div>
        </div>

        <div class="flex flex-wrap gap-x-6 gap-y-3 lg:justify-end lg:text-right">
          <div
            v-for="stat in heroStats"
            :key="stat.label"
            class="flex items-baseline gap-2"
          >
            <p class="text-[0.72rem] font-medium tracking-[0.14em] text-slate-500 dark:text-slate-400">{{ stat.label }}</p>
            <p class="text-base font-semibold text-slate-950 sm:text-lg dark:text-slate-50">{{ stat.value }}</p>
          </div>
        </div>
      </div>
    </YunyuHero>

    <section id="tag-list" class="mx-auto max-w-[1360px] px-5 pb-12 pt-6 sm:px-8 lg:px-10 lg:pt-8">
      <div class="border-t border-slate-200/75 pt-8 dark:border-white/10">
        <div class="max-w-3xl">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.28em] text-emerald-500 dark:text-emerald-300">
            Tag Index
          </p>
          <h2 class="mt-3 text-[clamp(1.9rem,1.55rem+0.9vw,2.7rem)] font-semibold leading-[1.02] tracking-[-0.04em] [font-family:var(--font-display)] text-slate-950 dark:text-slate-50">
            全部标签
          </h2>
          <p class="mt-4 text-[0.96rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ tagsCountText }}
          </p>
        </div>

        <div v-if="tags.length > 0" class="mt-10 grid gap-x-8 gap-y-6 md:grid-cols-2 xl:grid-cols-3">
          <NuxtLink
            v-for="tag in tags"
            :key="tag.slug"
            :to="`/tags/${tag.slug}`"
            class="group grid min-h-[164px] grid-rows-[auto_1fr_auto] gap-4 border-b border-slate-200/75 pb-6 transition duration-200 hover:border-emerald-200 dark:border-white/10 dark:hover:border-emerald-900"
          >
            <div class="flex items-center justify-between gap-3">
              <p class="text-[0.68rem] font-semibold uppercase tracking-[0.18em] text-emerald-600 dark:text-emerald-300">
                Tag
              </p>
              <span class="text-xs text-slate-500 dark:text-slate-400">{{ tag.articleCount }} 篇</span>
            </div>

            <div class="min-w-0">
              <h3 class="text-[clamp(1.18rem,1.06rem+0.36vw,1.42rem)] font-semibold leading-[1.12] tracking-[-0.03em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 transition group-hover:text-emerald-600 dark:text-slate-50 dark:group-hover:text-emerald-200">
                {{ tag.name }}
              </h3>
              <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                {{ tag.description || '从这个标签进入，查看相关主题下的全部文章。' }}
              </p>
            </div>

            <div class="flex items-center gap-2 text-sm text-slate-500 transition group-hover:text-slate-950 dark:text-slate-400 dark:group-hover:text-slate-50">
              <span>进入标签</span>
              <UIcon name="i-lucide-arrow-right" class="size-4 transition duration-200 group-hover:translate-x-0.5" />
            </div>
          </NuxtLink>
        </div>

        <div
          v-else
          class="mt-10 rounded-[28px] border border-dashed border-slate-200/80 bg-white/42 px-6 py-12 text-center text-sm text-slate-500 dark:border-white/10 dark:bg-white/[0.03] dark:text-slate-400"
        >
          暂时还没有可展示的标签。
        </div>
      </div>
    </section>
  </main>
</template>
