import { inject, provide, ref } from 'vue'

export type VideoToastTone = 'info' | 'success' | 'warning' | 'error'

export interface VideoToastInput {
  title: string
  description?: string
  tone?: VideoToastTone
  duration?: number
}

export interface VideoToastItem extends VideoToastInput {
  id: number
}

interface VideoToastManager {
  items: Ref<VideoToastItem[]>
  add: (input: VideoToastInput) => number
  remove: (id: number) => void
  clear: () => void
}

const VIDEO_TOAST_KEY = Symbol('video-toast')
let videoToastId = 0

/**
 * 创建播放器专用 toast 管理器，
 * 让播放器目录在不依赖外部通知系统时仍能提供统一反馈。
 */
function createVideoToastManager(): VideoToastManager {
  const items = ref<VideoToastItem[]>([])
  const timerMap = new Map<number, ReturnType<typeof setTimeout>>()

  /**
   * 移除指定 toast，并同步清理对应计时器。
   */
  function remove(id: number) {
    const currentTimer = timerMap.get(id)

    if (currentTimer) {
      clearTimeout(currentTimer)
      timerMap.delete(id)
    }

    items.value = items.value.filter((item) => item.id !== id)
  }

  /**
   * 添加一条新的 toast 反馈，并在超时后自动移除。
   */
  function add(input: VideoToastInput) {
    const id = ++videoToastId
    const duration = input.duration ?? (input.tone === 'error' ? 4200 : 2600)
    const nextItem: VideoToastItem = {
      id,
      tone: input.tone || 'info',
      title: input.title,
      description: input.description,
      duration
    }

    items.value = [...items.value, nextItem]

    if (duration > 0) {
      const timer = setTimeout(() => {
        remove(id)
      }, duration)
      timerMap.set(id, timer)
    }

    return id
  }

  /**
   * 清空全部 toast，通常用于播放器销毁或强制重置场景。
   */
  function clear() {
    for (const timer of timerMap.values()) {
      clearTimeout(timer)
    }

    timerMap.clear()
    items.value = []
  }

  return {
    items,
    add,
    remove,
    clear
  }
}

const fallbackVideoToastManager = createVideoToastManager()

/**
 * 在播放器根组件中提供 toast 管理器，
 * 让目录内所有子组件与 composable 共享同一套通知状态。
 */
export function provideVideoToast() {
  const manager = createVideoToastManager()
  provide(VIDEO_TOAST_KEY, manager)
  return manager
}

/**
 * 读取当前播放器作用域内的 toast 管理器，
 * 若未显式提供则回退到安全的本地兜底实例。
 */
export function useVideoToast() {
  return inject<VideoToastManager>(VIDEO_TOAST_KEY, fallbackVideoToastManager)
}
