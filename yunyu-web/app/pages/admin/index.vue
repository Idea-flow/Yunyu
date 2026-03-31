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
    to: '/admin/posts'
  },
  {
    title: '更新站点信息',
    description: '查看工作台状态与后续站点设置入口。',
    to: '/admin'
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
            <UBadge color="success" variant="soft" class="rounded-full px-3 py-1">系统已就绪</UBadge>
            <UButton
              icon="i-lucide-log-out"
              label="退出登录"
              color="neutral"
              variant="ghost"
              class="rounded-2xl"
              @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
            />
            <UButton
              icon="i-lucide-log-in"
              label="共享登录页"
              color="neutral"
              variant="outline"
              class="rounded-2xl"
              to="/login?redirect=/admin"
            />
          </div>
        </template>
      </UDashboardNavbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <UCard class="admin-surface-card overflow-hidden rounded-[34px]">
          <div class="grid gap-6 lg:grid-cols-[1.25fr_0.75fr] lg:items-end">
            <div class="space-y-5">
              <div class="space-y-3">
                <p class="admin-kicker">Yunyu Admin Workspace</p>
                <h1 class="max-w-2xl text-3xl font-semibold tracking-tight text-[color:var(--admin-text-strong)] lg:text-[2.4rem]">
                  以更安静、更稳定的方式管理内容、用户与站点节奏。
                </h1>
                <p class="max-w-2xl text-sm leading-8 text-[color:var(--admin-text-body)]">
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
                  class="rounded-2xl"
                  to="/admin/users"
                />
              </div>
            </div>

            <div class="admin-muted-panel relative overflow-hidden rounded-[28px] p-5">
              <div class="admin-grid-fade absolute inset-0 opacity-70" />
              <div class="relative space-y-4">
                <div>
                  <p class="admin-kicker">当前站长</p>
                  <p class="mt-2 text-xl font-semibold text-[color:var(--admin-text-strong)]">
                    {{ auth.currentUser?.userName || '未知用户' }}
                  </p>
                  <p class="mt-1 text-sm text-[color:var(--admin-text-muted)]">
                    {{ auth.currentUser?.email || '请检查当前登录态' }}
                  </p>
                </div>

                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-[22px] border border-[color:var(--admin-line)] bg-white/55 p-4 dark:bg-white/5">
                    <p class="text-xs uppercase tracking-[0.18em] text-[color:var(--admin-text-muted)]">主题</p>
                    <p class="mt-2 text-sm font-medium text-[color:var(--admin-text-strong)]">亮暗模式已统一</p>
                  </div>
                  <div class="rounded-[22px] border border-[color:var(--admin-line)] bg-white/55 p-4 dark:bg-white/5">
                    <p class="text-xs uppercase tracking-[0.18em] text-[color:var(--admin-text-muted)]">权限</p>
                    <p class="mt-2 text-sm font-medium text-[color:var(--admin-text-strong)]">站长访问已启用</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </UCard>

        <div class="grid gap-4 md:grid-cols-3">
          <UCard
            v-for="card in overviewCards"
            :key="card.title"
            class="admin-surface-card overflow-hidden rounded-[28px]"
          >
            <div class="space-y-4">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-medium text-[color:var(--admin-text-muted)]">{{ card.title }}</p>
                <UBadge :color="card.color" variant="soft" class="rounded-full px-3 py-1">{{ card.description }}</UBadge>
              </div>
              <p class="text-4xl font-semibold tracking-tight text-[color:var(--admin-text-strong)]">{{ card.value }}</p>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
          <UCard class="admin-surface-card rounded-[30px]">
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="admin-kicker">快捷入口</p>
                  <p class="mt-1 text-base font-semibold text-[color:var(--admin-text-strong)]">快捷操作</p>
                  <p class="mt-1 text-sm text-[color:var(--admin-text-muted)]">以最少层级进入常用后台流程</p>
                </div>
                <UBadge color="primary" variant="soft" class="rounded-full px-3 py-1">第一阶段</UBadge>
              </div>
            </template>

            <div class="grid gap-4 md:grid-cols-3">
              <button
                v-for="action in quickActions"
                :key="action.title"
                type="button"
                class="admin-muted-panel cursor-pointer rounded-[24px] p-5 text-left transition duration-200 hover:-translate-y-0.5 hover:border-[color:var(--admin-line-strong)] hover:bg-white/70 dark:hover:bg-[rgba(255,255,255,0.06)]"
                @click="navigateTo(action.to)"
              >
                <p class="text-base font-semibold text-[color:var(--admin-text-strong)]">{{ action.title }}</p>
                <p class="mt-2 text-sm leading-7 text-[color:var(--admin-text-body)]">{{ action.description }}</p>
              </button>
            </div>
          </UCard>

          <UCard class="admin-surface-card rounded-[30px]">
            <template #header>
              <div>
                <p class="admin-kicker">发布节奏</p>
                <p class="mt-1 text-base font-semibold text-[color:var(--admin-text-strong)]">发布准备度</p>
                <p class="mt-1 text-sm text-[color:var(--admin-text-muted)]">当前站点内容准备情况概览</p>
              </div>
            </template>

            <div class="space-y-5">
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-[color:var(--admin-text-body)]">首页内容完成度</span>
                  <span class="font-medium text-[color:var(--admin-text-strong)]">72%</span>
                </div>
                <UProgress :value="72" color="primary" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-[color:var(--admin-text-body)]">文章体系完成度</span>
                  <span class="font-medium text-[color:var(--admin-text-strong)]">58%</span>
                </div>
                <UProgress :value="58" color="warning" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-[color:var(--admin-text-body)]">后台配置完成度</span>
                  <span class="font-medium text-[color:var(--admin-text-strong)]">41%</span>
                </div>
                <UProgress :value="41" color="neutral" />
              </div>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <UCard class="admin-surface-card rounded-[30px]">
            <template #header>
              <div>
                <p class="admin-kicker">本周关注</p>
                <p class="mt-1 text-base font-semibold text-[color:var(--admin-text-strong)]">待处理事项</p>
                <p class="mt-1 text-sm text-[color:var(--admin-text-muted)]">帮助站长快速定位本周最重要的后台任务</p>
              </div>
            </template>

            <ul class="space-y-4">
              <li
                v-for="item in pendingItems"
                :key="item"
                class="admin-muted-panel rounded-[22px] px-4 py-4 text-sm leading-7 text-[color:var(--admin-text-body)]"
              >
                {{ item }}
              </li>
            </ul>
          </UCard>

          <UCard class="admin-surface-card rounded-[30px]">
            <template #header>
              <div>
                <p class="admin-kicker">系统状态</p>
                <p class="mt-1 text-base font-semibold text-[color:var(--admin-text-strong)]">系统状态</p>
                <p class="mt-1 text-sm text-[color:var(--admin-text-muted)]">用于展示当前工作台的基础环境与登录态情况</p>
              </div>
            </template>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="admin-muted-panel rounded-[22px] p-4">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-[color:var(--admin-text-muted)]">前端状态</p>
                <p class="mt-3 text-lg font-semibold text-[color:var(--admin-text-strong)]">Nuxt 4 / Nuxt UI 已接入</p>
                <p class="mt-2 text-sm leading-7 text-[color:var(--admin-text-body)]">后台工作台已具备统一布局、侧边导航和主题基础能力。</p>
              </div>
              <div class="admin-muted-panel rounded-[22px] p-4">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-[color:var(--admin-text-muted)]">认证状态</p>
                <p class="mt-3 text-lg font-semibold text-[color:var(--admin-text-strong)]">
                  {{ auth.currentUser?.userName || '未知用户' }}
                </p>
                <p class="mt-2 text-sm leading-7 text-[color:var(--admin-text-body)]">
                  当前后台仅允许站长角色进入，已接入真实登录态校验与管理员访问控制。
                </p>
              </div>
            </div>
          </UCard>
        </div>
      </div>
    </template>
  </UDashboardPanel>
</template>
