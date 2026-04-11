<script setup lang="ts">
/**
 * 前台认证页。
 * 作用：承接前台与后台统一的登录、注册入口，并通过品牌化展示区强化云屿站点识别，
 * 同时在移动端收敛为单栏认证体验，保证输入与提交操作足够直接。
 */
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
const purpleBlinking = ref(false)
const blackBlinking = ref(false)
const purplePeeking = ref(false)
const purplePeekPhase = ref<'forward' | 'return'>('forward')
const passwordVisibility = reactive<Record<PasswordField, boolean>>({
  password: false,
  confirmPassword: false
})
const scenePointer = reactive({
  offsetX: 0,
  offsetY: 0
})

let purpleBlinkTimer: ReturnType<typeof setTimeout> | null = null
let blackBlinkTimer: ReturnType<typeof setTimeout> | null = null
let purplePeekTimer: ReturnType<typeof setTimeout> | null = null
let sceneErrorTimer: ReturnType<typeof setTimeout> | null = null
let sceneErrorShakeTimer: ReturnType<typeof setTimeout> | null = null

const inputUi = {
  base: [
    'w-full rounded-[22px] border border-white/70 bg-white/82 px-4 py-3 text-[15px] text-slate-800 shadow-[0_18px_38px_-30px_rgba(15,23,42,0.18)] backdrop-blur-sm transition',
    'placeholder:text-slate-400 focus:border-[color:color-mix(in_srgb,var(--site-primary-color)_28%,white)] focus:bg-white focus:ring-4 focus:ring-[color:color-mix(in_srgb,var(--site-primary-color)_12%,transparent)]',
    'dark:border-white/10 dark:bg-slate-950/60 dark:text-slate-100 dark:placeholder:text-slate-500 dark:focus:border-[var(--site-primary-color)] dark:focus:bg-slate-950'
  ].join(' '),
  trailing: 'pe-2',
  leading: 'ps-2'
}

const siteConfig = computed(() => siteConfigData.value)
const brandName = computed(() => siteConfig.value?.siteName?.trim() || '云屿')
const brandSubtitle = computed(() => siteConfig.value?.siteSubTitle?.trim() || 'Yunyu')
const logoUrl = computed(() => siteConfig.value?.logoUrl?.trim() || '/icon-512-maskable.png')
const isIdentityFocused = computed(() => focusedField.value === 'account' || focusedField.value === 'email')
const isPasswordFocused = computed(() => focusedField.value === 'password' || focusedField.value === 'confirmPassword')
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
 * 计算场景当前的观察向量。
 * 作用：统一把鼠标跟随、输入框聚焦、密码显隐和报错状态映射成角色朝向，
 * 避免在模板里分散维护多套条件判断。
 *
 * @return 角色朝向向量
 */
const sceneVector = computed(() => {
  if (isSceneError.value) {
    return { x: -0.62, y: 0.8 }
  }

  if (isPasswordFocused.value && !isShowingPassword.value) {
    return { x: -1.02, y: -0.9 }
  }

  if (isShowingPassword.value) {
    return { x: purplePeeking.value ? 0.84 : -0.88, y: purplePeeking.value ? 0.78 : -0.54 }
  }

  if (isIdentityFocused.value) {
    return { x: 0.82, y: 0.96 }
  }

  return {
    x: scenePointer.offsetX,
    y: scenePointer.offsetY
  }
})

/**
 * 根据角色在舞台中的相对站位，计算各自的观察向量。
 * 作用：参考原始示例里“每个角色按自身位置单独追踪鼠标”的逻辑，
 * 避免所有角色共用同一向量后出现同步过强、表情发呆的问题。
 *
 * @param anchorX 角色横向锚点
 * @param anchorY 角色纵向锚点
 * @return 当前角色专属的观察向量
 */
function resolveCharacterVector(anchorX: number, anchorY: number) {
  if (isSceneError.value || isPasswordFocused.value || isShowingPassword.value || isIdentityFocused.value) {
    return sceneVector.value
  }

  return {
    x: clamp(scenePointer.offsetX - anchorX, -1.36, 1.36),
    y: clamp(scenePointer.offsetY - anchorY, -1.24, 1.24)
  }
}

const purpleSceneVector = computed(() => resolveCharacterVector(-0.34, -0.1))
const inkSceneVector = computed(() => resolveCharacterVector(0.05, -0.02))
const orangeSceneVector = computed(() => resolveCharacterVector(-0.22, 0.18))
const yellowSceneVector = computed(() => resolveCharacterVector(0.34, -0.08))

/**
 * 根据朝向向量计算角色的瞳孔位移。
 *
 * @param vector 当前角色使用的观察向量
 * @param strength 位移强度
 * @return 瞳孔位移样式
 */
function pupilStyle(vector: { x: number, y: number }, strength: number) {
  return {
    transform: `translate(${(vector.x * strength).toFixed(2)}px, ${(vector.y * strength).toFixed(2)}px)`
  }
}

/**
 * 计算角色眼睛区域的整体位移。
 * 作用：在保留瞳孔跟随的同时，让整个眼睛区也产生更明显的方向偏移，
 * 更接近参考页里“整张脸在转向”的感觉。
 *
 * @param vector 当前角色使用的观察向量
 * @param baseX 基准横向偏移
 * @param baseY 基准纵向偏移
 * @param factorX 横向放大系数
 * @param factorY 纵向放大系数
 * @return 眼睛区域变换样式
 */
function eyesShiftStyle(vector: { x: number, y: number }, baseX: number, baseY: number, factorX: number, factorY: number) {
  const x = clamp(baseX + vector.x * factorX, -20, 20)
  const y = clamp(baseY + vector.y * factorY, -15, 15)

  return {
    transform: `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`
  }
}

/**
 * 计算单只眼球的细微位移。
 * 作用：让左右眼在同方向跟随时保留轻微错位，避免两只眼睛永远像镜像复制，
 * 更贴近参考页里“眼白位置也在变化”的灵动感。
 *
 * @param vector 当前角色使用的观察向量
 * @param strength 位移强度
 * @param side 眼睛方向
 * @return 单只眼球位移样式
 */
function eyeballStyle(vector: { x: number, y: number }, strength: number, side: 'left' | 'right') {
  const sideOffset = side === 'left' ? -0.8 : 0.8
  const x = clamp(vector.x * strength + sideOffset + vector.y * 0.3, -7, 7)
  const y = clamp(vector.y * (strength * 0.72) + (side === 'left' ? -0.22 : 0.22), -5, 5)

  return {
    transform: `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`
  }
}

/**
 * 计算左侧紫色角色的主体姿态。
 *
 * @return 角色变换样式
 */
const purpleCharacterStyle = computed(() => {
  if (isPasswordFocused.value && !isShowingPassword.value) {
    return {
      transform: 'skewX(-15deg) translateX(-19px) translateY(-1px)',
      height: '20.15rem'
    }
  }

  if (isShowingPassword.value) {
    if (purplePeeking.value && purplePeekPhase.value === 'forward') {
      return {
        transform: 'skewX(6deg) translateX(18px) translateY(-4px)',
        height: '18.75rem'
      }
    }

    if (purplePeeking.value && purplePeekPhase.value === 'return') {
      return {
        transform: 'skewX(-10deg) translateX(-10px) translateY(-1px)',
        height: '18.1rem'
      }
    }

    return {
      transform: 'skewX(-8deg) translateX(-8px)',
      height: '18.2rem'
    }
  }

  if (isIdentityFocused.value) {
    return {
      transform: `skewX(${(-14 + purpleSceneVector.value.x * 4.5).toFixed(2)}deg) translateX(28px)`,
      height: '20.15rem'
    }
  }

  return {
    transform: `skewX(${(purpleSceneVector.value.x * -8).toFixed(2)}deg) translateX(${(purpleSceneVector.value.x * 10).toFixed(2)}px)`,
    height: '18.2rem'
  }
})

/**
 * 计算左侧深色角色的主体姿态。
 *
 * @return 角色变换样式
 */
const inkCharacterStyle = computed(() => {
  if (isPasswordFocused.value && !isShowingPassword.value) {
    return {
      transform: 'skewX(13deg) translateX(-11px) translateY(-1px)'
    }
  }

  if (isIdentityFocused.value) {
    return {
      transform: `skewX(${(12 + inkSceneVector.value.x * 5.5).toFixed(2)}deg) translateX(20px)`
    }
  }

  return {
    transform: `skewX(${(inkSceneVector.value.x * -7).toFixed(2)}deg) translateX(${(inkSceneVector.value.x * 5).toFixed(2)}px)`
  }
})

/**
 * 计算橙色角色的主体姿态。
 *
 * @return 角色变换样式
 */
const orangeCharacterStyle = computed(() => {
  if (isPasswordFocused.value && !isShowingPassword.value) {
    return {
      transform: 'skewX(5deg) translateX(-9px)'
    }
  }

  return {
    transform: isShowingPassword.value
      ? 'skewX(0deg)'
      : `skewX(${(orangeSceneVector.value.x * -6.2).toFixed(2)}deg) translateX(${(orangeSceneVector.value.x * 7).toFixed(2)}px)`
  }
})

/**
 * 计算黄色角色的主体姿态。
 *
 * @return 角色变换样式
 */
const yellowCharacterStyle = computed(() => {
  if (isPasswordFocused.value && !isShowingPassword.value) {
    return {
      transform: 'skewX(7deg) translateX(-6px) translateY(-1px)'
    }
  }

  return {
    transform: isShowingPassword.value
      ? 'skewX(0deg)'
      : `skewX(${(yellowSceneVector.value.x * -5.8).toFixed(2)}deg) translateX(${(yellowSceneVector.value.x * 6).toFixed(2)}px)`
  }
})

/**
 * 计算错误态下的嘴部样式。
 *
 * @return 嘴部类名
 */
const orangeMouthClassName = computed(() => {
  return isSceneError.value
    ? `auth-scene-mouth auth-scene-mouth--sad visible ${sceneErrorStage.value === 'shake' ? 'shake-head' : ''}`.trim()
    : 'auth-scene-mouth auth-scene-mouth--sad'
})

/**
 * 计算黄色角色嘴部样式。
 *
 * @return 嘴部变换样式
 */
const yellowMouthStyle = computed(() => {
  if (isSceneError.value) {
    return {
      transform: 'translate(-8px, 4px) rotate(-8deg)'
    }
  }

  if (isPasswordFocused.value && !isShowingPassword.value) {
    return {
      transform: 'translate(-12px, -8px) rotate(0deg)'
    }
  }

  return {
    transform: `translate(${(yellowSceneVector.value.x * 5).toFixed(2)}px, ${(yellowSceneVector.value.y * 4).toFixed(2)}px)`
  }
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
  scenePointer.offsetX = 0
  scenePointer.offsetY = 0
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

/**
 * 安排紫色角色的随机眨眼动画。
 * 作用：为左侧角色区补充生命感，避免静止时显得过于机械。
 */
function schedulePurpleBlink() {
  purpleBlinkTimer = setTimeout(() => {
    purpleBlinking.value = true

    setTimeout(() => {
      purpleBlinking.value = false
      schedulePurpleBlink()
    }, 160)
  }, Math.random() * 3200 + 2400)
}

/**
 * 安排深色角色的随机眨眼动画。
 * 作用：与紫色角色错开节奏，形成更自然的双角色状态变化。
 */
function scheduleBlackBlink() {
  blackBlinkTimer = setTimeout(() => {
    blackBlinking.value = true

    setTimeout(() => {
      blackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, Math.random() * 3600 + 2600)
}

/**
 * 安排密码显隐时的偷看动画。
 * 作用：当密码可见且已有内容时，让左侧角色偶尔偷看，复用参考页最有记忆点的交互之一。
 */
function schedulePurplePeek() {
  if (!isShowingPassword.value) {
    purplePeeking.value = false
    purplePeekTimer = null
    return
  }

  purplePeekTimer = setTimeout(() => {
    purplePeeking.value = true
    purplePeekPhase.value = 'forward'

    setTimeout(() => {
      purplePeekTimer = setTimeout(() => {
        purplePeekPhase.value = 'return'

        setTimeout(() => {
          purplePeeking.value = false
          purplePeekPhase.value = 'forward'
          schedulePurplePeek()
        }, 460)
      }, 260)
    }, 560)
  }, Math.random() * 1400 + 1500)
}

watch(isShowingPassword, (value) => {
  if (purplePeekTimer) {
    clearTimeout(purplePeekTimer)
    purplePeekTimer = null
  }

  purplePeeking.value = false

  if (value) {
    schedulePurplePeek()
  }
})

onMounted(() => {
  schedulePurpleBlink()
  scheduleBlackBlink()
})

onBeforeUnmount(() => {
  if (purpleBlinkTimer) {
    clearTimeout(purpleBlinkTimer)
  }

  if (blackBlinkTimer) {
    clearTimeout(blackBlinkTimer)
  }

  if (purplePeekTimer) {
    clearTimeout(purplePeekTimer)
  }

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
        <section class="relative flex min-h-0 flex-col overflow-hidden px-3 py-3 sm:px-4 sm:py-4 lg:px-6 lg:py-5">
          <div class="relative flex items-center justify-between gap-4">
            <NuxtLink to="/" class="flex min-w-0 items-center gap-3">
              <div class="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-2xl border border-white/65 bg-white/72 shadow-[0_18px_40px_-28px_rgba(15,23,42,0.22)] dark:border-white/10 dark:bg-white/[0.05]">
                <img :src="logoUrl" :alt="`${brandName} 图标`" class="h-full w-full object-cover">
              </div>
              <div class="min-w-0">
                <p class="truncate text-[1.15rem] font-semibold tracking-[-0.05em] text-slate-950 [font-family:var(--font-display)] dark:text-white">
                  {{ brandName }}
                </p>
                <p class="truncate text-[0.7rem] uppercase tracking-[0.24em] text-slate-500 dark:text-slate-400">
                  {{ brandSubtitle }}
                </p>
              </div>
            </NuxtLink>

            <NuxtLink
              to="/"
              class="inline-flex shrink-0 items-center gap-2 rounded-full border border-white/70 bg-white/70 px-3 py-2 text-sm text-slate-600 backdrop-blur-sm transition hover:-translate-y-0.5 hover:text-slate-900 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-300 dark:hover:text-white"
            >
              <UIcon name="i-lucide-house" class="size-4" />
              <span>返回首页</span>
            </NuxtLink>
          </div>

          <div class="relative mt-4 flex flex-1 items-center justify-center lg:mt-0">
            <div
              class="relative hidden h-full max-h-[760px] min-h-[20rem] w-full overflow-hidden lg:block"
            >
              <div class="auth-scene relative h-full">
                <div class="auth-scene__stage">
                  <div class="auth-character auth-character--purple" :style="purpleCharacterStyle">
                    <div class="auth-eyes auth-eyes--white" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="eyesShiftStyle(purpleSceneVector, isPasswordFocused && !isShowingPassword ? -14 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 14 : isShowingPassword && purplePeeking && purplePeekPhase === 'return' ? -10 : isIdentityFocused ? 10 : 0, isPasswordFocused && !isShowingPassword ? -12 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 9 : isShowingPassword && purplePeeking && purplePeekPhase === 'return' ? -5 : isIdentityFocused ? 8 : 0, 5.8, 4.1)">
                      <div class="auth-eyeball" :class="{ 'auth-eyeball--blink': purpleBlinking }" :style="eyeballStyle(purpleSceneVector, isPasswordFocused && !isShowingPassword ? 1.1 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 1.4 : isShowingPassword ? 1 : 1.45, 'left')">
                        <div class="auth-pupil" :style="pupilStyle(purpleSceneVector, isPasswordFocused && !isShowingPassword ? 2.4 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 4 : isShowingPassword && purplePeekPhase === 'return' ? 2.8 : isShowingPassword ? 3.2 : 4.7)" />
                      </div>
                      <div class="auth-eyeball" :class="{ 'auth-eyeball--blink': purpleBlinking }" :style="eyeballStyle(purpleSceneVector, isPasswordFocused && !isShowingPassword ? 1.1 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 1.4 : isShowingPassword ? 1 : 1.45, 'right')">
                        <div class="auth-pupil" :style="pupilStyle(purpleSceneVector, isPasswordFocused && !isShowingPassword ? 2.4 : isShowingPassword && purplePeeking && purplePeekPhase === 'forward' ? 4 : isShowingPassword && purplePeekPhase === 'return' ? 2.8 : isShowingPassword ? 3.2 : 4.7)" />
                      </div>
                    </div>
                  </div>

                  <div class="auth-character auth-character--ink" :style="inkCharacterStyle">
                    <div class="auth-eyes auth-eyes--white" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="eyesShiftStyle(inkSceneVector, isPasswordFocused && !isShowingPassword ? -10 : isIdentityFocused ? 6 : 0, isPasswordFocused && !isShowingPassword ? -11 : isIdentityFocused ? -7 : 0, 4.3, 3.5)">
                      <div class="auth-eyeball auth-eyeball--small" :class="{ 'auth-eyeball--blink': blackBlinking }" :style="eyeballStyle(inkSceneVector, isPasswordFocused && !isShowingPassword ? 1 : isShowingPassword ? 0.9 : 1.2, 'left')">
                        <div class="auth-pupil auth-pupil--small" :style="pupilStyle(inkSceneVector, isPasswordFocused && !isShowingPassword ? 2.3 : isShowingPassword ? 2.8 : 3.7)" />
                      </div>
                      <div class="auth-eyeball auth-eyeball--small" :class="{ 'auth-eyeball--blink': blackBlinking }" :style="eyeballStyle(inkSceneVector, isPasswordFocused && !isShowingPassword ? 1 : isShowingPassword ? 0.9 : 1.2, 'right')">
                        <div class="auth-pupil auth-pupil--small" :style="pupilStyle(inkSceneVector, isPasswordFocused && !isShowingPassword ? 2.3 : isShowingPassword ? 2.8 : 3.7)" />
                      </div>
                    </div>
                  </div>

                  <div class="auth-character auth-character--orange" :style="orangeCharacterStyle">
                    <div class="auth-eyes auth-eyes--bare" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="eyesShiftStyle(orangeSceneVector, isPasswordFocused && !isShowingPassword ? -13 : isShowingPassword ? -9 : 0, isPasswordFocused && !isShowingPassword ? -10 : isShowingPassword ? -4 : 0, 4.7, 3.7)">
                      <div class="auth-pupil auth-pupil--bare" :style="pupilStyle(orangeSceneVector, isPasswordFocused && !isShowingPassword ? 2.7 : isShowingPassword ? 3.1 : 4.6)" />
                      <div class="auth-pupil auth-pupil--bare" :style="pupilStyle(orangeSceneVector, isPasswordFocused && !isShowingPassword ? 2.7 : isShowingPassword ? 3.1 : 4.6)" />
                    </div>
                    <div :class="orangeMouthClassName" />
                  </div>

                  <div class="auth-character auth-character--yellow" :style="yellowCharacterStyle">
                    <div class="auth-eyes auth-eyes--bare" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="eyesShiftStyle(yellowSceneVector, isPasswordFocused && !isShowingPassword ? -12 : isShowingPassword ? -8 : 0, isPasswordFocused && !isShowingPassword ? -9 : isShowingPassword ? -3 : 0, 4.4, 3.6)">
                      <div class="auth-pupil auth-pupil--bare" :style="pupilStyle(yellowSceneVector, isPasswordFocused && !isShowingPassword ? 2.4 : isShowingPassword ? 2.8 : 4.6)" />
                      <div class="auth-pupil auth-pupil--bare" :style="pupilStyle(yellowSceneVector, isPasswordFocused && !isShowingPassword ? 2.4 : isShowingPassword ? 2.8 : 4.6)" />
                    </div>
                    <div class="auth-scene-mouth auth-scene-mouth--line" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="yellowMouthStyle" />
                  </div>
                </div>

                <div class="auth-scene__status">
                  <span class="inline-flex rounded-full border border-white/70 bg-white/60 px-3 py-1 text-[0.68rem] font-medium tracking-[0.2em] text-slate-500 backdrop-blur-sm dark:border-white/10 dark:bg-white/[0.04] dark:text-slate-400">
                    LIVE
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="relative flex min-h-0 items-center px-1 py-3 sm:px-3 sm:py-4 lg:px-6 lg:py-5">
          <div class="relative mx-auto w-full max-w-[430px]">
            <div class="px-3 py-2 sm:px-4">
              <div class="flex items-center justify-end gap-4">
                <ThemeModeSwitch variant="icon" />
              </div>

              <div class="mt-4 grid grid-cols-2 gap-2 rounded-[22px] bg-slate-100/86 p-1.5 dark:bg-white/[0.05]">
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
                      <UIcon name="i-lucide-user-round" class="size-4 text-slate-400" />
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
                      <UIcon name="i-lucide-mail" class="size-4 text-slate-400" />
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
                      <UIcon name="i-lucide-lock-keyhole" class="size-4 text-slate-400" />
                    </template>
                    <template #trailing>
                      <button
                        type="button"
                        class="inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-white/[0.06] dark:hover:text-slate-200"
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
                      <UIcon name="i-lucide-shield-check" class="size-4 text-slate-400" />
                    </template>
                    <template #trailing>
                      <button
                        type="button"
                        class="inline-flex h-9 w-9 items-center justify-center rounded-full text-slate-400 transition hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-white/[0.06] dark:hover:text-slate-200"
                        :aria-label="passwordVisibility.confirmPassword ? '隐藏确认密码' : '显示确认密码'"
                        @click="togglePasswordVisibility('confirmPassword')"
                      >
                        <UIcon :name="passwordVisibility.confirmPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
                      </button>
                    </template>
                  </UInput>
                </UFormField>

                <div class="flex items-center justify-between gap-3 text-sm">
                  <p class="text-slate-500 dark:text-slate-400">
                    {{ authMode === 'login' ? '支持邮箱或用户名登录' : '密码需为8-20位，并包含字母和数字' }}
                  </p>
                  <span class="rounded-full border border-white/80 bg-white/72 px-3 py-1 text-xs font-medium text-slate-500 dark:border-white/10 dark:bg-white/[0.05] dark:text-slate-400">
                    {{ authMode === 'login' ? '快捷认证' : '安全校验' }}
                  </span>
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
.auth-scene {
  min-height: 20rem;
}

.auth-scene__stage {
  position: absolute;
  left: 50%;
  top: 50.5%;
  height: 20.6rem;
  width: 24.2rem;
  transform: translate(-50%, -50%);
}

.auth-scene__status {
  position: absolute;
  left: 50%;
  bottom: 1.5rem;
  transform: translateX(-50%);
}

.auth-character {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition: transform 0.62s cubic-bezier(0.22, 1, 0.36, 1), height 0.62s cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-character--purple {
  left: 3.15rem;
  width: 10rem;
  height: 18.2rem;
  border-radius: 1.25rem 1.25rem 0 0;
  background: linear-gradient(180deg, color-mix(in srgb, var(--site-primary-color) 76%, white) 0%, color-mix(in srgb, var(--site-primary-color) 88%, #2563eb) 100%);
  box-shadow: 0 28px 60px -42px rgba(56, 189, 248, 0.55);
  z-index: 1;
}

.auth-character--ink {
  left: 11.1rem;
  width: 6.4rem;
  height: 14.2rem;
  border-radius: 1rem 1rem 0 0;
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.96), rgba(15, 23, 42, 0.98));
  box-shadow: 0 22px 50px -42px rgba(15, 23, 42, 0.45);
  z-index: 2;
}

.auth-character--orange {
  left: 0.78rem;
  width: 12.8rem;
  height: 9.6rem;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, color-mix(in srgb, var(--site-secondary-color) 74%, white) 0%, color-mix(in srgb, var(--site-secondary-color) 86%, #fb923c) 100%);
  z-index: 3;
}

.auth-character--yellow {
  left: 15.25rem;
  width: 7.3rem;
  height: 10.7rem;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, #fde68a 0%, #facc15 100%);
  z-index: 4;
}

.auth-eyes {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 1rem;
  transform-origin: center center;
  transition: transform 0.42s cubic-bezier(0.22, 1, 0.36, 1);
}

.auth-eyes--white {
  left: 2.1rem;
  top: 2.35rem;
}

.auth-character--ink .auth-eyes--white {
  left: 1.5rem;
  top: 1.9rem;
  gap: 0.8rem;
}

.auth-eyes--bare {
  left: 4.6rem;
  top: 4.1rem;
}

.auth-character--yellow .auth-eyes--bare {
  left: 2.6rem;
  top: 2.15rem;
  gap: 0.8rem;
}

.auth-eyeball {
  display: flex;
  height: 1.1rem;
  width: 1.1rem;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 999px;
  background: white;
  transition: transform 0.24s cubic-bezier(0.22, 1, 0.36, 1), height 0.15s ease;
}

.auth-eyeball--small {
  height: 1rem;
  width: 1rem;
}

.auth-eyeball--blink {
  height: 2px;
}

.auth-pupil {
  height: 0.45rem;
  width: 0.45rem;
  border-radius: 999px;
  background: #1e293b;
  transition: transform 0.12s ease-out;
}

.auth-pupil--small {
  height: 0.38rem;
  width: 0.38rem;
}

.auth-pupil--bare {
  height: 0.75rem;
  width: 0.75rem;
}

.auth-scene-mouth {
  position: absolute;
  transform-origin: center center;
  transition: transform 0.42s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.42s ease;
}

.auth-scene-mouth--sad {
  left: 5rem;
  top: 5.8rem;
  height: 0.85rem;
  width: 1.8rem;
  border: 3px solid #1e293b;
  border-top: none;
  border-radius: 0 0 999px 999px;
  opacity: 0;
}

.auth-scene-mouth--sad.visible {
  opacity: 1;
}

.auth-scene-mouth--line {
  left: 2.1rem;
  top: 4.7rem;
  height: 0.25rem;
  width: 2rem;
  border-radius: 999px;
  background: #1e293b;
}

.shake-head {
  animation: shakeHead 0.9s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

@keyframes login-float {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(1);
  }

  50% {
    transform: translate3d(0, -14px, 0) scale(1.04);
  }
}

@keyframes shakeHead {
  0%,
  100% {
    translate: 0 0;
  }

  14% {
    translate: -10px 0;
  }

  28% {
    translate: 9px 0;
  }

  42% {
    translate: -7px 0;
  }

  56% {
    translate: 6px 0;
  }

  70% {
    translate: -4px 0;
  }

  84% {
    translate: 2px 0;
  }

  90% {
    translate: -0.5px 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .shake-head {
    animation: none;
  }
}
</style>
