# 升级 SQL 目录说明

本目录用于存放线上环境版本升级时执行的 SQL 脚本。

## 命名建议

```text
VYYYYMMDD_NNN__描述.sql
RYYYYMMDD_NNN__rollback_描述.sql
```

示例：

```text
V20260425_001__add_post_status.sql
R20260425_001__rollback_add_post_status.sql
```

## 使用建议

1. 每次发布把 SQL 文件与后端镜像版本一一对应。
2. 优先提交可重复执行且向后兼容的 SQL。
3. 涉及不可逆变更时，务必提供回滚脚本或明确恢复方案。
