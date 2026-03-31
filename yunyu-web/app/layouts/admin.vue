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
    class="admin-shell"
  >
    <UDashboardSidebar collapsible resizable class="border-none bg-transparent p-3">
      <template #header="{ collapsed }">
        <div class="admin-sidebar-panel rounded-[30px] p-3">
          <div class="flex items-center gap-3 px-1 py-1">
            <div class="admin-brand-badge flex size-11 items-center justify-center rounded-[18px] text-sm font-semibold text-white">
              Y
            </div>

            <div v-if="!collapsed" class="min-w-0">
              <p class="truncate text-sm font-semibold text-[color:var(--admin-text-strong)]">云屿后台</p>
              <p class="truncate text-xs text-[color:var(--admin-text-muted)]">Editorial workspace</p>
            </div>

            <div
              v-if="!collapsed"
              class="ms-auto flex size-2.5 shrink-0 rounded-full bg-emerald-400 shadow-[0_0_0_6px_rgba(16,185,129,0.12)]"
            />
          </div>
        </div>
      </template>

      <template #default="{ collapsed }">
        <div class="admin-sidebar-panel flex h-full min-h-0 flex-col rounded-[30px] p-3">
          <div v-if="!collapsed" class="px-2 pb-3 pt-1">
            <p class="admin-kicker">导航</p>
            <p class="mt-2 text-sm leading-6 text-[color:var(--admin-text-muted)]">
              内容创作、运营配置与站点管理的统一工作台。
            </p>
          </div>

          <nav class="flex flex-1 flex-col gap-2 overflow-y-auto px-1 pb-1">
            <NuxtLink
              v-for="item in navigationItems"
              :key="item.to"
              :to="item.to"
              :class="[
                'admin-nav-link',
                isActiveItem(item) ? 'admin-nav-link-active' : ''
              ]"
              :aria-label="collapsed ? item.label : undefined"
            >
              <div class="flex size-9 shrink-0 items-center justify-center rounded-2xl bg-white/50 text-[color:var(--admin-text-strong)] dark:bg-white/5">
                <UIcon :name="item.icon" class="size-[18px]" />
              </div>

              <div v-if="!collapsed" class="min-w-0">
                <p class="truncate text-sm font-semibold">{{ item.label }}</p>
                <p class="truncate text-xs text-[color:var(--admin-text-muted)]">{{ item.description }}</p>
              </div>
            </NuxtLink>
          </nav>

          <div class="mt-4 space-y-3 border-t border-[color:var(--admin-line)] pt-4">
            <div v-if="!collapsed" class="admin-muted-panel rounded-[24px] p-3">
              <div class="flex items-center gap-3">
                <div class="flex size-10 items-center justify-center rounded-2xl bg-white/70 text-[color:var(--admin-text-strong)] dark:bg-[rgba(255,255,255,0.08)]">
                  <UIcon name="i-lucide-user-round" class="size-[18px]" />
                </div>

                <div class="min-w-0">
                  <p class="truncate text-sm font-semibold text-[color:var(--admin-text-strong)]">
                    {{ auth.currentUser?.userName || '站长账号' }}
                  </p>
                  <p class="truncate text-xs text-[color:var(--admin-text-muted)]">
                    {{ auth.currentUser?.email || '内容运营权限已启用' }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="!collapsed" class="space-y-3 rounded-[24px] border border-[color:var(--admin-line)] p-3">
              <div>
                <p class="admin-kicker">主题</p>
                <p class="mt-1 text-xs text-[color:var(--admin-text-muted)]">切换后台的明亮与暗黑阅读环境</p>
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
