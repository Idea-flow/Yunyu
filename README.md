# 云屿 / Yunyu

`Yunyu` 是一个面向个人创作与内容沉淀的前后端分离内容系统，目标不是“再做一个模板化博客”，而是做一个兼具审美表达、内容管理与后续平台扩展能力的内容站。

当前仓库包含前台站点、后台管理后端、部署脚本、数据库基线 SQL 与较完整的产品/架构文档，适合作为个人内容品牌站、博客系统和后续内容平台的基础工程。

## 项目定位

- 强调内容表达、阅读体验与页面质感
- 支持从个人博客逐步扩展到内容平台
- 前后端分离，兼顾 SEO、运营管理与后续能力演进
- 内容主数据存放于服务端，前端负责 SSR 输出与体验呈现

## 当前技术栈

- 前端：`Nuxt 4`、`Vue 3`、`Tailwind CSS 4`、`@nuxt/ui`
- 后端：`Spring Boot 4`、`Spring Security`、`MyBatis-Plus`
- 数据库：当前仓库默认按 `MySQL 8` 落地
- 接口文档：`OpenAPI 3` + `Swagger UI`
- 部署方向：前端可部署到 `Cloudflare Pages`，后端可部署到容器或传统云主机
- Native 支持：提供 `GraalVM Native Image` 相关支持模块

## 仓库结构

```text
.
├── yunyu-web/                    # Nuxt 4 前端，承载前台站点与后续后台界面
├── yunyu-server/                 # Spring Boot 后端服务
├── yunyu-native-image-support/   # Native Image 兼容与支持模块
├── docs/                         # 产品、架构、前后端、部署、运维文档
├── docker/                       # Docker 相关构建与部署文件
│   ├── docker-compose.yml        # 纯 Docker 模式（nginx + 前后端 + 数据库）
│   ├── docker-compose-server.yml # 仅后端模式（Cloudflare Pages + 1Panel）
│   ├── backend/                  # 后端 Dockerfile（JVM / Native）
│   ├── frontend/                 # 前端 Dockerfile
│   ├── nginx/                    # nginx 配置
│   └── .env.example              # 部署环境变量示例
└── scripts/                      # 数据库、发布、升级脚本
```

## 当前已落地能力

后端当前已经不是空骨架，已落地的业务模块主要包括：

- 内容资源：文章、分类、标签、专题、评论、友链
- 站点配置：站点基础配置、首页配置、公开聚合接口、存储配置
- 用户与认证：注册、登录、当前用户、后台用户管理
- 平台扩展：附件管理、内容访问控制、AI 提供商配置

更完整的模块地图可查看：

- [docs/后端/08-当前真实模块地图.md](./docs/后端/08-当前真实模块地图.md)

## 快速开始

### 环境要求

- `JDK 25`
- `Node.js` 与 `pnpm`
- `MySQL 8`
- 可选：`Docker` / `Docker Compose`

说明：

- 前端包管理器当前使用 `pnpm`
- 后端仓库自带 `Maven Wrapper`，优先使用 `./mvnw`
- 虽然架构文档里讨论过 `PostgreSQL` 演进方向，但当前这份代码仓库的实际运行配置和 SQL 基线都基于 `MySQL 8`

### 本地开发

#### 1. 启动后端

进入后端目录：

```bash
cd yunyu-server
```

默认开发配置位于 `src/main/resources/application-dev.yml`，当前默认连接：

- 数据库地址：`127.0.0.1:3306`
- 数据库名：`yunyu`
- 用户名：`root`
- 密码：`123456`

启动命令：

```bash
./mvnw spring-boot:run
```

后端默认地址：

- 服务地址：`http://127.0.0.1:20000`
- Swagger：`http://127.0.0.1:20000/swagger-ui.html`
- 健康检查：`http://127.0.0.1:20000/actuator/health`

首次启动说明：

- 如果目标数据库还不存在，后端会在启动阶段自动创建数据库、执行初始化建表脚本，并写入默认超级管理员账号
- 默认超级管理员账号为：`yunyu / yunyu`
- 这个默认账号只适合本地开发或首次引导，非本地环境请尽快修改

重要提示：

- 当前自动引导更适合“数据库尚未创建”的场景
- 如果你的数据库已经存在但库内还没有表，请先手动执行初始化 SQL，否则应用不会替你补表
- 可使用的基线脚本见：[docs/技术/sql/init.sql](./docs/技术/sql/init.sql)

手动导入示例：

```bash
mysql -h 127.0.0.1 -P 3306 -uroot -p123456 yunyu < docs/技术/sql/init.sql
```

如需演示数据，可额外执行：

- [docs/技术/sql/demo/002-seed-demo-data.sql](./docs/技术/sql/demo/002-seed-demo-data.sql)

#### 2. 启动前端

进入前端目录并安装依赖：

```bash
cd yunyu-web
pnpm install
```

配置后端 API 地址：

```bash
YUNYU_PUBLIC_API_BASE=http://127.0.0.1:20000
```

启动开发环境：

```bash
pnpm dev
```

前端默认地址：

- `http://127.0.0.1:19999`

常用命令：

```bash
pnpm dev
pnpm build
pnpm preview
pnpm generate
```

### Docker 部署

> **只需下载 `/docker` 目录**，无需克隆整个仓库（源码不参与部署）。

#### 方式一：sparse-checkout（推荐，仅下载 docker 目录）

> 注意：`--filter` 与 `--sparse` 同时使用在 Git 2.25.x 有已知 bug，建议拆成以下两步执行。

```bash
# 克隆仓库元数据（不下载任何文件内容，速度极快）
git clone --filter=blob:none --no-checkout https://github.com/Idea-flow/Yunyu.git

# 进入仓库目录
cd Yunyu

# 启用 sparse-checkout，--cone 模式按目录粒度过滤（性能更好）
git sparse-checkout init --cone

# 声明只需要 docker 这一个目录
git sparse-checkout set docker

# 实际检出文件（此时只会下载 docker/ 目录的内容）
git checkout main
```

执行后本地只有 `docker/` 目录，体积极小。

#### 方式二：完整克隆

```bash
git clone https://github.com/Idea-flow/Yunyu.git
cd Yunyu
```

---

项目提供两种部署模式，按实际情况选择其一：

**模式 A — 纯 Docker（nginx 统一入口）**

适合：服务器只装了 Docker，前后端全部容器化，nginx 作为唯一对外入口（80 端口）。

```bash
# 1. 创建数据库挂载目录
mkdir -p yunyu_mysql_data

# 2. 创建环境变量文件（放在 docker/ 目录下，与 compose 文件同级）
cp docker/.env.example docker/.env

# 3. （可选）修改 docker/.env 中的关键配置
#    不修改也能直接启动，内置了一套默认值，适合快速体验
#    正式环境建议替换以下三项：
#    MYSQL_PASSWORD=强密码
#    MYSQL_ROOT_PASSWORD=强密码
#    YUNYU_JWT_SECRET=openssl rand -hex 32 生成的64位字符串
#    NGINX_HTTP_PORT=80（80端口被占用时改为其他端口）

# 4. 启动所有服务
docker compose -f docker/docker-compose.yml pull
docker compose -f docker/docker-compose.yml up -d

# 5. 验证
docker compose -f docker/docker-compose.yml ps
curl http://服务器IP/actuator/health
```

访问：`http://服务器IP`

> **80 端口被占用？** 在 `.env` 中设置 `NGINX_HTTP_PORT=8080`（换成任意空闲端口），无需改 compose 文件，之后访问 `http://服务器IP:8080`。
> 数据库 3306 端口不受影响，MySQL 容器不对外暴露端口，只在内部网络通信，不会与宿主机冲突。

**默认管理员账号**：后端首次启动时自动创建，账号 `yunyu`，密码 `yunyu`，登录后请立即修改密码。

详细说明：[docs/部署/11-纯Docker模式部署说明（nginx统一入口）.md](./docs/部署/11-纯Docker模式部署说明（nginx统一入口）.md)



部署成功图片实例:

![image-20260523151617575](https://oss.6667000.xyz/2026/05/1df07e4a4e2513137fd41e29e7ecebf3.webp)





---

**模式 B — Cloudflare Pages + 1Panel**

适合：前端部署到 Cloudflare Pages（免费 CDN），后端 + 数据库部署在服务器，1Panel 提供反向代理和 HTTPS。

```bash
# 1. 创建数据库挂载目录
mkdir -p yunyu_mysql_data

# 2. 创建环境变量文件（放在 docker/ 目录下，与 compose 文件同级）
cp docker/.env.example docker/.env

# 3. （可选）修改 docker/.env 中的关键配置
#    不修改也能直接启动，内置了一套默认值，适合快速体验
#    正式环境建议替换以下三项：
#    MYSQL_PASSWORD=强密码
#    MYSQL_ROOT_PASSWORD=强密码
#    YUNYU_JWT_SECRET=openssl rand -hex 32 生成的64位字符串

# 4. 启动后端服务（仅后端 + 数据库）
docker compose -f docker/docker-compose-server.yml pull
docker compose -f docker/docker-compose-server.yml up -d

# 5. 验证后端
curl http://127.0.0.1:20000/actuator/health
```

前端在 Cloudflare Pages 控制台连接 GitHub 仓库，设置环境变量 `YUNYU_PUBLIC_API_BASE=https://api.yourdomain.com` 后触发部署。

详细说明：[docs/部署/12-Cloudflare Pages + 1Panel 模式部署说明.md](./docs/部署/12-Cloudflare%20Pages%20+%201Panel%20模式部署说明.md)

## 数据库与初始化说明

仓库当前维护了两套你最常会用到的 SQL 入口：

- 基线脚本：[docs/技术/sql/init.sql](./docs/技术/sql/init.sql)
- 增量升级脚本目录：[docs/技术/sql/upgrade/README.md](./docs/技术/sql/upgrade/README.md)

后端类路径下也保留了启动自动引导所使用的脚本：

- [yunyu-server/src/main/resources/db/init/001-init-schema.sql](./yunyu-server/src/main/resources/db/init/001-init-schema.sql)

如果你是第一次接手这个仓库，建议优先理解 SQL 管理规则：

- [docs/技术/sql/README.md](./docs/技术/sql/README.md)

## 文档导航

如果你想快速理解这个项目，推荐按下面顺序阅读：

1. [docs/架构/01-总体技术架构.md](./docs/架构/01-总体技术架构.md)
2. [docs/产品/01-产品定位与路线图.md](./docs/产品/01-产品定位与路线图.md)
3. [docs/后端/08-当前真实模块地图.md](./docs/后端/08-当前真实模块地图.md)
4. [docs/前端/README.md](./docs/前端/README.md)
5. [docs/部署/README.md](./docs/部署/README.md)
6. [docs/运维/README.md](./docs/运维/README.md)

文档总入口：

- [docs/README.md](./docs/README.md)

## 部署与镜像

仓库已经包含 GitHub Actions 工作流，可用于构建：

- `yunyu-server` 常规镜像
- `yunyu-server` Native Image 镜像

相关工作流位于：

- [build-yunyu-web-image.yml](./.github/workflows/build-yunyu-web-image.yml)
- [build-yunyu-server-image-jar.yml](./.github/workflows/build-yunyu-server-image-jar.yml)
- [build-yunyu-server-native-image.yml](./.github/workflows/build-yunyu-server-native-image.yml)

## 当前阶段说明

`Yunyu` 当前处于“个人内容站到内容平台”的持续建设阶段。

如果你是第一次打开这个仓库，最适合的理解方式是：

- 把它看成一个已经具备真实后端业务模块的内容系统
- 把 `docs/` 看成产品、架构与部署设计的主入口
- 把根目录 `README.md` 看成快速启动与仓库导览入口
