# Cloudflare Pages + 1Panel 模式部署说明

## 适用场景

- **前端**：部署在 Cloudflare Pages（免费、全球 CDN、自动 HTTPS）
- **后端 + 数据库**：部署在服务器 Docker 容器中，由 1Panel 提供反向代理和 HTTPS

**使用文件：`docker/docker-compose-server.yml`**

---

## 一、架构说明

```
用户浏览器
    │
    ├─── Cloudflare Pages（yourdomain.com）
    │         前端静态/SSR，构建时注入后端地址
    │
    └─── Cloudflare CDN → 1Panel 反代（api.yourdomain.com:443）
                │
          yunyu-server-native（:20000）
                │
          yunyu-mysql（内部，不对外）
```

- 前端调用后端时，使用 `https://api.yourdomain.com`，跨域由后端 CORS 配置处理（已全开）
- 后端 20000 端口对外暴露，1Panel 将其反代到 `api.yourdomain.com`

---

## 二、部署步骤

### 后端部分（服务器）

#### 步骤 1：克隆仓库

```bash
git clone https://github.com/Idea-flow/Yunyu.git
cd Yunyu
```

#### 步骤 2：创建数据库目录

```bash
mkdir -p yunyu_mysql_data
```

#### 步骤 3：创建 `.env` 配置文件

```bash
cp docker/.env.example .env
```

修改 `.env`，至少替换以下三项：

```bash
MYSQL_PASSWORD=你的数据库密码
MYSQL_ROOT_PASSWORD=你的Root密码
YUNYU_JWT_SECRET=openssl rand -hex 32 生成的64位字符串
```

#### 步骤 4：启动后端服务

```bash
docker compose -f docker/docker-compose-server.yml pull
docker compose -f docker/docker-compose-server.yml up -d
```

#### 步骤 5：验证后端启动

```bash
docker compose -f docker/docker-compose-server.yml ps
curl http://127.0.0.1:20000/actuator/health
```

返回 `{"status":"UP"}` 即表示后端正常。

---

### 1Panel 配置反向代理

1. 登录 1Panel 管理后台
2. 进入 **网站** → **添加网站** → 填写域名 `api.yourdomain.com`
3. 开启 **反向代理**，代理地址填写：`http://127.0.0.1:20000`
4. 申请 SSL 证书（Let's Encrypt 免费证书），开启 HTTPS

验证：

```bash
curl https://api.yourdomain.com/actuator/health
```

---

### 前端部分（Cloudflare Pages）

#### 步骤 1：连接 GitHub 仓库

在 Cloudflare Pages 控制台，连接 GitHub 仓库 `Yunyu`，选择 `yunyu-web` 作为构建目录（或配置 root directory）。

#### 步骤 2：构建配置

| 配置项 | 值 |
|---|---|
| 框架预设 | Nuxt.js |
| 构建命令 | `pnpm build` |
| 构建输出目录 | `.output/public`（静态模式）或参考 Nuxt 文档 |
| 根目录 | `yunyu-web` |

#### 步骤 3：配置环境变量

在 Cloudflare Pages 的 **设置 → 环境变量** 中添加：

| 变量名 | 值 |
|---|---|
| `YUNYU_PUBLIC_API_BASE` | `https://api.yourdomain.com` |

> 这个变量会在构建时注入前端，使浏览器直接请求后端域名。

#### 步骤 4：触发部署

推送代码到 `main` 分支，Cloudflare Pages 自动构建并部署。

---

## 三、常用运维命令（后端）

### 升级后端镜像

```bash
docker compose -f docker/docker-compose-server.yml pull yunyu-server-native
docker compose -f docker/docker-compose-server.yml up -d yunyu-server-native
```

### 查看后端日志

```bash
docker compose -f docker/docker-compose-server.yml logs -f yunyu-server-native
```

### 停止后端（不删数据）

```bash
docker compose -f docker/docker-compose-server.yml down
```

---

## 四、注意事项

1. 正式环境必须修改 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`YUNYU_JWT_SECRET`
2. 不要执行 `docker compose down -v`，会删除数据库 volume
3. 不要误删 `yunyu_mysql_data` 目录
4. 1Panel 反代配置完成后，后端 20000 端口建议通过服务器防火墙只允许本机访问，避免直接暴露公网
5. Cloudflare Pages 部署前端时，`YUNYU_PUBLIC_API_BASE` 必须填写完整的 HTTPS 地址，否则浏览器端 API 请求会失败
