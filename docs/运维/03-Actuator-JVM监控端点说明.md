# Actuator JVM 监控端点说明

## 文档目的

本文档用于说明 Yunyu 后端当前开放的 Actuator JVM 监控端点、访问权限、调用方式以及典型使用场景，便于开发与运维快速查看运行时 JVM 信息。

## 一、当前开放的 JVM 相关端点

当前项目在 `dev` 与 `prod` 环境中开放了以下 Actuator 端点：

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/metrics/jvm.memory.used`
- `/actuator/metrics/jvm.memory.max`
- `/actuator/metrics/jvm.threads.live`
- `/actuator/metrics/system.cpu.usage`
- `/actuator/threaddump`

其中：

- `health` 用于基础存活检查。
- `info` 用于查看补充后的 JVM 基础信息。
- `metrics` 用于查看 Micrometer 暴露的 JVM 与系统指标。
- `threaddump` 用于查看当前线程栈快照。

## 二、访问权限说明

### 1. 无需登录即可访问

- `/actuator/health`

该接口保留为公开访问，适合负载均衡、容器探针和部署健康检查。

### 2. 需要管理员权限访问

以下 `/actuator/**` 端点需要使用 `SUPER_ADMIN` 角色的登录令牌访问：

- `/actuator/info`
- `/actuator/metrics`
- `/actuator/metrics/**`
- `/actuator/threaddump`

这样做的原因是 JVM 与系统指标属于运维敏感信息，不适合对匿名用户开放。

## 三、如何获取访问令牌

### 1. 调用登录接口

接口地址：

```text
POST /api/auth/login
```

请求示例：

```bash
curl -X POST 'http://127.0.0.1:20000/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "admin@example.com",
    "password": "123456"
  }'
```

响应中的 `data.accessToken` 就是后续访问 Actuator 管理端点所需的 Bearer Token。

### 2. 取出 Token

登录成功后，响应结构类似：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userId": 1,
    "email": "admin@example.com",
    "userName": "admin",
    "role": "SUPER_ADMIN"
  }
}
```

后续请求头中加入：

```text
Authorization: Bearer <accessToken>
```

## 四、如何调用各个接口

### 1. 查看健康状态

```bash
curl 'http://127.0.0.1:20000/actuator/health'
```

这个接口通常返回：

```json
{
  "status": "UP"
}
```

### 2. 查看 JVM 基础信息

```bash
curl 'http://127.0.0.1:20000/actuator/info' \
  -H 'Authorization: Bearer <accessToken>'
```

返回结果中会包含 `jvm` 节点，典型字段包括：

- `javaVersion`
- `javaVendor`
- `vmName`
- `vmVendor`
- `vmVersion`
- `inputArguments`
- `startTime`
- `uptimeMs`
- `availableProcessors`
- `maxMemory`
- `totalMemory`
- `freeMemory`
- `defaultTimeZone`

这个接口适合快速确认：

- 当前 Java 版本是否正确
- JVM 启动参数是否已生效
- 进程运行时长
- 当前进程可见 CPU 数量
- JVM 当前内存基线

### 3. 查看所有可用指标名称

```bash
curl 'http://127.0.0.1:20000/actuator/metrics' \
  -H 'Authorization: Bearer <accessToken>'
```

这个接口会返回当前项目可查询的指标名列表。

### 4. 查看 JVM 已使用内存

```bash
curl 'http://127.0.0.1:20000/actuator/metrics/jvm.memory.used' \
  -H 'Authorization: Bearer <accessToken>'
```

你可以从返回结果里看到：

- 指标名
- 可用 tag
- 各个内存区的测量值

常见 tag 包括：

- `area=heap`
- `area=nonheap`
- `id=G1 Eden Space`
- `id=Metaspace`

### 5. 查看 JVM 最大可用内存

```bash
curl 'http://127.0.0.1:20000/actuator/metrics/jvm.memory.max' \
  -H 'Authorization: Bearer <accessToken>'
```

该接口适合配合 `jvm.memory.used` 对比当前使用量和理论上限。

### 6. 查看活动线程数

```bash
curl 'http://127.0.0.1:20000/actuator/metrics/jvm.threads.live' \
  -H 'Authorization: Bearer <accessToken>'
```

该接口适合观察：

- Tomcat 工作线程是否持续升高
- 是否有线程泄漏迹象
- 压测期间线程数增长是否异常

### 7. 查看 CPU 使用率

```bash
curl 'http://127.0.0.1:20000/actuator/metrics/system.cpu.usage' \
  -H 'Authorization: Bearer <accessToken>'
```

该接口适合判断高内存是否伴随着高 CPU。

### 8. 查看线程栈快照

```bash
curl 'http://127.0.0.1:20000/actuator/threaddump' \
  -H 'Authorization: Bearer <accessToken>'
```

这个接口适合排查：

- 请求卡死
- 死锁
- 某类线程长时间阻塞
- 数据库调用堆栈异常

## 五、典型排查方法

### 1. 判断 300MB 内存是否正常

先按下面顺序看：

1. 调 `/actuator/info` 看 `maxMemory`、`totalMemory`、`freeMemory`
2. 调 `/actuator/metrics/jvm.memory.used` 看 heap 和 non-heap 占用
3. 调 `/actuator/metrics/jvm.threads.live` 看线程数量
4. 调 `/actuator/metrics/system.cpu.usage` 看 CPU 是否也异常偏高

如果：

- `heap` 不高
- `non-heap` 较高
- 线程数也不多

那更可能是 Spring Boot、Tomcat、类元数据、JIT、驱动和框架基线开销，而不一定是业务代码异常。

### 2. 判断 JVM 参数是否生效

调用：

```bash
curl 'http://127.0.0.1:20000/actuator/info' \
  -H 'Authorization: Bearer <accessToken>'
```

查看 `jvm.inputArguments`，确认类似以下参数是否存在：

- `-Xlog:os+container=info`
- `-Xlog:gc*=info:file=/tmp/jvm-gc.log:time,level,tags`
- `-XX:NativeMemoryTracking=summary`

## 六、注意事项

- `/actuator/threaddump` 返回内容较多，不建议高频调用。
- `/actuator/metrics` 中的值会随着运行状态实时变化，建议结合压测或线上观测一起看。
- `/actuator/info` 已补充 JVM 信息，但它更适合看基线和参数，不适合代替完整内存剖析。
- 如果后续需要更深层次排查堆外内存，仍建议结合 `jcmd` 与 GC 日志一起分析。

## 七、相关配置位置

本次能力涉及以下配置与代码：

- `yunyu-server/src/main/resources/application-dev.yml`
- `yunyu-server/src/main/resources/application-prod.yml`
- `yunyu-server/src/main/java/com/ideaflow/yunyu/security/SecurityConfiguration.java`
- `yunyu-server/src/main/java/com/ideaflow/yunyu/infrastructure/actuator/JvmInfoContributor.java`
