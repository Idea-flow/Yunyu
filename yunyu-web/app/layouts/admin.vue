<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'

/**
 * 后台布局组件。
 * 负责承载 Yunyu 后台区域的导航、工具入口、主题切换与统一框架，
 * 作为所有后台页面共享的布局基础。
 */
const navigationItems = computed<NavigationMenuItem[]>(() => [
  {
    label: '后台首页',
    icon: 'i-lucide-layout-dashboard',
    to: '/admin'
  },
  {
    label: '内容创作',
    icon: 'i-lucide-file-plus-2',
    to: '/admin'
  },
  {
    label: '文章管理',
    icon: 'i-lucide-files',
    to: '/admin'
  },
  {
    label: '站点设置',
    icon: 'i-lucide-settings-2',
    to: '/admin'
  }
])
</script>

<template>
  <UDashboardGroup
    storage="cookie"
    storage-key="yunyu-admin-dashboard"
    class="min-h-screen bg-muted/40"
  >
    <UDashboardSidebar collapsible resizable>
      <template #header="{ collapsed }">
        <div class="flex items-center gap-3 px-1 py-1">
          <div
            class="flex size-10 items-center justify-center rounded-2xl bg-primary text-inverted shadow-sm"
          >
            Y
          </div>

          <div v-if="!collapsed" class="min-w-0">
            <p class="truncate text-sm font-semibold text-highlighted">云屿后台</p>
            <p class="truncate text-xs text-muted">内容运营与创作工作台</p>
          </div>
        </div>
      </template>

      <template #default="{ collapsed }">
        <UNavigationMenu
          :items="navigationItems"
          orientation="vertical"
          :ui="{
            root: 'w-full',
            link: collapsed ? 'justify-center' : 'justify-start'
          }"
        />
      </template>

      <template #footer="{ collapsed }">
        <div class="space-y-4">
          <div v-if="!collapsed" class="rounded-2xl border border-default bg-default p-3">
            <p class="text-xs font-medium text-muted">主题模式</p>
            <div class="mt-3">
              <ThemeModeSwitch />
            </div>
          </div>

          <UButton
            :icon="collapsed ? 'i-lucide-log-in' : 'i-lucide-log-in'"
            :label="collapsed ? undefined : '前往共享登录页'"
            color="neutral"
            variant="ghost"
            to="/login?redirect=/admin"
            block
          />
        </div>
      </template>
    </UDashboardSidebar>

    <slot />
  </UDashboardGroup>
</template>
