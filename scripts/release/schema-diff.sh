#!/usr/bin/env bash

# 功能：基于 init schema SQL 与线上数据库结构生成“只增不删”的差异 SQL。
# 作用：先拉取远端数据库完整 schema 到本地快照目录，再与 docs/技术/sql/init.sql 对比并输出增量 SQL。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/schema-diff.env"
LEGACY_CONFIG_FILE="${PROJECT_ROOT}/scripts/db/mysql-sync.env"
DEFAULT_SCHEMA_FILE="${PROJECT_ROOT}/docs/技术/sql/init.sql"
OUTPUT_DIR="${SCRIPT_DIR}/output"
SNAPSHOT_ROOT_DIR="${OUTPUT_DIR}/schema-snapshots"
TIMESTAMP="$(date +"%Y%m%d-%H%M%S")"
DEFAULT_OUTPUT_FILE="${OUTPUT_DIR}/schema-diff-${TIMESTAMP}.sql"

CONFIG_FILE="${SCHEMA_DIFF_CONFIG:-${DEFAULT_CONFIG_FILE}}"
SCHEMA_FILE="${SCHEMA_FILE:-${DEFAULT_SCHEMA_FILE}}"
OUTPUT_FILE="${OUTPUT_FILE:-${DEFAULT_OUTPUT_FILE}}"
SNAPSHOT_DIR="${SNAPSHOT_ROOT_DIR}/${TIMESTAMP}"

# 功能：输出脚本帮助信息。
# 作用：说明参数、配置文件和安全边界。
usage() {
  cat <<'EOF'
用法：
  bash scripts/release/schema-diff.sh [--config 配置文件路径] [--schema 目标schema文件路径] [--output 输出SQL路径]

说明：
  1) 优先读取独立配置文件：scripts/release/schema-diff.env
  2) 若未找到独立配置文件，会自动回退到 scripts/db/mysql-sync.env
  3) 可选配置 MYSQL_CLIENT_CONTAINER 复用已启动的 MySQL 容器（docker exec 模式）
  4) 可选配置 MYSQL_EXEC_DB_HOST 指定 exec 模式使用的数据库主机（常用 127.0.0.1）
  5) 仅生成安全边界内差异：
     - CREATE TABLE IF NOT EXISTS
     - ALTER TABLE ADD COLUMN
     - ALTER TABLE ADD [UNIQUE] KEY
  6) 不会生成 DROP / MODIFY / CHANGE / RENAME 等破坏性语句。
EOF
}

# 功能：输出日志，统一日志前缀。
# 作用：便于在发布窗口快速识别执行进度。
log() {
  echo "[schema-diff] $*"
}

# 功能：输出错误并终止执行。
# 作用：在关键依赖缺失或执行失败时快速失败。
fail() {
  echo "[schema-diff][ERROR] $*" >&2
  exit 1
}

# 功能：输出阶段进度日志。
# 作用：让脚本在每个关键阶段都可见，便于观察进度和卡点。
log_step() {
  local current="$1"
  local total="$2"
  local message="$3"
  log "步骤 ${current}/${total}：${message}"
}

# 功能：判断是否配置且可用的 MySQL 客户端容器。
# 作用：在 schema 拉取阶段优先复用已启动容器，避免每次临时启动新容器。
can_use_mysql_client_container() {
  if [[ -z "${MYSQL_CLIENT_CONTAINER}" ]]; then
    return 1
  fi
  docker ps --format '{{.Names}}' | rg -xq "${MYSQL_CLIENT_CONTAINER}"
}

# 功能：导出远端库 schema（仅结构）。
# 作用：统一封装两种执行模式：复用容器（docker exec）或临时容器（docker run）。
dump_remote_schema() {
  if can_use_mysql_client_container; then
    local exec_db_host="${MYSQL_EXEC_DB_HOST:-${REMOTE_DB_HOST}}"
    if [[ -z "${MYSQL_EXEC_DB_HOST:-}" && "${REMOTE_DB_HOST}" == "host.docker.internal" ]]; then
      exec_db_host="127.0.0.1"
      log "检测到 exec 模式 + host.docker.internal，自动切换数据库主机为 127.0.0.1。"
      log "如需自定义，请在配置中设置 MYSQL_EXEC_DB_HOST。"
    fi

    log "使用已启动容器导出 schema：${MYSQL_CLIENT_CONTAINER}（dbHost=${exec_db_host}）"
    docker exec \
      -e MYSQL_PWD="${REMOTE_DB_PASSWORD}" \
      "${MYSQL_CLIENT_CONTAINER}" \
      mysqldump \
      --no-data \
      --skip-comments \
      --skip-add-drop-table \
      --set-gtid-purged=OFF \
      --host="${exec_db_host}" \
      --port="${REMOTE_DB_PORT}" \
      --user="${REMOTE_DB_USER}" \
      "${REMOTE_DB_NAME}" > "${REMOTE_SCHEMA_FILE}"
    return 0
  fi

  if [[ -n "${MYSQL_CLIENT_CONTAINER}" ]]; then
    log "指定的容器未运行，回退临时容器模式：${MYSQL_CLIENT_CONTAINER}"
  fi

  log "使用临时容器导出 schema：${MYSQL_CLIENT_IMAGE}"
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -e MYSQL_PWD="${REMOTE_DB_PASSWORD}" \
    "${MYSQL_CLIENT_IMAGE}" \
    mysqldump \
    --no-data \
    --skip-comments \
    --skip-add-drop-table \
    --set-gtid-purged=OFF \
    --host="${REMOTE_DB_HOST}" \
    --port="${REMOTE_DB_PORT}" \
    --user="${REMOTE_DB_USER}" \
    "${REMOTE_DB_NAME}" > "${REMOTE_SCHEMA_FILE}"
}

# 功能：提取 SQL 文件中的 CREATE TABLE 语句到按表拆分目录。
# 作用：让后续字段、索引的差异计算更简单可控。
extract_table_blocks() {
  local source_file="$1"
  local output_dir="$2"

  awk -v outdir="${output_dir}" '
    BEGIN { in_table = 0; table = ""; file = "" }
    /^CREATE TABLE / {
      in_table = 1
      line = $0
      sub(/^CREATE TABLE[[:space:]]+/, "", line)
      sub(/^IF NOT EXISTS[[:space:]]+/, "", line)
      sub(/^`/, "", line)
      sub(/`[[:space:]]*\(.*/, "", line)
      table = line
      file = outdir "/" table ".sql"
      print $0 > file
      next
    }
    in_table == 1 {
      print $0 >> file
      if ($0 ~ /^\)[[:space:]]*ENGINE=/) {
        in_table = 0
        close(file)
        file = ""
      }
    }
  ' "${source_file}"
}

# 功能：从按表 SQL 文件中提取列定义。
# 作用：用于判断线上缺失字段并生成 ADD COLUMN。
extract_columns() {
  local table_dir="$1"
  local output_file="$2"
  : > "${output_file}"

  for table_file in "${table_dir}"/*.sql; do
    local table_name
    table_name="$(basename "${table_file}" .sql)"
    awk -v table="${table_name}" '
      /^[[:space:]]*`[^`]+`[[:space:]]+/ {
        line = $0
        gsub(/^[[:space:]]+/, "", line)
        col = line
        sub(/`/, "", col)
        sub(/`.*/, "", col)
        def = line
        sub(/^`[^`]+`[[:space:]]+/, "", def)
        sub(/,[[:space:]]*$/, "", def)
        printf "%s\t%s\t%s\n", table, col, def
      }
    ' "${table_file}" >> "${output_file}"
  done
}

# 功能：从按表 SQL 文件中提取索引定义。
# 作用：用于判断线上缺失索引并生成 ADD KEY。
extract_indexes() {
  local table_dir="$1"
  local output_file="$2"
  : > "${output_file}"

  for table_file in "${table_dir}"/*.sql; do
    local table_name
    table_name="$(basename "${table_file}" .sql)"
    awk -v table="${table_name}" '
      /^[[:space:]]*(UNIQUE KEY|KEY)[[:space:]]+`[^`]+`[[:space:]]*\(.*/ {
        line = $0
        gsub(/^[[:space:]]+/, "", line)
        sub(/,[[:space:]]*$/, "", line)
        idx = line
        sub(/^(UNIQUE KEY|KEY)[[:space:]]+`/, "", idx)
        sub(/`.*/, "", idx)
        printf "%s\t%s\t%s\n", table, idx, line
      }
    ' "${table_file}" >> "${output_file}"
  done
}

# 功能：从 init.sql 提取 site_config 初始化语句并转换为“仅补齐缺失 key”的安全写法。
# 作用：确保新功能新增 config_key 时可自动补齐，同时避免覆盖线上已存在配置值。
append_safe_site_config_seed_sql() {
  local init_sql_file="$1"
  local output_sql_file="$2"
  local raw_block
  local values_block

  raw_block="$(awk '
    BEGIN { capture = 0 }
    /^INSERT INTO `site_config` \(`config_key`, `config_name`, `config_json`, `remark`\)/ { capture = 1 }
    capture == 1 { print }
    capture == 1 && /;[[:space:]]*$/ { exit }
  ' "${init_sql_file}")"

  if [[ -z "${raw_block}" ]]; then
    log "未在 init.sql 中找到 site_config 初始化语句，跳过配置补齐 SQL 生成。"
    return
  fi

  values_block="$(printf '%s\n' "${raw_block}" | sed '/^ON DUPLICATE KEY UPDATE/,$d')"
  values_block="$(printf '%s\n' "${values_block}" | sed '/^[[:space:]]*$/d')"

  {
    echo "-- 补齐 site_config 缺失配置键（来源：init.sql，安全模式不覆盖已有值）"
    printf '%s\n' "${values_block}"
    echo "ON DUPLICATE KEY UPDATE"
    echo "  \`config_key\` = \`config_key\`;"
    echo
  } >> "${output_sql_file}"
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
    --output)
      OUTPUT_FILE="$2"
      shift 2
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

if [[ ! -f "${CONFIG_FILE}" ]]; then
  if [[ "${CONFIG_FILE}" == "${DEFAULT_CONFIG_FILE}" && -f "${LEGACY_CONFIG_FILE}" ]]; then
    log "未找到独立配置文件，回退使用：${LEGACY_CONFIG_FILE}"
    CONFIG_FILE="${LEGACY_CONFIG_FILE}"
  else
    fail "未找到配置文件：${CONFIG_FILE}"
  fi
fi

[[ -f "${SCHEMA_FILE}" ]] || fail "未找到 schema 文件：${SCHEMA_FILE}"

# shellcheck disable=SC1090
source "${CONFIG_FILE}"

: "${REMOTE_DB_HOST:?REMOTE_DB_HOST 未配置}"
: "${REMOTE_DB_PORT:?REMOTE_DB_PORT 未配置}"
: "${REMOTE_DB_NAME:?REMOTE_DB_NAME 未配置}"
: "${REMOTE_DB_USER:?REMOTE_DB_USER 未配置}"
: "${REMOTE_DB_PASSWORD:?REMOTE_DB_PASSWORD 未配置}"

MYSQL_CLIENT_IMAGE="${MYSQL_CLIENT_IMAGE:-mysql:8.4}"
MYSQL_CLIENT_CONTAINER="${MYSQL_CLIENT_CONTAINER:-}"
MYSQL_EXEC_DB_HOST="${MYSQL_EXEC_DB_HOST:-}"

mkdir -p "${OUTPUT_DIR}"
mkdir -p "${SNAPSHOT_DIR}"
mkdir -p "$(dirname "${OUTPUT_FILE}")"

INIT_TABLE_SQL_DIR="${SNAPSHOT_DIR}/init-tables"
REMOTE_TABLE_SQL_DIR="${SNAPSHOT_DIR}/remote-tables"
INIT_COLUMN_DEF_FILE="${SNAPSHOT_DIR}/init-columns.tsv"
INIT_INDEX_DEF_FILE="${SNAPSHOT_DIR}/init-indexes.tsv"
REMOTE_COLUMN_DEF_FILE="${SNAPSHOT_DIR}/remote-columns.tsv"
REMOTE_INDEX_DEF_FILE="${SNAPSHOT_DIR}/remote-indexes.tsv"
REMOTE_COLUMN_KEYS_FILE="${SNAPSHOT_DIR}/remote-column-keys.tsv"
REMOTE_INDEX_KEYS_FILE="${SNAPSHOT_DIR}/remote-index-keys.tsv"
REMOTE_SCHEMA_FILE="${SNAPSHOT_DIR}/remote-schema.sql"

mkdir -p "${INIT_TABLE_SQL_DIR}" "${REMOTE_TABLE_SQL_DIR}"

log_step 1 6 "读取 init.sql 并提取 init 表结构。"
extract_table_blocks "${SCHEMA_FILE}" "${INIT_TABLE_SQL_DIR}"
INIT_TABLE_COUNT="$(ls -1 "${INIT_TABLE_SQL_DIR}" | wc -l | tr -d ' ')"
log "init schema 表数量：${INIT_TABLE_COUNT}"

log_step 2 6 "从远端数据库拉取完整 schema 快照到本地目录。"
dump_remote_schema
log "远端 schema 快照文件：${REMOTE_SCHEMA_FILE}"

log_step 3 6 "提取远端表结构定义。"
extract_table_blocks "${REMOTE_SCHEMA_FILE}" "${REMOTE_TABLE_SQL_DIR}"
REMOTE_TABLE_COUNT="$(ls -1 "${REMOTE_TABLE_SQL_DIR}" | wc -l | tr -d ' ')"
log "远端 schema 表数量：${REMOTE_TABLE_COUNT}"

log_step 4 6 "提取 init 与远端的字段、索引定义。"
extract_columns "${INIT_TABLE_SQL_DIR}" "${INIT_COLUMN_DEF_FILE}"
extract_indexes "${INIT_TABLE_SQL_DIR}" "${INIT_INDEX_DEF_FILE}"
extract_columns "${REMOTE_TABLE_SQL_DIR}" "${REMOTE_COLUMN_DEF_FILE}"
extract_indexes "${REMOTE_TABLE_SQL_DIR}" "${REMOTE_INDEX_DEF_FILE}"

awk -F'\t' '{print $1 "\t" $2}' "${REMOTE_COLUMN_DEF_FILE}" | sort -u > "${REMOTE_COLUMN_KEYS_FILE}"
awk -F'\t' '{print $1 "\t" $2}' "${REMOTE_INDEX_DEF_FILE}" | sort -u > "${REMOTE_INDEX_KEYS_FILE}"

log_step 5 6 "计算差异并生成增量 SQL。"
{
  echo "-- 自动生成：线上 schema 差异 SQL（安全边界：仅新增）"
  echo "-- 生成时间：$(date '+%Y-%m-%d %H:%M:%S')"
  echo "-- init schema：${SCHEMA_FILE}"
  echo "-- 目标库：${REMOTE_DB_HOST}:${REMOTE_DB_PORT}/${REMOTE_DB_NAME}"
  echo "-- 本地快照目录：${SNAPSHOT_DIR}"
  echo
} > "${OUTPUT_FILE}"

create_table_count=0
add_column_count=0
add_index_count=0

for table_file in "${INIT_TABLE_SQL_DIR}"/*.sql; do
  table_name="$(basename "${table_file}" .sql)"
  if [[ ! -f "${REMOTE_TABLE_SQL_DIR}/${table_name}.sql" ]]; then
    {
      echo "-- 缺失表：${table_name}"
      cat "${table_file}"
      echo
    } >> "${OUTPUT_FILE}"
    create_table_count=$((create_table_count + 1))
  fi
done

while IFS=$'\t' read -r table_name column_name column_def; do
  [[ -n "${table_name}" ]] || continue
  [[ -f "${REMOTE_TABLE_SQL_DIR}/${table_name}.sql" ]] || continue
  if ! printf '%s\t%s\n' "${table_name}" "${column_name}" | rg -Fqx -f /dev/stdin "${REMOTE_COLUMN_KEYS_FILE}"; then
    {
      echo "-- 缺失字段：${table_name}.${column_name}"
      echo "ALTER TABLE \`${table_name}\` ADD COLUMN \`${column_name}\` ${column_def};"
      echo
    } >> "${OUTPUT_FILE}"
    add_column_count=$((add_column_count + 1))
  fi
done < "${INIT_COLUMN_DEF_FILE}"

while IFS=$'\t' read -r table_name index_name index_clause; do
  [[ -n "${table_name}" ]] || continue
  [[ -f "${REMOTE_TABLE_SQL_DIR}/${table_name}.sql" ]] || continue
  if ! printf '%s\t%s\n' "${table_name}" "${index_name}" | rg -Fqx -f /dev/stdin "${REMOTE_INDEX_KEYS_FILE}"; then
    {
      echo "-- 缺失索引：${table_name}.${index_name}"
      echo "ALTER TABLE \`${table_name}\` ADD ${index_clause};"
      echo
    } >> "${OUTPUT_FILE}"
    add_index_count=$((add_index_count + 1))
  fi
done < "${INIT_INDEX_DEF_FILE}"

append_safe_site_config_seed_sql "${SCHEMA_FILE}" "${OUTPUT_FILE}"

{
  echo "-- 统计："
  echo "--   CREATE TABLE: ${create_table_count}"
  echo "--   ADD COLUMN  : ${add_column_count}"
  echo "--   ADD INDEX   : ${add_index_count}"
} >> "${OUTPUT_FILE}"

log_step 6 6 "输出结果。"
log "差异 SQL 已生成：${OUTPUT_FILE}"
log "快照目录：${SNAPSHOT_DIR}"
log "统计：CREATE TABLE=${create_table_count}, ADD COLUMN=${add_column_count}, ADD INDEX=${add_index_count}"
