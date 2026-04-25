# Deploy 升级脚本说明

本目录用于存放线上升级辅助脚本，当前提供：

1. `schema-diff.sh`：根据 `docs/技术/sql/001-init-schema.sql` 生成“只增不删”的差异 SQL。
2. `schema-apply.sh`：执行“生成差异 -> 备份数据库 -> 应用差异”的安全升级流程。

## 一、前置约束

1. `docs/技术/sql/001-init-schema.sql` 作为目标结构基线（最新 schema）。
2. demo 数据单独放在 `docs/技术/sql/demo`，本脚本不会处理 demo 数据。
3. 数据库连接信息默认来自 `scripts/mysql/mysql-sync.env`。

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

仅生成差异 SQL：

```bash
bash scripts/deploy/schema-diff.sh
```

指定输出文件：

```bash
bash scripts/deploy/schema-diff.sh --output scripts/deploy/output/release-001.sql
```

执行升级（默认先备份）：

```bash
bash scripts/deploy/schema-apply.sh
```

使用已有差异文件执行：

```bash
bash scripts/deploy/schema-apply.sh --plan scripts/deploy/output/release-001.sql
```

## 四、推荐发布流程

1. 先跑 `schema-diff.sh` 并人工确认差异 SQL。
2. 发布窗口执行 `schema-apply.sh`，完成结构升级。
3. 执行后端镜像升级：
   `docker compose pull yunyu-server-native && docker compose up -d yunyu-server-native`

