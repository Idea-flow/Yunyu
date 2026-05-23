# 纯 Docker 模式部署说明（nginx 统一入口）

## 适用场景

服务器上只安装 Docker，不依赖 1Panel、宿主机 Nginx 等额外工具。  
项目所有服务（前端、后端、数据库、反向代理）全部由 Docker Compose 管理，nginx 容器作为唯一对外入口。

**使用文件：`docker/docker-compose.yml`**

---

## 一、架构说明

```
外部请求（80 端口）
        │
   yunyu-nginx（nginx:alpine）
        │
   ┌────┴────────────────┐
   │ /api/*              │ 其余请求
   ▼                     ▼
yunyu-server-native   yunyu-web
   （:20000）           （:3000）
        │
   yunyu-mysql
   （内部，不对外）
```

- **yunyu-nginx**：唯一对外暴露 80 端口的容器，负责路由分发
- **yunyu-web**：Nuxt SSR 前端，不对外暴露端口
- **yunyu-server-native**：Spring Boot Native 后端，不对外暴露端口
- **yunyu-mysql**：数据库，不对外暴露端口

> 前后端同域（同一个 IP/域名），浏览器请求 `/api/*` 经 nginx 转发给后端，无跨域问题，`NUXT_PUBLIC_API_BASE` 留空即可。

---

## 二、部署前准备

确认服务器已安装：

```bash
git --version
docker --version
docker compose version
```

---

## 三、部署步骤

### 步骤 1：克隆仓库

```bash
git clone https://github.com/Idea-flow/Yunyu.git
cd Yunyu
```

### 步骤 2：创建数据库目录

```bash
mkdir -p yunyu_mysql_data
```

### 步骤 3：创建 `.env` 配置文件

```bash
cp docker/.env.example .env
```

修改 `.env`，至少替换以下三项（其余保持默认即可）：

```bash
MYSQL_PASSWORD=你的数据库密码
MYSQL_ROOT_PASSWORD=你的Root密码
YUNYU_JWT_SECRET=openssl rand -hex 32 生成的64位字符串
```

> `YUNYU_PUBLIC_API_BASE` 留空即可，前后端同域通过 nginx 内部转发。

### 步骤 4：启动所有服务

```bash
docker compose -f docker/docker-compose.yml pull
docker compose -f docker/docker-compose.yml up -d
```

### 步骤 5：查看启动状态

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f
```

---

## 四、验证

### 访问前端

```
http://服务器IP
```

### 后端健康检查（经由 nginx 转发）

```bash
curl http://服务器IP/actuator/health
```

### 直接验证后端（跳过 nginx，容器内部）

```bash
docker exec yunyu-server-native wget -qO- http://localhost:20000/actuator/health
```

---

## 五、配置 HTTPS（可选）

若需要 HTTPS，有两种方式：

### 方式 A：在服务器前置 Cloudflare（推荐）

Cloudflare 开启代理模式（橙色云朵），自动提供 HTTPS，服务器只需保持 80 端口正常响应。无需修改 nginx 配置。

### 方式 B：修改 nginx.conf 加载 SSL 证书

1. 将证书文件挂载进 nginx 容器
2. 修改 `docker/nginx/nginx.conf`，添加 443 监听与 ssl 配置
3. 重启 nginx 容器：`docker compose -f docker/docker-compose.yml restart yunyu-nginx`

---

## 六、常用运维命令

### 升级镜像

```bash
docker compose -f docker/docker-compose.yml pull
docker compose -f docker/docker-compose.yml up -d
```

### 查看某个服务日志

```bash
docker compose -f docker/docker-compose.yml logs -f yunyu-server-native
docker compose -f docker/docker-compose.yml logs -f yunyu-web
docker compose -f docker/docker-compose.yml logs -f yunyu-nginx
```

### 重启某个服务

```bash
docker compose -f docker/docker-compose.yml restart yunyu-nginx
```

### 停止所有服务（不删数据）

```bash
docker compose -f docker/docker-compose.yml down
```

---

## 七、注意事项

1. 正式环境必须修改 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`YUNYU_JWT_SECRET`
2. 不要执行 `docker compose down -v`，会删除数据库 volume
3. 不要误删 `yunyu_mysql_data` 目录
4. nginx 配置文件位于 `docker/nginx/nginx.conf`，修改后需重启 nginx 容器
5. 若需要开放后端端口给外部直接访问，在 `.env` 中设置 `SERVER_PORT=20000` 并在 `docker-compose.yml` 的 `yunyu-server-native` 下手动添加 `ports`（不建议生产环境这样做）
