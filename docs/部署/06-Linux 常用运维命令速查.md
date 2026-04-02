# Linux 常用运维命令速查

## 文档目标

本文档用于集中整理 `Yunyu` 在 Linux 服务器上基于 `docker compose` 部署时最常用的运维命令，方便日常启动、停止、重启、升级、查看日志与排查问题。

## 一、一句话结论

日常最常用的就是这几条：

```bash
docker compose up -d
docker compose ps
docker compose logs -f yunyu-server
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

## 二、进入部署目录

先进入你的部署目录，例如：

```bash
cd /opt/yunyu
```

后续所有命令都默认在这个目录下执行。

## 三、启动服务

启动全部服务：

```bash
docker compose up -d
```

含义：

- 按 `docker-compose.yml` 启动全部服务
- 如果本地没有镜像，会自动尝试拉取
- `-d` 表示后台运行

只启动后端：

```bash
docker compose up -d yunyu-server
```

只启动数据库：

```bash
docker compose up -d yunyu-mysql
```

## 四、停止服务

停止全部服务：

```bash
docker compose stop
```

只停止后端：

```bash
docker compose stop yunyu-server
```

只停止数据库：

```bash
docker compose stop yunyu-mysql
```

## 五、重启服务

重启全部服务：

```bash
docker compose restart
```

只重启后端：

```bash
docker compose restart yunyu-server
```

只重启数据库：

```bash
docker compose restart yunyu-mysql
```

## 六、升级最新后端镜像

拉取最新后端镜像并重启后端：

```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
```

如果需要强制重建后端容器：

```bash
docker compose pull yunyu-server && docker compose up -d --force-recreate yunyu-server
```

## 七、查看容器状态

查看当前服务状态：

```bash
docker compose ps
```

查看所有 Docker 容器：

```bash
docker ps -a
```

## 八、查看日志

实时查看后端日志：

```bash
docker compose logs -f yunyu-server
```

实时查看数据库日志：

```bash
docker compose logs -f yunyu-mysql
```

查看最近 200 行后端日志：

```bash
docker compose logs --tail=200 yunyu-server
```

查看最近 200 行数据库日志：

```bash
docker compose logs --tail=200 yunyu-mysql
```

## 九、查看镜像

查看本机 Docker 镜像：

```bash
docker images
```

查看 `yunyu-server` 相关镜像：

```bash
docker images | grep yunyu-server
```

手动拉取后端镜像：

```bash
docker pull ghcr.io/idea-flow/yunyu-server:latest
```

## 十、查看网络和数据目录

查看 Docker 网络：

```bash
docker network ls
```

查看 `yunyu-network` 详情：

```bash
docker network inspect yunyu-network
```

查看 MySQL 数据目录：

```bash
ls -lah ./yunyu_mysql_data
```

## 十一、健康检查与接口验证

检查后端健康接口：

```bash
curl http://127.0.0.1:20000/actuator/health
```

检查 Swagger 文档页面：

```bash
http://服务器IP:20000/swagger-ui.html
```

## 十二、更新部署文件

如果仓库里的 `docker-compose.yml` 更新了，可重新下载：

```bash
curl -L -o docker-compose.yml https://raw.githubusercontent.com/Idea-flow/Yunyu/main/docker-compose.yml
```

下载后重新应用：

```bash
docker compose up -d
```

## 十三、谨慎使用的命令

不要轻易执行：

```bash
docker compose down -v
```

原因：

- 这通常用于彻底销毁服务
- 容易误删资源
- 对当前项目的日常升级和重启来说没有必要

如果只是临时停服务，优先使用：

```bash
docker compose stop
```

## 十四、推荐的日常操作组合

首次启动：

```bash
docker compose up -d
docker compose ps
docker compose logs -f yunyu-server
```

日常升级后端：

```bash
docker compose pull yunyu-server && docker compose up -d yunyu-server
docker compose logs -f yunyu-server
curl http://127.0.0.1:20000/actuator/health
```

快速排查：

```bash
docker compose ps
docker compose logs --tail=200 yunyu-server
docker compose logs --tail=200 yunyu-mysql
```
