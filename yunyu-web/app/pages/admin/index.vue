<script setup lang="ts">
/**
 * 后台首页。
 * 作用：作为后台工作台首页，承载概览数据、快捷入口和待处理事项。
 */
definePageMeta({
  layout: 'admin',
  middleware: 'admin'
})

const auth = useAuth()

const overviewCards = [
  {
    title: '已发布文章',
    value: '128',
    color: 'primary' as const
  },
  {
    title: '草稿',
    value: '09',
    color: 'warning' as const
  },
  {
    title: '专题',
    value: '06',
    color: 'success' as const
  }
]

const quickActions = [
  {
    title: '文章管理',
    to: '/admin/posts',
    icon: 'i-lucide-files'
  },
  {
    title: '用户管理',
    to: '/admin/users',
    icon: 'i-lucide-users'
  },
  {
    title: '站点设置',
    to: '/admin/site',
    icon: 'i-lucide-settings-2'
  }
]

const pendingItems = [
  '补充缺少封面的文章',
  '更新首页推荐内容',
  '检查专题页头图'
]
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="后台首页">
        <template #right>
          <div class="flex items-center gap-2">
            <UBadge color="success" variant="soft" class="rounded-[8px] px-3 py-1">已登录</UBadge>
            <UButton
              icon="i-lucide-log-out"
              label="退出登录"
              color="neutral"
              variant="ghost"
              class="rounded-[8px]"
              @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
            />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <section class="grid gap-4 md:grid-cols-3">
          <div
            v-for="card in overviewCards"
            :key="card.title"
            class="admin-surface p-5"
          >
            <div class="flex items-center justify-between gap-3">
              <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
              <UBadge :color="card.color" variant="soft" class="rounded-[8px] px-2.5 py-1">{{ card.value }}</UBadge>
            </div>
            <p class="mt-5 text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
          </div>
        </section>

        <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
          <section class="admin-surface p-5">
            <div class="mb-4 flex items-center justify-between gap-3">
              <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">快捷入口</h2>
              <AdminPrimaryButton label="新建文章" icon="i-lucide-plus" @click="navigateTo('/admin/posts')" />
            </div>

            <div class="grid gap-3 md:grid-cols-3">
              <button
                v-for="action in quickActions"
                :key="action.title"
                type="button"
                class="admin-surface-soft flex items-center gap-3 p-4 text-left transition duration-200 hover:border-slate-200 hover:bg-white/80 dark:hover:border-white/10 dark:hover:bg-white/5"
                @click="navigateTo(action.to)"
              >
                <div class="flex size-9 shrink-0 items-center justify-center rounded-[8px] bg-slate-100 text-slate-700 dark:bg-slate-900 dark:text-slate-200">
                  <UIcon :name="action.icon" class="size-4" />
                </div>
                <span class="text-sm font-medium text-slate-900 dark:text-slate-50">{{ action.title }}</span>
              </button>
            </div>
          </section>

          <section class="admin-surface p-5">
            <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">当前账号</h2>
            <div class="mt-4 space-y-3">
              <div class="admin-surface-soft p-4">
                <p class="text-sm font-semibold text-slate-900 dark:text-slate-50">
                  {{ auth.currentUser?.userName || '未知用户' }}
                </p>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                  {{ auth.currentUser?.email || '--' }}
                </p>
              </div>
              <ThemeModeSwitch />
            </div>
          </section>
        </div>

        <section class="admin-surface p-5">
          <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">待处理</h2>
          <div class="mt-4 grid gap-3 md:grid-cols-3">
            <div
              v-for="item in pendingItems"
              :key="item"
              class="admin-surface-soft p-4 text-sm text-slate-700 dark:text-slate-300"
            >
              {{ item }}
            </div>
          </div>
        </section>
      </div>
    </template>
  </UDashboardPanel>
</template>
