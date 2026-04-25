# 后端版本升级（SQL+代码）线上最便捷升级方案

## 文档目标

本文档用于约束 `Yunyu` 在“后端代码 + SQL 同时变更”时的线上升级流程，目标是：

1. 升级过程可重复执行。
2. 停机时间最短。
3. 出问题可快速回滚。

## 一、适用范围

适用于当前仓库的两种后端部署方式：

1. 标准镜像：`docker-compose.yml` 中的 `yunyu-server`
2. Native 镜像：`docker/native-image/docker-compose.yml` 中的 `yunyu-server-native`

数据库默认是同一个 MySQL 服务：`yunyu-mysql`。

## 二、一句话策略（最便捷）

最便捷升级策略：`先备份数据库 -> 执行向后兼容 SQL -> 拉取新镜像并只重建后端容器 -> 健康检查 -> 观察日志`。

说明：

1. 不执行 `docker compose down -v`，避免误伤数据库。
2. SQL 优先做“向后兼容变更”（新增列/新增表/新增索引），避免代码切换瞬间读写失败。
3. 镜像必须保留上一版 Tag，确保可快速回滚代码版本。

## 三、发布前清单（必须）

上线前准备以下内容：

1. 本次升级 SQL：建议放到 `docs/技术/sql/upgrade/`（按版本号命名）。
2. SQL 回滚脚本（可逆变更必须准备）。
3. 本次发布镜像 Tag（例如 `v2026.04.25-01`）和上一版稳定 Tag。
4. 升级窗口与负责人（执行人/验证人）。

推荐 SQL 命名：

```text
V20260425_001__add_post_status.sql
R20260425_001__rollback_add_post_status.sql
```

## 四、标准升级流程（生产可执行）

### 步骤 1：数据库备份

优先复用仓库已有脚本：

```bash
bash scripts/mysql/backup-remote-db.sh
```

说明：

1. 需先准备 `scripts/mysql/mysql-sync.env`。
2. 备份成功后，记录 SQL 文件路径，作为回滚输入。

### 步骤 2：执行 SQL 升级

推荐在服务器执行（以标准 compose 为例）：

```bash
docker compose exec -T yunyu-mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' < docs/技术/sql/upgrade/V20260425_001__add_post_status.sql
```

如果是 Native compose：

```bash
docker compose -f docker/native-image/docker-compose.yml exec -T yunyu-mysql sh -c 'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' < docs/技术/sql/upgrade/V20260425_001__add_post_status.sql
```

### 步骤 3：升级后端容器（只动后端，不动 MySQL）

标准镜像：

```bash
docker compose pull yunyu-server
docker compose up -d yunyu-server
```

Native 镜像：

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

### 步骤 4：上线验证（5 分钟内完成）

建议按顺序执行：

1. 健康检查：`/actuator/health`
2. 核心接口抽样（登录、内容列表、详情、后台关键写接口）
3. 错误日志观察（5~10 分钟）

示例：

```bash
curl http://127.0.0.1:20000/actuator/health
docker compose logs -f --tail=200 yunyu-server
```

Native 示例：

```bash
curl http://127.0.0.1:20001/actuator/health
docker compose -f docker/native-image/docker-compose.yml logs -f --tail=200 yunyu-server-native
```

## 五、回滚策略（必须提前演练）

### 1. 代码回滚

原则：优先回滚镜像 Tag，通常 1~3 分钟可恢复。

操作方式：

1. 将 `.env` 中后端镜像变量改回上一版 Tag。
2. 执行 `docker compose up -d <后端服务名>` 重建容器。

### 2. SQL 回滚

原则：

1. 仅当本次 SQL 与旧代码不兼容时执行。
2. 优先使用预先准备好的回滚 SQL。
3. 若无法安全回滚，使用备份恢复：

```bash
bash scripts/mysql/restore-remote-db.sh <备份文件路径>
```

## 六、SQL 变更约束（降低升级风险）

生产升级建议遵循：

1. 避免“同次发布立即删列/改列类型”。
2. 默认采用“扩展优先、收缩滞后”两阶段模式：
   阶段 A：新增字段并让新旧代码都可运行。
   阶段 B：观察稳定后，再删除旧字段或旧逻辑。
3. 关键 DDL 尽量写成可重复执行（例如 `IF NOT EXISTS`）。
4. 对大表加索引优先评估执行时长，必要时单独窗口执行。

## 七、已落地的自动升级脚本

当前仓库已新增以下脚本：

1. `scripts/deploy/schema-diff.sh`
   作用：以 `docs/技术/sql/init.sql` 为目标结构，自动生成“新增表/新增字段/新增索引”的差异 SQL。
2. `scripts/deploy/schema-apply.sh`
   作用：执行“生成差异 -> 备份远程库 -> 应用差异 SQL -> 记录执行历史”的自动流程。

脚本说明文档：

1. `scripts/deploy/README.md`

## 八、推荐执行顺序（可直接照抄）

### 1. 先预览 SQL 差异（人工确认）

```bash
bash scripts/deploy/schema-diff.sh
```

### 2. 执行数据库结构升级（自动先备份）

```bash
bash scripts/deploy/schema-apply.sh
```

### 3. 升级 Native 后端镜像

```bash
docker compose -f docker/native-image/docker-compose.yml pull yunyu-server-native
docker compose -f docker/native-image/docker-compose.yml up -d yunyu-server-native
```

### 4. 健康检查与日志观察

```bash
curl http://127.0.0.1:20001/actuator/health
docker compose -f docker/native-image/docker-compose.yml logs -f --tail=200 yunyu-server-native
```
