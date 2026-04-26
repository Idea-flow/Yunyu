# Skill-Only 后台 AI 操作实现方案（一期）

## 文档目标

本文档用于定义 `Yunyu` 当前阶段的后台 AI 接入方式。

本期先不做独立 `MCP`，而是先建设一个简单可用的 `skill-only` 版本，目标是：

1. 让 agent 可以通过项目内 skill 操作后台接口。
2. 把完整 `/v3/api-docs` 收敛成精简的后台接口 Markdown。
3. 让 agent 先从精简接口文档读取可用能力，降低 token 消耗。
4. 为后续 DTO / VO 字段描述增强打好基础。

## 一、为什么先做 skill-only

当前阶段先做 `skill-only`，原因是：

1. 实现更轻
2. 维护成本更低
3. 不需要单独启动新的 `MCP` 服务
4. 当前项目已经具备 OpenAPI、后台权限和浏览器 token 存储三项基础条件

因此，一期可以先采用：

1. `skill` 负责操作规范
2. `/v3/api-docs` 负责能力来源
3. 精简接口 Markdown 负责低成本阅读
4. agent 直接通过现有 HTTP 能力访问后台

## 二、本期交付内容

本期建议交付以下内容：

1. 项目内 skill：`yunyu-admin-operator`
2. 仓库外层后台接口参考生成脚本
3. 精简后台接口文档：`admin-api-docs.md`
4. 后续 DTO / VO 字段描述增强规范

## 三、目录设计

建议目录如下：

```text
scripts/ai/
├── generate_admin_api_reference.py
└── refresh_admin_api_reference.sh

.agents/skills/yunyu-admin-operator/
├── SKILL.md
└── references/
    ├── admin-api-docs.md
    ├── post-management.md
    ├── taxonomy-management.md
    └── site-settings-management.md
```

各文件职责如下：

### `SKILL.md`

作用：

1. 告诉 agent 什么时候触发本 skill
2. 告诉 agent 应该按什么步骤操作后台
3. 告诉 agent 什么时候必须先确认
4. 告诉 agent 如何获取站长 token

### `scripts/ai/generate_admin_api_reference.py`

作用：

1. 请求 `/v3/api-docs`
2. 过滤出所有 `/api/admin/**`
3. 输出适合 agent 阅读的精简 Markdown
4. 默认写回 `.agents/skills/yunyu-admin-operator/references/admin-api-docs.md`

### `scripts/ai/refresh_admin_api_reference.sh`

作用：

1. 作为仓库外层便捷刷新入口
2. 调用 Python 生成脚本
3. 把产物写回 skill 的 `references` 目录

### `admin-api-docs.md`

作用：

1. 作为 agent 优先读取的后台接口参考
2. 避免直接把完整 OpenAPI JSON 放进上下文
3. 降低 token 消耗

### `post-management.md`

作用：

1. 详细说明文章新增和编辑时的操作顺序
2. 说明文章状态、分类 / 标签 / 专题查询、SEO 生成和内容权限确认规则

### `taxonomy-management.md`

作用：

1. 详细说明分类、标签、专题的增删改流程
2. 统一状态值和更新前先查详情的约束

### `site-settings-management.md`

作用：

1. 详细说明站点配置、首页配置及敏感设置的修改流程
2. 强调设置类接口应先 GET 再合并再 PUT

## 四、skill 工作流

skill 在使用时建议遵循以下流程：

1. 先读取 `admin-api-docs.md`
2. 如果文档缺失或过期，则执行仓库外层脚本重新生成
3. 根据业务类型继续读取对应详细文档：
   - 文章管理：`post-management.md`
   - 分类 / 标签 / 专题：`taxonomy-management.md`
   - 站点 / 首页 / S3 / AI 设置：`site-settings-management.md`
4. 若需要后台权限，先获取 Bearer Token
5. 调用 `/api/auth/me` 验证当前 token 是否是 `SUPER_ADMIN`
6. 读取目标接口的 method、path、参数和请求体字段
7. 对读操作直接执行
8. 对写操作先给出变更摘要
9. 对删除、批量和敏感配置变更要求确认后再执行

## 五、接口文档精简策略

为了减少 token 消耗，精简版接口 Markdown 不应该原样复制整个 OpenAPI 文档。

建议只保留以下信息：

1. HTTP method
2. path
3. summary
4. tag
5. operationId
6. 参数列表
7. 请求体顶层字段
8. 响应 schema 摘要
9. 风险等级

这已经足够让 agent 完成大多数后台操作判断。

## 六、为什么 DTO / VO 字段描述增强很重要

这是本方案里很关键的一点。

你的判断是对的：

> 补充 DTO 和 VO 的字段描述后，AI 会更容易理解接口语义。

原因是：

1. skill 本身只定义流程，不定义具体业务字段含义
2. 精简接口 Markdown 来自 `/v3/api-docs`
3. OpenAPI 的字段说明质量，决定 agent 对接口的理解质量

所以后续最值得做的增强，不是继续堆 prompt，而是提升 DTO / VO 的 OpenAPI 描述质量。

## 七、DTO / VO 描述增强建议

建议后续逐步在后端 DTO / VO 上增加以下信息：

1. 字段中文说明
2. 示例值
3. 枚举值说明
4. 是否必填
5. 长度限制或格式约束
6. 默认策略说明，例如“若用户未明确要求，文章状态默认草稿”

建议优先使用：

- `io.swagger.v3.oas.annotations.media.Schema`

例如：

```java
@Schema(description = "站点标题", example = "云屿")
private String siteTitle;
```

再例如：

```java
@Schema(description = "文章状态", example = "DRAFT", allowableValues = {"DRAFT", "PUBLISHED", "PRIVATE"})
private String status;
```

这样做的效果是：

1. Swagger UI 更清晰
2. `/v3/api-docs` 更清晰
3. 生成出的 `admin-api-docs.md` 也会更清晰
4. agent 不用猜字段含义

## 八、建议优先增强的对象

建议优先增强后台高频使用的 DTO / VO：

1. 文章相关 DTO / VO
2. 站点配置 DTO / VO
3. 首页配置 DTO / VO
4. 评论状态更新 DTO / VO
5. 友链状态更新 DTO / VO
6. S3 配置 DTO / VO
7. AI 提供商配置 DTO / VO

这样 skill 的使用体验会最快提升。

## 九、认证方案

skill-only 版本继续复用当前已有认证体系：

1. 后台接口仍由 `/api/admin/**` + `SUPER_ADMIN` 控制
2. token 来源优先为浏览器中的 `yunyu_access_token`
3. 若 agent 访问不到浏览器上下文，再由用户手动提供 token

建议固定校验步骤：

1. 获取 token
2. 调用 `/api/auth/me`
3. 检查 `role == SUPER_ADMIN`
4. 通过后才允许写后台

## 十、一期边界

一期只做简单版，不引入额外重型设计：

1. 不做独立 MCP server
2. 不做复杂自动工作流编排
3. 不做额外数据库审计表
4. 不做站内专属 AI 操作平台

一期先解决的是：

1. agent 知道有哪些后台接口可用
2. agent 能低 token 成本读取接口说明
3. agent 能安全地用现有 token 操作后台

## 十一、后续演进方向

当 `skill-only` 版本稳定后，再考虑是否升级到 `MCP`。

后续可按以下顺序演进：

1. 先持续增强 DTO / VO 的 `@Schema` 描述
2. 再稳定精简版后台接口 Markdown
3. 再根据复用需求决定是否收敛为 MCP

如果未来你希望：

1. 任意 agent 标准接入
2. 跨项目复用更强
3. 工具调用更结构化

再升级到 MCP 会更合适。

## 十二、结论

当前最合适的一期方案是：

1. 先做 repo 内 `skill`
2. 先用仓库外层脚本把 `/v3/api-docs` 过滤成精简后台接口 Markdown
3. 让 agent 优先从这个 Markdown 读取后台可用能力
4. 同时逐步增强 DTO / VO 字段描述

这样做的优点是：

1. 轻量
2. 容易维护
3. token 成本低
4. 能快速投入使用
5. 又为未来升级到 MCP 保留空间
