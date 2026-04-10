# Yunyu Server Native Image 构建与真机验证说明

## 1. 文档目的

本文档用于沉淀 `main-native-image` 分支当前已经跑通的 Native Image 构建、运行与验证结论。

本文档重点回答三个问题：

1. 当前方案最终是怎么接入的
2. 当前 Native 二进制如何构建与启动
3. 当前已经真机验证通过了哪些能力

## 2. 当前最终结论

截至 `2026-04-10`，`yunyu-server` 已经可以在 **不修改 `yunyu-server/src/main/java` 任何业务代码** 的前提下，通过以下方式完成 Native Image 部署支持：

1. 引入独立工程 `yunyu-native-image-support`
2. 在 `yunyu-server/pom.xml` 中接入 `yunyu-native-image-support-starter`
3. 在 `yunyu-server` 中只调整 `pom.xml`、`application-native.yml` 和 Native 相关构建配置

当前已经完成并验证通过：

1. `yunyu-native-image-support` 可独立 `install`
2. `yunyu-server` 可成功执行 `-Pnative package`
3. Native 二进制可成功启动
4. 数据库初始化链路可正常执行
5. 登录接口可正常返回 Token
6. `LambdaQueryWrapper` 查询接口可正常返回分页数据

## 3. 当前工程结构

当前 Native 支撑保持为独立工程，不与 `yunyu-server` 做根聚合：

```text
Yunyu
├── yunyu-native-image-support
│   ├── pom.xml
│   ├── yunyu-native-image-support-core
│   ├── yunyu-native-image-support-lambda-override
│   └── yunyu-native-image-support-starter
├── yunyu-server
└── docs
```

三个模块职责如下：

1. `yunyu-native-image-support-core`
   - 提供 RuntimeHints、GraalVM Feature、类扫描、反射注册、资源注册等核心能力

2. `yunyu-native-image-support-lambda-override`
   - 覆盖 MyBatis-Plus Lambda 元信息提取逻辑
   - 解决 Native 环境下 `LambdaQueryWrapper` / `LambdaUpdateWrapper` 兼容问题

3. `yunyu-native-image-support-starter`
   - 作为 `yunyu-server` 唯一直接依赖的 starter
   - 负责自动配置与属性绑定

## 4. 当前接入方式定稿

### 4.1 `yunyu-server` 改动边界

当前方案仍然满足最初约束：

1. 不修改 `yunyu-server/src/main/java` 现有业务代码
2. 允许修改 `yunyu-server/pom.xml`
3. 允许修改 `yunyu-server/src/main/resources/application-native.yml`
4. 允许补充少量 Native 构建配置

### 4.2 Native 构建参数最终结论

本轮实际跑通后，当前最终结论如下：

必须保留的参数：

```text
--no-fallback
-Dfile.encoding=UTF-8
--features=com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeFeature
-Dyunyu.native.applicationClass=com.ideaflow.yunyu.YunyuServerApplication
```

说明：

1. `--no-fallback`
   - 用于强制暴露 Native 真问题，避免生成 fallback JVM 镜像

2. `-Dfile.encoding=UTF-8`
   - 用于统一构建期编码行为

3. `--features=com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeFeature`
   - 当前 **是需要的**
   - 作用是在 GraalVM 分析阶段补充 Spring AOT 之外仍然缺失的 MyBatis-Plus Native 元数据
   - 其中最关键的是 Lambda 捕获类、Wrapper 体系、Mapper 代理与基础设施注册

4. `-Dyunyu.native.applicationClass=...`
   - 当前 **是需要的**
   - 作用是让 `Feature` 在 Spring 容器之外也能准确拿到启动类主包，推导默认扫描范围

当前不要求手工传入的参数：

```text
-Dyunyu.native.scan-packages=...
```

原因：

1. 默认已经以启动类所在主包 `com.ideaflow.yunyu` 为根包递归扫描
2. 当前项目业务代码都在主包下，已经够用
3. 只有未来出现主包外类型需要纳入扫描时，才需要额外补充该参数或配置项

## 5. 本次关键实现结论

本次最终跑通依赖了下面几个关键实现点。

### 5.1 MyBatis-Plus Lambda 覆盖

Native 环境下，MyBatis-Plus 默认 Lambda 提取路径并不稳定，当前通过覆盖下列类解决：

- `com.baomidou.mybatisplus.core.toolkit.LambdaUtils`
- `com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda`

并在 Native 构建阶段把覆盖后的 class 解包到 `yunyu-server/target/classes`，确保最终镜像使用的是覆盖实现，而不是原始依赖中的实现。

### 5.2 启用自定义 GraalVM Feature

最终确认，单靠 RuntimeHints 不够，需要启用自定义 Feature：

- `com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeFeature`

Feature 在 GraalVM 分析阶段会驱动：

1. 扫描业务主包
2. 注册 Lambda 捕获类
3. 注册 Wrapper 相关反射信息
4. 注册 Mapper 接口与代理
5. 注册 MyBatis 基础设施与资源文件

### 5.3 默认扫描主包，而不是扫描全部 classpath

最终仍然坚持“默认扫描主包，不扫描全部 classpath”的策略。

原因：

1. 扫描全部 classpath 会让反射注册范围失控
2. 会拉入大量第三方无关类，增加镜像风险与构建成本
3. 本次实际问题也证明，扫描范围必须受控

同时在扫描器中额外排除了：

- `com.ideaflow.yunyu.nativeimage.support`

这样可以避免把仅在构建期使用的 support 类再次当成业务类注册，进而引发 Graal 构建错误。

## 6. 当前构建步骤

### 6.1 先安装 Native Support 工程

在目录：

- `/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-native-image-support`

执行：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests install
```

### 6.2 再构建 `yunyu-server` Native 二进制

在目录：

- `/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server`

执行：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests -Pnative package
```

构建成功后可得到：

- `/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server/target/yunyu-server`

## 7. 当前运行步骤

在目录：

- `/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server`

执行：

```bash
./target/yunyu-server --spring.profiles.active=native
```

当前默认监听端口：

- `20000`

启动成功后，可以看到类似日志：

1. Tomcat 监听 `20000`
2. 成功连接 `127.0.0.1:3306`
3. 检测到数据库 `yunyu` 已存在
4. 系统状态为 `READY`

## 8. 真机验证记录

本次真机验证时间：

- `2026-04-10`

### 8.1 健康检查

请求：

```bash
curl -i http://127.0.0.1:20000/actuator/health
```

结果：

1. 返回 `HTTP/1.1 200`
2. 响应体包含 `status: UP`

### 8.2 登录接口

请求：

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"account":"yunyu","password":"yunyu"}' \
  http://127.0.0.1:20000/api/auth/login
```

结果：

1. 返回 `HTTP/1.1 200`
2. 成功返回 `accessToken`
3. 返回用户信息 `userName=yunyu`、`role=SUPER_ADMIN`

### 8.3 `LambdaQueryWrapper` 查询接口

请求：

```bash
curl -i 'http://127.0.0.1:20000/api/site/posts?pageNo=1&pageSize=10'
```

结果：

1. 返回 `HTTP/1.1 200`
2. 成功返回分页数据
3. `total=12`
4. 说明 `LambdaQueryWrapper` 查询链路已经在 Native 二进制中正常工作

## 9. 当前已知注意事项

1. `yunyu-native-image-support` 仍然需要先 `install`，再由 `yunyu-server` 引用
2. 当前验证环境依赖本地 MySQL `127.0.0.1:3306`
3. 当前数据库 `yunyu` 已存在，因此本次日志表现为“跳过初始化”，这属于预期行为
4. 当前 Native 支撑已经依赖自定义 GraalVM `Feature`，后续不要再回退为“仅 RuntimeHints”心智

## 10. 后续建议

当前这套能力已经达到“可构建、可启动、可访问核心接口”的目标，后续建议按以下方向继续收尾：

1. 在 CI 中补一条 Native 构建链
2. 视需要增加更多业务接口回归用例
3. 后续如果出现主包外模块，再考虑补充 `scan-packages` 配置
