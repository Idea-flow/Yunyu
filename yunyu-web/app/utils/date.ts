/**
 * 格式化前台日期。
 * 作用：把后端返回的日期字符串统一转换为中文用户更容易理解的年月日格式，
 * 供首页、列表页和文章详情页等前台展示场景复用。
 *
 * @param value 原始日期字符串
 * @returns 中文日期字符串
 */
export function formatChineseDate(value: string) {
  if (!value) {
    return ''
  }

  const normalizedValue = /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value
  const date = new Date(normalizedValue)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(date)
}

/**
 * 格式化前后台日期时间。
 * 作用：把字符串时间、时间戳或 Date 对象统一转换为中文日期时间格式，
 * 供评论区、后台列表和系统信息等需要展示精确时间的场景复用。
 *
 * @param value 原始时间值
 * @param fallback 空值时的兜底文案
 * @returns 中文日期时间字符串
 */
export function formatChineseDateTime(value: string | number | Date | null | undefined, fallback = '') {
  if (value === null || value === undefined || value === '') {
    return fallback
  }

  const date = value instanceof Date ? value : new Date(value)

  if (Number.isNaN(date.getTime())) {
    return typeof value === 'string' ? value : fallback
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(date)
}
