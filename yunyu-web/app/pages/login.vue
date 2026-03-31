<script setup lang="ts">
/**
 * 共享登录页。
 * 负责承接前台与后台统一登录入口，登录成功后根据来源或回跳地址进入对应区域，
 * 避免前后台分别维护两套登录界面和交互逻辑。
 */
const route = useRoute()
const toast = useToast()
const auth = useAuth()

await auth.fetchCurrentUser()

const formState = reactive({
  account: '',
  password: '',
  remember: true
})

const isSubmitting = ref(false)

/**
 * 计算登录页提示信息。
 * 用于在未登录跳转或非站长访问后台被拦截时，向用户展示清晰原因。
 */
const pageAlert = computed(() => {
  if (route.query.reason === 'not-owner') {
    return {
      color: 'error' as const,
      title: '当前账号不是站长，不允许进入后台',
      description: '后台首页只允许站长角色访问。若你需要普通用户登录，请继续使用共享登录页完成前台登录。'
    }
  }

  if (route.query.reason === 'login-required') {
    return {
      color: 'warning' as const,
      title: '访问后台前需要先登录',
      description: '请先完成共享登录，再根据回跳地址进入后台首页。'
    }
  }

  return null
})

/**
 * 计算登录成功后的回跳地址。
 * 当前优先读取 redirect 参数，若没有则默认进入后台首页，
 * 后续接入真实用户体系后可根据角色与来源进一步扩展。
 */
const redirectPath = computed(() => {
  const redirect = route.query.redirect

  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    return redirect
  }

  return '/admin'
})

/**
 * 提交登录表单。
 * 当前阶段先完成交互链路与页面跳转演示，后续接入认证接口后保留页面结构不变。
 */
async function handleLogin() {
  if (!formState.account || !formState.password) {
    toast.add({
      title: '请补全账号与密码',
      description: '共享登录页会同时服务前台与后台，因此需要完整的登录凭证。',
      color: 'warning'
    })
    return
  }

  isSubmitting.value = true

  try {
    const user = await auth.login({
      account: formState.account,
      password: formState.password
    })

    if (!user) {
      throw new Error('未获取到当前登录用户')
    }

    if (redirectPath.value.startsWith('/admin') && user.role !== 'SUPER_ADMIN') {
      toast.add({
        title: '当前账号不是站长，不允许进入后台',
        description: '后台首页仅允许站长角色访问。你已完成登录，但没有后台访问权限。',
        color: 'error'
      })
      return
    }

    toast.add({
      title: '登录成功',
      description: `正在进入 ${redirectPath.value}。`,
      color: 'success'
    })

    await navigateTo(redirectPath.value)
  } catch (error: any) {
    toast.add({
      title: '登录失败',
      description: error?.message || '登录过程中发生错误，请稍后重试。',
      color: 'error'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="min-h-screen bg-default text-default">
    <div class="mx-auto flex min-h-screen w-full max-w-7xl items-center px-6 py-12">
      <div class="grid w-full overflow-hidden rounded-[32px] border border-default bg-elevated shadow-2xl shadow-slate-900/5 lg:grid-cols-[1.05fr_0.95fr]">
        <section class="relative overflow-hidden border-b border-default p-8 lg:border-b-0 lg:border-r lg:p-12">
          <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(56,189,248,0.14),transparent_38%),linear-gradient(180deg,rgba(255,255,255,0.06),transparent)]" />
          <div class="relative flex h-full flex-col justify-between gap-12">
            <div class="space-y-6">
              <div class="flex items-center justify-between gap-4">
                <div>
                  <p class="text-sm uppercase tracking-[0.28em] text-primary">Yunyu Login</p>
                  <h1 class="mt-4 text-4xl font-semibold leading-tight text-highlighted lg:text-5xl">
                    前后台共用的统一登录入口
                  </h1>
                </div>
                <ThemeModeSwitch />
              </div>

              <p class="max-w-2xl text-base leading-8 text-toned">
                该页面用于承接内容站前台用户登录与后台管理员登录。登录成功后，会根据回跳参数或身份角色进入对应区域，避免维护两套重复登录流程。
              </p>
            </div>

            <div class="grid gap-4 md:grid-cols-3">
              <UCard>
                <p class="text-sm font-medium text-muted">入口统一</p>
                <p class="mt-3 text-lg font-semibold text-highlighted">前台与后台共用</p>
              </UCard>
              <UCard>
                <p class="text-sm font-medium text-muted">默认回跳</p>
                <p class="mt-3 text-lg font-semibold text-highlighted">{{ redirectPath }}</p>
              </UCard>
              <UCard>
                <p class="text-sm font-medium text-muted">主题模式</p>
                <p class="mt-3 text-lg font-semibold text-highlighted">light / dark / system</p>
              </UCard>
            </div>
          </div>
        </section>

        <section class="p-8 lg:p-12">
          <UCard
            :ui="{
              body: 'space-y-6 p-6 sm:p-8'
            }"
          >
            <template #header>
            <div class="space-y-2">
              <p class="text-xl font-semibold text-highlighted">登录你的 Yunyu 账户</p>
              <p class="text-sm leading-7 text-muted">
                  当前是共享登录页，现已接入真实后端认证接口，并支持后台回跳与站长权限校验。
                </p>
              </div>
            </template>

            <form class="space-y-5" @submit.prevent="handleLogin">
              <UAlert
                v-if="pageAlert"
                :color="pageAlert.color"
                :title="pageAlert.title"
                :description="pageAlert.description"
                variant="soft"
              />

              <UFormField name="account" label="账号" description="支持邮箱、用户名或后续统一账号标识。">
                <UInput
                  v-model="formState.account"
                  size="xl"
                  placeholder="请输入账号"
                />
              </UFormField>

              <UFormField name="password" label="密码" description="后续将连接后端认证接口进行真实校验。">
                <UInput
                  v-model="formState.password"
                  type="password"
                  size="xl"
                  placeholder="请输入密码"
                />
              </UFormField>

              <div class="flex items-center justify-between gap-4 rounded-2xl border border-default bg-default px-4 py-3">
                <UCheckbox
                  v-model="formState.remember"
                  label="记住当前设备"
                />
                <ULink class="text-sm text-primary" to="/login?redirect=/admin">
                  管理员快捷进入
                </ULink>
              </div>

              <div class="flex flex-col gap-3 sm:flex-row">
                <button
                  type="submit"
                  class="inline-flex min-h-14 w-full items-center justify-center rounded-2xl bg-sky-500 px-6 text-base font-semibold text-white shadow-sm transition hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-70 sm:flex-1 dark:bg-sky-400 dark:text-slate-950 dark:hover:bg-sky-300"
                  :disabled="isSubmitting"
                >
                  {{ isSubmitting ? '登录中...' : '登录并继续' }}
                </button>
                <UButton
                  type="button"
                  size="xl"
                  color="neutral"
                  variant="outline"
                  class="w-full justify-center rounded-2xl sm:flex-1"
                  :to="auth.isSuperAdmin ? '/admin' : '/login?redirect=/admin'"
                >
                  查看后台首页
                </UButton>
              </div>
            </form>
          </UCard>
        </section>
      </div>
    </div>
  </main>
</template>
