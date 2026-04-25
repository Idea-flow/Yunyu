#!/usr/bin/env bash

# 功能：执行基于 schema-diff 生成的差异 SQL，并在执行前自动备份远程数据库。
# 作用：将“差异预览 -> 人工确认 -> 线上执行”流程标准化，降低 SQL 升级风险。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEFAULT_CONFIG_FILE="${PROJECT_ROOT}/scripts/mysql/mysql-sync.env"
SCHEMA_DIFF_SCRIPT="${SCRIPT_DIR}/schema-diff.sh"
DB_BACKUP_SCRIPT="${PROJECT_ROOT}/scripts/mysql/backup-remote-db.sh"
OUTPUT_DIR="${SCRIPT_DIR}/output"
TIMESTAMP="$(date +"%Y%m%d-%H%M%S")"
DEFAULT_PLAN_FILE="${OUTPUT_DIR}/schema-diff-${TIMESTAMP}.sql"

CONFIG_FILE="${MYSQL_SYNC_CONFIG:-${DEFAULT_CONFIG_FILE}}"
PLAN_FILE=""
SCHEMA_FILE=""
AUTO_YES="false"
SKIP_BACKUP="false"

# 功能：输出脚本帮助信息。
# 作用：说明参数、执行顺序和安全边界。
usage() {
  cat <<'EOF'
用法：
  bash scripts/deploy/schema-apply.sh [--config 配置文件路径] [--schema 目标schema文件路径] [--plan 差异SQL路径] [--yes] [--skip-backup]

说明：
  1) 默认先执行数据库备份，再执行差异 SQL。
  2) 若未指定 --plan，会先调用 schema-diff.sh 生成差异 SQL。
  3) 仅执行安全边界内差异（新增表/新增字段/新增索引），不会执行删除或改类型操作。
EOF
}

# 功能：输出日志，统一日志前缀。
# 作用：发布窗口可快速定位执行阶段。
log() {
  echo "[schema-apply] $*"
}

# 功能：输出错误并终止执行。
# 作用：在关键步骤异常时立刻中断，避免产生半执行状态。
fail() {
  echo "[schema-apply][ERROR] $*" >&2
  exit 1
}

# 功能：执行 MySQL 查询并返回原始结果。
# 作用：统一复用 docker mysql client，不依赖宿主机 mysql 客户端。
mysql_query() {
  local sql="$1"
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    "${MYSQL_CLIENT_IMAGE}" \
    mysql \
    --batch \
    --skip-column-names \
    --raw \
    --host="${REMOTE_DB_HOST}" \
    --port="${REMOTE_DB_PORT}" \
    --user="${REMOTE_DB_USER}" \
    --password="${REMOTE_DB_PASSWORD}" \
    --database="${REMOTE_DB_NAME}" \
    -e "${sql}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --config)
      CONFIG_FILE="$2"
      shift 2
      ;;
    --schema)
      SCHEMA_FILE="$2"
      shift 2
      ;;
    --plan)
      PLAN_FILE="$2"
      shift 2
      ;;
    --yes)
      AUTO_YES="true"
      shift
      ;;
    --skip-backup)
      SKIP_BACKUP="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      fail "未知参数：$1"
      ;;
  esac
done

[[ -f "${CONFIG_FILE}" ]] || fail "未找到配置文件：${CONFIG_FILE}"
[[ -f "${SCHEMA_DIFF_SCRIPT}" ]] || fail "未找到差异脚本：${SCHEMA_DIFF_SCRIPT}"
[[ -f "${DB_BACKUP_SCRIPT}" ]] || fail "未找到备份脚本：${DB_BACKUP_SCRIPT}"

# shellcheck disable=SC1090
source "${CONFIG_FILE}"

: "${REMOTE_DB_HOST:?REMOTE_DB_HOST 未配置}"
: "${REMOTE_DB_PORT:?REMOTE_DB_PORT 未配置}"
: "${REMOTE_DB_NAME:?REMOTE_DB_NAME 未配置}"
: "${REMOTE_DB_USER:?REMOTE_DB_USER 未配置}"
: "${REMOTE_DB_PASSWORD:?REMOTE_DB_PASSWORD 未配置}"

MYSQL_CLIENT_IMAGE="${MYSQL_CLIENT_IMAGE:-mysql:8.4}"

mkdir -p "${OUTPUT_DIR}"

if [[ -z "${PLAN_FILE}" ]]; then
  PLAN_FILE="${DEFAULT_PLAN_FILE}"
  log "未指定 --plan，开始生成差异 SQL。"
  if [[ -n "${SCHEMA_FILE}" ]]; then
    MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${SCHEMA_DIFF_SCRIPT}" --config "${CONFIG_FILE}" --schema "${SCHEMA_FILE}" --output "${PLAN_FILE}"
  else
    MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${SCHEMA_DIFF_SCRIPT}" --config "${CONFIG_FILE}" --output "${PLAN_FILE}"
  fi
fi

[[ -f "${PLAN_FILE}" ]] || fail "差异 SQL 文件不存在：${PLAN_FILE}"

statement_count="$(rg -c '^(CREATE TABLE IF NOT EXISTS|ALTER TABLE)' "${PLAN_FILE}" || true)"
statement_count="${statement_count:-0}"

if [[ "${statement_count}" == "0" ]]; then
  log "未检测到可执行差异，跳过应用。"
  exit 0
fi

log "目标数据库：${REMOTE_DB_HOST}:${REMOTE_DB_PORT}/${REMOTE_DB_NAME}"
log "即将执行差异 SQL：${PLAN_FILE}"
log "可执行语句数：${statement_count}"

if [[ "${AUTO_YES}" != "true" ]]; then
  read -r -p "请输入 APPLY ${REMOTE_DB_NAME} 确认执行: " confirm_text
  if [[ "${confirm_text}" != "APPLY ${REMOTE_DB_NAME}" ]]; then
    fail "确认文本不匹配，已取消执行。"
  fi
fi

if [[ "${SKIP_BACKUP}" != "true" ]]; then
  log "开始执行数据库备份。"
  MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${DB_BACKUP_SCRIPT}"
fi

log "开始执行差异 SQL。"
docker run --rm -i \
  --add-host=host.docker.internal:host-gateway \
  "${MYSQL_CLIENT_IMAGE}" \
  mysql \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --database="${REMOTE_DB_NAME}" < "${PLAN_FILE}"

# 功能：写入 schema 升级历史表。
# 作用：保留每次自动升级记录，便于后续审计与问题回溯。
mysql_query "CREATE TABLE IF NOT EXISTS schema_upgrade_history (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  plan_file VARCHAR(255) NOT NULL COMMENT '执行的差异SQL文件路径',
  statement_count INT NOT NULL DEFAULT 0 COMMENT '执行语句数量',
  executed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
  PRIMARY KEY (id),
  KEY idx_schema_upgrade_history_executed_at (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Schema 自动升级执行历史';"

mysql_query "INSERT INTO schema_upgrade_history (plan_file, statement_count) VALUES ('${PLAN_FILE}', ${statement_count});"

log "差异 SQL 执行完成。"
log "已记录执行历史：schema_upgrade_history"
