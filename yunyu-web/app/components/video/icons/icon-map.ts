import checkSvg from './check.svg?raw'
import chevronRightSvg from './chevron-right.svg?raw'
import closeSvg from './close.svg?raw'
import copySvg from './copy.svg?raw'
import expandSvg from './expand.svg?raw'
import listVideoSvg from './list-video.svg?raw'
import loaderSvg from './loader.svg?raw'
import minimizeSvg from './minimize.svg?raw'
import pictureInPictureSvg from './picture-in-picture.svg?raw'
import playSvg from './play.svg?raw'
import rotateCcwSvg from './rotate-ccw.svg?raw'
import rotateCwSvg from './rotate-cw.svg?raw'
import skipForwardSvg from './skip-forward.svg?raw'
import volumeOffSvg from './volume-off.svg?raw'
import volumeSvg from './volume.svg?raw'

/**
 * 这个映射文件负责统一维护播放器本地图标资源与名称的对应关系，
 * 让图标调用层只关注语义名称，不关心具体 SVG 文件路径。
 */
export const videoIconMap = {
  copy: copySvg,
  expand: expandSvg,
  minimize: minimizeSvg,
  'picture-in-picture': pictureInPictureSvg,
  play: playSvg,
  'rotate-ccw': rotateCcwSvg,
  'rotate-cw': rotateCwSvg,
  'skip-forward': skipForwardSvg,
  volume: volumeSvg,
  'volume-off': volumeOffSvg,
  close: closeSvg,
  check: checkSvg,
  'chevron-right': chevronRightSvg,
  'list-video': listVideoSvg,
  loader: loaderSvg
} as const

export type VideoIconName = keyof typeof videoIconMap
