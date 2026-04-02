<script setup lang="ts">
/**
 * 后台导航项类型。
 * 作用：约束后台侧边栏导航所需的最小数据结构，保证渲染层语义稳定。
 */
interface AdminNavigationItem {
  label: string
  icon: string
  to: string
  description: string
}

/**
 * 后台布局组件。
 * 负责承载 Yunyu 后台区域的导航、主题切换、身份展示与统一框架，
 * 作为所有后台页面共享的布局基础。
 */
const route = useRoute()
const auth = useAuth()

const navigationItems: AdminNavigationItem[] = [
  {
    label: '后台首页',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin',
    description: '总览与工作台'
  },
  {
    label: '文章管理',
    icon: 'i-lucide-files',
    to: '/admin/posts',
    description: '内容编辑与状态管理'
  },
  {
    label: '分类管理',
    icon: 'i-lucide-folders',
    to: '/admin/categories',
    description: '分类维护与排序'
  },
  {
    label: '标签管理',
    icon: 'i-lucide-tags',
    to: '/admin/tags',
    description: '标签整理与状态维护'
  },
  {
    label: '专题管理',
    icon: 'i-lucide-book-open-text',
    to: '/admin/topics',
    description: '专题编排与展示顺序'
  },
  {
    label: '用户管理',
    icon: 'i-lucide-users',
    to: '/admin/users',
    description: '账号与权限维护'
  },
  {
    label: '系统监控',
    icon: 'i-lucide-activity',
    to: '/admin/system',
    description: 'JVM 与运行状态查看'
  },
  {
    label: '站点设置',
    icon: 'i-lucide-settings-2',
    to: '/admin/site',
    description: '品牌、SEO 与主题配置'
  }
]

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
 * 退出当前登录态。
 * 完成清理后回到共享登录页，方便再次进入后台。
 */
async function handleLogout() {
  await auth.logout()
  await navigateTo('/login?redirect=/admin')
}
</script>

<template>
  <UDashboardGroup
    storage="cookie"
    storage-key="yunyu-admin-dashboard"
    class="admin-workspace relative min-h-screen"
  >
    <UDashboardSidebar collapsible resizable class="min-w-[272px] border-none bg-transparent p-3 lg:p-4">
      <template #header="{ collapsed }">
        <div class="admin-shell overflow-hidden p-3">
          <div class="flex items-center gap-3 px-1 py-1">
            <div class="flex size-11 items-center justify-center rounded-[12px] bg-sky-500 text-sm font-semibold text-white shadow-[0_14px_24px_-18px_rgba(14,165,233,0.7)] dark:bg-sky-400 dark:text-slate-950">
              Y
            </div>

            <div v-if="!collapsed" class="min-w-0">
              <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">云屿后台</p>
              <p class="truncate text-xs text-slate-500 dark:text-slate-400">内容工作台</p>
            </div>

            <div
              v-if="!collapsed"
              class="ms-auto flex size-2.5 shrink-0 rounded-full bg-emerald-400 shadow-[0_0_0_5px_rgba(16,185,129,0.12)]"
            />
          </div>
        </div>
      </template>

      <template #default="{ collapsed }">
        <div class="admin-shell flex h-full min-h-0 flex-col overflow-hidden p-3">
          <div v-if="!collapsed" class="px-2 pb-3 pt-1">
            <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">导航</p>
            <p class="mt-2 text-sm leading-6 text-slate-500 dark:text-slate-400">
              内容创作、运营配置与站点管理的统一工作台。
            </p>
          </div>

          <nav class="flex flex-1 flex-col gap-2 overflow-y-auto px-1 pb-1">
            <NuxtLink
              v-for="item in navigationItems"
              :key="item.to"
              :to="item.to"
              :class="[
                'relative flex min-h-12 items-center gap-3 rounded-[12px] border px-3.5 py-3 text-slate-600 transition duration-200 dark:text-slate-300',
                isActiveItem(item)
                  ? 'border-sky-200 bg-sky-50 text-slate-900 before:absolute before:start-2 before:top-1/2 before:h-6 before:w-1 before:-translate-y-1/2 before:rounded-full before:bg-sky-500 dark:border-sky-400/25 dark:bg-sky-400/10 dark:text-slate-50'
                  : 'border-transparent hover:border-slate-200 hover:bg-slate-50 hover:text-slate-900 dark:hover:border-slate-700 dark:hover:bg-white/5 dark:hover:text-slate-50'
              ]"
              :aria-label="collapsed ? item.label : undefined"
            >
              <div class="flex size-9 shrink-0 items-center justify-center rounded-[10px] bg-slate-100 text-slate-900 dark:bg-white/5 dark:text-slate-50">
                <UIcon :name="item.icon" class="size-[18px]" />
              </div>

              <div v-if="!collapsed" class="min-w-0 flex-1">
                <p class="text-sm font-semibold leading-5 text-slate-900 dark:text-slate-50">{{ item.label }}</p>
                <p class="hidden truncate text-xs text-slate-500 dark:text-slate-400 xl:block">{{ item.description }}</p>
              </div>
            </NuxtLink>
          </nav>

          <div class="mt-4 space-y-3 border-t border-slate-200 pt-4 dark:border-slate-800">
            <div v-if="!collapsed" class="admin-surface-soft p-3">
              <div class="flex items-center gap-3">
                <div class="flex size-10 items-center justify-center rounded-[10px] bg-white text-slate-900 dark:bg-white/10 dark:text-slate-50">
                  <UIcon name="i-lucide-user-round" class="size-[18px]" />
                </div>

                <div class="min-w-0">
                  <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">
                    {{ auth.currentUser?.userName || '站长账号' }}
                  </p>
                  <p class="truncate text-xs text-slate-500 dark:text-slate-400">
                    {{ auth.currentUser?.email || '内容运营权限已启用' }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="!collapsed" class="admin-surface-soft space-y-3 p-3">
              <div>
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">主题</p>
                <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">切换后台的明亮与暗黑阅读环境</p>
              </div>
              <ThemeModeSwitch />
            </div>

            <UButton
              :icon="collapsed ? 'i-lucide-log-out' : 'i-lucide-log-out'"
              :label="collapsed ? undefined : '退出登录'"
              color="neutral"
              variant="ghost"
              class="rounded-[12px] hover:bg-slate-100 dark:hover:bg-slate-900"
              block
              @click="handleLogout"
            />
          </div>
        </div>
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>
</template>
