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
├── docker/                       # Docker / Native Image 相关构建文件
├── scripts/                      # 数据库、发布、升级脚本
├── docker-compose.yml            # 后端 + MySQL 部署编排
└── .env.deploy.example           # 部署环境变量示例
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

根目录 `docker-compose.yml` 当前主要用于启动：

- `MySQL 8`
- `yunyu-server` 后端镜像

启动前先复制环境变量文件：

```bash
cp .env.deploy.example .env
```

建议至少修改以下配置：

```bash
MYSQL_PASSWORD=请替换成强密码
MYSQL_ROOT_PASSWORD=请替换成强密码
YUNYU_JWT_SECRET=请替换成正式密钥
YUNYU_SERVER_IMAGE=ghcr.io/<github-owner>/yunyu-server:latest
```

启动服务：

```bash
docker compose pull
docker compose up -d
```

查看状态：

```bash
docker compose ps
docker compose logs -f yunyu-server
```

说明：

- 当前 `docker-compose.yml` 只负责后端和数据库，不包含前端站点
- 由于 `MySQL` 容器会预创建业务库，首次部署时建议手动导入一次基线 SQL
- 更完整的部署步骤见：[docs/部署/02-部署执行步骤.md](./docs/部署/02-部署执行步骤.md)

可参考：

```bash
docker exec -i yunyu-mysql mysql -uroot -p<你的-root-密码> yunyu < docs/技术/sql/init.sql
```

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

- [build-yunyu-server-image.yml](./.github/workflows/build-yunyu-server-image.yml)
- [build-yunyu-server-image-jar.yml](./.github/workflows/build-yunyu-server-image-jar.yml)
- [build-yunyu-server-native-image.yml](./.github/workflows/build-yunyu-server-native-image.yml)

## 当前阶段说明

`Yunyu` 当前处于“个人内容站到内容平台”的持续建设阶段。

如果你是第一次打开这个仓库，最适合的理解方式是：

- 把它看成一个已经具备真实后端业务模块的内容系统
- 把 `docs/` 看成产品、架构与部署设计的主入口
- 把根目录 `README.md` 看成快速启动与仓库导览入口
