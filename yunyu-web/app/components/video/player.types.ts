/**
 * 这个文件统一维护播放器组件会复用的视频源类型定义，
 * 避免手机端、桌面端和详情页各自声明导致字段漂移，
 * 同时沉淀播放器对外 props / emits 契约，方便跨项目复用。
 */
export interface VideoSourceItem {
  id: number
  label: string
  sourceType: 'mp4' | 'm3u8' | 'iframe' | 'iframeFull'
  sourceUrl: string
}

/**
 * 这个接口统一描述播放器对外暴露的属性，
 * 让调用方和播放器内部组件共享同一份字段定义。
 */
export interface VideoPlayerProps {
  sources: VideoSourceItem[]
  controls?: boolean
  activeSourceId?: number | null
  posterUrl?: string
  debug?: boolean
  immersivePoster?: boolean
}

/**
 * 这个接口统一描述播放器对外抛出的事件，
 * 目前保留激活线路同步这一条最核心的交互契约。
 */
export interface VideoPlayerEmits {
  'update:activeSourceId': [value: number | null]
}
