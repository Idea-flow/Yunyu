#!/usr/bin/env bash

# 功能：备份远程 MySQL 数据库的完整表结构和数据。
# 作用：在执行覆盖式同步前，先生成一份远程数据库 SQL 备份文件，便于出现问题时回滚。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"
BACKUP_DIR="${MYSQL_BACKUP_DIR:-${SCRIPT_DIR}/backups}"
TIMESTAMP="$(date +"%Y%m%d-%H%M%S")"

if [[ ! -f "${CONFIG_FILE}" ]]; then
  echo "未找到配置文件：${CONFIG_FILE}"
  echo "请先复制 ${SCRIPT_DIR}/mysql-sync.env.example 为 ${SCRIPT_DIR}/mysql-sync.env 并填写连接信息。"
  exit 1
fi

# shellcheck disable=SC1090
source "${CONFIG_FILE}"

: "${REMOTE_DB_HOST:?REMOTE_DB_HOST 未配置}"
: "${REMOTE_DB_PORT:?REMOTE_DB_PORT 未配置}"
: "${REMOTE_DB_NAME:?REMOTE_DB_NAME 未配置}"
: "${REMOTE_DB_USER:?REMOTE_DB_USER 未配置}"
: "${REMOTE_DB_PASSWORD:?REMOTE_DB_PASSWORD 未配置}"

MYSQL_CLIENT_IMAGE="${MYSQL_CLIENT_IMAGE:-mysql:8.4}"
BACKUP_FILE="${BACKUP_DIR}/${REMOTE_DB_NAME}-${TIMESTAMP}.sql"

mkdir -p "${BACKUP_DIR}"

file_size_bytes() {
  if [[ -f "$1" ]]; then
    wc -c < "$1" | tr -d ' '
  else
    echo "0"
  fi
}

echo "开始备份远程数据库："
echo "  Host: ${REMOTE_DB_HOST}"
echo "  Port: ${REMOTE_DB_PORT}"
echo "  Database: ${REMOTE_DB_NAME}"
echo "  User: ${REMOTE_DB_USER}"
echo "  Backup File: ${BACKUP_FILE}"
echo "  Client Image: ${MYSQL_CLIENT_IMAGE}"
echo
echo "正在执行远程数据库导出。"
echo "说明：mysqldump 在备份过程中通常不会持续输出日志，脚本会每 5 秒打印一次当前备份文件大小。"

docker run --rm \
  --add-host=host.docker.internal:host-gateway \
  "${MYSQL_CLIENT_IMAGE}" \
  mysqldump \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --set-gtid-purged=OFF \
  "${REMOTE_DB_NAME}" > "${BACKUP_FILE}" &

DUMP_PID=$!

while kill -0 "${DUMP_PID}" >/dev/null 2>&1; do
  sleep 5
  CURRENT_SIZE_BYTES="$(file_size_bytes "${BACKUP_FILE}")"
  echo "备份进行中，当前文件大小：${CURRENT_SIZE_BYTES} bytes"
done

wait "${DUMP_PID}"

FINAL_SIZE_BYTES="$(file_size_bytes "${BACKUP_FILE}")"
echo "远程数据库备份完成：${BACKUP_FILE}"
echo "备份文件大小：${FINAL_SIZE_BYTES} bytes"
