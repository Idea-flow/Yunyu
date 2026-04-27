#!/usr/bin/env bash
# 作用：基于本地保存的 baseUrl / token 统一发起 Yunyu 后台接口请求。
# 用途：避免每次手动拼接 Authorization、域名和 curl 参数，便于 agent 稳定调用后台接口。

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SKILL_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
LOCAL_DIR="$SKILL_DIR/.local"
CONNECTION_FILE="$LOCAL_DIR/admin-connection.json"
DEFAULT_TIMEOUT=30

# 输出脚本帮助信息。
usage() {
  cat <<'USAGE'
用法：
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh <METHOD> <PATH> [选项]

示例：
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh GET /api/admin/posts
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh GET /api/admin/posts --query 'pageNo=1&pageSize=10'
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh POST /api/admin/categories --json '{"name":"AI","slug":"ai"}'
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh PUT /api/admin/site-config --body-file payload.json
  bash .agents/skills/yunyu-admin-operator/scripts/admin_request.sh DELETE /api/admin/tags/3

选项：
  --query <QUERY>       追加查询字符串，例如 'pageNo=1&pageSize=10'
  --json <JSON>         直接传入 JSON 请求体
  --body-file <FILE>    从文件读取 JSON 请求体
  --header <HEADER>     追加自定义请求头，可重复传入
  --timeout <SECONDS>   请求超时时间，默认 30 秒
  --output <FILE>       将响应体写入指定文件
  --raw                 不做 JSON 美化，原样输出响应体
  --show-url            额外输出实际请求 URL
  -h, --help            显示帮助

说明：
  1. 脚本会自动读取 .agents/skills/yunyu-admin-operator/.local/admin-connection.json
  2. 如果 token 未带 Bearer 前缀，脚本会自动补成 Authorization: Bearer <token>
  3. 遇到 4xx/5xx 时会输出响应体并以非 0 状态退出，便于 agent 判断失败原因
USAGE
}

# 输出错误并退出。
fail() {
  echo "$1" >&2
  exit 1
}

# 确认依赖命令存在。
require_command() {
  local cmd="$1"
  command -v "$cmd" >/dev/null 2>&1 || fail "缺少依赖命令：$cmd"
}

# 确认连接信息文件存在。
require_connection_file() {
  [[ -f "$CONNECTION_FILE" ]] || fail "连接信息文件不存在：${CONNECTION_FILE}，请先执行 admin_connection.sh set"
}

# 使用 Python 读取连接信息字段，避免依赖 jq。
read_connection_field() {
  local field="$1"
  python3 - "$CONNECTION_FILE" "$field" <<'PY'
import json
import sys
from pathlib import Path

file_path = Path(sys.argv[1])
field = sys.argv[2]

try:
    data = json.loads(file_path.read_text(encoding="utf-8"))
except Exception as exc:
    print(f"连接信息文件解析失败：{exc}", file=sys.stderr)
    sys.exit(1)

value = data.get(field)
if value is None or value == "":
    print(f"连接信息字段缺失：{field}", file=sys.stderr)
    sys.exit(1)

print(value)
PY
}

# 去掉 baseUrl 尾部斜杠，避免双斜杠拼接。
normalize_base_url() {
  local base_url="$1"
  printf '%s\n' "${base_url%/}"
}

# 规范化接口路径，确保以斜杠开头。
normalize_path() {
  local path="$1"
  if [[ "$path" == /* ]]; then
    printf '%s\n' "$path"
  else
    printf '/%s\n' "$path"
  fi
}

# 构造完整 URL。
build_url() {
  local base_url="$1"
  local path="$2"
  local query="$3"
  local normalized_base normalized_path full_url
  normalized_base="$(normalize_base_url "$base_url")"
  normalized_path="$(normalize_path "$path")"
  full_url="${normalized_base}${normalized_path}"
  if [[ -n "$query" ]]; then
    full_url="${full_url}?${query}"
  fi
  printf '%s\n' "$full_url"
}

# 根据 token 内容生成 Authorization 请求头。
auth_header() {
  local token="$1"
  if [[ "$token" == Bearer\ * ]]; then
    printf 'Authorization: %s\n' "$token"
  else
    printf 'Authorization: Bearer %s\n' "$token"
  fi
}

# 尽量把 JSON 响应格式化，方便阅读；不是 JSON 时原样输出。
pretty_print_response() {
  local input_file="$1"
  python3 - "$input_file" <<'PY'
import json
import sys
from pathlib import Path

file_path = Path(sys.argv[1])
content = file_path.read_text(encoding="utf-8")
if not content.strip():
    sys.exit(0)
try:
    data = json.loads(content)
except Exception:
    sys.stdout.write(content)
    sys.exit(0)
json.dump(data, sys.stdout, ensure_ascii=False, indent=2)
sys.stdout.write("\n")
PY
}

# 发起 HTTP 请求并处理响应状态。
request_api() {
  local method="$1"
  local url="$2"
  local token="$3"
  local query="$4"
  local json_payload="$5"
  local body_file="$6"
  local timeout="$7"
  local output_file="$8"
  local raw_output="$9"
  local show_url="${10}"
  shift 10

  local temp_body curl_status http_status
  temp_body="$(mktemp)"

  local curl_args=(
    -sS
    -X "$method"
    -H "$(auth_header "$token")"
    -H 'Accept: application/json'
    --connect-timeout "$timeout"
    --max-time "$timeout"
    -o "$temp_body"
    -w '%{http_code}'
    "$url"
  )

  local header
  for header in "$@"; do
    curl_args+=( -H "$header" )
  done

  if [[ -n "$json_payload" ]]; then
    curl_args+=( -H 'Content-Type: application/json' --data "$json_payload" )
  elif [[ -n "$body_file" ]]; then
    [[ -f "$body_file" ]] || fail "请求体文件不存在：$body_file"
    curl_args+=( -H 'Content-Type: application/json' --data-binary "@$body_file" )
  fi

  if [[ "$show_url" == "true" ]]; then
    echo "URL: $url"
  fi

  set +e
  http_status="$(curl "${curl_args[@]}")"
  curl_status=$?
  set -e
  if [[ "$curl_status" -ne 0 ]]; then
    if [[ -s "$temp_body" ]]; then
      cat "$temp_body" >&2
    fi
    rm -f "$temp_body"
    fail "请求执行失败，curl 退出码：$curl_status"
  fi

  echo "HTTP_STATUS: $http_status"

  if [[ -n "$output_file" ]]; then
    cp "$temp_body" "$output_file"
    echo "响应体已写入：$output_file"
  fi

  if [[ "$raw_output" == "true" ]]; then
    cat "$temp_body"
  else
    pretty_print_response "$temp_body"
  fi

  if [[ "$http_status" -lt 200 || "$http_status" -ge 300 ]]; then
    rm -f "$temp_body"
    exit 1
  fi

  rm -f "$temp_body"
}

main() {
  require_command curl
  require_command python3
  require_connection_file

  local method="${1:-}"
  local path="${2:-}"
  if [[ -z "$method" || -z "$path" || "$method" == "-h" || "$method" == "--help" ]]; then
    usage
    exit 0
  fi
  shift 2

  local query=""
  local json_payload=""
  local body_file=""
  local timeout="$DEFAULT_TIMEOUT"
  local output_file=""
  local raw_output="false"
  local show_url="false"
  local extra_headers=()
  local has_extra_headers="false"

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --query)
        query="${2:-}"
        shift 2
        ;;
      --json)
        json_payload="${2:-}"
        shift 2
        ;;
      --body-file)
        body_file="${2:-}"
        shift 2
        ;;
      --header)
        extra_headers+=("${2:-}")
        has_extra_headers="true"
        shift 2
        ;;
      --timeout)
        timeout="${2:-}"
        shift 2
        ;;
      --output)
        output_file="${2:-}"
        shift 2
        ;;
      --raw)
        raw_output="true"
        shift
        ;;
      --show-url)
        show_url="true"
        shift
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

  if [[ -n "$json_payload" && -n "$body_file" ]]; then
    fail "--json 和 --body-file 不能同时使用"
  fi

  local base_url token url uppercase_method
  base_url="$(read_connection_field baseUrl)"
  token="$(read_connection_field token)"
  uppercase_method="$(printf '%s' "$method" | tr '[:lower:]' '[:upper:]')"
  url="$(build_url "$base_url" "$path" "$query")"

  if [[ "$has_extra_headers" == "true" ]]; then
    request_api \
      "$uppercase_method" \
      "$url" \
      "$token" \
      "$query" \
      "$json_payload" \
      "$body_file" \
      "$timeout" \
      "$output_file" \
      "$raw_output" \
      "$show_url" \
      "${extra_headers[@]}"
  else
    request_api \
      "$uppercase_method" \
      "$url" \
      "$token" \
      "$query" \
      "$json_payload" \
      "$body_file" \
      "$timeout" \
      "$output_file" \
      "$raw_output" \
      "$show_url"
  fi
}

main "$@"
