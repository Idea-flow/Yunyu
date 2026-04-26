# 分类 / 标签 / 专题管理操作说明

本文档用于指导 agent 操作分类、标签、专题三类内容编排资源。

## 一、通用规则

1. 创建时若用户没有提供 `slug`，可根据名称生成。
2. `slug` 建议使用小写英文和连字符。
3. 更新时不要只凭用户一句话直接发 `PUT`。
4. 应先查询详情，再在原数据基础上合并修改。

## 二、分类管理

接口：

1. `GET /api/admin/categories`
2. `POST /api/admin/categories`
3. `GET /api/admin/categories/{categoryId}`
4. `PUT /api/admin/categories/{categoryId}`
5. `DELETE /api/admin/categories/{categoryId}`

字段注意：

1. `name`：必填
2. `slug`：必填
3. `description`：可选
4. `coverUrl`：可选
5. `sortOrder`：可选，数值越小通常越靠前
6. `status`：
   - `ACTIVE`：启用
   - `DISABLED`：停用

## 三、标签管理

接口：

1. `GET /api/admin/tags`
2. `POST /api/admin/tags`
3. `GET /api/admin/tags/{tagId}`
4. `PUT /api/admin/tags/{tagId}`
5. `DELETE /api/admin/tags/{tagId}`

字段注意：

1. `name`：必填
2. `slug`：必填
3. `description`：可选
4. `status`：
   - `ACTIVE`：启用
   - `DISABLED`：停用

## 四、专题管理

接口：

1. `GET /api/admin/topics`
2. `POST /api/admin/topics`
3. `GET /api/admin/topics/{topicId}`
4. `PUT /api/admin/topics/{topicId}`
5. `DELETE /api/admin/topics/{topicId}`

字段注意：

1. `name`：必填
2. `slug`：必填
3. `description`：可选
4. `coverUrl`：可选
5. `sortOrder`：可选
6. `status`：
   - `ACTIVE`：启用
   - `DISABLED`：停用

## 五、更新时的建议流程

对分类、标签、专题执行编辑时，建议固定流程：

1. 先根据名称或 ID 找到目标对象
2. 读取详情
3. 合并本次修改
4. 再发 `PUT`

## 六、删除时的建议流程

删除是高风险动作。

建议：

1. 先向用户确认目标对象
2. 明确提示这是删除操作
3. 确认后再调用 `DELETE`
