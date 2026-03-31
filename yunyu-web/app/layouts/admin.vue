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
    label: '用户管理',
    icon: 'i-lucide-users',
    to: '/admin/users',
    description: '账号与权限维护'
  },
  {
    label: '站点设置',
    icon: 'i-lucide-settings-2',
    to: '/admin',
    description: '配置入口预留'
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
    class="relative min-h-screen bg-[radial-gradient(circle_at_0%_0%,rgba(56,189,248,0.12),transparent_24rem),radial-gradient(circle_at_100%_0%,rgba(14,165,233,0.08),transparent_18rem),linear-gradient(180deg,rgba(248,250,252,0.94)_0%,rgba(239,246,255,0.98)_100%)] before:pointer-events-none before:absolute before:inset-0 before:bg-[linear-gradient(rgba(148,163,184,0.06)_1px,transparent_1px),linear-gradient(90deg,rgba(148,163,184,0.06)_1px,transparent_1px)] before:bg-[size:48px_48px] before:[mask-image:linear-gradient(180deg,rgba(0,0,0,0.38),transparent_85%)] after:pointer-events-none after:absolute after:inset-0 after:bg-[radial-gradient(circle_at_14%_8%,rgba(14,165,233,0.12),transparent_18rem),radial-gradient(circle_at_86%_12%,rgba(56,189,248,0.08),transparent_16rem)] dark:bg-[radial-gradient(circle_at_0%_0%,rgba(56,189,248,0.12),transparent_24rem),radial-gradient(circle_at_100%_0%,rgba(14,165,233,0.08),transparent_18rem),linear-gradient(180deg,rgba(2,6,23,0.98)_0%,rgba(15,23,42,0.98)_100%)]"
  >
    <UDashboardSidebar collapsible resizable class="border-none bg-transparent p-3">
      <template #header="{ collapsed }">
        <div class="relative overflow-hidden rounded-[30px] border border-slate-200/80 bg-white/80 p-3 shadow-[0_24px_64px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl after:pointer-events-none after:absolute after:inset-0 after:bg-[linear-gradient(180deg,rgba(255,255,255,0.18),transparent_20%,transparent_80%,rgba(255,255,255,0.04))] dark:border-slate-800 dark:bg-slate-950/75 dark:shadow-[0_28px_72px_-40px_rgba(0,0,0,0.62)]">
          <div class="flex items-center gap-3 px-1 py-1">
            <div class="flex size-11 items-center justify-center rounded-[18px] bg-[radial-gradient(circle_at_30%_25%,rgba(255,255,255,0.48),transparent_48%),linear-gradient(135deg,#0ea5e9,#7dd3fc)] text-sm font-semibold text-white shadow-[0_16px_32px_-20px_rgba(14,165,233,0.8)]">
              Y
            </div>

            <div v-if="!collapsed" class="min-w-0">
              <p class="truncate text-sm font-semibold text-slate-900 dark:text-slate-50">云屿后台</p>
              <p class="truncate text-xs text-slate-500 dark:text-slate-400">Editorial workspace</p>
            </div>

            <div
              v-if="!collapsed"
              class="ms-auto flex size-2.5 shrink-0 rounded-full bg-emerald-400 shadow-[0_0_0_6px_rgba(16,185,129,0.12)]"
            />
          </div>
        </div>
      </template>

      <template #default="{ collapsed }">
        <div class="relative flex h-full min-h-0 flex-col overflow-hidden rounded-[30px] border border-slate-200/80 bg-white/80 p-3 shadow-[0_24px_64px_-34px_rgba(15,23,42,0.24)] backdrop-blur-xl after:pointer-events-none after:absolute after:inset-0 after:bg-[linear-gradient(180deg,rgba(255,255,255,0.18),transparent_20%,transparent_80%,rgba(255,255,255,0.04))] dark:border-slate-800 dark:bg-slate-950/75 dark:shadow-[0_28px_72px_-40px_rgba(0,0,0,0.62)]">
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
                'relative flex min-h-12 items-center gap-3.5 rounded-[1.1rem] border px-4 py-3 text-slate-600 transition duration-200 hover:translate-x-0.5 dark:text-slate-300',
                isActiveItem(item)
                  ? 'border-sky-200 bg-sky-50/90 text-slate-900 shadow-[0_14px_30px_-22px_rgba(14,165,233,0.5)] before:absolute before:start-2 before:top-1/2 before:h-6 before:w-1 before:-translate-y-1/2 before:rounded-full before:bg-sky-500 dark:border-sky-400/25 dark:bg-sky-400/10 dark:text-slate-50'
                  : 'border-transparent hover:border-slate-200 hover:bg-white/55 hover:text-slate-900 dark:hover:border-slate-700 dark:hover:bg-white/5 dark:hover:text-slate-50'
              ]"
              :aria-label="collapsed ? item.label : undefined"
            >
              <div class="flex size-9 shrink-0 items-center justify-center rounded-2xl bg-white/60 text-slate-900 dark:bg-white/5 dark:text-slate-50">
                <UIcon :name="item.icon" class="size-[18px]" />
              </div>

              <div v-if="!collapsed" class="min-w-0">
                <p class="truncate text-sm font-semibold">{{ item.label }}</p>
                <p class="truncate text-xs text-slate-500 dark:text-slate-400">{{ item.description }}</p>
              </div>
            </NuxtLink>
          </nav>

          <div class="mt-4 space-y-3 border-t border-slate-200 pt-4 dark:border-slate-800">
            <div v-if="!collapsed" class="rounded-[24px] border border-slate-200/80 bg-slate-50/80 p-3 dark:border-slate-700 dark:bg-slate-900/70">
              <div class="flex items-center gap-3">
                <div class="flex size-10 items-center justify-center rounded-2xl bg-white/70 text-slate-900 dark:bg-white/10 dark:text-slate-50">
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

            <div v-if="!collapsed" class="space-y-3 rounded-[24px] border border-slate-200 p-3 dark:border-slate-800">
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
              class="rounded-2xl"
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
