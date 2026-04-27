# 站点设置操作说明

本文档用于指导 agent 操作站点配置、首页配置，以及相关敏感设置。

## 零、统一前置步骤

在查询或写入站点设置类后台接口前，先处理连接信息：

1. 先读取：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show`
2. 如果本地没有可用连接信息，先让用户提供：
   - 后台域名或基础地址
   - 当前环境的后台 token
3. 收到后保存：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>`
4. 如请求失败，优先判断是否需要更换 `baseUrl` 或 `token`，不要直接假设是配置字段本身有误。

## 一、站点配置

接口：

1. `GET /api/admin/site-config`
2. `PUT /api/admin/site-config`

建议流程：

1. 先读取当前配置
2. 在当前配置上合并修改
3. 再执行 `PUT`

原因：

1. 更新接口包含多个必填字段
2. 如果只凭一句修改请求直接提交零散字段，容易把其他必填配置丢掉

重点字段：

1. `siteName`
2. `siteSubTitle`
3. `footerText`
4. `logoUrl`
5. `faviconUrl`
6. `defaultTitle`
7. `defaultDescription`
8. `primaryColor`
9. `secondaryColor`
10. `wechatAccessCodeEnabled`
11. `wechatAccessCode`
12. `wechatAccessCodeHint`
13. `wechatQrCodeUrl`

特别注意：

1. 颜色字段必须是 `#RRGGBB`
2. 如果启用公众号验证码，建议同时检查验证码、提示文案和二维码地址是否完整

## 二、首页配置

接口：

1. `GET /api/admin/site/homepage-config`
2. `PUT /api/admin/site/homepage-config`

建议流程：

1. 先读取当前首页配置
2. 合并本次改动
3. 再执行 `PUT`

重点字段：

1. `heroTitle`
2. `heroSubtitle`
3. `heroPrimaryButtonText`
4. `heroPrimaryButtonLink`
5. `heroSecondaryButtonText`
6. `heroSecondaryButtonLink`
7. `heroBackgroundMode`
8. `heroKeywords`
9. 各模块显示开关和标题

默认理解：

1. 首页文案改动属于普通写操作
2. 若用户只说“改得更有品牌感”，应先生成建议文案，再请用户确认

## 三、S3 配置

接口：

1. `GET /api/admin/site/storage/s3`
2. `PUT /api/admin/site/storage/s3`
3. `POST /api/admin/site/storage/s3/test`

建议：

1. 修改前先读取当前配置
2. 涉及密钥类字段时显式确认
3. 保存后如用户有要求，可再调用测试连接接口验证

## 四、AI 提供商配置

接口：

1. `GET /api/admin/site/ai/providers`
2. `PUT /api/admin/site/ai/providers`
3. `POST /api/admin/site/ai/providers/test`

建议：

1. 修改前先读取当前配置
2. 涉及 `apiKey`、模型、超时参数时，最好明确告诉用户会改哪些字段
3. 保存后如用户有要求，可调用测试接口验证连接

## 五、设置类接口的统一原则

对所有设置类接口，建议固定流程：

1. 先 `GET`
2. 再合并本次修改
3. 最后 `PUT`

不要直接构造一份“看起来像完整”的配置盲目覆盖。
