<script setup lang="ts">
import FriendLinkStoryScene from '~/components/friend-link/FriendLinkStoryScene.vue'
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
    '--friend-link-accent-soft': `${themeColor || '#7CC6B8'}22`
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
  <main class="min-h-screen overflow-hidden bg-[linear-gradient(180deg,#f8fafc_0%,#f8fafc_100%)] pb-24 dark:bg-[linear-gradient(180deg,#020617_0%,#020617_100%)]">
    <section class="relative isolate overflow-hidden pt-28 sm:pt-32">
      <div class="absolute inset-0 -z-10 bg-[linear-gradient(180deg,rgba(250,250,249,0.98),rgba(248,250,252,0.98)),linear-gradient(90deg,rgba(148,163,184,0.08)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.08)_1px,transparent_1px)] bg-[size:auto,32px_32px,32px_32px] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.99),rgba(2,6,23,0.99)),linear-gradient(90deg,rgba(71,85,105,0.18)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.18)_1px,transparent_1px)] dark:bg-[size:auto,32px_32px,32px_32px]" />
      <div class="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.14),transparent_24%),radial-gradient(circle_at_84%_20%,rgba(249,115,22,0.1),transparent_24%)] dark:bg-[radial-gradient(circle_at_16%_18%,rgba(56,189,248,0.12),transparent_24%),radial-gradient(circle_at_84%_20%,rgba(249,115,22,0.08),transparent_24%)]" />

      <div class="mx-auto grid max-w-[1240px] items-center gap-10 px-5 sm:px-8 lg:grid-cols-[minmax(0,0.98fr)_420px] lg:px-10">
        <div class="max-w-[680px]">
          <p class="text-[0.69rem] font-semibold uppercase tracking-[0.36em] text-slate-500 dark:text-slate-400">
            {{ brandName }} / 友链
          </p>
          <h1 class="mt-5 text-[clamp(2.5rem,1.9rem+2.2vw,4.4rem)] font-semibold leading-[0.98] tracking-[-0.065em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] dark:text-white">
            友链列表
          </h1>
          <p class="mt-5 max-w-[34rem] text-[0.95rem] leading-8 text-slate-600 dark:text-slate-300">
            一些正在稳定更新的小站。
          </p>

          <div class="mt-8 flex flex-wrap items-center gap-5">
            <p class="text-sm text-slate-500 dark:text-slate-400">
              当前已收录 {{ friendLinks.length }} 个站点
            </p>
            <UButton
              to="/friends/apply"
              label="申请交换友链"
              color="neutral"
              variant="soft"
              class="rounded-full px-6"
            />
          </div>
        </div>

        <div class="relative mx-auto hidden w-full max-w-[420px] lg:justify-self-end lg:block">
          <div class="absolute inset-x-8 top-10 h-[72%] rounded-full bg-[radial-gradient(circle,rgba(125,198,184,0.16),transparent_72%)] blur-3xl" />
          <div class="relative overflow-hidden rounded-[32px] bg-[linear-gradient(180deg,rgba(255,255,255,0.64),rgba(248,250,252,0.34))] px-4 py-5 shadow-[0_24px_56px_-44px_rgba(15,23,42,0.12)] backdrop-blur-md dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.04),rgba(255,255,255,0.015))]">
            <FriendLinkStoryScene compact />
          </div>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1240px] px-5 py-14 sm:px-8 lg:px-10 lg:py-16">
      <section class="border-t border-slate-200/80 pt-14 dark:border-white/10">
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
            class="group grid min-h-[220px] grid-rows-[auto_1fr_auto] gap-5 rounded-[28px] bg-[linear-gradient(180deg,rgba(255,255,255,0.82),rgba(248,250,252,0.74))] p-6 shadow-[0_24px_50px_-40px_rgba(15,23,42,0.18)] transition duration-300 hover:-translate-y-0.5 hover:shadow-[0_30px_56px_-38px_rgba(15,23,42,0.22)] dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.025))]"
            :style="buildCardStyle(item.themeColor)"
          >
            <div class="flex items-start justify-between gap-4">
              <div
                class="flex h-[68px] w-[68px] shrink-0 items-center justify-center overflow-hidden rounded-[22px] text-xl font-semibold text-[var(--friend-link-accent)] shadow-[0_16px_36px_-28px_rgba(15,23,42,0.16)]"
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
                :style="{ color: item.themeColor, backgroundColor: `${item.themeColor}14` }"
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
          class="mt-10 rounded-[30px] border border-dashed border-slate-300/80 bg-white/50 px-8 py-12 text-center dark:border-white/10 dark:bg-white/[0.03]"
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
