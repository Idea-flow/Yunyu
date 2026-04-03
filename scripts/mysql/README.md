# MySQL 同步脚本

## 目录说明

本目录用于放置 `Yunyu` 的 MySQL 数据库维护脚本，当前提供：

- `mysql-sync.env.example`：连接配置示例文件
- `backup-remote-db.sh`：备份远程数据库
- `reset-remote-db.sh`：删除远程数据库中的全部表
- `restore-remote-db.sh`：把备份文件恢复到远程数据库
- `run-sync-flow.sh`：按“备份 -> 同步 -> 输出恢复提示”的总流程执行
- `sync-local-to-remote.sh`：把本地数据库表结构和数据全量同步到远程数据库

## 使用前准备

1. 复制配置文件：

```bash
cp scripts/mysql/mysql-sync.env.example scripts/mysql/mysql-sync.env
```

2. 填写本地和远程数据库连接信息。

3. 确保当前机器已经安装并可用：

```bash
docker
```

当前脚本会直接使用 Docker 镜像内置的：

- `mysql`
- `mysqldump`

所以你本机不需要额外安装 MySQL Client。

如果你本机已经有可用的 MySQL 镜像，建议直接在配置文件中指定：

```bash
MYSQL_CLIENT_IMAGE=mysql:8
```

你可以先查看本机已有镜像：

```bash
docker images | grep mysql
```

然后把已有镜像名写到 `scripts/mysql/mysql-sync.env` 中。

## 连接说明

如果你的本地数据库也是通过 Docker 运行，并且已经映射到宿主机端口，例如：

```bash
3306:3306
```

那么在脚本配置里通常写：

```bash
LOCAL_DB_HOST=host.docker.internal
LOCAL_DB_PORT=3306
```

原因是脚本内部也是通过 Docker 容器执行 `mysql` 和 `mysqldump`，容器里访问宿主机端口时，不能写 `127.0.0.1`，而应该写 `host.docker.internal`。

## 脚本说明

### 1. 备份远程数据库

```bash
bash scripts/mysql/backup-remote-db.sh
```

默认会把备份文件生成到：

```bash
scripts/mysql/backups/
```

如果你想自定义备份目录，可以在执行时指定：

```bash
MYSQL_BACKUP_DIR=/自定义目录 bash scripts/mysql/backup-remote-db.sh
```

### 2. 删除远程数据库全部表

```bash
bash scripts/mysql/reset-remote-db.sh
```

执行前会要求你输入确认文本，例如：

```bash
DELETE yunyu
```

### 3. 从备份文件恢复远程数据库

```bash
bash scripts/mysql/restore-remote-db.sh scripts/mysql/backups/你的备份文件.sql
```

执行前会要求你输入确认文本，例如：

```bash
RESTORE yunyu
```

### 4. 把本地数据库全量同步到远程

```bash
bash scripts/mysql/sync-local-to-remote.sh
```

执行前会要求你输入确认文本，例如：

```bash
SYNC yunyu TO yunyu
```

脚本执行过程：

1. 先从本地库导出完整 SQL
2. 再清空远程目标库全部表
3. 最后把本地导出的表结构和数据导入远程库

### 5. 使用总入口脚本执行完整同步流程

```bash
bash scripts/mysql/run-sync-flow.sh
```

执行过程会输出完整日志，包括：

- 当前本地库和远程库信息
- 远程数据库备份进度
- 本地到远程的同步进度
- 同步完成后的恢复命令提示

执行前会要求你输入确认文本：

```bash
RUN SYNC FLOW
```

## 注意事项

- 这是覆盖式同步，不是增量同步
- 远程数据库中的原有表和数据会被清空
- 建议执行覆盖同步前先运行 `backup-remote-db.sh`
- 建议先对远程数据库做备份
- 推荐优先用于演示环境、测试环境和初始化环境
- 脚本通过 Docker 镜像执行 MySQL 客户端命令，可通过 `MYSQL_CLIENT_IMAGE` 指定你本机已有的 MySQL 镜像
