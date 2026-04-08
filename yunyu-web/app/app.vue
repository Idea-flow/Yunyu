<script setup lang="ts">
/**
 * 前台应用根组件。
 * 作用：统一承接全局布局、页面切换和 Nuxt UI 通用能力配置，
 * 这里集中定义全站 toast 的展示位置与基础体验。
 */
const siteContent = useSiteContent()
const { data: siteConfigData } = await useAsyncData('site-base-info', async () => {
  return await siteContent.getSiteConfig()
})

/**
 * 当前站点配置。
 * 作用：统一提供 favicon、主题色和站点基础信息的全局兜底来源。
 */
const siteConfig = computed(() => siteConfigData.value)

/**
 * 当前站点主色。
 * 作用：在站点未配置视觉主色时，继续沿用现有默认主色。
 */
const primaryColor = computed(() => siteConfig.value?.primaryColor?.trim() || '#38BDF8')

/**
 * 当前站点辅助色。
 * 作用：在站点未配置辅助色时，继续沿用现有默认辅助色。
 */
const secondaryColor = computed(() => siteConfig.value?.secondaryColor?.trim() || '#FB923C')

/**
 * 当前站点 favicon 地址。
 * 作用：优先使用后台配置 favicon，未配置时回退到现有静态资源。
 */
const faviconUrl = computed(() => siteConfig.value?.faviconUrl?.trim() || '/favicon.ico')

useHead(() => ({
  htmlAttrs: {
    style: `--site-primary-color:${primaryColor.value};--site-secondary-color:${secondaryColor.value};`
  },
  link: [
    {
      key: 'site-favicon',
      rel: 'icon',
      type: 'image/x-icon',
      href: faviconUrl.value,
      sizes: 'any'
    }
  ],
  meta: [
    {
      key: 'site-theme-color',
      name: 'theme-color',
      content: primaryColor.value
    }
  ]
}))

const globalToaster = {
  position: 'top-center' as const,
  duration: 2000,
  progress: false,
  expand: false,
  max: 3,
  ui: {
    viewport: 'fixed left-1/2 top-5 z-[120] flex w-[min(calc(100vw-1.5rem),26rem)] -translate-x-1/2 flex-col px-0 sm:top-6',
    base: 'pointer-events-auto absolute inset-x-0 z-(--index) transform-(--transform) data-[expanded=false]:data-[front=false]:h-(--front-height) data-[expanded=false]:data-[front=false]:*:opacity-0 data-[front=false]:*:transition-opacity data-[front=false]:*:duration-100 data-[state=closed]:animate-[toast-closed_180ms_ease-in-out] data-[state=closed]:data-[expanded=false]:data-[front=false]:animate-[toast-collapsed-closed_180ms_ease-in-out] data-[state=open]:data-[swipe=move]:transition-none transition-[transform,translate,height,opacity] duration-200 ease-out'
  }
}
</script>

<template>
  <UApp :toaster="globalToaster">
    <NuxtRouteAnnouncer />
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </UApp>
</template>
