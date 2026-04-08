/**
 * 云屿全局提示封装。
 * 作用：在 `Nuxt UI` 全局 toast 能力之上统一成功、错误、信息、警告提示的文案风格与图标语义，
 * 让前后台临时反馈保持同一套产品语言。
 */
export function useYunyuToast() {
  const toast = useToast()

  /**
   * 发送一条统一风格的提示。
   * 作用：屏蔽页面内重复散写的颜色、图标与默认文案规则。
   *
   * @param options 当前提示的配置项
   */
  function show(options: {
    type?: 'success' | 'error' | 'info' | 'warning'
    title: string
    description?: string
    duration?: number
  }) {
    const type = options.type || 'info'

    toast.add({
      title: options.title,
      description: options.description,
      color: type === 'info' ? 'info' : type,
      icon: resolveIcon(type),
      duration: options.duration ?? 2000,
      close: false,
      progress: false,
      ui: {
        root: 'rounded-[18px] border border-slate-200/75 bg-white/94 px-4 py-3 shadow-[0_18px_42px_-30px_rgba(15,23,42,0.22)] ring-0 backdrop-blur-md dark:border-white/10 dark:bg-slate-950/92',
        wrapper: 'min-w-0',
        title: 'text-[0.92rem] font-medium tracking-[-0.01em] text-slate-900 dark:text-slate-50',
        description: 'mt-1 text-[0.8rem] leading-6 text-slate-500 dark:text-slate-400',
        icon: 'mt-0.5 size-4.5 shrink-0',
        actions: 'hidden',
        close: 'hidden',
        progress: 'hidden'
      }
    })
  }

  /**
   * 发送成功提示。
   * 作用：为提交完成、复制成功等正向反馈提供简洁统一的调用入口。
   *
   * @param title 提示标题
   * @param description 提示说明
   * @param duration 展示时长
   */
  function success(title: string, description?: string, duration?: number) {
    show({ type: 'success', title, description, duration })
  }

  /**
   * 发送错误提示。
   * 作用：为接口失败、复制失败等异常反馈提供统一语义入口。
   *
   * @param title 提示标题
   * @param description 提示说明
   * @param duration 展示时长
   */
  function error(title: string, description?: string, duration?: number) {
    show({ type: 'error', title, description, duration })
  }

  /**
   * 发送信息提示。
   * 作用：为普通操作反馈或状态提醒提供默认提示入口。
   *
   * @param title 提示标题
   * @param description 提示说明
   * @param duration 展示时长
   */
  function info(title: string, description?: string, duration?: number) {
    show({ type: 'info', title, description, duration })
  }

  /**
   * 发送警告提示。
   * 作用：为存在风险但尚未失败的场景提供更温和的提醒能力。
   *
   * @param title 提示标题
   * @param description 提示说明
   * @param duration 展示时长
   */
  function warning(title: string, description?: string, duration?: number) {
    show({ type: 'warning', title, description, duration })
  }

  /**
   * 解析不同提示类型对应的图标。
   * 作用：集中维护提示图标映射，避免页面侧重复判断。
   *
   * @param type 提示类型
   * @returns 图标名称
   */
  function resolveIcon(type: 'success' | 'error' | 'info' | 'warning') {
    switch (type) {
      case 'success':
        return 'i-lucide-badge-check'
      case 'error':
        return 'i-lucide-octagon-alert'
      case 'warning':
        return 'i-lucide-triangle-alert'
      default:
        return 'i-lucide-sparkles'
    }
  }

  return {
    show,
    success,
    error,
    info,
    warning
  }
}
