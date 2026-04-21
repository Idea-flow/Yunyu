<script setup lang="ts">
/**
 * 后台文本悬浮提示组件。
 * 作用：用于在表格等场景中展示被截断文本，并在鼠标悬浮时显示完整内容提示层。
 * 说明：提示层通过 Teleport 挂载到 body，避免被表格容器的 overflow 裁剪。
 */
const props = withDefaults(defineProps<{
  text: string
  tooltipMaxWidthClass?: string
}>(), {
  tooltipMaxWidthClass: 'max-w-[560px]'
})

const triggerRef = ref<HTMLElement | null>(null)
const tooltipRef = ref<HTMLElement | null>(null)
const toast = useToast()
const isVisible = ref(false)
const hideTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const tooltipPosition = ref({
  left: 0,
  top: 0
})
const tooltipPlacement = ref<'top' | 'bottom'>('bottom')

/**
 * 清理延迟关闭定时器。
 * 作用：避免重复定时导致提示层状态抖动。
 */
function clearHideTimer() {
  if (!hideTimer.value) {
    return
  }
  clearTimeout(hideTimer.value)
  hideTimer.value = null
}

/**
 * 计算并更新提示层坐标。
 * 作用：根据可视区域自动决定提示层显示在触发文本上方或下方，并避免超出视口边界。
 */
function updateTooltipPosition() {
  if (!triggerRef.value) {
    return
  }

  const rect = triggerRef.value.getBoundingClientRect()
  const viewportPadding = 12
  const estimatedTooltipWidth = 560
  const estimatedTooltipHeight = 84
  const verticalOffset = 8
  const safeLeft = Math.min(
    rect.left,
    window.innerWidth - estimatedTooltipWidth - viewportPadding
  )
  const shouldUseTop = rect.bottom + verticalOffset + estimatedTooltipHeight > window.innerHeight
    && rect.top - verticalOffset - estimatedTooltipHeight >= viewportPadding

  tooltipPlacement.value = shouldUseTop ? 'top' : 'bottom'

  tooltipPosition.value = {
    left: Math.max(viewportPadding, safeLeft),
    top: shouldUseTop ? rect.top - verticalOffset : rect.bottom + verticalOffset
  }
}

/**
 * 显示提示层。
 */
function showTooltip() {
  if (!props.text) {
    return
  }

  clearHideTimer()
  updateTooltipPosition()
  isVisible.value = true
}

/**
 * 隐藏提示层。
 */
function hideTooltip() {
  clearHideTimer()
  isVisible.value = false
}

/**
 * 延迟隐藏提示层。
 * 作用：鼠标从触发区移动到提示层时，允许经过短暂空隙，避免提示层瞬间消失。
 */
function scheduleHideTooltip() {
  clearHideTimer()
  hideTimer.value = setTimeout(() => {
    isVisible.value = false
    hideTimer.value = null
  }, 180)
}

/**
 * 处理触发元素移出事件。
 * 作用：当鼠标从触发元素移动到提示层时保持显示，避免无法进入提示层复制内容。
 *
 * @param event 鼠标移出事件
 */
function handleTriggerMouseLeave(event: MouseEvent) {
  const relatedTarget = event.relatedTarget as Node | null
  if (relatedTarget && tooltipRef.value?.contains(relatedTarget)) {
    return
  }
  scheduleHideTooltip()
}

/**
 * 处理提示层移出事件。
 * 作用：当鼠标从提示层回到触发元素时保持显示，否则关闭提示层。
 *
 * @param event 鼠标移出事件
 */
function handleTooltipMouseLeave(event: MouseEvent) {
  const relatedTarget = event.relatedTarget as Node | null
  if (relatedTarget && triggerRef.value?.contains(relatedTarget)) {
    return
  }
  scheduleHideTooltip()
}

/**
 * 复制提示文本。
 * 作用：支持将完整链接/对象键快速复制到剪贴板。
 */
async function copyText() {
  try {
    await navigator.clipboard.writeText(props.text)
    toast.add({ title: '内容已复制', color: 'success' })
  }
  catch {
    toast.add({ title: '复制失败', description: '浏览器未授权复制。', color: 'warning' })
  }
}

/**
 * 监听窗口尺寸变化。
 * 作用：窗口尺寸变化时重新计算提示层位置，避免错位。
 */
function handleWindowResize() {
  if (!isVisible.value) {
    return
  }
  updateTooltipPosition()
}

/**
 * 监听页面滚动。
 * 作用：滚动时同步更新提示层位置，避免提示层停留在旧位置。
 */
function handleWindowScroll() {
  if (!isVisible.value) {
    return
  }
  updateTooltipPosition()
}

onMounted(() => {
  window.addEventListener('resize', handleWindowResize)
  window.addEventListener('scroll', handleWindowScroll, true)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize)
  window.removeEventListener('scroll', handleWindowScroll, true)
  clearHideTimer()
})
</script>

<template>
  <div
    ref="triggerRef"
    class="min-w-0"
    @mouseenter="showTooltip"
    @mouseleave="handleTriggerMouseLeave"
  >
    <p class="truncate text-xs cursor-help">
      {{ text }}
    </p>
  </div>

  <Teleport to="body">
    <div
      v-if="isVisible"
      ref="tooltipRef"
      class="fixed z-[9999]"
      :style="{
        left: `${tooltipPosition.left}px`,
        top: `${tooltipPosition.top}px`,
        transform: tooltipPlacement === 'top' ? 'translateY(-100%)' : 'none'
      }"
      @mouseenter="showTooltip"
      @mouseleave="handleTooltipMouseLeave"
    >
      <div
        class="rounded-[10px] border border-slate-200/80 bg-white/96 px-3 py-2 text-xs leading-5 text-slate-700 shadow-[0_16px_30px_-24px_rgba(15,23,42,0.45)] backdrop-blur-sm dark:border-white/15 dark:bg-slate-950/95 dark:text-slate-200"
        :class="tooltipMaxWidthClass"
      >
        <div class="mb-2 flex items-center justify-end">
          <button
            type="button"
            class="rounded-md border border-slate-200 bg-white px-2 py-1 text-[11px] leading-none text-slate-600 transition hover:border-slate-300 hover:text-slate-900 dark:border-white/20 dark:bg-white/8 dark:text-slate-300 dark:hover:border-white/30 dark:hover:text-white"
            @click="copyText"
          >
            复制
          </button>
        </div>
        <p class="break-all select-text">
          {{ text }}
        </p>
      </div>
    </div>
  </Teleport>
</template>
