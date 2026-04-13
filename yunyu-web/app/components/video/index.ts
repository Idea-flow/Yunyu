/**
 * 这个统一导出文件负责为播放器目录提供可选的单入口导出，
 * 方便其他项目在需要时按模块方式引入组件、类型与工具。
 */
export { default as VideoPlayer } from './VideoPlayer.vue'
export { default as VideoToastViewport } from './VideoToastViewport.vue'
export { default as VideoButton } from './base/VideoButton.vue'
export { default as VideoBadge } from './base/VideoBadge.vue'
export { default as VideoIcon } from './icons/VideoIcon.vue'
export { videoIconMap, type VideoIconName } from './icons/icon-map'
export type { VideoPlayerEmits, VideoPlayerProps, VideoSourceItem } from './player.types'
export { useVideoPlayerController } from './composables/useVideoPlayerController'
export { provideVideoToast, useVideoToast } from './composables/useVideoToast'
export * from './utils/video-iframe'
export * from './utils/video-source-order'
