---
name: yunyu-admin-operator
description: Use when operating the Yunyu admin backend through the OpenAPI-derived admin API reference, including updating site config, homepage config, posts, categories, tags, topics, comments, attachments, friend links, users, storage settings, or AI provider settings under /api/admin/**. Read the generated references/admin-api-docs.md first, refresh it with the external repo script when missing or stale, and require a SUPER_ADMIN Bearer token from the browser storage key yunyu_access_token or from explicit user input.
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
2. 如果文档不存在、明显过期、或缺少目标接口，执行：
   `bash scripts/ai/refresh_admin_api_reference.sh --base-url http://127.0.0.1:20000`
3. 如果是文章新增、编辑或文章元信息生成，再继续读取 `references/post-management.md`。
4. 如果是分类、标签、专题管理，再继续读取 `references/taxonomy-management.md`。
5. 如果是站点配置、首页配置、S3 配置或 AI 提供商配置，再继续读取 `references/site-settings-management.md`。
6. 如果需要鉴权但没有 token，提示用户从已登录后台浏览器获取：
   `localStorage.getItem('yunyu_access_token')`
7. 对任何写操作，先用 `/api/auth/me` 校验当前 token 对应用户是否为 `SUPER_ADMIN`。
8. 读操作可直接执行；写操作先给出接口、关键参数和预期影响。
9. 对 `DELETE`、批量操作、S3/AI 密钥类敏感配置更新，必须显式确认后再执行。
10. 执行完成后，说明实际调用的接口、修改的关键字段和结果。

## 参考文件

- `references/admin-api-docs.md`
  作用：从 `/v3/api-docs` 过滤出的 `/api/admin/**` 接口参考，供 agent 快速查找可用后台能力。
- `references/post-management.md`
  作用：文章新增、编辑、状态选择、元信息生成和内容权限相关的详细操作说明。
- `references/taxonomy-management.md`
  作用：分类、标签、专题的创建、编辑、删除说明。
- `references/site-settings-management.md`
  作用：站点配置、首页配置、S3 配置和 AI 提供商配置的操作说明。
- `scripts/ai/generate_admin_api_reference.py`
  作用：仓库外层生成脚本，从 OpenAPI 文档生成精简版后台接口参考 Markdown。
- `scripts/ai/refresh_admin_api_reference.sh`
  作用：仓库外层的便捷刷新脚本，生成后会把结果写回本 skill 的 `references/admin-api-docs.md`。

## 执行约束

1. 优先只发送本次操作真正需要的字段，不要盲目提交整对象。
2. 如果 OpenAPI 文档里没有字段说明，不要猜复杂业务语义，应提示“字段描述不足”。
3. 若用户意图模糊，优先先查接口和对象，再做最小确认。
4. 如果后续 DTO / VO 增加了 `@Schema` 描述、示例值或枚举说明，重新生成 `references/admin-api-docs.md` 后再使用更新后的说明。
