<script setup lang="ts">
import FriendLinkStoryScene from '~/components/friend-link/FriendLinkStoryScene.vue'
import type { SiteFriendLinkApplyRequest } from '../../types/friend-link'
import type { SiteBaseInfo } from '../../types/site'

/**
 * 前台友链申请页。
 * 作用：以更有记忆点的插画式页面承接友链申请表单，让友链交换过程更像一次认真投递。
 */
const toast = useToast()
const friendLinksApi = useFriendLinks()
const siteContent = useSiteContent()
const isSubmitting = ref(false)
const themeColorPresets = ['#7CC6B8', '#5FA8FF', '#F2A65A', '#F4BF75', '#9BCB6D', '#E88F8F']

const { data: siteConfigData } = await useAsyncData('site-friend-links-apply-config', async () => {
  return await siteContent.getSiteConfig()
})

const siteConfig = computed<SiteBaseInfo | null>(() => siteConfigData.value || null)
const brandName = computed(() => siteConfig.value?.siteName || '云屿')

const formState = reactive<SiteFriendLinkApplyRequest>({
  siteName: '',
  siteUrl: '',
  logoUrl: '',
  description: '',
  contactName: '',
  contactEmail: '',
  contactMessage: '',
  themeColor: '#7CC6B8'
})

useSeoMeta({
  title: () => `友链申请 - ${brandName.value}`,
  description: () => `向 ${brandName.value} 提交友链申请，留下你的小站名称、地址和想说的话。`
})

/**
 * 校验友链申请表单。
 * 作用：在提交前给出最基本的前端反馈，减少无效请求进入后端。
 */
function validateForm() {
  if (!formState.siteName.trim()) {
    toast.add({ title: '请填写站点名称', color: 'warning' })
    return false
  }

  if (!/^https?:\/\//.test(formState.siteUrl.trim())) {
    toast.add({ title: '站点地址需以 http:// 或 https:// 开头', color: 'warning' })
    return false
  }

  if (!formState.contactName.trim()) {
    toast.add({ title: '请填写联系人名称', color: 'warning' })
    return false
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formState.contactEmail.trim())) {
    toast.add({ title: '请填写正确的联系邮箱', color: 'warning' })
    return false
  }

  if (!/^#([A-Fa-f0-9]{6})$/.test(formState.themeColor.trim())) {
    toast.add({ title: '主题色需为 #RRGGBB 格式', color: 'warning' })
    return false
  }

  return true
}

/**
 * 重置友链申请表单。
 * 作用：在提交成功后清空当前输入，方便用户确认已经投递完成。
 */
function resetForm() {
  formState.siteName = ''
  formState.siteUrl = ''
  formState.logoUrl = ''
  formState.description = ''
  formState.contactName = ''
  formState.contactEmail = ''
  formState.contactMessage = ''
  formState.themeColor = '#7CC6B8'
}

/**
 * 提交友链申请。
 * 作用：把当前页面表单发送到公开申请接口，并在成功后给出明确反馈。
 */
async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true

  try {
    const response = await friendLinksApi.applyFriendLink({
      siteName: formState.siteName.trim(),
      siteUrl: formState.siteUrl.trim(),
      logoUrl: formState.logoUrl.trim(),
      description: formState.description.trim(),
      contactName: formState.contactName.trim(),
      contactEmail: formState.contactEmail.trim(),
      contactMessage: formState.contactMessage.trim(),
      themeColor: formState.themeColor.trim().toUpperCase()
    })

    toast.add({
      title: '申请已寄出',
      description: response.message,
      color: 'success'
    })
    resetForm()
  } catch (error: any) {
    toast.add({
      title: '提交失败',
      description: error?.message || '友链申请暂时没有寄达，请稍后再试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="min-h-screen overflow-hidden bg-[linear-gradient(180deg,#f8fafc_0%,#f8fafc_100%)] pb-24 dark:bg-[linear-gradient(180deg,#020617_0%,#020617_100%)]">
    <section class="relative isolate overflow-hidden pt-28 sm:pt-32">
      <div class="absolute inset-0 -z-10 bg-[linear-gradient(180deg,rgba(250,250,249,0.98),rgba(248,250,252,0.98)),linear-gradient(90deg,rgba(148,163,184,0.08)_1px,transparent_1px),linear-gradient(180deg,rgba(148,163,184,0.08)_1px,transparent_1px)] bg-[size:auto,32px_32px,32px_32px] dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.99),rgba(2,6,23,0.99)),linear-gradient(90deg,rgba(71,85,105,0.18)_1px,transparent_1px),linear-gradient(180deg,rgba(71,85,105,0.18)_1px,transparent_1px)] dark:bg-[size:auto,32px_32px,32px_32px]" />
      <div class="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_18%_16%,rgba(56,189,248,0.13),transparent_22%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.08),transparent_20%),radial-gradient(circle_at_50%_100%,rgba(125,198,184,0.12),transparent_28%)] dark:bg-[radial-gradient(circle_at_18%_16%,rgba(56,189,248,0.1),transparent_22%),radial-gradient(circle_at_82%_18%,rgba(249,115,22,0.06),transparent_20%),radial-gradient(circle_at_50%_100%,rgba(125,198,184,0.08),transparent_28%)]" />

      <div class="mx-auto max-w-[860px] px-5 sm:px-8 lg:px-10">
        <div class="mx-auto max-w-[560px] text-center">
          <p class="text-[0.69rem] font-semibold uppercase tracking-[0.36em] text-slate-500 dark:text-slate-400">
            {{ brandName }} / 友链申请
          </p>
          <h1 class="mt-5 text-[clamp(2.5rem,1.9rem+2.2vw,4.4rem)] font-semibold leading-[0.98] tracking-[-0.065em] text-slate-950 [font-family:var(--font-display)] [text-wrap:balance] dark:text-white">
            添加友链
          </h1>
          <p class="mt-5 text-[0.95rem] leading-8 text-slate-600 dark:text-slate-300">
            填完下面这份表单就可以。
          </p>

          <div class="mt-8 flex flex-wrap items-center justify-center gap-5">
            <UButton
              to="/friends"
              label="先看看友链列表"
              color="neutral"
              variant="soft"
              class="rounded-full px-6"
            />
          </div>
        </div>
      </div>
    </section>

    <section class="mx-auto max-w-[1240px] px-5 py-14 sm:px-8 lg:px-10 lg:py-16">
      <section class="border-t border-slate-200/80 pt-14 dark:border-white/10">
        <div class="relative mx-auto max-w-[760px]">
          <div class="pointer-events-none absolute -left-[72px] top-10 hidden xl:block">
            <div class="relative w-[180px] opacity-80">
              <FriendLinkStoryScene compact />
            </div>
          </div>

          <section class="relative rounded-[34px] bg-[linear-gradient(180deg,rgba(255,255,255,0.86),rgba(248,250,252,0.78))] p-6 shadow-[0_28px_60px_-42px_rgba(15,23,42,0.18)] backdrop-blur-xl dark:bg-[linear-gradient(180deg,rgba(255,255,255,0.05),rgba(255,255,255,0.025))] sm:p-8 md:p-10">
            <form class="space-y-6" @submit.prevent="handleSubmit">
            <div class="grid gap-5 md:grid-cols-2">
              <UFormField name="siteName" label="站点名称" required>
                <UInput
                  v-model="formState.siteName"
                  variant="outline"
                  size="xl"
                  placeholder="例如：风铃图书室"
                  :ui="{ base: 'rounded-[18px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
                />
              </UFormField>

              <UFormField name="siteUrl" label="站点地址" required>
                <UInput
                  v-model="formState.siteUrl"
                  variant="outline"
                  size="xl"
                  placeholder="https://example.com"
                  :ui="{ base: 'rounded-[18px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
                />
              </UFormField>

              <UFormField name="contactName" label="联系人" required>
                <UInput
                  v-model="formState.contactName"
                  variant="outline"
                  size="xl"
                  placeholder="怎么称呼你"
                  :ui="{ base: 'rounded-[18px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
                />
              </UFormField>

              <UFormField name="contactEmail" label="联系邮箱" required>
                <UInput
                  v-model="formState.contactEmail"
                  variant="outline"
                  size="xl"
                  placeholder="name@example.com"
                  :ui="{ base: 'rounded-[18px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
                />
              </UFormField>
            </div>

            <div class="grid gap-5 md:grid-cols-[minmax(0,1fr)_15rem]">
              <UFormField name="logoUrl" label="Logo 地址">
                <UInput
                  v-model="formState.logoUrl"
                  variant="outline"
                  size="xl"
                  placeholder="可选"
                  :ui="{ base: 'rounded-[18px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
                />
              </UFormField>

              <UFormField name="themeColor" label="卡片主色">
                <div class="flex items-center gap-3 rounded-[18px] border border-white/70 bg-white/82 px-4 py-3 dark:border-white/10 dark:bg-white/[0.04]">
                  <input
                    v-model="formState.themeColor"
                    type="color"
                    class="h-10 w-10 cursor-pointer rounded-full border-0 bg-transparent p-0"
                  >
                  <UInput
                    v-model="formState.themeColor"
                    variant="none"
                    size="xl"
                    :ui="{ base: 'min-h-0 border-0 bg-transparent px-0 py-0 shadow-none focus-visible:ring-0 dark:bg-transparent' }"
                  />
                </div>
              </UFormField>
            </div>

            <UFormField name="description" label="站点简介">
              <UTextarea
                v-model="formState.description"
                :rows="4"
                variant="outline"
                placeholder="一句话介绍你的小站"
                :ui="{ base: 'rounded-[22px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
              />
            </UFormField>

            <UFormField name="contactMessage" label="想说的话">
              <UTextarea
                v-model="formState.contactMessage"
                :rows="5"
                variant="outline"
                placeholder="可选"
                :ui="{ base: 'rounded-[22px] border-white/70 bg-white/82 px-4 py-3 shadow-none dark:border-white/10 dark:bg-white/[0.04]' }"
              />
            </UFormField>

            <div>
              <p class="mb-3 text-sm font-medium text-slate-700 dark:text-slate-200">推荐主色</p>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="color in themeColorPresets"
                  :key="color"
                  type="button"
                  class="h-10 w-10 rounded-full border-2 transition hover:scale-105"
                  :class="formState.themeColor.toUpperCase() === color ? 'border-slate-950 dark:border-white' : 'border-white/70 dark:border-white/10'"
                  :style="{ backgroundColor: color }"
                  @click="formState.themeColor = color"
                />
              </div>
            </div>

            <div class="flex flex-col gap-3 border-t border-slate-200/70 pt-6 dark:border-white/10 sm:flex-row sm:items-center sm:justify-between">
              <p class="text-sm leading-7 text-slate-500 dark:text-slate-400">
                提交后会先进入审核队列。
              </p>

              <UButton
                type="submit"
                :loading="isSubmitting"
                label="投递友链申请"
                loading-label="正在投递..."
                color="neutral"
                variant="solid"
                class="rounded-full bg-slate-950 px-6 py-3 text-white hover:bg-slate-800 dark:bg-white dark:text-slate-950 dark:hover:bg-slate-100"
              />
            </div>
            </form>
          </section>
        </div>
      </section>
    </section>
  </main>
</template>
