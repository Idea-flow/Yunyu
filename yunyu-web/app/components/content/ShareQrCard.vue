<script setup lang="ts">
import { computed } from 'vue'

/**
 * 分享二维码卡片组件。
 * 作用：统一承载二维码展示、复制链接、下载图片与关闭操作，
 * 让文章、专题或活动页面都可以复用同一套轻量分享卡片视觉与交互。
 */
const props = withDefaults(defineProps<{
  title?: string
  brandLabel?: string
  posterTitle?: string
  qrCodeUrl?: string
  qrCodeAlt?: string
  shareUrl?: string
  downloadFileName?: string
  copyButtonText?: string
  downloadButtonText?: string
  loadingText?: string
}>(), {
  title: '扫码分享',
  brandLabel: 'WeChat',
  posterTitle: '',
  qrCodeUrl: '',
  qrCodeAlt: '分享二维码',
  shareUrl: '',
  downloadFileName: '',
  copyButtonText: '复制链接',
  downloadButtonText: '下载图片',
  loadingText: '二维码生成中...'
})

const emit = defineEmits<{
  close: []
}>()

const yunyuToast = useYunyuToast()
const colorMode = useColorMode()

/**
 * 计算下载文件名。
 * 作用：根据传入标题生成更稳定、可读的二维码图片文件名，
 * 避免不同页面重复下载后出现难以识别的默认文件名。
 */
const downloadFileName = computed(() => {
  const normalizedTitle = (props.downloadFileName || props.posterTitle || props.title || 'share')
    .replace(/[\\/:*?"<>|]/g, '')
    .replace(/\s+/g, '-')
    .trim()

  return `${normalizedTitle || 'share'}.png`
})

/**
 * 判断当前二维码是否已经可用。
 * 作用：统一控制二维码区、复制按钮和下载按钮的可交互状态。
 */
const hasQrCode = computed(() => Boolean(props.qrCodeUrl))

/**
 * 计算下载分享图使用的主标题。
 * 作用：优先使用文章标题生成更有识别度的分享图内容，避免下载出来只有“扫码分享”这类泛标题。
 */
const posterDisplayTitle = computed(() => props.posterTitle || props.title || '分享文章')

/**
 * 复制分享链接。
 * 作用：在二维码之外提供直接转发链接的快捷操作，并统一处理成功失败提示。
 */
async function handleCopyShareUrl() {
  if (!props.shareUrl) {
    yunyuToast.error('复制失败', '当前分享链接暂时不可用。')
    return
  }

  if (!navigator.clipboard?.writeText) {
    yunyuToast.error('复制失败', '当前环境暂时无法访问剪贴板。')
    return
  }

  try {
    await navigator.clipboard.writeText(props.shareUrl)
    yunyuToast.success('文章链接已复制')
  } catch {
    yunyuToast.error('复制失败', '暂时无法复制文章链接。')
  }
}

/**
 * 加载指定图片资源。
 * 作用：在绘制下载海报前先把二维码图片加载为可供 Canvas 使用的图像对象。
 *
 * @param src 图片地址
 * @returns 已完成加载的图片对象
 */
function loadImage(src: string) {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()
    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error('图片加载失败'))
    image.src = src
  })
}

/**
 * 按指定宽度切分标题文本。
 * 作用：让较长的文章标题在下载海报中自动换行，避免标题超出画布可读区域。
 *
 * @param context Canvas 绘图上下文
 * @param text 原始文本
 * @param maxWidth 单行最大宽度
 * @returns 切分后的文本行列表
 */
function wrapCanvasText(context: CanvasRenderingContext2D, text: string, maxWidth: number) {
  const lines: string[] = []
  let currentLine = ''

  for (const character of text) {
    const nextLine = `${currentLine}${character}`

    if (!currentLine || context.measureText(nextLine).width <= maxWidth) {
      currentLine = nextLine
      continue
    }

    lines.push(currentLine)
    currentLine = character
  }

  if (currentLine) {
    lines.push(currentLine)
  }

  return lines.slice(0, 3)
}

/**
 * 在画布中绘制圆角矩形路径。
 * 作用：统一海报里卡片、徽标和二维码底座的圆角造型，避免重复拼装路径逻辑。
 *
 * @param context Canvas 绘图上下文
 * @param x 起始横坐标
 * @param y 起始纵坐标
 * @param width 宽度
 * @param height 高度
 * @param radius 圆角半径
 */
function drawRoundedRectPath(
  context: CanvasRenderingContext2D,
  x: number,
  y: number,
  width: number,
  height: number,
  radius: number
) {
  context.beginPath()
  context.roundRect(x, y, width, height, radius)
}

/**
 * 下载生成后的分享海报。
 * 作用：优先输出一张经过美化的分享图，而不是直接下载裸二维码，
 * 让用户更方便把图片转发到微信、群聊或朋友圈。
 */
async function handleDownloadQrCode() {
  if (!props.qrCodeUrl) {
    yunyuToast.error('下载失败', '二维码尚未生成完成。')
    return
  }

  try {
    const qrImage = await loadImage(props.qrCodeUrl)
    const canvas = document.createElement('canvas')
    const context = canvas.getContext('2d')

    if (!context) {
      throw new Error('当前环境暂不支持图片生成')
    }

    const isDarkMode = colorMode.value === 'dark'
    const canvasWidth = 1120
    const canvasHeight = 1480
    const cardX = 92
    const cardY = 86
    const cardWidth = canvasWidth - cardX * 2
    const cardHeight = canvasHeight - cardY * 2
    const qrPanelSize = 596
    const qrSize = 472
    const qrX = (canvasWidth - qrSize) / 2
    const qrPanelX = (canvasWidth - qrPanelSize) / 2
    const qrPanelY = 570
    const qrY = qrPanelY + (qrPanelSize - qrSize) / 2

    canvas.width = canvasWidth
    canvas.height = canvasHeight

    const backgroundGradient = context.createLinearGradient(0, 0, 0, canvasHeight)
    backgroundGradient.addColorStop(0, isDarkMode ? '#08111f' : '#f7fbff')
    backgroundGradient.addColorStop(1, isDarkMode ? '#0d1828' : '#eef5fb')
    context.fillStyle = backgroundGradient
    context.fillRect(0, 0, canvasWidth, canvasHeight)

    const glowGradient = context.createRadialGradient(200, 180, 40, 200, 180, 320)
    glowGradient.addColorStop(0, isDarkMode ? 'rgba(52, 211, 153, 0.16)' : 'rgba(52, 211, 153, 0.18)')
    glowGradient.addColorStop(1, 'rgba(52, 211, 153, 0)')
    context.fillStyle = glowGradient
    context.fillRect(0, 0, canvasWidth, canvasHeight)

    const secondaryGlow = context.createRadialGradient(920, 120, 30, 920, 120, 280)
    secondaryGlow.addColorStop(0, isDarkMode ? 'rgba(56, 189, 248, 0.14)' : 'rgba(56, 189, 248, 0.14)')
    secondaryGlow.addColorStop(1, 'rgba(56, 189, 248, 0)')
    context.fillStyle = secondaryGlow
    context.fillRect(0, 0, canvasWidth, canvasHeight)

    const lowerGlow = context.createRadialGradient(560, 1080, 60, 560, 1080, 360)
    lowerGlow.addColorStop(0, isDarkMode ? 'rgba(59, 130, 246, 0.08)' : 'rgba(59, 130, 246, 0.08)')
    lowerGlow.addColorStop(1, 'rgba(59, 130, 246, 0)')
    context.fillStyle = lowerGlow
    context.fillRect(0, 0, canvasWidth, canvasHeight)

    context.save()
    context.shadowColor = isDarkMode ? 'rgba(0, 0, 0, 0.28)' : 'rgba(15, 23, 42, 0.10)'
    context.shadowBlur = 48
    context.fillStyle = isDarkMode ? 'rgba(10, 18, 34, 0.78)' : 'rgba(255, 255, 255, 0.76)'
    context.strokeStyle = isDarkMode ? 'rgba(255, 255, 255, 0.06)' : 'rgba(226, 232, 240, 0.9)'
    context.lineWidth = 2
    drawRoundedRectPath(context, cardX, cardY, cardWidth, cardHeight, 44)
    context.fill()
    context.shadowBlur = 0
    context.stroke()
    context.restore()

    context.save()
    context.fillStyle = isDarkMode ? 'rgba(16, 185, 129, 0.14)' : 'rgba(16, 185, 129, 0.12)'
    drawRoundedRectPath(context, 156, 132, 196, 64, 32)
    context.fill()
    context.restore()

    context.fillStyle = isDarkMode ? 'rgba(167, 243, 208, 0.92)' : 'rgba(5, 150, 105, 0.92)'
    context.font = '600 26px sans-serif'
    context.fillText(props.brandLabel, 184, 174)

    context.fillStyle = isDarkMode ? 'rgba(148, 163, 184, 0.48)' : 'rgba(148, 163, 184, 0.82)'
    context.fillRect(156, 224, cardWidth - 128, 2)

    context.fillStyle = isDarkMode ? '#f8fafc' : '#0f172a'
    context.font = '600 70px sans-serif'
    const titleLines = wrapCanvasText(context, posterDisplayTitle.value, cardWidth - 128)

    titleLines.forEach((line, index) => {
      context.fillText(line, 156, 328 + index * 84)
    })

    context.fillStyle = isDarkMode ? 'rgba(226, 232, 240, 0.68)' : 'rgba(71, 85, 105, 0.78)'
    context.font = '400 28px sans-serif'
    context.fillText('微信扫一扫，直接在手机里打开这篇文章', 156, 344 + titleLines.length * 84 + 34)

    context.save()
    context.shadowColor = isDarkMode ? 'rgba(0, 0, 0, 0.16)' : 'rgba(15, 23, 42, 0.08)'
    context.shadowBlur = 40
    context.fillStyle = isDarkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(255, 255, 255, 0.72)'
    drawRoundedRectPath(context, qrPanelX, qrPanelY, qrPanelSize, qrPanelSize, 56)
    context.fill()
    context.shadowBlur = 0
    context.strokeStyle = isDarkMode ? 'rgba(255, 255, 255, 0.05)' : 'rgba(226, 232, 240, 0.88)'
    context.lineWidth = 2
    context.stroke()
    context.fillStyle = '#ffffff'
    drawRoundedRectPath(context, qrX, qrY, qrSize, qrSize, 40)
    context.fill()
    context.restore()

    context.drawImage(qrImage, qrX + 20, qrY + 20, qrSize - 40, qrSize - 40)

    context.fillStyle = isDarkMode ? 'rgba(226, 232, 240, 0.54)' : 'rgba(100, 116, 139, 0.78)'
    context.font = '500 26px sans-serif'
    context.fillText('保存图片后，可直接转发到微信', 290, 1244)

    const link = document.createElement('a')
    link.href = canvas.toDataURL('image/png')
    link.download = downloadFileName.value
    link.rel = 'noopener'
    document.body.appendChild(link)
    link.click()
    link.remove()
    yunyuToast.success('分享图片已开始下载')
  } catch (error) {
    console.error('[ShareQrCard] Download poster failed.', error)
    yunyuToast.error('下载失败', '暂时无法生成分享图片。')
  }
}

/**
 * 关闭分享卡片。
 * 作用：对外派发关闭事件，让父层统一控制弹窗开关状态。
 */
function handleClose() {
  emit('close')
}
</script>

<template>
  <div class="relative overflow-hidden p-6 sm:p-7">
    <div class="pointer-events-none absolute inset-x-0 top-0 h-32 bg-[radial-gradient(circle_at_top_left,rgba(34,197,94,0.12),transparent_58%),radial-gradient(circle_at_top_right,rgba(14,165,233,0.1),transparent_56%)] dark:bg-[radial-gradient(circle_at_top_left,rgba(34,197,94,0.12),transparent_56%),radial-gradient(circle_at_top_right,rgba(56,189,248,0.1),transparent_52%)]" />

    <div class="relative flex items-start justify-between gap-4">
      <div class="min-w-0">
        <div class="inline-flex items-center gap-2 text-emerald-600/82 dark:text-emerald-200/72">
          <Icon name="social:wechat" class="size-[0.92rem]" />
          <span class="text-[0.72rem] font-medium uppercase tracking-[0.24em] text-slate-400/78 dark:text-white/32">{{ props.brandLabel }}</span>
        </div>

        <h2 class="mt-3 text-[1.48rem] font-semibold tracking-[-0.04em] [font-family:var(--font-display)] text-slate-950 dark:text-white">{{ props.title }}</h2>
      </div>

      <button
        type="button"
        class="inline-flex h-9 w-9 items-center justify-center rounded-full bg-slate-100/62 text-slate-400 transition duration-200 hover:bg-slate-200/78 hover:text-slate-800 dark:bg-white/5 dark:text-white/54 dark:hover:bg-white/8 dark:hover:text-white"
        aria-label="关闭分享卡片"
        @click="handleClose"
      >
        <UIcon name="i-lucide-x" class="size-[0.92rem]" />
      </button>
    </div>

    <div class="relative mt-8 flex items-center justify-center">
      <div class="pointer-events-none absolute inset-0 rounded-[28px] bg-[radial-gradient(circle,rgba(255,255,255,0.44),transparent_68%)] dark:bg-[radial-gradient(circle,rgba(255,255,255,0.04),transparent_68%)]" />

      <img
        v-if="hasQrCode"
        :src="props.qrCodeUrl"
        :alt="props.qrCodeAlt"
        class="relative h-[15.25rem] w-[15.25rem] rounded-[24px] bg-white/92 object-contain p-3 shadow-[0_22px_40px_-28px_rgba(15,23,42,0.12)] ring-1 ring-slate-200/58 dark:bg-white dark:shadow-[0_18px_34px_-26px_rgba(0,0,0,0.28)] dark:ring-white/8"
      >

      <div
        v-else
        class="relative flex h-[15.25rem] w-[15.25rem] items-center justify-center rounded-[24px] bg-white/56 text-sm text-slate-400 ring-1 ring-slate-200/54 dark:bg-white/6 dark:text-white/38 dark:ring-white/8"
      >
        {{ props.loadingText }}
      </div>
    </div>

    <div class="mt-8 flex items-center justify-between gap-3">
      <button
        type="button"
        class="inline-flex min-h-11 items-center gap-2 rounded-full border border-white/65 bg-[linear-gradient(180deg,rgba(255,255,255,0.9),rgba(255,255,255,0.78))] px-3.5 py-2 text-[0.82rem] font-medium text-slate-600 shadow-[0_16px_34px_-28px_rgba(15,23,42,0.16)] backdrop-blur-xl transition duration-200 hover:border-sky-200/80 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.88),rgba(15,23,42,0.8))] dark:text-slate-300 dark:hover:border-sky-400/20 dark:hover:text-white dark:focus-visible:ring-sky-400/25 dark:focus-visible:ring-offset-slate-950"
        @click="handleCopyShareUrl"
      >
        <UIcon name="i-lucide-copy" class="size-[0.95rem] text-sky-500/82 dark:text-sky-300/78" />
        <span>{{ props.copyButtonText }}</span>
      </button>

      <button
        type="button"
        class="inline-flex min-h-11 items-center gap-2 rounded-full border border-white/65 bg-[linear-gradient(180deg,rgba(255,255,255,0.9),rgba(255,255,255,0.78))] px-3.5 py-2 text-[0.82rem] font-medium text-slate-600 shadow-[0_16px_34px_-28px_rgba(15,23,42,0.16)] backdrop-blur-xl transition duration-200 hover:border-sky-200/80 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sky-300/40 focus-visible:ring-offset-2 focus-visible:ring-offset-white disabled:cursor-not-allowed disabled:opacity-55 dark:border-white/10 dark:bg-[linear-gradient(180deg,rgba(2,6,23,0.88),rgba(15,23,42,0.8))] dark:text-slate-300 dark:hover:border-sky-400/20 dark:hover:text-white dark:focus-visible:ring-sky-400/25 dark:focus-visible:ring-offset-slate-950"
        :disabled="!hasQrCode"
        @click="handleDownloadQrCode"
      >
        <UIcon name="i-lucide-download" class="size-[0.95rem] text-sky-500/82 dark:text-sky-300/78" />
        <span>{{ props.downloadButtonText }}</span>
      </button>
    </div>
  </div>
</template>
