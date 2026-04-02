# Linux 升级最新镜像说明

## 文档目标

本文档用于说明当 `yunyu-server` 的代码已经更新、镜像也已经重新打包后，Linux 服务器如何拉取最新镜像并重新启动服务。

## 一、一句话结论

如果你当前服务器已经在运行 `docker-compose.yml` 中的服务，升级最新版后端镜像最常用的命令就是：

第一次执行命令记录:




```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

## 二、当前场景说明

你当前的部署方式是：

- 后端镜像：`ghcr.io/idea-flow/yunyu-server:latest`
- 编排方式：`docker-compose.yml`
- 已经有正在运行的容器：`yunyu-server` 和 `yunyu-mysql`

所以升级的核心逻辑是：

1. 先拉取最新的 `yunyu-server` 镜像
2. 再用最新镜像重建 `yunyu-server` 容器
3. `yunyu-mysql` 保持不动

## 三、标准升级命令

进入你的部署目录后执行：

```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

这条命令的含义是：

- `docker compose pull yunyu-server`
  - 只拉取 `yunyu-server` 服务对应的最新镜像
  - 不会动数据库容器

- `&&`
  - 表示前一条命令成功后，再执行后一条

- `docker compose up -d yunyu-server`
  - 用最新镜像重新创建并后台启动 `yunyu-server`
  - 不会删除 `yunyu_mysql_data` 数据目录
  - 不会影响 `yunyu-mysql` 的数据库数据

## 四、推荐升级步骤

### 步骤 1：进入部署目录

例如：

```bash
cd /opt/yunyu
```

### 步骤 2：拉取最新后端镜像并重启后端

```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

### 步骤 3：查看后端容器状态

```bash
docker compose ps
```

### 步骤 4：查看后端启动日志

```bash
docker compose logs -f yunyu-server
```

### 步骤 5：验证健康检查接口

```bash
curl http://127.0.0.1:20000/actuator/health
```

## 五、如果你也更新了 `docker-compose.yml`

如果仓库中的 `docker-compose.yml` 也改了，例如：

- 环境变量变了
- 端口变了
- 网络配置变了
- 数据目录挂载变了

那么先更新本地部署文件，再执行升级：

```bash
curl -L -o docker-compose.yml https://raw.githubusercontent.com/Idea-flow/Yunyu/main/docker-compose.yml
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

## 六、如果你想强制完整重建后端容器

可以使用：

```bash
docker compose up -d --force-recreate yunyu-server
```

如果你想先拉最新镜像，再强制重建：

```bash
docker compose pull yunyu-server && docker compose up -d --force-recreate yunyu-server
```

适用场景：

- 你怀疑容器没有正确重建
- 你修改了环境变量
- 你需要确保一定是新容器

## 七、不要这样做

不建议为了升级后端执行：

```bash
docker compose down -v
```

原因是：

- `-v` 可能删除卷数据
- 你当前 MySQL 数据目录虽然是 `./yunyu_mysql_data` 绑定挂载，不属于 Docker 命名卷，但这个命令依然没有必要
- 升级后端镜像不需要停掉整个数据库

## 八、最常用的三条命令

只升级后端镜像：

```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

升级后看日志：

```bash
docker compose logs -f yunyu-server
```

升级后做健康检查：

```bash
curl http://127.0.0.1:20000/actuator/health
```
