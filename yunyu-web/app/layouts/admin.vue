<script setup lang="ts">
/**
 * 后台导航项类型。
 * 作用：约束后台侧边栏导航所需的最小数据结构，保证渲染层语义稳定。
 */
interface AdminNavigationItem {
  label: string
  icon: string
  to: string
  badgeType?: 'pending-comments'
}

/**
 * 后台布局组件。
 * 负责承载 Yunyu 后台区域的导航、主题切换、身份展示与统一框架，
 * 作为所有后台页面共享的布局基础。
 */
const route = useRoute()
const auth = useAuth()
const adminComments = useAdminComments()
const systemSlogan = '记录热爱，沉淀表达'
const sidebarCollapsed = useState('admin-sidebar-collapsed', () => false)
const pendingCommentTotal = ref<number | null>(null)
const isPendingCommentTotalLoading = ref(false)

const navigationItems: AdminNavigationItem[] = [
  {
    label: '后台首页',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin'
  },
  {
    label: '文章管理',
    icon: 'i-lucide-files',
    to: '/admin/posts'
  },
  {
    label: '分类管理',
    icon: 'i-lucide-folders',
    to: '/admin/categories'
  },
  {
    label: '标签管理',
    icon: 'i-lucide-tags',
    to: '/admin/tags'
  },
  {
    label: '专题管理',
    icon: 'i-lucide-book-open-text',
    to: '/admin/topics'
  },
  {
    label: '评论管理',
    icon: 'i-lucide-messages-square',
    to: '/admin/comments',
    badgeType: 'pending-comments'
  },
  {
    label: '用户管理',
    icon: 'i-lucide-users',
    to: '/admin/users'
  },
  {
    label: '系统监控',
    icon: 'i-lucide-activity',
    to: '/admin/system'
  },
  {
    label: '站点设置',
    icon: 'i-lucide-settings-2',
    to: '/admin/site'
  }
]

/**
 * 同步读取侧边栏折叠状态。
 * 作用：在客户端初始化时恢复上次使用的折叠偏好，让后台导航交互更连贯。
 */
onMounted(() => {
  const storedValue = localStorage.getItem('yunyu-admin-sidebar-collapsed')

  if (storedValue !== null) {
    sidebarCollapsed.value = storedValue === 'true'
  }

  loadPendingCommentTotal()
})

/**
 * 持久化侧边栏折叠状态。
 * 作用：在用户切换折叠后保留当前偏好，下次进入后台继续沿用。
 */
watch(sidebarCollapsed, (value) => {
  localStorage.setItem('yunyu-admin-sidebar-collapsed', String(value))
})

watch(
  () => route.fullPath,
  async () => {
    if (!route.path.startsWith('/admin/comments')) {
      return
    }

    await loadPendingCommentTotal()
  }
)

/**
 * 判断当前导航项是否处于激活状态。
 *
 * @param item 导航项
 * @returns 是否为当前激活路由
 */
function isActiveItem(item: AdminNavigationItem) {
  if (item.to === '/admin') {
    return route.path === '/admin'
  }

  return route.path.startsWith(item.to)
}

/**
 * 加载待审核评论数量。
 * 作用：为后台侧边栏中的“评论管理”入口提供真实待审核数量徽标，帮助站长快速感知待处理积压。
 */
async function loadPendingCommentTotal() {
  if (isPendingCommentTotalLoading.value) {
    return
  }

  isPendingCommentTotalLoading.value = true

  try {
    const response = await adminComments.listComments({
      status: 'PENDING',
      pageNo: 1,
      pageSize: 1
    })
    pendingCommentTotal.value = response.total
  } catch {
    pendingCommentTotal.value = null
  } finally {
    isPendingCommentTotalLoading.value = false
  }
}

/**
 * 解析导航项徽标文本。
 * 作用：统一处理后台侧边栏不同入口的徽标显示规则，避免模板层散落条件判断。
 *
 * @param item 导航项
 * @returns 当前导航项需要展示的徽标文本
 */
function resolveNavigationBadge(item: AdminNavigationItem) {
  if (item.badgeType !== 'pending-comments') {
    return ''
  }

  if (pendingCommentTotal.value === null || pendingCommentTotal.value <= 0) {
    return ''
  }

  return pendingCommentTotal.value > 99 ? '99+' : String(pendingCommentTotal.value)
}

/**
 * 切换侧边栏折叠状态。
 * 作用：在完整导航和图标导航之间切换，提升后台页面的可用空间利用率。
 */
function toggleSidebarCollapsed() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

/**
 * 退出当前登录态。
 * 完成清理后回到共享登录页，方便再次进入后台。
 */
async function handleLogout() {
  await auth.logout()
  await navigateTo('/login?redirect=/admin')
}
</script>

<template>
  <div class="admin-workspace fixed inset-0 flex overflow-hidden">
    <aside
      :class="[
        'h-dvh shrink-0 px-3 pb-3 pt-3 transition-[width,min-width,padding] duration-300 ease-out',
        sidebarCollapsed ? 'w-[104px] min-w-[104px]' : 'w-[296px] min-w-[296px]'
      ]"
    >
      <div class="flex h-full min-h-0 flex-col overflow-hidden rounded-[20px] border border-white/50 bg-[linear-gradient(180deg,rgba(255,255,255,0.76),rgba(255,255,255,0.56))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
        <div
          :class="[
            'bg-[linear-gradient(135deg,var(--admin-primary-soft-surface),var(--admin-secondary-soft-surface)_88%)] py-5 dark:bg-[linear-gradient(135deg,color-mix(in_srgb,var(--site-primary-color)_16%,transparent),color-mix(in_srgb,var(--site-secondary-color)_14%,transparent)_88%)]',
            sidebarCollapsed ? 'px-3' : 'px-5'
          ]"
        >
          <div :class="['flex items-center', sidebarCollapsed ? 'justify-center' : 'gap-3']">
            <div class="flex size-11 items-center justify-center rounded-[12px] bg-[var(--site-primary-color)] text-sm font-semibold text-white shadow-[0_14px_24px_-18px_var(--admin-primary-shadow)] dark:text-slate-950">
              Y
            </div>

            <div v-if="!sidebarCollapsed" class="min-w-0 flex-1">
              <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">云屿后台</p>
              <p class="truncate text-xs text-slate-600 dark:text-slate-300">{{ systemSlogan }}</p>
            </div>
          </div>
        </div>

        <div :class="[sidebarCollapsed ? 'mx-3' : 'mx-4', 'border-t border-white/65 dark:border-white/10']" />

        <nav
          :class="[
            'flex flex-1 flex-col gap-2 overflow-y-auto py-4 [scrollbar-width:thin] [&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80',
            sidebarCollapsed ? 'px-3' : 'px-4'
          ]"
        >
          <NuxtLink
            v-for="item in navigationItems"
            :key="item.to"
            :to="item.to"
            :title="item.label"
            :aria-label="item.label"
            :class="[
              'relative flex min-h-11 items-center rounded-[12px] border py-2.5 text-slate-700 transition duration-200 dark:text-slate-200',
              sidebarCollapsed ? 'justify-center px-0' : 'gap-2.5 px-3',
              isActiveItem(item)
                ? 'border-[var(--admin-primary-border)] bg-[linear-gradient(135deg,var(--admin-primary-soft-surface),var(--admin-secondary-soft-surface))] text-slate-900 before:absolute before:start-2 before:top-1/2 before:h-5 before:w-[3px] before:-translate-y-1/2 before:rounded-full before:bg-[var(--site-primary-color)] dark:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,transparent)] dark:bg-[linear-gradient(135deg,color-mix(in_srgb,var(--site-primary-color)_10%,transparent),color-mix(in_srgb,var(--site-secondary-color)_8%,transparent))] dark:text-slate-50'
                : 'border-transparent hover:border-white/70 hover:bg-white/68 hover:text-slate-900 dark:hover:border-white/10 dark:hover:bg-white/5 dark:hover:text-slate-50'
            ]"
          >
            <div class="flex size-8 shrink-0 items-center justify-center rounded-[9px] bg-slate-100/90 text-slate-900 dark:bg-white/5 dark:text-slate-50">
              <UIcon :name="item.icon" class="size-4" />
            </div>

            <div v-if="!sidebarCollapsed" class="min-w-0 flex-1">
              <p class="text-[13px] font-semibold leading-5 text-slate-900 dark:text-slate-50">{{ item.label }}</p>
            </div>

            <span
              v-if="!sidebarCollapsed && resolveNavigationBadge(item)"
              class="ms-auto inline-flex min-w-[1.5rem] items-center justify-center rounded-full bg-[var(--admin-primary-soft-strong)] px-2 py-0.5 text-[11px] font-semibold leading-none text-[var(--admin-primary-text)] dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_14%,transparent)] dark:text-[var(--site-primary-color)]"
            >
              {{ resolveNavigationBadge(item) }}
            </span>
          </NuxtLink>
        </nav>

        <div :class="['border-t border-white/65 py-4 dark:border-white/10', sidebarCollapsed ? 'px-3' : 'px-4']">
          <div
            :class="[
              'flex items-center gap-2 rounded-[14px] border border-white/60 bg-white/55 p-3 backdrop-blur-md dark:border-white/10 dark:bg-white/5',
              sidebarCollapsed ? 'flex-col justify-center' : 'justify-end'
            ]"
          >
            <button
              type="button"
              :title="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'"
              :aria-label="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'"
              class="inline-flex size-10 items-center justify-center rounded-[8px] border border-slate-200 bg-white text-slate-600 transition duration-200 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-950/70 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:text-slate-50"
              @click="toggleSidebarCollapsed"
            >
              <UIcon :name="sidebarCollapsed ? 'i-lucide-panel-left-open' : 'i-lucide-panel-left-close'" class="size-4" />
            </button>

            <ThemeModeSwitch />

            <button
              type="button"
              class="inline-flex size-10 items-center justify-center rounded-[8px] border border-slate-200 bg-white text-slate-600 transition duration-200 hover:border-slate-300 hover:text-slate-900 dark:border-slate-700 dark:bg-slate-950/70 dark:text-slate-300 dark:hover:border-slate-600 dark:hover:text-slate-50"
              title="退出登录"
              aria-label="退出登录"
              @click="handleLogout"
            >
              <UIcon name="i-lucide-log-out" class="size-4" />
            </button>
          </div>
        </div>
      </div>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col overflow-hidden px-4 pb-4 pt-3 lg:px-5 lg:pb-5 lg:pl-3 lg:pt-4">
      <div class="admin-shell overflow-hidden rounded-[18px]">
        <div class="bg-[linear-gradient(135deg,var(--admin-primary-soft-surface),var(--admin-secondary-soft-surface)_92%)] px-5 py-3 dark:bg-[linear-gradient(135deg,color-mix(in_srgb,var(--site-primary-color)_12%,transparent),color-mix(in_srgb,var(--site-secondary-color)_10%,transparent)_92%)]">
          <div class="flex items-center justify-between gap-4">
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">云屿内容管理系统</p>
              <p class="truncate text-xs text-slate-600 dark:text-slate-300">{{ systemSlogan }}</p>
            </div>
            <UBadge color="primary" variant="soft" class="rounded-[8px] px-3 py-1">Yunyu Admin</UBadge>
          </div>
        </div>
      </div>

      <div class="min-h-0 flex-1 overflow-y-auto px-0 pt-4 [scrollbar-width:thin] [&::-webkit-scrollbar]:w-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:rounded-full [&::-webkit-scrollbar-thumb]:bg-slate-300/80 dark:[&::-webkit-scrollbar-thumb]:bg-slate-600/80">
        <slot />
      </div>
    </div>
  </div>
</template>
