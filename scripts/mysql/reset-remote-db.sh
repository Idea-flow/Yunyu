#!/usr/bin/env bash

# 功能：删除远程 MySQL 目标数据库中的全部业务表。
# 作用：为“本地全量同步到远程”提供一个显式、可确认的清库步骤，避免误删错误库。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"

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

run_mysql() {
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    "${MYSQL_CLIENT_IMAGE}" \
    mysql "$@"
}

echo "即将删除远程数据库中的所有表："
echo "  Host: ${REMOTE_DB_HOST}"
echo "  Port: ${REMOTE_DB_PORT}"
echo "  Database: ${REMOTE_DB_NAME}"
echo "  User: ${REMOTE_DB_USER}"
echo
echo "此操作不可恢复。"

read -r -p "请输入 DELETE ${REMOTE_DB_NAME} 确认继续: " CONFIRM_TEXT

if [[ "${CONFIRM_TEXT}" != "DELETE ${REMOTE_DB_NAME}" ]]; then
  echo "确认文本不匹配，已取消执行。"
  exit 1
fi

TABLE_LIST="$(
  run_mysql \
    --host="${REMOTE_DB_HOST}" \
    --port="${REMOTE_DB_PORT}" \
    --user="${REMOTE_DB_USER}" \
    --password="${REMOTE_DB_PASSWORD}" \
    --database="${REMOTE_DB_NAME}" \
    --batch \
    --skip-column-names \
    --execute="SELECT table_name FROM information_schema.tables WHERE table_schema='${REMOTE_DB_NAME}' AND table_type='BASE TABLE' ORDER BY table_name;"
)"

if [[ -z "${TABLE_LIST}" ]]; then
  echo "远程数据库 ${REMOTE_DB_NAME} 中没有业务表，无需删除。"
  exit 0
fi

DROP_SQL="SET FOREIGN_KEY_CHECKS = 0;"
while IFS= read -r TABLE_NAME; do
  [[ -z "${TABLE_NAME}" ]] && continue
  DROP_SQL="${DROP_SQL} DROP TABLE IF EXISTS \`${TABLE_NAME}\`;"
done <<< "${TABLE_LIST}"
DROP_SQL="${DROP_SQL} SET FOREIGN_KEY_CHECKS = 1;"

run_mysql \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --database="${REMOTE_DB_NAME}" \
  --execute="${DROP_SQL}"

echo "远程数据库 ${REMOTE_DB_NAME} 的所有表已删除完成。"
