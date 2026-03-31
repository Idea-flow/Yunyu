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

const toast = useToast()
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
    description: '进入编辑流程，创建新的内容草稿。'
  },
  {
    title: '管理专题',
    description: '查看专题结构、排序与展示状态。'
  },
  {
    title: '更新站点信息',
    description: '调整导航、作者信息与页脚配置。'
  }
]

const pendingItems = [
  '有 2 篇文章缺少摘要与封面，需要补充后再发布。',
  '专题页“设计随笔”缺少头图与简介文案。',
  '站点首页推荐内容建议在本周内更新一次。'
]

/**
 * 处理快捷操作点击。
 * 当前阶段先用提示反馈占位，后续接入真实业务路由和接口。
 */
function handleQuickAction(title: string) {
  toast.add({
    title: `${title} 功能即将接入`,
    description: '当前阶段已完成页面交互占位，后续会连接到实际业务流程。',
    color: 'primary'
  })
}
</script>

<template>
  <UDashboardPanel>
    <template #header>
      <UDashboardNavbar title="后台首页">
        <template #right>
          <div class="flex items-center gap-3">
            <UBadge color="success" variant="soft">系统已就绪</UBadge>
            <UButton
              icon="i-lucide-log-out"
              label="退出登录"
              color="neutral"
              variant="ghost"
              @click="auth.logout().then(() => navigateTo('/login?redirect=/admin'))"
            />
            <UButton
              icon="i-lucide-log-in"
              label="共享登录页"
              color="neutral"
              variant="outline"
              to="/login?redirect=/admin"
            />
          </div>
        </template>
      </UDashboardNavbar>

      <UDashboardToolbar>
        <template #left>
          <div>
            <p class="text-sm font-medium text-highlighted">Yunyu Admin Workspace</p>
            <p class="text-xs text-muted">面向内容创作、运营配置与站点管理的统一工作台</p>
          </div>
        </template>
      </UDashboardToolbar>
    </template>

    <template #body>
      <div class="space-y-6 p-4 lg:p-6">
        <div class="grid gap-4 md:grid-cols-3">
          <UCard
            v-for="card in overviewCards"
            :key="card.title"
            class="overflow-hidden"
          >
            <div class="space-y-4">
              <div class="flex items-center justify-between gap-3">
                <p class="text-sm font-medium text-muted">{{ card.title }}</p>
                <UBadge :color="card.color" variant="soft">{{ card.description }}</UBadge>
              </div>
              <p class="text-4xl font-semibold tracking-tight text-highlighted">{{ card.value }}</p>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
          <UCard>
            <template #header>
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-base font-semibold text-highlighted">快捷操作</p>
                  <p class="text-sm text-muted">以最少层级进入常用后台流程</p>
                </div>
                <UBadge color="primary" variant="soft">第一阶段</UBadge>
              </div>
            </template>

            <div class="grid gap-4 md:grid-cols-3">
              <button
                v-for="action in quickActions"
                :key="action.title"
                type="button"
                class="rounded-2xl border border-default bg-default p-5 text-left transition hover:border-primary/40 hover:bg-muted"
                @click="handleQuickAction(action.title)"
              >
                <p class="text-base font-semibold text-highlighted">{{ action.title }}</p>
                <p class="mt-2 text-sm leading-7 text-toned">{{ action.description }}</p>
              </button>
            </div>
          </UCard>

          <UCard>
            <template #header>
              <div>
                <p class="text-base font-semibold text-highlighted">发布准备度</p>
                <p class="text-sm text-muted">当前站点内容准备情况概览</p>
              </div>
            </template>

            <div class="space-y-5">
              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-toned">首页内容完成度</span>
                  <span class="font-medium text-highlighted">72%</span>
                </div>
                <UProgress :value="72" color="primary" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-toned">文章体系完成度</span>
                  <span class="font-medium text-highlighted">58%</span>
                </div>
                <UProgress :value="58" color="warning" />
              </div>

              <div>
                <div class="mb-2 flex items-center justify-between text-sm">
                  <span class="text-toned">后台配置完成度</span>
                  <span class="font-medium text-highlighted">41%</span>
                </div>
                <UProgress :value="41" color="neutral" />
              </div>
            </div>
          </UCard>
        </div>

        <div class="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]">
          <UCard>
            <template #header>
              <div>
                <p class="text-base font-semibold text-highlighted">待处理事项</p>
                <p class="text-sm text-muted">帮助站长快速定位本周最重要的后台任务</p>
              </div>
            </template>

            <ul class="space-y-4">
              <li
                v-for="item in pendingItems"
                :key="item"
                class="rounded-2xl border border-default bg-default px-4 py-4 text-sm leading-7 text-toned"
              >
                {{ item }}
              </li>
            </ul>
          </UCard>

          <UCard>
            <template #header>
              <div>
                <p class="text-base font-semibold text-highlighted">系统状态</p>
                <p class="text-sm text-muted">第一阶段用于展示环境、主题与认证联调状态</p>
              </div>
            </template>

            <div class="grid gap-4 md:grid-cols-2">
              <div class="rounded-2xl border border-default bg-default p-4">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-muted">前端状态</p>
                <p class="mt-3 text-lg font-semibold text-highlighted">Nuxt 4 / Nuxt UI 已接入</p>
                <p class="mt-2 text-sm leading-7 text-toned">后台工作台已具备布局、导航和主题基础能力。</p>
              </div>
              <div class="rounded-2xl border border-default bg-default p-4">
                <p class="text-xs font-medium uppercase tracking-[0.18em] text-muted">认证状态</p>
                <p class="mt-3 text-lg font-semibold text-highlighted">
                  {{ auth.currentUser?.userName || '未知用户' }}
                </p>
                <p class="mt-2 text-sm leading-7 text-toned">
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
