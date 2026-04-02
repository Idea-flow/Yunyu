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
  <div class="space-y-4">
    <section class="overflow-hidden rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
      <div class="flex flex-col gap-4 px-5 py-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <h1 class="truncate text-base font-semibold text-slate-900 dark:text-slate-50">后台首页</h1>
          <p class="mt-1 text-xs text-slate-500 dark:text-slate-400">
            {{ auth.currentUser?.userName || auth.currentUser?.email || '已登录' }}
          </p>
        </div>

        <div class="flex items-center gap-2">
          <UBadge color="success" variant="soft" class="rounded-[8px] px-3 py-1">已登录</UBadge>
          <UButton
            icon="i-lucide-log-out"
            label="退出登录"
            color="neutral"
            variant="ghost"
            class="rounded-[10px]"
            @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
          />
        </div>
      </div>
    </section>

    <div class="space-y-4">
      <section class="grid gap-4 md:grid-cols-3">
        <div
          v-for="card in overviewCards"
          :key="card.title"
          class="rounded-[16px] border border-white/55 bg-white/70 p-5 shadow-[0_16px_32px_-30px_rgba(15,23,42,0.14)] backdrop-blur-lg dark:border-white/10 dark:bg-white/5 dark:shadow-none"
        >
          <div class="flex items-center justify-between gap-3">
            <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
            <UBadge :color="card.color" variant="soft" class="rounded-[8px] px-2.5 py-1">{{ card.value }}</UBadge>
          </div>
          <p class="mt-5 text-3xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
        </div>
      </section>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">
        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <div class="mb-4 flex items-center justify-between gap-3">
              <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">快捷入口</h2>
              <AdminPrimaryButton label="新建文章" icon="i-lucide-plus" @click="navigateTo('/admin/posts')" />
            </div>

            <div class="grid gap-3 md:grid-cols-3">
              <button
                v-for="action in quickActions"
                :key="action.title"
                type="button"
                class="flex items-center gap-3 rounded-[14px] border border-white/60 bg-white/56 p-4 text-left backdrop-blur-md transition duration-200 hover:border-white/80 hover:bg-white/72 dark:border-white/10 dark:bg-white/5 dark:hover:border-white/15 dark:hover:bg-white/8"
                @click="navigateTo(action.to)"
              >
                <div class="flex size-9 shrink-0 items-center justify-center rounded-[9px] bg-slate-100/90 text-slate-700 dark:bg-white/5 dark:text-slate-200">
                  <UIcon :name="action.icon" class="size-4" />
                </div>
                <span class="text-sm font-medium text-slate-900 dark:text-slate-50">{{ action.title }}</span>
              </button>
            </div>
        </section>

        <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
            <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">当前账号</h2>
            <div class="mt-4 space-y-3">
              <div class="rounded-[14px] border border-white/60 bg-white/56 p-4 backdrop-blur-md dark:border-white/10 dark:bg-white/5">
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

      <section class="rounded-[18px] border border-white/55 bg-[linear-gradient(180deg,rgba(255,255,255,0.78),rgba(255,255,255,0.6))] p-5 shadow-[0_18px_36px_-30px_rgba(15,23,42,0.16)] backdrop-blur-xl dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.76),rgba(15,23,42,0.66))] dark:shadow-[0_20px_40px_-32px_rgba(0,0,0,0.42)]">
          <h2 class="text-base font-semibold text-slate-900 dark:text-slate-50">待处理</h2>
          <div class="mt-4 grid gap-3 md:grid-cols-3">
            <div
              v-for="item in pendingItems"
              :key="item"
              class="rounded-[14px] border border-white/60 bg-white/56 p-4 text-sm text-slate-700 backdrop-blur-md dark:border-white/10 dark:bg-white/5 dark:text-slate-300"
            >
              {{ item }}
            </div>
          </div>
      </section>
    </div>
  </div>
</template>
