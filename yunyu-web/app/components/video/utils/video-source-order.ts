/**
 * 这个工具文件负责统一定义前后台共用的播放线路优先级，
 * 保证详情接口返回顺序、默认选中线路和播放器按钮展示顺序保持一致。
 */

const VIDEO_SOURCE_PRIORITY_PREFIXES = ['VF', 'RM', 'UM', 'RF', 'UF'] as const

/**
 * 规范化线路名称，便于兼容空格、大小写和轻微命名差异。
 */
function normalizeVideoSourceLabel(label: string | null | undefined) {
  return (label || '')
    .trim()
    .toUpperCase()
    .replace(/\s+/g, '')
}

/**
 * 计算单条线路的排序权重，权重越小优先级越高。
 */
export function getVideoSourcePriority(label: string | null | undefined) {
  const normalizedLabel = normalizeVideoSourceLabel(label)
  const matchedIndex = VIDEO_SOURCE_PRIORITY_PREFIXES.findIndex((prefix) => normalizedLabel.startsWith(prefix))

  return matchedIndex >= 0 ? matchedIndex : VIDEO_SOURCE_PRIORITY_PREFIXES.length
}

/**
 * 按约定线路优先级排序。
 * 默认优先级依次为 VF、RM、UM、RF、UF，其余线路保持原始相对顺序排在后面。
 */
export function sortVideoSourcesByPriority<T extends { label: string | null | undefined }>(sources: T[]) {
  return sources
    .map((source, index) => ({
      source,
      index,
      priority: getVideoSourcePriority(source.label)
    }))
    .sort((left, right) => {
      if (left.priority !== right.priority) {
        return left.priority - right.priority
      }

      return left.index - right.index
    })
    .map((item) => item.source)
}
