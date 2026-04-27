# Yunyu 后台接口参考

本文档由仓库外层脚本从 `/v3/api-docs` 自动生成，只保留 `/api/admin/**` 接口，用于让 agent 以更低 token 成本快速读取后台能力。

使用前提醒：

1. 调用任一后台接口前，先读取本地连接信息：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show`
2. 如果没有可用连接信息，先向用户索要当前环境的 `baseUrl` 和 `token`。
3. 若请求失败，优先参考 `references/troubleshooting.md` 判断是 token、域名、权限还是接口问题。

- 来源：`http://127.0.0.1:20000/v3/api-docs`
- 生成时间：`2026-04-27 00:06:10 +0800`
- 接口总数：`52`

## 接口总览

- `GET /api/admin/attachments`：查询附件列表 [LOW]
- `POST /api/admin/attachments/check-exists`：检查附件是否已存在 [MEDIUM]
- `POST /api/admin/attachments/complete`：提交上传完成回执 [MEDIUM]
- `POST /api/admin/attachments/presign`：生成上传预签名 [MEDIUM]
- `DELETE /api/admin/attachments/{attachmentId}`：删除附件 [HIGH]
- `GET /api/admin/categories`：查询后台分类列表 [LOW]
- `POST /api/admin/categories`：创建分类 [MEDIUM]
- `GET /api/admin/categories/{categoryId}`：查询单个分类详情 [LOW]
- `PUT /api/admin/categories/{categoryId}`：更新分类 [MEDIUM]
- `DELETE /api/admin/categories/{categoryId}`：删除分类 [HIGH]
- `GET /api/admin/comments`：查询后台评论列表 [LOW]
- `GET /api/admin/comments/thread-groups`：查询后台评论树形审核列表 [LOW]
- `GET /api/admin/comments/{commentId}`：查询单条评论详情 [LOW]
- `DELETE /api/admin/comments/{commentId}`：删除评论 [HIGH]
- `PUT /api/admin/comments/{commentId}/status`：更新评论状态 [MEDIUM]
- `GET /api/admin/friend-links`：查询后台友链列表 [LOW]
- `POST /api/admin/friend-links`：创建友链 [MEDIUM]
- `GET /api/admin/friend-links/{friendLinkId}`：查询后台友链详情 [LOW]
- `PUT /api/admin/friend-links/{friendLinkId}`：更新友链 [MEDIUM]
- `DELETE /api/admin/friend-links/{friendLinkId}`：删除友链 [HIGH]
- `PUT /api/admin/friend-links/{friendLinkId}/status`：快速更新友链状态 [MEDIUM]
- `GET /api/admin/posts`：查询后台文章列表 [LOW]
- `POST /api/admin/posts`：创建文章 [MEDIUM]
- `POST /api/admin/posts/ai/meta/generate`：生成文章 Slug/摘要/SEO 元信息 [MEDIUM]
- `GET /api/admin/posts/{postId}`：查询后台文章详情 [LOW]
- `PUT /api/admin/posts/{postId}`：更新文章 [MEDIUM]
- `DELETE /api/admin/posts/{postId}`：删除文章 [HIGH]
- `GET /api/admin/site-config`：查询站点配置 [LOW]
- `PUT /api/admin/site-config`：更新站点配置 [MEDIUM]
- `GET /api/admin/site/ai/providers`：查询后台 AI 提供商配置 [LOW]
- `PUT /api/admin/site/ai/providers`：保存后台 AI 提供商配置 [MEDIUM]
- `POST /api/admin/site/ai/providers/test`：测试 AI 提供商连接 [MEDIUM]
- `GET /api/admin/site/homepage-config`：查询首页配置 [LOW]
- `PUT /api/admin/site/homepage-config`：更新首页配置 [MEDIUM]
- `GET /api/admin/site/storage/s3`：查询后台 S3 配置 [LOW]
- `PUT /api/admin/site/storage/s3`：保存后台 S3 配置 [MEDIUM]
- `POST /api/admin/site/storage/s3/test`：测试 S3 配置连接 [MEDIUM]
- `GET /api/admin/tags`：查询后台标签列表 [LOW]
- `POST /api/admin/tags`：创建标签 [MEDIUM]
- `GET /api/admin/tags/{tagId}`：查询单个标签详情 [LOW]
- `PUT /api/admin/tags/{tagId}`：更新标签 [MEDIUM]
- `DELETE /api/admin/tags/{tagId}`：删除标签 [HIGH]
- `GET /api/admin/topics`：查询后台专题列表 [LOW]
- `POST /api/admin/topics`：创建专题 [MEDIUM]
- `GET /api/admin/topics/{topicId}`：查询单个专题详情 [LOW]
- `PUT /api/admin/topics/{topicId}`：更新专题 [MEDIUM]
- `DELETE /api/admin/topics/{topicId}`：删除专题 [HIGH]
- `GET /api/admin/users`：查询后台用户列表 [LOW]
- `POST /api/admin/users`：创建用户 [MEDIUM]
- `GET /api/admin/users/{userId}`：查询单个用户详情 [LOW]
- `PUT /api/admin/users/{userId}`：更新用户 [MEDIUM]
- `DELETE /api/admin/users/{userId}`：删除用户 [HIGH]

## 后台 AI 提供商配置

### GET /api/admin/site/ai/providers

- 摘要：查询后台 AI 提供商配置
- `operationId`：`getConfig`
- 风险等级：`LOW`

#### 参数

- 无路径参数或查询参数

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminSiteAiProviderConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.activeProfileKey` (string; 可选; 示例: openai-main)：当前启用的 AI 配置键。
- `data.profiles` (array<AdminSiteAiProviderProfileResponse>; 可选)：AI 提供商配置列表。
### PUT /api/admin/site/ai/providers

- 摘要：保存后台 AI 提供商配置
- `operationId`：`updateConfig`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminSiteAiProviderConfigUpdateRequest`
- `activeProfileKey` (string; 可选; 示例: openai-main)：当前启用的 AI 配置键。可不传；不传时后端会自动取 `enabled=true` 的那一项。若传入，必须与唯一启用的配置项一致。
- `profiles` (array<AdminSiteAiProviderProfileRequest>; 可选)：AI 提供商配置列表。保存时至少提供一个配置项，且必须且只能有一个 `enabled=true`。
- `profiles[].profileKey` (string; 可选; 示例: openai-main)：AI 配置键。建议使用稳定且可读的英文标识。
- `profiles[].name` (string; 可选; 示例: OpenAI 主配置)：AI 配置名称。用于后台展示与人工区分。
- `profiles[].enabled` (boolean; 可选; 示例: true)：该配置是否启用。保存整组配置时必须且只能有一个为 true。
- `profiles[].upstreamBaseUrl` (string; 可选; 示例: https://api.openai.com)：上游 AI 服务基础地址。通常不包含具体接口路径。
- `profiles[].apiKey` (string; 可选; 示例: sk-xxxxxxxxxxxxxxxx)：上游 AI 服务 API Key。保存或测试连接时必须提供完整明文值。
- `profiles[].model` (string; 可选; 示例: gpt-4.1-mini)：默认模型名称。文章元信息生成等场景未显式覆盖模型时，会优先使用这里的值。
- `profiles[].upstreamProtocol` (string; 可选; 枚举: COMPLETIONS, RESPONSES; 示例: COMPLETIONS)：上游协议类型。COMPLETIONS=走 `/v1/chat/completions`，RESPONSES=走 `/v1/responses`。文章元信息生成当前要求使用 COMPLETIONS。
- `profiles[].connectTimeoutMs` (integer; 可选; 示例: 3000)：连接超时时间，单位毫秒。有效范围 100-120000；未传时默认 3000。
- `profiles[].readTimeoutMs` (integer; 可选; 示例: 15000)：读取超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。
- `profiles[].writeTimeoutMs` (integer; 可选; 示例: 15000)：写入超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。
- `profiles[].maxTokens` (integer; 可选; 示例: 800)：默认最大输出 token 数。有效范围 1-128000；未传时默认 800。
- `profiles[].temperature` (number; 可选; 示例: 0.4)：默认采样温度。有效范围 0-2；未传时默认 0.4。

#### 响应

- `200`；ApiResponseAdminSiteAiProviderConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.activeProfileKey` (string; 可选; 示例: openai-main)：当前启用的 AI 配置键。
- `data.profiles` (array<AdminSiteAiProviderProfileResponse>; 可选)：AI 提供商配置列表。
### POST /api/admin/site/ai/providers/test

- 摘要：测试 AI 提供商连接
- `operationId`：`testConnection`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminSiteAiProviderProfileRequest`
- `profileKey` (string; 可选; 示例: openai-main)：AI 配置键。建议使用稳定且可读的英文标识。
- `name` (string; 可选; 示例: OpenAI 主配置)：AI 配置名称。用于后台展示与人工区分。
- `enabled` (boolean; 可选; 示例: true)：该配置是否启用。保存整组配置时必须且只能有一个为 true。
- `upstreamBaseUrl` (string; 可选; 示例: https://api.openai.com)：上游 AI 服务基础地址。通常不包含具体接口路径。
- `apiKey` (string; 可选; 示例: sk-xxxxxxxxxxxxxxxx)：上游 AI 服务 API Key。保存或测试连接时必须提供完整明文值。
- `model` (string; 可选; 示例: gpt-4.1-mini)：默认模型名称。文章元信息生成等场景未显式覆盖模型时，会优先使用这里的值。
- `upstreamProtocol` (string; 可选; 枚举: COMPLETIONS, RESPONSES; 示例: COMPLETIONS)：上游协议类型。COMPLETIONS=走 `/v1/chat/completions`，RESPONSES=走 `/v1/responses`。文章元信息生成当前要求使用 COMPLETIONS。
- `connectTimeoutMs` (integer; 可选; 示例: 3000)：连接超时时间，单位毫秒。有效范围 100-120000；未传时默认 3000。
- `readTimeoutMs` (integer; 可选; 示例: 15000)：读取超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。
- `writeTimeoutMs` (integer; 可选; 示例: 15000)：写入超时时间，单位毫秒。有效范围 100-120000；未传时默认 15000。
- `maxTokens` (integer; 可选; 示例: 800)：默认最大输出 token 数。有效范围 1-128000；未传时默认 800。
- `temperature` (number; 可选; 示例: 0.4)：默认采样温度。有效范围 0-2；未传时默认 0.4。

#### 响应

- `200`；ApiResponseAdminSiteAiProviderConnectionTestResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.success` (boolean; 可选; 示例: true)：连接测试是否成功。
- `data.message` (string; 可选; 示例: 连接测试成功)：连接测试结果说明。

## 后台专题管理

### GET /api/admin/topics

- 摘要：查询后台专题列表
- `operationId`：`listTopics`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminTopicQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选)
- `status` (string; 可选)
- `pageNo` (integer; 可选)
- `pageSize` (integer; 可选)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminTopicListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminTopicItemResponse>; 可选)
- `data.total` (integer; 可选)
- `data.pageNo` (integer; 可选)
- `data.pageSize` (integer; 可选)
- `data.totalPages` (integer; 可选)
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选)
- `data.list[].name` (string; 可选)
- `data.list[].slug` (string; 可选)
- `data.list[].description` (string; 可选)
- `data.list[].coverUrl` (string; 可选)
- `data.list[].status` (string; 可选)
- `data.list[].sortOrder` (integer; 可选)
- `data.list[].relatedPostCount` (integer; 可选)
- `data.list[].createdTime` (string; 可选)
- `data.list[].updatedTime` (string; 可选)
### POST /api/admin/topics

- 摘要：创建专题
- `operationId`：`createTopic`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminTopicCreateRequest`
- `name` (string; 必填; 示例: Nuxt 工程实践)：专题名称。创建专题时必填。
- `slug` (string; 必填; 示例: nuxt-engineering-practice)：专题 slug。建议使用小写英文和连字符。
- `description` (string; 可选; 示例: 持续收录 Nuxt 项目的工程化实践与部署经验。)：专题简介。可选。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/topics/nuxt.png)：专题封面图片地址。可选。
- `sortOrder` (integer; 可选; 示例: 20)：专题排序值。数值越小越靠前。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：专题状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminTopicItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### GET /api/admin/topics/{topicId}

- 摘要：查询单个专题详情
- `operationId`：`getTopic`
- 风险等级：`LOW`

#### 参数

- `topicId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminTopicItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### PUT /api/admin/topics/{topicId}

- 摘要：更新专题
- `operationId`：`updateTopic`
- 风险等级：`MEDIUM`

#### 参数

- `topicId` (path; integer; 必填)

#### 请求体

- Schema：`AdminTopicUpdateRequest`
- `name` (string; 必填; 示例: Nuxt 工程实践)：专题名称。更新专题时必填。
- `slug` (string; 必填; 示例: nuxt-engineering-practice)：专题 slug。更新时必填。
- `description` (string; 可选; 示例: 持续收录 Nuxt 项目的工程化实践与部署经验。)：专题简介。可选。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/topics/nuxt.png)：专题封面图片地址。可选。
- `sortOrder` (integer; 可选; 示例: 20)：专题排序值。数值越小越靠前。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：专题状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminTopicItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### DELETE /api/admin/topics/{topicId}

- 摘要：删除专题
- `operationId`：`deleteTopic`
- 风险等级：`HIGH`

#### 参数

- `topicId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台分类管理

### GET /api/admin/categories

- 摘要：查询后台分类列表
- `operationId`：`listCategories`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminCategoryQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选)
- `status` (string; 可选)
- `pageNo` (integer; 可选)
- `pageSize` (integer; 可选)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminCategoryListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminCategoryItemResponse>; 可选)
- `data.total` (integer; 可选)
- `data.pageNo` (integer; 可选)
- `data.pageSize` (integer; 可选)
- `data.totalPages` (integer; 可选)
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选)
- `data.list[].name` (string; 可选)
- `data.list[].slug` (string; 可选)
- `data.list[].description` (string; 可选)
- `data.list[].coverUrl` (string; 可选)
- `data.list[].status` (string; 可选)
- `data.list[].sortOrder` (integer; 可选)
- `data.list[].relatedPostCount` (integer; 可选)
- `data.list[].createdTime` (string; 可选)
- `data.list[].updatedTime` (string; 可选)
### POST /api/admin/categories

- 摘要：创建分类
- `operationId`：`createCategory`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminCategoryCreateRequest`
- `name` (string; 必填; 示例: 后端开发)：分类名称。创建分类时必填。
- `slug` (string; 必填; 示例: backend-development)：分类 slug。建议使用小写英文和连字符。
- `description` (string; 可选; 示例: 记录 Spring Boot、数据库与服务端工程实践。)：分类描述。可选。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/categories/backend.png)：分类封面图片地址。可选。
- `sortOrder` (integer; 可选; 示例: 10)：分类排序值。数值越小越靠前。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：分类状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminCategoryItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### GET /api/admin/categories/{categoryId}

- 摘要：查询单个分类详情
- `operationId`：`getCategory`
- 风险等级：`LOW`

#### 参数

- `categoryId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminCategoryItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### PUT /api/admin/categories/{categoryId}

- 摘要：更新分类
- `operationId`：`updateCategory`
- 风险等级：`MEDIUM`

#### 参数

- `categoryId` (path; integer; 必填)

#### 请求体

- Schema：`AdminCategoryUpdateRequest`
- `name` (string; 必填; 示例: 后端开发)：分类名称。更新分类时必填。
- `slug` (string; 必填; 示例: backend-development)：分类 slug。更新时必填。
- `description` (string; 可选; 示例: 记录 Spring Boot、数据库与服务端工程实践。)：分类描述。可选。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/categories/backend.png)：分类封面图片地址。可选。
- `sortOrder` (integer; 可选; 示例: 10)：分类排序值。数值越小越靠前。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：分类状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminCategoryItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### DELETE /api/admin/categories/{categoryId}

- 摘要：删除分类
- `operationId`：`deleteCategory`
- 风险等级：`HIGH`

#### 参数

- `categoryId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台友链管理

### GET /api/admin/friend-links

- 摘要：查询后台友链列表
- `operationId`：`listFriendLinks`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminFriendLinkQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选; 示例: IdeaFlow)：关键词。可匹配站点名称、站点地址、联系人等文本字段。
- `status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `pageNo` (integer; 可选; 示例: 1)：页码。最小为 1。
- `pageSize` (integer; 可选; 示例: 10)：每页条数。范围 1-100。

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminFriendLinkListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminFriendLinkItemResponse>; 可选)：友链列表数据。
- `data.total` (integer; 可选; 示例: 24)：总记录数。
- `data.pageNo` (integer; 可选; 示例: 1)：当前页码。
- `data.pageSize` (integer; 可选; 示例: 10)：当前每页条数。
- `data.totalPages` (integer; 可选; 示例: 3)：总页数。
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选; 示例: 9)：友链 ID。
- `data.list[].siteName` (string; 可选; 示例: IdeaFlow)：站点名称。
- `data.list[].siteUrl` (string; 可选; 示例: https://ideaflow.example.com)：站点地址。
- `data.list[].logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 地址。
- `data.list[].description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。
- `data.list[].contactName` (string; 可选; 示例: 王小明)：联系人名称。
- `data.list[].contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。
- `data.list[].contactMessage` (string; 可选; 示例: 欢迎互链。)：申请留言或备注。
- `data.list[].themeColor` (string; 可选; 示例: #2563EB)：站点主题色。
- `data.list[].sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前。
- `data.list[].status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `data.list[].createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.list[].updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### POST /api/admin/friend-links

- 摘要：创建友链
- `operationId`：`createFriendLink`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminFriendLinkCreateRequest`
- `siteName` (string; 必填; 示例: IdeaFlow)：友链站点名称。创建友链时必填。
- `siteUrl` (string; 必填; 示例: https://ideaflow.example.com)：友链站点地址。必须以 http:// 或 https:// 开头。
- `logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 或图标地址。可为空。
- `description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。用于后台展示和前台友链卡片说明。
- `contactName` (string; 可选; 示例: 王小明)：联系人名称。可为空。
- `contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。可为空。
- `contactMessage` (string; 可选; 示例: 已在本站添加贵站友链，欢迎互链。)：申请留言或备注。可为空。
- `themeColor` (string; 可选; 示例: #2563EB)：站点主题色。格式必须为 #RRGGBB。
- `sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前；未传时按后端默认处理。
- `status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: PENDING)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。

#### 响应

- `200`；ApiResponseAdminFriendLinkItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 9)：友链 ID。
- `data.siteName` (string; 可选; 示例: IdeaFlow)：站点名称。
- `data.siteUrl` (string; 可选; 示例: https://ideaflow.example.com)：站点地址。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 地址。
- `data.description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。
- `data.contactName` (string; 可选; 示例: 王小明)：联系人名称。
- `data.contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。
- `data.contactMessage` (string; 可选; 示例: 欢迎互链。)：申请留言或备注。
- `data.themeColor` (string; 可选; 示例: #2563EB)：站点主题色。
- `data.sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### GET /api/admin/friend-links/{friendLinkId}

- 摘要：查询后台友链详情
- `operationId`：`getFriendLink`
- 风险等级：`LOW`

#### 参数

- `friendLinkId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminFriendLinkItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 9)：友链 ID。
- `data.siteName` (string; 可选; 示例: IdeaFlow)：站点名称。
- `data.siteUrl` (string; 可选; 示例: https://ideaflow.example.com)：站点地址。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 地址。
- `data.description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。
- `data.contactName` (string; 可选; 示例: 王小明)：联系人名称。
- `data.contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。
- `data.contactMessage` (string; 可选; 示例: 欢迎互链。)：申请留言或备注。
- `data.themeColor` (string; 可选; 示例: #2563EB)：站点主题色。
- `data.sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### PUT /api/admin/friend-links/{friendLinkId}

- 摘要：更新友链
- `operationId`：`updateFriendLink`
- 风险等级：`MEDIUM`

#### 参数

- `friendLinkId` (path; integer; 必填)

#### 请求体

- Schema：`AdminFriendLinkUpdateRequest`
- `siteName` (string; 必填; 示例: IdeaFlow)：友链站点名称。更新时必填。
- `siteUrl` (string; 必填; 示例: https://ideaflow.example.com)：友链站点地址。必须以 http:// 或 https:// 开头。
- `logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 或图标地址。可为空。
- `description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。用于后台展示和前台友链卡片说明。
- `contactName` (string; 可选; 示例: 王小明)：联系人名称。可为空。
- `contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。可为空。
- `contactMessage` (string; 可选; 示例: 已在本站添加贵站友链，欢迎互链。)：申请留言或备注。可为空。
- `themeColor` (string; 可选; 示例: #2563EB)：站点主题色。格式必须为 #RRGGBB。
- `sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前；未传时按后端默认处理。
- `status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。

#### 响应

- `200`；ApiResponseAdminFriendLinkItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 9)：友链 ID。
- `data.siteName` (string; 可选; 示例: IdeaFlow)：站点名称。
- `data.siteUrl` (string; 可选; 示例: https://ideaflow.example.com)：站点地址。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 地址。
- `data.description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。
- `data.contactName` (string; 可选; 示例: 王小明)：联系人名称。
- `data.contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。
- `data.contactMessage` (string; 可选; 示例: 欢迎互链。)：申请留言或备注。
- `data.themeColor` (string; 可选; 示例: #2563EB)：站点主题色。
- `data.sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### DELETE /api/admin/friend-links/{friendLinkId}

- 摘要：删除友链
- `operationId`：`deleteFriendLink`
- 风险等级：`HIGH`

#### 参数

- `friendLinkId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK
### PUT /api/admin/friend-links/{friendLinkId}/status

- 摘要：快速更新友链状态
- `operationId`：`updateFriendLinkStatus`
- 风险等级：`MEDIUM`

#### 参数

- `friendLinkId` (path; integer; 必填)

#### 请求体

- Schema：`AdminFriendLinkStatusUpdateRequest`
- `status` (string; 必填; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。

#### 响应

- `200`；ApiResponseAdminFriendLinkItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 9)：友链 ID。
- `data.siteName` (string; 可选; 示例: IdeaFlow)：站点名称。
- `data.siteUrl` (string; 可选; 示例: https://ideaflow.example.com)：站点地址。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/logos/ideaflow.png)：站点 Logo 地址。
- `data.description` (string; 可选; 示例: 专注工程实践与产品思考。)：站点简介。
- `data.contactName` (string; 可选; 示例: 王小明)：联系人名称。
- `data.contactEmail` (string; 可选; 示例: editor@example.com)：联系邮箱。
- `data.contactMessage` (string; 可选; 示例: 欢迎互链。)：申请留言或备注。
- `data.themeColor` (string; 可选; 示例: #2563EB)：站点主题色。
- `data.sortOrder` (integer; 可选; 示例: 10)：排序值。值越小越靠前。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED, OFFLINE; 示例: APPROVED)：友链状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回，OFFLINE=已下线。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。

## 后台文章 AI 能力

### POST /api/admin/posts/ai/meta/generate

- 摘要：生成文章 Slug/摘要/SEO 元信息
- `operationId`：`generateMeta`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminPostAiMetaGenerateRequest`
- `title` (string; 可选; 示例: Spring Boot 与 Nuxt 联调记录)：文章标题。标题和正文至少填写一项；若两者都提供，标题会优先影响 slug 与 SEO 标题生成。
- `contentMarkdown` (string; 可选; 示例: # 一、背景\n\n本文记录 Spring Boot 与 Nuxt 的联调过程。)：文章正文 Markdown。标题和正文至少填写一项；后端在构造提示词时默认只截取前 6000 个字符参与生成。
- `stream` (boolean; 可选; 示例: false)：是否使用 SSE 流式返回 OpenAI Chat 分片。true=流式，false=一次性返回；未传时默认 false。
- `model` (string; 可选; 示例: gpt-4.1-mini)：可选的模型覆盖值。未传时优先使用当前启用的 AI 提供商配置中的默认模型。
- `temperature` (number; 可选; 示例: 0.2)：可选的采样温度。未传时使用当前启用 AI 配置中的默认温度；建议文章元信息生成使用较低温度。
- `maxTokens` (integer; 可选; 示例: 800)：可选的最大输出 token 数。未传时使用当前启用 AI 配置中的默认值；通常无需超过 800。

#### 响应

- `200`；object；OK

## 后台文章管理

### GET /api/admin/posts

- 摘要：查询后台文章列表
- `operationId`：`listPosts`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminPostQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选)
- `status` (string; 可选)
- `categoryId` (integer; 可选)
- `tagId` (integer; 可选)
- `topicId` (integer; 可选)
- `isTop` (integer; 可选)
- `isRecommend` (integer; 可选)
- `allowComment` (integer; 可选)
- `pageNo` (integer; 可选)
- `pageSize` (integer; 可选)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminPostListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminPostItemResponse>; 可选)
- `data.total` (integer; 可选)
- `data.pageNo` (integer; 可选)
- `data.pageSize` (integer; 可选)
- `data.totalPages` (integer; 可选)
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选)
- `data.list[].title` (string; 可选)
- `data.list[].slug` (string; 可选)
- `data.list[].summary` (string; 可选)
- `data.list[].coverUrl` (string; 可选)
- `data.list[].videoUrl` (string; 可选)
- `data.list[].categoryId` (integer; 可选)
- `data.list[].categoryName` (string; 可选)
- `data.list[].tagIds` (array<integer>; 可选)
- `data.list[].tagNames` (array<string>; 可选)
- `data.list[].topicIds` (array<integer>; 可选)
- `data.list[].topicNames` (array<string>; 可选)
- `data.list[].topic` (string; 可选)
- `data.list[].status` (string; 可选)
- `data.list[].isTop` (boolean; 可选)
- `data.list[].isRecommend` (boolean; 可选)
- `data.list[].allowComment` (boolean; 可选)
- `data.list[].seoTitle` (string; 可选)
- `data.list[].seoDescription` (string; 可选)
- `data.list[].coverReady` (boolean; 可选)
- `data.list[].videoReady` (boolean; 可选)
- `data.list[].summaryReady` (boolean; 可选)
- `data.list[].readingMinutes` (integer; 可选)
- `data.list[].wordCount` (integer; 可选)
- `data.list[].contentMarkdown` (string; 可选)
- `data.list[].contentAccessConfig` (ContentAccessConfig; 可选)
- `data.list[].tailHiddenContentMarkdown` (string; 可选)
- `data.list[].updatedAt` (string; 可选)
- `data.list[].publishedAt` (string; 可选)
### POST /api/admin/posts

- 摘要：创建文章
- `operationId`：`createPost`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminPostCreateRequest`
- `title` (string; 必填; 示例: Spring Boot 与 Nuxt 联调记录)：文章标题。新增时必填。
- `slug` (string; 必填; 示例: spring-boot-nuxt-integration-notes)：文章唯一 slug。建议根据标题或正文语义生成，使用小写英文和连字符。
- `summary` (string; 可选; 示例: 记录 Spring Boot 后端与 Nuxt 前端联调过程中的接口联调、鉴权和部署注意事项。)：文章摘要。可由 AI 根据正文自动生成。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/covers/spring-nuxt.png)：文章封面图片地址。没有封面时可留空。
- `videoUrl` (string; 可选; 示例: https://cdn.example.com/videos/demo.mp4)：文章视频地址。仅在需要视频首屏或视频正文时填写。
- `categoryId` (integer; 可选; 示例: 12)：所属分类 ID。可为空；如需填写，应先查询可用分类列表。
- `tagIds` (array<integer>; 可选; 示例: [3, 8])：标签 ID 列表。新增文章前应先查询可用标签。
- `topicIds` (array<integer>; 可选; 示例: [5])：专题 ID 列表。新增文章前应先查询可用专题。
- `status` (string; 可选; 枚举: DRAFT, PUBLISHED, OFFLINE; 示例: DRAFT)：文章状态。DRAFT=草稿，PUBLISHED=发布，OFFLINE=下线。若用户未明确要求发布，建议默认传 DRAFT。
- `isTop` (boolean; 可选; 示例: false)：是否置顶。默认 false。
- `isRecommend` (boolean; 可选; 示例: false)：是否推荐。默认 false。
- `allowComment` (boolean; 可选; 示例: true)：是否允许评论。默认 true。
- `seoTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调完整实践)：SEO 标题。可由 AI 根据标题和正文自动生成。
- `seoDescription` (string; 可选; 示例: 从接口设计、JWT 鉴权到部署配置，完整记录 Spring Boot 与 Nuxt 的联调过程。)：SEO 描述。可由 AI 根据正文自动生成。
- `contentMarkdown` (string; 可选; 示例: # 一、背景\n\n本文记录 Spring Boot 与 Nuxt 的联调过程。)：文章正文 Markdown。新增或更新文章时必填。
- `contentHtml` (string; 可选)：文章正文 HTML。通常由编辑器或服务端渲染生成，不建议手工维护。
- `contentTocJson` (string; 可选)：文章目录 JSON。通常由 Markdown 渲染结果自动生成，不建议手工维护。
- `contentAccessConfig` (ContentAccessConfig; 可选)：内容权限配置。默认不启用；若启用应先明确询问用户是否需要访问控制。
- `contentAccessConfig.version` (integer; 可选; 示例: 1)：配置版本号，当前默认传 1
- `contentAccessConfig.articleAccess` (ContentAccessArticleConfig; 可选)：整篇文章访问控制配置。默认不启用；若启用访问码规则，必须同时提供文章访问码和提示文案。
- `contentAccessConfig.articleAccess.enabled` (boolean; 可选; 示例: false)：是否启用整篇文章访问控制。默认 false。
- `contentAccessConfig.articleAccess.ruleTypes` (array<string>; 可选; 示例: ["LOGIN"])：文章访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。
- `contentAccessConfig.articleAccess.articleAccessCode` (string; 可选; 示例: spring-2026)：文章访问码。当规则中包含 ACCESS_CODE 时必填。
- `contentAccessConfig.articleAccess.articleAccessCodeHint` (string; 可选; 示例: 请输入文章访问码后查看全文)：文章访问码提示文案。当规则中包含 ACCESS_CODE 时建议填写，便于前台提示用户。
- `contentAccessConfig.tailHiddenAccess` (ContentAccessTailHiddenConfig; 可选)：文章尾部隐藏内容访问控制配置。默认不启用；若启用则还需要填写隐藏内容标题、规则和隐藏正文。
- `contentAccessConfig.tailHiddenAccess.enabled` (boolean; 可选; 示例: false)：是否启用尾部隐藏内容。默认 false。
- `contentAccessConfig.tailHiddenAccess.title` (string; 可选; 示例: 进阶扩展内容)：隐藏内容模块标题。启用尾部隐藏内容时必填。
- `contentAccessConfig.tailHiddenAccess.ruleTypes` (array<string>; 可选; 示例: ["LOGIN"])：尾部隐藏内容访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。
- `tailHiddenContentMarkdown` (string; 可选; 示例: ## 扩展阅读\n\n这里是需要解锁后查看的内容。)：尾部隐藏内容 Markdown。只有启用了尾部隐藏内容时才需要填写。
- `tailHiddenContentHtml` (string; 可选)：尾部隐藏内容 HTML。通常由 Markdown 渲染自动生成，不建议手工维护。

#### 响应

- `200`；ApiResponseAdminPostItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.title` (string; 可选)
- `data.slug` (string; 可选)
- `data.summary` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.videoUrl` (string; 可选)
- `data.categoryId` (integer; 可选)
- `data.categoryName` (string; 可选)
- `data.tagIds` (array<integer>; 可选)
- `data.tagNames` (array<string>; 可选)
- `data.topicIds` (array<integer>; 可选)
- `data.topicNames` (array<string>; 可选)
- `data.topic` (string; 可选)
- `data.status` (string; 可选)
- `data.isTop` (boolean; 可选)
- `data.isRecommend` (boolean; 可选)
- `data.allowComment` (boolean; 可选)
- `data.seoTitle` (string; 可选)
- `data.seoDescription` (string; 可选)
- `data.coverReady` (boolean; 可选)
- `data.videoReady` (boolean; 可选)
- `data.summaryReady` (boolean; 可选)
- `data.readingMinutes` (integer; 可选)
- `data.wordCount` (integer; 可选)
- `data.contentMarkdown` (string; 可选)
- `data.contentAccessConfig` (ContentAccessConfig; 可选)
- `data.tailHiddenContentMarkdown` (string; 可选)
- `data.updatedAt` (string; 可选)
- `data.publishedAt` (string; 可选)
### GET /api/admin/posts/{postId}

- 摘要：查询后台文章详情
- `operationId`：`getPost`
- 风险等级：`LOW`

#### 参数

- `postId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminPostItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.title` (string; 可选)
- `data.slug` (string; 可选)
- `data.summary` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.videoUrl` (string; 可选)
- `data.categoryId` (integer; 可选)
- `data.categoryName` (string; 可选)
- `data.tagIds` (array<integer>; 可选)
- `data.tagNames` (array<string>; 可选)
- `data.topicIds` (array<integer>; 可选)
- `data.topicNames` (array<string>; 可选)
- `data.topic` (string; 可选)
- `data.status` (string; 可选)
- `data.isTop` (boolean; 可选)
- `data.isRecommend` (boolean; 可选)
- `data.allowComment` (boolean; 可选)
- `data.seoTitle` (string; 可选)
- `data.seoDescription` (string; 可选)
- `data.coverReady` (boolean; 可选)
- `data.videoReady` (boolean; 可选)
- `data.summaryReady` (boolean; 可选)
- `data.readingMinutes` (integer; 可选)
- `data.wordCount` (integer; 可选)
- `data.contentMarkdown` (string; 可选)
- `data.contentAccessConfig` (ContentAccessConfig; 可选)
- `data.tailHiddenContentMarkdown` (string; 可选)
- `data.updatedAt` (string; 可选)
- `data.publishedAt` (string; 可选)
### PUT /api/admin/posts/{postId}

- 摘要：更新文章
- `operationId`：`updatePost`
- 风险等级：`MEDIUM`

#### 参数

- `postId` (path; integer; 必填)

#### 请求体

- Schema：`AdminPostUpdateRequest`
- `title` (string; 必填; 示例: Spring Boot 与 Nuxt 联调记录)：文章标题。更新时必填。
- `slug` (string; 必填; 示例: spring-boot-nuxt-integration-notes)：文章唯一 slug。更新时必填，通常应在原有值基础上调整。
- `summary` (string; 可选; 示例: 记录 Spring Boot 后端与 Nuxt 前端联调过程中的接口联调、鉴权和部署注意事项。)：文章摘要。可由 AI 根据正文自动生成或重写。
- `coverUrl` (string; 可选; 示例: https://cdn.example.com/covers/spring-nuxt.png)：文章封面图片地址。没有封面时可留空。
- `videoUrl` (string; 可选; 示例: https://cdn.example.com/videos/demo.mp4)：文章视频地址。仅在需要视频首屏或视频正文时填写。
- `categoryId` (integer; 可选; 示例: 12)：所属分类 ID。可为空；如需修改应先查询可用分类列表。
- `tagIds` (array<integer>; 可选; 示例: [3, 8])：标签 ID 列表。更新前建议先读取当前文章详情，再合并目标标签。
- `topicIds` (array<integer>; 可选; 示例: [5])：专题 ID 列表。更新前建议先读取当前文章详情，再合并目标专题。
- `status` (string; 可选; 枚举: DRAFT, PUBLISHED, OFFLINE; 示例: DRAFT)：文章状态。DRAFT=草稿，PUBLISHED=发布，OFFLINE=下线。更新时若用户未明确要改状态，应保留原值。
- `isTop` (boolean; 可选; 示例: false)：是否置顶。默认 false。
- `isRecommend` (boolean; 可选; 示例: false)：是否推荐。默认 false。
- `allowComment` (boolean; 可选; 示例: true)：是否允许评论。默认 true。
- `seoTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调完整实践)：SEO 标题。可由 AI 根据标题和正文自动生成。
- `seoDescription` (string; 可选; 示例: 从接口设计、JWT 鉴权到部署配置，完整记录 Spring Boot 与 Nuxt 的联调过程。)：SEO 描述。可由 AI 根据正文自动生成。
- `contentMarkdown` (string; 可选; 示例: # 一、背景\n\n本文记录 Spring Boot 与 Nuxt 的联调过程。)：文章正文 Markdown。更新文章时必填。
- `contentHtml` (string; 可选)：文章正文 HTML。通常由编辑器或服务端渲染生成，不建议手工维护。
- `contentTocJson` (string; 可选)：文章目录 JSON。通常由 Markdown 渲染结果自动生成，不建议手工维护。
- `contentAccessConfig` (ContentAccessConfig; 可选)：内容权限配置。默认不启用；若启用应先明确询问用户是否需要访问控制。
- `contentAccessConfig.version` (integer; 可选; 示例: 1)：配置版本号，当前默认传 1
- `contentAccessConfig.articleAccess` (ContentAccessArticleConfig; 可选)：整篇文章访问控制配置。默认不启用；若启用访问码规则，必须同时提供文章访问码和提示文案。
- `contentAccessConfig.articleAccess.enabled` (boolean; 可选; 示例: false)：是否启用整篇文章访问控制。默认 false。
- `contentAccessConfig.articleAccess.ruleTypes` (array<string>; 可选; 示例: ["LOGIN"])：文章访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。
- `contentAccessConfig.articleAccess.articleAccessCode` (string; 可选; 示例: spring-2026)：文章访问码。当规则中包含 ACCESS_CODE 时必填。
- `contentAccessConfig.articleAccess.articleAccessCodeHint` (string; 可选; 示例: 请输入文章访问码后查看全文)：文章访问码提示文案。当规则中包含 ACCESS_CODE 时建议填写，便于前台提示用户。
- `contentAccessConfig.tailHiddenAccess` (ContentAccessTailHiddenConfig; 可选)：文章尾部隐藏内容访问控制配置。默认不启用；若启用则还需要填写隐藏内容标题、规则和隐藏正文。
- `contentAccessConfig.tailHiddenAccess.enabled` (boolean; 可选; 示例: false)：是否启用尾部隐藏内容。默认 false。
- `contentAccessConfig.tailHiddenAccess.title` (string; 可选; 示例: 进阶扩展内容)：隐藏内容模块标题。启用尾部隐藏内容时必填。
- `contentAccessConfig.tailHiddenAccess.ruleTypes` (array<string>; 可选; 示例: ["LOGIN"])：尾部隐藏内容访问规则列表。支持 LOGIN、WECHAT_ACCESS_CODE、ACCESS_CODE。
- `tailHiddenContentMarkdown` (string; 可选; 示例: ## 扩展阅读\n\n这里是需要解锁后查看的内容。)：尾部隐藏内容 Markdown。只有启用了尾部隐藏内容时才需要填写。
- `tailHiddenContentHtml` (string; 可选)：尾部隐藏内容 HTML。通常由 Markdown 渲染自动生成，不建议手工维护。

#### 响应

- `200`；ApiResponseAdminPostItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.title` (string; 可选)
- `data.slug` (string; 可选)
- `data.summary` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.videoUrl` (string; 可选)
- `data.categoryId` (integer; 可选)
- `data.categoryName` (string; 可选)
- `data.tagIds` (array<integer>; 可选)
- `data.tagNames` (array<string>; 可选)
- `data.topicIds` (array<integer>; 可选)
- `data.topicNames` (array<string>; 可选)
- `data.topic` (string; 可选)
- `data.status` (string; 可选)
- `data.isTop` (boolean; 可选)
- `data.isRecommend` (boolean; 可选)
- `data.allowComment` (boolean; 可选)
- `data.seoTitle` (string; 可选)
- `data.seoDescription` (string; 可选)
- `data.coverReady` (boolean; 可选)
- `data.videoReady` (boolean; 可选)
- `data.summaryReady` (boolean; 可选)
- `data.readingMinutes` (integer; 可选)
- `data.wordCount` (integer; 可选)
- `data.contentMarkdown` (string; 可选)
- `data.contentAccessConfig` (ContentAccessConfig; 可选)
- `data.tailHiddenContentMarkdown` (string; 可选)
- `data.updatedAt` (string; 可选)
- `data.publishedAt` (string; 可选)
### DELETE /api/admin/posts/{postId}

- 摘要：删除文章
- `operationId`：`deletePost`
- 风险等级：`HIGH`

#### 参数

- `postId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台标签管理

### GET /api/admin/tags

- 摘要：查询后台标签列表
- `operationId`：`listTags`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminTagQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选)
- `status` (string; 可选)
- `pageNo` (integer; 可选)
- `pageSize` (integer; 可选)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminTagListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminTagItemResponse>; 可选)
- `data.total` (integer; 可选)
- `data.pageNo` (integer; 可选)
- `data.pageSize` (integer; 可选)
- `data.totalPages` (integer; 可选)
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选)
- `data.list[].name` (string; 可选)
- `data.list[].slug` (string; 可选)
- `data.list[].description` (string; 可选)
- `data.list[].coverUrl` (string; 可选)
- `data.list[].status` (string; 可选)
- `data.list[].sortOrder` (integer; 可选)
- `data.list[].relatedPostCount` (integer; 可选)
- `data.list[].createdTime` (string; 可选)
- `data.list[].updatedTime` (string; 可选)
### POST /api/admin/tags

- 摘要：创建标签
- `operationId`：`createTag`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminTagCreateRequest`
- `name` (string; 必填; 示例: Spring Boot)：标签名称。创建标签时必填。
- `slug` (string; 必填; 示例: spring-boot)：标签 slug。建议使用小写英文和连字符。
- `description` (string; 可选; 示例: 与 Spring Boot 相关的实践记录。)：标签描述。可选。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：标签状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminTagItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### GET /api/admin/tags/{tagId}

- 摘要：查询单个标签详情
- `operationId`：`getTag`
- 风险等级：`LOW`

#### 参数

- `tagId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminTagItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### PUT /api/admin/tags/{tagId}

- 摘要：更新标签
- `operationId`：`updateTag`
- 风险等级：`MEDIUM`

#### 参数

- `tagId` (path; integer; 必填)

#### 请求体

- Schema：`AdminTagUpdateRequest`
- `name` (string; 必填; 示例: Spring Boot)：标签名称。更新标签时必填。
- `slug` (string; 必填; 示例: spring-boot)：标签 slug。更新时必填。
- `description` (string; 可选; 示例: 与 Spring Boot 相关的实践记录。)：标签描述。可选。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：标签状态。ACTIVE=启用，DISABLED=停用。

#### 响应

- `200`；ApiResponseAdminTagItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.name` (string; 可选)
- `data.slug` (string; 可选)
- `data.description` (string; 可选)
- `data.coverUrl` (string; 可选)
- `data.status` (string; 可选)
- `data.sortOrder` (integer; 可选)
- `data.relatedPostCount` (integer; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### DELETE /api/admin/tags/{tagId}

- 摘要：删除标签
- `operationId`：`deleteTag`
- 风险等级：`HIGH`

#### 参数

- `tagId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台用户管理

### GET /api/admin/users

- 摘要：查询后台用户列表
- `operationId`：`listUsers`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminUserQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选)
- `role` (string; 可选)
- `status` (string; 可选)
- `pageNo` (integer; 可选)
- `pageSize` (integer; 可选)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminUserListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminUserItemResponse>; 可选)
- `data.total` (integer; 可选)
- `data.pageNo` (integer; 可选)
- `data.pageSize` (integer; 可选)
- `data.totalPages` (integer; 可选)
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选)
- `data.list[].email` (string; 可选)
- `data.list[].userName` (string; 可选)
- `data.list[].avatarUrl` (string; 可选)
- `data.list[].role` (string; 可选)
- `data.list[].status` (string; 可选)
- `data.list[].lastLoginAt` (string; 可选)
- `data.list[].lastLoginIp` (string; 可选)
- `data.list[].createdTime` (string; 可选)
- `data.list[].updatedTime` (string; 可选)
### POST /api/admin/users

- 摘要：创建用户
- `operationId`：`createUser`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminUserCreateRequest`
- `email` (string; 必填; 示例: editor@example.com)：用户邮箱。系统内必须唯一。
- `userName` (string; 必填; 示例: 内容编辑小王)：用户名。用于后台与前台展示。
- `avatarUrl` (string; 可选; 示例: https://cdn.example.com/avatars/editor.png)：头像图片地址。可为空。
- `password` (string; 必填; 示例: yunyu123456)：登录密码。创建用户时必填，长度需在 6-32 个字符之间。
- `role` (string; 可选; 枚举: SUPER_ADMIN, USER; 示例: USER)：用户角色。SUPER_ADMIN=站长，USER=普通用户。未明确授权时不要默认创建 SUPER_ADMIN。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：用户状态。ACTIVE=启用，DISABLED=禁用。

#### 响应

- `200`；ApiResponseAdminUserItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.email` (string; 可选)
- `data.userName` (string; 可选)
- `data.avatarUrl` (string; 可选)
- `data.role` (string; 可选)
- `data.status` (string; 可选)
- `data.lastLoginAt` (string; 可选)
- `data.lastLoginIp` (string; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### GET /api/admin/users/{userId}

- 摘要：查询单个用户详情
- `operationId`：`getUser`
- 风险等级：`LOW`

#### 参数

- `userId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminUserItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.email` (string; 可选)
- `data.userName` (string; 可选)
- `data.avatarUrl` (string; 可选)
- `data.role` (string; 可选)
- `data.status` (string; 可选)
- `data.lastLoginAt` (string; 可选)
- `data.lastLoginIp` (string; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### PUT /api/admin/users/{userId}

- 摘要：更新用户
- `operationId`：`updateUser`
- 风险等级：`MEDIUM`

#### 参数

- `userId` (path; integer; 必填)

#### 请求体

- Schema：`AdminUserUpdateRequest`
- `email` (string; 必填; 示例: editor@example.com)：用户邮箱。更新后仍需保持全站唯一。
- `userName` (string; 必填; 示例: 内容编辑小王)：用户名。用于后台与前台展示。
- `avatarUrl` (string; 可选; 示例: https://cdn.example.com/avatars/editor.png)：头像图片地址。可为空。
- `password` (string; 可选; 示例: yunyu123456)：登录密码。可为空；仅在需要重置密码时填写，长度需在 6-32 个字符之间。
- `role` (string; 可选; 枚举: SUPER_ADMIN, USER; 示例: USER)：用户角色。SUPER_ADMIN=站长，USER=普通用户。调整为 SUPER_ADMIN 前应确认授权。
- `status` (string; 可选; 枚举: ACTIVE, DISABLED; 示例: ACTIVE)：用户状态。ACTIVE=启用，DISABLED=禁用。

#### 响应

- `200`；ApiResponseAdminUserItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选)
- `data.email` (string; 可选)
- `data.userName` (string; 可选)
- `data.avatarUrl` (string; 可选)
- `data.role` (string; 可选)
- `data.status` (string; 可选)
- `data.lastLoginAt` (string; 可选)
- `data.lastLoginIp` (string; 可选)
- `data.createdTime` (string; 可选)
- `data.updatedTime` (string; 可选)
### DELETE /api/admin/users/{userId}

- 摘要：删除用户
- `operationId`：`deleteUser`
- 风险等级：`HIGH`

#### 参数

- `userId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台站点存储配置

### GET /api/admin/site/storage/s3

- 摘要：查询后台 S3 配置
- `operationId`：`getS3Config`
- 风险等级：`LOW`

#### 参数

- 无路径参数或查询参数

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminSiteStorageS3ConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.activeProfileKey` (string; 可选; 示例: r2-main)：当前启用的 S3 配置键。
- `data.profiles` (array<AdminSiteStorageS3ProfileResponse>; 可选)：S3 配置列表。
### PUT /api/admin/site/storage/s3

- 摘要：保存后台 S3 配置
- `operationId`：`updateS3Config`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminSiteStorageS3ConfigUpdateRequest`
- `activeProfileKey` (string; 可选; 示例: r2-main)：当前启用的 S3 配置键。可不传；不传时后端会自动取 `enabled=true` 的那一项。若传入，必须与唯一启用的配置项一致。
- `profiles` (array<AdminSiteStorageS3ProfileRequest>; 可选)：S3 配置列表。保存时至少提供一个配置项，且必须且只能有一个 `enabled=true`。
- `profiles[].profileKey` (string; 可选; 示例: r2-main)：S3 配置键。建议使用稳定且可读的英文标识。
- `profiles[].name` (string; 可选; 示例: Cloudflare R2 主配置)：S3 配置名称。用于后台展示与人工区分。
- `profiles[].enabled` (boolean; 可选; 示例: true)：该配置是否启用。保存整组配置时必须且只能有一个为 true。
- `profiles[].endpoint` (string; 可选; 示例: https://<account-id>.r2.cloudflarestorage.com)：S3 Endpoint 地址。通常为对象存储服务提供的 API 入口。
- `profiles[].region` (string; 可选; 示例: auto)：S3 区域标识。未传时默认 auto。
- `profiles[].bucket` (string; 可选; 示例: yunyu-assets)：目标 Bucket 名称。保存或测试连接时必填。
- `profiles[].accessKey` (string; 可选; 示例: AKIAIOSFODNN7EXAMPLE)：S3 Access Key。保存或测试连接时必须提供完整明文值。
- `profiles[].secretKey` (string; 可选; 示例: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY)：S3 Secret Key。保存或测试连接时必须提供完整明文值。
- `profiles[].pathStyleAccess` (boolean; 可选; 示例: false)：是否启用 Path Style 访问。多数兼容型对象存储需要按实际厂商要求设置。未传时默认 false。
- `profiles[].publicBaseUrl` (string; 可选; 示例: https://cdn.example.com)：公开访问基地址。用于拼接对外资源 URL；如走 CDN 或自定义域名，建议填写完整地址。
- `profiles[].presignExpireSeconds` (integer; 可选; 示例: 300)：预签名上传地址有效期，单位秒。有效范围 60-3600；未传时默认 300。
- `profiles[].maxFileSizeMb` (integer; 可选; 示例: 20)：允许上传的最大文件大小，单位 MB。有效范围 1-2048；未传时默认 20。
- `profiles[].allowedContentTypes` (array<string>; 可选; 示例: ["image/png", "image/jpeg", "video/mp4"])：允许上传的 MIME 类型列表。至少配置一个，保存时会自动去重并转为小写。

#### 响应

- `200`；ApiResponseAdminSiteStorageS3ConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.activeProfileKey` (string; 可选; 示例: r2-main)：当前启用的 S3 配置键。
- `data.profiles` (array<AdminSiteStorageS3ProfileResponse>; 可选)：S3 配置列表。
### POST /api/admin/site/storage/s3/test

- 摘要：测试 S3 配置连接
- `operationId`：`testS3Connection`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminSiteStorageS3ProfileRequest`
- `profileKey` (string; 可选; 示例: r2-main)：S3 配置键。建议使用稳定且可读的英文标识。
- `name` (string; 可选; 示例: Cloudflare R2 主配置)：S3 配置名称。用于后台展示与人工区分。
- `enabled` (boolean; 可选; 示例: true)：该配置是否启用。保存整组配置时必须且只能有一个为 true。
- `endpoint` (string; 可选; 示例: https://<account-id>.r2.cloudflarestorage.com)：S3 Endpoint 地址。通常为对象存储服务提供的 API 入口。
- `region` (string; 可选; 示例: auto)：S3 区域标识。未传时默认 auto。
- `bucket` (string; 可选; 示例: yunyu-assets)：目标 Bucket 名称。保存或测试连接时必填。
- `accessKey` (string; 可选; 示例: AKIAIOSFODNN7EXAMPLE)：S3 Access Key。保存或测试连接时必须提供完整明文值。
- `secretKey` (string; 可选; 示例: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY)：S3 Secret Key。保存或测试连接时必须提供完整明文值。
- `pathStyleAccess` (boolean; 可选; 示例: false)：是否启用 Path Style 访问。多数兼容型对象存储需要按实际厂商要求设置。未传时默认 false。
- `publicBaseUrl` (string; 可选; 示例: https://cdn.example.com)：公开访问基地址。用于拼接对外资源 URL；如走 CDN 或自定义域名，建议填写完整地址。
- `presignExpireSeconds` (integer; 可选; 示例: 300)：预签名上传地址有效期，单位秒。有效范围 60-3600；未传时默认 300。
- `maxFileSizeMb` (integer; 可选; 示例: 20)：允许上传的最大文件大小，单位 MB。有效范围 1-2048；未传时默认 20。
- `allowedContentTypes` (array<string>; 可选; 示例: ["image/png", "image/jpeg", "video/mp4"])：允许上传的 MIME 类型列表。至少配置一个，保存时会自动去重并转为小写。

#### 响应

- `200`；ApiResponseAdminSiteStorageS3ConnectionTestResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.success` (boolean; 可选; 示例: true)：连接测试是否成功。
- `data.message` (string; 可选; 示例: 连接成功，可访问目标 Bucket)：连接测试结果说明。

## 后台站点配置

### GET /api/admin/site-config

- 摘要：查询站点配置
- `operationId`：`getSiteConfig`
- 风险等级：`LOW`

#### 参数

- 无路径参数或查询参数

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminSiteConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.siteName` (string; 可选; 示例: 云屿)：站点名称。
- `data.siteSubTitle` (string; 可选; 示例: 记录技术与生活)：站点副标题。
- `data.footerText` (string; 可选; 示例: 持续记录，持续分享。)：页脚文案。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/assets/logo.png)：站点 Logo 地址。
- `data.faviconUrl` (string; 可选; 示例: https://cdn.example.com/assets/favicon.ico)：站点 Favicon 地址。
- `data.defaultTitle` (string; 可选; 示例: 云屿 | 技术与生活)：默认标题模板。
- `data.defaultDescription` (string; 可选; 示例: 记录技术实践、阅读思考与生活感受。)：默认描述。
- `data.primaryColor` (string; 可选; 示例: #2563EB)：站点主色。
- `data.secondaryColor` (string; 可选; 示例: #0F172A)：站点辅助色。
- `data.wechatAccessCodeEnabled` (boolean; 可选; 示例: false)：是否启用公众号验证码访问。
- `data.wechatAccessCode` (string; 可选; 示例: yunyu-2026)：公众号访问验证码。
- `data.wechatAccessCodeHint` (string; 可选; 示例: 关注公众号后回复验证码获取访问权限)：公众号验证码提示文案。
- `data.wechatQrCodeUrl` (string; 可选; 示例: https://cdn.example.com/assets/wechat-qrcode.png)：公众号二维码图片地址。
### PUT /api/admin/site-config

- 摘要：更新站点配置
- `operationId`：`updateSiteConfig`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminSiteConfigUpdateRequest`
- `siteName` (string; 必填; 示例: 云屿)：站点名称。
- `siteSubTitle` (string; 必填; 示例: 记录技术与生活)：站点副标题。
- `footerText` (string; 可选; 示例: 持续记录，持续分享。)：页脚文案。
- `logoUrl` (string; 可选; 示例: https://cdn.example.com/assets/logo.png)：站点 Logo 地址。
- `faviconUrl` (string; 可选; 示例: https://cdn.example.com/assets/favicon.ico)：站点 Favicon 地址。
- `defaultTitle` (string; 必填; 示例: 云屿 | 技术与生活)：站点默认标题模板。
- `defaultDescription` (string; 必填; 示例: 记录技术实践、阅读思考与生活感受。)：站点默认描述。
- `primaryColor` (string; 可选; 示例: #2563EB)：站点主色，格式必须为 #RRGGBB。
- `secondaryColor` (string; 可选; 示例: #0F172A)：站点辅助色，格式必须为 #RRGGBB。
- `wechatAccessCodeEnabled` (boolean; 可选; 示例: false)：是否启用公众号验证码访问。
- `wechatAccessCode` (string; 可选; 示例: yunyu-2026)：公众号访问验证码。仅在启用公众号验证码时填写。
- `wechatAccessCodeHint` (string; 可选; 示例: 关注公众号后回复验证码获取访问权限)：公众号验证码提示文案。
- `wechatQrCodeUrl` (string; 可选; 示例: https://cdn.example.com/assets/wechat-qrcode.png)：公众号二维码图片地址。

#### 响应

- `200`；ApiResponseAdminSiteConfigResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.siteName` (string; 可选; 示例: 云屿)：站点名称。
- `data.siteSubTitle` (string; 可选; 示例: 记录技术与生活)：站点副标题。
- `data.footerText` (string; 可选; 示例: 持续记录，持续分享。)：页脚文案。
- `data.logoUrl` (string; 可选; 示例: https://cdn.example.com/assets/logo.png)：站点 Logo 地址。
- `data.faviconUrl` (string; 可选; 示例: https://cdn.example.com/assets/favicon.ico)：站点 Favicon 地址。
- `data.defaultTitle` (string; 可选; 示例: 云屿 | 技术与生活)：默认标题模板。
- `data.defaultDescription` (string; 可选; 示例: 记录技术实践、阅读思考与生活感受。)：默认描述。
- `data.primaryColor` (string; 可选; 示例: #2563EB)：站点主色。
- `data.secondaryColor` (string; 可选; 示例: #0F172A)：站点辅助色。
- `data.wechatAccessCodeEnabled` (boolean; 可选; 示例: false)：是否启用公众号验证码访问。
- `data.wechatAccessCode` (string; 可选; 示例: yunyu-2026)：公众号访问验证码。
- `data.wechatAccessCodeHint` (string; 可选; 示例: 关注公众号后回复验证码获取访问权限)：公众号验证码提示文案。
- `data.wechatQrCodeUrl` (string; 可选; 示例: https://cdn.example.com/assets/wechat-qrcode.png)：公众号二维码图片地址。

## 后台评论管理

### GET /api/admin/comments

- 摘要：查询后台评论列表
- `operationId`：`listComments_1`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminCommentQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选; 示例: 写得很好)：关键词。可用于匹配评论内容、评论人名称等文本字段。
- `status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED; 示例: PENDING)：评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。
- `postId` (integer; 可选; 示例: 18)：文章 ID。仅查看某篇文章下的评论时填写。
- `userId` (integer; 可选; 示例: 3)：评论用户 ID。用于按评论作者筛选。
- `pageNo` (integer; 可选; 示例: 1)：页码。默认 1。
- `pageSize` (integer; 可选; 示例: 10)：每页条数。默认 10。

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminCommentListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminCommentItemResponse>; 可选)：评论列表数据。
- `data.total` (integer; 可选; 示例: 58)：总记录数。
- `data.pageNo` (integer; 可选; 示例: 1)：当前页码。
- `data.pageSize` (integer; 可选; 示例: 10)：当前每页条数。
- `data.totalPages` (integer; 可选; 示例: 6)：总页数。
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选; 示例: 101)：评论 ID。
- `data.list[].postId` (integer; 可选; 示例: 18)：所属文章 ID。
- `data.list[].postTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调记录)：所属文章标题。
- `data.list[].postSlug` (string; 可选; 示例: spring-boot-nuxt-integration-notes)：所属文章 slug。
- `data.list[].userId` (integer; 可选; 示例: 3)：评论用户 ID。
- `data.list[].userName` (string; 可选; 示例: 内容编辑小王)：评论用户名。
- `data.list[].userEmail` (string; 可选; 示例: editor@example.com)：评论用户邮箱。
- `data.list[].replyCommentId` (integer; 可选; 示例: 88)：直接回复目标评论 ID。根评论时为空。
- `data.list[].rootId` (integer; 可选; 示例: 80)：评论根节点 ID。根评论时通常与当前评论 ID 一致或为空。
- `data.list[].replyToUserName` (string; 可选; 示例: 站长)：被回复用户名称。
- `data.list[].content` (string; 可选; 示例: 这篇文章写得很清楚。)：评论内容。
- `data.list[].status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED; 示例: PENDING)：评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。
- `data.list[].ip` (string; 可选; 示例: 127.0.0.1)：评论来源 IP。
- `data.list[].createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.list[].updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### GET /api/admin/comments/thread-groups

- 摘要：查询后台评论树形审核列表
- `operationId`：`listCommentThreadGroups`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminCommentQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选; 示例: 写得很好)：关键词。可用于匹配评论内容、评论人名称等文本字段。
- `status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED; 示例: PENDING)：评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。
- `postId` (integer; 可选; 示例: 18)：文章 ID。仅查看某篇文章下的评论时填写。
- `userId` (integer; 可选; 示例: 3)：评论用户 ID。用于按评论作者筛选。
- `pageNo` (integer; 可选; 示例: 1)：页码。默认 1。
- `pageSize` (integer; 可选; 示例: 10)：每页条数。默认 10。

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminCommentThreadGroupListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminCommentThreadGroupResponse>; 可选)：按文章分组后的评论树列表。
- `data.total` (integer; 可选; 示例: 8)：总记录数。
- `data.pageNo` (integer; 可选; 示例: 1)：当前页码。
- `data.pageSize` (integer; 可选; 示例: 10)：当前每页条数。
- `data.totalPages` (integer; 可选; 示例: 1)：总页数。
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].postId` (integer; 可选; 示例: 18)：文章 ID。
- `data.list[].postTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调记录)：文章标题。
- `data.list[].postSlug` (string; 可选; 示例: spring-boot-nuxt-integration-notes)：文章 slug。
- `data.list[].totalCommentCount` (integer; 可选; 示例: 24)：该文章下的评论总数。
- `data.list[].pendingCommentCount` (integer; 可选; 示例: 5)：待审核评论数。
- `data.list[].approvedCommentCount` (integer; 可选; 示例: 17)：已通过评论数。
- `data.list[].rejectedCommentCount` (integer; 可选; 示例: 2)：已驳回评论数。
- `data.list[].latestCommentTime` (string; 可选; 示例: 2026-04-26T20:35:00)：最新评论时间。
- `data.list[].roots` (array<AdminCommentThreadRootItemResponse>; 可选)：该文章下的根评论列表。
### GET /api/admin/comments/{commentId}

- 摘要：查询单条评论详情
- `operationId`：`getComment`
- 风险等级：`LOW`

#### 参数

- `commentId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminCommentItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 101)：评论 ID。
- `data.postId` (integer; 可选; 示例: 18)：所属文章 ID。
- `data.postTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调记录)：所属文章标题。
- `data.postSlug` (string; 可选; 示例: spring-boot-nuxt-integration-notes)：所属文章 slug。
- `data.userId` (integer; 可选; 示例: 3)：评论用户 ID。
- `data.userName` (string; 可选; 示例: 内容编辑小王)：评论用户名。
- `data.userEmail` (string; 可选; 示例: editor@example.com)：评论用户邮箱。
- `data.replyCommentId` (integer; 可选; 示例: 88)：直接回复目标评论 ID。根评论时为空。
- `data.rootId` (integer; 可选; 示例: 80)：评论根节点 ID。根评论时通常与当前评论 ID 一致或为空。
- `data.replyToUserName` (string; 可选; 示例: 站长)：被回复用户名称。
- `data.content` (string; 可选; 示例: 这篇文章写得很清楚。)：评论内容。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED; 示例: PENDING)：评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。
- `data.ip` (string; 可选; 示例: 127.0.0.1)：评论来源 IP。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### DELETE /api/admin/comments/{commentId}

- 摘要：删除评论
- `operationId`：`deleteComment`
- 风险等级：`HIGH`

#### 参数

- `commentId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK
### PUT /api/admin/comments/{commentId}/status

- 摘要：更新评论状态
- `operationId`：`updateCommentStatus`
- 风险等级：`MEDIUM`

#### 参数

- `commentId` (path; integer; 必填)

#### 请求体

- Schema：`AdminCommentStatusUpdateRequest`
- `status` (string; 必填; 枚举: PENDING, APPROVED, REJECTED; 示例: APPROVED)：评论审核状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。

#### 响应

- `200`；ApiResponseAdminCommentItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 101)：评论 ID。
- `data.postId` (integer; 可选; 示例: 18)：所属文章 ID。
- `data.postTitle` (string; 可选; 示例: Spring Boot 与 Nuxt 联调记录)：所属文章标题。
- `data.postSlug` (string; 可选; 示例: spring-boot-nuxt-integration-notes)：所属文章 slug。
- `data.userId` (integer; 可选; 示例: 3)：评论用户 ID。
- `data.userName` (string; 可选; 示例: 内容编辑小王)：评论用户名。
- `data.userEmail` (string; 可选; 示例: editor@example.com)：评论用户邮箱。
- `data.replyCommentId` (integer; 可选; 示例: 88)：直接回复目标评论 ID。根评论时为空。
- `data.rootId` (integer; 可选; 示例: 80)：评论根节点 ID。根评论时通常与当前评论 ID 一致或为空。
- `data.replyToUserName` (string; 可选; 示例: 站长)：被回复用户名称。
- `data.content` (string; 可选; 示例: 这篇文章写得很清楚。)：评论内容。
- `data.status` (string; 可选; 枚举: PENDING, APPROVED, REJECTED; 示例: PENDING)：评论状态。PENDING=待审核，APPROVED=已通过，REJECTED=已驳回。
- `data.ip` (string; 可选; 示例: 127.0.0.1)：评论来源 IP。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。

## 后台附件管理

### GET /api/admin/attachments

- 摘要：查询附件列表
- `operationId`：`list`
- 风险等级：`LOW`

#### 参数

- `request` (query; AdminAttachmentQueryRequest; 必填)
- Query DTO 展开：
- `keyword` (string; 可选; 示例: spring-boot)：关键词。会按文件名等可搜索字段模糊匹配。
- `mimeType` (string; 可选; 示例: image/png)：MIME 类型筛选。可按图片、视频等具体类型过滤。
- `pageNo` (integer; 可选; 示例: 1)：页码。未传时后端会按默认分页处理。
- `pageSize` (integer; 可选; 示例: 10)：每页条数。未传时后端会按默认分页处理。

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminAttachmentListResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.list` (array<AdminAttachmentItemResponse>; 可选)：附件列表数据。
- `data.total` (integer; 可选; 示例: 128)：总记录数。
- `data.pageNo` (integer; 可选; 示例: 1)：当前页码。
- `data.pageSize` (integer; 可选; 示例: 10)：当前每页条数。
- `data.totalPages` (integer; 可选; 示例: 13)：总页数。
- 列表项结构提示（`data.list[]` 第一层）：
- `data.list[].id` (integer; 可选; 示例: 12)：附件 ID。
- `data.list[].fileName` (string; 可选; 示例: spring-boot-guide.png)：原始文件名。
- `data.list[].fileExt` (string; 可选; 示例: png)：文件扩展名。
- `data.list[].mimeType` (string; 可选; 示例: image/png)：文件 MIME 类型。
- `data.list[].sizeBytes` (integer; 可选; 示例: 245760)：文件大小，单位字节。
- `data.list[].sha256` (string; 可选; 示例: 4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a)：文件 SHA-256 摘要。
- `data.list[].storageProvider` (string; 可选; 示例: S3)：存储提供商标识。
- `data.list[].storageConfigKey` (string; 可选; 示例: r2-main)：命中的存储配置键。
- `data.list[].bucket` (string; 可选; 示例: yunyu-assets)：对象所在 Bucket。
- `data.list[].objectKey` (string; 可选; 示例: attachments/2026/04/spring-boot-guide.png)：对象在存储中的 object key。
- `data.list[].accessUrl` (string; 可选; 示例: https://cdn.example.com/attachments/2026/04/spring-boot-guide.png)：对外访问地址。
- `data.list[].etag` (string; 可选; 示例: d41d8cd98f00b204e9800998ecf8427e)：对象存储返回的 ETag。
- `data.list[].uploaderUserId` (integer; 可选; 示例: 1)：上传者用户 ID。
- `data.list[].createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.list[].updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### POST /api/admin/attachments/check-exists

- 摘要：检查附件是否已存在
- `operationId`：`checkExists`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminAttachmentExistsCheckRequest`
- `sha256` (string; 必填; 示例: 4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a)：文件内容的 SHA-256 摘要。用于检查后台是否已存在同一文件。

#### 响应

- `200`；ApiResponseAdminAttachmentExistsCheckResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.exists` (boolean; 可选; 示例: true)：是否已存在相同 SHA-256 的附件。
- `data.attachment` (AdminAttachmentItemResponse; 可选)：命中时返回的附件详情；未命中时为空。
### POST /api/admin/attachments/complete

- 摘要：提交上传完成回执
- `operationId`：`complete`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminAttachmentCompleteRequest`
- `fileName` (string; 必填; 示例: spring-boot-guide.png)：原始文件名。应与申请预签名时提交的文件名一致。
- `contentType` (string; 必填; 示例: image/png)：文件 MIME 类型。应与真实上传文件保持一致。
- `sizeBytes` (integer; 可选; 示例: 245760)：文件大小，单位字节。
- `sha256` (string; 必填; 示例: 4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a)：文件内容的 SHA-256 摘要，用于去重和秒传判断。
- `bucket` (string; 必填; 示例: yunyu-assets)：对象最终所在的 Bucket 名称。通常来自预签名响应。
- `objectKey` (string; 必填; 示例: attachments/2026/04/spring-boot-guide.png)：对象最终写入的 object key。通常来自预签名响应。
- `storageConfigKey` (string; 必填; 示例: r2-main)：命中的存储配置键。通常来自预签名响应。
- `etag` (string; 可选; 示例: d41d8cd98f00b204e9800998ecf8427e)：对象存储返回的 ETag。可选；如果客户端可获取，建议原样回传。

#### 响应

- `200`；ApiResponseAdminAttachmentItemResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.id` (integer; 可选; 示例: 12)：附件 ID。
- `data.fileName` (string; 可选; 示例: spring-boot-guide.png)：原始文件名。
- `data.fileExt` (string; 可选; 示例: png)：文件扩展名。
- `data.mimeType` (string; 可选; 示例: image/png)：文件 MIME 类型。
- `data.sizeBytes` (integer; 可选; 示例: 245760)：文件大小，单位字节。
- `data.sha256` (string; 可选; 示例: 4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a)：文件 SHA-256 摘要。
- `data.storageProvider` (string; 可选; 示例: S3)：存储提供商标识。
- `data.storageConfigKey` (string; 可选; 示例: r2-main)：命中的存储配置键。
- `data.bucket` (string; 可选; 示例: yunyu-assets)：对象所在 Bucket。
- `data.objectKey` (string; 可选; 示例: attachments/2026/04/spring-boot-guide.png)：对象在存储中的 object key。
- `data.accessUrl` (string; 可选; 示例: https://cdn.example.com/attachments/2026/04/spring-boot-guide.png)：对外访问地址。
- `data.etag` (string; 可选; 示例: d41d8cd98f00b204e9800998ecf8427e)：对象存储返回的 ETag。
- `data.uploaderUserId` (integer; 可选; 示例: 1)：上传者用户 ID。
- `data.createdTime` (string; 可选; 示例: 2026-04-26T20:30:00)：创建时间。
- `data.updatedTime` (string; 可选; 示例: 2026-04-26T20:35:00)：更新时间。
### POST /api/admin/attachments/presign

- 摘要：生成上传预签名
- `operationId`：`presign`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminAttachmentPresignRequest`
- `fileName` (string; 必填; 示例: spring-boot-guide.png)：原始文件名。用于生成对象键和回显展示。
- `contentType` (string; 必填; 示例: image/png)：文件 MIME 类型。需与后续真实上传内容保持一致。
- `sizeBytes` (integer; 可选; 示例: 245760)：文件大小，单位字节。必须大于 0，且会受当前启用 S3 配置的最大文件大小限制。

#### 响应

- `200`；ApiResponseAdminAttachmentPresignResponse；OK
- 响应字段展开（高频接口第一层）：
- `data.uploadUrl` (string; 可选; 示例: https://example-bucket.s3.amazonaws.com/attachments/2026/04/spring-boot-guide.png?X-Amz-Algorithm=...)：预签名上传地址。客户端应按返回的 HTTP 方法和请求头直传文件。
- `data.httpMethod` (string; 可选; 示例: PUT)：上传所需的 HTTP 方法。
- `data.headers` (object; 可选; 示例: {"Content-Type": "image/png"})：上传所需附加请求头。
- `data.storageConfigKey` (string; 可选; 示例: r2-main)：命中的存储配置键。
- `data.bucket` (string; 可选; 示例: yunyu-assets)：目标 Bucket 名称。
- `data.objectKey` (string; 可选; 示例: attachments/2026/04/spring-boot-guide.png)：目标 object key。
- `data.accessUrl` (string; 可选; 示例: https://cdn.example.com/attachments/2026/04/spring-boot-guide.png)：上传完成后可访问的资源地址。
- `data.expireAt` (string; 可选; 示例: 2026-04-26T20:35:00)：预签名过期时间。
### DELETE /api/admin/attachments/{attachmentId}

- 摘要：删除附件
- `operationId`：`delete`
- 风险等级：`HIGH`

#### 参数

- `attachmentId` (path; integer; 必填)

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseVoid；OK

## 后台首页配置

### GET /api/admin/site/homepage-config

- 摘要：查询首页配置
- `operationId`：`getHomepageConfig`
- 风险等级：`LOW`

#### 参数

- 无路径参数或查询参数

#### 请求体

- 无请求体

#### 响应

- `200`；ApiResponseAdminHomepageConfigResponse；OK
### PUT /api/admin/site/homepage-config

- 摘要：更新首页配置
- `operationId`：`updateHomepageConfig`
- 风险等级：`MEDIUM`

#### 参数

- 无路径参数或查询参数

#### 请求体

- Schema：`AdminHomepageConfigUpdateRequest`
- `heroEnabled` (boolean; 可选; 示例: true)：是否启用首页首屏模块。
- `heroLayout` (string; 可选; 枚举: brand; 示例: brand)：首屏布局。当前仅支持 brand。
- `heroBackgroundMode` (string; 可选; 枚举: gradient-grid, soft-glow, minimal-lines, keyword-cloud; 示例: gradient-grid)：首屏背景模式。
- `heroEyebrow` (string; 可选; 示例: Personal Knowledge Base)：首页眉题。可选。
- `heroTitle` (string; 必填; 示例: 在技术与生活之间，持续写作与整理)：首页主标题。
- `heroSubtitle` (string; 必填; 示例: 这里记录后端、前端、工程实践，以及一些慢慢沉淀下来的想法。)：首页副标题。
- `heroPrimaryButtonText` (string; 必填; 示例: 查看文章)：主按钮文案。
- `heroPrimaryButtonLink` (string; 必填; 示例: /posts)：主按钮跳转地址。
- `heroSecondaryButtonText` (string; 可选; 示例: 进入专题)：次按钮文案。可选。
- `heroSecondaryButtonLink` (string; 可选; 示例: /topics)：次按钮跳转地址。可选。
- `heroVisualPostId` (integer; 可选; 示例: 18)：首屏视觉文章 ID。可选；填写后用于从文章中取视觉内容。
- `heroVisualClickable` (boolean; 可选; 示例: true)：首屏视觉区是否允许点击跳转文章。
- `heroKeywords` (array<string>; 可选; 示例: ["Spring Boot", "Nuxt", "架构"])：首页关键词列表，最多 6 个。
- `showHeroKeywords` (boolean; 可选; 示例: true)：是否展示首页关键词。
- `showHeroStats` (boolean; 可选; 示例: true)：是否展示首页统计项。
- `heroStats` (array<AdminHomepageHeroStatRequest>; 可选)：首页统计项列表，最多 4 个。
- `heroStats[].label` (string; 必填)
- `heroStats[].value` (string; 必填)
- `showFeaturedSection` (boolean; 可选; 示例: true)：是否展示推荐文章区。
- `featuredSectionTitle` (string; 可选; 示例: 精选推荐)：推荐文章区标题。
- `showLatestSection` (boolean; 可选; 示例: true)：是否展示最新文章区。
- `latestSectionTitle` (string; 可选; 示例: 最新文章)：最新文章区标题。
- `showCategorySection` (boolean; 可选; 示例: true)：是否展示分类区。
- `categorySectionTitle` (string; 可选; 示例: 分类浏览)：分类区标题。
- `showTopicSection` (boolean; 可选; 示例: true)：是否展示专题区。
- `topicSectionTitle` (string; 可选; 示例: 专题阅读)：专题区标题。

#### 响应

- `200`；ApiResponseAdminHomepageConfigResponse；OK
