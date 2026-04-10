# Yunyu Native Image Support 开发落地设计

## 1. 文档目的

本文档是在方案方向已经确认后的开发前定稿文档，目标是把 `yunyu-native-image-support` 的实现边界、工程结构、模块职责、接入方式和开发顺序落实到可执行层面。

本文档对应的前置方案文档为：

- [01-yunyu-server-native-image-support-方案设计.md](/Users/wangpenglong/projects/full-stack-project/Yunyu/docs/后端/native-image/01-yunyu-server-native-image-support-方案设计.md)

## 2. 本期最终边界

本期按当前确认结论，边界固定如下：

1. 不在 `Yunyu` 仓库根目录新增聚合 `pom.xml`
2. 新建独立工程目录 `yunyu-native-image-support`
3. `yunyu-native-image-support` 内部拆分为 3 个 Maven 模块
4. 先在 `yunyu-native-image-support` 内执行 `mvn clean install`
5. 再由 `yunyu-server` 通过普通 Maven 依赖方式引用 starter
6. `yunyu-server` 不改任何现有业务 Java 源码
7. `yunyu-server` 只允许修改 `pom.xml`、Native 配置和少量 Native 专用资源文件

## 3. 独立工程结构定稿

### 3.1 目录结构

建议最终落地为：

```text
Yunyu
├── yunyu-native-image-support
│   ├── pom.xml
│   ├── yunyu-native-image-support-core
│   │   ├── pom.xml
│   │   └── src/main/java
│   ├── yunyu-native-image-support-lambda-override
│   │   ├── pom.xml
│   │   └── src/main/java
│   └── yunyu-native-image-support-starter
│       ├── pom.xml
│       ├── src/main/java
│       └── src/main/resources
├── yunyu-server
├── yunyu-web
└── docs
```

### 3.2 父工程 `pom.xml` 职责

`yunyu-native-image-support/pom.xml` 只承担以下职责：

1. 作为 3 个子模块的父工程
2. 统一版本号
3. 统一 `java.version`
4. 统一 `spring-boot` 与 `mybatis-plus` 版本
5. 统一 GraalVM Native 相关依赖版本

它不承担：

1. 业务应用打包
2. 业务配置
3. `yunyu-server` 的模块聚合

### 3.3 版本策略

第一版建议尽量与 `yunyu-server` 对齐：

- `spring-boot-starter-parent`: `4.0.5`
- `java.version`: `25`
- `mybatis-plus.version`: `3.5.15`

这样可以减少类库版本差异带来的 Native 偏差。

## 4. 三个模块的职责定稿

### 4.1 `yunyu-native-image-support-core`

这是核心模块，负责 Native 元数据与运行时兼容能力。

建议放入的内容：

1. `RuntimeHintsRegistrar`
2. MyBatis / MyBatis-Plus 反射注册逻辑
3. MyBatis 插件链 JDK 代理注册逻辑
4. 实体、Mapper、Lambda 捕获类扫描逻辑
5. 资源文件注册逻辑
6. starter 可复用的配置对象

第一版建议优先做的能力：

1. 默认根据启动类主包递归扫描
2. 自动发现 `BaseMapper` 子接口
3. 自动发现实体类
4. 注册 `application*.yml`
5. 注册 `db/init/*.sql`
6. 注册 MyBatis-Plus `Wrapper` 相关类型

第一版暂不强求：

1. XML Mapper 自动定位增强
2. 复杂 SPI 资源合并
3. 所有第三方库的全量自定义 Hint

### 4.2 `yunyu-native-image-support-lambda-override`

这是兼容模块，负责 MyBatis-Plus Lambda Native 兼容。

建议只承载一件事：

1. 覆盖或替代 MyBatis-Plus 默认的 `LambdaUtils` Lambda 元信息提取路径

设计目标：

1. 优先用 `writeReplace`
2. 失败后尝试 `MethodHandle`
3. 尽可能减少 Native 环境下对默认序列化兜底路径的依赖

这个模块需要尽量“窄”：

1. 不混入 starter 自动装配逻辑
2. 不混入 RuntimeHints 注册逻辑
3. 不混入业务项目扫描逻辑

### 4.3 `yunyu-native-image-support-starter`

这是对外使用的模块，也是 `yunyu-server` 唯一直接依赖的模块。

建议放入的内容：

1. `AutoConfiguration`
2. 配置属性类绑定
3. `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
4. 对 `core` 模块的自动导入
5. 对 `lambda-override` 模块的依赖传递

starter 的原则是：

1. 对业务工程低侵入
2. 默认主包扫描
3. 只有业务明确需要时才暴露扩展配置

## 5. `yunyu-server` 最小接入面定稿

### 5.1 `pom.xml` 改动项

`yunyu-server/pom.xml` 第一阶段只做以下改动：

1. 增加 `yunyu-native-image-support-starter` 依赖
2. 增加 `native` profile
3. 在 `native` profile 中增加 `native-maven-plugin`

第一版建议保留的构建参数：

```text
--no-fallback
-Dfile.encoding=UTF-8
```

开发前预估里，第一版原本不默认加入的构建参数如下：

```text
--features=...
-Dyunyu.native.scan-packages=...
```

但结合本轮实际构建与运行验证，当前结论已经更新为：

1. `scan-packages` 仍然不建议默认手工传入
2. `--features` 在当前项目里已经被验证为 **需要启用**
3. 最终以 [03-yunyu-server-native-image-构建与真机验证说明.md](/Users/wangpenglong/projects/full-stack-project/Yunyu/docs/后端/native-image/03-yunyu-server-native-image-构建与真机验证说明.md) 为准

### 5.2 配置文件改动项

第一阶段主要改：

- `yunyu-server/src/main/resources/application-native.yml`

建议新增配置形式如下：

```yaml
yunyu:
  native:
    enabled: true
    scan-main-package: true
    scan-packages: []
```

语义说明：

1. `enabled`
   - 是否启用 starter Native 支持

2. `scan-main-package`
   - 是否默认扫描启动类主包
   - 第一版默认 `true`

3. `scan-packages`
   - 主包之外的补充扫描包
   - 第一版默认空数组

### 5.3 允许新增的资源文件

如果第一版需要，允许在 `yunyu-server/src/main/resources` 下新增少量 Native 配置文件，例如：

- `META-INF/native-image/...`

但原则上第一优先级还是：

1. 先用 starter 的 `RuntimeHints`
2. 再考虑是否必须补充静态 `native-image` 配置文件

也就是说，业务工程内的静态 Native 配置文件应尽量少。

## 6. 默认扫描策略定稿

### 6.1 为什么不做“扫描全部 classpath”

不建议默认扫描全部 classpath，原因如下：

1. 扫描范围过大，Native 构建元数据容易失控
2. 会把大量不相关第三方类带入反射注册范围
3. 会增加构建时间和镜像体积
4. 会降低问题定位的可控性

### 6.2 第一版默认扫描规则

第一版建议规则：

1. 以启动类 `com.ideaflow.yunyu.YunyuServerApplication` 所在包作为根包
2. 递归扫描其所有子包
3. 在此基础上，若配置了 `scan-packages`，再额外补充

对于当前 `yunyu-server` 而言，这已经能覆盖：

- `com.ideaflow.yunyu.module`
- `com.ideaflow.yunyu.security`
- `com.ideaflow.yunyu.common`
- `com.ideaflow.yunyu.infrastructure`

因此第一版 **不需要** 手写额外扫描包。

### 6.3 哪些类型需要被扫描发现

第一版扫描目标建议包括：

1. `BaseMapper` 子接口
2. 标注 `@Mapper` 的接口
3. 实体类
4. 含有 Lambda 捕获入口的业务类
5. 启动类本身

## 7. `--features` 参数的定稿策略

### 7.1 第一版默认策略

第一版不把 `--features=...` 作为默认接入要求。

首选路线：

1. Spring AOT
2. starter 自动装配
3. `RuntimeHintsRegistrar`

只有在以下情况出现时，才升级到自定义 GraalVM `Feature`：

1. 某些类型必须在 GraalVM 分析前注册，`RuntimeHints` 不够用
2. Lambda Native 兼容必须依赖更底层的构建期钩子
3. MyBatis-Plus 某些元数据注册只能通过 `Feature` 稳定实现

### 7.2 这样做的好处

1. 降低 `yunyu-server` 的接入复杂度
2. 避免业务工程理解过多 GraalVM 底层参数
3. 先走 Spring 官方推荐链路，更容易维护

## 8. 第一阶段开发顺序定稿

### 8.1 第一步

新建 `yunyu-native-image-support` 独立工程及 3 个模块骨架。

交付结果：

1. 父工程可编译
2. 3 个子模块依赖关系正确
3. starter 能被 `mvn install`

### 8.2 第二步

在 `core` 中落地最小可用的 `RuntimeHints` 与扫描能力。

交付结果：

1. 能注册基础 MyBatis-Plus 类型
2. 能注册配置文件与 SQL 资源
3. 能从启动类主包中发现 Mapper

### 8.3 第三步

在 `lambda-override` 中落地 MyBatis-Plus Lambda 兼容实现。

交付结果：

1. 编译通过
2. starter 传递依赖后可进入 `yunyu-server`
3. 为后续 Native 构建验证做好准备

### 8.4 第四步

在 `yunyu-server` 中接入 starter，并补齐 `native` profile。

交付结果：

1. `yunyu-server` 正常编译
2. `yunyu-server` 可进入 Native 构建阶段

### 8.5 第五步

执行 Native 构建，按报错迭代补齐 Hint 和兼容逻辑。

交付结果：

1. Native 二进制产出
2. 基础启动成功
3. 核心接口可验证

## 9. 第一阶段验收清单

开发完成后，第一阶段至少要验证：

1. `yunyu-native-image-support` 可以独立 `mvn clean install`
2. `yunyu-server` 可以解析 starter 依赖
3. `yunyu-server` 可以执行 `native` profile 构建
4. Native 二进制可以启动
5. `db/init/001-init-schema.sql` 可以在 Native 运行时被读取
6. 登录接口可正常工作
7. 至少一个使用 `LambdaQueryWrapper` 的查询接口可正常工作
8. 至少一个分页查询接口可正常工作

## 10. 风险处理策略

### 10.1 如果 `RuntimeHints` 不足

处理顺序建议为：

1. 先补充 `RuntimeHints`
2. 再补静态 `META-INF/native-image` 配置
3. 最后才考虑引入自定义 `--features`

### 10.2 如果 `LambdaUtils` 覆盖不稳定

处理顺序建议为：

1. 先验证同名覆盖路径是否生效
2. 若不稳定，评估是否改为显式替代实现
3. 必要时再引入更底层的构建期注册方案

### 10.3 如果 `yunyu-server` 仍出现必须改业务源码的问题

处理策略：

1. 先暂停开发
2. 回到方案边界重新评估
3. 明确是否接受突破“不改业务代码”的约束

## 11. 下一步执行建议

现在方案已经可以进入开发实现阶段。

建议下一步直接按以下顺序执行：

1. 创建 `yunyu-native-image-support` 独立工程骨架
2. 建立 `core + lambda-override + starter` 三个模块
3. 完成第一版 `pom.xml` 和 starter 自动装配
4. 在 `yunyu-server` 中接入并开始 Native 构建验证

如果没有新的边界变更，下一步就不再继续写方案，而是直接开始创建工程和改代码。
