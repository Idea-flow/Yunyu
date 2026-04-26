#!/usr/bin/env python3
"""生成 Yunyu 后台精简接口参考文档。

作用：请求 /v3/api-docs，过滤出所有 /api/admin/** 接口，
并输出成更适合 agent 阅读的 Markdown，减少直接加载完整 OpenAPI 的 token 消耗。
"""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Iterable

HTTP_METHODS = ("get", "post", "put", "patch", "delete")
MAX_SCHEMA_RENDER_DEPTH = 2
HIGH_FREQUENCY_PATH_PREFIXES = (
    "/api/admin/posts",
    "/api/admin/categories",
    "/api/admin/tags",
    "/api/admin/topics",
    "/api/admin/comments",
    "/api/admin/attachments",
    "/api/admin/friend-links",
    "/api/admin/site-config",
    "/api/admin/homepage-config",
    "/api/admin/site/ai/providers",
    "/api/admin/site/storage/s3",
    "/api/admin/users",
)
SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent.parent
DEFAULT_OUTPUT = REPO_ROOT / ".agents" / "skills" / "yunyu-admin-operator" / "references" / "admin-api-docs.md"


def fetch_openapi(base_url: str) -> dict[str, Any]:
    """请求 OpenAPI 文档。"""
    url = f"{base_url.rstrip('/')}/v3/api-docs"
    request = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def resolve_ref_name(ref: str) -> str:
    """从 OpenAPI $ref 中提取 schema 名称。"""
    return ref.rsplit("/", 1)[-1]


def format_example(example: Any) -> str:
    """把示例值格式化为更接近 JSON 的文本。"""
    if isinstance(example, (dict, list, bool, int, float)) or example is None:
        return json.dumps(example, ensure_ascii=False)
    return str(example)


def describe_schema_type(schema: dict[str, Any] | None) -> str:
    """生成简短的 schema 类型描述。"""
    if not schema:
        return "unknown"
    if "$ref" in schema:
        return resolve_ref_name(schema["$ref"])
    if schema.get("type") == "array":
        return f"array<{describe_schema_type(schema.get('items'))}>"
    if schema.get("type"):
        return schema["type"]
    if "allOf" in schema:
        return "allOf"
    if "oneOf" in schema:
        return "oneOf"
    return "object"


def merge_object_schema(schema: dict[str, Any], components: dict[str, Any], seen: set[str] | None = None) -> tuple[dict[str, Any], set[str]]:
    """解析对象 schema 的顶层字段与必填信息。"""
    seen = seen or set()
    properties: dict[str, Any] = {}
    required: set[str] = set()

    if not schema:
        return properties, required

    if "$ref" in schema:
        ref_name = resolve_ref_name(schema["$ref"])
        if ref_name in seen:
            return properties, required
        seen.add(ref_name)
        target = components.get(ref_name, {})
        return merge_object_schema(target, components, seen)

    for item in schema.get("allOf", []):
        sub_properties, sub_required = merge_object_schema(item, components, seen.copy())
        properties.update(sub_properties)
        required.update(sub_required)

    schema_properties = schema.get("properties", {}) or {}
    properties.update(schema_properties)
    required.update(schema.get("required", []) or [])
    return properties, required


def render_property_line(name: str, schema: dict[str, Any], required: bool) -> str:
    """渲染单个字段描述。"""
    type_name = describe_schema_type(schema)
    required_text = "必填" if required else "可选"
    description = (schema.get("description") or "").strip()
    enum_values = schema.get("enum") or []
    example = schema.get("example")
    extras: list[str] = [type_name, required_text]
    if enum_values:
        extras.append("枚举: " + ", ".join(str(item) for item in enum_values))
    if example is not None and str(example).strip():
        extras.append("示例: " + format_example(example))
    line = f"- `{name}` ({'; '.join(extras)})"
    if description:
        line += f"：{description}"
    return line


def resolve_schema(schema: dict[str, Any] | None,
                   components: dict[str, Any],
                   seen: set[str] | None = None) -> dict[str, Any]:
    """解析并返回可直接读取的 schema。"""
    seen = seen or set()
    if not schema:
        return {}

    if "$ref" in schema:
        ref_name = resolve_ref_name(schema["$ref"])
        if ref_name in seen:
            return {}
        seen.add(ref_name)
        return resolve_schema(components.get(ref_name, {}), components, seen)

    if "allOf" in schema:
        merged: dict[str, Any] = {}
        properties, required = merge_object_schema(schema, components, seen.copy())
        if properties:
            merged["type"] = "object"
            merged["properties"] = properties
            if required:
                merged["required"] = sorted(required)
        for key in ("description", "example", "enum"):
            if key in schema:
                merged[key] = schema[key]
        return merged or schema
    return schema


def render_nested_property_lines(name: str,
                                 schema: dict[str, Any],
                                 components: dict[str, Any],
                                 depth: int = 1,
                                 seen: set[str] | None = None) -> list[str]:
    """递归渲染对象或数组内部字段。"""
    if depth > MAX_SCHEMA_RENDER_DEPTH:
        return []

    seen = seen or set()
    resolved_schema = resolve_schema(schema, components, seen.copy())
    if not resolved_schema:
        return []

    if resolved_schema.get("type") == "array":
        item_schema = resolved_schema.get("items") or {}
        item_name = f"{name}[]"
        return render_nested_property_lines(item_name, item_schema, components, depth, seen.copy())

    properties, required = merge_object_schema(resolved_schema, components, seen.copy())
    if not properties:
        return []

    lines: list[str] = []
    for field_name, field_schema in properties.items():
        full_name = f"{name}.{field_name}" if name else field_name
        lines.append(render_property_line(full_name, field_schema, field_name in required))
        if depth < MAX_SCHEMA_RENDER_DEPTH:
            lines.extend(render_nested_property_lines(full_name, field_schema, components, depth + 1, seen.copy()))
    return lines


def render_schema_lines(schema: dict[str, Any], components: dict[str, Any]) -> list[str]:
    """渲染 schema 的字段描述，必要时递归展开子对象。"""
    properties, required = merge_object_schema(schema, components)
    lines: list[str] = []
    for name, field_schema in properties.items():
        lines.append(render_property_line(name, field_schema, name in required))
        lines.extend(render_nested_property_lines(name, field_schema, components))
    return lines


def render_parameters(parameters: Iterable[dict[str, Any]]) -> list[str]:
    """渲染路径参数和查询参数。"""
    result: list[str] = []
    for parameter in parameters:
        name = parameter.get("name", "unknown")
        location = parameter.get("in", "query")
        required = "必填" if parameter.get("required") else "可选"
        schema = parameter.get("schema")
        schema_type = describe_schema_type(schema)
        description = (parameter.get("description") or "").strip()
        extras = [location, schema_type, required]
        example = (schema or {}).get("example")
        if example is not None and str(example).strip():
            extras.append("示例: " + format_example(example))
        line = f"- `{name}` ({'; '.join(extras)})"
        if description:
            line += f"：{description}"
        result.append(line)
    return result


def extract_query_schema_fields(parameters: Iterable[dict[str, Any]], components: dict[str, Any]) -> list[str]:
    """展开 query DTO，输出真实查询参数字段。"""
    lines: list[str] = []
    for parameter in parameters:
        if parameter.get("in") != "query":
            continue
        schema = parameter.get("schema") or {}
        resolved_schema = resolve_schema(schema, components)
        properties, required = merge_object_schema(resolved_schema, components)
        if not properties:
            continue
        for name, field_schema in properties.items():
            lines.append(render_property_line(name, field_schema, name in required))
    return lines


def extract_request_schema(operation: dict[str, Any], components: dict[str, Any]) -> tuple[str | None, list[str]]:
    """提取请求体 schema 摘要与顶层字段。"""
    request_body = operation.get("requestBody") or {}
    content = request_body.get("content") or {}
    payload = content.get("application/json") or next(iter(content.values()), None)
    if not payload:
        return None, []
    schema = payload.get("schema") or {}
    schema_name = describe_schema_type(schema)
    return schema_name, render_schema_lines(schema, components)


def should_expand_response_fields(path: str, method: str) -> bool:
    """判断当前接口是否需要展开第一层响应字段。"""
    if method == "DELETE":
        return False
    return path.startswith(HIGH_FREQUENCY_PATH_PREFIXES)


def extract_response_fields(operation: dict[str, Any],
                            components: dict[str, Any],
                            path: str,
                            method: str) -> list[str]:
    """提取高频接口的第一层响应字段。"""
    if not should_expand_response_fields(path, method):
        return []

    response = ((operation.get("responses") or {}).get("200") or {})
    content = response.get("content") or {}
    payload = content.get("application/json") or next(iter(content.values()), None)
    if not payload:
        return []

    schema = resolve_schema((payload or {}).get("schema") or {}, components)
    properties, _ = merge_object_schema(schema, components)
    if not properties:
        return []

    data_schema = properties.get("data")
    if not data_schema:
        return []

    data_resolved_schema = resolve_schema(data_schema, components)
    data_properties, data_required = merge_object_schema(data_resolved_schema, components)
    if not data_properties:
        return []

    return [
        render_property_line(f"data.{name}", field_schema, name in data_required)
        for name, field_schema in data_properties.items()
    ]


def extract_response_list_item_fields(operation: dict[str, Any],
                                      components: dict[str, Any],
                                      path: str,
                                      method: str) -> list[str]:
    """提取高频列表接口中 data.list[] 的首层字段提示。"""
    if not should_expand_response_fields(path, method):
        return []

    response = ((operation.get("responses") or {}).get("200") or {})
    content = response.get("content") or {}
    payload = content.get("application/json") or next(iter(content.values()), None)
    if not payload:
        return []

    schema = resolve_schema((payload or {}).get("schema") or {}, components)
    response_properties, _ = merge_object_schema(schema, components)
    data_schema = response_properties.get("data")
    if not data_schema:
        return []

    data_resolved_schema = resolve_schema(data_schema, components)
    data_properties, _ = merge_object_schema(data_resolved_schema, components)
    list_schema = data_properties.get("list")
    if not list_schema:
        return []

    list_resolved_schema = resolve_schema(list_schema, components)
    if list_resolved_schema.get("type") != "array":
        return []

    item_schema = resolve_schema(list_resolved_schema.get("items") or {}, components)
    item_properties, item_required = merge_object_schema(item_schema, components)
    if not item_properties:
        return []

    return [
        render_property_line(f"data.list[].{name}", field_schema, name in item_required)
        for name, field_schema in item_properties.items()
    ]


def extract_response_summaries(operation: dict[str, Any]) -> list[str]:
    """提取响应结构摘要。"""
    results: list[str] = []
    for status_code, response in sorted((operation.get("responses") or {}).items()):
        content = response.get("content") or {}
        payload = content.get("application/json") or next(iter(content.values()), None)
        schema = (payload or {}).get("schema") or {}
        description = (response.get("description") or "").strip()
        schema_name = describe_schema_type(schema)
        parts = [f"`{status_code}`", schema_name]
        if description:
            parts.append(description)
        results.append("- " + "；".join(parts))
    return results


def infer_risk_level(method: str, path: str, summary: str) -> str:
    """根据 HTTP method 和语义关键字推断风险等级。"""
    text = f"{path} {summary}".lower()
    if method == "delete":
        return "HIGH"
    if any(keyword in text for keyword in ("batch", "批量", "publish", "发布", "offline", "下线", "delete", "remove", "删除")):
        return "HIGH"
    if any(keyword in text for keyword in ("test", "测试", "query", "查询", "get", "check", "检查")) and method == "get":
        return "LOW"
    if method == "get":
        return "LOW"
    return "MEDIUM"


def collect_admin_operations(document: dict[str, Any]) -> list[dict[str, Any]]:
    """收集所有后台接口操作。"""
    operations: list[dict[str, Any]] = []
    components = ((document.get("components") or {}).get("schemas") or {})
    for path, path_item in sorted((document.get("paths") or {}).items()):
        if not path.startswith("/api/admin/"):
            continue
        for method in HTTP_METHODS:
            operation = (path_item or {}).get(method)
            if not operation:
                continue
            summary = (operation.get("summary") or operation.get("operationId") or path).strip()
            tag = ((operation.get("tags") or ["未分组"]) or ["未分组"])[0]
            request_schema_name, request_fields = extract_request_schema(operation, components)
            query_fields = extract_query_schema_fields(operation.get("parameters") or [], components)
            operations.append(
                {
                    "tag": tag,
                    "method": method.upper(),
                    "path": path,
                    "summary": summary,
                    "operation_id": operation.get("operationId") or "",
                    "risk_level": infer_risk_level(method, path, summary),
                    "parameters": render_parameters(operation.get("parameters") or []),
                    "query_fields": query_fields,
                    "request_schema_name": request_schema_name,
                    "request_fields": request_fields,
                    "responses": extract_response_summaries(operation),
                    "response_fields": extract_response_fields(operation, components, path, method.upper()),
                    "response_list_item_fields": extract_response_list_item_fields(operation, components, path, method.upper()),
                }
            )
    return operations


def build_markdown(base_url: str, operations: list[dict[str, Any]]) -> str:
    """构建精简后的 Markdown 文档。"""
    generated_at = datetime.now(timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S %z")
    grouped: dict[str, list[dict[str, Any]]] = defaultdict(list)
    for operation in operations:
        grouped[operation["tag"]].append(operation)

    lines: list[str] = [
        "# Yunyu 后台接口参考",
        "",
        "本文档由仓库外层脚本从 `/v3/api-docs` 自动生成，只保留 `/api/admin/**` 接口，用于让 agent 以更低 token 成本快速读取后台能力。",
        "",
        f"- 来源：`{base_url.rstrip('/')}/v3/api-docs`",
        f"- 生成时间：`{generated_at}`",
        f"- 接口总数：`{len(operations)}`",
        "",
        "## 接口总览",
        "",
    ]

    for operation in operations:
        lines.append(f"- `{operation['method']} {operation['path']}`：{operation['summary']} [{operation['risk_level']}]")

    for tag in sorted(grouped):
        lines.extend(["", f"## {tag}", ""])
        for operation in grouped[tag]:
            lines.extend(
                [
                    f"### {operation['method']} {operation['path']}",
                    "",
                    f"- 摘要：{operation['summary']}",
                    f"- `operationId`：`{operation['operation_id']}`" if operation["operation_id"] else "- `operationId`：未提供",
                    f"- 风险等级：`{operation['risk_level']}`",
                ]
            )

            lines.append("")
            lines.append("#### 参数")
            lines.append("")
            if operation["parameters"]:
                lines.extend(operation["parameters"])
                if operation["query_fields"]:
                    lines.append("- Query DTO 展开：")
                    lines.extend(operation["query_fields"])
            else:
                lines.append("- 无路径参数或查询参数")

            lines.append("")
            lines.append("#### 请求体")
            lines.append("")
            if operation["request_schema_name"]:
                lines.append(f"- Schema：`{operation['request_schema_name']}`")
                if operation["request_fields"]:
                    lines.extend(operation["request_fields"])
                else:
                    lines.append("- 无可展开的顶层字段")
            else:
                lines.append("- 无请求体")

            lines.append("")
            lines.append("#### 响应")
            lines.append("")
            if operation["responses"]:
                lines.extend(operation["responses"])
                if operation["response_fields"]:
                    lines.append("- 响应字段展开（高频接口第一层）：")
                    lines.extend(operation["response_fields"])
                if operation["response_list_item_fields"]:
                    lines.append("- 列表项结构提示（`data.list[]` 第一层）：")
                    lines.extend(operation["response_list_item_fields"])
            else:
                lines.append("- 无响应描述")

    lines.append("")
    return "\n".join(lines)


def parse_args() -> argparse.Namespace:
    """解析命令行参数。"""
    parser = argparse.ArgumentParser(description="生成 Yunyu 后台精简接口参考文档")
    parser.add_argument("--base-url", default="http://127.0.0.1:20000", help="后端服务地址，默认 http://127.0.0.1:20000")
    parser.add_argument("--output", default=str(DEFAULT_OUTPUT), help="输出 Markdown 文件路径")
    return parser.parse_args()


def main() -> int:
    """脚本入口。"""
    args = parse_args()
    output_path = Path(args.output).resolve()
    output_path.parent.mkdir(parents=True, exist_ok=True)

    try:
        document = fetch_openapi(args.base_url)
    except urllib.error.URLError as error:
        print(f"[ERROR] 无法请求 OpenAPI 文档：{error}", file=sys.stderr)
        return 1
    except json.JSONDecodeError as error:
        print(f"[ERROR] OpenAPI 文档不是合法 JSON：{error}", file=sys.stderr)
        return 1

    operations = collect_admin_operations(document)
    markdown = build_markdown(args.base_url, operations)
    output_path.write_text(markdown, encoding="utf-8")
    print(f"[OK] 已生成后台接口参考：{output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
