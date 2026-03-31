# 文章编辑与 Markdown 渲染方案

## 文档目标

本文档用于明确 `Yunyu` 后台文章新增/编辑页面在前端侧如何处理 `Markdown` 编辑、`HTML` 预览、目录生成与提交数据结构。

本文档重点解决以下问题：

- 后台文章编辑页是否需要前端把 `Markdown` 渲染成 `HTML`
- 前端使用什么技术把 `Markdown` 渲染成 `HTML`
- 目录 `TOC` 是否在前端生成
- 提交文章时前端向后端传哪些字段
- 如何与当前已有数据库设计和内容渲染策略保持一致

## 一、已有约束

当前项目已有文档已经明确以下事实：

- `Markdown` 是文章主内容源
- `HTML` 是派生结果，不能作为唯一真相
- `content_toc_json` 需要存储目录数据
- `post_content` 表已经支持 `content_markdown`、`content_html`、`content_toc_json`

相关文档：

- [docs/技术/01-内容模型与渲染策略.md](/Users/wangpenglong/projects/full-stack-project/Yunyu/docs/技术/01-内容模型与渲染策略.md)
- [docs/技术/03-数据库设计.md](/Users/wangpenglong/projects/full-stack-project/Yunyu/docs/技术/03-数据库设计.md)
- [docs/架构/01-总体技术架构.md](/Users/wangpenglong/projects/full-stack-project/Yunyu/docs/架构/01-总体技术架构.md)

因此本方案不是重新定义内容真相，而是补充：

- 后台编辑阶段前端如何提升编辑体验
- 前端如何把编辑过程中的派生结果传给后端

## 二、核心结论

推荐采用以下方案：

- 前端编辑器仍然以 `Markdown` 作为唯一输入源
- 前端在编辑页本地实时把 `Markdown` 渲染为 `HTML` 预览
- 前端本地实时提取标题结构，生成 `TOC`
- 前端提交文章时，同时提交 `contentMarkdown`、`contentHtml`、`contentToc`
- 后端保存 `Markdown` 作为主内容源，`HTML / TOC` 作为派生字段
- 后端后续应具备对 `HTML / TOC` 的重建或校验能力

换句话说：

- 编辑体验依赖前端实时渲染
- 长期内容可信源依赖后端存储的 `Markdown`

## 三、为什么前端需要本地渲染 HTML

后台文章编辑页如果只保存 `Markdown` 文本，不做前端本地渲染，会有这些问题：

- 作者无法在编辑时即时看到最终排版效果
- 无法实时预览标题层级、引用、列表、代码块、图片等展示结果
- 无法即时生成目录并检查结构是否合理
- 无法在提交前判断正文层级、阅读节奏和篇幅信息

因此对于后台编辑页，前端本地渲染 `HTML` 是必要的。

但这里要注意：

- 前端本地渲染是为了编辑预览
- 不是为了取代后端内容派生链路

## 四、前端推荐技术选型

### 4.1 推荐主技术

前端正式确认使用：

- `markdown-it`
- `shiki`

原因：

- 当前项目架构文档已经把 `markdown-it` 作为推荐 Markdown 解析器
- `markdown-it` 可以在浏览器和 Node.js 中运行
- 生态成熟，适合后台编辑器预览
- 与当前项目已有技术决策保持一致，避免前后端使用两套完全不同的 Markdown 规则
- `shiki` 更适合技术内容平台，代码块高亮质量更高
- `shiki` 的高亮效果更接近 VS Code，适合作为文章编辑预览与前台详情展示的统一方向

### 4.2 推荐配套能力

在前端编辑预览场景中，建议围绕 `markdown-it` 增加以下配套能力：

- 标题锚点插件：用于给标题生成稳定 `id`
- 代码高亮插件：用于代码块预览
- `HTML` 清洗能力：用于限制危险标签与属性
- 目录提取逻辑：根据标题节点生成 `TOC`

当前阶段正式采用：

- Markdown 解析：`markdown-it`
- 标题锚点：`markdown-it-anchor`
- 代码高亮：`shiki`

当前阶段暂不接入：

- `sanitize-html` 或等价白名单清洗方案

说明：

- 当前阶段先优先完成编辑预览、目录生成和内容提交闭环
- `HTML` 白名单清洗后续再补到正式发布链路或服务端校验链路中

### 4.3 为什么当前选择 `shiki` 而不是 `highlight.js`

当前项目最终选择 `shiki`，不选择 `highlight.js`，原因如下：

- `shiki` 的代码高亮质量更高，更适合技术文章与内容平台
- `shiki` 主题体系更成熟，后续更容易与前台详情页保持统一
- 后台文章编辑页本身就是内容工作台，预览质量比“先快速有个能用版本”更重要

`highlight.js` 的优势主要是接入更快、更轻，但对于本项目当前方向，优先级低于代码块展示质量。

### 4.4 为什么前端不推荐直接手写正则转换

不推荐自己用正则把 `Markdown` 转 `HTML`，原因包括：

- 很难正确处理嵌套列表、引用、代码块
- 标题、链接、图片等边界情况多
- 后续扩展任务列表、表格、脚注等语法会越来越难维护
- 安全处理容易遗漏

因此前端必须使用成熟解析器，不手写 Markdown 转换逻辑。

## 五、前端渲染职责划分

### 5.1 编辑页前端负责

前端在文章编辑页负责：

- 维护 `contentMarkdown`
- 本地实时渲染 `contentHtml`
- 本地生成 `contentToc`
- 统计正文字符数
- 估算阅读时长
- 提供编辑 / 预览切换

### 5.2 前端不负责

前端不负责以下事情：

- 把前端生成的 `HTML` 视为唯一可信源
- 决定最终线上详情页渲染真相
- 省略 `Markdown` 只提交 `HTML`
- 用客户端首屏渲染取代正式详情页的服务端渲染方案

## 六、推荐前端模块设计

### 6.1 `useMarkdownRenderer`

建议新增一个前端渲染 composable，统一负责：

- 输入：`markdown`
- 输出：`html`
- 输出：`toc`
- 输出：`plainText`
- 输出：`readingMinutes`

建议接口形态：

```ts
interface ArticleTocItem {
  id: string
  text: string
  level: number
}

interface MarkdownRenderResult {
  html: string
  toc: ArticleTocItem[]
  plainText: string
  readingMinutes: number
}
```

### 6.2 `MarkdownPreview`

建议新增一个公共预览组件：

- 输入：`markdown`
- 内部调用 `useMarkdownRenderer`
- 输出安全 `HTML`
- 用于后台文章编辑页预览区

### 6.3 `ArticleTocPanel`

建议新增一个目录组件：

- 输入：`toc`
- 展示标题层级结构
- 支持点击跳转到预览区对应锚点

### 6.4 `AdminMarkdownEditor`

建议后续把正文区进一步抽成专用组件：

- 左侧编辑
- 右侧预览
- 顶部切换编辑 / 预览 / 分屏
- 底部或侧栏展示字数、时长、目录摘要

## 七、文章编辑页推荐交互流

编辑页正文区建议采用以下流程：

1. 作者输入 `Markdown`
2. 前端监听 `contentMarkdown`
3. 使用 `markdown-it` 渲染为 `HTML`
4. 同时扫描标题节点，生成 `TOC`
5. 将 `HTML` 渲染到预览区
6. 将 `TOC` 渲染到目录面板
7. 提交时将 `Markdown + HTML + TOC` 一并发送给后端

## 八、提交数据结构建议

前端提交文章时，正文相关字段建议至少包含：

- `contentMarkdown`
- `contentHtml`
- `contentToc`

可选补充字段：

- `contentPlainText`
- `readingMinutes`

推荐请求载荷示意：

```json
{
  "title": "文章标题",
  "slug": "article-slug",
  "summary": "文章摘要",
  "categoryId": 1,
  "tagIds": [1, 2],
  "topicIds": [3],
  "status": "DRAFT",
  "seoTitle": "SEO 标题",
  "seoDescription": "SEO 描述",
  "contentMarkdown": "# 标题\\n\\n正文",
  "contentHtml": "<h1 id=\"标题\">标题</h1><p>正文</p>",
  "contentToc": [
    {
      "id": "标题",
      "text": "标题",
      "level": 1
    }
  ]
}
```

## 九、与后端的职责边界

推荐边界如下：

- 前端负责实时生成，提升编辑体验
- 后端负责长期存储和最终可信结果

后端保存时建议遵守以下原则：

- `content_markdown` 必存
- `content_html` 可由前端提交
- `content_toc_json` 可由前端提交
- 后端后续应支持重新生成或校验这些派生字段

这意味着：

- 当前阶段前端可以先把派生结果提交给后端，帮助业务快速闭环
- 后续后端能力增强后，可以逐步把“派生字段重建”收回服务端

## 十、为什么不建议只让前端生成最终 HTML

如果完全依赖前端生成最终 `HTML`，存在这些风险：

- 前后端渲染规则容易漂移
- 后续更换解析规则时历史文章难以统一回收
- 如果前端清洗不严格，存在安全风险
- 不利于后端做统一审核、摘要重建和目录重建

因此正确做法是：

- 前端先生成派生结果，服务当前编辑体验
- 后端长期仍以 `Markdown` 为主源

## 十一、当前阶段前端定稿建议

当前阶段前端正式建议定为：

- 编辑页正文仍然维护 `Markdown`
- 前端使用 `markdown-it + markdown-it-anchor + shiki` 进行实时预览渲染
- 前端使用标题提取逻辑生成 `TOC`
- 前端提交 `contentMarkdown + contentHtml + contentToc`
- 当前阶段暂不接入 `sanitize-html`
- 后端存储 `Markdown` 为主源，`HTML / TOC` 为派生字段

## 十二、后续实施顺序

建议按以下顺序落地：

1. 新增 `useMarkdownRenderer`
2. 新增 `MarkdownPreview`
3. 将文章编辑页正文区升级为实时预览模式
4. 增加目录面板
5. 调整文章新增/编辑接口字段，支持接收 `contentHtml` 与 `contentToc`
6. 后端后续再补统一校验或重建链路

## 十三、与当前项目技术路线的关系

本方案不改变现有技术路线，只是补充后台编辑阶段的前端实现方案。

现有正式路线仍然成立：

- `Markdown` 是内容主源
- 数据库存储 `Markdown + HTML + TOC`
- 正式内容详情页仍以服务端内容链路为准
- `Nuxt SSR` 负责最终 Web 页面输出

因此这份文档的定位是：

- 后台编辑体验方案
- 前端提交协议建议
- 与现有后端存储结构对齐的实施说明
