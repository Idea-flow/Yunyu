# Yunyu Server Native Image 支持方案设计

## 1. 文档目标

本文档用于评估并设计 `Yunyu` 项目在 `main-native-image` 分支上的 GraalVM Native Image 支持方案，目标是在 **不改动 `yunyu-server` 任何业务代码** 的前提下，仅通过：

- 新增 `yunyu-native-image-support-starter` 相关项目
- 调整 `yunyu-server` 的 `pom.xml`
- 调整 `yunyu-server` 的 Native 部署配置

最终让 `yunyu-server` 可以构建为 Native Image 二进制并用于部署。

本文档当前只做方案设计，不进入代码开发实现，也尚未执行 Native Image 实际构建验证。

## 2. 当前项目现状分析

### 2.1 仓库结构现状

当前 `Yunyu` 根目录下主要包含：

- `yunyu-server`：Spring Boot 后端服务，当前为独立 Maven 项目
- `yunyu-web`：前端项目
- `docs`：项目文档目录

当前仓库根目录 **还不是 Maven 聚合工程**，`yunyu-server` 自身直接使用 `spring-boot-starter-parent` 作为父工程。

### 2.2 `yunyu-server` 技术栈现状

已确认 `yunyu-server` 当前技术栈如下：

- Spring Boot `4.0.5`
- Java `25`
- MyBatis-Plus `3.5.15`
- MySQL `8` 驱动
- Spring Security + OAuth2 Resource Server + JWT
- `springdoc-openapi-starter-webmvc-ui`

### 2.3 与 Native Image 直接相关的现状

当前项目已经具备一些对 Native 部署有利的基础条件：

1. 已存在 `application-native.yml`
2. 已在 `application-native.yml` 中关闭 `springdoc` 相关配置
3. Mapper 目前均为注解/接口方式，`resources` 下没有 XML Mapper
4. 启动类清晰，主包固定为 `com.ideaflow.yunyu`
5. 业务代码大量使用 `LambdaQueryWrapper` / `LambdaUpdateWrapper`

其中第 5 点非常关键，这意味着 **MyBatis-Plus Lambda 元信息提取** 是本项目 Native Image 成功运行的核心风险点之一。

## 3. 参考 Demo 后得到的结论

参考项目：

- `/Users/wangpenglong/projects/java/mybatis-plus-native-demo/mybatis-plus-native-support-spring-boot-starter`

结合 Demo 的完整结构，实际可提炼出下面的设计模式：

1. `starter` 本身只负责自动装配，不承载全部 Native 逻辑
2. 真正的 Native 能力通常拆到 `core` 模块
3. MyBatis-Plus Lambda Native 兼容问题，可能需要单独的 `lambda-override` 模块
4. 业务项目只需要：
   - 引入 starter
   - 增加 native profile
   - 增加少量 native 配置
5. Native 构建参数通过 `native-maven-plugin` 的 `buildArgs` 注入

这套思路与本项目诉求高度匹配，因此 **整体方向可行**。

## 4. 可行性结论

### 4.1 结论

**可行。**

但这里的“可行”不是指“只加一个 starter 依赖就一定一次成功”，而是指：

- 通过新增一个专门的 Native 支撑工程
- 在 `yunyu-server` 中只修改 `pom.xml` 和配置文件
- 通过 RuntimeHints、GraalVM Feature、资源注册、MyBatis-Plus Lambda 兼容处理

可以把 Native Image 所需的大部分兼容逻辑从业务工程中抽离出去。

### 4.2 需要明确的边界

为了满足“`yunyu-server` 不改动任何一行业务代码”的目标，本方案默认约束如下：

- 不修改 `yunyu-server/src/main/java` 下现有业务类
- 不修改 `yunyu-server/src/test/java` 下现有测试类
- 允许修改：
  - `yunyu-server/pom.xml`
  - `yunyu-server/src/main/resources/application-native.yml`
  - 如有必要，可补充其他 Native 专用配置文件到 `resources`

### 4.3 当前判断下最有把握的方向

最稳妥的设计不是只做一个“空 starter”，而是提供一个独立的 Native 支撑工程，对外交付 `yunyu-native-image-support-starter`，内部按职责拆分为：

- `yunyu-native-image-support-core`
- `yunyu-native-image-support-lambda-override`
- `yunyu-native-image-support-starter`

也就是说：

- **对 `yunyu-server` 而言，只引用 starter 成品依赖**
- **对实现层而言，建议按 3 个 Maven 模块拆分**
- **对仓库结构而言，不要求与 `yunyu-server` 做根聚合**

这样既满足你的使用诉求，也更接近已验证 demo 的成功模式。

## 5. 推荐的落地结构

### 5.1 目录结构建议

本期按你的要求，建议采用“独立 Native 支撑工程 + `yunyu-server` 引用已安装依赖”的结构，而不是根目录聚合：

```text
Yunyu
├── yunyu-native-image-support
│   ├── pom.xml                               # 独立父工程 / 聚合工程
│   ├── yunyu-native-image-support-core
│   ├── yunyu-native-image-support-lambda-override
│   └── yunyu-native-image-support-starter
├── yunyu-server
├── yunyu-web
└── docs
```

说明：

- `yunyu-native-image-support` 是一个独立 Maven 工程目录，内部再拆分为 3 个模块
- `yunyu-server` 不并入该工程，也不作为其子模块
- `yunyu-server` 继续保留当前 `spring-boot-starter-parent` 父工程关系
- `yunyu-server` 通过正常 Maven 依赖方式引用已经 `install` 的 starter 成品

### 5.2 为什么本期不加根聚合 `pom.xml`

本期按你的要求，**不在仓库根目录新增聚合 `pom.xml`**，原因和边界如下：

1. `yunyu-native-image-support` 需要保持为独立工程，避免与 `yunyu-server` 工程结构耦合
2. 允许先单独 `install`，再由 `yunyu-server` 通过依赖方式接入
3. 这样更符合“可复用 starter 独立演进”的目标

对应的构建链会调整为：

1. 先在 `yunyu-native-image-support` 目录下执行 `mvn clean install`
2. 再在 `yunyu-server` 目录下执行 native 构建

这种方式的代价是联调步骤会比根聚合略多，但当前阶段是可以接受的。

后续如果将来需要统一 CI/CD，再考虑是否把它升级为更大的聚合工程。

## 6. 各模块职责设计

### 6.1 `yunyu-native-image-support-core`

该模块负责 Native 兼容的核心实现，不直接给业务项目使用。

建议承担职责：

1. 注册 Spring `RuntimeHints`
2. 注册 MyBatis / MyBatis-Plus 相关反射提示
3. 注册 MyBatis 插件链相关的 JDK 动态代理提示
4. 注册 Native 运行所需资源文件
5. 扫描应用主包，补齐实体、Mapper、Lambda 捕获类等元数据
6. 提供 GraalVM `Feature` 入口类

建议重点覆盖的内容：

- MyBatis-Plus `Wrapper` 体系
- `PaginationInnerInterceptor`
- `SqlSessionFactory` 相关类型
- `BaseMapper` 子接口
- 实体类
- `application*.yml`
- `db/init/*.sql`

### 6.2 `yunyu-native-image-support-lambda-override`

该模块用于解决 MyBatis-Plus Lambda 在 Native 环境下的兼容问题。

建议职责：

1. 参考 demo 中的 `LambdaUtils` 覆盖方案
2. 优先尝试 `writeReplace + MethodHandle` 方式提取 Lambda 元信息
3. 降低对默认反序列化路径的依赖

该模块是本方案中风险最高、但很可能也是必须存在的模块。

原因是 `yunyu-server` 当前大量使用：

- `LambdaQueryWrapper`
- `LambdaUpdateWrapper`
- 方法引用式字段提取

如果没有这层兼容处理，Native 运行期极可能在条件构造、列名提取时失败。

### 6.3 `yunyu-native-image-support-starter`

这是业务工程唯一直接引用的模块。

建议职责：

1. 提供 `AutoConfiguration`
2. 自动导入 `core` 模块中的 RuntimeHints 能力
3. 对外暴露少量可配置项
4. 保持“业务项目零侵入接入”

建议暴露配置前缀：

```yaml
yunyu:
  native:
    enabled: true
    scan-packages:
      - com.ideaflow.yunyu.module
    register-mappers: false
```

说明：

- `scan-packages` 用于补充扫描包
- `register-mappers` 默认建议关闭
- 当前 `yunyu-server` 已有自己的 `@MapperScan`，starter 不应默认强接管

## 7. `yunyu-server` 侧允许的改动设计

在你的约束下，`yunyu-server` 侧只做以下改动。

### 7.1 `pom.xml` 改动

需要增加：

1. 引用 `yunyu-native-image-support-starter`
2. 增加 `native` profile
3. 在 `native` profile 中配置 `org.graalvm.buildtools:native-maven-plugin`

建议的 Native 构建参数方向需要分成“必须”、“推荐”和“按需”三类看待：

```text
--no-fallback
-Dfile.encoding=UTF-8
--features=xxx.YunyuNativeRuntimeFeature
```

说明如下：

1. `--no-fallback`
   - 建议保留
   - 作用是禁止生成 fallback JVM 镜像，强制我们在构建期把 Native 问题暴露干净
   - 对做正式 Native 支撑来说，这个参数基本有必要

2. `-Dfile.encoding=UTF-8`
   - 建议保留，但不是绝对必须
   - 主要是防止 SQL 脚本、YAML、中文内容在不同构建机上出现编码差异

3. `scan-packages`
   - **不建议第一版放在 buildArgs 里强制要求**
   - 默认应该以应用主类所在包 `com.ideaflow.yunyu` 作为根包递归扫描
   - 只有当 Mapper、实体或其他需要注册的类型放在主包之外时，才需要额外配置
   - 更合理的放置位置是 starter 的配置项，例如 `application-native.yml`

4. `--features=xxx.YunyuNativeRuntimeFeature`
   - **不是第一版必须**
   - 只有当我们最终采用“自定义 GraalVM Feature 做构建期注册”方案时才需要
   - 如果第一版能够仅通过 Spring AOT + `RuntimeHints` + starter 自动配置完成，就应尽量不暴露这个参数给 `yunyu-server`

因此，本方案修正后更推荐的方向是：

- `scan-packages` 默认扫描启动类主包，不强制写 buildArgs
- 扩展扫描包通过 starter 配置处理，而不是通过命令行强耦合
- `--features` 作为预备方案保留，不默认纳入第一版最小接入面

### 7.2 `application-native.yml` 改动

当前文件已经有一版原始内容，方案上建议继续保留并增强：

1. 继续关闭 `springdoc`
2. 增加 starter 的 Native 开关与扫描包配置
3. 明确日志、监控、资源加载相关配置

建议新增或调整的配置方向：

```yaml
yunyu:
  native:
    enabled: true
    scan-main-package: true
    scan-packages: []
```

说明：

- `scan-main-package: true` 表示默认扫描启动类主包
- `scan-packages` 默认为空，仅在存在主包之外的补充扫描需求时才配置

### 7.3 不改动业务源码

本方案默认不修改以下已有类：

- `YunyuServerApplication`
- `MybatisPlusConfig`
- `DataSourceBootstrapConfig`
- 各模块的 `service / mapper / controller / entity / dto / vo`

如果后续开发阶段发现必须改动 Java 源码，说明方案没有完全满足初始目标，需要回到这里重新评估。

## 8. Native 兼容重点分析

### 8.1 MyBatis-Plus 是第一优先级风险

原因：

1. 项目大量使用 Lambda Wrapper
2. MyBatis-Plus 内部存在较多反射、代理、序列化相关逻辑
3. Native 环境下 `SerializedLambda`、代理类和元数据注册都容易出问题

结论：

- **必须把 MyBatis-Plus 作为 Native 支撑方案的核心对象**
- 不能只依赖 Spring Boot 默认 AOT 能力

### 8.2 当前没有 XML Mapper，这是利好

当前 `yunyu-server/resources` 下未发现 MyBatis XML Mapper 文件，因此相较 demo：

- 不必优先实现 XML Mapper 资源自动补偿
- starter 第一阶段可不接管 XML 映射逻辑
- 方案可以更聚焦在注解 Mapper、实体反射、Lambda 兼容

这会显著降低第一版实现复杂度。

### 8.3 `springdoc` 需要继续在 Native Profile 下关闭

当前 `application-native.yml` 已经通过 `spring.autoconfigure.exclude` 关闭了多个 `springdoc` 自动配置项，这个方向是正确的。

原因：

- `springdoc` 对 Native 支持通常不是最稳定链路
- 对部署型二进制而言，Swagger UI 不是运行必需能力

因此建议保持：

- JVM 模式可用 Swagger
- Native 模式默认关闭 Swagger

### 8.4 资源文件需要显式纳入 Native

当前明显需要纳入 Native Image 的资源包括：

- `application.yml`
- `application-dev.yml`
- `application-prod.yml`
- `application-native.yml`
- `db/init/init.sql`
- `db/init/002-seed-demo-data.sql`

尤其是 `DatabaseBootstrapService` 通过 `ClassPathResource` 读取 SQL 脚本，这部分必须由 starter/core 的 RuntimeHints 显式注册资源。

### 8.5 Jackson 与 Security 大概率可以先借助 Spring AOT

当前项目虽然使用了：

- 自定义 `ObjectMapper`
- JWT 编解码
- Security 过滤链

但从 Spring Boot 4 + Spring Framework AOT 的能力看，这一部分大概率可以先依赖 Spring 原生支持，不作为第一阶段的重点改造对象。

不过需要在开发验证阶段重点测试：

1. 登录接口
2. JWT 鉴权接口
3. JSON 序列化包含时间、列表、嵌套对象的接口

## 9. 实施方案建议

### 9.1 第一阶段：基础骨架

目标：

- 建立 `yunyu-native-image-support-*` 模块
- 建立独立的 `yunyu-native-image-support/pom.xml`
- 让 `yunyu-server` 可以编译进入 Native 构建流程

本阶段不追求一次成功产出可运行二进制，先打通工程结构。

### 9.2 第二阶段：MyBatis-Plus Native 支撑

目标：

- 完成 MyBatis/MyBatis-Plus RuntimeHints
- 完成实体与 Mapper 扫描
- 完成 Lambda 兼容处理
- 解决构建期缺少反射提示的问题

### 9.3 第三阶段：资源与运行验证

目标：

- 纳入 SQL 脚本和配置资源
- 构建 Native 二进制
- 启动后验证数据库初始化、登录、基础查询、分页查询

### 9.4 第四阶段：部署收口

目标：

- 输出 Native 构建命令
- 输出二进制启动命令
- 输出部署说明

## 10. 验收标准建议

如果进入开发阶段，建议把验收标准定为：

1. `yunyu-server` 业务 Java 源码零改动
2. `yunyu-server` 只修改 `pom.xml` 与 Native 配置文件
3. 可以成功产出 Native Image 二进制
4. 二进制可以在指定环境下启动
5. 健康检查接口可访问
6. 登录接口可访问
7. 至少一个分页查询接口可访问
8. 至少一个使用 `LambdaQueryWrapper` 的接口可访问
9. 初始化 SQL 脚本可被 Native 二进制正确读取

## 11. 主要风险与注意事项

### 11.1 最大风险：`LambdaUtils` 覆盖策略是否稳定

参考 demo 的 `lambda-override` 方案，本质上是在兼容 MyBatis-Plus Lambda 的 Native 行为。

这里需要特别注意：

- 该方案可能依赖类加载顺序
- 与原始 `mybatis-plus-core` 中同名类并存时，需要验证实际覆盖效果
- 在 Spring Boot 可执行包和 Native 构建链下都要验证

因此这部分建议在开发阶段尽早做最小 POC。

### 11.2 独立 Native 支撑工程会带来一次额外安装步骤

虽然不改业务代码，但这会引入一个额外的本地依赖安装流程。

好处是：

- 后续 Native 支撑模块能长期维护
- 与 `yunyu-server` 工程结构解耦
- 更接近可复用 starter 的独立交付方式

代价是：

- 需要先构建并安装 `yunyu-native-image-support`
- `yunyu-server` 的联调步骤会多一步

### 11.3 Java 25 与 GraalVM 版本需要统一验证

当前项目使用 Java `25`，后续实现时需要统一验证：

- 构建机 JDK 版本
- GraalVM Native Image 版本
- Spring Boot 4.0.5 与当前依赖组合的兼容性

这里建议直接参考 demo 的版本基线，优先保持一致。

## 12. 最终推荐结论

我的推荐是：

1. **方案继续推进**
2. **采用“对外一个 starter，内部三个模块”的结构**
3. **采用独立 Native 支撑工程，不与 `yunyu-server` 做根聚合**
4. **`yunyu-server` 只改 `pom.xml` 和 Native 配置**
5. **优先解决 MyBatis-Plus Lambda Native 兼容问题**

## 13. 建议你重点确认的决策点

在进入开发前，建议你确认下面几项：

1. 是否接受在仓库根目录新增聚合 `pom.xml`
2. 是否接受内部拆分为 `core + lambda-override + starter` 三个模块
3. `yunyu-server` 是否允许除了 `pom.xml` 和 `application-native.yml` 之外，再新增少量 Native 专用 `resources` 文件
4. Native 模式下是否确认继续关闭 Swagger / OpenAPI

本次你已经明确的决策是：

1. 本期不在仓库根目录新增聚合 `pom.xml`
2. Native 支撑工程与 `yunyu-server` 保持解耦
3. 先 `install` starter，再由 `yunyu-server` 引用

剩余只需要在进入开发前继续确认：

1. 是否接受内部拆分为 `core + lambda-override + starter` 三个模块
2. `yunyu-server` 是否允许除了 `pom.xml` 和 `application-native.yml` 之外，再新增少量 Native 专用 `resources` 文件
3. Native 模式下是否确认继续关闭 Swagger / OpenAPI

## 14. 本次方案的判断结论

结论再次明确：

**本方案可行，且推荐继续推进。**

但最关键的技术抓手不是“加一个 starter 名字”，而是：

- Maven 工程结构设计
- MyBatis-Plus Native 元数据注册
- Lambda Native 兼容处理
- 资源文件纳入 Native Image

只要这几件事设计正确，`yunyu-server` 在不改业务代码的前提下实现 Native Image 部署，是有较高把握落地的。
