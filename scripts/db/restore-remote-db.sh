#!/usr/bin/env bash

# 功能：将指定 SQL 备份文件恢复到远程 MySQL 数据库。
# 作用：在远程数据库被误覆盖或同步异常时，把之前备份的 SQL 文件重新导入目标库。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"
RESET_SCRIPT="${SCRIPT_DIR}/reset-remote-db.sh"

if [[ $# -lt 1 ]]; then
  echo "用法：bash scripts/db/restore-remote-db.sh <备份文件路径>"
  exit 1
fi

BACKUP_FILE="$1"

if [[ ! -f "${BACKUP_FILE}" ]]; then
  echo "备份文件不存在：${BACKUP_FILE}"
  exit 1
fi

if [[ ! -f "${CONFIG_FILE}" ]]; then
  echo "未找到配置文件：${CONFIG_FILE}"
  echo "请先复制 ${SCRIPT_DIR}/mysql-sync.env.example 为 ${SCRIPT_DIR}/mysql-sync.env 并填写连接信息。"
  exit 1
fi

if [[ ! -f "${RESET_SCRIPT}" ]]; then
  echo "未找到清库脚本：${RESET_SCRIPT}"
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

echo "即将把备份文件恢复到远程数据库："
echo "  Backup File: ${BACKUP_FILE}"
echo "  Host: ${REMOTE_DB_HOST}"
echo "  Port: ${REMOTE_DB_PORT}"
echo "  Database: ${REMOTE_DB_NAME}"
echo "  User: ${REMOTE_DB_USER}"
echo
echo "此操作会先删除远程数据库中的全部表，再导入备份文件。"

read -r -p "请输入 RESTORE ${REMOTE_DB_NAME} 确认继续: " CONFIRM_TEXT

if [[ "${CONFIRM_TEXT}" != "RESTORE ${REMOTE_DB_NAME}" ]]; then
  echo "确认文本不匹配，已取消执行。"
  exit 1
fi

echo "开始清空远程数据库 ${REMOTE_DB_NAME} ..."
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${RESET_SCRIPT}"

echo "开始恢复备份文件到远程数据库 ${REMOTE_DB_NAME} ..."
docker run --rm -i \
  --add-host=host.docker.internal:host-gateway \
  "${MYSQL_CLIENT_IMAGE}" \
  mysql \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --database="${REMOTE_DB_NAME}" < "${BACKUP_FILE}"

echo "远程数据库 ${REMOTE_DB_NAME} 已恢复完成。"
