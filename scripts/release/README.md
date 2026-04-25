# Release 升级脚本说明

本目录用于存放线上升级辅助脚本，当前提供：

1. `schema-diff.sh`：根据 `docs/技术/sql/init.sql` 生成“只增不删”的差异 SQL。
2. `schema-apply.sh`：执行“生成差异 -> 备份数据库 -> 应用差异”的安全升级流程。

## 一、前置约束

1. `docs/技术/sql/init.sql` 作为目标结构基线（最新 schema）。
2. demo 数据单独放在 `docs/技术/sql/demo`，本脚本不会处理 demo 数据。
3. `schema-diff.sh` 优先读取独立配置文件：`scripts/release/schema-diff.env`（模板：`scripts/release/schema-diff.env.example`）。
4. 若未提供独立配置文件，`schema-diff.sh` 会回退到 `scripts/db/mysql-sync.env`。
5. 可选配置 `MYSQL_CLIENT_CONTAINER`，优先复用本机已启动容器（`docker exec`），不再每次新起临时容器。
6. exec 模式可配置 `MYSQL_EXEC_DB_HOST`，当容器本身就是数据库容器时建议设为 `127.0.0.1`。

## 二、安全边界

脚本只会处理以下变更：

1. 新增表：`CREATE TABLE IF NOT EXISTS`
2. 新增字段：`ALTER TABLE ADD COLUMN`
3. 新增索引：`ALTER TABLE ADD KEY / ADD UNIQUE KEY`

不会处理以下变更：

1. 删除表/删字段
2. 字段改名、改类型、改默认值
3. 数据迁移、清洗、回填

## 三、常用命令

准备独立配置文件：

```bash
cp scripts/release/schema-diff.env.example scripts/release/schema-diff.env
```

仅生成差异 SQL：

```bash
bash scripts/release/schema-diff.sh
```

指定输出文件：

```bash
bash scripts/release/schema-diff.sh --output scripts/release/output/release-001.sql
```

执行升级（默认先备份）：

```bash
bash scripts/release/schema-apply.sh
```

使用已有差异文件执行：

```bash
bash scripts/release/schema-apply.sh --plan scripts/release/output/release-001.sql
```

## 四、推荐发布流程

1. 先跑 `schema-diff.sh` 并人工确认差异 SQL。
2. 发布窗口执行 `schema-apply.sh`，完成结构升级。
3. 执行后端镜像升级：
   `docker compose pull yunyu-server-native && docker compose up -d yunyu-server-native`

补充说明：

1. `schema-diff.sh` 会先把远端数据库完整 schema 拉取到 `scripts/release/output/schema-snapshots/<timestamp>/`。
2. 然后再把本地 `init.sql` 与远端快照做 diff，并输出增量 SQL。
3. 脚本会按“步骤 1/6 ~ 6/6”打印进度日志。
