<script setup lang="ts">
/**
 * 前台认证页。
 * 作用：承接前台与后台统一的登录、注册入口，并通过品牌化展示区强化云屿站点识别，
 * 同时在移动端收敛为单栏认证体验，保证输入与提交操作足够直接。
 */
import YunyuAuthCharacterScene from '~/components/common/YunyuAuthCharacterScene.vue'

definePageMeta({
  layout: false
})

const route = useRoute()
const toast = useToast()
const auth = useAuth()
const siteContent = useSiteContent()

type AuthMode = 'login' | 'register'
type PasswordField = 'password' | 'confirmPassword'
type FocusField = 'account' | 'email' | 'password' | 'confirmPassword' | null
type SceneErrorStage = 'idle' | 'pose' | 'shake'

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)\S{8,20}$/

const { data: siteConfigData } = await useAsyncData('auth-site-config', async () => {
  return await siteContent.getSiteConfig()
})

await auth.fetchCurrentUser()

const authMode = ref<AuthMode>('login')

const formState = reactive({
  account: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isSubmitting = ref(false)
const focusedField = ref<FocusField>(null)
const isSceneError = ref(false)
const sceneErrorStage = ref<SceneErrorStage>('idle')
const passwordVisibility = reactive<Record<PasswordField, boolean>>({
  password: false,
  confirmPassword: false
})
const scenePointer = reactive({
  offsetX: -0.96,
  offsetY: -1.04
})

let sceneErrorTimer: ReturnType<typeof setTimeout> | null = null
let sceneErrorShakeTimer: ReturnType<typeof setTimeout> | null = null

const inputUi = {
  base: [
    'h-13 w-full rounded-full border border-slate-200/80 bg-white/76 px-4 py-0 text-[15px] text-slate-900 shadow-[0_14px_28px_-24px_rgba(15,23,42,0.35)] backdrop-blur-sm transition-all duration-300',
    'placeholder:text-slate-400 focus:border-[color:color-mix(in_srgb,var(--site-primary-color)_36%,white)] focus:bg-white/92 focus:shadow-[0_18px_36px_-26px_rgba(56,189,248,0.28)] focus:ring-0',
    'dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-100 dark:placeholder:text-slate-500 dark:focus:border-[color:color-mix(in_srgb,var(--site-primary-color)_42%,transparent)] dark:focus:bg-white/[0.07]'
  ].join(' '),
  trailing: 'pe-3',
  leading: 'ps-3'
}

const siteConfig = computed(() => siteConfigData.value)
const brandName = computed(() => siteConfig.value?.siteName?.trim() || '云屿')
const brandSubtitle = computed(() => siteConfig.value?.siteSubTitle?.trim() || 'Yunyu')
const logoUrl = computed(() => siteConfig.value?.logoUrl?.trim() || '/icon-512-maskable.png')
const hasPasswordContent = computed(() => Boolean(formState.password || formState.confirmPassword))
const isShowingPassword = computed(() => {
  return hasPasswordContent.value && (passwordVisibility.password || passwordVisibility.confirmPassword)
})

/**
 * 将数值限制在指定范围内。
 *
 * @param value 原始值
 * @param min 最小值
 * @param max 最大值
 * @return 限制后的值
 */
function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}

/**
 * 计算提交按钮文案。
 *
 * @return 提交按钮文案
 */
const submitButtonText = computed(() => {
  if (isSubmitting.value) {
    return authMode.value === 'login' ? '登录中...' : '注册中...'
  }

  return authMode.value === 'login' ? '登录并进入' : '注册并进入'
})

/**
 * 切换认证模式。
 * 切换时会清空敏感输入并重置密码显示状态，避免上一个模式的输入内容影响当前操作。
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
  focusedField.value = null
  passwordVisibility.password = false
  passwordVisibility.confirmPassword = false
}

/**
 * 记录当前聚焦字段。
 * 作用：把右侧表单交互同步到左侧角色区，形成账号输入和密码输入时的联动反馈。
 *
 * @param field 当前聚焦字段
 */
function handleFieldFocus(field: Exclude<FocusField, null>) {
  focusedField.value = field
}

/**
 * 清空当前聚焦字段。
 * 作用：输入框失焦后恢复到鼠标跟随态，避免角色长期停留在输入联动态。
 *
 * @param field 当前失焦字段
 */
function handleFieldBlur(field: Exclude<FocusField, null>) {
  if (focusedField.value === field) {
    focusedField.value = null
  }
}

/**
 * 处理左侧场景内的鼠标移动。
 * 作用：将鼠标位置归一化为 -1 到 1 的向量，用于驱动角色眼睛与身体轻微跟随。
 *
 * @param event 鼠标事件
 */
function handleScenePointerMove(event: PointerEvent) {
  const currentTarget = event.currentTarget as HTMLElement | null

  if (!currentTarget) {
    return
  }

  const rect = currentTarget.getBoundingClientRect()
  const ratioX = (event.clientX - rect.left) / rect.width
  const ratioY = (event.clientY - rect.top) / rect.height

  scenePointer.offsetX = clamp((ratioX - 0.5) * 2.35, -1.28, 1.28)
  scenePointer.offsetY = clamp((ratioY - 0.42) * 2.15, -1.2, 1.2)
}

/**
 * 重置左侧场景的鼠标观察点。
 * 作用：在鼠标离开场景后让角色缓慢回到默认姿态。
 */
function resetScenePointer() {
  // 参考页在鼠标滑出浏览器后会保持最后一帧状态，这里不主动重置观察点。
}

/**
 * 切换密码字段的可见状态。
 * 作用：让用户在登录或注册时按需查看密码内容，减少移动端输入出错概率。
 *
 * @param field 目标字段
 */
function togglePasswordVisibility(field: PasswordField) {
  passwordVisibility[field] = !passwordVisibility[field]
}

/**
 * 触发左侧角色错误反馈。
 * 作用：在表单校验失败或登录异常时同步显示角色错误态，让左侧互动区与认证结果联动。
 */
function triggerSceneError() {
  isSceneError.value = true
  sceneErrorStage.value = 'pose'

  if (sceneErrorTimer) {
    clearTimeout(sceneErrorTimer)
  }

  if (sceneErrorShakeTimer) {
    clearTimeout(sceneErrorShakeTimer)
  }

  sceneErrorShakeTimer = setTimeout(() => {
    sceneErrorStage.value = 'shake'
    sceneErrorShakeTimer = null
  }, 520)

  sceneErrorTimer = setTimeout(() => {
    isSceneError.value = false
    sceneErrorStage.value = 'idle'
    sceneErrorTimer = null
  }, 2680)
}

onBeforeUnmount(() => {
  if (sceneErrorTimer) {
    clearTimeout(sceneErrorTimer)
  }

  if (sceneErrorShakeTimer) {
    clearTimeout(sceneErrorShakeTimer)
  }
})

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
    triggerSceneError()
    toast.add({
      title: '请输入账号和密码',
      color: 'warning'
    })
    return
  }

  if (authMode.value === 'register') {
    const validationMessage = validateRegisterForm()

    if (validationMessage) {
      triggerSceneError()
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
    triggerSceneError()
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
  <main
    class="login-page relative isolate h-[100svh] overflow-hidden bg-[radial-gradient(circle_at_20%_18%,rgba(255,255,255,0.82),transparent_24%),radial-gradient(circle_at_78%_24%,rgba(255,244,228,0.62),transparent_28%),linear-gradient(135deg,#eef6ff_0%,#edf3ff_44%,#f7f9fc_100%)] text-slate-900 dark:bg-[radial-gradient(circle_at_18%_18%,rgba(56,189,248,0.1),transparent_22%),radial-gradient(circle_at_76%_28%,rgba(251,146,60,0.08),transparent_24%),linear-gradient(135deg,#081120_0%,#0d1729_46%,#101827_100%)] dark:text-slate-50"
    @pointermove="handleScenePointerMove"
    @pointerleave="resetScenePointer"
  >
    <div class="pointer-events-none absolute inset-0 overflow-hidden">
      <div class="absolute -left-24 top-[-4rem] h-80 w-80 rounded-full bg-[color:color-mix(in_srgb,var(--site-primary-color)_12%,white)] blur-3xl dark:bg-[color:color-mix(in_srgb,var(--site-primary-color)_14%,transparent)]" />
      <div class="absolute right-[-5rem] top-[14%] h-96 w-96 rounded-full bg-[color:color-mix(in_srgb,var(--site-secondary-color)_10%,white)] blur-3xl dark:bg-[color:color-mix(in_srgb,var(--site-secondary-color)_12%,transparent)]" />
      <div class="absolute bottom-[-6rem] left-[28%] h-80 w-80 rounded-full bg-white/40 blur-3xl dark:bg-white/[0.03]" />
    </div>

    <div class="relative mx-auto flex h-[100svh] w-full max-w-[1480px] items-center px-4 py-4 sm:px-6 sm:py-5 lg:px-8">
      <div class="grid h-full w-full lg:grid-cols-[1.08fr_0.92fr]">
        <YunyuAuthCharacterScene
          :brand-name="brandName"
          :brand-subtitle="brandSubtitle"
          :logo-url="logoUrl"
          :focused-field="focusedField"
          :is-showing-password="isShowingPassword"
          :is-scene-error="isSceneError"
          :scene-error-stage="sceneErrorStage"
          :pointer-x="scenePointer.offsetX"
          :pointer-y="scenePointer.offsetY"
        />

        <section class="relative flex min-h-0 items-center px-1 py-3 sm:px-3 sm:py-4 lg:px-6 lg:py-5">
          <div class="relative mx-auto w-full max-w-[430px]">
            <div class="px-3 py-2 sm:px-4">
              <div class="flex items-center justify-end gap-4">
                <ThemeModeSwitch variant="icon" />
              </div>

              <div class="mt-4 grid grid-cols-2 gap-2 rounded-[22px] bg-transparent p-1">
                <button
                  type="button"
                  class="rounded-[18px] px-4 py-3 text-sm font-medium transition"
                  :class="authMode === 'login'
                    ? 'bg-[linear-gradient(135deg,var(--site-primary-color),color-mix(in_srgb,var(--site-primary-color)_68%,white))] text-white shadow-[0_18px_34px_-24px_rgba(56,189,248,0.72)]'
                    : 'text-slate-500 hover:bg-white/75 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/[0.06] dark:hover:text-white'"
                  @click="switchAuthMode('login')"
                >
                  登录
                </button>
                <button
                  type="button"
                  class="rounded-[18px] px-4 py-3 text-sm font-medium transition"
                  :class="authMode === 'register'
                    ? 'bg-[linear-gradient(135deg,var(--site-primary-color),color-mix(in_srgb,var(--site-primary-color)_68%,white))] text-white shadow-[0_18px_34px_-24px_rgba(56,189,248,0.72)]'
                    : 'text-slate-500 hover:bg-white/75 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-white/[0.06] dark:hover:text-white'"
                  @click="switchAuthMode('register')"
                >
                  注册
                </button>
              </div>

              <form class="mt-6 space-y-5" @submit.prevent="handleSubmit">
                <UFormField
                  v-if="authMode === 'login'"
                  name="account"
                  label="账号"
                  :ui="{ label: 'mb-2 text-sm font-medium text-slate-700 dark:text-slate-300' }"
                >
                  <UInput
                    v-model="formState.account"
                    size="xl"
                    placeholder="请输入邮箱或用户名"
                    class="w-full"
                    :ui="inputUi"
                    @focus="handleFieldFocus('account')"
                    @blur="handleFieldBlur('account')"
                  >
                    <template #leading>
                      <UIcon name="i-lucide-user-round" class="auth-input-leading-icon" />
                    </template>
                  </UInput>
                </UFormField>

                <UFormField
                  v-else
                  name="email"
                  label="邮箱"
                  :ui="{ label: 'mb-2 text-sm font-medium text-slate-700 dark:text-slate-300' }"
                >
                  <UInput
                    v-model="formState.email"
                    type="email"
                    size="xl"
                    placeholder="请输入注册邮箱"
                    class="w-full"
                    :ui="inputUi"
                    @focus="handleFieldFocus('email')"
                    @blur="handleFieldBlur('email')"
                  >
                    <template #leading>
                      <UIcon name="i-lucide-mail" class="auth-input-leading-icon" />
                    </template>
                  </UInput>
                </UFormField>

                <UFormField
                  name="password"
                  label="密码"
                  :ui="{ label: 'mb-2 text-sm font-medium text-slate-700 dark:text-slate-300' }"
                >
                  <UInput
                    v-model="formState.password"
                    :type="passwordVisibility.password ? 'text' : 'password'"
                    size="xl"
                    placeholder="请输入密码"
                    class="w-full"
                    :ui="inputUi"
                    @focus="handleFieldFocus('password')"
                    @blur="handleFieldBlur('password')"
                  >
                    <template #leading>
                      <UIcon name="i-lucide-lock-keyhole" class="auth-input-leading-icon" />
                    </template>
                    <template #trailing>
                      <button
                        type="button"
                        class="auth-input-trailing-icon"
                        :aria-label="passwordVisibility.password ? '隐藏密码' : '显示密码'"
                        @click="togglePasswordVisibility('password')"
                      >
                        <UIcon :name="passwordVisibility.password ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
                      </button>
                    </template>
                  </UInput>
                </UFormField>

                <UFormField
                  v-if="authMode === 'register'"
                  name="confirmPassword"
                  label="确认密码"
                  :ui="{ label: 'mb-2 text-sm font-medium text-slate-700 dark:text-slate-300' }"
                >
                  <UInput
                    v-model="formState.confirmPassword"
                    :type="passwordVisibility.confirmPassword ? 'text' : 'password'"
                    size="xl"
                    placeholder="请再次输入密码"
                    class="w-full"
                    :ui="inputUi"
                    @focus="handleFieldFocus('confirmPassword')"
                    @blur="handleFieldBlur('confirmPassword')"
                  >
                    <template #leading>
                      <UIcon name="i-lucide-shield-check" class="auth-input-leading-icon" />
                    </template>
                    <template #trailing>
                      <button
                        type="button"
                        class="auth-input-trailing-icon"
                        :aria-label="passwordVisibility.confirmPassword ? '隐藏确认密码' : '显示确认密码'"
                        @click="togglePasswordVisibility('confirmPassword')"
                      >
                        <UIcon :name="passwordVisibility.confirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
                      </button>
                    </template>
                  </UInput>
                </UFormField>

                <div class="text-sm">
                  <p class="text-slate-500 dark:text-slate-400">
                    {{ authMode === 'login' ? '支持邮箱或用户名登录' : '密码需为8-20位，并包含字母和数字' }}
                  </p>
                </div>

                <button
                  type="submit"
                  class="inline-flex min-h-14 w-full items-center justify-center gap-2 rounded-[22px] bg-[linear-gradient(135deg,var(--site-primary-color),color-mix(in_srgb,var(--site-secondary-color)_36%,var(--site-primary-color)))] px-6 text-base font-semibold text-white shadow-[0_22px_44px_-24px_rgba(56,189,248,0.58)] transition hover:-translate-y-0.5 hover:brightness-[1.03] focus:outline-none focus:ring-2 focus:ring-[var(--site-primary-color)] focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-70 dark:text-slate-950 dark:focus:ring-offset-slate-950"
                  :disabled="isSubmitting"
                >
                  <UIcon name="i-lucide-arrow-right" class="size-4" />
                  <span>{{ submitButtonText }}</span>
                </button>

                <p class="text-center text-sm leading-6 text-slate-500 dark:text-slate-400">
                  {{ authMode === 'login' ? '还没有账户？' : '已经有账户了？' }}
                  <button
                    type="button"
                    class="font-semibold text-[var(--site-primary-color)] transition hover:opacity-80"
                    @click="switchAuthMode(authMode === 'login' ? 'register' : 'login')"
                  >
                    {{ authMode === 'login' ? '立即注册' : '返回登录' }}
                  </button>
                </p>
              </form>
            </div>
          </div>
        </section>
      </div>
    </div>
  </main>
</template>

<style scoped>
.auth-input-leading-icon {
  width: 0.95rem;
  height: 0.95rem;
  color: rgb(100 116 139);
  opacity: 0.95;
}

.dark .auth-input-leading-icon {
  color: rgb(203 213 225);
}

.auth-input-trailing-icon {
  display: inline-flex;
  height: 2rem;
  width: 2rem;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  color: rgb(100 116 139);
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.auth-input-trailing-icon:hover {
  background: rgba(255, 255, 255, 0.82);
  color: rgb(51 65 85);
  transform: scale(1.03);
}

.dark .auth-input-trailing-icon {
  color: rgb(148 163 184);
}

.dark .auth-input-trailing-icon:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgb(226 232 240);
}

@media (prefers-reduced-motion: reduce) {
}
</style>
