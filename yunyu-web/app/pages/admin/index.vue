<script setup lang="ts">
/**
 * 后台首页。
 * 负责承载后台工作台的第一屏信息，包括运营总览、快捷操作、待处理事项和系统状态，
 * 作为后续文章管理、专题管理、站点设置等后台页面的入口页。
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
    description: '本月新增 12 篇',
    color: 'primary' as const
  },
  {
    title: '待完善草稿',
    value: '09',
    description: '包含 3 篇待补图内容',
    color: 'warning' as const
  },
  {
    title: '专题数量',
    value: '06',
    description: '其中 2 个专题正在筹备',
    color: 'success' as const
  }
]

const quickActions = [
  {
    title: '新建文章',
    description: '进入内容工作区，开始整理与创建文章。',
    to: '/admin/posts'
  },
  {
    title: '管理专题',
    description: '整理专题结构、排序与内容归属。',
    to: '/admin/topics'
  },
  {
    title: '更新站点信息',
    description: '更新品牌、SEO 与首页展示相关配置。',
    to: '/admin/site'
  }
]

const pendingItems = [
  '有 2 篇文章缺少摘要与封面，需要补充后再发布。',
  '专题页“设计随笔”缺少头图与简介文案。',
  '站点首页推荐内容建议在本周内更新一次。'
]

</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="后台首页">
        <template #right>
          <div class="flex items-center gap-3">
            <UBadge color="success" variant="soft" class="rounded-[8px] px-3 py-1">系统已就绪</UBadge>
  <UButton
    icon="i-lucide-log-out"
    label="退出登录"
    color="neutral"
    variant="ghost"
    class="rounded-[10px]"
    @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
  />
  <UButton
    icon="i-lucide-log-in"
    label="共享登录页"
    color="neutral"
    variant="outline"
    class="rounded-[10px]"
    to="/login?redirect=/admin"
  />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <section class="grid gap-6 lg:grid-cols-[1.25fr_0.75fr] lg:items-start">
          <div class="space-y-4">
            <div class="space-y-3">
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">Yunyu Admin Workspace</p>
              <h1 class="max-w-2xl text-3xl font-semibold tracking-tight text-slate-900 lg:text-[2.2rem] dark:text-slate-50">
                以更安静、更稳定的方式管理内容、用户与站点节奏。
              </h1>
              <p class="max-w-2xl text-sm leading-8 text-slate-600 dark:text-slate-300">
                后台保持编辑工作台的秩序感与操作优先级，让你在较低噪声的环境中完成创作、发布与配置管理。
              </p>
            </div>

            <div class="flex flex-wrap gap-3">
              <AdminPrimaryButton label="进入文章管理" icon="i-lucide-files" @click="navigateTo('/admin/posts')" />
              <UButton
                icon="i-lucide-users"
                label="查看用户管理"
                color="neutral"
                variant="outline"
                class="rounded-[10px]"
                to="/admin/users"
              />
            </div>
          </div>

          <section class="admin-surface-soft p-5">
            <div class="space-y-4">
              <div>
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">当前站长</p>
                <p class="mt-2 text-xl font-semibold text-slate-900 dark:text-slate-50">
                  {{ auth.currentUser?.userName || '未知用户' }}
                </p>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                  {{ auth.currentUser?.email || '请检查当前登录态' }}
                </p>
              </div>

              <div class="grid gap-3 sm:grid-cols-2">
                <div class="admin-surface p-4">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">主题</p>
                  <p class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">亮暗模式已统一</p>
                </div>
                <div class="admin-surface p-4">
                  <p class="text-xs tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">权限</p>
                  <p class="mt-2 text-sm font-medium text-slate-900 dark:text-slate-50">站长访问已启用</p>
                </div>
              </div>
            </div>
          </section>
        </section>

        <div class="grid gap-4 md:grid-cols-3">
          <section
            v-for="card in overviewCards"
            :key="card.title"
            class="admin-surface p-5"
          >
            <div class="space-y-4">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-medium text-slate-500 dark:text-slate-400">{{ card.title }}</p>
                <UBadge :color="card.color" variant="soft" class="rounded-[8px] px-3 py-1">{{ card.description }}</UBadge>
              </div>
              <p class="text-4xl font-semibold tracking-tight text-slate-900 dark:text-slate-50">{{ card.value }}</p>
            </div>
          </section>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
          <section class="admin-surface p-5">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">快捷入口</p>
                <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">快捷操作</p>
                <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">以最少层级进入常用后台流程</p>
              </div>
              <UBadge color="primary" variant="soft" class="rounded-[8px] px-3 py-1">当前可用</UBadge>
            </div>

            <div class="grid gap-4 md:grid-cols-3">
              <button
                v-for="action in quickActions"
                :key="action.title"
                type="button"
                class="admin-surface-soft cursor-pointer p-5 text-left transition duration-200 hover:border-slate-200/80 hover:bg-white/70 dark:hover:border-white/10 dark:hover:bg-white/4"
                @click="navigateTo(action.to)"
              >
                <p class="text-base font-semibold text-slate-900 dark:text-slate-50">{{ action.title }}</p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">{{ action.description }}</p>
              </button>
            </div>
          </section>

          <section class="admin-surface p-5">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">发布节奏</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">发布准备度</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">当前站点内容准备情况概览</p>
            </div>

            <div class="mt-5 space-y-5">
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-slate-600 dark:text-slate-300">首页内容完成度</span>
                  <span class="font-medium text-slate-900 dark:text-slate-50">72%</span>
                </div>
                <UProgress :value="72" color="primary" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-slate-600 dark:text-slate-300">文章体系完成度</span>
                  <span class="font-medium text-slate-900 dark:text-slate-50">58%</span>
                </div>
                <UProgress :value="58" color="warning" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-slate-600 dark:text-slate-300">后台配置完成度</span>
                  <span class="font-medium text-slate-900 dark:text-slate-50">41%</span>
                </div>
                <UProgress :value="41" color="neutral" />
              </div>
            </div>
          </section>
        </div>

        <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <section class="admin-surface p-5">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">本周关注</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">待处理事项</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">帮助站长快速定位本周最重要的后台任务</p>
            </div>

            <ul class="space-y-4">
              <li
                v-for="item in pendingItems"
                :key="item"
                class="admin-surface-soft px-4 py-4 text-sm leading-7 text-slate-600 dark:text-slate-300"
              >
                {{ item }}
              </li>
            </ul>
          </section>

          <section class="admin-surface p-5">
            <div>
              <p class="text-[0.72rem] font-semibold tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">系统状态</p>
              <p class="mt-1 text-base font-semibold text-slate-900 dark:text-slate-50">系统状态</p>
              <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">用于展示当前工作台的基础环境与登录态情况</p>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="admin-surface-soft p-4">
                <p class="text-xs font-medium tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">前端状态</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">Nuxt 4 / Nuxt UI 已接入</p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">后台工作台已具备统一布局、侧边导航和主题基础能力。</p>
              </div>
              <div class="admin-surface-soft p-4">
                <p class="text-xs font-medium tracking-[0.18em] text-slate-400 uppercase dark:text-slate-500">认证状态</p>
                <p class="mt-3 text-lg font-semibold text-slate-900 dark:text-slate-50">
                  {{ auth.currentUser?.userName || '未知用户' }}
                </p>
                <p class="mt-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
                  当前后台仅允许站长角色进入，已接入真实登录态校验与管理员访问控制。
                </p>
              </div>
            </div>
          </section>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
