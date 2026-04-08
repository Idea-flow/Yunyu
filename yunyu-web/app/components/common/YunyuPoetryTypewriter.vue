<script setup lang="ts">
/**
 * 云屿古诗词打字机组件。
 * 作用：在前台封面区客户端请求公开古诗词接口，展示一行诗句与出处信息，
 * 并通过打字机动效增强文章页、分类页和专题页的封面氛围。
 */
interface PoetryOrigin {
  title: string
  dynasty: string
  author: string
}

/**
 * 古诗词展示数据。
 * 作用：统一约束接口返回和本地兜底诗句在组件中的消费结构。
 */
interface PoetryDisplayData {
  content: string
  origin: PoetryOrigin
}

/**
 * 今日诗词 SDK 返回结构。
 * 作用：为浏览器端 SDK 回调结果提供最小必要类型描述。
 */
interface JinrishiciLoadResult {
  data: {
    content: string
    origin: PoetryOrigin
  }
}

/**
 * 今日诗词浏览器 SDK 类型。
 * 作用：描述组件内实际会调用的 `load` 方法签名，避免在页面层散写 `window` 类型断言。
 */
interface JinrishiciBrowserSdk {
  load: (
    success: (result: JinrishiciLoadResult) => void,
    error?: (error: unknown) => void
  ) => void
}

const props = withDefaults(defineProps<{
  variant?: 'sky' | 'orange' | 'neutral'
  align?: 'center' | 'left'
}>(), {
  variant: 'neutral',
  align: 'center'
})

const displayedContent = ref('')
const poetry = ref<PoetryDisplayData | null>(null)
const loading = ref(true)
const cursorVisible = ref(true)

const fallbackPoetryList: PoetryDisplayData[] = [
  {
    content: '海上生明月，天涯共此时。',
    origin: {
      title: '望月怀远',
      dynasty: '唐',
      author: '张九龄'
    }
  },
  {
    content: '山光悦鸟性，潭影空人心。',
    origin: {
      title: '题破山寺后禅院',
      dynasty: '唐',
      author: '常建'
    }
  },
  {
    content: '树深时见鹿，溪午不闻钟。',
    origin: {
      title: '访戴天山道士不遇',
      dynasty: '唐',
      author: '李白'
    }
  }
]

let typingTimer: ReturnType<typeof window.setTimeout> | null = null
let cursorTimer: ReturnType<typeof window.setInterval> | null = null
let scriptLoadingPromise: Promise<JinrishiciBrowserSdk> | null = null

/**
 * 计算当前主题色样式。
 * 作用：让同一组件在专题页、分类页和文章页中复用时，能保持和页面主色一致的高光质感。
 */
const variantClass = computed(() => {
  if (props.variant === 'sky') {
    return {
      ring: 'bg-white/[0.045] shadow-[0_8px_20px_-18px_rgba(14,116,144,0.18)]',
      accent: 'text-sky-50'
    }
  }

  if (props.variant === 'orange') {
    return {
      ring: 'bg-white/[0.045] shadow-[0_8px_20px_-18px_rgba(194,65,12,0.16)]',
      accent: 'text-orange-50'
    }
  }

  return {
    ring: 'bg-white/[0.05] shadow-[0_8px_20px_-18px_rgba(15,23,42,0.2)]',
    accent: 'text-white'
  }
})

/**
 * 计算文本对齐样式。
 * 作用：让组件在不同封面结构下支持居中或左对齐展示。
 */
const alignClass = computed(() => props.align === 'left' ? 'items-start text-left' : 'items-center text-center')

/**
 * 返回随机兜底诗句。
 * 作用：在外部接口超时或脚本加载失败时，保证封面区仍有稳定文案可展示。
 */
function pickFallbackPoetry(): PoetryDisplayData {
  const index = Math.floor(Math.random() * fallbackPoetryList.length)
  return fallbackPoetryList[index]
}

/**
 * 读取浏览器端今日诗词 SDK。
 * 作用：按需加载官方脚本，并复用全局实例，避免多个页面或组件重复插入脚本标签。
 */
async function loadJinrishiciSdk(): Promise<JinrishiciBrowserSdk> {
  if (!import.meta.client) {
    throw new Error('今日诗词 SDK 仅支持浏览器环境')
  }

  const currentWindow = window as Window & { jinrishici?: JinrishiciBrowserSdk }
  if (currentWindow.jinrishici) {
    return currentWindow.jinrishici
  }

  if (scriptLoadingPromise) {
    return await scriptLoadingPromise
  }

  scriptLoadingPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector<HTMLScriptElement>('script[data-jinrishici-sdk="true"]')
    if (existingScript) {
      const handleLoad = () => {
        if (currentWindow.jinrishici) {
          resolve(currentWindow.jinrishici)
          return
        }

        reject(new Error('今日诗词 SDK 未正确初始化'))
      }

      existingScript.addEventListener('load', handleLoad, { once: true })
      existingScript.addEventListener('error', () => reject(new Error('今日诗词 SDK 脚本加载失败')), { once: true })
      return
    }

    const script = document.createElement('script')
    script.src = 'https://sdk.jinrishici.com/v2/browser/jinrishici.js'
    script.charset = 'utf-8'
    script.async = true
    script.dataset.jinrishiciSdk = 'true'
    script.onload = () => {
      if (currentWindow.jinrishici) {
        resolve(currentWindow.jinrishici)
        return
      }

      reject(new Error('今日诗词 SDK 未挂载到 window'))
    }
    script.onerror = () => reject(new Error('今日诗词 SDK 脚本加载失败'))
    document.head.appendChild(script)
  })

  try {
    return await scriptLoadingPromise
  }
  finally {
    scriptLoadingPromise = null
  }
}

/**
 * 请求一条古诗词数据。
 * 作用：通过官方浏览器 SDK 获取当前设备环境下推荐的一句诗与出处信息。
 */
async function requestPoetry(): Promise<PoetryDisplayData> {
  const sdk = await loadJinrishiciSdk()

  return await new Promise((resolve, reject) => {
    sdk.load((result) => {
      resolve({
        content: result.data.content,
        origin: {
          title: result.data.origin.title,
          dynasty: result.data.origin.dynasty,
          author: result.data.origin.author
        }
      })
    }, reject)
  })
}

/**
 * 清理打字机动画计时器。
 * 作用：在重新请求诗句或组件卸载时及时回收计时器，避免残留动画继续运行。
 */
function clearTypingTimer() {
  if (typingTimer) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
}

/**
 * 启动光标闪烁动画。
 * 作用：在诗句打字过程中和完成后保持轻微呼吸感，强化打字机的动态反馈。
 */
function startCursorAnimation() {
  if (cursorTimer) {
    window.clearInterval(cursorTimer)
  }

  cursorTimer = window.setInterval(() => {
    cursorVisible.value = !cursorVisible.value
  }, 520)
}

/**
 * 以打字机方式渲染诗句。
 * 作用：逐字展示接口返回内容，让封面区在静态大图之上增加轻量动效与停留感。
 *
 * @param content 需要展示的完整诗句
 */
function startTypewriter(content: string) {
  clearTypingTimer()
  displayedContent.value = ''

  const characters = Array.from(content)
  const writeCharacter = (index: number) => {
    displayedContent.value = characters.slice(0, index).join('')

    if (index >= characters.length) {
      return
    }

    const currentCharacter = characters[index]
    const delay = /[，。！？；、]/.test(currentCharacter) ? 180 : 82
    typingTimer = window.setTimeout(() => writeCharacter(index + 1), delay)
  }

  writeCharacter(1)
}

/**
 * 加载并渲染古诗词。
 * 作用：统一处理外部请求、失败兜底和打字机启动逻辑，减少模板层状态判断复杂度。
 */
async function loadPoetry() {
  loading.value = true

  try {
    poetry.value = await requestPoetry()
  }
  catch {
    poetry.value = pickFallbackPoetry()
  }
  finally {
    loading.value = false
  }

  startTypewriter(poetry.value.content)
}

onMounted(async () => {
  startCursorAnimation()
  await loadPoetry()
})

onBeforeUnmount(() => {
  clearTypingTimer()

  if (cursorTimer) {
    window.clearInterval(cursorTimer)
    cursorTimer = null
  }
})
</script>

<template>
  <div
    class="flex w-full flex-col"
    :class="alignClass"
  >
    <div
      class="w-fit max-w-[min(92vw,42rem)] rounded-[999px] px-3.5 py-1.5 backdrop-blur-[10px] sm:px-4.5 sm:py-2 lg:px-6 lg:py-2.5"
      :class="variantClass.ring"
    >
      <p
        class="text-[clamp(0.8rem,0.76rem+0.22vw,0.94rem)] font-normal leading-[1.4] tracking-[0.008em] [text-wrap:balance] sm:text-[clamp(0.88rem,0.82rem+0.26vw,1.02rem)] lg:text-[clamp(1rem,0.92rem+0.3vw,1.14rem)]"
        :class="variantClass.accent"
      >
        <span v-if="loading">正在取一行诗意...</span>
        <template v-else>{{ displayedContent }}</template>
        <span
          class="ml-0.5 inline-block h-[0.9em] w-[1px] align-[-0.06em] bg-current transition-opacity duration-200"
          :class="cursorVisible ? 'opacity-100' : 'opacity-0'"
        />
      </p>
    </div>
  </div>
</template>
