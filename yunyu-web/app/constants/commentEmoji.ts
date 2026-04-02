/**
 * 评论自定义表情项类型。
 * 作用：统一描述评论表情包中的编码、名称与静态资源地址，供选择器和渲染器复用。
 */
export interface CommentEmojiItem {
  id: string
  name: string
  code: string
  src: string
}

/**
 * 评论自定义表情包配置。
 * 作用：集中维护评论输入框可选表情，避免前后台各自维护一套资源清单。
 */
export const COMMENT_EMOJI_PACK: CommentEmojiItem[] = [
  {
    id: 'smile',
    name: '微笑',
    code: '[微笑]',
    src: '/emojis/comments/smile.svg'
  },
  {
    id: 'laugh',
    name: '大笑',
    code: '[大笑]',
    src: '/emojis/comments/laugh.svg'
  },
  {
    id: 'shy',
    name: '害羞',
    code: '[害羞]',
    src: '/emojis/comments/shy.svg'
  },
  {
    id: 'wow',
    name: '哇哦',
    code: '[哇哦]',
    src: '/emojis/comments/wow.svg'
  },
  {
    id: 'clap',
    name: '鼓掌',
    code: '[鼓掌]',
    src: '/emojis/comments/clap.svg'
  },
  {
    id: 'heart',
    name: '比心',
    code: '[比心]',
    src: '/emojis/comments/heart.svg'
  }
]

