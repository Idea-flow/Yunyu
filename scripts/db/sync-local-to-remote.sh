#!/usr/bin/env bash

# 功能：将本地 MySQL 数据库的表结构和数据全量同步到远程数据库。
# 作用：在开发环境确认本地数据正确后，一次性覆盖远程目标库，适合演示环境和初始化环境同步。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"
RESET_SCRIPT="${SCRIPT_DIR}/reset-remote-db.sh"
TMP_DUMP_FILE="$(mktemp "/tmp/yunyu-mysql-sync-XXXXXX.sql")"
AUTO_CONFIRM_ALL="${MYSQL_AUTO_CONFIRM_ALL:-false}"

cleanup() {
  rm -f "${TMP_DUMP_FILE}"
}
trap cleanup EXIT

file_size_bytes() {
  if [[ -f "$1" ]]; then
    wc -c < "$1" | tr -d ' '
  else
    echo "0"
  fi
}

# 功能：解析命令行参数。
# 作用：支持通过 `--yes` 开启自动确认模式。
parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      -y|--yes)
        AUTO_CONFIRM_ALL="true"
        shift
        ;;
      *)
        echo "未知参数：$1"
        exit 1
        ;;
    esac
  done
}

# 功能：统一处理确认逻辑。
# 作用：在自动确认模式下跳过手工输入确认文本。
confirm_or_exit() {
  local expected_text="$1"
  local prompt_text="$2"

  if [[ "${AUTO_CONFIRM_ALL}" == "true" ]]; then
    echo "已启用自动确认，跳过手工输入：${expected_text}"
    return 0
  fi

  read -r -p "${prompt_text}" confirm_text
  if [[ "${confirm_text}" != "${expected_text}" ]]; then
    echo "确认文本不匹配，已取消执行。"
    exit 1
  fi
}

parse_args "$@"

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
confirm_or_exit "SYNC ${LOCAL_DB_NAME} TO ${REMOTE_DB_NAME}" "请输入 SYNC ${LOCAL_DB_NAME} TO ${REMOTE_DB_NAME} 确认继续: "

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
  "${LOCAL_DB_NAME}" > "${TMP_DUMP_FILE}" &

DUMP_PID=$!

while kill -0 "${DUMP_PID}" >/dev/null 2>&1; do
  sleep 5
  CURRENT_DUMP_SIZE_BYTES="$(file_size_bytes "${TMP_DUMP_FILE}")"
  echo "本地数据库导出进行中，当前临时 SQL 文件大小：${CURRENT_DUMP_SIZE_BYTES} bytes"
done

wait "${DUMP_PID}"

FINAL_DUMP_SIZE_BYTES="$(file_size_bytes "${TMP_DUMP_FILE}")"
echo "本地数据库导出完成，临时 SQL 文件大小：${FINAL_DUMP_SIZE_BYTES} bytes"

echo "本地数据库导出完成，开始清空远程数据库 ${REMOTE_DB_NAME} ..."
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" MYSQL_AUTO_CONFIRM_ALL="${AUTO_CONFIRM_ALL}" bash "${RESET_SCRIPT}"

echo "开始将本地数据导入远程数据库 ${REMOTE_DB_NAME} ..."
echo "说明：导入过程中 mysql 客户端通常不会持续输出日志，脚本会每 5 秒打印一次导入心跳。"
docker run --rm -i \
  --add-host=host.docker.internal:host-gateway \
  "${MYSQL_CLIENT_IMAGE}" \
  mysql \
  --host="${REMOTE_DB_HOST}" \
  --port="${REMOTE_DB_PORT}" \
  --user="${REMOTE_DB_USER}" \
  --password="${REMOTE_DB_PASSWORD}" \
  --database="${REMOTE_DB_NAME}" < "${TMP_DUMP_FILE}" &

IMPORT_PID=$!

while kill -0 "${IMPORT_PID}" >/dev/null 2>&1; do
  sleep 5
  echo "远程数据库导入进行中，目标库：${REMOTE_DB_NAME}，当前导入源文件大小：${FINAL_DUMP_SIZE_BYTES} bytes"
done

wait "${IMPORT_PID}"

echo "本地数据库 ${LOCAL_DB_NAME} 已成功同步到远程数据库 ${REMOTE_DB_NAME} 。"
