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
  },
  {
    id: 'wink',
    name: '眨眼',
    code: '[眨眼]',
    src: '/emojis/comments/wink.svg'
  },
  {
    id: 'angry',
    name: '生气',
    code: '[生气]',
    src: '/emojis/comments/angry.svg'
  },
  {
    id: 'tears',
    name: '流泪',
    code: '[流泪]',
    src: '/emojis/comments/tears.svg'
  },
  {
    id: 'blank',
    name: '发呆',
    code: '[发呆]',
    src: '/emojis/comments/blank.svg'
  },
  {
    id: 'sly',
    name: '偷笑',
    code: '[偷笑]',
    src: '/emojis/comments/sly.svg'
  },
  {
    id: 'cheer',
    name: '点赞',
    code: '[点赞]',
    src: '/emojis/comments/cheer.svg'
  },
  {
    id: 'pray',
    name: '拜托',
    code: '[拜托]',
    src: '/emojis/comments/pray.svg'
  },
  {
    id: 'dizzy',
    name: '晕乎',
    code: '[晕乎]',
    src: '/emojis/comments/dizzy.svg'
  }
]
