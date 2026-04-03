<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import YunyuDropdownMenu from '~/components/common/YunyuDropdownMenu.vue'

/**
 * 前台默认布局。
 * 作用：统一承接前台页面的顶部导航、主题切换和底部品牌信息，减少首页与列表页重复结构。
 */
const route = useRoute()
const auth = useAuth()
const navigationItems = [
  { label: '首页', to: '/' },
  { label: '文章', to: '/posts' },
  { label: '分类', to: '/categories' },
  { label: '标签', to: '/tags' },
  { label: '专题', to: '/topics' }
]

const isScrolled = ref(false)
const isPostDetailPage = computed(() => route.path.startsWith('/posts/'))
const isLoggedIn = computed(() => Boolean(auth.currentUser.value))
const canAccessAdmin = computed(() => auth.currentUser.value?.role === 'SUPER_ADMIN')
const currentUserDisplayName = computed(() => {
  return auth.currentUser.value?.userName || auth.currentUser.value?.email || '已登录用户'
})
const currentUserInitial = computed(() => currentUserDisplayName.value.slice(0, 1).toUpperCase())

/**
 * 判断当前页面是否需要导航栏覆盖首屏区域。
 * 作用：让首页与文章详情页的导航栏直接叠在首屏大图之上，避免导航区和首屏内容被硬切开。
 */
const isOverlayPage = computed(() => {
  return route.path === '/'
    || route.path === '/topics'
    || route.path.startsWith('/posts/')
    || route.path.startsWith('/topics/')
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
  if (!isSolidNav.value) {
    if (isPostDetailPage.value) {
      return 'border border-white/10 bg-[linear-gradient(180deg,rgba(15,23,42,0.18)_0%,rgba(15,23,42,0.08)_100%)] shadow-[0_12px_34px_-28px_rgba(15,23,42,0.2)] backdrop-blur-[10px]'
    }

    return 'border-white/12 bg-[linear-gradient(180deg,rgba(15,23,42,0.14)_0%,rgba(15,23,42,0.1)_100%)] shadow-none backdrop-blur-[14px]'
  }

  return 'border border-white/55 bg-[linear-gradient(180deg,rgba(245,248,252,0.54)_0%,rgba(239,246,255,0.42)_100%)] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.1)] backdrop-blur-[18px] dark:border-white/8 dark:bg-[linear-gradient(180deg,rgba(15,23,42,0.42)_0%,rgba(15,23,42,0.3)_100%)]'
})

/**
 * 计算品牌标题文字样式。
 * 作用：覆盖页在首屏顶部默认使用浅色文案，滚动后切回常规导航配色。
 */
const brandTitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white'
    : 'text-slate-900 dark:text-slate-50'
})

/**
 * 计算品牌副标题文字样式。
 * 作用：让覆盖态与普通态都保持合适的对比度。
 */
const brandSubtitleClassName = computed(() => {
  return !isSolidNav.value
    ? 'text-white/72'
    : 'text-slate-500 dark:text-slate-400'
})

/**
 * 计算导航链接基础样式。
 * 作用：统一覆盖态与普通态下导航文字的可读性与交互反馈。
 */
const navLinkClassName = computed(() => {
  return !isSolidNav.value
    ? 'rounded-full px-4 py-2 text-sm font-medium text-white/88 transition hover:bg-white/12 hover:text-white'
    : 'rounded-full px-4 py-2 text-sm font-medium text-slate-600 transition hover:bg-white/42 hover:text-sky-700 dark:text-slate-300 dark:hover:bg-white/8 dark:hover:text-sky-200'
})

/**
 * 计算导航链接激活样式。
 * 作用：让当前页面入口在覆盖态和普通态下都保持明确的激活反馈。
 */
const navLinkActiveClassName = computed(() => {
  return !isSolidNav.value
    ? 'bg-white/14 text-white'
    : 'bg-white/58 text-sky-700 shadow-[0_8px_20px_-18px_rgba(14,165,233,0.26)] dark:bg-white/10 dark:text-sky-200'
})

/**
 * 计算导航外层留白。
 * 作用：文章详情页首屏改为全宽顶图后，导航需要更贴近浏览器顶部，
 * 避免顶部留白破坏整张封面的连续感。
 */
const headerInnerClassName = computed(() => {
  if (isOverlayPage.value && isPostDetailPage.value) {
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
  if (isOverlayPage.value && isPostDetailPage.value && !isSolidNav.value) {
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
    ? 'inline-flex h-10 items-center justify-center rounded-full border border-white/16 bg-white/10 px-4 text-sm font-medium text-white transition hover:bg-white/16'
    : 'inline-flex h-10 items-center justify-center rounded-full border border-slate-200/80 bg-white/72 px-4 text-sm font-medium text-slate-700 transition hover:border-slate-300 hover:text-slate-950 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10'
})

/**
 * 计算用户下拉触发器样式。
 * 作用：让当前用户入口既能作为身份展示，也能作为下拉菜单的统一触发器。
 */
const userDropdownTriggerClassName = computed(() => {
  return !isSolidNav.value
    ? 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/14 bg-white/10 text-white transition hover:bg-white/16'
    : 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200/80 bg-white/72 text-slate-800 transition hover:border-slate-300 hover:bg-white/82 dark:border-white/10 dark:bg-white/6 dark:text-slate-100 dark:hover:bg-white/10'
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
 * 同步页面滚动状态。
 * 作用：根据滚动距离切换导航栏是否进入实底玻璃态。
 */
function syncScrollState() {
  if (!import.meta.client) {
    return
  }

  isScrolled.value = window.scrollY > 36
}

onMounted(() => {
  if (!import.meta.client) {
    return
  }

  auth.hydratePersistedUser()
  void auth.fetchCurrentUser()
  syncScrollState()
  window.addEventListener('scroll', syncScrollState, { passive: true })
})

watch(() => route.fullPath, () => {
  syncScrollState()
})

onBeforeUnmount(() => {
  if (!import.meta.client) {
    return
  }

  window.removeEventListener('scroll', syncScrollState)
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
          class="flex items-center justify-between gap-4 transition-all duration-300"
          :class="[navPanelClassName, navPanelLayoutClassName]"
        >
          <div class="flex min-w-0 items-center gap-3 sm:gap-4">
            <NuxtLink to="/" class="flex shrink-0 items-center gap-3">
              <div
                class="flex h-11 w-11 items-center justify-center overflow-hidden rounded-2xl shadow-[0_12px_28px_-18px_rgba(15,23,42,0.18)]"
              >
                <img
                  src="/icon-512-maskable.png"
                  alt="云屿图标"
                  class="h-full w-full object-cover"
                >
              </div>
              <div class="min-w-0">
                <p class="text-[clamp(1.25rem,1.05rem+0.75vw,1.7rem)] font-semibold leading-[0.96] tracking-[-0.05em] [font-family:var(--font-display)]" :class="brandTitleClassName">云屿</p>
                <p class="mt-1 text-[0.66rem] uppercase tracking-[0.14em]" :class="brandSubtitleClassName">Yunyu</p>
              </div>
            </NuxtLink>

            <div
              class="hidden h-8 w-px md:block"
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

          <div class="flex shrink-0 items-center gap-2">
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
                  <div class="flex h-9 w-9 items-center justify-center rounded-full bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-sm font-semibold text-white shadow-[0_10px_24px_-14px_rgba(56,189,248,0.68)]">
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
        <p>云屿 Yunyu · 把二次元内容整理成更适合阅读的节奏。</p>
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
