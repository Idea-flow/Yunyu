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
 * 评论自定义表情分组类型。
 * 作用：统一描述评论表情包的分组结构，便于输入面板支持多组表情切换。
 */
export interface CommentEmojiGroup {
  id: string
  name: string
  items: CommentEmojiItem[]
}

/**
 * 评论自定义表情分组配置。
 * 作用：集中维护评论输入框可选表情及其分组关系，避免前后台各自维护一套资源清单。
 */
export const COMMENT_EMOJI_GROUPS: CommentEmojiGroup[] = [
  {
    id: 'classic',
    name: '经典',
    items: [
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
  },
  {
    id: 'sticker',
    name: '贴纸',
    items: [
      {
        id: 'question',
        name: '疑问',
        code: '[疑问]',
        src: '/emojis/comments/question.svg'
      },
      {
        id: 'speechless',
        name: '无语',
        code: '[无语]',
        src: '/emojis/comments/speechless.svg'
      },
      {
        id: 'call',
        name: '打call',
        code: '[打call]',
        src: '/emojis/comments/call.svg'
      },
      {
        id: 'watch',
        name: '围观',
        code: '[围观]',
        src: '/emojis/comments/watch.svg'
      },
      {
        id: 'got-it',
        name: '收到',
        code: '[收到]',
        src: '/emojis/comments/got-it.svg'
      },
      {
        id: 'idea',
        name: '灵感',
        code: '[灵感]',
        src: '/emojis/comments/idea.svg'
      },
      {
        id: 'go-go',
        name: '加油',
        code: '[加油]',
        src: '/emojis/comments/go-go.svg'
      },
      {
        id: 'good-night',
        name: '晚安',
        code: '[晚安]',
        src: '/emojis/comments/good-night.svg'
      }
    ]
  }
]

/**
 * 评论自定义表情平铺列表。
 * 作用：给评论解析和编码匹配提供统一的扁平数据来源，避免分组结构影响渲染逻辑。
 */
export const COMMENT_EMOJI_PACK: CommentEmojiItem[] = COMMENT_EMOJI_GROUPS.flatMap(group => group.items)
