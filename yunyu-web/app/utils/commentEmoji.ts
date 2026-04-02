import { COMMENT_EMOJI_PACK, type CommentEmojiItem } from '../constants/commentEmoji'

/**
 * 评论富文本分段类型。
 * 作用：统一描述评论正文在渲染前拆分出的文本段和表情段。
 */
export type CommentContentSegment =
  | { type: 'text', value: string }
  | { type: 'emoji', value: CommentEmojiItem }

const COMMENT_EMOJI_MAP = new Map(COMMENT_EMOJI_PACK.map(item => [item.code, item]))
const COMMENT_EMOJI_PATTERN = new RegExp(
  COMMENT_EMOJI_PACK
    .map(item => item.code)
    .sort((left, right) => right.length - left.length)
    .map(code => code.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'))
    .join('|'),
  'g'
)

/**
 * 根据编码查找评论表情。
 * 作用：给评论输入和评论渲染提供统一的表情检索能力。
 *
 * @param code 表情编码
 * @returns 对应表情项
 */
export function findCommentEmojiByCode(code: string) {
  return COMMENT_EMOJI_MAP.get(code)
}

/**
 * 解析评论正文中的自定义表情。
 * 作用：将评论文本拆成普通文本与表情资源片段，便于模板安全渲染。
 *
 * @param content 评论正文
 * @returns 解析后的片段列表
 */
export function parseCommentRichContent(content: string | null | undefined): CommentContentSegment[] {
  const normalizedContent = String(content || '')

  if (!normalizedContent) {
    return []
  }

  const segments: CommentContentSegment[] = []
  let cursor = 0

  for (const match of normalizedContent.matchAll(COMMENT_EMOJI_PATTERN)) {
    const matchedCode = match[0]
    const startIndex = match.index ?? 0

    if (startIndex > cursor) {
      segments.push({
        type: 'text',
        value: normalizedContent.slice(cursor, startIndex)
      })
    }

    const emoji = findCommentEmojiByCode(matchedCode)

    if (emoji) {
      segments.push({
        type: 'emoji',
        value: emoji
      })
    } else {
      segments.push({
        type: 'text',
        value: matchedCode
      })
    }

    cursor = startIndex + matchedCode.length
  }

  if (cursor < normalizedContent.length) {
    segments.push({
      type: 'text',
      value: normalizedContent.slice(cursor)
    })
  }

  return segments
}

