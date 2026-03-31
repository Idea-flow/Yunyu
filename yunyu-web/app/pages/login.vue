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
      title: '请输入账号和密码',
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
        title: '当前账号无后台访问权限',
        color: 'error'
      })
      return
    }

    toast.add({
      title: '登录成功',
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
    <div class="mx-auto flex min-h-screen w-full max-w-7xl items-center justify-center px-6 py-12">
      <div class="w-full max-w-5xl overflow-hidden rounded-[32px] border border-default bg-elevated shadow-2xl shadow-slate-900/5">
        <div class="grid lg:grid-cols-[1.05fr_0.95fr]">
          <section class="relative overflow-hidden border-b border-default p-8 lg:border-b-0 lg:border-r lg:p-12">
            <div class="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(56,189,248,0.14),transparent_38%),linear-gradient(180deg,rgba(255,255,255,0.06),transparent)]" />
            <div class="relative flex h-full flex-col justify-between gap-12">
              <div class="space-y-6">
                <div>
                  <p class="text-sm uppercase tracking-[0.28em] text-primary">Yunyu</p>
                  <h1 class="mt-4 text-4xl font-semibold leading-tight text-highlighted lg:text-5xl">
                    欢迎回来
                  </h1>
                </div>

                <p class="max-w-2xl text-base leading-8 text-toned">
                  登录后即可继续访问你的内容与账户。
                </p>
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
                  <p class="text-xl font-semibold text-highlighted">登录你的账户</p>
                </div>
              </template>

              <form class="space-y-6" @submit.prevent="handleLogin">
                <UFormField name="account" label="账号">
                <UInput
                  v-model="formState.account"
                  size="xl"
                  placeholder="请输入账号"
                  class="w-full"
                  :ui="{
                    base: 'w-full rounded-2xl'
                  }"
                />
              </UFormField>

                <UFormField name="password" label="密码">
                <UInput
                  v-model="formState.password"
                  type="password"
                  size="xl"
                  placeholder="请输入密码"
                  class="w-full"
                  :ui="{
                    base: 'w-full rounded-2xl'
                  }"
                />
              </UFormField>

                <button
                  type="submit"
                  class="inline-flex min-h-14 w-full items-center justify-center rounded-2xl bg-sky-500 px-6 text-base font-semibold text-white shadow-lg shadow-sky-500/20 transition hover:bg-sky-600 focus:outline-none focus:ring-2 focus:ring-sky-400 focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-70 dark:bg-sky-400 dark:text-slate-950 dark:shadow-sky-400/10 dark:hover:bg-sky-300 dark:focus:ring-sky-300 dark:focus:ring-offset-slate-950"
                  :disabled="isSubmitting"
                >
                  {{ isSubmitting ? '登录中...' : '登录' }}
                </button>
              </form>
            </UCard>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
