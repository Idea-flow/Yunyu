<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import YunyuDropdownMenu from '~/components/common/YunyuDropdownMenu.vue'

/**
 * 前台默认布局。
 * 作用：统一承接前台页面的顶部导航、主题切换和底部品牌信息，减少首页与列表页重复结构。
 */
const route = useRoute()
const auth = useAuth()
const siteContent = useSiteContent()
const { data: siteConfigData } = await useAsyncData('site-base-info', async () => {
  return await siteContent.getSiteConfig()
})

type OverlaySurfaceVariant = 'light' | 'content'
type FrontNavTone = 'light-overlay' | 'overlay-inverse' | 'solid'
type ThemeModeTone = 'default' | 'inverse'

const NAV_SCROLL_ENTER_THRESHOLD = 56
const NAV_SCROLL_EXIT_THRESHOLD = 20
const OVERLAY_NAV_SOLID_THRESHOLD = 0.64
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '标签', to: '/tags' },
  { label: '专题', to: '/topics' },
  { label: '友链', to: '/friends' }
]

const NAV_APPEARANCE_CLASS_MAP = {
  'light-overlay': {
    brandTitle: 'text-slate-900 transition-colors duration-300 ease-out dark:text-slate-50 motion-reduce:transition-none',
    brandSubtitle: 'text-slate-500 transition-colors duration-300 ease-out dark:text-slate-300 motion-reduce:transition-none',
    link: 'rounded-full px-4 py-2 text-sm font-medium text-slate-700 transition-[background-color,color,transform,box-shadow] duration-200 ease-out hover:bg-white/58 hover:text-slate-950 motion-reduce:transition-none dark:text-slate-200 dark:hover:bg-white/10 dark:hover:text-white',
    activeLink: 'border border-white/78 bg-white/80 text-slate-950 shadow-[0_14px_30px_-24px_rgba(56,189,248,0.26)] dark:border-white/12 dark:bg-white/10 dark:text-sky-100',
    authAction: 'inline-flex h-10 items-center justify-center rounded-full border border-white/70 bg-white/60 px-4 text-sm font-medium text-slate-700 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/78 hover:text-slate-950 motion-reduce:transition-none dark:border-white/12 dark:bg-white/[0.08] dark:text-slate-100 dark:hover:bg-white/[0.12]',
    userDropdownTrigger: 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/70 bg-white/60 text-slate-800 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/78 motion-reduce:transition-none dark:border-white/12 dark:bg-white/[0.08] dark:text-slate-100 dark:hover:bg-white/[0.12]',
    divider: 'bg-slate-300/72 dark:bg-white/12',
    themeModeTone: 'default' as const
  },
  'overlay-inverse': {
    brandTitle: 'text-white transition-colors duration-300 ease-out motion-reduce:transition-none',
    brandSubtitle: 'text-white/72 transition-colors duration-300 ease-out motion-reduce:transition-none',
    link: 'rounded-full px-4 py-2 text-sm font-medium text-white/88 transition-[background-color,color,transform] duration-200 ease-out hover:bg-white/10 hover:text-white motion-reduce:transition-none',
    activeLink: 'bg-white/12 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.06)]',
    authAction: 'inline-flex h-10 items-center justify-center rounded-full border border-white/16 bg-white/10 px-4 text-sm font-medium text-white transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/16 motion-reduce:transition-none',
    userDropdownTrigger: 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/14 bg-white/10 text-white transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/16 motion-reduce:transition-none',
    divider: 'bg-white/14',
    themeModeTone: 'inverse' as const
  },
  solid: {
    brandTitle: 'text-slate-900 transition-colors duration-300 ease-out dark:text-slate-50 motion-reduce:transition-none',
    brandSubtitle: 'text-slate-500 transition-colors duration-300 ease-out dark:text-slate-400 motion-reduce:transition-none',
    link: 'rounded-full px-4 py-2 text-sm font-medium text-slate-700 transition-[background-color,color,transform,box-shadow] duration-200 ease-out hover:bg-white/62 hover:text-slate-950 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-sky-200 motion-reduce:transition-none',
    activeLink: 'border border-white/76 bg-white/80 text-slate-950 shadow-[0_12px_26px_-24px_rgba(15,23,42,0.16)] dark:border-white/10 dark:bg-white/12 dark:text-sky-100',
    authAction: 'inline-flex h-10 items-center justify-center rounded-full border border-slate-200/78 bg-white/72 px-4 text-sm font-medium text-slate-700 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:border-slate-300/88 hover:text-slate-950 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10 motion-reduce:transition-none',
    userDropdownTrigger: 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200/78 bg-white/72 text-slate-800 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:border-slate-300/88 hover:bg-white/82 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10 motion-reduce:transition-none',
    divider: 'bg-slate-300/70 dark:bg-white/10',
    themeModeTone: 'default' as const
  }
} as const

const OVERLAY_SURFACE_CLASS_MAP = {
  light: {
    initial: 'absolute inset-0 rounded-[inherit] bg-[linear-gradient(180deg,rgba(255,255,255,0.72),rgba(248,250,252,0.56))] shadow-[0_18px_42px_-30px_rgba(15,23,42,0.14)] backdrop-blur-[18px] transition-opacity duration-300 ease-out dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.4),rgba(2,6,23,0.24))] dark:shadow-[0_18px_40px_-30px_rgba(2,6,23,0.52)] motion-reduce:transition-none',
    solid: 'absolute inset-0 rounded-[inherit] bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(244,246,250,0.86))] shadow-[0_22px_52px_-34px_rgba(15,23,42,0.18)] backdrop-blur-[24px] transition-opacity duration-300 ease-out dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(2,6,23,0.72))] motion-reduce:transition-none'
  },
  content: {
    initial: 'absolute inset-0 rounded-[inherit] bg-slate-950/12 shadow-[0_12px_28px_-24px_rgba(15,23,42,0.18)] transition-opacity duration-300 ease-out dark:bg-slate-950/16 motion-reduce:transition-none',
    solid: 'absolute inset-0 rounded-[inherit] bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(244,246,250,0.88))] shadow-[0_24px_52px_-34px_rgba(15,23,42,0.22)] backdrop-blur-[24px] transition-opacity duration-300 ease-out dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(2,6,23,0.72))] motion-reduce:transition-none'
  }
} as const

const isScrolled = ref(false)
const navTransitionProgress = ref(0)
const isLoggedIn = computed(() => Boolean(auth.currentUser.value))
const canAccessAdmin = computed(() => auth.currentUser.value?.role === 'SUPER_ADMIN')
const currentUserDisplayName = computed(() => {
  return auth.currentUser.value?.userName || auth.currentUser.value?.email || '已登录用户'
})
const currentUserInitial = computed(() => currentUserDisplayName.value.slice(0, 1).toUpperCase())
const siteConfig = computed(() => siteConfigData.value)
const brandName = computed(() => siteConfig.value?.siteName?.trim() || '云屿')
const brandSubtitle = computed(() => siteConfig.value?.siteSubTitle?.trim() || 'Yunyu')
const footerText = computed(() => siteConfig.value?.footerText?.trim() || '云屿 Yunyu · 把二次元内容整理成更适合阅读的节奏。')
const logoUrl = computed(() => siteConfig.value?.logoUrl?.trim() || '/icon-512-maskable.png')
const primaryColor = computed(() => siteConfig.value?.primaryColor?.trim() || '#38BDF8')
const secondaryColor = computed(() => siteConfig.value?.secondaryColor?.trim() || '#FB923C')
const brandGradientStyle = computed(() => {
  return {
    backgroundImage: `linear-gradient(135deg, ${primaryColor.value}, ${secondaryColor.value})`
  }
})

/**
 * 判断给定路径是否为首页。
 * 作用：将首页的导航视觉从通用封面页中拆开，允许单独维护更轻的浅玻璃态。
 */
function isHomeRoute(path: string) {
  return path === '/'
}

/**
 * 判断给定路径是否为内容封面型页面。
 * 作用：统一收口文章、分类、专题等需要深色覆盖式导航的页面，避免模板内散落路径判断。
 */
function isContentOverlayRoute(path: string) {
  return path === '/posts'
    || path.startsWith('/posts/')
    || path === '/topics'
    || path.startsWith('/topics/')
    || path === '/categories'
    || path.startsWith('/categories/')
    || path === '/tags'
    || path === '/friends'
}

/**
 * 限制导航过渡进度到 0~1。
 * 作用：让不同滚动来源都复用同一套进度范围，避免模板层处理边界条件。
 */
function clampTransitionProgress(value: number) {
  return Math.min(Math.max(value, 0), 1)
}

/**
 * 根据普通滚动距离计算导航过渡进度。
 * 作用：首页没有封面标记时，导航仍可在轻玻璃与实底玻璃之间平滑过渡，而不是直接跳变。
 */
function getScrollTransitionProgress(scrollTop: number) {
  const transitionRange = Math.max(NAV_SCROLL_ENTER_THRESHOLD - NAV_SCROLL_EXIT_THRESHOLD, 1)
  return clampTransitionProgress((scrollTop - NAV_SCROLL_EXIT_THRESHOLD) / transitionRange)
}

/**
 * 根据封面底部位置计算导航过渡进度。
 * 作用：让文章、分类等带首屏封面的页面继续跟随封面离场节奏切换导航层次。
 */
function getHeroTransitionProgress(heroBottom: number, headerHeight: number) {
  const transitionStart = Math.max(headerHeight + 116, 164)
  const transitionEnd = Math.max(headerHeight + 12, 76)
  return clampTransitionProgress((transitionStart - heroBottom) / Math.max(transitionStart - transitionEnd, 1))
}

/**
 * 判断当前页面是否需要启用首屏覆盖式导航。
 * 作用：首页使用浅玻璃覆盖态，内容封面页继续使用深色覆盖态，普通页面保持常规 sticky 导航。
 */
const isHomePage = computed(() => isHomeRoute(route.path))
const isContentOverlayPage = computed(() => isContentOverlayRoute(route.path))
const usesLightOverlayNav = computed(() => {
  return isHomePage.value || route.path === '/tags' || route.path === '/friends'
})
const isOverlayPage = computed(() => isHomePage.value || isContentOverlayPage.value)

/**
 * 判断导航栏当前是否应进入实底玻璃态。
 * 作用：覆盖式页面统一按滚动进度切换，普通页面始终保持常规实底导航。
 */
const isSolidNav = computed(() => {
  if (isOverlayPage.value) {
    return navTransitionProgress.value >= OVERLAY_NAV_SOLID_THRESHOLD
  }

  return true
})

/**
 * 计算导航当前视觉语义。
 * 作用：把首页浅玻璃、封面页深覆盖和常规实底三种状态收口为单一入口，方便集中维护配色。
 */
const navTone = computed<FrontNavTone>(() => {
  if (!isSolidNav.value) {
    return usesLightOverlayNav.value ? 'light-overlay' : 'overlay-inverse'
  }

  return 'solid'
})

/**
 * 读取当前导航视觉配置。
 * 作用：集中提供品牌文案、链接、按钮和图标所需的样式，避免每个计算属性重复写条件分支。
 */
const navAppearance = computed(() => NAV_APPEARANCE_CLASS_MAP[navTone.value])

/**
 * 读取当前覆盖式导航的背景层配置。
 * 作用：首页和内容封面页分别维护自己的初始浮层与实底层，模板只负责渲染。
 */
const overlaySurfaceVariant = computed<OverlaySurfaceVariant>(() => {
  return usesLightOverlayNav.value ? 'light' : 'content'
})
const overlaySurfaceClasses = computed(() => OVERLAY_SURFACE_CLASS_MAP[overlaySurfaceVariant.value])

/**
 * 计算头部容器定位样式。
 * 作用：覆盖页使用 fixed 覆盖在首屏上方，普通页面继续使用 sticky 导航。
 */
const headerClassName = computed(() => {
  return isOverlayPage.value
    ? 'fixed inset-x-0 top-0 z-40'
    : 'sticky top-2 z-30 bg-transparent sm:top-3'
})

/**
 * 计算导航面板样式。
 * 作用：覆盖页保留透明承载层，常规页面直接使用稳定的实底玻璃面板。
 */
const navPanelClassName = computed(() => {
  if (isOverlayPage.value) {
    return 'bg-transparent shadow-none backdrop-blur-[12px] sm:backdrop-blur-[14px]'
  }

  return 'bg-[linear-gradient(180deg,rgba(255,255,255,0.94),rgba(244,246,250,0.88))] shadow-[0_24px_52px_-34px_rgba(15,23,42,0.22)] backdrop-blur-[24px] dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.82),rgba(2,6,23,0.72))]'
})

/**
 * 计算覆盖式导航的初始浮层样式。
 * 作用：首页进入时使用浅雾玻璃，内容封面页继续使用深色覆盖层。
 */
const overlayInitialLayerClassName = computed(() => overlaySurfaceClasses.value.initial)

/**
 * 计算覆盖式导航的实底玻璃层样式。
 * 作用：统一首页与内容封面页滚动后的稳定面板表现，同时保留各自的层次差异。
 */
const overlaySolidLayerClassName = computed(() => overlaySurfaceClasses.value.solid)

/**
 * 计算覆盖式导航初始浮层透明度。
 * 作用：随着首屏离场逐步淡出初始浮层，避免导航背景突变。
 */
const overlayInitialLayerStyle = computed(() => {
  return {
    opacity: `${1 - navTransitionProgress.value}`
  }
})

/**
 * 计算覆盖式导航实底层透明度。
 * 作用：让导航在滚动过程中逐步获得更稳定的实体感。
 */
const overlaySolidLayerStyle = computed(() => {
  return {
    opacity: `${navTransitionProgress.value}`
  }
})

/**
 * 计算品牌标题文字样式。
 * 作用：统一跟随导航视觉语义切换品牌主文案配色。
 */
const brandTitleClassName = computed(() => navAppearance.value.brandTitle)

/**
 * 计算品牌副标题文字样式。
 * 作用：让副标题在首页浅玻璃、封面深覆盖和实底玻璃之间保持稳定对比度。
 */
const brandSubtitleClassName = computed(() => navAppearance.value.brandSubtitle)

/**
 * 计算导航链接基础样式。
 * 作用：统一首页与其他页面的导航文本可读性与 hover 反馈。
 */
const navLinkClassName = computed(() => navAppearance.value.link)

/**
 * 计算导航链接激活样式。
 * 作用：让当前页面入口在不同导航语义下都保持明确但不过重的激活反馈。
 */
const navLinkActiveClassName = computed(() => navAppearance.value.activeLink)

/**
 * 计算导航分隔线样式。
 * 作用：将分隔线颜色跟随导航语义统一维护，避免模板内再写三元判断。
 */
const navDividerClassName = computed(() => {
  return `hidden h-8 w-px transition-colors duration-300 ease-out motion-reduce:transition-none md:block ${navAppearance.value.divider}`
})

/**
 * 计算导航外层留白。
 * 作用：文章详情页首屏改为全宽顶图后，导航需要更贴近浏览器顶部，
 * 避免顶部留白破坏整张封面的连续感。
 */
const headerInnerClassName = computed(() => {
  if (isOverlayPage.value) {
    return 'mx-auto max-w-[1360px] px-5 pt-2 pb-3 sm:px-8 sm:pt-3 lg:px-10'
  }

  return 'mx-auto max-w-[1360px] px-5 pb-3 sm:px-8 sm:pb-4 lg:px-10'
})

/**
 * 计算导航面板尺寸节奏。
 * 作用：覆盖式首屏统一使用更轻更薄的导航条，普通页面继续保持略厚的稳定面板。
 */
const navPanelLayoutClassName = computed(() => {
  if (isOverlayPage.value) {
    return 'rounded-[24px] px-4 py-2.5 sm:px-5'
  }

  return 'rounded-[28px] px-4 py-3 sm:px-5'
})

/**
 * 计算前台认证按钮样式。
 * 作用：统一登录按钮与退出按钮在不同导航语义下的视觉反馈。
 */
const authActionClassName = computed(() => navAppearance.value.authAction)

/**
 * 计算用户下拉触发器样式。
 * 作用：让当前用户入口既能作为身份展示，也能在不同导航语义下保持统一触感。
 */
const userDropdownTriggerClassName = computed(() => navAppearance.value.userDropdownTrigger)

/**
 * 计算主题切换按钮语义。
 * 作用：首页浅玻璃与常规导航使用默认深色图标，深色覆盖态切换为反相图标。
 */
const themeModeTone = computed<ThemeModeTone>(() => navAppearance.value.themeModeTone)

/**
 * 计算用户菜单项。
 * 作用：统一组织个人中心、后台入口和退出登录动作，避免模板内分散维护交互逻辑。
 */
const userMenuItems = computed(() => {
  const primaryItems = [
    {
      key: 'profile',
      label: '个人中心',
      icon: 'i-lucide-user-round',
      description: '查看当前登录账户信息',
      to: '/profile'
    }
  ]

  if (canAccessAdmin.value) {
    primaryItems.push({
      key: 'admin',
      label: '后台入口',
      icon: 'i-lucide-layout-dashboard',
      description: '进入云屿后台管理系统',
      to: '/admin'
    })
  }

  return [primaryItems, [
    {
      key: 'logout',
      label: '退出登录',
      icon: 'i-lucide-log-out',
      description: '清理当前登录状态并返回首页',
      tone: 'danger' as const
    }
  ]]
})

/**
 * 读取覆盖式首屏区域底部位置。
 * 作用：内容封面页在存在 Hero 标记时，让导航切换时机跟随首屏离场进度。
 *
 * @returns 首屏区域相对视口的底部位置；如果当前不存在首屏标记则返回 `null`
 */
function getOverlayHeroBottom() {
  if (!import.meta.client) {
    return null
  }

  const heroElement = document.querySelector<HTMLElement>('[data-post-cover-hero]')

  if (!(heroElement instanceof HTMLElement)) {
    return null
  }

  return heroElement.getBoundingClientRect().bottom
}

/**
 * 同步页面滚动状态。
 * 作用：根据首屏区域或滚动距离切换导航栏的覆盖态与实底玻璃态。
 */
function syncScrollState() {
  if (!import.meta.client) {
    return
  }

  if (isOverlayPage.value) {
    const heroBottom = getOverlayHeroBottom()
    const headerElement = document.querySelector('header')
    const headerHeight = headerElement instanceof HTMLElement ? headerElement.getBoundingClientRect().height : 0

    if (heroBottom !== null) {
      const progress = getHeroTransitionProgress(heroBottom, headerHeight)

      navTransitionProgress.value = progress
      isScrolled.value = progress >= 0.995
      return
    }
  }

  const currentScrollTop = window.scrollY || window.pageYOffset || 0
  const progress = getScrollTransitionProgress(currentScrollTop)

  navTransitionProgress.value = progress
  isScrolled.value = progress >= 0.995
}

onMounted(() => {
  if (!import.meta.client) {
    return
  }

  auth.hydratePersistedUser()
  void auth.fetchCurrentUser()
  void nextTick(() => {
    syncScrollState()
  })
  window.addEventListener('scroll', syncScrollState, { passive: true })
  window.addEventListener('resize', syncScrollState, { passive: true })
})

watch(() => route.fullPath, () => {
  void nextTick(() => {
    syncScrollState()
  })
})

onBeforeUnmount(() => {
  if (!import.meta.client) {
    return
  }

  window.removeEventListener('scroll', syncScrollState)
  window.removeEventListener('resize', syncScrollState)
})

/**
 * 跳转到登录页。
 * 作用：保留当前前台页面地址作为回跳目标，登录成功后可以回到原页面继续浏览。
 */
async function goToLogin() {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
}

/**
 * 退出当前登录态。
 * 作用：清理前台登录信息后返回首页，并让导航栏立即恢复未登录状态。
 */
async function handleLogout() {
  await auth.logout()
  await navigateTo('/')
}

/**
 * 处理用户菜单项选择。
 * 作用：将通用下拉菜单组件的选择事件映射为当前布局所需的跳转和退出行为。
 *
 * @param item 菜单项
 */
async function handleUserMenuSelect(item: { key: string }) {
  if (item.key === 'logout') {
    await handleLogout()
  }
}
</script>

<template>
  <div class="min-h-screen">
    <header :class="headerClassName">
      <div :class="headerInnerClassName">
        <div
          class="relative flex items-center justify-between gap-4 will-change-[background-color,border-color,box-shadow,backdrop-filter] transition-[background-color,border-color,box-shadow,backdrop-filter,border-radius,padding] duration-300 ease-[cubic-bezier(0.22,1,0.36,1)] motion-reduce:transition-none"
          :class="[navPanelClassName, navPanelLayoutClassName]"
        >
          <div
            v-if="isOverlayPage"
            aria-hidden="true"
            class="pointer-events-none absolute inset-0 overflow-hidden rounded-[inherit]"
          >
            <div
              :class="overlayInitialLayerClassName"
              :style="overlayInitialLayerStyle"
            />
            <div
              :class="overlaySolidLayerClassName"
              :style="overlaySolidLayerStyle"
            />
          </div>

          <div class="relative z-10 flex min-w-0 items-center gap-3 sm:gap-4">
            <NuxtLink to="/" class="flex shrink-0 items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center overflow-hidden rounded-2xl shadow-[0_12px_28px_-18px_rgba(15,23,42,0.18)]"
              >
                <img
                  :src="logoUrl"
                  :alt="`${brandName} 图标`"
                  class="h-full w-full object-cover"
                >
              </div>
              <div class="min-w-0">
                <p class="text-[clamp(1.25rem,1.05rem+0.75vw,1.7rem)] font-semibold leading-[0.96] tracking-[-0.05em] [font-family:var(--font-display)]" :class="brandTitleClassName">{{ brandName }}</p>
                <p class="mt-1 line-clamp-1 max-w-[18rem] text-[0.66rem] tracking-[0.08em]" :class="brandSubtitleClassName">{{ brandSubtitle }}</p>
              </div>
            </NuxtLink>

            <div :class="navDividerClassName" />

            <nav class="hidden items-center gap-1 md:flex">
              <NuxtLink
                v-for="item in navigationItems"
                :key="item.to"
                :to="item.to"
                :class="navLinkClassName"
                :active-class="navLinkActiveClassName"
              >
                {{ item.label }}
              </NuxtLink>
            </nav>
          </div>

          <div class="relative z-10 flex shrink-0 items-center gap-2">
            <ThemeModeSwitch variant="icon" :tone="themeModeTone" />

            <YunyuDropdownMenu
              v-if="isLoggedIn"
              :items="userMenuItems"
              :variant="isSolidNav ? 'solid' : 'overlay'"
              align="end"
              @select="handleUserMenuSelect"
            >
              <template #trigger="{ triggerProps }">
                <button
                  v-bind="triggerProps"
                  :class="userDropdownTriggerClassName"
                  :aria-label="`${currentUserDisplayName} 的用户菜单`"
                  :title="currentUserDisplayName"
                >
                  <div class="flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold text-white shadow-[0_10px_24px_-14px_rgba(56,189,248,0.68)]" :style="brandGradientStyle">
                    {{ currentUserInitial }}
                  </div>
                </button>
              </template>
            </YunyuDropdownMenu>

            <button
              v-else
              type="button"
              :class="authActionClassName"
              @click="goToLogin"
            >
              登录
            </button>
          </div>
        </div>
      </div>
    </header>

    <slot />

    <footer class="border-t border-white/60 bg-white/72 dark:border-white/10 dark:bg-slate-950/70">
      <div class="mx-auto flex max-w-[1360px] flex-col gap-3 px-5 py-8 text-sm text-slate-500 sm:px-8 lg:px-10 dark:text-slate-400 md:flex-row md:items-center md:justify-between">
        <p>{{ footerText }}</p>
        <div class="flex items-center gap-4">
          <NuxtLink to="/posts" class="hover:text-slate-900 dark:hover:text-slate-50">文章</NuxtLink>
          <NuxtLink to="/categories" class="hover:text-slate-900 dark:hover:text-slate-50">分类</NuxtLink>
          <NuxtLink to="/tags" class="hover:text-slate-900 dark:hover:text-slate-50">标签</NuxtLink>
          <NuxtLink to="/topics" class="hover:text-slate-900 dark:hover:text-slate-50">专题</NuxtLink>
        </div>
      </div>
    </footer>
  </div>
</template>
