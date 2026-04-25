# SQL 升级方案（两种链路）

## 文档目标

本文档仅说明 `Yunyu` 的 SQL 升级方案，统一为两种可执行链路：

1. 增量升级链路（Diff + Apply）
2. 全量同步链路（本地库全量覆盖远程库）

## 一、方案 A：增量升级链路（推荐生产）

### 1. 适用场景

1. 线上已有业务数据，不能被覆盖。
2. 只希望补齐新表、新字段、新索引和缺失配置键。
3. 需要可重复执行、可审阅 SQL 差异。

### 2. 核心特点

1. 以 `docs/技术/sql/init.sql` 作为目标基线。
2. 先拉取远端库 schema 快照，再做差异对比。
3. 只生成安全边界内 SQL：`CREATE TABLE IF NOT EXISTS`、`ADD COLUMN`、`ADD KEY`、`site_config` 缺失 key 补齐。
4. 默认先备份数据库再执行差异 SQL。

### 3. 执行命令

准备配置文件（首次）：

```bash
cp scripts/release/schema-diff.env.example scripts/release/schema-diff.env
```

生成差异 SQL：

```bash
bash scripts/release/schema-diff.sh
```

执行差异 SQL（自动先备份）：

```bash
bash scripts/release/schema-apply.sh
```

如果要执行指定 diff 文件：

```bash
bash scripts/release/schema-apply.sh --plan /绝对路径/你的-diff.sql
```

## 二、方案 B：全量同步链路（覆盖远程库）

### 1. 适用场景

1. 演示环境、测试环境、初始化环境。
2. 明确要把远程库整体替换为本地库结构与数据。

### 2. 风险说明

1. 该方案会清空远程库后再导入本地库内容。
2. 远程现有数据会被覆盖，生产环境默认不建议使用。

### 3. 执行命令

准备配置文件（首次）：

```bash
cp scripts/db/mysql-sync.env.example scripts/db/mysql-sync.env
```

执行总流程（备份 -> 覆盖同步 -> 输出回滚提示）：

```bash
bash scripts/db/run-sync-flow.sh

bash scripts/db/run-sync-flow.sh --yes

MYSQL_AUTO_CONFIRM_ALL=true bash scripts/db/run-sync-flow.sh
```

## 三、回滚命令

两种链路都可使用数据库备份恢复：

```bash
bash scripts/db/restore-remote-db.sh /绝对路径/备份文件.sql
```

## 四、选择建议

1. 生产环境优先用方案 A（增量升级链路）。
2. 非生产覆盖式同步才用方案 B（全量同步链路）。
