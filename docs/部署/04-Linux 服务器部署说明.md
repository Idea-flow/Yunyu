# Linux 服务器部署说明

## 文档目标

本文档用于说明如何在 Linux 服务器上获取 `Yunyu` 的部署文件，并执行 `docker compose` 启动第一版服务。

项目地址：

- GitHub 仓库：[https://github.com/Idea-flow/Yunyu](https://github.com/Idea-flow/Yunyu)

## 一、一句话结论

第一版最推荐的方式是：在 Linux 服务器上直接拉取 `Yunyu` 仓库，进入项目目录，直接执行 `docker compose up -d`；只有当你需要覆盖默认值时，再额外创建 `.env`。

## 二、部署前准备

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

## 三、推荐方式：直接拉取整个仓库

### 步骤 1：克隆仓库

```bash
git clone https://github.com/Idea-flow/Yunyu.git
```

### 步骤 2：进入项目目录

```bash
cd Yunyu
```

### 步骤 3：直接使用默认值启动

当前 `docker-compose.yml` 已经带了默认值，默认镜像地址也是可用的：

```bash
ghcr.io/idea-flow/yunyu-server:latest
```

所以第一版可以先不创建 `.env`，直接启动。

## 四、启动服务

启动前，确保当前目录下存在 MySQL 数据目录，或允许 Docker 自动创建：

```bash
mkdir -p ./yunyu_mysql_data
```

然后执行：

```bash
docker compose up -d
```

查看状态：

```bash
docker compose ps
docker compose logs -f yunyu-server
```

## 五、需要覆盖默认值时再创建 `.env`

如果你后续需要修改密码、端口、时区或镜像版本，再在项目根目录创建 `.env` 即可。

最小示例：

```bash
cat > .env <<'EOF'
MYSQL_PASSWORD=请替换成强密码
MYSQL_ROOT_PASSWORD=请替换成强密码
YUNYU_JWT_SECRET=请替换成正式密钥
EOF
```

如果你想固定镜像版本，也可以加上：

```bash
YUNYU_SERVER_IMAGE=ghcr.io/idea-flow/yunyu-server:latest
```

修改后重新执行：

```bash
docker compose up -d
```

## 六、镜像拉取说明

当前后端镜像默认地址：

```bash
ghcr.io/idea-flow/yunyu-server:latest
```

如果镜像仓库是公开的，通常可以直接拉取：

```bash
docker pull ghcr.io/idea-flow/yunyu-server:latest
```

如果镜像仓库是私有的，需要先登录：

```bash
docker login ghcr.io
docker pull ghcr.io/idea-flow/yunyu-server:latest
```

## 七、启动后验证

### 1. 检查容器状态

```bash
docker compose ps
```

### 2. 检查后端健康检查接口

```bash
curl http://127.0.0.1:20000/actuator/health
```

### 3. 检查接口文档

浏览器访问：

```bash
http://服务器IP:20000/swagger-ui.html
```

## 八、后续接入

后端启动成功后，继续按第一版方案执行：

1. 在 `1Panel` 中将 `api.你的域名` 反向代理到 `http://127.0.0.1:20000`
2. 前端在 Cloudflare Pages 中配置 `YUNYU_PUBLIC_API_BASE=https://api.你的域名`
3. 联调前后端

## 九、注意事项

- 不要执行 `docker compose down -v`，否则可能删除数据库数据
- 不要误删当前目录下的 `./yunyu_mysql_data`
- `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` 和 `YUNYU_JWT_SECRET` 上线前必须替换
- 第一版可以不创建 `.env` 直接启动，但正式上线前建议补上自己的密码和密钥
