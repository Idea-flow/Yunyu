<script setup lang="ts">
import FriendLinkStoryScene from '~/components/friend-link/FriendLinkStoryScene.vue'
import YunyuHero from '~/components/common/YunyuHero.vue'
import YunyuSectionTitle from '~/components/common/YunyuSectionTitle.vue'
import type { SiteBaseInfo } from '../../types/site'

/**
 * 前台友链展示页。
 * 作用：集中展示已通过审核的友链站点，并为读者提供进入友链申请页的入口。
 */
const friendLinksApi = useFriendLinks()
const siteContent = useSiteContent()

const { data: friendLinksData } = await useAsyncData('site-friend-links', async () => {
  return await friendLinksApi.listFriendLinks()
})

const { data: siteConfigData } = await useAsyncData('site-friend-links-config', async () => {
  return await siteContent.getSiteConfig()
})

const friendLinks = computed(() => friendLinksData.value || [])
const siteConfig = computed<SiteBaseInfo | null>(() => siteConfigData.value || null)
const brandName = computed(() => siteConfig.value?.siteName || '云屿')
const brandSubtitle = computed(() => siteConfig.value?.siteSubTitle || '把值得长期互访的站点收进同一片风景')

/**
 * 计算友链页首屏统计项。
 * 作用：将页头展示信息统一收口，避免模板中再散落数字和说明拼接。
 */
const heroStats = computed(() => {
  return [
    { label: '收录站点', value: friendLinks.value.length },
    { label: '连接方式', value: '长期互访' },
    { label: '页面节奏', value: '慢慢逛' }
  ]
})

/**
 * 计算友链页首屏描述文案。
 * 作用：让首屏语气同时覆盖站点关系与浏览预期，不必在模板层再做条件描述。
 */
const heroDescription = computed(() => {
  if (friendLinks.value.length > 0) {
    return `这里收录了 ${friendLinks.value.length} 个正在稳定更新的小站，适合顺着兴趣一站一站地慢慢逛过去。`
  }

  return '这里会收录值得长期互访的小站，也欢迎你把自己的站点加入这片地图。'
})

const friendsPageBackgroundClass = 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef5ff_28%,#ffffff_76%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0b1220_44%,#020617_100%)]'
const friendsHeroBackgroundClass = 'bg-[linear-gradient(180deg,#f8fafc_0%,#eef6ff_36%,#f8fafc_100%)] dark:bg-[linear-gradient(180deg,#020617_0%,#0b1220_42%,#020617_100%)]'
const friendsHeroOverlayClass = 'bg-[linear-gradient(180deg,rgba(255,255,255,0.16)_0%,rgba(255,255,255,0.06)_18%,rgba(255,255,255,0)_36%),radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.14),transparent_24%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.12),transparent_24%),linear-gradient(90deg,rgba(148,163,184,0.1)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.1)_1px,transparent_1px)] bg-[size:auto,auto,auto,32px_32px,32px_32px] [mask-image:linear-gradient(180deg,rgba(255,255,255,1),rgba(255,255,255,0.96)_58%,rgba(255,255,255,0.56)_100%)] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.04)_0%,rgba(255,255,255,0.015)_20%,rgba(255,255,255,0)_42%),radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.12),transparent_24%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.08),transparent_24%),linear-gradient(90deg,rgba(71,85,105,0.18)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.18)_1px,transparent_1px)] dark:bg-[size:auto,auto,auto,32px_32px,32px_32px]'

useSeoMeta({
  title: () => `友链 - ${brandName.value}`,
  description: () => `${brandSubtitle.value}。浏览 ${brandName.value} 精选的友链站点，并申请加入友链地图。`
})

/**
 * 构建友链卡片强调样式。
 * 作用：根据友链主题色生成卡片左侧色带、阴影和按钮强调色。
 *
 * @param themeColor 主题色
 * @returns 卡片样式对象
 */
function buildCardStyle(themeColor: string) {
  return {
    '--friend-link-accent': themeColor || '#7CC6B8',
    '--friend-link-accent-soft': `${themeColor || '#7CC6B8'}18`
  }
}

/**
 * 读取站点名称首字母。
 * 作用：当友链未提供 Logo 时，为卡片生成更有识别度的占位头像。
 *
 * @param siteName 站点名称
 * @returns 首字母占位
 */
function resolveSiteInitial(siteName: string) {
  return siteName.trim().slice(0, 1).toUpperCase() || 'L'
}
</script>

<template>
  <main class="min-h-screen overflow-hidden pb-24" :class="friendsPageBackgroundClass">
    <YunyuHero
      :show-starry="false"
      :background-class="friendsHeroBackgroundClass"
      :overlay-class="friendsHeroOverlayClass"
      min-height-class="h-[min(78svh,46rem)] sm:h-[min(84svh,50rem)] lg:h-[min(88svh,52rem)]"
      content-width-class="max-w-[1360px]"
      center-width-class="max-w-[1360px]"
      content-padding-class="px-5 pb-8 sm:px-8 sm:pb-10 lg:px-10 lg:pb-12"
    >
      <template #center>
        <div class="hidden lg:block">
          <div class="mx-auto grid max-w-[1360px] items-center gap-10 lg:grid-cols-[minmax(0,0.98fr)_420px]">
            <div />
            <div class="relative mx-auto w-full max-w-[420px] justify-self-end">
              <div class="absolute inset-x-8 top-10 h-[72%] rounded-full bg-[radial-gradient(circle,rgba(125,198,184,0.16),transparent_72%)] blur-3xl dark:bg-[radial-gradient(circle,rgba(56,189,248,0.12),transparent_72%)]" />
              <div class="relative overflow-hidden rounded-[32px] border border-white/58 bg-white/40 px-4 py-5 shadow-[0_26px_64px_-46px_rgba(15,23,42,0.14)] backdrop-blur-sm dark:border-white/10 dark:bg-white/[0.03]">
                <FriendLinkStoryScene compact />
              </div>
            </div>
          </div>
        </div>
      </template>

      <div class="grid gap-10 lg:grid-cols-[minmax(0,0.98fr)_420px] lg:items-end">
        <div class="max-w-[720px]">
          <p class="text-[0.72rem] font-semibold uppercase tracking-[0.34em] text-sky-600 dark:text-sky-300">
            {{ brandName }} / Friends
          </p>
          <h1 class="mt-4 text-[clamp(2.35rem,1.82rem+2vw,4.4rem)] font-semibold leading-[0.98] tracking-[-0.06em] [font-family:var(--font-display)] [text-wrap:balance] text-slate-950 dark:text-slate-50">
            友链列表
          </h1>
          <p class="mt-5 max-w-[38rem] text-[0.96rem] leading-8 text-slate-600 dark:text-slate-300">
            {{ heroDescription }}
          </p>

          <div class="mt-7 flex flex-wrap gap-3">
            <NuxtLink
              to="/friends/apply"
              class="inline-flex items-center gap-2 rounded-full bg-slate-950 px-5 py-2.5 text-sm font-medium text-white transition duration-200 hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-200"
            >
              <span>申请交换友链</span>
              <UIcon name="i-lucide-arrow-right" class="size-4" />
            </NuxtLink>
            <a
              href="#friends-list"
              class="inline-flex items-center gap-2 rounded-full border border-slate-200/80 bg-white/72 px-5 py-2.5 text-sm font-medium text-slate-700 transition duration-200 hover:border-slate-300 hover:bg-white hover:text-slate-950 dark:border-white/12 dark:bg-white/[0.05] dark:text-slate-200 dark:hover:bg-white/[0.08] dark:hover:text-white"
            >
              <span>浏览友链</span>
            </a>
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

    <section id="friends-list" class="mx-auto max-w-[1360px] px-5 py-12 sm:px-8 lg:px-10 lg:py-14">
      <section class="border-t border-slate-200/80 pt-10 dark:border-white/10">
        <div class="flex flex-wrap items-center justify-between gap-4">
          <YunyuSectionTitle
            eyebrow="Friends"
            title="友链列表"
            description=""
            size="compact"
            tone="slate"
          />
          <UButton
            to="/friends/apply"
            label="提交申请"
            color="neutral"
            variant="soft"
            class="rounded-full px-6"
          />
        </div>

        <div v-if="friendLinks.length > 0" class="mt-10 grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          <a
            v-for="item in friendLinks"
            :key="item.id"
            :href="item.siteUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="group grid min-h-[220px] grid-rows-[auto_1fr_auto] gap-5 rounded-[28px] border border-slate-200/72 bg-white/46 p-6 shadow-[0_24px_50px_-44px_rgba(15,23,42,0.12)] backdrop-blur-[6px] transition duration-300 hover:-translate-y-0.5 hover:border-slate-300/80 hover:bg-white/58 hover:shadow-[0_28px_56px_-40px_rgba(15,23,42,0.14)] dark:border-white/10 dark:bg-white/[0.03] dark:hover:bg-white/[0.05]"
            :style="buildCardStyle(item.themeColor)"
          >
            <div class="flex items-start justify-between gap-4">
              <div
                class="flex h-[68px] w-[68px] shrink-0 items-center justify-center overflow-hidden rounded-[22px] border border-white/70 text-xl font-semibold text-[var(--friend-link-accent)] shadow-[0_16px_36px_-28px_rgba(15,23,42,0.12)] dark:border-white/10"
                :style="{ backgroundImage: 'linear-gradient(135deg,var(--friend-link-accent-soft),rgba(255,255,255,0.92))' }"
              >
                <img
                  v-if="item.logoUrl"
                  :src="item.logoUrl"
                  :alt="item.siteName"
                  class="h-full w-full object-cover"
                >
                <span v-else>{{ resolveSiteInitial(item.siteName) }}</span>
              </div>

              <span
                class="inline-flex rounded-full px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.16em]"
                :style="{ color: item.themeColor, backgroundColor: `${item.themeColor}12` }"
              >
                访问
              </span>
            </div>

            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-x-4 gap-y-2 text-[0.68rem] uppercase tracking-[0.18em] text-slate-400 dark:text-slate-500">
                <span>{{ item.siteUrl.replace(/^https?:\/\//, '') }}</span>
              </div>

              <h3 class="mt-3 text-[1.18rem] font-semibold leading-7 tracking-[-0.035em] text-slate-950 [font-family:var(--font-display)] transition group-hover:text-slate-700 dark:text-slate-50 dark:group-hover:text-white">
                {{ item.siteName }}
              </h3>

              <p class="mt-3 text-sm leading-7 text-slate-600 dark:text-slate-300">
                {{ item.description || '这个站点正在以自己的节奏认真更新。' }}
              </p>
            </div>

            <div class="flex items-center justify-between gap-3 border-t border-slate-200/70 pt-4 dark:border-white/10">
              <span class="text-sm text-slate-500 dark:text-slate-400">
                打开站点
              </span>
              <span
                class="inline-flex h-10 w-10 items-center justify-center rounded-full text-base text-slate-600 transition group-hover:translate-x-0.5 group-hover:text-slate-950 dark:text-slate-300 dark:group-hover:text-white"
              >
                →
              </span>
            </div>
          </a>
        </div>

        <div
          v-else
          class="mt-10 rounded-[30px] border border-dashed border-slate-300/80 bg-white/42 px-8 py-12 text-center dark:border-white/10 dark:bg-white/[0.03]"
        >
          <p class="text-base font-medium text-slate-900 dark:text-slate-50">这里还没有展示中的友链</p>
          <p class="mt-3 text-sm leading-7 text-slate-500 dark:text-slate-400">
            如果你也想把自己的小站放在这里，可以直接提交申请。
          </p>
          <UButton
            to="/friends/apply"
            label="去提交友链申请"
            color="neutral"
            variant="soft"
            class="mt-6 rounded-full px-6"
          />
        </div>
      </section>
    </section>
  </main>
</template>
