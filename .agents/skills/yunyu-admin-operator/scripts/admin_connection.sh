#!/usr/bin/env bash
# 作用：读取、保存、展示和清理 yunyu-admin-operator 的本地后台连接信息。
# 用途：统一管理 baseUrl / token，避免每次操作后台时重复输入。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCAL_DIR="$SKILL_DIR/.local"
CONNECTION_FILE="$LOCAL_DIR/admin-connection.json"

# 输出脚本帮助信息。
usage() {
  cat <<'USAGE'
用法：
  bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh show
  bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh get baseUrl
  bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh get token
  bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh set --base-url <URL> --token <TOKEN>
  bash .agents/skills/yunyu-admin-operator/scripts/admin_connection.sh clear

说明：
  show            展示当前连接信息，token 会做脱敏处理。
  get <field>     读取指定字段，支持 baseUrl / token / updatedAt。
  set             保存或覆盖连接信息。
  clear           删除本地连接信息文件。
USAGE
}

# 确保本地目录存在。
ensure_local_dir() {
  mkdir -p "$LOCAL_DIR"
}

# 判断连接文件是否存在。
require_connection_file() {
  if [[ ! -f "$CONNECTION_FILE" ]]; then
    echo "连接信息文件不存在：$CONNECTION_FILE" >&2
    exit 1
  fi
}

# 使用 Python 读取 JSON 字段，避免依赖 jq。
read_field() {
  local field="$1"
  python3 - "$CONNECTION_FILE" "$field" <<'PY'
import json
import sys
from pathlib import Path

file_path = Path(sys.argv[1])
field = sys.argv[2]

if not file_path.exists():
    print(f"连接信息文件不存在：{file_path}", file=sys.stderr)
    sys.exit(1)

try:
    data = json.loads(file_path.read_text(encoding="utf-8"))
except Exception as exc:
    print(f"连接信息文件解析失败：{exc}", file=sys.stderr)
    sys.exit(1)

value = data.get(field)
if value is None:
    print(f"字段不存在或为空：{field}", file=sys.stderr)
    sys.exit(1)

print(value)
PY
}

# 对 token 做脱敏展示，避免完整回显。
mask_token() {
  local token="$1"
  local length=${#token}
  if (( length <= 8 )); then
    printf '%s\n' '***'
    return
  fi
  local prefix="${token:0:4}"
  local suffix="${token: -4}"
  printf '%s***%s\n' "$prefix" "$suffix"
}

# 展示当前连接信息。
show_connection() {
  require_connection_file
  local base_url token updated_at masked_token
  base_url="$(read_field baseUrl)"
  token="$(read_field token)"
  updated_at="$(read_field updatedAt 2>/dev/null || true)"
  masked_token="$(mask_token "$token")"

  echo "baseUrl: $base_url"
  echo "token: $masked_token"
  if [[ -n "$updated_at" ]]; then
    echo "updatedAt: $updated_at"
  fi
}

# 保存连接信息。
set_connection() {
  local base_url=""
  local token=""

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --base-url)
        base_url="${2:-}"
        shift 2
        ;;
      --token)
        token="${2:-}"
        shift 2
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *)
        echo "未知参数：$1" >&2
        usage >&2
        exit 1
        ;;
    esac
  done

  if [[ -z "$base_url" || -z "$token" ]]; then
    echo "保存连接信息时必须同时提供 --base-url 和 --token" >&2
    exit 1
  fi

  ensure_local_dir
  python3 - "$CONNECTION_FILE" "$base_url" "$token" <<'PY'
import json
import sys
from datetime import datetime, timezone, timedelta
from pathlib import Path

file_path = Path(sys.argv[1])
base_url = sys.argv[2]
token = sys.argv[3]
shanghai = timezone(timedelta(hours=8))
updated_at = datetime.now(shanghai).isoformat(timespec="seconds")

payload = {
    "baseUrl": base_url,
    "token": token,
    "updatedAt": updated_at,
}
file_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
PY

  echo "连接信息已保存到：$CONNECTION_FILE"
}

# 删除本地连接信息。
clear_connection() {
  if [[ -f "$CONNECTION_FILE" ]]; then
    rm -f "$CONNECTION_FILE"
    echo "已删除连接信息文件：$CONNECTION_FILE"
  else
    echo "连接信息文件不存在，无需删除：$CONNECTION_FILE"
  fi
}

main() {
  local command="${1:-}"
  case "$command" in
    show)
      shift
      show_connection "$@"
      ;;
    get)
      shift
      if [[ $# -ne 1 ]]; then
        echo "get 需要提供字段名" >&2
        usage >&2
        exit 1
      fi
      require_connection_file
      read_field "$1"
      ;;
    set)
      shift
      set_connection "$@"
      ;;
    clear)
      shift
      clear_connection "$@"
      ;;
    -h|--help|help|"")
      usage
      ;;
    *)
      echo "未知命令：$command" >&2
      usage >&2
      exit 1
      ;;
  esac
}

main "$@"
