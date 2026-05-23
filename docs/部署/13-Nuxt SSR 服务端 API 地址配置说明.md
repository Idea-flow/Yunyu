# Nuxt SSR 服务端 API 地址配置说明

## 一、问题背景

使用纯 Docker 模式部署后，访问首页出现报错：

```
Page not found: /api/site/home
```

而直接访问 `https://your-domain.com/api/site/home` 却能正常返回数据。

---

## 二、根本原因

Nuxt 是 SSR（服务端渲染）框架，首页数据在**服务端渲染阶段**就需要调用后端接口获取。

### 两个运行环境的请求链路完全不同

| 环境 | 请求发起方 | 请求路径 |
|---|---|---|
| 浏览器端（CSR） | 用户浏览器 | 浏览器 → nginx → 后端 |
| 服务端渲染（SSR） | yunyu-web 容器内部 | 容器内 → ??? |

**问题就在 SSR 阶段**：

```
用户访问首页
    ↓
nginx 转发请求给 yunyu-web:3000（Nuxt SSR）
    ↓
Nuxt 服务端需要调用 /api/site/home 获取数据
    ↓
YUNYU_PUBLIC_API_BASE 为空
    ↓
代码 fallback：http://127.0.0.1:20000
    ↓
yunyu-web 容器内的 127.0.0.1 没有 20000 端口 → 连接失败
    ↓
Page not found: /api/site/home  ✗
```

浏览器直接访问 `/api/site/home` 能通，是因为走的是：
```
浏览器 → Cloudflare → nginx → yunyu-server-native:20000  ✓
```
完全不经过 Nuxt SSR，所以没问题。

---

## 三、为什么不能把 YUNYU_PUBLIC_API_BASE 设为容器名

看起来把 `YUNYU_PUBLIC_API_BASE=http://yunyu-server-native:20000` 能让 SSR 访问后端，但这个配置是 `public`，**服务端和浏览器共享同一个值**：

```
SSR 服务端：http://yunyu-server-native:20000  ✓ 容器内网能解析
浏览器端：  http://yunyu-server-native:20000  ✗ 用户浏览器不认识这个域名
```

页面首次加载正常，但用户交互时所有浏览器端请求全部失败。

---

## 四、解决方案

利用 Nuxt `runtimeConfig` 的分层机制：

- `runtimeConfig.public.*` — 服务端 + 浏览器**共享**
- `runtimeConfig.*`（非 public）— **仅服务端可见**，浏览器拿不到

新增一个仅服务端可见的 `apiBaseInternal`，SSR 阶段优先用它，浏览器端用 `public.apiBase`。

### 改动一：`yunyu-web/nuxt.config.ts`

```js
runtimeConfig: {
  // 仅服务端可见：SSR 阶段调用后端使用容器内部地址
  apiBaseInternal: process.env.NUXT_API_BASE_INTERNAL || '',
  public: {
    // 浏览器端：留空时走相对路径，由 nginx 同域转发到后端
    apiBase: process.env.YUNYU_PUBLIC_API_BASE || ''
  }
}
```

### 改动二：`yunyu-web/app/composables/useApiClient.ts`

新增 `resolveApiBase()` 函数，在 SSR 阶段自动选择内部地址：

```js
function resolveApiBase() {
  if (import.meta.server && config.apiBaseInternal) {
    return config.apiBaseInternal  // SSR：容器内网直连
  }
  return config.public.apiBase    // 浏览器：走 nginx 转发
}
```

所有请求中的 `config.public.apiBase` 替换为 `resolveApiBase()`。

### 改动三：`docker/docker-compose.yml`

给 `yunyu-web` 服务注入内部地址：

```yaml
yunyu-web:
  environment:
    NUXT_API_BASE_INTERNAL: http://yunyu-server-native:20000
    NUXT_PUBLIC_API_BASE: ${YUNYU_PUBLIC_API_BASE:-}
```

---

## 五、各部署模式下的配置方式

### 模式 A：纯 Docker（nginx 统一入口）

`docker-compose.yml` 已内置 `NUXT_API_BASE_INTERNAL`，无需额外配置：

```
NUXT_API_BASE_INTERNAL = http://yunyu-server-native:20000  （已硬编码在 compose 文件）
YUNYU_PUBLIC_API_BASE  = （留空，浏览器走 nginx 同域转发）
```

**最终调用链：**

```
浏览器访问首页
    ↓ nginx → yunyu-web:3000（SSR）
    ↓ import.meta.server = true → 使用 http://yunyu-server-native:20000
    ↓ 容器内网直连，不走公网  ✓

浏览器端后续请求
    ↓ apiBase = "" → 相对路径 /api/xxx
    ↓ nginx 转发给 yunyu-server-native:20000  ✓
```

---

### 模式 B：Cloudflare Pages（纯静态/SSR 托管在 Cloudflare）

Cloudflare Pages 运行的是静态构建产物，没有 Node.js 服务端，**不存在 SSR 问题**。

浏览器直接请求后端域名，只需配置一个变量：

```
YUNYU_PUBLIC_API_BASE = https://api.yourdomain.com
```

`NUXT_API_BASE_INTERNAL` 不需要配置。

---

### 模式 C：本地开发

本地开发时 Nuxt dev server 运行在宿主机，直接能访问后端，不需要容器内网地址。

在 `yunyu-web/.env` 中配置：

```bash
# 留空，走代码默认值 http://127.0.0.1:20000
YUNYU_PUBLIC_API_BASE=

# 或者明确指定本地后端地址
YUNYU_PUBLIC_API_BASE=http://127.0.0.1:20000
```

`NUXT_API_BASE_INTERNAL` 留空即可，`resolveApiBase()` 会 fallback 到 `public.apiBase`。

---

### 模式 D：Cloudflare Pages + 1Panel（前端 Cloudflare，后端服务器）

前端部署在 Cloudflare Pages，运行环境是 Cloudflare 的边缘网络（无 Node.js SSR 容器），所有请求都是浏览器发出，只需：

```
YUNYU_PUBLIC_API_BASE = https://api.yourdomain.com
```

`NUXT_API_BASE_INTERNAL` 不需要配置。

---

## 六、配置速查表

| 部署模式 | `NUXT_API_BASE_INTERNAL` | `YUNYU_PUBLIC_API_BASE` |
|---|---|---|
| 纯 Docker（nginx 统一入口） | `http://yunyu-server-native:20000`（compose 已内置） | 留空 |
| Cloudflare Pages | 不需要 | `https://api.yourdomain.com` |
| Cloudflare Pages + 1Panel | 不需要 | `https://api.yourdomain.com` |
| 本地开发 | 不需要 | 留空或 `http://127.0.0.1:20000` |

---

## 七、注意事项

1. `NUXT_API_BASE_INTERNAL` 是服务端私有变量，**不会暴露给浏览器**，安全。
2. 纯 Docker 模式下 `NUXT_API_BASE_INTERNAL` 已在 `docker-compose.yml` 中硬编码，`.env` 中无需配置。
3. SSR 容器内的请求走内网，不经过公网和 Cloudflare，延迟更低，也不消耗外部流量。
4. 如果在纯 Docker 模式下把 `YUNYU_PUBLIC_API_BASE` 设为公网域名，SSR 会绕出公网再回来，能用但会增加延迟，不推荐。
