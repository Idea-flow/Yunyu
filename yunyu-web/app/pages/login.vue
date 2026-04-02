<script setup lang="ts">
/**
 * 共享认证页。
 * 负责承接前台与后台统一的登录、注册入口，并在成功后根据角色或回跳地址进入对应区域，
 * 避免前后台分别维护多套认证页面和交互逻辑。
 */
const route = useRoute()
const toast = useToast()
const auth = useAuth()

type AuthMode = 'login' | 'register'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)\S{8,20}$/

await auth.fetchCurrentUser()

const authMode = ref<AuthMode>('login')

const formState = reactive({
  account: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isSubmitting = ref(false)

/**
 * 计算当前认证模式下的主标题。
 *
 * @return 页面主标题
 */
const pageTitle = computed(() => {
  return authMode.value === 'login' ? '欢迎回来' : '创建账户'
})

/**
 * 计算当前认证模式下的说明文案。
 *
 * @return 页面说明文案
 */
const pageDescription = computed(() => {
  return authMode.value === 'login'
    ? '登录后即可继续访问你的内容与账户。'
    : '使用邮箱和密码快速创建新账户，注册成功后将自动登录。'
})

/**
 * 计算提交按钮文案。
 *
 * @return 提交按钮文案
 */
const submitButtonText = computed(() => {
  if (isSubmitting.value) {
    return authMode.value === 'login' ? '登录中...' : '注册中...'
  }

  return authMode.value === 'login' ? '登录' : '注册并进入'
})

/**
 * 切换认证模式。
 * 切换时会清空敏感输入，避免用户误把上一个模式的密码内容带入新的提交流程。
 *
 * @param mode 目标认证模式
 */
function switchAuthMode(mode: AuthMode) {
  if (authMode.value === mode) {
    return
  }

  authMode.value = mode
  formState.password = ''
  formState.confirmPassword = ''
}

/**
 * 解析认证成功后的回跳地址。
 * 若 URL 中显式传入 redirect，则优先尊重原始目标；
 * 否则站长进入后台，普通用户进入前台首页。
 *
 * @param user 当前登录用户
 * @return 最终跳转地址
 */
function resolvePostAuthPath(user: { role: string }) {
  const redirect = route.query.redirect

  if (typeof redirect === 'string' && redirect.startsWith('/')) {
    return redirect
  }

  return user.role === 'SUPER_ADMIN' ? '/admin' : '/'
}

/**
 * 校验注册表单。
 * 当前在前端提前拦截邮箱格式、密码强度和确认密码不一致等常见问题，
 * 减少无效请求，同时仍以后端校验作为最终兜底。
 *
 * @return 校验失败时返回错误提示，校验通过返回空值
 */
function validateRegisterForm() {
  if (!formState.email) {
    return '请输入注册邮箱'
  }

  if (!emailPattern.test(formState.email.trim())) {
    return '邮箱格式不正确'
  }

  if (!formState.password) {
    return '请输入注册密码'
  }

  if (!passwordPattern.test(formState.password)) {
    return '密码需为8-20位，且同时包含字母和数字'
  }

  if (formState.password !== formState.confirmPassword) {
    return '两次输入的密码不一致'
  }

  return null
}

/**
 * 提交认证表单。
 * 当前会根据模式分别调用登录或注册接口，并在成功后按角色和回跳地址进入对应页面。
 */
async function handleSubmit() {
  if (authMode.value === 'login' && (!formState.account || !formState.password)) {
    toast.add({
      title: '请输入账号和密码',
      color: 'warning'
    })
    return
  }

  if (authMode.value === 'register') {
    const validationMessage = validateRegisterForm()

    if (validationMessage) {
      toast.add({
        title: validationMessage,
        color: 'warning'
      })
      return
    }
  }

  isSubmitting.value = true

  try {
    const user = authMode.value === 'login'
      ? await auth.login({
          account: formState.account.trim(),
          password: formState.password
        })
      : await auth.register({
          email: formState.email.trim(),
          password: formState.password
        })

    if (!user) {
      throw new Error(authMode.value === 'login' ? '未获取到当前登录用户' : '注册成功后未获取到当前用户')
    }

    const targetPath = resolvePostAuthPath(user)

    if (targetPath.startsWith('/admin') && user.role !== 'SUPER_ADMIN') {
      toast.add({
        title: '当前账号无后台访问权限',
        color: 'error'
      })
      await navigateTo('/')
      return
    }

    toast.add({
      title: authMode.value === 'login' ? '登录成功' : '注册成功',
      color: 'success'
    })

    await navigateTo(targetPath)
  } catch (error: any) {
    toast.add({
      title: authMode.value === 'login' ? '登录失败' : '注册失败',
      description: error?.message || '认证过程中发生错误，请稍后重试。',
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
                    {{ pageTitle }}
                  </h1>
                </div>

                <p class="max-w-2xl text-base leading-8 text-toned">
                  {{ pageDescription }}
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
                  <p class="text-xl font-semibold text-highlighted">
                    {{ authMode === 'login' ? '登录你的账户' : '注册新账户' }}
                  </p>
                </div>
              </template>

              <div class="grid grid-cols-2 gap-2 rounded-2xl bg-default/70 p-1">
                <button
                  type="button"
                  class="rounded-2xl px-4 py-3 text-sm font-medium transition"
                  :class="authMode === 'login'
                    ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20'
                    : 'text-toned hover:bg-default/80'"
                  @click="switchAuthMode('login')"
                >
                  登录
                </button>
                <button
                  type="button"
                  class="rounded-2xl px-4 py-3 text-sm font-medium transition"
                  :class="authMode === 'register'
                    ? 'bg-sky-500 text-white shadow-lg shadow-sky-500/20'
                    : 'text-toned hover:bg-default/80'"
                  @click="switchAuthMode('register')"
                >
                  注册
                </button>
              </div>

              <form class="space-y-6" @submit.prevent="handleSubmit">
                <UFormField v-if="authMode === 'login'" name="account" label="账号">
                  <UInput
                    v-model="formState.account"
                    size="xl"
                    placeholder="请输入邮箱或用户名"
                    class="w-full"
                    :ui="{
                      base: 'w-full rounded-2xl'
                    }"
                  />
                </UFormField>

                <UFormField v-else name="email" label="邮箱">
                  <UInput
                    v-model="formState.email"
                    type="email"
                    size="xl"
                    placeholder="请输入注册邮箱"
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

                <UFormField v-if="authMode === 'register'" name="confirmPassword" label="确认密码">
                  <UInput
                    v-model="formState.confirmPassword"
                    type="password"
                    size="xl"
                    placeholder="请再次输入密码"
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
                  {{ submitButtonText }}
                </button>

                <p class="text-center text-sm text-toned">
                  {{ authMode === 'login' ? '还没有账户？' : '已经有账户了？' }}
                  <button
                    type="button"
                    class="font-semibold text-primary transition hover:opacity-80"
                    @click="switchAuthMode(authMode === 'login' ? 'register' : 'login')"
                  >
                    {{ authMode === 'login' ? '立即注册' : '返回登录' }}
                  </button>
                </p>
              </form>
            </UCard>
          </section>
        </div>
      </div>
    </div>
  </main>
</template>
