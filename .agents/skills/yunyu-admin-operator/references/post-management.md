# 文章管理操作说明

本文档用于指导 agent 通过后台接口完成文章新增、编辑与相关 AI 辅助操作。

## 零、统一前置步骤

在查询或写入文章相关后台接口前，先处理连接信息：

1. 先读取：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show`
2. 如果本地没有可用连接信息，先让用户提供：
   - 后台域名或基础地址
   - 当前环境的后台 token
3. 收到后保存：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>`
4. 如请求失败，优先判断是否需要更换 `baseUrl` 或 `token`，不要直接假设是业务字段错误。

## 一、创建文章

建议按以下顺序操作：

1. 先确认用户是否已经提供文章标题和正文。
2. 如果用户没有指定分类或标签，先查询可用列表：
   - `GET /api/admin/categories`
   - `GET /api/admin/tags`
3. 根据文章标题和正文语义，从已有分类、标签里挑选最合适的项：
   - 若确实存在明显匹配项，应自动选中对应分类 / 标签
   - 若没有明显匹配项，可以留空，不要为了凑字段乱选
4. 专题默认视为“不设置”：
   - 只有用户明确说本次也要挂专题时，才查询 `GET /api/admin/topics`
   - 用户未指定时，不调用专题相关接口
5. `slug`、`summary`、`seoTitle`、`seoDescription`：
   - 如果用户已提供，优先使用用户提供值。
   - 如果用户未提供，优先由当前 AI agent 根据标题和正文直接生成。
   - 默认不要为了补这些字段再额外调用 `POST /api/admin/posts/ai/meta/generate`。
   - 只有当用户明确要求使用后台 AI 元信息生成能力，或需要和后台内置生成结果保持一致时，才考虑调用该接口。
   - 调用该接口时优先传 `title` 与 `contentMarkdown`；后端会统一组装提示词并返回 OpenAI Chat 风格结果。
6. 文章状态默认建议：
   - 若用户没有明确要求立即发布，默认传 `DRAFT`
   - 若用户明确说“直接发布”，传 `PUBLISHED`
   - 若用户明确说“保存但先下线”，传 `OFFLINE`
7. 布尔默认建议：
   - `isTop=false`
   - `isRecommend=false`
   - `allowComment=true`
8. 内容权限默认不启用。
   - 应主动提示用户：“当前默认不启用内容权限，要不要开启文章访问控制或隐藏内容权限？”
9. 若用户不启用内容权限：
   - 仍建议提交默认的禁用结构，保持与当前后台编辑器一致。
10. 若用户启用整篇文章访问控制：
   - 至少选择一个规则：`LOGIN` / `WECHAT_ACCESS_CODE` / `ACCESS_CODE`
   - 若包含 `ACCESS_CODE`，必须同时填写：
     - `articleAccessCode`
     - `articleAccessCodeHint`
11. 若用户启用尾部隐藏内容：
    - 必须填写：
      - `tailHiddenAccess.enabled=true`
      - `tailHiddenAccess.title`
      - `tailHiddenAccess.ruleTypes`
      - `tailHiddenContentMarkdown`
12. 创建文章最终调用：
    - `POST /api/admin/posts`

## 二、编辑文章

编辑文章不要直接凭空拼 `PUT` 请求。

建议按以下顺序：

1. 先定位目标文章。
2. 用 `GET /api/admin/posts/{postId}` 读取当前完整详情。
3. 在原详情基础上合并本次修改。
4. 因为更新接口要求很多字段保留完整语义，不能只传一个零散字段然后丢掉其他字段。
5. 若用户只说“改摘要”或“改标题”：
   - 也应该先获取原文详情
   - 再仅修改目标字段
   - 其余字段保持原值
6. 更新文章最终调用：
   - `PUT /api/admin/posts/{postId}`

## 三、文章创建与编辑时的重点字段

### 3.1 `status`

- `DRAFT`：草稿
- `PUBLISHED`：已发布
- `OFFLINE`：已下线

默认规则：

1. 新建文章默认 `DRAFT`
2. 编辑文章时若用户没明确改状态，保留原值

### 3.2 `categoryId` / `tagIds` / `topicIds`

这些字段不要猜。

应先查询真实可用项，再根据文章主题选择最合适的 ID。

专题默认留空；只有用户明确要求时才处理 `topicIds`。

### 3.3 `slug`

建议：

1. 使用小写英文和连字符
2. 与标题或内容主题一致
3. 避免中文、空格和随意符号

### 3.4 SEO 字段

- `seoTitle`
- `seoDescription`

若用户未指定，可根据文章标题和正文自动生成。

### 3.5 `contentAccessConfig`

若未明确需求，默认不启用。

但新增文章时应提醒一次，不要静默假设用户一定不需要。

## 四、推荐给用户的最小确认

在新增文章时，若信息不完整，可优先补以下最少问题：

1. 这篇文章要不要直接发布，还是先存草稿？
2. 要不要挂分类 / 标签 / 专题？
3. 要不要启用内容权限或尾部隐藏内容？

如果用户说“按默认来”：

1. 状态用 `DRAFT`
2. 优先自动匹配已有分类 / 标签；没有明显匹配时可留空
3. 不设置专题
4. 不启用内容权限
5. 允许评论
6. 不置顶、不推荐
