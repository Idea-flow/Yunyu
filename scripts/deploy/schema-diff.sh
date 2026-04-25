#!/usr/bin/env bash

# 功能：基于目标 schema SQL 与线上数据库结构生成“只增不删”的差异 SQL。
# 作用：把 docs/技术/sql/init.sql 作为目标结构基线，自动生成新增表、新增字段、新增索引语句。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEFAULT_CONFIG_FILE="${PROJECT_ROOT}/scripts/mysql/mysql-sync.env"
DEFAULT_SCHEMA_FILE="${PROJECT_ROOT}/docs/技术/sql/init.sql"
OUTPUT_DIR="${SCRIPT_DIR}/output"
TIMESTAMP="$(date +"%Y%m%d-%H%M%S")"
DEFAULT_OUTPUT_FILE="${OUTPUT_DIR}/schema-diff-${TIMESTAMP}.sql"

CONFIG_FILE="${MYSQL_SYNC_CONFIG:-${DEFAULT_CONFIG_FILE}}"
SCHEMA_FILE="${SCHEMA_FILE:-${DEFAULT_SCHEMA_FILE}}"
OUTPUT_FILE="${OUTPUT_FILE:-${DEFAULT_OUTPUT_FILE}}"

# 功能：输出脚本帮助信息。
# 作用：说明参数和安全边界，避免误用。
usage() {
  cat <<'EOF'
用法：
  bash scripts/deploy/schema-diff.sh [--config 配置文件路径] [--schema 目标schema文件路径] [--output 输出SQL路径]

说明：
  1) 仅生成安全边界内差异：
     - CREATE TABLE IF NOT EXISTS
     - ALTER TABLE ADD COLUMN
     - ALTER TABLE ADD [UNIQUE] KEY
  2) 不会生成 DROP / MODIFY / CHANGE / RENAME 等破坏性语句。
EOF
}

# 功能：输出日志，统一日志前缀。
# 作用：提高执行可读性，方便在发布窗口排查。
log() {
  echo "[schema-diff] $*"
}

# 功能：中断执行并输出错误信息。
# 作用：在关键依赖缺失时快速失败，避免生成错误 SQL。
fail() {
  echo "[schema-diff][ERROR] $*" >&2
  exit 1
}

# 功能：执行 MySQL 查询并返回原始结果。
# 作用：统一复用 docker mysql client，避免依赖宿主机本地 mysql 二进制。
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

[[ -f "${CONFIG_FILE}" ]] || fail "未找到配置文件：${CONFIG_FILE}"
[[ -f "${SCHEMA_FILE}" ]] || fail "未找到 schema 文件：${SCHEMA_FILE}"

# shellcheck disable=SC1090
source "${CONFIG_FILE}"

: "${REMOTE_DB_HOST:?REMOTE_DB_HOST 未配置}"
: "${REMOTE_DB_PORT:?REMOTE_DB_PORT 未配置}"
: "${REMOTE_DB_NAME:?REMOTE_DB_NAME 未配置}"
: "${REMOTE_DB_USER:?REMOTE_DB_USER 未配置}"
: "${REMOTE_DB_PASSWORD:?REMOTE_DB_PASSWORD 未配置}"

MYSQL_CLIENT_IMAGE="${MYSQL_CLIENT_IMAGE:-mysql:8.4}"

mkdir -p "${OUTPUT_DIR}"
mkdir -p "$(dirname "${OUTPUT_FILE}")"

TMP_DIR="$(mktemp -d)"
TABLE_SQL_DIR="${TMP_DIR}/tables"
COLUMN_DEF_FILE="${TMP_DIR}/column-def.tsv"
INDEX_DEF_FILE="${TMP_DIR}/index-def.tsv"

# 功能：清理临时目录。
# 作用：避免多次执行残留临时文件影响结果。
cleanup() {
  rm -rf "${TMP_DIR}"
}
trap cleanup EXIT

mkdir -p "${TABLE_SQL_DIR}"
: > "${COLUMN_DEF_FILE}"
: > "${INDEX_DEF_FILE}"

log "读取目标 schema：${SCHEMA_FILE}"

# 功能：从 schema 文件提取每个 CREATE TABLE 语句到独立文件。
# 作用：后续按表逐个判断“建表/补字段/补索引”差异。
awk -v outdir="${TABLE_SQL_DIR}" '
  BEGIN { in_table = 0; table = ""; file = "" }
  match($0, /^CREATE TABLE IF NOT EXISTS `([^`]+)` \(/, m) {
    in_table = 1
    table = m[1]
    file = outdir "/" table ".sql"
    print $0 > file
    next
  }
  in_table == 1 {
    print $0 >> file
    if ($0 ~ /^\) ENGINE=/) {
      in_table = 0
      close(file)
      file = ""
    }
  }
' "${SCHEMA_FILE}"

TABLE_FILES=("${TABLE_SQL_DIR}"/*.sql)
if [[ ! -e "${TABLE_FILES[0]}" ]]; then
  fail "未在 schema 中解析到 CREATE TABLE 语句。"
fi

for table_file in "${TABLE_SQL_DIR}"/*.sql; do
  table_name="$(basename "${table_file}" .sql)"

  # 功能：提取列定义（每行一列）。
  # 作用：后续用于判断线上缺失字段并生成 ADD COLUMN。
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
  ' "${table_file}" >> "${COLUMN_DEF_FILE}"

  # 功能：提取索引定义（UNIQUE KEY / KEY）。
  # 作用：后续用于判断线上缺失索引并生成 ADD KEY。
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
  ' "${table_file}" >> "${INDEX_DEF_FILE}"
done

{
  echo "-- 自动生成：线上 schema 差异 SQL（安全边界：仅新增）"
  echo "-- 生成时间：$(date '+%Y-%m-%d %H:%M:%S')"
  echo "-- 目标 schema：${SCHEMA_FILE}"
  echo "-- 目标库：${REMOTE_DB_HOST}:${REMOTE_DB_PORT}/${REMOTE_DB_NAME}"
  echo
} > "${OUTPUT_FILE}"

create_table_count=0
add_column_count=0
add_index_count=0

for table_file in "${TABLE_SQL_DIR}"/*.sql; do
  table_name="$(basename "${table_file}" .sql)"
  table_exists="$(mysql_query "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema='${REMOTE_DB_NAME}' AND table_name='${table_name}';")"
  table_exists="${table_exists:-0}"

  if [[ "${table_exists}" == "0" ]]; then
    {
      echo "-- 缺失表：${table_name}"
      cat "${table_file}"
      echo
    } >> "${OUTPUT_FILE}"
    create_table_count=$((create_table_count + 1))
  fi
done

if [[ -s "${COLUMN_DEF_FILE}" ]]; then
  while IFS=$'\t' read -r table_name column_name column_def; do
    [[ -n "${table_name}" ]] || continue

    table_exists="$(mysql_query "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema='${REMOTE_DB_NAME}' AND table_name='${table_name}';")"
    table_exists="${table_exists:-0}"
    if [[ "${table_exists}" == "0" ]]; then
      continue
    fi

    column_exists="$(mysql_query "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema='${REMOTE_DB_NAME}' AND table_name='${table_name}' AND column_name='${column_name}';")"
    column_exists="${column_exists:-0}"

    if [[ "${column_exists}" == "0" ]]; then
      {
        echo "-- 缺失字段：${table_name}.${column_name}"
        echo "ALTER TABLE \`${table_name}\` ADD COLUMN \`${column_name}\` ${column_def};"
        echo
      } >> "${OUTPUT_FILE}"
      add_column_count=$((add_column_count + 1))
    fi
  done < "${COLUMN_DEF_FILE}"
fi

if [[ -s "${INDEX_DEF_FILE}" ]]; then
  while IFS=$'\t' read -r table_name index_name index_clause; do
    [[ -n "${table_name}" ]] || continue

    table_exists="$(mysql_query "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema='${REMOTE_DB_NAME}' AND table_name='${table_name}';")"
    table_exists="${table_exists:-0}"
    if [[ "${table_exists}" == "0" ]]; then
      continue
    fi

    index_exists="$(mysql_query "SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema='${REMOTE_DB_NAME}' AND table_name='${table_name}' AND index_name='${index_name}';")"
    index_exists="${index_exists:-0}"

    if [[ "${index_exists}" == "0" ]]; then
      {
        echo "-- 缺失索引：${table_name}.${index_name}"
        echo "ALTER TABLE \`${table_name}\` ADD ${index_clause};"
        echo
      } >> "${OUTPUT_FILE}"
      add_index_count=$((add_index_count + 1))
    fi
  done < "${INDEX_DEF_FILE}"
fi

{
  echo "-- 统计："
  echo "--   CREATE TABLE: ${create_table_count}"
  echo "--   ADD COLUMN  : ${add_column_count}"
  echo "--   ADD INDEX   : ${add_index_count}"
} >> "${OUTPUT_FILE}"

log "差异 SQL 已生成：${OUTPUT_FILE}"
log "统计：CREATE TABLE=${create_table_count}, ADD COLUMN=${add_column_count}, ADD INDEX=${add_index_count}"
