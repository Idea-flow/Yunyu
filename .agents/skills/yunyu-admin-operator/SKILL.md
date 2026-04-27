---
name: yunyu-admin-operator
description: Use when operating the Yunyu admin backend through the generated admin API reference, including updating site config, homepage config, posts, categories, tags, topics, comments, attachments, friend links, users, storage settings, or AI provider settings under /api/admin/**. Read references/admin-api-docs.md first, require a SUPER_ADMIN Bearer token plus the target base URL, and persist them locally for reuse when available.
---

# Yunyu Admin Operator

## 适用场景

当用户希望让 agent 操作 `Yunyu` 后台时使用本 skill，例如：

1. 修改站点配置
2. 新增、修改、删除文章
3. 管理分类、标签、专题
4. 审核或删除评论
5. 管理友链、附件、用户
6. 修改后台 S3 配置或 AI 提供商配置

## 工作流

1. 先读取 `references/admin-api-docs.md`，不要直接猜接口。
2. 如果 `references/admin-api-docs.md` 不存在，或者文档里缺少目标接口，停止操作并明确告诉用户：
   当前 skill 缺少可用接口文档，无法安全操作，请先更新 skill 文档。
3. 再读取 `references/connection-management.md`，先确定当前是否已有本地保存的连接信息。
4. 连接信息固定保存在：
   `.agents/skills/yunyu-admin-operator/.local/admin-connection.json`
   可优先通过脚本读取：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show`
5. 若本地连接文件不存在，先向用户索要：
   - 后台域名或基础地址，例如 `https://admin.example.com`
   - 对应后台登录后的 Bearer token
6. 如用户未直接提供 token，可提示用户从已登录后台浏览器获取：
   `localStorage.getItem('yunyu_access_token')`
7. 收到 `baseUrl` 和 `token` 后，通过脚本保存到本地连接文件，后续优先复用，减少重复询问：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>`
8. 如果是文章新增、编辑或文章元信息生成，再继续读取 `references/post-management.md`。
   文章相关的 `slug`、`summary`、`seoTitle`、`seoDescription` 默认优先由当前 AI agent 直接生成，不默认调用后台元信息生成接口。
   若用户未指定分类或标签，默认先查询已有分类 / 标签并按文章语义自动匹配最合适的项；没有明显匹配时可留空，不乱选。
   专题默认不设置，只有用户明确要求挂专题时，才查询和处理专题相关接口。
   文章状态在用户未明确指定时默认使用 `DRAFT`；只有用户明确要求直接发布时，才使用 `PUBLISHED`。
9. 如果是分类、标签、专题管理，再继续读取 `references/taxonomy-management.md`。
10. 如果是站点配置、首页配置、S3 配置或 AI 提供商配置，再继续读取 `references/site-settings-management.md`。
11. 对任何写操作，先用 `/api/auth/me` 校验当前 token 对应用户是否为 `SUPER_ADMIN`。
12. 实际调接口时，优先通过统一脚本发起请求，例如：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh GET /api/admin/posts`
13. 读操作可直接执行；写操作先给出接口、关键参数和预期影响。
14. 对 `DELETE`、批量操作、S3/AI 密钥类敏感配置更新，必须显式确认后再执行。
15. 如果接口调用因为鉴权失败、域名错误、网络错误或 4xx/5xx 失败，要明确告诉用户失败原因，并询问是否更换 token 或域名后重试。
16. 执行完成后，说明实际调用的接口、修改的关键字段和结果。

## 参考文件

- `references/admin-api-docs.md`
  作用：生成 skill 时产出的 `/api/admin/**` 接口参考，供 agent 快速查找可用后台能力。
- `references/connection-management.md`
  作用：说明后台域名、token 的获取方式、本地保存位置以及失败后的处理方式。
- `references/troubleshooting.md`
  作用：说明后台接口失败时如何区分 token、域名、权限、接口路径和服务端异常问题。
- `scripts/admin_connection.sh`
  作用：本地连接信息读写脚本，负责保存、读取、展示和清理 `admin-connection.json`。
- `scripts/admin_request.sh`
  作用：统一读取本地连接信息并发起后台 HTTP 请求，减少手工拼接 `curl` 的出错概率。
- `references/post-management.md`
  作用：文章新增、编辑、状态选择、元信息生成和内容权限相关的详细操作说明。
- `references/taxonomy-management.md`
  作用：分类、标签、专题的创建、编辑、删除说明。
- `references/site-settings-management.md`
  作用：站点配置、首页配置、S3 配置和 AI 提供商配置的操作说明。

## 执行约束

1. 优先只发送本次操作真正需要的字段，不要盲目提交整对象。
2. 如果 OpenAPI 文档里没有字段说明，不要猜复杂业务语义，应提示“字段描述不足”。
3. 若用户意图模糊，优先先查接口和对象，再做最小确认。
4. `references/admin-api-docs.md` 属于 skill 生成产物；使用 skill 时不负责刷新它，缺失或接口不全时应提示用户先更新 skill 文档。
5. 每次实际发起请求前，都应优先读取本地连接文件；只有在缺失或请求失败时，才要求用户补充或更换 `baseUrl` / `token`。
