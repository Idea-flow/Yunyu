#!/usr/bin/env bash
# 作用：从仓库外层刷新 skill 使用的后台接口参考文档。
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
python3 "$SCRIPT_DIR/generate_admin_api_reference.py" "$@"
