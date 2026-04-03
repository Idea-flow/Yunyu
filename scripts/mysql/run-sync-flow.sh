#!/usr/bin/env bash

# 功能：串联执行远程数据库备份、本地数据库全量同步和结果提示。
# 作用：为数据库覆盖同步提供一个统一入口，减少手工执行多条命令时的出错概率，并输出清晰的操作日志。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CONFIG_FILE="${SCRIPT_DIR}/mysql-sync.env"
CONFIG_FILE="${MYSQL_SYNC_CONFIG:-$DEFAULT_CONFIG_FILE}"
BACKUP_SCRIPT="${SCRIPT_DIR}/backup-remote-db.sh"
SYNC_SCRIPT="${SCRIPT_DIR}/sync-local-to-remote.sh"
BACKUP_DIR="${MYSQL_BACKUP_DIR:-${SCRIPT_DIR}/backups}"
LAST_BACKUP_FILE=""

timestamp() {
  date +"%Y-%m-%d %H:%M:%S"
}

log_info() {
  echo "[$(timestamp)] [INFO] $*"
}

log_warn() {
  echo "[$(timestamp)] [WARN] $*"
}

log_error() {
  echo "[$(timestamp)] [ERROR] $*" >&2
}

on_error() {
  local exit_code=$?
  log_error "数据库同步流程执行失败。"
  if [[ -n "${LAST_BACKUP_FILE}" ]]; then
    log_warn "本次流程已生成备份文件：${LAST_BACKUP_FILE}"
    log_warn "如需恢复远程数据库，可执行："
    echo "bash scripts/mysql/restore-remote-db.sh ${LAST_BACKUP_FILE}"
  else
    log_warn "本次流程尚未生成备份文件，请先检查备份步骤是否执行成功。"
  fi
  exit "${exit_code}"
}

trap on_error ERR

if [[ ! -f "${CONFIG_FILE}" ]]; then
  log_error "未找到配置文件：${CONFIG_FILE}"
  log_error "请先复制 scripts/mysql/mysql-sync.env.example 为 scripts/mysql/mysql-sync.env 并填写连接信息。"
  exit 1
fi

if [[ ! -f "${BACKUP_SCRIPT}" ]]; then
  log_error "未找到备份脚本：${BACKUP_SCRIPT}"
  exit 1
fi

if [[ ! -f "${SYNC_SCRIPT}" ]]; then
  log_error "未找到同步脚本：${SYNC_SCRIPT}"
  exit 1
fi

# shellcheck disable=SC1090
source "${CONFIG_FILE}"

: "${LOCAL_DB_HOST:?LOCAL_DB_HOST 未配置}"
: "${LOCAL_DB_PORT:?LOCAL_DB_PORT 未配置}"
: "${LOCAL_DB_NAME:?LOCAL_DB_NAME 未配置}"
: "${REMOTE_DB_HOST:?REMOTE_DB_HOST 未配置}"
: "${REMOTE_DB_PORT:?REMOTE_DB_PORT 未配置}"
: "${REMOTE_DB_NAME:?REMOTE_DB_NAME 未配置}"

log_info "数据库同步总流程开始。"
log_info "本地数据库：${LOCAL_DB_HOST}:${LOCAL_DB_PORT}/${LOCAL_DB_NAME}"
log_info "远程数据库：${REMOTE_DB_HOST}:${REMOTE_DB_PORT}/${REMOTE_DB_NAME}"
log_warn "该流程会覆盖远程数据库中的全部表和数据。"

read -r -p "请输入 RUN SYNC FLOW 确认继续: " FLOW_CONFIRM

if [[ "${FLOW_CONFIRM}" != "RUN SYNC FLOW" ]]; then
  log_warn "确认文本不匹配，已取消执行。"
  exit 1
fi

log_info "步骤 1/3：开始备份远程数据库。"
mkdir -p "${BACKUP_DIR}"
BEFORE_BACKUP_LIST="$(find "${BACKUP_DIR}" -maxdepth 1 -type f -name "${REMOTE_DB_NAME}-*.sql" | sort || true)"
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" MYSQL_BACKUP_DIR="${BACKUP_DIR}" bash "${BACKUP_SCRIPT}"
AFTER_BACKUP_LIST="$(find "${BACKUP_DIR}" -maxdepth 1 -type f -name "${REMOTE_DB_NAME}-*.sql" | sort || true)"
LAST_BACKUP_FILE="$(comm -13 <(printf '%s\n' "${BEFORE_BACKUP_LIST}") <(printf '%s\n' "${AFTER_BACKUP_LIST}") | tail -n 1)"

if [[ -z "${LAST_BACKUP_FILE}" ]]; then
  LAST_BACKUP_FILE="$(find "${BACKUP_DIR}" -maxdepth 1 -type f -name "${REMOTE_DB_NAME}-*.sql" | sort | tail -n 1)"
fi

log_info "步骤 1/3 完成。备份文件：${LAST_BACKUP_FILE}"

log_info "步骤 2/3：开始执行本地数据库到远程数据库的全量同步。"
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" bash "${SYNC_SCRIPT}"
log_info "步骤 2/3 完成。远程数据库已被本地数据库覆盖。"

log_info "步骤 3/3：输出后续建议。"
log_info "建议立即验证远程接口、后台登录和关键页面数据。"
log_info "如同步结果异常，可使用以下命令回滚："
echo "bash scripts/mysql/restore-remote-db.sh ${LAST_BACKUP_FILE}"

log_info "数据库同步总流程执行完成。"
