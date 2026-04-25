# 升级 SQL 目录说明

本目录用于存放线上环境版本升级时执行的 SQL 脚本。

当前已补充：

1. `V20260425_001__create_user_auth_table.sql`
2. `V20260425_002__upgrade_post_content_for_content_access.sql`
3. `V20260425_003__create_content_access_grant_table.sql`
4. `V20260425_004__create_friend_link_table.sql`
5. `V20260425_005__create_attachment_file_table.sql`
6. `V20260425_006__seed_site_config_for_new_features.sql`

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
4. 每次修改本目录任一脚本时，必须同步更新 `docs/技术/sql/init.sql`。
