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
AUTO_CONFIRM_ALL="${MYSQL_AUTO_CONFIRM_ALL:-false}"

timestamp() {
  date +"%Y-%m-%d %H:%M:%S"
}

# 功能：解析命令行参数。
# 作用：支持通过 `--yes` 开启全流程自动确认，避免手工输入确认文本。
parse_args() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      -y|--yes)
        AUTO_CONFIRM_ALL="true"
        shift
        ;;
      *)
        log_error "未知参数：$1"
        exit 1
        ;;
    esac
  done
}

# 功能：统一处理确认逻辑。
# 作用：在默认模式保留人工确认，在自动模式跳过交互确认。
confirm_or_exit() {
  local expected_text="$1"
  local prompt_text="$2"

  if [[ "${AUTO_CONFIRM_ALL}" == "true" ]]; then
    log_warn "已启用自动确认，跳过手工输入：${expected_text}"
    return 0
  fi

  read -r -p "${prompt_text}" flow_confirm
  if [[ "${flow_confirm}" != "${expected_text}" ]]; then
    log_warn "确认文本不匹配，已取消执行。"
    exit 1
  fi
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
    echo "bash scripts/db/restore-remote-db.sh ${LAST_BACKUP_FILE}"
  else
    log_warn "本次流程尚未生成备份文件，请先检查备份步骤是否执行成功。"
  fi
  exit "${exit_code}"
}

trap on_error ERR
parse_args "$@"

if [[ ! -f "${CONFIG_FILE}" ]]; then
  log_error "未找到配置文件：${CONFIG_FILE}"
  log_error "请先复制 scripts/db/mysql-sync.env.example 为 scripts/db/mysql-sync.env 并填写连接信息。"
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
confirm_or_exit "RUN SYNC FLOW" "请输入 RUN SYNC FLOW 确认继续: "

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
MYSQL_SYNC_CONFIG="${CONFIG_FILE}" MYSQL_AUTO_CONFIRM_ALL="${AUTO_CONFIRM_ALL}" bash "${SYNC_SCRIPT}"
log_info "步骤 2/3 完成。远程数据库已被本地数据库覆盖。"

log_info "步骤 3/3：输出后续建议。"
log_info "建议立即验证远程接口、后台登录和关键页面数据。"
log_info "如同步结果异常，可使用以下命令回滚："
echo "bash scripts/db/restore-remote-db.sh ${LAST_BACKUP_FILE}"

log_info "数据库同步总流程执行完成。"
