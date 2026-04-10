# Linux Native Image Docker 部署说明

## 文档目标

本文档用于说明如何在 Linux 服务器上部署 `yunyu-server` 的 Native Image Docker 镜像。

当前 Native Docker 部署使用的核心文件为：

- [build-yunyu-server-native-image.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/.github/workflows/build-yunyu-server-native-image.yml)
- [Dockerfile](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker/native-image/Dockerfile)
- [docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker/native-image/docker-compose.yml)

## 一、一句话结论

如果 `main-native-image` 分支已经成功推送了 Native 镜像，那么在 Linux 服务器上最推荐的启动方式是：

```bash
docker compose -f docker/native-image/docker-compose.yml pull
docker compose -f docker/native-image/docker-compose.yml up -d
```

## 二、当前镜像说明

当前 Native 镜像默认命名规划为：

```bash
ghcr.io/<github-owner>/yunyu-server-native:latest
```

镜像特点：

1. 基于 GraalVM Native Image 构建
2. 工作流默认同时构建 `linux/amd64` 与 `linux/arm64`
3. 运行时直接启动 Native 二进制，不再经过 `java -jar`
4. 在 macOS / Windows 上也可以通过 Docker Desktop 拉取并运行同一份 Linux 容器镜像

## 三、部署前准备

请先确认服务器已经具备以下条件：

1. 已安装 `git`
2. 已安装 `Docker`
3. 已安装 `docker compose`
4. 服务器可以访问 `ghcr.io`

可先执行：

```bash
git --version
docker --version
docker compose version
```

## 四、推荐部署方式

### 步骤 1：拉取仓库

```bash
git clone https://github.com/Idea-flow/Yunyu.git
```

### 步骤 2：进入项目目录

```bash
cd Yunyu
```

### 步骤 3：切换到 Native 分支

```bash
git checkout main-native-image
```

### 步骤 4：准备数据库目录

Native 版 `docker-compose` 默认把 MySQL 数据挂载到当前目录下的 `./yunyu_mysql_data`：

```bash
mkdir -p ./yunyu_mysql_data
```

### 步骤 5：按需创建 `.env`

如果你需要覆盖默认镜像地址、数据库密码或端口，建议在仓库根目录创建 `.env`。

示例：

```bash
cat > .env <<'EOF'
YUNYU_SERVER_NATIVE_IMAGE=ghcr.io/<github-owner>/yunyu-server-native:latest
MYSQL_PASSWORD=请替换成强密码
MYSQL_ROOT_PASSWORD=请替换成强密码
YUNYU_JWT_SECRET=请替换成正式密钥
EOF
```

### 步骤 6：启动 Native 服务

```bash
docker compose -f docker/native-image/docker-compose.yml pull
docker compose -f docker/native-image/docker-compose.yml up -d
```

### 步骤 7：查看状态与日志

```bash
docker compose -f docker/native-image/docker-compose.yml ps
docker compose -f docker/native-image/docker-compose.yml logs -f yunyu-server-native
```

## 五、启动后验证

### 1. 健康检查

```bash
curl http://127.0.0.1:20000/actuator/health
```

### 2. 登录接口

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"account":"yunyu","password":"yunyu"}' \
  http://127.0.0.1:20000/api/auth/login
```

### 3. Lambda 查询接口

```bash
curl -i 'http://127.0.0.1:20000/api/site/posts?pageNo=1&pageSize=10'
```

## 六、与 JVM 版部署的区别

当前仓库里同时存在两套后端镜像部署方式：

1. JVM 版
   - 使用根目录 [docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker-compose.yml)
   - 镜像默认是 `ghcr.io/<github-owner>/yunyu-server:latest`

2. Native 版
   - 使用 [docker/native-image/docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker/native-image/docker-compose.yml)
   - 镜像默认是 `ghcr.io/<github-owner>/yunyu-server-native:latest`

部署时请明确使用哪一套文件，不要混用。

## 七、常用升级命令

如果 Native 镜像已经更新，最常用的升级命令是：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

升级后查看日志：

```bash
docker compose -f docker/native-image/docker-compose.yml logs -f yunyu-server-native
```

## 八、注意事项

1. 正式环境必须修改 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` 与 `YUNYU_JWT_SECRET`
2. 不要执行 `docker compose down -v`，避免误删数据库数据
3. 不要误删 `./yunyu_mysql_data`
4. Native 运行时建议同时启用 `prod,native` 两个 profile
5. 如果镜像仓库是私有的，请先执行 `docker login ghcr.io`
