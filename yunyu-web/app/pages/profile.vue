<script setup lang="ts">
/**
 * 前台个人中心页。
 * 作用：为已登录用户提供一个稳定的身份展示入口，并承接导航栏下拉菜单中的“个人中心”跳转目标。
 */
const route = useRoute()
const auth = useAuth()

await auth.fetchCurrentUser(true)

/**
 * 计算当前页面展示的用户名称。
 *
 * @return 用户名称
 */
const profileName = computed(() => {
  return auth.currentUser.value?.userName || auth.currentUser.value?.email || '云屿用户'
})

/**
 * 计算当前页面展示的邮箱。
 *
 * @return 用户邮箱
 */
const profileEmail = computed(() => auth.currentUser.value?.email || '--')

/**
 * 计算当前页面展示的角色文案。
 *
 * @return 角色文案
 */
const profileRoleLabel = computed(() => {
  return auth.currentUser.value?.role === 'SUPER_ADMIN' ? '站长账号' : '普通用户'
})

/**
 * 计算当前页面展示的状态文案。
 *
 * @return 状态文案
 */
const profileStatusLabel = computed(() => {
  return auth.currentUser.value?.status === 'ACTIVE' ? '状态正常' : '状态异常'
})

/**
 * 计算用户头像首字母。
 *
 * @return 头像首字母
 */
const profileInitial = computed(() => profileName.value.slice(0, 1).toUpperCase())

/**
 * 退出当前登录态。
 * 作用：从个人中心直接退出后回到首页，并清理缓存的用户展示信息。
 */
async function handleLogout() {
  await auth.logout()
  await navigateTo('/')
}

if (!auth.currentUser.value) {
  await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`, {
    replace: true
  })
}
</script>

<template>
  <main class="bg-[linear-gradient(180deg,#f7fbff_0%,#eef6ff_38%,#f8fafc_100%)] pt-32 pb-20 dark:bg-[linear-gradient(180deg,#020617_0%,#0f172a_40%,#111827_100%)]">
    <div class="mx-auto flex w-full max-w-5xl flex-col gap-8 px-6 sm:px-8">
      <section class="overflow-hidden rounded-[32px] border border-white/70 bg-white/82 shadow-[0_28px_80px_-48px_rgba(15,23,42,0.28)] backdrop-blur-xl dark:border-white/10 dark:bg-white/6">
        <div class="grid gap-8 p-8 md:grid-cols-[1.1fr_0.9fr] md:p-10">
          <div class="space-y-6">
            <div class="inline-flex h-16 w-16 items-center justify-center rounded-[22px] bg-[linear-gradient(135deg,#38bdf8,#fb923c)] text-2xl font-semibold text-white shadow-[0_18px_40px_-24px_rgba(56,189,248,0.66)]">
              {{ profileInitial }}
            </div>

            <div class="space-y-3">
              <p class="text-sm uppercase tracking-[0.3em] text-sky-500 dark:text-sky-300">Profile</p>
              <h1 class="text-[clamp(2.2rem,1.9rem+1vw,3.2rem)] font-semibold tracking-[-0.05em] text-slate-950 dark:text-slate-50">
                {{ profileName }}
              </h1>
              <p class="max-w-2xl text-base leading-8 text-slate-600 dark:text-slate-300">
                这里集中展示当前登录用户的基本身份信息，后续可以继续扩展收藏、评论记录和账户设置等功能。
              </p>
            </div>

            <div class="flex flex-wrap items-center gap-3">
              <UBadge color="primary" variant="soft" class="rounded-full px-4 py-2">
                {{ profileRoleLabel }}
              </UBadge>
              <UBadge color="success" variant="soft" class="rounded-full px-4 py-2">
                {{ profileStatusLabel }}
              </UBadge>
            </div>
          </div>

          <div class="rounded-[28px] border border-slate-200/70 bg-slate-50/85 p-6 dark:border-white/10 dark:bg-slate-950/50">
            <dl class="space-y-5">
              <div>
                <dt class="text-xs uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">用户名</dt>
                <dd class="mt-2 text-lg font-semibold text-slate-950 dark:text-slate-50">
                  {{ profileName }}
                </dd>
              </div>

              <div>
                <dt class="text-xs uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">邮箱</dt>
                <dd class="mt-2 break-all text-base text-slate-700 dark:text-slate-200">
                  {{ profileEmail }}
                </dd>
              </div>

              <div>
                <dt class="text-xs uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">角色</dt>
                <dd class="mt-2 text-base text-slate-700 dark:text-slate-200">
                  {{ auth.currentUser?.role || '--' }}
                </dd>
              </div>

              <div>
                <dt class="text-xs uppercase tracking-[0.22em] text-slate-400 dark:text-slate-500">状态</dt>
                <dd class="mt-2 text-base text-slate-700 dark:text-slate-200">
                  {{ auth.currentUser?.status || '--' }}
                </dd>
              </div>
            </dl>

            <div class="mt-8 flex flex-wrap gap-3">
              <UButton
                label="返回首页"
                color="neutral"
                variant="soft"
                class="rounded-full"
                to="/"
              />
              <UButton
                v-if="auth.currentUser?.role === 'SUPER_ADMIN'"
                label="进入后台"
                color="primary"
                variant="soft"
                class="rounded-full"
                to="/admin"
              />
              <UButton
                label="退出登录"
                color="error"
                variant="soft"
                class="rounded-full"
                @click="handleLogout"
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
