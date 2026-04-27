# 后台连接信息管理

本文档用于约定 `yunyu-admin-operator` skill 在调用后台接口时如何获取、保存和复用连接信息。

## 一、连接信息组成

调用后台接口前，至少需要以下两个字段：

1. `baseUrl`
   目标后台域名或基础地址，例如：
   - `http://127.0.0.1:20000`
   - `https://admin.example.com`
2. `token`
   后台登录后的 Bearer token，不需要手动加 `Bearer ` 前缀时，由调用方按实际请求格式处理。

## 二、本地保存位置

连接信息固定保存在：

```text
.agents/skills/yunyu-admin-operator/.local/admin-connection.json
```

推荐结构：

```json
{
  "baseUrl": "http://127.0.0.1:20000",
  "token": "<SECRET>",
  "updatedAt": "2026-04-27T00:00:00+08:00"
}
```

说明：

- `baseUrl` 和 `token` 是必填。
- `updatedAt` 用于记录最后一次更新连接信息的时间，便于排查失效问题。
- 该文件只用于本地 skill 运行，不应提交到仓库。

## 三、读取顺序

建议固定按以下顺序处理：

1. 先读取本地连接文件。
   可直接执行：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show`
2. 如果存在有效 `baseUrl` 和 `token`，优先直接使用。
3. 如果文件不存在、字段缺失或用户明确要求切换环境，再向用户索要新的 `baseUrl` 和 `token`。
4. 收到新的连接信息后，覆盖写回本地连接文件，供后续复用：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>`

## 四、脚本用法

推荐统一使用以下脚本管理连接信息：

```bash
bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show
bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh get baseUrl
bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh get token
bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>
bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh clear
```

说明：

1. `show` 会脱敏展示 token。
2. `get` 会输出原始字段值，适合脚本或 agent 内部继续使用。
3. `clear` 会删除当前本地连接信息，适合切换环境或 token 失效后重置。
4. 发起实际后台请求时，优先配合使用：
   `bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh <METHOD> <PATH>`

## 五、向用户索要信息时的要求

如果本地连接文件不可用，应明确向用户索要：

1. 当前要操作的后台域名或基础地址
2. 当前环境对应的后台 token

如用户没有直接提供 token，可提示用户从浏览器控制台执行：

```js
localStorage.getItem('yunyu_access_token')
```

## 六、失败后的处理

如果接口调用失败，不要直接假设是业务错误，应先判断是否属于连接信息问题。

优先检查：

1. `baseUrl` 是否错误
2. token 是否过期或无效
3. 当前 token 是否对应 `SUPER_ADMIN`
4. 目标环境是否可访问

如果怀疑是连接信息问题，应明确告诉用户：

- 当前请求失败的原因
- 是否需要更换 `baseUrl`
- 是否需要更换 `token`

## 七、安全约束

1. 不要把 token 写进参考文档或提交到 Git。
2. 不要在回复中完整回显 token。
3. 对写操作，仍应先校验 `/api/auth/me`，确认当前用户具备 `SUPER_ADMIN` 权限。
