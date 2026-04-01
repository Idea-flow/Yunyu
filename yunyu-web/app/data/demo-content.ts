import type { ArticleTocItem } from '../types/post'

/**
 * 前台演示数据类型定义与静态内容集。
 * 作用：在前台公开接口尚未补齐前，先为首页与文章详情页提供一套可稳定复用的演示数据。
 */
export interface DemoSiteInfo {
  name: string
  subTitle: string
  description: string
  heroImageUrl: string
}

/**
 * 前台统计卡片类型。
 * 作用：统一描述首页头图区的内容指标展示结构。
 */
export interface DemoSiteStat {
  label: string
  value: string
  hint: string
}

/**
 * 前台作者信息类型。
 * 作用：统一描述演示文章作者的头像、名称与角色信息。
 */
export interface DemoAuthor {
  name: string
  role: string
  avatarUrl: string
}

/**
 * 前台分类类型。
 * 作用：为首页分类入口区提供统一的数据结构。
 */
export interface DemoCategory {
  id: number
  name: string
  slug: string
  description: string
  coverUrl: string
  articleCount: number
}

/**
 * 前台专题类型。
 * 作用：为首页专题模块提供统一的名称、摘要和视觉信息。
 */
export interface DemoTopic {
  id: number
  name: string
  slug: string
  summary: string
  coverUrl: string
  articleCount: number
}

/**
 * 前台文章类型。
 * 作用：统一描述首页卡片和详情页正文所需的文章字段。
 */
export interface DemoPost {
  id: number
  slug: string
  title: string
  summary: string
  coverUrl: string
  categoryName: string
  categorySlug: string
  tagNames: string[]
  topicNames: string[]
  publishedAt: string
  readingMinutes: number
  viewCount: number
  likeCount: number
  featured?: boolean
  author: DemoAuthor
  contentMarkdown: string
}

/**
 * 站点基础信息。
 * 作用：提供首页头部与 SEO 场景的品牌文案和主视觉资源。
 */
export const demoSiteInfo: DemoSiteInfo = {
  name: '云屿',
  subTitle: '在二次元场景与情绪里漫游的内容站',
  description: '聚焦新番观察、角色成长、场景美学与专题推荐，把内容整理成更适合沉浸阅读的形式。',
  heroImageUrl: 'https://image.pollinations.ai/prompt/anime%20homepage%20hero%20cinematic%20sky%20island?width=1600&height=1100&seed=9001&nologo=true'
}

/**
 * 首页统计数据。
 * 作用：展示当前演示站点的内容规模和阅读氛围。
 */
export const demoSiteStats: DemoSiteStat[] = [
  { label: '已整理文章', value: '36', hint: '围绕新番、场景和角色持续更新' },
  { label: '精选专题', value: '08', hint: '按情绪、风格与观看路线归档' },
  { label: '本周推荐', value: '12', hint: '适合先收藏后慢慢阅读' }
]

const demoAuthors: DemoAuthor[] = [
  {
    name: '星野澄',
    role: '主编',
    avatarUrl: 'https://image.pollinations.ai/prompt/anime%20girl%20portrait%20clean%20soft%20light?width=512&height=512&seed=3201&nologo=true'
  },
  {
    name: '雾岛栞',
    role: '内容编辑',
    avatarUrl: 'https://image.pollinations.ai/prompt/anime%20editor%20portrait%20blue%20hair%20illustration?width=512&height=512&seed=3202&nologo=true'
  },
  {
    name: '朝雾未央',
    role: '氛围观察员',
    avatarUrl: 'https://image.pollinations.ai/prompt/anime%20girl%20portrait%20warm%20sunset%20illustration?width=512&height=512&seed=3203&nologo=true'
  }
]

/**
 * 首页分类数据。
 * 作用：承接首页分类导航和后续分类页扩展。
 */
export const demoCategories: DemoCategory[] = [
  {
    id: 1,
    name: '新番观察',
    slug: 'season-watch',
    description: '追踪季度新番、口碑变化与值得补完的作品。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20spring%20season%20illustration%20sky?width=1200&height=675&seed=1101&nologo=true',
    articleCount: 9
  },
  {
    id: 2,
    name: '场景美学',
    slug: 'scene-aesthetics',
    description: '城市夜景、海边列车、教室和黄昏天台的情绪切片。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20city%20night%20aesthetic%20illustration?width=1200&height=675&seed=1102&nologo=true',
    articleCount: 7
  },
  {
    id: 3,
    name: '角色档案',
    slug: 'character-file',
    description: '更关注角色如何一步步成为自己，而不是只看设定标签。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20character%20design%20sheet%20illustration?width=1200&height=675&seed=1103&nologo=true',
    articleCount: 6
  },
  {
    id: 4,
    name: '音乐与配乐',
    slug: 'music-score',
    description: '收集那些适合夜里反复循环的片头、片尾和氛围曲。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20music%20studio%20illustration%20soft%20light?width=1200&height=675&seed=1104&nologo=true',
    articleCount: 5
  }
]

/**
 * 首页专题数据。
 * 作用：提供前台专题模块与后续专题列表页的演示基础。
 */
export const demoTopics: DemoTopic[] = [
  {
    id: 1,
    name: '2026 春季追番清单',
    slug: 'spring-2026-watchlist',
    summary: '用一页内容看完春季新番里最值得先追的作品与观看理由。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20spring%20watchlist%20cover%20illustration?width=1200&height=675&seed=2101&nologo=true',
    articleCount: 5
  },
  {
    id: 2,
    name: '城市夜色美学',
    slug: 'city-night-aesthetics',
    summary: '把最有氛围感的电车窗景、雨夜路口和天台夜风都收进同一个专题。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20night%20city%20aesthetic%20cover?width=1200&height=675&seed=2102&nologo=true',
    articleCount: 4
  },
  {
    id: 3,
    name: '入坑路线图',
    slug: 'starter-roadmap',
    summary: '给第一次接触云屿的新读者准备的阅读路线与分类导航。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20starter%20guide%20map%20illustration?width=1200&height=675&seed=2103&nologo=true',
    articleCount: 3
  }
]

/**
 * 前台文章演示数据。
 * 作用：同时服务首页卡片、详情页正文和后续公开接口联调前的内容展示。
 */
export const demoPosts: DemoPost[] = [
  {
    id: 1,
    slug: 'spring-2026-first-five',
    title: '2026 春季新番先看哪 5 部：从情绪密度到作画稳定度的一次筛选',
    summary: '如果你只想先追 5 部作品，这篇会按情绪浓度、画面完成度和追更压力给出最稳妥的入场顺序。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20spring%20festival%20key%20visual?width=1600&height=900&seed=4101&nologo=true',
    categoryName: '新番观察',
    categorySlug: 'season-watch',
    tagNames: ['治愈系', '奇幻冒险', '新手入门'],
    topicNames: ['2026 春季追番清单', '入坑路线图'],
    publishedAt: '2026-03-27',
    readingMinutes: 5,
    viewCount: 4821,
    likeCount: 326,
    featured: true,
    author: demoAuthors[0],
    contentMarkdown: `# 2026 春季新番先看哪 5 部

如果你本季时间不多，这篇会先给出一个**成本最低、情绪回报最高**的追番顺序。

## 这次筛选只看三个维度

- 情绪密度是否稳定
- 作画完成度是否足够
- 连载节奏会不会造成追更压力

## 我建议的观看顺序

### 1. 先看“最稳”的那一部

这类作品通常第一集就能明确风格，后续波动也相对更小，适合先建立追更惯性。

### 2. 再补“情绪更重”的作品

如果你偏爱细腻关系线，可以从第二梯队开始补。它们的回报很高，但会更吃情绪投入。

### 3. 最后再决定要不要追实验风格作品

有些片子讨论度很高，但节奏和表达不一定适合所有人，放在最后更稳妥。

## 适合什么样的观众

> 如果你没有太多时间，先追完成度更稳的作品，会更容易把一整个季度看下来。`
  },
  {
    id: 2,
    slug: 'rainy-city-train-scenes',
    title: '雨夜电车窗景为什么总能打动人：12 个高情绪城市镜头拆解',
    summary: '从霓虹倒影、玻璃反光和人物留白三个角度，拆解动画中最容易让人沉浸的城市夜景镜头。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20rainy%20city%20train%20window%20illustration?width=1600&height=900&seed=4102&nologo=true',
    categoryName: '场景美学',
    categorySlug: 'scene-aesthetics',
    tagNames: ['都市夜景', '壁纸向'],
    topicNames: ['城市夜色美学'],
    publishedAt: '2026-03-26',
    readingMinutes: 4,
    viewCount: 3680,
    likeCount: 281,
    author: demoAuthors[1],
    contentMarkdown: `# 雨夜电车窗景为什么总能打动人

真正让人沉浸的，并不是雨本身，而是玻璃反光、城市灯牌和人物留白一起成立。

## 氛围的关键不是“下雨”

下雨只是媒介，真正建立情绪的是镜头里的层次：

- 窗外的流动灯光
- 玻璃上的倒影
- 人物没有被解释清楚的情绪

## 三个最有力量的镜头

### 霓虹倒影

城市像漂浮在角色脸侧，观众会同时感受到外部世界和内部情绪。

### 电车报站声

哪怕没有台词，只要报站声和车厢晃动存在，孤独感就成立。

### 留白的视线

镜头不需要告诉观众角色在想什么，只要给她一段看向窗外的安静时间。`
  },
  {
    id: 3,
    slug: 'character-growth-arc',
    title: '从“温柔而坚定”到“终于开口”：本季最完整的角色成长线',
    summary: '这篇围绕一条完整成长线，记录角色从沉默到表达、从观望到行动的关键节点。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20character%20growth%20storyboard%20illustration?width=1600&height=900&seed=4103&nologo=true',
    categoryName: '角色档案',
    categorySlug: 'character-file',
    tagNames: ['角色成长', '校园'],
    topicNames: ['2026 春季追番清单'],
    publishedAt: '2026-03-24',
    readingMinutes: 4,
    viewCount: 2950,
    likeCount: 214,
    author: demoAuthors[1],
    contentMarkdown: `# 本季最完整的角色成长线

真正动人的角色变化，往往来自很多个微小决定叠加起来。

## 成长不是突然爆发

角色真正变强的时刻，通常发生在观众差一点忽略的细节里：

1. 第一次明确拒绝
2. 第一次主动表达
3. 第一次为别人做决定

## 为什么这条线成立

因为它没有把成长写成“突然觉醒”，而是写成一个人终于愿意面对自己。`
  },
  {
    id: 4,
    slug: 'midnight-anime-soundtracks',
    title: '一张配乐单就够了：适合深夜循环播放的 8 首动画氛围曲',
    summary: '当你只想在夜里开着台灯慢慢听，这 8 首动画配乐能把整个房间都变得柔软下来。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20music%20room%20night%20illustration?width=1600&height=900&seed=4104&nologo=true',
    categoryName: '音乐与配乐',
    categorySlug: 'music-score',
    tagNames: ['配乐精选', '治愈系'],
    topicNames: [],
    publishedAt: '2026-03-22',
    readingMinutes: 3,
    viewCount: 2234,
    likeCount: 168,
    author: demoAuthors[0],
    contentMarkdown: `# 深夜循环播放的 8 首动画氛围曲

当信息量变少以后，旋律里那些细小起伏会更容易被感知到。

## 为什么夜里更适合听配乐

夜里会放大音色细节，也更适合慢节奏的旋律推进。

## 推荐的收听顺序

- 先听片尾曲
- 再听钢琴主题
- 最后补弦乐主旋律`
  },
  {
    id: 5,
    slug: 'how-to-use-yunyu',
    title: '第一次来云屿应该怎么逛：分类、专题、标签三条主线一次看懂',
    summary: '如果你是第一次进入这个站点，这篇会告诉你最省心的阅读路径与内容组织方式。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20guide%20board%20illustration%20library?width=1600&height=900&seed=4105&nologo=true',
    categoryName: '入坑指南',
    categorySlug: 'starter-guide',
    tagNames: ['新手入门', '治愈系'],
    topicNames: ['入坑路线图'],
    publishedAt: '2026-03-21',
    readingMinutes: 4,
    viewCount: 1890,
    likeCount: 141,
    author: demoAuthors[0],
    contentMarkdown: `# 第一次来云屿应该怎么逛

如果你是第一次进入这个站点，这篇会告诉你最省心的阅读路径。

## 三条最省心的入口

### 看分类

适合先按兴趣看内容方向。

### 看专题

适合一次性阅读同主题内容。

### 看标签

适合快速找到某种氛围或某类镜头。

## 什么时候用哪一种入口

| 入口 | 适合场景 | 阅读感受 |
| --- | --- | --- |
| 分类 | 想先看大方向 | 稳定、有秩序 |
| 专题 | 想连续读一组内容 | 沉浸、连贯 |
| 标签 | 想找某种氛围 | 快速、轻量 |

## 如果后续要接真实接口

\`\`\`ts
const query = {
  categorySlug: 'season-watch',
  topicSlug: 'starter-roadmap',
  tags: ['healing', 'beginner']
}
\`\`\`

这只是示意结构，真正接接口时会走服务端的公开查询参数。`
  },
  {
    id: 6,
    slug: 'nine-anime-wallpaper-scenes',
    title: '海边列车、黄昏天台和放学路口：适合做壁纸的 9 个动画场景',
    summary: '这一篇不做剧情分析，只挑那些一眼就想保存下来的场景镜头。',
    coverUrl: 'https://image.pollinations.ai/prompt/anime%20seaside%20train%20sunset%20illustration?width=1600&height=900&seed=4106&nologo=true',
    categoryName: '场景美学',
    categorySlug: 'scene-aesthetics',
    tagNames: ['壁纸向', '都市夜景'],
    topicNames: ['城市夜色美学'],
    publishedAt: '2026-03-20',
    readingMinutes: 3,
    viewCount: 4110,
    likeCount: 356,
    author: demoAuthors[2],
    contentMarkdown: `# 适合做壁纸的 9 个动画场景

这一篇不做剧情分析，只挑那些一眼就想保存下来的场景镜头。

## 选择标准

- 构图完整
- 色彩有记忆点
- 单张截图也能成立

## 最推荐的三个关键词

1. 海边列车
2. 黄昏天台
3. 放学路口`
  }
]

/**
 * 获取首页主打文章。
 * 作用：统一选出首页 Hero 区的重点内容，避免页面层反复写筛选逻辑。
 */
export function getFeaturedDemoPost() {
  return demoPosts.find(post => post.featured) || demoPosts[0]
}

/**
 * 根据 slug 获取演示文章。
 * 作用：供前台文章详情页查找当前要展示的文章内容。
 *
 * @param slug 文章 slug
 * @returns 演示文章或空值
 */
export function getDemoPostBySlug(slug: string) {
  return demoPosts.find(post => post.slug === slug) || null
}

/**
 * 获取与当前文章相关的推荐文章。
 * 作用：为详情页底部推荐区提供简单稳定的演示数据。
 *
 * @param slug 当前文章 slug
 * @returns 推荐文章列表
 */
export function getRelatedDemoPosts(slug: string) {
  return demoPosts.filter(post => post.slug !== slug).slice(0, 3)
}

/**
 * 从目录项中选出首个标题。
 * 作用：为详情页目录初始激活状态提供一个稳定默认值。
 *
 * @param items 目录项列表
 * @returns 首个目录 ID
 */
export function getFirstTocId(items: ArticleTocItem[]) {
  return items[0]?.id || ''
}
