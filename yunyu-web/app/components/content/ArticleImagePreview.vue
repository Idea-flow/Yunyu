<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

/**
 * 文章图片预览组件。
 * 作用：统一管理正文容器中的图片增强逻辑，为图片补充点击放大、键盘访问、
 * 同组切换与全屏预览层，让正文渲染组件只负责内容输出。
 */
interface PreviewImageItem {
  src: string
  alt: string
}

const props = withDefaults(defineProps<{
  targetElement: HTMLElement | null
  contentKey?: string
}>(), {
  contentKey: ''
})

const previewImages = ref<PreviewImageItem[]>([])
const currentPreviewIndex = ref(-1)
const previewTouchStartX = ref(0)
const previewTouchCurrentX = ref(0)
const cleanupCallbacks: Array<() => void> = []
const imagePreloadCache = new Set<string>()
const PREVIEW_SWIPE_THRESHOLD = 48

/**
 * 判断当前是否处于图片预览状态。
 * 作用：统一给模板和键盘逻辑提供当前弹层开关状态。
 */
const isPreviewOpen = computed(() => currentPreviewIndex.value >= 0 && currentPreviewIndex.value < previewImages.value.length)

/**
 * 判断当前是否可以切换到上一张图片。
 * 作用：用于控制左侧切换按钮显示与键盘切换边界。
 */
const canPreviewPrevious = computed(() => currentPreviewIndex.value > 0)

/**
 * 判断当前是否可以切换到下一张图片。
 * 作用：用于控制右侧切换按钮显示与键盘切换边界。
 */
const canPreviewNext = computed(() => currentPreviewIndex.value >= 0 && currentPreviewIndex.value < previewImages.value.length - 1)

/**
 * 读取当前正在预览的图片信息。
 * 作用：把当前索引映射为模板展示所需的图片地址和说明文本。
 */
const currentPreviewImage = computed<PreviewImageItem | null>(() => {
  if (!isPreviewOpen.value) {
    return null
  }

  return previewImages.value[currentPreviewIndex.value] || null
})

/**
 * 生成当前图片预览的稳定键值。
 * 作用：让切换上一张/下一张时只重播图片动画，不让整层遮罩反复闪动。
 */
const currentPreviewImageKey = computed(() => {
  const image = currentPreviewImage.value
  return image ? `${currentPreviewIndex.value}:${image.src}` : 'empty'
})

/**
 * 清理图片增强事件。
 * 作用：在正文内容更新或组件卸载时移除旧图片监听，防止重复绑定。
 */
function cleanupImageEnhancements() {
  while (cleanupCallbacks.length) {
    cleanupCallbacks.pop()?.()
  }
}

/**
 * 锁定页面滚动。
 * 作用：图片预览打开后阻止正文继续滚动，保证查看体验稳定。
 */
function lockBodyScroll() {
  document.body.style.overflow = 'hidden'
}

/**
 * 恢复页面滚动。
 * 作用：图片预览关闭后释放滚动锁定，恢复正文页面原本交互。
 */
function unlockBodyScroll() {
  document.body.style.overflow = ''
}

/**
 * 按索引打开图片预览层。
 * 作用：将当前点击图片定位到图片组中的具体位置，并显示全屏预览。
 *
 * @param index 当前要打开的图片索引
 */
function openImagePreviewByIndex(index: number) {
  if (index < 0 || index >= previewImages.value.length) {
    return
  }

  currentPreviewIndex.value = index
  lockBodyScroll()
}

/**
 * 关闭图片预览层。
 * 作用：重置当前预览索引并恢复正文滚动。
 */
function closeImagePreview() {
  currentPreviewIndex.value = -1
  unlockBodyScroll()
}

/**
 * 切换到上一张图片。
 * 作用：在多图预览时支持向前浏览同一篇文章中的上一张图片。
 */
function previewPreviousImage() {
  if (!canPreviewPrevious.value) {
    return
  }

  currentPreviewIndex.value -= 1
}

/**
 * 切换到下一张图片。
 * 作用：在多图预览时支持向后浏览同一篇文章中的下一张图片。
 */
function previewNextImage() {
  if (!canPreviewNext.value) {
    return
  }

  currentPreviewIndex.value += 1
}

/**
 * 预加载指定地址的图片资源。
 * 作用：把即将切换到的相邻图片提前交给浏览器加载，减少切图时的瞬时白屏。
 *
 * @param src 图片地址
 */
function preloadImage(src: string) {
  if (!src || imagePreloadCache.has(src)) {
    return
  }

  const image = new Image()
  image.decoding = 'async'
  image.loading = 'eager'
  image.src = src
  imagePreloadCache.add(src)
}

/**
 * 预加载当前图片相邻的前后图片。
 * 作用：在用户打开图片或切图后，提前准备上一张和下一张资源，提升连续浏览流畅度。
 *
 * @param index 当前正在预览的图片索引
 */
function preloadNearbyImages(index: number) {
  const previousImage = previewImages.value[index - 1]
  const nextImage = previewImages.value[index + 1]

  if (previousImage?.src) {
    preloadImage(previousImage.src)
  }

  if (nextImage?.src) {
    preloadImage(nextImage.src)
  }
}

/**
 * 根据点击热区处理图片切换。
 * 作用：让桌面端用户点击大图左右半区即可切换，减少对左右按钮的依赖。
 *
 * @param event 当前点击事件
 */
function handleImageAreaClick(event: MouseEvent) {
  const currentTarget = event.currentTarget

  if (!(currentTarget instanceof HTMLElement)) {
    return
  }

  const rect = currentTarget.getBoundingClientRect()
  const clickOffsetX = event.clientX - rect.left
  const isLeftArea = clickOffsetX < rect.width / 2

  if (isLeftArea) {
    previewPreviousImage()
    return
  }

  previewNextImage()
}

/**
 * 记录触摸起点。
 * 作用：为移动端横向滑动切图计算滑动方向和距离。
 *
 * @param event 触摸事件
 */
function handlePreviewTouchStart(event: TouchEvent) {
  const touch = event.touches[0]

  if (!touch) {
    return
  }

  previewTouchStartX.value = touch.clientX
  previewTouchCurrentX.value = touch.clientX
}

/**
 * 更新当前触摸位置。
 * 作用：持续记录用户横向滑动轨迹，为触摸结束时的切图判断提供依据。
 *
 * @param event 触摸事件
 */
function handlePreviewTouchMove(event: TouchEvent) {
  const touch = event.touches[0]

  if (!touch) {
    return
  }

  previewTouchCurrentX.value = touch.clientX
}

/**
 * 根据滑动距离处理切图。
 * 作用：当用户横向滑动距离达到阈值时切换上一张或下一张图片。
 */
function handlePreviewTouchEnd() {
  const deltaX = previewTouchCurrentX.value - previewTouchStartX.value

  if (Math.abs(deltaX) < PREVIEW_SWIPE_THRESHOLD) {
    previewTouchStartX.value = 0
    previewTouchCurrentX.value = 0
    return
  }

  if (deltaX > 0) {
    previewPreviousImage()
  } else {
    previewNextImage()
  }

  previewTouchStartX.value = 0
  previewTouchCurrentX.value = 0
}

/**
 * 处理全局键盘交互。
 * 作用：允许用户通过 ESC 关闭预览，并使用左右方向键切换同组图片。
 *
 * @param event 键盘事件
 */
function handleWindowKeydown(event: KeyboardEvent) {
  if (!isPreviewOpen.value) {
    return
  }

  if (event.key === 'Escape') {
    closeImagePreview()
    return
  }

  if (event.key === 'ArrowLeft') {
    event.preventDefault()
    previewPreviousImage()
    return
  }

  if (event.key === 'ArrowRight') {
    event.preventDefault()
    previewNextImage()
  }
}

/**
 * 收集当前正文中的可预览图片。
 * 作用：把正文中未被链接包裹的图片统一组织成图片组，供预览层切换使用。
 *
 * @returns 当前正文中的图片预览数据列表
 */
function collectPreviewImages() {
  if (!props.targetElement) {
    return []
  }

  const images = props.targetElement.querySelectorAll<HTMLImageElement>('img')

  return Array.from(images)
    .filter(image => !image.closest('a'))
    .map(image => ({
      src: image.currentSrc || image.src || '',
      alt: image.alt || '文章配图预览'
    }))
    .filter(item => Boolean(item.src))
}

/**
 * 为正文图片绑定预览能力。
 * 作用：扫描目标容器中的图片节点，为未被链接包裹的图片补充点击放大和键盘打开能力，
 * 同时建立当前文章图片组索引，支持后续左右切换。
 */
async function bindPreviewableImages() {
  if (import.meta.server) {
    return
  }

  await nextTick()
  closeImagePreview()
  cleanupImageEnhancements()
  previewImages.value = collectPreviewImages()

  if (!props.targetElement) {
    return
  }

  const images = props.targetElement.querySelectorAll<HTMLImageElement>('img')
  let previewableIndex = 0

  for (const image of images) {
    if (image.closest('a')) {
      continue
    }

    const imageIndex = previewableIndex
    previewableIndex += 1

    image.classList.add(
      'cursor-zoom-in',
      'transition-transform',
      'duration-300',
      'ease-out',
      'hover:scale-[1.01]'
    )
    image.setAttribute('role', 'button')
    image.setAttribute('tabindex', '0')
    image.setAttribute('aria-label', image.alt || '点击放大查看图片')

    const handleClick = () => {
      openImagePreviewByIndex(imageIndex)
    }

    const handleKeydown = (event: KeyboardEvent) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        openImagePreviewByIndex(imageIndex)
      }
    }

    image.addEventListener('click', handleClick)
    image.addEventListener('keydown', handleKeydown)

    cleanupCallbacks.push(() => {
      image.removeEventListener('click', handleClick)
      image.removeEventListener('keydown', handleKeydown)
    })
  }
}

watch(() => [props.targetElement, props.contentKey] as const, async () => {
  await bindPreviewableImages()
})

watch(currentPreviewIndex, index => {
  if (index < 0) {
    return
  }

  preloadNearbyImages(index)
})

onMounted(async () => {
  window.addEventListener('keydown', handleWindowKeydown)
  await bindPreviewableImages()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleWindowKeydown)
  closeImagePreview()
  cleanupImageEnhancements()
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="duration-[280ms] ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="duration-[220ms] ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="isPreviewOpen"
        class="fixed inset-0 z-[120] flex items-center justify-center bg-slate-950/68 px-4 py-6 backdrop-blur-[10px] sm:px-8"
        @click="closeImagePreview"
      >
        <div class="pointer-events-none absolute inset-x-0 top-0 flex items-center justify-between px-4 py-4 sm:px-6">
          <div class="pointer-events-auto rounded-full border border-white/12 bg-white/10 px-3 py-1 text-xs font-medium tracking-[0.02em] text-white/88 shadow-[0_10px_30px_-18px_rgba(15,23,42,0.8)]">
            {{ currentPreviewIndex + 1 }} / {{ previewImages.length }}
          </div>

          <button
            type="button"
            class="pointer-events-auto inline-flex h-11 w-11 items-center justify-center rounded-full border border-white/14 bg-white/10 text-white/90 transition duration-300 hover:scale-[1.02] hover:bg-white/16"
            aria-label="关闭图片预览"
            @click.stop="closeImagePreview"
          >
            <UIcon name="i-lucide-x" class="size-5" />
          </button>
        </div>

        <button
          v-if="canPreviewPrevious"
          type="button"
          class="absolute left-3 top-1/2 z-[1] inline-flex h-12 w-12 -translate-y-1/2 items-center justify-center rounded-full border border-white/12 bg-white/10 text-white/92 shadow-[0_18px_34px_-24px_rgba(15,23,42,0.85)] transition duration-300 hover:scale-[1.03] hover:bg-white/16 sm:left-5 sm:h-14 sm:w-14"
          aria-label="查看上一张图片"
          @click.stop="previewPreviousImage"
        >
          <UIcon name="i-lucide-chevron-left" class="size-6" />
        </button>

        <button
          v-if="canPreviewNext"
          type="button"
          class="absolute right-3 top-1/2 z-[1] inline-flex h-12 w-12 -translate-y-1/2 items-center justify-center rounded-full border border-white/12 bg-white/10 text-white/92 shadow-[0_18px_34px_-24px_rgba(15,23,42,0.85)] transition duration-300 hover:scale-[1.03] hover:bg-white/16 sm:right-5 sm:h-14 sm:w-14"
          aria-label="查看下一张图片"
          @click.stop="previewNextImage"
        >
          <UIcon name="i-lucide-chevron-right" class="size-6" />
        </button>

        <div class="flex w-full max-w-[min(92vw,1400px)] items-center justify-center px-10 sm:px-16">
          <Transition
            mode="out-in"
            enter-active-class="duration-[320ms] ease-out"
            enter-from-class="translate-y-1 scale-[0.975] opacity-0"
            enter-to-class="translate-y-0 scale-100 opacity-100"
            leave-active-class="duration-[220ms] ease-in"
            leave-from-class="translate-y-0 scale-100 opacity-100"
            leave-to-class="translate-y-1 scale-[0.985] opacity-0"
          >
            <figure
              :key="currentPreviewImageKey"
              class="flex max-h-[88vh] w-full flex-col items-center justify-center"
              @click.stop
            >
              <div
                class="relative flex w-full items-center justify-center"
                @click.stop="handleImageAreaClick"
                @touchstart.passive="handlePreviewTouchStart"
                @touchmove.passive="handlePreviewTouchMove"
                @touchend.passive="handlePreviewTouchEnd"
              >
                <img
                  v-if="currentPreviewImage"
                  :src="currentPreviewImage.src"
                  :alt="currentPreviewImage.alt"
                  class="max-h-[78vh] max-w-full rounded-[24px] object-contain shadow-[0_30px_80px_-32px_rgba(15,23,42,0.78)]"
                >

                <div
                  v-if="canPreviewPrevious"
                  class="pointer-events-none absolute inset-y-0 left-0 hidden w-1/2 items-center justify-start pl-4 sm:flex"
                >
                  <span class="rounded-full border border-white/10 bg-white/8 px-2.5 py-1 text-xs text-white/68 backdrop-blur-sm">
                    点击查看上一张
                  </span>
                </div>

                <div
                  v-if="canPreviewNext"
                  class="pointer-events-none absolute inset-y-0 right-0 hidden w-1/2 items-center justify-end pr-4 sm:flex"
                >
                  <span class="rounded-full border border-white/10 bg-white/8 px-2.5 py-1 text-xs text-white/68 backdrop-blur-sm">
                    点击查看下一张
                  </span>
                </div>
              </div>

              <figcaption
                v-if="currentPreviewImage?.alt"
                class="mt-4 max-w-3xl rounded-full border border-white/10 bg-white/10 px-4 py-2 text-center text-sm leading-6 text-white/82 backdrop-blur-sm"
              >
                {{ currentPreviewImage.alt }}
              </figcaption>
            </figure>
          </Transition>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
