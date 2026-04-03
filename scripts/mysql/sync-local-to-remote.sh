#!/usr/bin/env bash

# 功能：将本地 MySQL 数据库的表结构和数据全量同步到远程数据库。
# 作用：在开发环境确认本地数据正确后，一次性覆盖远程目标库，适合演示环境和初始化环境同步。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"
RESET_SCRIPT="${SCRIPT_DIR}/reset-remote-db.sh"
TMP_DUMP_FILE="$(mktemp "/tmp/yunyu-mysql-sync-XXXXXX.sql")"

cleanup() {
  rm -f "${TMP_DUMP_FILE}"
}
trap cleanup EXIT

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

: "${LOCAL_DB_HOST:?LOCAL_DB_HOST 未配置}"
: "${LOCAL_DB_PORT:?LOCAL_DB_PORT 未配置}"
: "${LOCAL_DB_NAME:?LOCAL_DB_NAME 未配置}"
: "${LOCAL_DB_USER:?LOCAL_DB_USER 未配置}"
: "${LOCAL_DB_PASSWORD:?LOCAL_DB_PASSWORD 未配置}"
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

run_mysqldump() {
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    "${MYSQL_CLIENT_IMAGE}" \
    mysqldump "$@"
}

echo "即将执行本地到远程的全量数据库同步："
echo "  Local : ${LOCAL_DB_HOST}:${LOCAL_DB_PORT}/${LOCAL_DB_NAME}"
echo "  Remote: ${REMOTE_DB_HOST}:${REMOTE_DB_PORT}/${REMOTE_DB_NAME}"
echo
echo "此操作会先清空远程数据库中的全部表，再导入本地库中的所有表和数据。"

read -r -p "请输入 SYNC ${LOCAL_DB_NAME} TO ${REMOTE_DB_NAME} 确认继续: " CONFIRM_TEXT

if [[ "${CONFIRM_TEXT}" != "SYNC ${LOCAL_DB_NAME} TO ${REMOTE_DB_NAME}" ]]; then
  echo "确认文本不匹配，已取消执行。"
  exit 1
fi

echo "开始导出本地数据库 ${LOCAL_DB_NAME} ..."
run_mysqldump \
  --host="${LOCAL_DB_HOST}" \
  --port="${LOCAL_DB_PORT}" \
  --user="${LOCAL_DB_USER}" \
  --password="${LOCAL_DB_PASSWORD}" \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --set-gtid-purged=OFF \
  "${LOCAL_DB_NAME}" > "${TMP_DUMP_FILE}"

echo "本地数据库导出完成，开始清空远程数据库 ${REMOTE_DB_NAME} ..."
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${RESET_SCRIPT}"

echo "开始将本地数据导入远程数据库 ${REMOTE_DB_NAME} ..."
docker run --rm -i \
  --add-host=host.docker.internal:host-gateway \
  "${MYSQL_CLIENT_IMAGE}" \
  mysql \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --database="${REMOTE_DB_NAME}" < "${TMP_DUMP_FILE}"

echo "本地数据库 ${LOCAL_DB_NAME} 已成功同步到远程数据库 ${REMOTE_DB_NAME} 。"
