# Docker 目录重构方案

## 一、现状分析

### 当前文件分布（混乱点）

| 文件路径 | 内容 | 问题 |
|---|---|---|
| `/docker-compose.yml`（根目录） | MySQL + JVM 后端 | 与 `/docker/docker-compose.yml` 几乎重复 |
| `/docker/docker-compose.yml` | MySQL + JVM 后端 | 与根目录重复，数据挂载路径用了 `../` |
| `/docker/native-image/docker-compose.yml` | MySQL + Native 后端 | 散落在子目录，不易发现 |
| `/docker/native-image/docker-compose-one.yml` | 仅 Native 后端（依赖外部 MySQL） | 职责不清晰 |
| `/docker/jar/Dockerfile` | JVM 版构建 | 目录命名不直观 |
| `/docker/native-image/Dockerfile` | Native 版构建 | 散落在子目录 |
| `/yunyu-server/Dockerfile` | 另一个 JVM 版构建 | 与 `/docker/jar/Dockerfile` 重复 |
| `/docker/native-image/info.md` | 一条 inspect 命令 | 无实际价值 |

**核心问题：**
1. 三个 `docker-compose.yml` 不知道用哪个
2. 没有前端 Docker 容器
3. Docker 相关文件散落在根目录和多个子目录
4. JVM 版 Dockerfile 存在两份重复

---

## 二、重构目标

1. **`/docker/docker-compose.yml`** — 全局启动文件，包含前端 + 后端（Native 优先）+ MySQL，执行一条命令即可运行整个项目
2. **`/docker/docker-compose-server.yml`** — 仅后端，包含 MySQL + Native 后端，适合前端部署在 Cloudflare Pages 等平台的场景
3. 所有 Docker 相关文件统一放在 `/docker` 目录下
4. 删除根目录的 `docker-compose.yml`
5. 整理 Dockerfile，按 `backend/` 和 `frontend/` 分类存放

---

## 三、新目录结构

```
/docker/
├── docker-compose.yml           # 全局：前端 + Native 后端 + MySQL
├── docker-compose-server.yml    # 仅后端：Native 后端 + MySQL
├── .env.example                 # 环境变量说明与默认值示例
├── backend/
│   ├── Dockerfile.jvm           # JVM 版后端镜像（原 docker/jar/Dockerfile）
│   └── Dockerfile.native        # Native 版后端镜像（原 docker/native-image/Dockerfile）
└── frontend/
    └── Dockerfile               # 前端 Nuxt SSR 镜像（新增）
```

**删除的文件/目录：**
- `/docker-compose.yml`（根目录）
- `/docker/docker-compose.yml`（旧的，被新文件替代）
- `/docker/native-image/`（整个目录，内容迁移后删除）
- `/docker/jar/`（整个目录，内容迁移后删除）
- `/yunyu-server/Dockerfile`（与 `docker/backend/Dockerfile.jvm` 重复）

---

## 四、各文件内容说明

### 4.1 `/docker/docker-compose.yml`（全局）

包含三个服务：

| 服务名 | 镜像 | 说明 |
|---|---|---|
| `yunyu-mysql` | `mysql:8.4` | 数据库，健康检查通过后后端才启动 |
| `yunyu-server-native` | `ghcr.io/idea-flow/yunyu-server-native:latest` | Native 后端，优先使用 |
| `yunyu-web` | `ghcr.io/idea-flow/yunyu-web:latest` | 前端 Nuxt SSR 容器 |

数据卷挂载：MySQL 数据挂载到 `../yunyu_mysql_data`（相对于 `/docker` 目录，即项目根目录同级）

```yaml
services:
  yunyu-mysql:
    image: mysql:8.4
    container_name: yunyu-mysql
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-yunyu}
      MYSQL_USER: ${MYSQL_USER:-yunyu}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-yunyu123456}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123456}
      TZ: ${TZ:-Asia/Shanghai}
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - ../yunyu_mysql_data:/var/lib/mysql
    networks:
      - yunyu-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-p${MYSQL_ROOT_PASSWORD:-root123456}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  yunyu-server-native:
    image: ${YUNYU_SERVER_NATIVE_IMAGE:-ghcr.io/idea-flow/yunyu-server-native:latest}
    container_name: yunyu-server-native
    restart: unless-stopped
    depends_on:
      yunyu-mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod,native}
      SPRING_DATASOURCE_URL: jdbc:mysql://yunyu-mysql:3306/${MYSQL_DATABASE:-yunyu}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER:-yunyu}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-yunyu123456}
      YUNYU_JWT_SECRET: ${YUNYU_JWT_SECRET:-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef}
      TZ: ${TZ:-Asia/Shanghai}
    ports:
      - "${SERVER_PORT:-20000}:20000"
    networks:
      - yunyu-network

  yunyu-web:
    image: ${YUNYU_WEB_IMAGE:-ghcr.io/idea-flow/yunyu-web:latest}
    container_name: yunyu-web
    restart: unless-stopped
    depends_on:
      - yunyu-server-native
    environment:
      NUXT_PUBLIC_API_BASE: ${YUNYU_PUBLIC_API_BASE:-http://yunyu-server-native:20000}
      TZ: ${TZ:-Asia/Shanghai}
    ports:
      - "${WEB_PORT:-3000}:3000"
    networks:
      - yunyu-network

networks:
  yunyu-network:
    name: yunyu-network
```

### 4.2 `/docker/docker-compose-server.yml`（仅后端）

包含两个服务：MySQL + Native 后端。前端部署在 Cloudflare Pages 时使用此文件。

```yaml
services:
  yunyu-mysql:
    image: mysql:8.4
    container_name: yunyu-mysql
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-yunyu}
      MYSQL_USER: ${MYSQL_USER:-yunyu}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-yunyu123456}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root123456}
      TZ: ${TZ:-Asia/Shanghai}
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - ../yunyu_mysql_data:/var/lib/mysql
    networks:
      - yunyu-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-p${MYSQL_ROOT_PASSWORD:-root123456}"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  yunyu-server-native:
    image: ${YUNYU_SERVER_NATIVE_IMAGE:-ghcr.io/idea-flow/yunyu-server-native:latest}
    container_name: yunyu-server-native
    restart: unless-stopped
    depends_on:
      yunyu-mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod,native}
      SPRING_DATASOURCE_URL: jdbc:mysql://yunyu-mysql:3306/${MYSQL_DATABASE:-yunyu}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER:-yunyu}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-yunyu123456}
      YUNYU_JWT_SECRET: ${YUNYU_JWT_SECRET:-0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef}
      TZ: ${TZ:-Asia/Shanghai}
    ports:
      - "${SERVER_PORT:-20000}:20000"
    networks:
      - yunyu-network

networks:
  yunyu-network:
    name: yunyu-network
```

### 4.3 `/docker/frontend/Dockerfile`（前端，新增）

Nuxt 4 SSR 模式，两阶段构建：

```dockerfile
# syntax=docker/dockerfile:1.7

# 构建阶段：安装依赖并构建 Nuxt 应用
FROM node:22-alpine AS builder

WORKDIR /app

# 安装 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

# 先复制 lockfile 和 package.json，利用 Docker 层缓存
COPY yunyu-web/package.json yunyu-web/pnpm-lock.yaml ./

RUN --mount=type=cache,target=/root/.pnpm-store \
    pnpm install --frozen-lockfile

# 复制源码并构建
COPY yunyu-web/ ./

RUN pnpm build

# 运行阶段：只保留 .output 目录
FROM node:22-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/.output ./

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000

CMD ["node", "server/index.mjs"]
```

> **注意**：前端 Dockerfile 的构建上下文为项目根目录，`COPY yunyu-web/` 从根目录复制前端源码。

### 4.4 `/docker/backend/Dockerfile.jvm`（JVM 版，原 `docker/jar/Dockerfile`）

内容与原文件一致，无需修改，仅移动位置。

### 4.5 `/docker/backend/Dockerfile.native`（Native 版，原 `docker/native-image/Dockerfile`）

内容与原文件一致，无需修改，仅移动位置。

### 4.6 `/docker/.env.example`（新增）

```bash
# 数据库配置
MYSQL_DATABASE=yunyu
MYSQL_USER=yunyu
MYSQL_PASSWORD=请替换成强密码
MYSQL_ROOT_PASSWORD=请替换成强密码
MYSQL_PORT=3306

# 后端配置
YUNYU_SERVER_NATIVE_IMAGE=ghcr.io/idea-flow/yunyu-server-native:latest
YUNYU_JWT_SECRET=请替换成64位随机字符串
SERVER_PORT=20000
SPRING_PROFILES_ACTIVE=prod,native

# 前端配置（全局模式使用）
YUNYU_WEB_IMAGE=ghcr.io/idea-flow/yunyu-web:latest
YUNYU_PUBLIC_API_BASE=https://api.你的域名
WEB_PORT=3000

# 时区
TZ=Asia/Shanghai
```

---

## 五、文件变更清单

### 新增

| 文件 | 说明 |
|---|---|
| `/docker/docker-compose.yml` | 全局启动（前端 + Native 后端 + MySQL） |
| `/docker/docker-compose-server.yml` | 仅后端（Native 后端 + MySQL） |
| `/docker/frontend/Dockerfile` | 前端 Nuxt SSR 镜像 |
| `/docker/.env.example` | 环境变量示例 |
| `/docker/backend/Dockerfile.jvm` | JVM 版后端（从 `docker/jar/` 迁移） |
| `/docker/backend/Dockerfile.native` | Native 版后端（从 `docker/native-image/` 迁移） |

### 删除

| 文件/目录 | 原因 |
|---|---|
| `/docker-compose.yml`（根目录） | 迁移到 `/docker/docker-compose-server.yml` |
| `/docker/docker-compose.yml` | 被新的全局文件替代 |
| `/docker/native-image/`（整个目录） | Dockerfile 迁移到 `backend/`，compose 文件被新文件替代 |
| `/docker/jar/`（整个目录） | Dockerfile 迁移到 `backend/` |
| `/yunyu-server/Dockerfile` | 与 `docker/backend/Dockerfile.jvm` 重复 |

### 更新

| 文件 | 变更内容 |
|---|---|
| `/.dockerignore` | 调整路径，确保前端构建上下文正确 |
| 相关部署文档（04、07、08） | 更新 compose 文件路径引用 |

---

## 六、使用方式（重构后）

### 场景一：全栈部署（前端 + 后端 + 数据库）

```bash
cd Yunyu
mkdir -p yunyu_mysql_data
docker compose -f docker/docker-compose.yml pull
docker compose -f docker/docker-compose.yml up -d
```

### 场景二：仅后端（前端部署在 Cloudflare Pages）

```bash
cd Yunyu
mkdir -p yunyu_mysql_data
docker compose -f docker/docker-compose-server.yml pull
docker compose -f docker/docker-compose-server.yml up -d
```

### 场景三：本地构建 Native 镜像

```bash
cd Yunyu
docker build -t ghcr.io/idea-flow/yunyu-server-native:latest \
  -f docker/backend/Dockerfile.native .
```

### 场景四：本地构建前端镜像

```bash
cd Yunyu
docker build -t ghcr.io/idea-flow/yunyu-web:latest \
  -f docker/frontend/Dockerfile .
```

---

## 七、注意事项

1. 所有 compose 文件都从 `/docker` 目录执行，MySQL 数据挂载路径 `../yunyu_mysql_data` 指向项目根目录同级
2. 前端镜像的构建上下文是项目根目录（不是 `yunyu-web/`），因为 Dockerfile 需要 `COPY yunyu-web/`
3. 正式环境必须修改 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`YUNYU_JWT_SECRET`
4. 不要执行 `docker compose down -v`，避免误删数据库数据
