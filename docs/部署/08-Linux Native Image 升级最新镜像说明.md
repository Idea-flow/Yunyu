# Linux Native Image 升级最新镜像说明

## 文档目标

本文档用于说明当 `yunyu-server-native` 镜像已经在 `GHCR` 中更新后，Linux 服务器如何拉取最新 Native 镜像并重新启动服务。

当前文档对应的编排文件为：

- [docker/native-image/docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker/native-image/docker-compose.yml)

## 一、一句话结论

如果你当前服务器已经在运行 Native 版服务，最常用的升级命令就是：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

## 二、当前场景说明

你当前的部署方式是：

1. 后端镜像：`ghcr.io/<github-owner>/yunyu-server-native:latest`
2. 编排方式：`docker/native-image/docker-compose.yml`
3. 已经有正在运行的容器：`yunyu-server-native` 和 `yunyu-native-mysql`

所以升级的核心逻辑是：

1. 先拉取最新的 `yunyu-server-native` 镜像
2. 再用最新镜像重建 `yunyu-server-native` 容器
3. `yunyu-native-mysql` 保持不动

## 三、标准升级命令

进入部署目录后执行：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

这条命令的含义是：

1. `pull yunyu-server-native`
   - 只拉取 Native 后端服务的最新镜像
   - 不会动数据库容器

2. `up -d yunyu-server-native`
   - 用最新镜像重新创建并后台启动 Native 后端容器
   - 不会删除 `./yunyu_mysql_data`
   - 不会影响 MySQL 数据

## 四、推荐升级步骤

### 步骤 1：进入部署目录

例如：

```bash
cd /opt/Yunyu
```

### 步骤 2：切换到 Native 分支

```bash
git checkout main-native-image
git pull
```

### 步骤 3：拉取最新 Native 镜像并重启后端

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

### 步骤 4：查看容器状态

```bash
docker compose -f docker/native-image/docker-compose.yml ps
```

### 步骤 5：查看启动日志

```bash
docker compose -f docker/native-image/docker-compose.yml logs -f yunyu-server-native
```

### 步骤 6：验证健康检查

```bash
curl http://127.0.0.1:20000/actuator/health
```

## 五、如果你也更新了 Native compose 文件

如果仓库中的以下文件也改了：

1. `docker/native-image/docker-compose.yml`
2. `.env`
3. 数据库或端口相关环境变量

那建议先更新仓库文件，再执行升级：

```bash
git pull
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

## 六、如果你想强制重建容器

可以使用：

```bash
docker compose -f docker/native-image/docker-compose.yml up -d --force-recreate yunyu-server-native
```

如果你想先拉最新镜像，再强制重建：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d --force-recreate yunyu-server-native
```

适用场景：

1. 你怀疑容器没有正确重建
2. 你修改了环境变量
3. 你需要确保一定是新容器

## 七、不要这样做

不建议为了升级 Native 后端执行：

```bash
docker compose -f docker/native-image/docker-compose.yml down -v
```

原因是：

1. `-v` 可能删除卷数据
2. 当前 MySQL 数据目录是 `./yunyu_mysql_data` 绑定挂载，虽然不是 Docker 命名卷，但这个命令依然没有必要
3. 升级后端镜像不需要停掉整个数据库

## 八、最常用的三条命令

只升级 Native 后端镜像：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native && \
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

升级后看日志：

```bash
docker compose -f docker/native-image/docker-compose.yml logs -f yunyu-server-native
```

升级后做健康检查：

```bash
curl http://127.0.0.1:20000/actuator/health
```
