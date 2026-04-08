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
const NAV_SCROLL_ENTER_THRESHOLD = 56
const NAV_SCROLL_EXIT_THRESHOLD = 20
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '标签', to: '/tags' },
  { label: '专题', to: '/topics' }
]

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
 * 判断当前页面是否需要启用文章首屏覆盖式导航。
 * 作用：统一收口文章列表页、文章详情页等封面首屏场景，后续若有其他页面复用同类交互，只需要维护这一个判断。
 */
const isOverlayPage = computed(() => {
  return route.path === '/posts'
    || route.path.startsWith('/posts/') || route.path === '/'
  || route.path === '/topics' || route.path.startsWith('/topics/')
      || route.path.startsWith('/categories/')
})

/**
 * 判断导航栏当前是否应进入实底玻璃态。
 * 作用：首页默认直接使用可读性更强的玻璃实底样式，
 * 其他覆盖页仍按滚动状态切换，避免首页首屏和滚动后出现两套导航视觉。
 */
const isSolidNav = computed(() => {
  if (route.path === '/') {
    return true
  }

  if (isOverlayPage.value) {
    return navTransitionProgress.value >= 0.64
  }

  return !isOverlayPage.value || isScrolled.value
})

/**
 * 计算头部容器定位样式。
 * 作用：覆盖页使用 fixed 覆盖在首屏上方，普通页面继续使用 sticky 导航。
 */
const headerClassName = computed(() => {
  return isOverlayPage.value
    ? 'fixed inset-x-0 top-0 z-40'
    : 'sticky top-0 z-30 bg-transparent'
})

/**
 * 计算导航面板样式。
 * 作用：统一控制覆盖态与滚动态下导航条的背景、边框和阴影层级。
 */
const navPanelClassName = computed(() => {
  if (isOverlayPage.value) {
    return 'border border-transparent bg-transparent shadow-none backdrop-blur-[12px] sm:backdrop-blur-[14px]'
  }

  if (!isSolidNav.value) {
    if (isOverlayPage.value) {
      return 'border border-white/8 bg-slate-950/12 shadow-[0_12px_28px_-24px_rgba(15,23,42,0.18)] backdrop-blur-[10px] dark:border-white/10 dark:bg-slate-950/16'
    }

    return 'border border-white/10 bg-slate-950/10 shadow-none backdrop-blur-[14px] dark:border-white/10 dark:bg-slate-950/14'
  }

  return 'border border-slate-200/58 bg-white/68 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.12)] backdrop-blur-[18px] dark:border-white/8 dark:bg-slate-950/52'
})

/**
 * 计算详情页导航浮层的暗态层透明度。
 * 作用：让封面仍在可视区时，导航优先保持轻薄覆盖态，
 * 随着封面接近离场再逐步淡出，避免边框和背景突然跳变。
 */
const postDetailOverlayLayerStyle = computed(() => {
  return {
    opacity: `${1 - navTransitionProgress.value}`
  }
})

/**
 * 计算详情页导航浮层的实底层透明度。
 * 作用：让导航在封面即将离开时提前出现一层更轻的实底玻璃，
 * 当封面完全离场后自然落到稳定状态。
 */
const postDetailSolidLayerStyle = computed(() => {
  return {
    opacity: `${navTransitionProgress.value}`
  }
})

/**
 * 计算品牌标题文字样式。
 * 作用：覆盖页在首屏顶部默认使用浅色文案，滚动后切回常规导航配色。
 */
const brandTitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white transition-colors duration-300 ease-out motion-reduce:transition-none'
    : 'text-slate-900 transition-colors duration-300 ease-out dark:text-slate-50 motion-reduce:transition-none'
})

/**
 * 计算品牌副标题文字样式。
 * 作用：让覆盖态与普通态都保持合适的对比度。
 */
const brandSubtitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white/72 transition-colors duration-300 ease-out motion-reduce:transition-none'
    : 'text-slate-500 transition-colors duration-300 ease-out dark:text-slate-400 motion-reduce:transition-none'
})

/**
 * 计算导航链接基础样式。
 * 作用：统一覆盖态与普通态下导航文字的可读性与交互反馈。
 */
const navLinkClassName = computed(() => {
  return !isSolidNav.value
    ? 'rounded-full px-4 py-2 text-sm font-medium text-white/88 transition-[background-color,color,transform] duration-200 ease-out hover:bg-white/10 hover:text-white motion-reduce:transition-none'
    : 'rounded-full px-4 py-2 text-sm font-medium text-slate-600 transition-[background-color,color,transform,box-shadow] duration-200 ease-out hover:bg-slate-100/88 hover:text-sky-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-sky-200 motion-reduce:transition-none'
})

/**
 * 计算导航链接激活样式。
 * 作用：让当前页面入口在覆盖态和普通态下都保持明确的激活反馈。
 */
const navLinkActiveClassName = computed(() => {
  return !isSolidNav.value
    ? 'bg-white/12 text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.06)]'
    : 'bg-white/72 text-sky-700 shadow-[0_10px_24px_-20px_rgba(14,165,233,0.22)] dark:bg-white/10 dark:text-sky-200'
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

  return 'mx-auto max-w-[1360px] px-5 py-4 sm:px-8 lg:px-10'
})

/**
 * 计算导航面板尺寸节奏。
 * 作用：文章详情页在首屏阶段使用更轻更薄的导航条，
 * 让导航像浮在封面上的一层细玻璃，而不是独立的大块容器。
 */
const navPanelLayoutClassName = computed(() => {
  if (isOverlayPage.value) {
    return 'rounded-[24px] px-4 py-2.5 sm:px-5'
  }

  return 'rounded-[28px] px-4 py-3 sm:px-5'
})

/**
 * 计算前台认证按钮样式。
 * 作用：统一登录按钮与退出按钮在覆盖态和普通态下的视觉反馈。
 */
const authActionClassName = computed(() => {
  return !isSolidNav.value
    ? 'inline-flex h-10 items-center justify-center rounded-full border border-white/16 bg-white/10 px-4 text-sm font-medium text-white transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/16 motion-reduce:transition-none'
    : 'inline-flex h-10 items-center justify-center rounded-full border border-slate-200/78 bg-white/72 px-4 text-sm font-medium text-slate-700 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:border-slate-300/88 hover:text-slate-950 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10 motion-reduce:transition-none'
})

/**
 * 计算用户下拉触发器样式。
 * 作用：让当前用户入口既能作为身份展示，也能作为下拉菜单的统一触发器。
 */
const userDropdownTriggerClassName = computed(() => {
  return !isSolidNav.value
    ? 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/14 bg-white/10 text-white transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:bg-white/16 motion-reduce:transition-none'
    : 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200/78 bg-white/72 text-slate-800 transition-[background-color,border-color,color,box-shadow] duration-200 ease-out hover:border-slate-300/88 hover:bg-white/82 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10 motion-reduce:transition-none'
})

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
 * 读取文章封面区域底部位置。
 * 作用：让文章列表页、文章详情页的导航切换时机统一跟随封面区域，
 * 避免在封面还未完全离开导航区域时就提前切换成实底样式。
 *
 * @returns 封面区域相对视口的底部位置；如果当前不存在封面区域则返回 `null`
 */
function getPostHeroBottom() {
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
 * 作用：根据封面区域或滚动距离切换导航栏是否进入实底玻璃态。
 */
function syncScrollState() {
  if (!import.meta.client) {
    return
  }

  if (isOverlayPage.value) {
    const heroBottom = getPostHeroBottom()
    const headerElement = document.querySelector('header')
    const headerHeight = headerElement instanceof HTMLElement ? headerElement.getBoundingClientRect().height : 0

    if (heroBottom !== null) {
      const transitionStart = Math.max(headerHeight + 116, 164)
      const transitionEnd = Math.max(headerHeight + 12, 76)
      const progress = Math.min(Math.max((transitionStart - heroBottom) / Math.max(transitionStart - transitionEnd, 1), 0), 1)

      navTransitionProgress.value = progress
      isScrolled.value = progress >= 0.995
      return
    }
  }

  const currentScrollTop = window.scrollY || window.pageYOffset || 0

  if (isScrolled.value) {
    isScrolled.value = currentScrollTop > NAV_SCROLL_EXIT_THRESHOLD
    navTransitionProgress.value = isScrolled.value ? 1 : 0
    return
  }

  isScrolled.value = currentScrollTop > NAV_SCROLL_ENTER_THRESHOLD
  navTransitionProgress.value = isScrolled.value ? 1 : 0
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
          class="relative overflow-hidden flex items-center justify-between gap-4 will-change-[background-color,border-color,box-shadow,backdrop-filter] transition-[background-color,border-color,box-shadow,backdrop-filter,border-radius,padding] duration-300 ease-[cubic-bezier(0.22,1,0.36,1)] motion-reduce:transition-none"
          :class="[navPanelClassName, navPanelLayoutClassName]"
        >
          <div
            v-if="isOverlayPage"
            aria-hidden="true"
            class="pointer-events-none absolute inset-0"
          >
            <div
              class="absolute inset-0 rounded-[inherit] border border-white/8 bg-slate-950/12 shadow-[0_12px_28px_-24px_rgba(15,23,42,0.18)] transition-opacity duration-300 ease-out dark:border-white/10 dark:bg-slate-950/16 motion-reduce:transition-none"
              :style="postDetailOverlayLayerStyle"
            />
            <div
              class="absolute inset-0 rounded-[inherit] border border-slate-200/58 bg-white/68 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.12)] transition-opacity duration-300 ease-out dark:border-white/8 dark:bg-slate-950/52 motion-reduce:transition-none"
              :style="postDetailSolidLayerStyle"
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

            <div
              class="hidden h-8 w-px transition-colors duration-300 ease-out motion-reduce:transition-none md:block"
              :class="!isSolidNav ? 'bg-white/14' : 'bg-slate-200/60 dark:bg-white/10'"
            />

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
            <ThemeModeSwitch variant="icon" />

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
