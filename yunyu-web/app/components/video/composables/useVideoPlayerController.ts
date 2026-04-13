import Hls from 'hls.js'
import type { VideoSourceItem } from '../player.types'
import { sortVideoSourcesByPriority } from '../utils/video-source-order'
import { buildIframeRenderAttributes } from '../utils/video-iframe'
import { useVideoToast } from './useVideoToast'

/**
 * 这个组合式函数负责统一管理播放器状态、HLS 初始化、线路切换和控件交互逻辑，
 * 让手机端与桌面端组件可以复用同一套播放能力，只分别维护各自的展示样式。
 */
export function useVideoPlayerController(
  props: {
    sources: VideoSourceItem[]
    activeSourceId?: number | null
    debug?: boolean
  },
  emit: (event: 'update:activeSourceId', value: number | null) => void
) {
  /**
   * 这个类型用于描述播放器当前的加载阶段，
   * 便于在界面上展示“连接中 / 缓冲中 / 定位中”等反馈。
   */
  type PlayerLoadingState = 'idle' | 'connecting' | 'loading_metadata' | 'buffering' | 'seeking'

  /**
   * 这个类型用于补齐 Safari 私有媒体 API，
   * 便于在标准接口缺失时回退到浏览器自身的视频能力。
   */
  type ExtendedVideoElement = HTMLVideoElement & {
    webkitEnterFullscreen?: () => void
    webkitSupportsFullscreen?: boolean
    webkitDisplayingFullscreen?: boolean
    webkitSetPresentationMode?: (mode: 'inline' | 'picture-in-picture' | 'fullscreen') => void
    webkitPresentationMode?: 'inline' | 'picture-in-picture' | 'fullscreen'
  }

  /**
   * 这个类型用于补齐旧版 WebKit 的元素全屏接口，
   * 避免某些移动端浏览器只支持私有前缀时无法进入全屏。
   */
  type ExtendedFullscreenElement = HTMLElement & {
    webkitRequestFullscreen?: () => Promise<void> | void
    webkitRequestFullScreen?: () => Promise<void> | void
  }

  const HLS_OPTIMIZED_BUFFER_CONFIG = {
    maxBufferLength: 45,
    maxMaxBufferLength: 90,
    backBufferLength: 30,
    maxBufferSize: 60 * 1000 * 1000,
    lowLatencyMode: false
  } satisfies Partial<Hls['config']>
  const BUFFERING_STATE_DELAY_MS = 450
  const BUFFERING_STATE_MIN_VISIBLE_MS = 650

  const videoToast = useVideoToast()
  const videoElement = ref<HTMLVideoElement | null>(null)
  const internalActiveSourceId = ref<number | null>(null)
  const sourceErrorMessage = ref('')
  const isSwitching = ref(false)
  const iframeLoaded = ref(false)
  const hlsInstance = shallowRef<Hls | null>(null)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(1)
  const isMuted = ref(false)
  const isPlaying = ref(false)
  const isFullscreen = ref(false)
  const isPictureInPicture = ref(false)
  const isPointerVisible = ref(true)
  const keepControlsVisible = ref(true)
  const isClientMounted = ref(false)
  const pictureInPictureUnavailable = ref(false)
  const fullscreenUnavailable = ref(false)
  const playerLoadingState = ref<PlayerLoadingState>('idle')
  const hasStartedPlayback = ref(false)
  const seekingTargetTime = ref<number | null>(null)
  const iframeLoadSlow = ref(false)
  const iframeLoadTimer = ref<ReturnType<typeof setTimeout> | null>(null)
  const bufferingStateTimer = ref<ReturnType<typeof setTimeout> | null>(null)
  const bufferingVisibleSince = ref<number | null>(null)
  const controlsHideTimer = ref<ReturnType<typeof setTimeout> | null>(null)
  const isExternallyControlled = computed(() => props.activeSourceId !== undefined)
  const normalizedSources = computed(() => {
    return props.sources.map((source) => ({
      ...source,
      normalizedSourceType: normalizeSourceType(source.sourceType, source.sourceUrl)
    }))
  })
  const isVideoSource = computed(() => {
    return activeSource.value?.normalizedSourceType === 'mp4' || activeSource.value?.normalizedSourceType === 'm3u8'
  })
  const isAppleMobileBrowser = computed(() => {
    if (!import.meta.client) {
      return false
    }

    const currentUserAgent = navigator.userAgent || ''
    const isAppleDevice = /iPhone|iPad|iPod/i.test(currentUserAgent)
      || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1)

    return isAppleDevice && navigator.maxTouchPoints > 0
  })
  const canUseFullscreen = computed(() => {
    if (!import.meta.client || !isClientMounted.value || !videoElement.value || !isVideoSource.value || fullscreenUnavailable.value) {
      return false
    }

    const video = videoElement.value as ExtendedVideoElement
    const playerContainer = video.closest('[data-player-shell]') as ExtendedFullscreenElement | null

    return Boolean(
      playerContainer?.requestFullscreen
      || playerContainer?.webkitRequestFullscreen
      || playerContainer?.webkitRequestFullScreen
      || video.webkitEnterFullscreen
    )
  })
  const canUsePictureInPicture = computed(() => {
    if (!import.meta.client || !isClientMounted.value || !videoElement.value || !isVideoSource.value || pictureInPictureUnavailable.value) {
      return false
    }

    const video = videoElement.value as ExtendedVideoElement

    return Boolean(
      (document.pictureInPictureEnabled && typeof video.requestPictureInPicture === 'function')
      || typeof video.webkitSetPresentationMode === 'function'
    )
  })
  const resolvedActiveSourceId = computed(() => {
    return isExternallyControlled.value ? props.activeSourceId ?? null : internalActiveSourceId.value
  })
  const sortedSources = computed(() => {
    /**
     * 播放器线路顺序要与服务端详情返回顺序一致，
     * 避免默认选中线路和用户看到的按钮顺序不一致。
     */
    return sortVideoSourcesByPriority(normalizedSources.value)
  })
  const activeSource = computed(() => {
    return sortedSources.value.find((item) => item.id === resolvedActiveSourceId.value) || sortedSources.value[0] || null
  })
  const activeSourceIndex = computed(() => {
    return sortedSources.value.findIndex((item) => item.id === activeSource.value?.id)
  })
  const nextFallbackSource = computed(() => {
    if (activeSourceIndex.value < 0) {
      return null
    }

    return sortedSources.value[activeSourceIndex.value + 1] || null
  })
  const playerHint = computed(() => {
    if (!activeSource.value) {
      return '当前没有可用播放源。'
    }

    if (activeSource.value.normalizedSourceType === 'iframe' || activeSource.value.normalizedSourceType === 'iframeFull') {
      return '当前为网页嵌入源，若加载较慢可尝试切换其他线路。'
    }

    if (activeSource.value.normalizedSourceType === 'm3u8') {
      return '当前为流媒体线路，适合更长时间播放。'
    }

    return '当前为直链视频源，通常启动更快。'
  })
  const progressPercent = computed(() => {
    if (!duration.value) {
      return 0
    }

    return Math.min(100, (currentTime.value / duration.value) * 100)
  })
  const volumePercent = computed(() => {
    return isMuted.value ? 0 : Math.round(volume.value * 100)
  })
  const isLoading = computed(() => {
    return playerLoadingState.value !== 'idle'
  })
  const shouldShowLoadingOverlay = computed(() => {
    /**
     * 进度定位时保留原画面，不再显示整层黑色 loading 遮罩，
     * 避免用户在拖动进度条时误以为播放器卡死或重新加载。
     */
    return isLoading.value && playerLoadingState.value !== 'seeking'
  })
  const shouldShowControlsOverlay = computed(() => {
    if (!isVideoSource.value) {
      return false
    }

    return keepControlsVisible.value || isPointerVisible.value || !isPlaying.value
  })
  const shouldShowImmersivePoster = computed(() => {
    if (!isVideoSource.value) {
      return false
    }

    return isSwitching.value || (!hasStartedPlayback.value && isLoading.value)
  })
  const loadingText = computed(() => {
    if (!activeSource.value) {
      return '正在准备播放器...'
    }

    if (playerLoadingState.value === 'connecting') {
      return isSwitching.value
        ? `正在切换到 ${activeSource.value.label}...`
        : `正在连接 ${activeSource.value.label}...`
    }

    if (playerLoadingState.value === 'loading_metadata') {
      return '正在加载视频信息...'
    }

    if (playerLoadingState.value === 'buffering') {
      return '正在缓冲，请稍候...'
    }

    if (playerLoadingState.value === 'seeking') {
      return `正在定位到 ${formatTime(seekingTargetTime.value || currentTime.value)}...`
    }

    return ''
  })
  const progressFeedbackText = computed(() => {
    if (playerLoadingState.value === 'buffering') {
      return '正在缓冲当前进度'
    }

    return ''
  })
  const iframeLoadingText = computed(() => {
    if (!activeSource.value || (activeSource.value.normalizedSourceType !== 'iframe' && activeSource.value.normalizedSourceType !== 'iframeFull')) {
      return ''
    }

    return iframeLoadSlow.value
      ? '目标站点响应较慢，请稍候或切换其他线路'
      : '正在加载嵌入播放页...'
  })

  /**
   * 更新播放器当前加载阶段，统一收口中间状态切换。
   */
  function setPlayerLoadingState(nextState: PlayerLoadingState, targetTime?: number | null) {
    if (nextState !== 'buffering') {
      bufferingVisibleSince.value = null
    }

    playerLoadingState.value = nextState
    seekingTargetTime.value = nextState === 'seeking' ? (targetTime ?? seekingTargetTime.value) : null

    if (nextState === 'buffering') {
      bufferingVisibleSince.value = Date.now()
    }
  }

  /**
   * 清空播放器加载阶段，恢复到普通播放或暂停状态。
   */
  function clearPlayerLoadingState() {
    playerLoadingState.value = 'idle'
    seekingTargetTime.value = null
    bufferingVisibleSince.value = null
  }

  /**
   * 清理缓冲态延迟切换计时器，避免重复进入 buffering。
   */
  function clearBufferingStateTimer() {
    if (bufferingStateTimer.value) {
      clearTimeout(bufferingStateTimer.value)
      bufferingStateTimer.value = null
    }
  }

  /**
   * 延迟进入缓冲态，过滤短暂的 waiting/stalled 抖动。
   */
  function scheduleBufferingState() {
    if (playerLoadingState.value === 'seeking' || playerLoadingState.value === 'buffering') {
      return
    }

    clearBufferingStateTimer()
    bufferingStateTimer.value = setTimeout(() => {
      bufferingStateTimer.value = null
      setPlayerLoadingState('buffering')
    }, BUFFERING_STATE_DELAY_MS)
  }

  /**
   * 在满足最短展示时长后再退出缓冲态，避免 loading 层一闪而过。
   */
  function clearBufferingStateWithGuard() {
    clearBufferingStateTimer()

    if (playerLoadingState.value !== 'buffering') {
      clearPlayerLoadingState()
      return
    }

    const visibleSince = bufferingVisibleSince.value
    const visibleDuration = visibleSince ? Date.now() - visibleSince : BUFFERING_STATE_MIN_VISIBLE_MS

    if (visibleDuration >= BUFFERING_STATE_MIN_VISIBLE_MS) {
      clearPlayerLoadingState()
      return
    }

    bufferingStateTimer.value = setTimeout(() => {
      bufferingStateTimer.value = null
      clearPlayerLoadingState()
    }, BUFFERING_STATE_MIN_VISIBLE_MS - visibleDuration)
  }

  /**
   * 清理 iframe 慢加载检测计时器，避免切线后残留旧状态。
   */
  function clearIframeLoadTimer() {
    if (iframeLoadTimer.value) {
      clearTimeout(iframeLoadTimer.value)
      iframeLoadTimer.value = null
    }
  }

  /**
   * 为 iframe 播放页启动慢加载检测，帮助用户理解等待来自外部站点。
   */
  function startIframeLoadTimer() {
    clearIframeLoadTimer()
    iframeLoadSlow.value = false

    iframeLoadTimer.value = setTimeout(() => {
      iframeLoadSlow.value = true
    }, 2500)
  }

  /**
   * 输出播放器调试信息，
   * 方便排查移动端按钮事件是否触发以及控制逻辑是否真正执行。
   */
  function debugPlayerAction(title: string, description?: string) {
    if (!props.debug) {
      return
    }

    console.info(`[VideoPlayer Debug] ${title}`, {
      description,
      activeSource: activeSource.value?.label,
      sourceType: activeSource.value?.normalizedSourceType,
      isPlaying: isPlaying.value,
      currentTime: currentTime.value,
      duration: duration.value
    })

    videoToast.add({
      title,
      description,
      tone: 'info'
    })
  }

  /**
   * 释放旧的 HLS 实例，避免切换播放源后残留监听器。
   */
  function destroyHlsInstance() {
    clearBufferingStateTimer()

    if (hlsInstance.value) {
      hlsInstance.value.destroy()
      hlsInstance.value = null
    }
  }

  /**
   * 清理自动隐藏控制层的计时器，避免多次重复创建。
   */
  function clearControlsHideTimer() {
    if (controlsHideTimer.value) {
      clearTimeout(controlsHideTimer.value)
      controlsHideTimer.value = null
    }
  }

  /**
   * 返回当前播放源的展示类型标签，便于 UI 统一显示。
   */
  function getSourceTypeLabel(sourceType: VideoSourceItem['sourceType'] | null | undefined) {
    const mapping = {
      mp4: 'MP4 直链',
      m3u8: 'M3U8 流媒体',
      iframe: 'Iframe 嵌入',
      iframeFull: 'Iframe 完整嵌入'
    }

    return mapping[sourceType as keyof typeof mapping] || '未知线路'
  }

  /**
   * 判断当前 iframe 线路是否需要附加沙盒权限。
   * 当前规则为：仅 fm线路、up线路、RF线路 与 UF线路 不加 sandbox，
   * 其余 iframe 线路都加上 allow-scripts 与 allow-same-origin。
   */
  function getIframeSandboxValue(source: Pick<VideoSourceItem, 'label' | 'sourceType'> | null | undefined) {
    if (!source || source.sourceType !== 'iframe') {
      return undefined
    }

    const normalizedLabel = source.label.trim()
    const lowerCaseLabel = normalizedLabel.toLowerCase()

    return lowerCaseLabel === 'fm线路'.toLowerCase()
      || lowerCaseLabel === 'up线路'.toLowerCase()
      || normalizedLabel === 'RF线路'
      || normalizedLabel === 'UF线路'
      ? undefined
      : 'allow-scripts allow-same-origin'
  }

  /**
   * 规范化历史播放源类型，兼容旧数据里的大小写、别名和按 URL 推断的情况。
   */
  function normalizeSourceType(sourceType: string | undefined, sourceUrl: string): VideoSourceItem['sourceType'] | null {
    const normalizedType = sourceType?.trim().toLowerCase()
    const normalizedUrl = sourceUrl.trim().toLowerCase()

    if (normalizedType === 'mp4' || normalizedType === 'video' || normalizedType === 'direct' || normalizedType === 's3mp4') {
      return 'mp4'
    }

    if (normalizedType === 'm3u8' || normalizedType === 'hls') {
      return 'm3u8'
    }

    if (normalizedType === 'iframefull' || normalizedType === 'iframe_full' || normalizedType === 'fulliframe') {
      return 'iframeFull'
    }

    if (normalizedType === 'iframe' || normalizedType === 'embed' || normalizedType === 'filemoon' || normalizedType === 'upnshare') {
      return 'iframe'
    }

    if (normalizedUrl.startsWith('<iframe')) {
      return 'iframeFull'
    }

    if (normalizedUrl.includes('.m3u8') || normalizedUrl.endsWith('.txt')) {
      return 'm3u8'
    }

    if (normalizedUrl.includes('/embed/') || normalizedUrl.includes('/e/') || normalizedUrl.includes('youtube.com/embed')) {
      return 'iframe'
    }

    if (normalizedUrl.includes('.mp4') || normalizedUrl.includes('.m4v') || normalizedUrl.includes('.webm')) {
      return 'mp4'
    }

    return null
  }

  /**
   * 将秒数转换成播放器常见的时间格式。
   */
  function formatTime(seconds: number) {
    if (!Number.isFinite(seconds) || seconds < 0) {
      return '00:00'
    }

    const totalSeconds = Math.floor(seconds)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const remainSeconds = totalSeconds % 60

    if (hours > 0) {
      return [hours, minutes, remainSeconds].map((value) => String(value).padStart(2, '0')).join(':')
    }

    return [minutes, remainSeconds].map((value) => String(value).padStart(2, '0')).join(':')
  }

  /**
   * 同步播放器当前播放状态，避免自定义控件与真实媒体状态脱节。
   */
  function syncPlaybackState() {
    if (!videoElement.value) {
      isPlaying.value = false
      keepControlsVisible.value = true
      currentTime.value = 0
      duration.value = 0
      volume.value = 1
      isMuted.value = false
      isFullscreen.value = false
      isPictureInPicture.value = false
      clearPlayerLoadingState()
      return
    }

    const video = videoElement.value as ExtendedVideoElement

    isPlaying.value = !video.paused
    keepControlsVisible.value = video.paused ? true : keepControlsVisible.value
    currentTime.value = video.currentTime || 0
    duration.value = Number.isFinite(video.duration) ? video.duration : 0
    volume.value = video.volume
    isMuted.value = video.muted
    isFullscreen.value = Boolean(
      document.fullscreenElement
      || video.webkitDisplayingFullscreen
      || video.webkitPresentationMode === 'fullscreen'
    )
    isPictureInPicture.value = Boolean(
      document.pictureInPictureElement === video
      || video.webkitPresentationMode === 'picture-in-picture'
    )
  }

  /**
   * 根据排序规则设置默认播放源，优先直链和流媒体源。
   */
  function initializeActiveSource() {
    const firstSourceId = sortedSources.value[0]?.id ?? null

    if (isExternallyControlled.value) {
      emit('update:activeSourceId', firstSourceId)
      return
    }

    internalActiveSourceId.value = firstSourceId
  }

  /**
   * 统一更新当前激活线路，兼容组件内部状态与外部受控状态。
   */
  function setActiveSource(sourceId: number | null) {
    if (isExternallyControlled.value) {
      emit('update:activeSourceId', sourceId)
      return
    }

    internalActiveSourceId.value = sourceId
  }

  /**
   * 切换播放源，并重置当前线路的错误状态与控件状态。
   */
  function switchSource(sourceId: number) {
    if (resolvedActiveSourceId.value === sourceId) {
      return
    }

    setActiveSource(sourceId)
    sourceErrorMessage.value = ''
    iframeLoaded.value = false
    isSwitching.value = true
    keepControlsVisible.value = true
    currentTime.value = 0
    duration.value = 0
    isPlaying.value = false
    isFullscreen.value = false
    isPictureInPicture.value = false
    clearIframeLoadTimer()
    iframeLoadSlow.value = false
    setPlayerLoadingState('connecting')
  }

  /**
   * 当视频源报错时，优先提示用户并尝试自动切换到下一条线路。
   */
  function handleSourceError() {
    if (!activeSource.value) {
      return
    }

    clearPlayerLoadingState()
    sourceErrorMessage.value = `当前线路“${activeSource.value.label}”加载失败。`

    if (nextFallbackSource.value) {
      videoToast.add({
        title: '播放源已切换',
        description: `“${activeSource.value.label}”不可用，已尝试切换到“${nextFallbackSource.value.label}”。`,
        tone: 'warning'
      })
      switchSource(nextFallbackSource.value.id)
      return
    }

    videoToast.add({
      title: '播放失败',
      description: '当前没有更多可切换线路，请稍后重试。',
      tone: 'error'
    })
  }

  /**
   * 主动切换到下一条可用线路，供播放器内嵌错误提示直接调用。
   */
  function switchToNextFallback() {
    if (!nextFallbackSource.value) {
      return
    }

    switchSource(nextFallbackSource.value.id)
  }

  /**
   * 复制当前播放源地址，便于后台排查或手动打开测试。
   */
  async function copySourceUrl() {
    if (!activeSource.value || !navigator?.clipboard) {
      return
    }

    try {
      await navigator.clipboard.writeText(activeSource.value.sourceUrl)
      videoToast.add({
        title: '已复制播放源地址',
        description: activeSource.value.label,
        tone: 'success'
      })
    } catch (error) {
      videoToast.add({
        title: '复制失败',
        description: error instanceof Error ? error.message : '当前浏览器未允许写入剪贴板。',
        tone: 'warning'
      })
    }
  }

  /**
   * 根据当前播放源类型加载不同的播放器能力。
   */
  async function setupPlayer() {
    destroyHlsInstance()
    iframeLoaded.value = false
    iframeLoadSlow.value = false
    clearIframeLoadTimer()
    clearBufferingStateTimer()
    pictureInPictureUnavailable.value = false
    fullscreenUnavailable.value = false

    const source = activeSource.value

    if (!source) {
      keepControlsVisible.value = true
      clearPlayerLoadingState()
      return
    }

    await nextTick()

    if (!source.normalizedSourceType) {
      keepControlsVisible.value = true
      clearPlayerLoadingState()
      sourceErrorMessage.value = `当前线路“${source.label}”的播放类型无法识别，请检查 sourceType 或 sourceUrl。`
      return
    }

    if (!videoElement.value || source.normalizedSourceType === 'iframe' || source.normalizedSourceType === 'iframeFull') {
      if (source.normalizedSourceType === 'iframe' || source.normalizedSourceType === 'iframeFull') {
        startIframeLoadTimer()
      }

      keepControlsVisible.value = true
      clearPlayerLoadingState()
      return
    }

    setPlayerLoadingState('connecting')
    keepControlsVisible.value = true
    videoElement.value.pause()
    videoElement.value.removeAttribute('src')
    videoElement.value.load()

    if (source.normalizedSourceType === 'mp4') {
      videoElement.value.src = source.sourceUrl
      syncPlaybackState()
      return
    }

    if (videoElement.value.canPlayType('application/vnd.apple.mpegurl')) {
      videoElement.value.src = source.sourceUrl
      syncPlaybackState()
      return
    }

    if (Hls.isSupported()) {
      const instance = new Hls(HLS_OPTIMIZED_BUFFER_CONFIG)
      hlsInstance.value = instance
      instance.loadSource(source.sourceUrl)
      instance.attachMedia(videoElement.value)
      instance.on(Hls.Events.MANIFEST_PARSED, () => {
        setPlayerLoadingState('loading_metadata')
        syncPlaybackState()
      })
      instance.on(Hls.Events.ERROR, (_, data) => {
        if (data.fatal) {
          handleSourceError()
        }
      })
      return
    }

    clearPlayerLoadingState()
    sourceErrorMessage.value = '当前浏览器无法播放该流媒体格式，请切换其他线路。'
  }

  /**
   * 切换视频播放与暂停状态，供自定义控件统一调用。
   */
  async function togglePlayback() {
    if (!videoElement.value || !isVideoSource.value) {
      debugPlayerAction('togglePlayback 被跳过', !videoElement.value ? 'videoElement 不存在' : '当前不是视频线路')
      return
    }

    debugPlayerAction('togglePlayback 已触发', videoElement.value.paused ? '准备播放' : '准备暂停')

    try {
      if (videoElement.value.paused) {
        setPlayerLoadingState('connecting')
        isPlaying.value = true
        keepControlsVisible.value = true
        await videoElement.value.play()
        syncPlaybackState()
        debugPlayerAction('togglePlayback 执行完成', 'play() 已调用')
      } else {
        videoElement.value.pause()
        /**
         * 暂停时立即把播放器视图状态切回“非播放中”，
         * 不再等待浏览器异步派发 pause 事件后才恢复玻璃控制层。
         */
        isPlaying.value = false
        keepControlsVisible.value = true
        clearPlayerLoadingState()
        syncPlaybackState()
        revealControls()
        debugPlayerAction('togglePlayback 执行完成', 'pause() 已调用')
      }
    } catch (error) {
      clearPlayerLoadingState()
      isPlaying.value = false
      keepControlsVisible.value = true
      syncPlaybackState()
      sourceErrorMessage.value = error instanceof Error ? error.message : '当前视频暂时无法播放，请稍后重试。'
      videoToast.add({
        title: '播放失败',
        description: error instanceof Error ? error.message : '当前视频暂时无法播放，请稍后重试。',
        tone: 'warning'
      })
      debugPlayerAction('togglePlayback 执行失败', error instanceof Error ? error.message : '未知错误')
    }
  }

  /**
   * 将播放进度向前或向后跳转指定秒数。
   */
  function seekBy(offsetSeconds: number) {
    if (!videoElement.value || !duration.value) {
      debugPlayerAction('seekBy 被跳过', !videoElement.value ? 'videoElement 不存在' : 'duration 为空')
      return
    }

    const nextTime = Math.min(duration.value, Math.max(0, videoElement.value.currentTime + offsetSeconds))
    debugPlayerAction('seekBy 已触发', `offset=${offsetSeconds}, from=${videoElement.value.currentTime.toFixed(2)}, to=${nextTime.toFixed(2)}`)
    setPlayerLoadingState('seeking', nextTime)
    videoElement.value.currentTime = nextTime
    currentTime.value = nextTime
  }

  /**
   * 按拖动条位置更新播放进度。
   */
  function updateSeekPosition(value: number | string) {
    if (!videoElement.value || !duration.value) {
      return
    }

    const nextTime = Number(value)
    setPlayerLoadingState('seeking', nextTime)
    videoElement.value.currentTime = nextTime
    currentTime.value = nextTime
  }

  /**
   * 更新播放器音量，并在拖到 0 时同步静音状态。
   */
  function updateVolume(value: number | string) {
    if (!videoElement.value) {
      return
    }

    const nextVolume = Number(value) / 100
    videoElement.value.volume = nextVolume
    videoElement.value.muted = nextVolume === 0
    syncPlaybackState()
  }

  /**
   * 切换静音状态，便于自定义音量按钮统一控制。
   */
  function toggleMute() {
    if (!videoElement.value) {
      return
    }

    videoElement.value.muted = !videoElement.value.muted
    syncPlaybackState()
  }

  /**
   * 切换浏览器全屏状态，优先使用标准接口，再回退到浏览器私有视频全屏。
   */
  async function toggleFullscreen() {
    if (!videoElement.value || !isVideoSource.value) {
      return
    }

    const video = videoElement.value as ExtendedVideoElement
    const playerContainer = video.closest('[data-player-shell]') as ExtendedFullscreenElement | null

    if (!playerContainer) {
      return
    }

    try {
      if (document.fullscreenElement) {
        await document.exitFullscreen()
        syncPlaybackState()
        return
      }

      if (playerContainer.requestFullscreen) {
        await playerContainer.requestFullscreen()
        syncPlaybackState()
        return
      }

      if (playerContainer.webkitRequestFullscreen) {
        await playerContainer.webkitRequestFullscreen()
        syncPlaybackState()
        return
      }

      if (playerContainer.webkitRequestFullScreen) {
        await playerContainer.webkitRequestFullScreen()
        syncPlaybackState()
        return
      }

      if (video.webkitEnterFullscreen) {
        video.webkitEnterFullscreen()
        syncPlaybackState()
        return
      }

      fullscreenUnavailable.value = true
      videoToast.add({
        title: '全屏不可用',
        description: '当前浏览器暂不支持该视频的全屏能力。',
        tone: 'warning'
      })
    } catch (error) {
      fullscreenUnavailable.value = true
      syncPlaybackState()
      videoToast.add({
        title: '全屏不可用',
        description: error instanceof Error ? error.message : '当前浏览器暂不支持该视频的全屏能力。',
        tone: 'warning'
      })
    }
  }

  /**
   * 切换画中画模式，优先使用标准接口，回退 Safari 私有展示模式。
   */
  async function togglePictureInPicture() {
    if (!videoElement.value || !isVideoSource.value) {
      return
    }

    const video = videoElement.value as ExtendedVideoElement

    try {
      if (document.pictureInPictureElement === video) {
        await document.exitPictureInPicture()
        syncPlaybackState()
        return
      }

      if (video.requestPictureInPicture) {
        await video.requestPictureInPicture()
        syncPlaybackState()
        return
      }

      if (video.webkitSetPresentationMode) {
        const nextMode = video.webkitPresentationMode === 'picture-in-picture' ? 'inline' : 'picture-in-picture'
        video.webkitSetPresentationMode(nextMode)
        syncPlaybackState()
        return
      }

      pictureInPictureUnavailable.value = true
      videoToast.add({
        title: '画中画不可用',
        description: isAppleMobileBrowser.value
          ? '当前苹果移动端浏览器没有开放稳定的网页画中画能力。'
          : '当前浏览器暂不支持该视频的画中画能力。',
        tone: 'warning'
      })
    } catch (error) {
      pictureInPictureUnavailable.value = true
      syncPlaybackState()
      videoToast.add({
        title: '画中画不可用',
        description: error instanceof Error ? error.message : '当前浏览器暂不支持该能力。',
        tone: 'warning'
      })
    }
  }

  /**
   * 根据交互状态决定是否自动隐藏控制层，减少控件遮挡画面。
   */
  function scheduleControlsAutoHide() {
    clearControlsHideTimer()

    if (!isVideoSource.value || !isPlaying.value) {
      isPointerVisible.value = true
      keepControlsVisible.value = true
      return
    }

    keepControlsVisible.value = false
    controlsHideTimer.value = setTimeout(() => {
      isPointerVisible.value = false
    }, 2200)
  }

  /**
   * 主动显示控制层，并重新安排自动隐藏时机。
   */
  function revealControls() {
    isPointerVisible.value = true
    keepControlsVisible.value = true
    scheduleControlsAutoHide()
  }

  /**
   * 主动隐藏控制层，通常用于桌面端鼠标离开播放器区域。
   */
  function hideControls() {
    clearControlsHideTimer()

    if (!isVideoSource.value) {
      return
    }

    if (keepControlsVisible.value || !isPlaying.value) {
      isPointerVisible.value = true
      return
    }

    isPointerVisible.value = false
  }

  /**
   * 处理 `loadedmetadata` 事件，确保元数据加载后立即同步状态。
   */
  function handleLoadedMetadata() {
    setPlayerLoadingState('loading_metadata')
    syncPlaybackState()
  }

  /**
   * 处理 `loadstart` 事件，提示当前媒体源开始加载。
   */
  function handleLoadStart() {
    setPlayerLoadingState('connecting')
  }

  /**
   * 处理 `canplay` 事件，表示当前视频已经具备开始播放的条件。
   */
  function handleCanPlay() {
    clearBufferingStateWithGuard()
    syncPlaybackState()
  }

  /**
   * 处理 iframe 播放页加载完成事件，结束慢加载提示。
   */
  function handleIframeLoad() {
    clearIframeLoadTimer()
    iframeLoadSlow.value = false
  }

  /**
   * 为当前 iframe 播放源构建可直接绑定到组件上的属性，
   * 让普通 iframe 与 iframeFull 使用统一渲染出口。
   */
  function buildIframeAttributes(source: Pick<VideoSourceItem, 'sourceType' | 'sourceUrl' | 'label'> | null | undefined) {
    return buildIframeRenderAttributes(source)
  }

  /**
   * 处理 `timeupdate` 事件，驱动时间文本和进度条更新。
   */
  function handleTimeUpdate() {
    if (playerLoadingState.value === 'buffering' || playerLoadingState.value === 'seeking') {
      clearBufferingStateWithGuard()
    }

    syncPlaybackState()
  }

  /**
   * 处理 `durationchange` 事件，避免切源后总时长显示不准确。
   */
  function handleDurationChange() {
    syncPlaybackState()
  }

  /**
   * 处理 `play` 事件，在真正开始播放后自动安排控件隐藏。
   */
  function handlePlay() {
    clearBufferingStateWithGuard()
    hasStartedPlayback.value = true
    syncPlaybackState()
    scheduleControlsAutoHide()
  }

  /**
   * 处理 `pause` 事件，在暂停时重新显示控件方便继续操作。
   */
  function handlePause() {
    /**
     * 某些浏览器在 pause 事件与状态回写之间存在时序差异，
     * 这里先强制回写暂停态，确保桌面端控制层立刻可见。
     */
    isPlaying.value = false

    if (playerLoadingState.value !== 'buffering' && playerLoadingState.value !== 'seeking') {
      clearPlayerLoadingState()
    }

    keepControlsVisible.value = true
    syncPlaybackState()
    revealControls()
  }

  /**
   * 处理 `volumechange` 事件，确保静音和音量 UI 与真实媒体同步。
   */
  function handleVolumeChange() {
    syncPlaybackState()
  }

  /**
   * 处理 `ended` 事件，在播放结束时重新展示控件。
   */
  function handleEnded() {
    clearBufferingStateTimer()
    clearPlayerLoadingState()
    keepControlsVisible.value = true
    syncPlaybackState()
    revealControls()
  }

  /**
   * 处理 `waiting` 事件，提示用户当前正在缓冲媒体数据。
   */
  function handleWaiting() {
    if (playerLoadingState.value !== 'seeking') {
      scheduleBufferingState()
    }
  }

  /**
   * 处理 `stalled` 事件，提示当前下载过程暂时停滞。
   */
  function handleStalled() {
    scheduleBufferingState()
  }

  /**
   * 处理 `seeking` 事件，提示用户播放器正在定位到目标时间点。
   */
  function handleSeeking() {
    if (!videoElement.value) {
      return
    }

    setPlayerLoadingState('seeking', videoElement.value.currentTime)
  }

  /**
   * 处理 `seeked` 事件，在定位完成后根据缓冲情况恢复状态。
   */
  function handleSeeked() {
    if (!videoElement.value) {
      clearPlayerLoadingState()
      return
    }

    if (videoElement.value.readyState >= 3) {
      clearBufferingStateWithGuard()
      return
    }

    scheduleBufferingState()
  }

  /**
   * 处理全屏状态变化，确保标准全屏与私有视频全屏都能及时回写到 UI。
   */
  function handleFullscreenChange() {
    syncPlaybackState()
  }

  /**
   * 处理进入画中画事件，便于按钮状态即时同步。
   */
  function handleEnterPictureInPicture() {
    syncPlaybackState()
  }

  /**
   * 处理离开画中画事件，避免按钮仍停留在激活态。
   */
  function handleLeavePictureInPicture() {
    syncPlaybackState()
  }

  /**
   * 处理 Safari 私有展示模式变化，让私有画中画与私有全屏状态同步到 UI。
   */
  function handleWebkitPresentationModeChange() {
    syncPlaybackState()
  }

  /**
   * 将视频 DOM 事件绑定到统一的状态同步方法上。
   */
  function bindVideoEvents() {
    if (!videoElement.value) {
      return
    }

    const video = videoElement.value as ExtendedVideoElement

    video.addEventListener('loadstart', handleLoadStart)
    video.addEventListener('loadedmetadata', handleLoadedMetadata)
    video.addEventListener('canplay', handleCanPlay)
    video.addEventListener('timeupdate', handleTimeUpdate)
    video.addEventListener('durationchange', handleDurationChange)
    video.addEventListener('play', handlePlay)
    video.addEventListener('pause', handlePause)
    video.addEventListener('waiting', handleWaiting)
    video.addEventListener('stalled', handleStalled)
    video.addEventListener('seeking', handleSeeking)
    video.addEventListener('seeked', handleSeeked)
    video.addEventListener('volumechange', handleVolumeChange)
    video.addEventListener('ended', handleEnded)
    video.addEventListener('webkitpresentationmodechanged', handleWebkitPresentationModeChange as EventListener)
  }

  /**
   * 解绑视频 DOM 事件，避免组件切换或销毁后残留监听器。
   */
  function unbindVideoEvents() {
    if (!videoElement.value) {
      return
    }

    const video = videoElement.value as ExtendedVideoElement

    video.removeEventListener('loadstart', handleLoadStart)
    video.removeEventListener('loadedmetadata', handleLoadedMetadata)
    video.removeEventListener('canplay', handleCanPlay)
    video.removeEventListener('timeupdate', handleTimeUpdate)
    video.removeEventListener('durationchange', handleDurationChange)
    video.removeEventListener('play', handlePlay)
    video.removeEventListener('pause', handlePause)
    video.removeEventListener('waiting', handleWaiting)
    video.removeEventListener('stalled', handleStalled)
    video.removeEventListener('seeking', handleSeeking)
    video.removeEventListener('seeked', handleSeeked)
    video.removeEventListener('volumechange', handleVolumeChange)
    video.removeEventListener('ended', handleEnded)
    video.removeEventListener('webkitpresentationmodechanged', handleWebkitPresentationModeChange as EventListener)
  }

  watch(
    sortedSources,
    () => {
      initializeActiveSource()
    },
    { immediate: true }
  )

  watch(activeSource, async () => {
    sourceErrorMessage.value = ''

    try {
      await setupPlayer()
    } catch (error) {
      clearPlayerLoadingState()
      sourceErrorMessage.value = error instanceof Error ? error.message : '当前线路初始化失败，请稍后重试。'
    } finally {
      isSwitching.value = false
      revealControls()
    }
  }, { immediate: true, flush: 'post' })

  /**
   * 当视频元素真正挂载完成后再次初始化播放器，避免首屏时因 ref 尚未就绪而跳过加载。
   */
  watch(videoElement, async (current, previous) => {
    if (previous) {
      unbindVideoEvents()
    }

    if (current) {
      bindVideoEvents()
      sourceErrorMessage.value = ''

      try {
        await setupPlayer()
      } catch (error) {
        clearPlayerLoadingState()
        sourceErrorMessage.value = error instanceof Error ? error.message : '当前线路初始化失败，请稍后重试。'
      } finally {
        isSwitching.value = false
        revealControls()
      }
    }
  })

  onMounted(() => {
    isClientMounted.value = true
    document.addEventListener('fullscreenchange', handleFullscreenChange)
    document.addEventListener('enterpictureinpicture', handleEnterPictureInPicture)
    document.addEventListener('leavepictureinpicture', handleLeavePictureInPicture)
  })

  onBeforeUnmount(() => {
    clearControlsHideTimer()
    clearIframeLoadTimer()
    clearBufferingStateTimer()
    unbindVideoEvents()
    destroyHlsInstance()
    document.removeEventListener('fullscreenchange', handleFullscreenChange)
    document.removeEventListener('enterpictureinpicture', handleEnterPictureInPicture)
    document.removeEventListener('leavepictureinpicture', handleLeavePictureInPicture)
  })

  return {
    videoElement,
    sourceErrorMessage,
    isSwitching,
    isLoading,
    shouldShowLoadingOverlay,
    iframeLoaded,
    iframeLoadingText,
    progressFeedbackText,
    loadingText,
    currentTime,
    duration,
    isPlaying,
    isFullscreen,
    isPictureInPicture,
    isPointerVisible,
    shouldShowControlsOverlay,
    shouldShowImmersivePoster,
    isMuted,
    hasStartedPlayback,
    progressPercent,
    volumePercent,
    activeSource,
    sortedSources,
    nextFallbackSource,
    playerHint,
    isVideoSource,
    canUseFullscreen,
    canUsePictureInPicture,
    getSourceTypeLabel,
    getIframeSandboxValue,
    buildIframeAttributes,
    normalizeSourceType,
    formatTime,
    copySourceUrl,
    switchSource,
    switchToNextFallback,
    handleSourceError,
    handleIframeLoad,
    togglePlayback,
    seekBy,
    updateSeekPosition,
    updateVolume,
    toggleMute,
    toggleFullscreen,
    togglePictureInPicture,
    revealControls,
    hideControls
  }
}
