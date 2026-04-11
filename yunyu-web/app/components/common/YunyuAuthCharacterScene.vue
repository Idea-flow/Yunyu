<script setup lang="ts">
/**
 * 登录页左侧动态角色组件。
 * 作用：承接品牌头部、返回首页入口与四个角色的动态联动表演，
 * 根据页面传入的聚焦状态、密码显隐状态、错误状态与全局鼠标观察点计算角色姿态。
 */
type FocusField = 'account' | 'email' | 'password' | 'confirmPassword' | null
type SceneErrorStage = 'idle' | 'pose' | 'shake'

const props = defineProps<{
  brandName: string
  brandSubtitle: string
  logoUrl: string
  focusedField: FocusField
  isShowingPassword: boolean
  isSceneError: boolean
  sceneErrorStage: SceneErrorStage
  pointerX: number
  pointerY: number
}>()

const identityGreetingPhase = ref<'idle' | 'greeting'>('idle')
const purpleBlinking = ref(false)
const blackBlinking = ref(false)
const purplePeeking = ref(false)
const purplePeekPhase = ref<'forward' | 'return'>('forward')

let purpleBlinkTimer: ReturnType<typeof setTimeout> | null = null
let blackBlinkTimer: ReturnType<typeof setTimeout> | null = null
let purplePeekTimer: ReturnType<typeof setTimeout> | null = null
let identityGreetingTimer: ReturnType<typeof setTimeout> | null = null

const isIdentityFocused = computed(() => props.focusedField === 'account' || props.focusedField === 'email')
const isPasswordFocused = computed(() => props.focusedField === 'password' || props.focusedField === 'confirmPassword')
const isIdentityGreeting = computed(() => identityGreetingPhase.value === 'greeting')
const hasPinnedFacePose = computed(() => {
  return props.isSceneError
    || (isPasswordFocused.value && !props.isShowingPassword)
    || props.isShowingPassword
    || isIdentityGreeting.value
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
 * 开启身份输入时的“角色互看”过渡。
 * 作用：对齐参考页中账号聚焦时先互看、再恢复正常跟随的阶段节奏。
 */
function startIdentityGreeting() {
  identityGreetingPhase.value = 'greeting'

  if (identityGreetingTimer) {
    clearTimeout(identityGreetingTimer)
  }

  identityGreetingTimer = setTimeout(() => {
    identityGreetingPhase.value = 'idle'
    identityGreetingTimer = null
  }, 800)
}

/**
 * 停止身份输入互看阶段。
 * 作用：当账号或邮箱失焦时立即退出互看态，避免角色停留在中间姿态。
 */
function stopIdentityGreeting() {
  identityGreetingPhase.value = 'idle'

  if (identityGreetingTimer) {
    clearTimeout(identityGreetingTimer)
    identityGreetingTimer = null
  }
}

/**
 * 计算场景当前的观察向量。
 * 作用：将页面级鼠标观察点与特殊状态统一映射为角色朝向。
 *
 * @return 当前场景观察向量
 */
const sceneVector = computed(() => {
  if (props.isSceneError) {
    return { x: -0.62, y: 0.8 }
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return { x: -1.02, y: -0.9 }
  }

  if (props.isShowingPassword) {
    return { x: purplePeeking.value ? 1.08 : -1.02, y: purplePeeking.value ? 0.92 : -0.62 }
  }

  return {
    x: props.pointerX,
    y: props.pointerY
  }
})

/**
 * 根据角色锚点解析每个角色自己的观察向量。
 * 作用：让角色在普通鼠标态下按各自站位独立观察，避免同步感过强。
 *
 * @param anchorX 角色横向锚点
 * @param anchorY 角色纵向锚点
 * @return 当前角色观察向量
 */
function resolveCharacterVector(anchorX: number, anchorY: number) {
  if (props.isSceneError || isPasswordFocused.value || props.isShowingPassword) {
    return sceneVector.value
  }

  return {
    x: clamp(props.pointerX - anchorX, -1.36, 1.36),
    y: clamp(props.pointerY - anchorY, -1.24, 1.24)
  }
}

const purpleSceneVector = computed(() => resolveCharacterVector(-0.34, -0.1))
const inkSceneVector = computed(() => resolveCharacterVector(0.05, -0.02))
const orangeSceneVector = computed(() => resolveCharacterVector(-0.22, 0.18))
const yellowSceneVector = computed(() => resolveCharacterVector(0.34, -0.08))

/**
 * 根据朝向向量计算瞳孔位移。
 *
 * @param vector 观察向量
 * @param strength 位移强度
 * @return 瞳孔位移样式
 */
function pupilStyle(vector: { x: number, y: number }, strength: number) {
  return {
    transform: `translate(${(vector.x * strength).toFixed(2)}px, ${(vector.y * strength).toFixed(2)}px)`
  }
}

/**
 * 生成固定平移样式。
 * 作用：用于互看、回避、偷看、报错等固定姿态下的绝对脸部位移。
 *
 * @param x 横向位移
 * @param y 纵向位移
 * @return 固定位移样式
 */
function fixedTranslateStyle(x: number, y: number) {
  return {
    transform: `translate(${x}px, ${y}px)`
  }
}

/**
 * 计算眼睛容器整体位移。
 *
 * @param vector 观察向量
 * @param baseX 基准横向偏移
 * @param baseY 基准纵向偏移
 * @param factorX 横向放大系数
 * @param factorY 纵向放大系数
 * @return 眼睛容器样式
 */
function eyesShiftStyle(vector: { x: number, y: number }, baseX: number, baseY: number, factorX: number, factorY: number) {
  const x = clamp(baseX + vector.x * factorX, -32, 32)
  const y = clamp(baseY + vector.y * factorY, -24, 24)

  return {
    transform: `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`
  }
}

/**
 * 计算单只眼球的细微位移。
 *
 * @param vector 观察向量
 * @param strength 位移强度
 * @param side 左右眼标记
 * @return 眼球位移样式
 */
function eyeballStyle(vector: { x: number, y: number }, strength: number, side: 'left' | 'right') {
  if (strength <= 0) {
    return {
      transform: 'translate(0px, 0px)'
    }
  }

  const sideOffset = side === 'left' ? -0.28 : 0.28
  const x = clamp(vector.x * strength + sideOffset + vector.y * 0.08, -2.6, 2.6)
  const y = clamp(vector.y * (strength * 0.52) + (side === 'left' ? -0.06 : 0.06), -1.8, 1.8)

  return {
    transform: `translate(${x.toFixed(2)}px, ${y.toFixed(2)}px)`
  }
}

/**
 * 计算紫色角色主体姿态。
 *
 * @return 角色样式
 */
const purpleCharacterStyle = computed(() => {
  if (isPasswordFocused.value && !props.isShowingPassword) {
    return {
      transform: 'skewX(-14deg) translateX(-20px) translateY(0px)',
      height: '25.625rem'
    }
  }

  if (props.isShowingPassword) {
    return {
      transform: 'skewX(0deg)',
      height: '23.125rem'
    }
  }

  if (isIdentityFocused.value) {
    return {
      transform: `skewX(${(-12 + purpleSceneVector.value.x * -6.2).toFixed(2)}deg) translateX(40px)`,
      height: '25.625rem'
    }
  }

  return {
    transform: `skewX(${(purpleSceneVector.value.x * -6.2).toFixed(2)}deg)`,
    height: '23.125rem'
  }
})

/**
 * 计算深色角色主体姿态。
 *
 * @return 角色样式
 */
const inkCharacterStyle = computed(() => {
  const typingSkew = inkSceneVector.value.x * -7.8

  if (props.isShowingPassword) {
    return {
      transform: 'skewX(0deg)'
    }
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return {
      transform: 'skewX(12deg) translateX(-10px) translateY(0px)'
    }
  }

  if (isIdentityGreeting.value) {
    return {
      transform: `skewX(${(typingSkew + 10).toFixed(2)}deg) translateX(20px)`
    }
  }

  if (isIdentityFocused.value) {
    return {
      transform: `skewX(${typingSkew.toFixed(2)}deg)`
    }
  }

  return {
    transform: `skewX(${(inkSceneVector.value.x * -5.2).toFixed(2)}deg)`
  }
})

/**
 * 计算橙色角色主体姿态。
 *
 * @return 角色样式
 */
const orangeCharacterStyle = computed(() => {
  return {
    transform: props.isShowingPassword
      ? 'skewX(0deg)'
      : `skewX(${(orangeSceneVector.value.x * -4.8).toFixed(2)}deg)`
  }
})

/**
 * 计算黄色角色主体姿态。
 *
 * @return 角色样式
 */
const yellowCharacterStyle = computed(() => {
  return {
    transform: props.isShowingPassword
      ? 'skewX(0deg)'
      : `skewX(${(yellowSceneVector.value.x * -4.8).toFixed(2)}deg)`
  }
})

/**
 * 计算紫色角色眼睛容器样式。
 *
 * @return 眼睛容器样式
 */
const purpleEyesStyle = computed(() => {
  if (props.isSceneError) {
    return eyesShiftStyle(purpleSceneVector.value, -15, 15, 0, 0)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return eyesShiftStyle(purpleSceneVector.value, -25, -15, 0, 0)
  }

  if (props.isShowingPassword) {
    return eyesShiftStyle(purpleSceneVector.value, -25, -5, 0, 0)
  }

  if (isIdentityGreeting.value) {
    return eyesShiftStyle(purpleSceneVector.value, 10, 25, 0, 0)
  }

  return eyesShiftStyle(purpleSceneVector.value, 0, 0, 11.1, 7.6)
})

/**
 * 计算紫色角色瞳孔样式。
 *
 * @return 瞳孔样式
 */
const purplePupilStyle = computed(() => {
  if (props.isSceneError) {
    return fixedTranslateStyle(-3, 4)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return fixedTranslateStyle(-5, -5)
  }

  if (props.isShowingPassword) {
    return purplePeeking.value && purplePeekPhase.value === 'forward'
      ? fixedTranslateStyle(4, 5)
      : fixedTranslateStyle(-4, -4)
  }

  if (isIdentityGreeting.value) {
    return fixedTranslateStyle(3, 4)
  }

  return pupilStyle(purpleSceneVector.value, 5)
})

/**
 * 计算深色角色眼睛容器样式。
 *
 * @return 眼睛容器样式
 */
const inkEyesStyle = computed(() => {
  if (props.isSceneError) {
    return eyesShiftStyle(inkSceneVector.value, -11, 8, 0, 0)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return eyesShiftStyle(inkSceneVector.value, -16, -12, 0, 0)
  }

  if (props.isShowingPassword) {
    return eyesShiftStyle(inkSceneVector.value, -16, -4, 0, 0)
  }

  if (isIdentityGreeting.value) {
    return eyesShiftStyle(inkSceneVector.value, 6, -20, 0, 0)
  }

  return eyesShiftStyle(inkSceneVector.value, 0, 0, 11.1, 7.6)
})

/**
 * 计算深色角色瞳孔样式。
 *
 * @return 瞳孔样式
 */
const inkPupilStyle = computed(() => {
  if (props.isSceneError) {
    return fixedTranslateStyle(-3, 4)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return fixedTranslateStyle(-4, -5)
  }

  if (props.isShowingPassword) {
    return fixedTranslateStyle(-4, -4)
  }

  if (isIdentityGreeting.value) {
    return fixedTranslateStyle(0, -4)
  }

  return pupilStyle(inkSceneVector.value, 4)
})

/**
 * 计算橙色角色眼睛容器样式。
 *
 * @return 眼睛容器样式
 */
const orangeEyesStyle = computed(() => {
  if (props.isSceneError) {
    return eyesShiftStyle(orangeSceneVector.value, -22, 5, 0, 0)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return eyesShiftStyle(orangeSceneVector.value, -32, -15, 0, 0)
  }

  if (props.isShowingPassword) {
    return eyesShiftStyle(orangeSceneVector.value, -32, -5, 0, 0)
  }

  return eyesShiftStyle(orangeSceneVector.value, 0, 0, 11.1, 7.6)
})

/**
 * 计算橙色角色瞳孔样式。
 *
 * @return 瞳孔样式
 */
const orangePupilStyle = computed(() => {
  if (props.isSceneError) {
    return fixedTranslateStyle(-3, 4)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return fixedTranslateStyle(-5, -5)
  }

  if (props.isShowingPassword) {
    return fixedTranslateStyle(-5, -4)
  }

  return pupilStyle(orangeSceneVector.value, 5)
})

/**
 * 计算黄色角色眼睛容器样式。
 *
 * @return 眼睛容器样式
 */
const yellowEyesStyle = computed(() => {
  if (props.isSceneError) {
    return eyesShiftStyle(yellowSceneVector.value, -17, 5, 0, 0)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return eyesShiftStyle(yellowSceneVector.value, -32, -10, 0, 0)
  }

  if (props.isShowingPassword) {
    return eyesShiftStyle(yellowSceneVector.value, -32, -5, 0, 0)
  }

  return eyesShiftStyle(yellowSceneVector.value, 0, 0, 11.1, 7.6)
})

/**
 * 计算黄色角色瞳孔样式。
 *
 * @return 瞳孔样式
 */
const yellowPupilStyle = computed(() => {
  if (props.isSceneError) {
    return fixedTranslateStyle(-3, 4)
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return fixedTranslateStyle(-5, -5)
  }

  if (props.isShowingPassword) {
    return fixedTranslateStyle(-5, -4)
  }

  return pupilStyle(yellowSceneVector.value, 5)
})

/**
 * 计算橙色角色嘴部类名。
 *
 * @return 嘴部类名
 */
const orangeMouthClassName = computed(() => {
  return props.isSceneError
    ? `auth-scene-mouth auth-scene-mouth--sad visible ${props.sceneErrorStage === 'shake' ? 'shake-head' : ''}`.trim()
    : 'auth-scene-mouth auth-scene-mouth--sad'
})

/**
 * 计算黄色角色嘴部样式。
 *
 * @return 嘴部样式
 */
const yellowMouthStyle = computed(() => {
  if (props.isSceneError) {
    return {
      transform: 'translate(-10px, 4px) rotate(-8deg)'
    }
  }

  if (isPasswordFocused.value && !props.isShowingPassword) {
    return {
      transform: 'translate(-25px, -10px) rotate(0deg)'
    }
  }

  if (props.isShowingPassword) {
    return {
      transform: 'translate(-30px, 0px) rotate(0deg)'
    }
  }

  if (yellowSceneVector.value.x > 0.68 && yellowSceneVector.value.y < -0.32) {
    return {
      transform: `translate(${(yellowSceneVector.value.x * 11.1 + 1.4).toFixed(2)}px, ${(yellowSceneVector.value.y * 7.6 - 1.2).toFixed(2)}px) rotate(-2deg)`
    }
  }

  return {
    transform: `translate(${(yellowSceneVector.value.x * 11.1).toFixed(2)}px, ${(yellowSceneVector.value.y * 7.6).toFixed(2)}px)`
  }
})

/**
 * 安排紫色角色随机眨眼。
 * 作用：增强角色生命感，避免长时间静止发呆。
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
 * 安排深色角色随机眨眼。
 * 作用：与紫色角色错开时间轴，让表演更自然。
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
 * 安排密码显示状态下的偷看动画。
 * 作用：复用参考页最具记忆点的“紫色角色偷看密码”表现。
 */
function schedulePurplePeek() {
  if (!props.isShowingPassword) {
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
        }, 320)
      }, 160)
    }, 760)
  }, Math.random() * 1400 + 1500)
}

watch(() => props.isShowingPassword, (value) => {
  if (purplePeekTimer) {
    clearTimeout(purplePeekTimer)
    purplePeekTimer = null
  }

  purplePeeking.value = false

  if (value) {
    schedulePurplePeek()
  }
})

watch(() => props.focusedField, (field, previousField) => {
  const isCurrentIdentityField = field === 'account' || field === 'email'
  const wasIdentityField = previousField === 'account' || previousField === 'email'

  if (isCurrentIdentityField && !wasIdentityField) {
    startIdentityGreeting()
  }

  if (!isCurrentIdentityField) {
    stopIdentityGreeting()
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

  if (identityGreetingTimer) {
    clearTimeout(identityGreetingTimer)
  }
})
</script>

<template>
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
      <div class="relative hidden h-full max-h-[760px] min-h-[20rem] w-full overflow-hidden lg:block">
        <div class="auth-scene relative h-full">
          <div class="auth-scene__stage">
            <div class="auth-character auth-character--purple" :style="purpleCharacterStyle">
              <div class="auth-eyes auth-eyes--white" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="purpleEyesStyle">
                <div class="auth-eyeball" :class="{ 'auth-eyeball--blink': purpleBlinking }" :style="eyeballStyle(purpleSceneVector, hasPinnedFacePose ? 0 : 0.34, 'left')">
                  <div class="auth-pupil" :style="purplePupilStyle" />
                </div>
                <div class="auth-eyeball" :class="{ 'auth-eyeball--blink': purpleBlinking }" :style="eyeballStyle(purpleSceneVector, hasPinnedFacePose ? 0 : 0.34, 'right')">
                  <div class="auth-pupil" :style="purplePupilStyle" />
                </div>
              </div>
            </div>

            <div class="auth-character auth-character--ink" :style="inkCharacterStyle">
              <div class="auth-eyes auth-eyes--white" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="inkEyesStyle">
                <div class="auth-eyeball auth-eyeball--small" :class="{ 'auth-eyeball--blink': blackBlinking }" :style="eyeballStyle(inkSceneVector, hasPinnedFacePose ? 0 : 0.28, 'left')">
                  <div class="auth-pupil auth-pupil--small" :style="inkPupilStyle" />
                </div>
                <div class="auth-eyeball auth-eyeball--small" :class="{ 'auth-eyeball--blink': blackBlinking }" :style="eyeballStyle(inkSceneVector, hasPinnedFacePose ? 0 : 0.28, 'right')">
                  <div class="auth-pupil auth-pupil--small" :style="inkPupilStyle" />
                </div>
              </div>
            </div>

            <div class="auth-character auth-character--orange" :style="orangeCharacterStyle">
              <div class="auth-eyes auth-eyes--bare" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="orangeEyesStyle">
                <div class="auth-pupil auth-pupil--bare" :style="orangePupilStyle" />
                <div class="auth-pupil auth-pupil--bare" :style="orangePupilStyle" />
              </div>
              <div :class="orangeMouthClassName" />
            </div>

            <div class="auth-character auth-character--yellow" :style="yellowCharacterStyle">
              <div class="auth-eyes auth-eyes--bare" :class="{ 'shake-head': sceneErrorStage === 'shake' }" :style="yellowEyesStyle">
                <div class="auth-pupil auth-pupil--bare" :style="yellowPupilStyle" />
                <div class="auth-pupil auth-pupil--bare" :style="yellowPupilStyle" />
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
</template>

<style scoped>
.auth-scene {
  min-height: 20rem;
}

.auth-scene__stage {
  position: absolute;
  left: 50%;
  top: 50.5%;
  height: 22.5rem;
  width: 30rem;
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
  transition: transform 0.7s ease-in-out, height 0.7s ease-in-out;
  will-change: transform, height;
}

.auth-character--purple {
  left: 3.75rem;
  width: 10.625rem;
  height: 23.125rem;
  border-radius: 1.25rem 1.25rem 0 0;
  background: linear-gradient(180deg, color-mix(in srgb, var(--site-primary-color) 76%, white) 0%, color-mix(in srgb, var(--site-primary-color) 88%, #2563eb) 100%);
  box-shadow: 0 28px 60px -42px rgba(56, 189, 248, 0.55);
  z-index: 1;
}

.auth-character--ink {
  left: 13.75rem;
  width: 7.1875rem;
  height: 18.125rem;
  border-radius: 1rem 1rem 0 0;
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.96), rgba(15, 23, 42, 0.98));
  box-shadow: 0 22px 50px -42px rgba(15, 23, 42, 0.45);
  z-index: 2;
}

.auth-character--orange {
  left: 0;
  width: 14.375rem;
  height: 11.875rem;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, color-mix(in srgb, var(--site-secondary-color) 74%, white) 0%, color-mix(in srgb, var(--site-secondary-color) 86%, #fb923c) 100%);
  z-index: 3;
}

.auth-character--yellow {
  left: 18.125rem;
  width: 8.4375rem;
  height: 13.4375rem;
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
  transition: transform 0.7s ease-in-out;
  will-change: transform;
}

.auth-eyes--white {
  left: 2.8125rem;
  top: 2.5rem;
}

.auth-character--ink .auth-eyes--white {
  left: 1.625rem;
  top: 2rem;
  gap: 0.8rem;
}

.auth-eyes--bare {
  left: 5.125rem;
  top: 5.625rem;
}

.auth-character--yellow .auth-eyes--bare {
  left: 3.25rem;
  top: 2.5rem;
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
  transition: transform 0.16s ease-out, height 0.15s ease;
  will-change: transform, height;
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
  transition: transform 0.1s ease-out;
  will-change: transform;
}

.auth-pupil--small {
  height: 0.38rem;
  width: 0.38rem;
}

.auth-pupil--bare {
  height: 0.75rem;
  width: 0.75rem;
  transition: transform 0.7s ease-in-out;
}

.auth-scene-mouth {
  position: absolute;
  transform-origin: center center;
  transition: transform 0.7s ease-in-out, opacity 0.7s ease-in-out, left 0.7s ease-in-out, top 0.7s ease-in-out;
  will-change: transform, left, top, opacity;
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
  left: 2.5rem;
  top: 5.5rem;
  height: 0.25rem;
  width: 3.125rem;
  border-radius: 999px;
  background: #1e293b;
}

.shake-head {
  animation: shakeHead 0.9s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
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
