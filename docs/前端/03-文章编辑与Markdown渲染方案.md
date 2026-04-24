# 文章编辑与 Markdown 渲染方案

## 文档目标

本文档用于沉淀 `Yunyu` 项目当前已经落地的 Markdown 渲染链路，并与早期架构文档中的目标方案做对照，避免后续继续出现“文档里是一套、代码里是另一套”的情况。

本文档重点回答五个问题：

- 当前项目真实的 Markdown 渲染链路到底是什么
- 后台编辑预览、文章保存、前台详情页分别是谁在负责渲染
- 当前已经支持了哪些 Markdown 能力，还缺哪些能力
- 数学公式当前是如何接入的，以及后续还应该如何继续扩展
- Markdown 图表能力当前支持到什么程度，第一期为什么先统一使用 Mermaid

## 一、当前代码里的真实实现

截至当前版本，项目里实际运行的 Markdown 渲染链路如下：

### 1.1 后台编辑页预览链路

- 后台正文编辑区维护的是 `contentMarkdown`
- 前端通过 `useMarkdownRenderer` 本地实时把 `Markdown` 转成 `HTML`
- 同时前端提取标题结构，生成 `TOC`
- 同时前端计算纯文本和阅读时长
- 右侧预览区通过 `MarkdownPreview -> ArticleContentRenderer` 渲染最终预览效果

对应代码位置：

- `yunyu-web/app/composables/useMarkdownRenderer.ts`
- `yunyu-web/app/components/admin/post/MarkdownPreview.vue`
- `yunyu-web/app/components/admin/post/AdminPostEditorScreen.vue`
- `yunyu-web/app/components/content/ArticleContentRenderer.vue`

### 1.2 提交保存链路

- 前端提交文章时，同时提交：
  - `contentMarkdown`
  - `contentHtml`
  - `contentTocJson`
- 后端当前不会重新完整渲染 Markdown
- 后端当前主要负责保存前端提交的派生结果，并补充：
  - `contentPlainText`
  - `readingTime`

对应代码位置：

- `yunyu-web/app/components/admin/post/AdminPostEditorScreen.vue`
- `yunyu-server/src/main/java/com/ideaflow/yunyu/module/post/service/AdminPostService.java`
- `yunyu-server/src/main/java/com/ideaflow/yunyu/module/post/entity/PostContentEntity.java`

### 1.3 前台文章详情页链路

- 前台详情页不再现场把 `Markdown` 重新渲染为 `HTML`
- 前台详情页直接消费后端返回的 `contentHtml`
- 目录直接消费后端返回的 `contentTocJson`
- 正文最终仍由 `ArticleContentRenderer` 负责展示层样式与代码块交互增强

对应代码位置：

- `yunyu-web/app/pages/posts/[slug].vue`
- `yunyu-web/app/components/content/ArticleContentRenderer.vue`
- `yunyu-server/src/main/java/com/ideaflow/yunyu/module/site/vo/SitePostDetailResponse.java`

## 二、当前前端渲染技术栈

项目当前真正已经接入并在使用的能力如下：

- Markdown 解析器：`markdown-it`
- 标题锚点：`markdown-it-anchor`
- 代码高亮：`shiki`
- 原生 HTML 宽松清洗：`sanitize-html`
- 代码主题：`github-light`、`github-dark-default`

`yunyu-web/package.json` 当前已经接入以下数学公式能力：

- `katex`
- `markdown-it-texmath`

这说明当前项目的数学公式能力已经正式接入到前端 Markdown 渲染链路中，而不是依赖正文展示层临时补救。

## 三、当前已经实现的 Markdown 能力

以当前代码为准，前端 `useMarkdownRenderer` 已经支持：

- 标题解析
- 标题 `id` 生成
- 目录提取 `TOC`
- 外部链接自动新标签页打开
- 基础段落、粗体、列表、引用、图片等常规 Markdown 能力
- 代码块通过 `Shiki` 输出高亮 HTML
- 任务清单 `- [x] / - [ ]` 自定义渲染
- 行内公式 `$...$`
- 块级公式 `$$...$$`
- 更宽松的内容型原生 HTML：`div`、`span`、`figure`、`iframe`、`video`、`audio`、`details`、`summary`、`kbd`、`sub`、`sup`、`mark` 等
- 不限制来源的 iframe 嵌入
- 纯文本提取
- 阅读时长估算

正文展示层 `ArticleContentRenderer` 当前额外负责：

- 代码块复制
- 代码块折叠 / 展开
- 代码块工具栏图标增强
- macOS 风格三色圆点兼容补齐
- 内容主题和代码主题视觉统一
- Mermaid 图表客户端渲染
- Mermaid 图表暗黑模式主题适配
- Mermaid 图表失败回退源码展示
- Mermaid 图表缩放控制（放大 / 缩小 / 还原）

## 四、当前还没有实现的能力

截至当前版本，以下能力尚未接入或未形成完整闭环：

- 脚注渲染
- 自定义容器语法
- 服务端统一重建 `contentHtml`
- 服务端统一重建 `contentTocJson`
- 服务端统一校验前端提交的 HTML 派生结果

当前最明显的待补齐项，已经从“数学公式”切换为“服务端派生链路补强”和“更多 Markdown 扩展语法能力”。

## 五、与历史架构文档的关系

项目早期文档里的目标方案，主线是：

- `Markdown` 作为主内容源
- `HTML` 作为派生结果
- 最理想状态下由服务端在发布链路中生成 `HTML / TOC / 摘要 / 阅读时长`
- 前台详情页直接消费服务端派生结果，Nuxt 负责 SSR 页面输出

这个方向本身是正确的，当前代码也没有偏离主原则。

但当前真实落地状态与目标方案之间还有一个阶段性差异：

### 5.1 当前真实状态

- 编辑预览的 Markdown 渲染在前端完成
- 提交时前端把 `contentHtml` 和 `contentTocJson` 一起提交给后端
- 后端主要负责保存和补充纯文本、阅读时长

### 5.2 目标状态

- 前端仍然保留实时预览能力
- 但后端应该具备独立重建 `HTML / TOC / plainText / readingTime` 的能力
- 前端提交的派生结果更适合视为“编辑期辅助结果”，而不是长期唯一来源

### 5.3 当前结论

所以本项目当前最准确的表达应该是：

- `Markdown` 仍然是主内容源
- 编辑预览的渲染责任当前在前端
- 线上详情页展示当前依赖后端保存的 `contentHtml`
- 后端尚未完全收回派生字段生成责任

## 六、数学公式当前如何渲染

当前数学公式已经接入到现有 Markdown 渲染主链路中，处理方式不是在前台详情页额外二次解析，而是在 Markdown 转 HTML 时一次性完成。

### 6.1 Markdown 解析阶段

`useMarkdownRenderer` 当前只接入了：

- `markdown-it`
- `markdown-it-anchor`
- 自定义外链规则
- 自定义任务清单规则
- `shiki` 代码高亮

同时，项目已经接入：

- `katex`
- `markdown-it-texmath`

这意味着：

- 行内公式 `$...$` 会被解析为 KaTeX 行内公式 HTML
- 块级公式 `$$...$$` 会被解析为 KaTeX 块级公式 HTML
- `\begin{...}...\end{...}` 形式的块级环境也已经纳入当前渲染器支持范围

### 6.2 公式输出样式

项目当前已经引入 `katex`，因此公式渲染所需的基础资源也已经接入：

- KaTeX 全局 CSS
- KaTeX 字体资源
- 块级公式的正文容器样式
- 行内公式的正文融合样式

因此后台预览和前台详情页都可以直接消费同一份公式 HTML。

### 6.3 前台详情页运行链路

前台详情页当前直接展示后端返回的 `contentHtml`，不会再现场解析 Markdown。

这意味着：

- 后台编辑预览阶段会先生成带公式的 `contentHtml`
- 后端保存后，前台详情页可以直接复用这份公式 HTML
- 详情页不需要再额外跑一套公式解析逻辑

因此当前正式实现方式是：

- 数学公式在 Markdown 渲染阶段完成
- 正文展示层只负责样式和滚动体验
- 存库的 `contentHtml` 已经可以承载公式结果

## 八、Markdown 图表支持方案

当前项目已经进入第一期 Markdown 图表支持阶段，正式方案是：

- 统一支持 ` ```mermaid ` 代码块
- 后台编辑预览支持 Mermaid 实时渲染
- 前台详情页支持 Mermaid 图表渲染
- Mermaid 语法错误时回退显示源码，而不是整块空白
- 图表展示层支持暗黑模式适配
- 图表展示层支持放大、缩小、还原比例

### 8.1 第一阶段为什么先统一使用 Mermaid

第一阶段不并行接入多套图表引擎，原因是：

- Mermaid 语法门槛低，适合内容型站点作者使用
- 支持流程图、时序图、类图、状态图、ER 图、旅程图、甘特图等常见知识型图表
- 与 Markdown fence 语法天然契合，接入成本低
- 前后端都更容易围绕同一种图表标准沉淀规范、示例和文档

所以第一期标准能力明确为：

- `Mermaid = Markdown 图表的统一标准能力`

后续如果要扩展 PlantUML、Graphviz、ECharts DSL 或 draw.io 嵌入，应作为第二阶段能力，不在当前主链路并行推进。

### 8.2 当前支持的 Mermaid 图表类型

基于 Mermaid 当前能力，项目第一期可以支持的主流图表类型包括：

- 流程图 `flowchart`
- 时序图 `sequenceDiagram`
- 类图 `classDiagram`
- 状态图 `stateDiagram-v2`
- 实体关系图 `erDiagram`
- 用户旅程图 `journey`
- 甘特图 `gantt`
- Git 提交流程图 `gitGraph`
- 饼图 `pie`
- 象限图 `quadrantChart`
- 时间线 `timeline`
- 思维导图 `mindmap`

其中最推荐优先用于知识库与技术方案文档的类型是：

- 流程图
- 时序图
- 状态图
- ER 图
- 类图

### 8.3 当前渲染链路

当前 Mermaid 渲染链路分为两段：

1. `useMarkdownRenderer` 在 Markdown 解析阶段识别 `mermaid` fence，不再按普通代码块输出，而是输出图表占位结构。
2. `ArticleContentRenderer` 在客户端挂载后扫描 Mermaid 占位节点，动态加载 `mermaid` 运行时并渲染为 SVG。

这意味着：

- 后台预览与前台展示共用同一套 Mermaid 展示组件能力
- Mermaid 不走 Shiki 代码高亮链路
- Mermaid 的渲染责任位于“Markdown 解析 + 正文展示增强”之间

### 8.4 为什么不是服务端直接渲染 Mermaid SVG

当前第一期没有把 Mermaid SVG 生成放到服务端，主要原因是：

- 当前项目 Markdown 主渲染仍在前端
- 客户端 Mermaid 更容易复用在后台预览与前台详情页
- 第一阶段先保证编辑体验和展示闭环，避免过早把渲染职责拆成两套

但这也意味着：

- 如果数据库里存的是旧版 `contentHtml`，需要在后台重新保存一次文章，才能把新的 Mermaid 占位结构持久化
- 后续如果项目收口为“服务端统一派生 HTML”，再评估是否把 Mermaid SVG 派生也一起放到服务端

### 8.5 当前交互能力

当前 Mermaid 图表在正文中支持：

- 自适应正文宽度展示
- 横向滚动查看宽图
- 暗黑模式主题优化
- 工具栏放大
- 工具栏缩小
- 工具栏还原比例
- `Ctrl + 滚轮` 或 `Command + 滚轮` 缩放

### 8.6 当前边界

当前 Mermaid 第一阶段仍有明确边界：

- 只支持 Mermaid，不支持多图表引擎并行解析
- 不支持图内拖拽平移
- 不支持导出 PNG / SVG
- 不支持编辑态所见即所得图形编辑器
- 不支持服务端统一校验 Mermaid 语法

这部分能力如果后续要补，应进入第二期“Markdown 图表增强方案”单独设计。

## 七、原生 HTML 与 iframe 当前如何渲染

当前项目对原生 HTML 的策略，已经切换为“当前阶段默认全放开”。

- Markdown 解析阶段开启原生 HTML 识别
- 进入最终 HTML 时不再做标签、属性、样式、协议层面的前端清洗
- 正文尽量按作者原始 HTML 直接渲染
- 这次改动的目的，是避免第三方播放页、嵌入页、复杂容器布局因为清洗规则而失效

### 7.1 当前真实行为

当前前端对原生 HTML 的真实行为是：

- 标签层面：默认不拦截，作者写什么就尽量输出什么
- 属性层面：默认不拦截，包括自定义属性、播放器属性、`sandbox`、`allow`、`referrerpolicy` 等
- `style` 层面：默认不拦截，不再限制可写 CSS 属性和属性值
- 协议层面：默认不拦截，不再额外限制 `src` / `href` 的协议
- `iframe` 层面：不做来源白名单限制，也不再自动注入固定 `16:9` 响应式样式

这意味着像下面这些内容，当前都按原始 HTML 直接渲染：

- 折叠块
- 快捷键标记
- 下标 / 上标
- 高亮标记
- 视频 iframe 嵌入
- `div + iframe` 的响应式嵌入容器
- `video / audio / picture` 等内容媒体结构
- 自定义 `div / span` 容器和内联布局片段

### 7.2 当前为什么有的网站仍然可能无法播放

如果现在依然有站点不能嵌入，优先排查的就不再是前端清洗规则，而是目标站自身限制：

- 目标站返回了 `X-Frame-Options`
- 目标站配置了 `Content-Security-Policy: frame-ancestors`
- 目标站播放器本身不允许第三方页面嵌入
- 目标站要求特定 `referer`、登录态、cookie 或 token
- 目标站只允许自家官方 embed 地址，而不允许普通详情页地址直接进 `iframe`

也就是说：

- 当前“渲染不出来”，大概率已经不是 `yunyu-web` 的 HTML 清洗在拦
- 而是第三方站点自己禁止被嵌入

### 7.3 当前实现方式

当前实现落点在：

- `yunyu-web/app/composables/useMarkdownRenderer.ts`

当前的处理流程是：

1. `MarkdownIt` 开启原生 HTML 识别能力
2. 对 `html_block` 和 `html_inline` token 直接原样透传
3. 不再对标签、属性、样式、协议做前端清洗
4. 不再对 iframe 来源做白名单限制
5. 不再对单独的 iframe 自动注入固定 16:9 响应式样式
6. 正文展示层尽量按作者原始 HTML 呈现

### 7.4 当前保留的治理预案

当前版本虽然已经全放开，但这不代表未来永远不收紧。

这份文档保留治理预案，目的是后续如果遇到下面这些问题，可以再把规则一项项加回来：

- 某些嵌入导致站点安全风险上升
- 某些文章写入了破坏全站样式的原生 HTML
- 某些协议、事件属性或脚本型标签需要重新封禁
- 某些第三方嵌入需要改成白名单治理
- 某些样式值需要重新限制范围

也就是说，当前策略是：

- 现在先全放开，保证内容兼容性和嵌入成功率
- 未来如果确实出现问题，再按标签、属性、协议、样式四层逐步收紧

### 7.5 未来如需收紧，可按这四层回收

如果后续要重新治理，建议按下面顺序回收，而不是一次性全收死：

1. 先限制协议，例如只允许 `http`、`https`、`mailto`、`tel`
2. 再限制危险属性，例如 `on*` 事件属性、`srcdoc`
3. 再限制危险标签，例如 `script`、`style`、`form`
4. 最后再决定是否给 `iframe` 增加来源白名单和 `style` 白名单

## 八、数学公式最合适的实现方向

基于本项目当前已经成型的链路，数学公式最适合沿着现有 `markdown-it` 体系扩展，不建议在这一块突然切到另一整套渲染体系。

### 8.1 推荐方向

推荐采用：

- `markdown-it` 继续作为主解析器
- `KaTeX` 负责公式渲染
- 在 `useMarkdownRenderer` 中接入数学公式插件

原因：

- 当前主链路已经是 `markdown-it`
- 接入成本最低
- 更容易与现有编辑预览和详情页链路保持一致
- `KaTeX` 输出 HTML 较快，适合内容站
- 更适合当前“前端预览生成 HTML，后端保存派生结果”的阶段性实现

### 8.2 语法支持建议

建议至少支持以下两种公式语法：

- 行内公式：`$E = mc^2$`
- 块级公式：`$$\\int_a^b f(x) dx$$`

这是内容站最常见的数学表达形式，也是后续技术文章最基本的能力边界。

### 8.3 为什么优先选 KaTeX

对于当前项目，更推荐 `KaTeX` 而不是 `MathJax`，原因是：

- 输出更偏静态 HTML，适合当前内容存储模式
- 预览和详情页更容易共用同一份渲染结果
- 更符合“文章内容站”的渲染节奏

如果后续需要更复杂的公式交互能力，再评估是否需要更重的方案。

## 九、数学公式当前接入点

当前项目已经按下面的层次完成接入。

### 9.1 第一层：前端 Markdown 渲染器

接入位置：

- `yunyu-web/app/composables/useMarkdownRenderer.ts`

当前已完成：

- 给 `MarkdownIt` 注册数学公式插件
- 让 `$...$` 和 `$$...$$` 输出公式 HTML
- 让后台编辑页实时预览立即可见

### 9.2 第二层：公式样式资源

当前已完成：

- 引入 `KaTeX` 样式文件
- 把公式样式纳入当前正文内容展示体系
- 处理块级公式的间距、横向滚动和容器表现

### 9.3 第三层：前台详情页复用

当前详情页本来就直接吃 `contentHtml`，因此现在已经实现：

- 编辑预览生成公式 HTML
- 保存到后端的 `contentHtml` 可以承载公式结果
- 前台加载了公式样式

那么前台详情页就能直接复用，不需要另起一套公式渲染逻辑。

### 9.4 第四层：后端派生能力补强

更长期仍建议：

- 后端后续补上独立的 Markdown 派生链路
- 让 `contentHtml / contentTocJson / contentPlainText / readingTime` 可以在服务端统一重建
- 这样即使前端渲染策略升级，历史文章也可以回收重建

## 十、数学公式接入后的推荐数据流

建议未来的数据流明确为：

1. 作者在后台输入包含公式的 `Markdown`
2. 前端 `useMarkdownRenderer` 把公式一起渲染成 `HTML`
3. 编辑预览区立即展示公式结果
4. 提交时把 `contentMarkdown + contentHtml + contentTocJson` 一起提交
5. 后端保存正文和派生字段
6. 前台详情页直接渲染带公式结果的 `contentHtml`

在当前阶段，这条链路已经足够支撑公式展示闭环。

## 十一、当前阶段的正式结论

当前项目关于 Markdown 渲染的正式结论如下：

- 内容主源仍然是 `Markdown`
- 当前编辑预览的 Markdown 渲染责任在前端
- 当前前端实际使用的是 `markdown-it + markdown-it-anchor + shiki + sanitize-html`
- 当前前端数学公式使用的是 `katex + markdown-it-texmath`
- 当前原生 HTML 采用“宽松内容型放行”策略，而不是完全无过滤开放
- 前台详情页当前直接展示后端返回的 `contentHtml`
- 后端当前主要负责保存 `Markdown / HTML / TOC`，并补充纯文本与阅读时长
- 数学公式当前已经打通后台预览、保存和前台详情页展示链路
- iframe 等原生嵌入当前已经支持，来源不再受白名单限制
- 当前更适合继续沿现有 `markdown-it` 链路扩展，而不是重换一套 Markdown 渲染体系

## 十二、后续实施建议

建议按这个顺序推进：

1. 用一篇包含公式的测试文章完整走通编辑预览、保存、详情页展示
2. 用一篇包含 iframe / video / div 嵌入的测试文章验证自由布局是否稳定
3. 继续补充 `\begin{...}...\end{...}` 等更复杂数学环境的内容样例
4. 再评估是否要补脚注、自定义容器等扩展语法
5. 后续再决定是否把公式渲染能力同步收回到后端派生链路

这套顺序的好处是：

- 先让前台和后台预览可用
- 不打断当前已成型的编辑链路
- 后续仍保留把派生能力收回后端的演进空间

## 十三、Markdown 图表渲染支持方案

## 13.1 当前为什么还不支持 Mermaid

以当前代码为准，`useMarkdownRenderer` 的 `fence` 代码块处理只做了两件事：

- 按语言类型走 `Shiki` 代码高亮
- 输出统一代码块工具栏与代码块容器 HTML

这意味着：

- ```` ```mermaid ```` 当前会被当作普通代码块
- `sequenceDiagram`、`flowchart` 等内容只会以源码文本展示
- 后台预览和前台详情页都不会真正执行 Mermaid 渲染

因此当前“不支持 Mermaid”不是正文样式层的问题，而是 Markdown 渲染链路里还没有接入“图表块识别 + 图表引擎渲染”这层能力。

## 13.2 正式技术结论

本项目关于 Markdown 图表支持，建议采用下面这套正式结论：

- 第一阶段统一采用 `Mermaid` 作为 Markdown 图表标准能力
- 仍然沿用当前 `markdown-it` 主链路扩展，不切换整套 Markdown 渲染体系
- 后台编辑预览与前台详情页共用同一套图表渲染组件能力
- Markdown 存储仍然保存原始源码，不把图表源码转成静态图片再存库
- `contentHtml` 中保存的是“图表占位节点 + 原始图表源码”，真正图形渲染在前端组件层完成

原因：

- 当前项目的 Markdown 主链路已经是 `markdown-it`
- Mermaid 已经覆盖流程图、时序图、类图、ER 图、甘特图等内容站最常见图种
- 后台预览和前台详情页可以共用统一渲染逻辑
- 后续继续支持更多图种时，只需要在“代码块语言 -> 图表渲染器”这层扩展

## 13.3 第一阶段建议支持的图表类型

第一阶段建议把 `mermaid` fenced code block 作为统一入口，支持 Mermaid 主流图种。

建议明确支持：

- `flowchart`：流程图
- `sequenceDiagram`：时序图
- `classDiagram`：类图
- `stateDiagram-v2`：状态图
- `erDiagram`：ER 图
- `journey`：用户旅程图
- `gantt`：甘特图
- `pie`：饼图
- `mindmap`：思维导图
- `timeline`：时间线
- `gitGraph`：Git 流程图
- `quadrantChart`：象限图
- `requirementDiagram`：需求图
- `C4Context / C4Container / C4Component / C4Dynamic / C4Deployment`：C4 架构图

第一阶段不建议一口气再并行接入多套图表引擎。

正式建议是：

- 先把 `mermaid` 这一套打通
- 先覆盖内容站最常见图表需求
- 后续再评估是否需要增加独立 fence 语言支持

## 13.4 除了 Mermaid 之外，Markdown 图表还可以支持哪些

从 Markdown 内容站的长期演进看，常见图表能力大致可分为两类。

### 13.4.1 通用图表语法

- `mermaid`
- `plantuml`
- `graphviz` / `dot`
- `d2`
- `markmap`

### 13.4.2 专项图表语法

- `vega-lite`：数据图表
- `wavedrom`：时序波形图
- `abc`：简谱 / 乐谱
- `nomnoml`：轻量 UML

但对当前项目，不建议第一阶段同时把这些全部接进来。

更合理的产品策略是：

1. 第一阶段统一支持 `mermaid`
2. 第二阶段如果明确有架构图、UML、数据图表等稳定需求，再扩 fence 语言
3. 不为了“看起来全能”而一次性引入多套前端图表运行时

## 13.5 推荐渲染架构

建议在当前链路上新增“图表块渲染层”。

正式分层建议如下：

1. Markdown 解析层：`useMarkdownRenderer`
2. 图表块识别层：识别 fenced code block 的语言
3. 图表占位输出层：输出统一图表节点 HTML
4. 图表运行时层：前端在挂载后执行 Mermaid 渲染
5. 展示样式层：统一图表容器、滚动、错误态、暗色模式

### 13.5.1 Markdown 解析层

在 `useMarkdownRenderer.ts` 中扩展 `fence` 规则：

- 如果语言不是图表语言，维持现有代码块逻辑
- 如果语言是 `mermaid`，不再走 `Shiki` 代码高亮
- 而是输出统一图表占位节点

建议输出形态类似：

```html
<div
  class="yy-md-diagram-block"
  data-diagram-engine="mermaid"
>
  <pre class="yy-md-diagram-source" hidden>原始 Mermaid 源码</pre>
  <div class="yy-md-diagram-canvas"></div>
</div>
```

不建议直接输出：

- 原始 `<script>` 标签
- 运行时内联脚本
- 直接把 Mermaid 生成 SVG 后存回数据库

### 13.5.2 图表运行时层

建议新增统一客户端能力，例如：

- `app/components/content/ArticleDiagramRenderer.vue`
- `app/composables/useArticleDiagramRenderer.ts`

职责：

- 扫描正文中的 `.yy-md-diagram-block`
- 读取 `data-diagram-engine`
- 如果是 `mermaid`，动态加载 Mermaid 运行时
- 把源码渲染为 SVG 并写入 `.yy-md-diagram-canvas`
- 渲染失败时显示源码回退和错误提示

这样后台编辑预览与前台详情页都能复用同一套图表运行时。

### 13.5.3 详情页与预览页复用原则

这块必须统一，不建议后台预览和前台详情页各写一套。

正式建议：

- `MarkdownPreview` 继续负责编辑器右侧预览承载
- `ArticleContentRenderer` 继续负责正文 HTML 展示
- 图表渲染能力作为 `ArticleContentRenderer` 内部增强的一部分统一接入

这样：

- 后台预览能看到 Mermaid 图
- 前台详情页也能渲染同样的 Mermaid 图
- 内容表现不会出现“后台能看，前台不能看”或反过来的分裂

## 13.6 数据流设计

建议第一阶段的数据流为：

1. 作者在后台输入 Mermaid Markdown
2. `useMarkdownRenderer` 在前端把 Mermaid fenced code block 转成图表占位 HTML
3. 编辑预览区由 `ArticleContentRenderer` 挂载 Mermaid 运行时并绘制图表
4. 提交保存时，仍然保存：
   - `contentMarkdown`
   - `contentHtml`
   - `contentTocJson`
5. 前台详情页直接消费 `contentHtml`
6. `ArticleContentRenderer` 在客户端再次把图表占位节点渲染为 Mermaid SVG

注意：

- 第一阶段 `contentHtml` 存的是“图表占位结构”，不是最终 SVG
- SVG 运行时生成，避免后续 Mermaid 升级后历史 SVG 难以统一回收

## 13.7 为什么第一阶段不建议把 Mermaid 渲染结果直接存成 SVG

虽然“保存时直接生成 SVG”看起来也能做，但当前阶段不推荐，原因有三点：

- 当前项目的 Markdown 派生主链路还在前端，先把运行时闭环打通更稳
- Mermaid 渲染涉及主题、暗色模式、字号和容器宽度，直接固化 SVG 不够灵活
- 未来如果 Mermaid 版本升级，历史内容的图形更新会更麻烦

所以当前更推荐：

- Markdown 存源码
- HTML 存占位结构
- SVG 在前端运行时生成

## 13.8 失败兜底与降级策略

图表能力不能只考虑成功态，必须定义失败兜底。

建议兜底规则如下：

1. Mermaid 源码解析成功：
   - 展示 SVG 图
2. Mermaid 渲染失败：
   - 显示简洁错误提示
   - 同时允许展开查看原始 Mermaid 源码
3. 客户端未加载 Mermaid 运行时：
   - 至少保留源码回退
4. 服务端渲染阶段：
   - 不阻塞页面 HTML 输出
   - 图表区域先输出占位节点，客户端再补图

正式要求：

- 不因为某一张 Mermaid 图报错导致整篇文章正文渲染失败
- 单个图失败时，影响范围必须限制在当前图表块内

## 13.9 暗色模式与主题适配

Mermaid 接入后，不能只解决“能画出来”，还要解决主题一致性。

建议：

- Mermaid 运行时主题跟随当前页面色彩模式
- `light` 模式和 `dark` 模式使用不同 Mermaid theme 配置
- 图表容器外层继续沿用当前正文卡片风格
- 移动端默认支持横向滚动与最大宽度限制

建议至少统一处理：

- 背景色
- 主文字色
- 线条色
- 节点圆角
- 标题字号
- 容器内边距

## 13.10 后续扩展方案

如果 Mermaid 第一阶段稳定后，后续可以按 fence language 扩展成统一图表协议：

```ts
type MarkdownDiagramEngine =
  | 'mermaid'
  | 'plantuml'
  | 'graphviz'
  | 'markmap'
  | 'vega-lite'
```

统一图表块结构建议保持：

```ts
interface MarkdownDiagramBlock {
  engine: MarkdownDiagramEngine
  source: string
  title?: string
}
```

这样未来新增图表引擎时：

- 不需要推翻现有 Markdown 渲染链路
- 只需要增加新的图表运行时适配器
- 正文展示层仍然保持统一结构

## 13.11 当前阶段的正式实施建议

结合当前项目状态，建议按下面顺序落地：

1. 第一阶段：只支持 ` ```mermaid ` fenced code block
2. 在 `useMarkdownRenderer` 中增加 Mermaid 图表块识别和占位输出
3. 在 `ArticleContentRenderer` 中增加 Mermaid 客户端渲染能力
4. 完成后台预览、前台详情页、暗色模式、错误态闭环
5. 再评估是否扩展 `plantuml / graphviz / markmap / vega-lite`

当前阶段的正式结论：

- Mermaid 是本项目 Markdown 图表支持的第一优先级
- 流程图、时序图之外，第一阶段一并覆盖 Mermaid 主流图种
- 不建议第一阶段并行接入多套图表引擎
- 最合适的落地方式是在现有 `markdown-it` 链路上增加“图表占位 + 前端运行时渲染”能力
