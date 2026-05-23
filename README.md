# 云屿 / Yunyu

`Yunyu` 是一个面向个人创作与内容沉淀的前后端分离内容系统，兼具审美表达、内容管理与后续平台扩展能力，适合作为个人内容品牌站、博客系统和内容平台的基础工程。

>  [LINUX DO 社区](https://linux.do/) 

## 演示地址

[https://yunyu.ideaflow.top](https://yunyu.ideaflow.top)

## 项目地址

- GitHub：[https://github.com/YunyuVv/Yunyu](https://github.com/YunyuVv/Yunyu)
- Gitee：[https://gitee.com/YunyuVv/Yunyu](https://gitee.com/YunyuVv/Yunyu)

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | `Nuxt 4`、`Vue 3`、`Tailwind CSS 4`、`@nuxt/ui` |
| 后端 | `Spring Boot 4`、`Spring Security`、`MyBatis-Plus`   、  `native` |
| 数据库 | `MySQL 8` |
| 接口文档 | `OpenAPI 3` + `Swagger UI` |
| 部署 | `Docker Compose`、`GraalVM Native Image`、`Cloudflare Pages` |

### 关于 GraalVM Native Image

后端提供 **GraalVM Native Image** 支持，编译为原生可执行文件后：

- 启动时间从数秒缩短到毫秒级

- 运行内存相比 JVM 模式大幅降低，**2 核 2G 服务器即可流畅部署运行** 常驻内存80mb-110mb

  ![内存占用图](https://oss.6667000.xyz/2026/05/5372911edda0144597b4efeaea2c996f.webp)

- 无需安装 JDK 运行环境，镜像体积更小

## 项目功能

- **内容管理**：文章、分类、标签、专题、评论、友链
- **站点配置**：首页配置、站点基础信息、存储配置
- **用户与认证**：注册、登录、后台用户管理、权限控制
- **附件管理**：文件上传与存储
- **前台展示**：SSR 渲染，支持 SEO，阅读体验优先
- **后台管理**：完整的内容与配置管理后台
- **AI 扩展**：AI 提供商配置接入
- **Agent skill管理**：Agent skill管理整个项目(yunyu-admin-operator)

## 项目截图

web首页:

![首页](https://oss.6667000.xyz/2026/05/ea70ccc3dc3c706523eef54ae0eceef1.webp)



后台首页:

![后台首页](https://oss.6667000.xyz/2026/05/29211e9e88d242c5efdf85de64ba10f8.webp)

文章详情页:



![文章详情页](https://oss.6667000.xyz/2026/05/37a5f4c14876ec00c2497507bc5e6c09.webp)





## 安装使用

### 环境要求

- `JDK 25`（本地开发）
- `Node.js` + `pnpm`（本地开发）
- `MySQL 8`（本地开发）
- `Docker` + `Docker Compose`（服务器部署）

---

### 本地开发

#### 1. 启动后端

```bash
cd yunyu-server
./mvnw spring-boot:run
```

默认连接本地 MySQL：`127.0.0.1:3306`，数据库名 `yunyu`，用户名 `root`，密码 `123456`（可在 `application-dev.yml` 修改）。

首次启动会自动建库建表，并创建默认管理员账号 `yunyu / yunyu`。

- 服务地址：`http://127.0.0.1:20000`
- Swagger：`http://127.0.0.1:20000/swagger-ui.html`

#### 2. 启动前端

```bash
cd yunyu-web
pnpm install
pnpm dev
```

在 `yunyu-web/.env` 中配置后端地址：

```bash
YUNYU_PUBLIC_API_BASE=http://127.0.0.1:20000
```

前端地址：`http://127.0.0.1:19999`

---

### Docker 部署

> 只需下载 `docker/` 目录，无需克隆整个仓库。

```bash
# 仅下载 docker/ 目录（推荐）
git clone --filter=blob:none --no-checkout https://github.com/YunyuVv/Yunyu.git
cd Yunyu
git sparse-checkout init --cone
git sparse-checkout set docker
git checkout main
```

项目提供两种部署模式：

---

#### 模式 A — 纯 Docker（nginx 统一入口）

适合：服务器只装了 Docker，前后端全部容器化，nginx 作为唯一对外入口。

```bash
# 1. 创建数据库挂载目录
mkdir -p yunyu_mysql_data

# 2. 创建环境变量文件
cp docker/.env.example docker/.env

# 3. （可选）修改 docker/.env 中的关键配置
#    正式环境建议替换以下几项：
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

> **80 端口被占用？** 在 `.env` 中设置 `NGINX_HTTP_PORT=8080` 即可，之后访问 `http://服务器IP:8080`。

**默认管理员账号**：后端首次启动时自动创建，账号 `yunyu`，密码 `yunyu`，登录后请立即修改密码。

部署成功示例：

![部署成功](https://oss.6667000.xyz/2026/05/1df07e4a4e2513137fd41e29e7ecebf3.webp)

详细说明：[纯Docker模式部署说明](./docs/部署/11-纯Docker模式部署说明（nginx统一入口）.md)

---

#### 模式 B — Cloudflare Pages + 1Panel

适合：前端部署到 Cloudflare Pages（免费 CDN），后端 + 数据库部署在服务器。

```bash
# 1. 创建数据库挂载目录
mkdir -p yunyu_mysql_data

# 2. 创建环境变量文件
cp docker/.env.example docker/.env

# 3. 启动后端服务（仅后端 + 数据库）
docker compose -f docker/docker-compose-server.yml pull
docker compose -f docker/docker-compose-server.yml up -d

# 4. 验证后端
curl http://127.0.0.1:20000/actuator/health
```

前端在 Cloudflare Pages 控制台连接 GitHub 仓库，设置环境变量 `YUNYU_PUBLIC_API_BASE=https://api.yourdomain.com` 后触发部署。

详细说明：[Cloudflare Pages + 1Panel 模式部署说明](./docs/部署/12-Cloudflare%20Pages%20+%201Panel%20模式部署说明.md)

---

## 文档

- [部署文档](./docs/部署/README.md)
- [架构设计](./docs/架构/01-总体技术架构.md)
- [后端模块地图](./docs/后端/08-当前真实模块地图.md)
- [前端文档](./docs/前端/README.md)
- [数据库设计](./docs/技术/sql/README.md)
- [运维手册](./docs/运维/README.md)
- [完整文档中心](./docs/README.md)
