# Spring Boot 内存与 JVM 诊断说明

## 文档目的

本文档用于说明 Yunyu 后端在生产环境中如何关闭 Swagger，以及如何通过 JVM 诊断参数观察 Spring Boot 服务的实际内存占用情况，避免仅凭 `docker stats` 的粗略数字做误判。

## 一、生产环境关闭 Swagger

### 1. 配置位置

生产环境配置文件位于：

- `yunyu-server/src/main/resources/application-prod.yml`

### 2. 推荐配置

在 `prod` 配置中关闭 OpenAPI 文档接口和 Swagger UI：

```yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

### 3. 说明

- `api-docs.enabled: false` 表示关闭 `/v3/api-docs` 等 OpenAPI 文档接口。
- `swagger-ui.enabled: false` 表示关闭 `/swagger-ui.html` 与 `/swagger-ui/**` 页面资源。
- 关闭后可以减少生产环境中的接口元数据扫描、Swagger 页面资源加载与相关 Bean 初始化开销。
- 对线上环境来说，这通常是一个低风险、收益明确的优化项。

## 二、为什么需要 JVM 诊断参数

很多时候看到容器占用 `300MB`，并不代表 Java 堆真的用了 `300MB`。

一个 Spring Boot 进程的内存通常由下面几部分组成：

- Java Heap：Java 对象主要存放区域。
- Metaspace：类元数据区域。
- Thread Stack：线程栈内存。
- Code Cache：JIT 编译后的代码缓存。
- Direct Memory / Native Memory：NIO、驱动、JVM 本地分配内存等。

所以我们需要区分：

- 容器总占用有多少
- Java 堆实际用了多少
- 元空间、线程、本地内存分别占了多少

## 三、推荐的 JVM 诊断参数

### 1. 启动参数示例

如果你使用 Docker 启动后端，可以先加上这一组诊断参数：

```dockerfile
ENTRYPOINT ["java", \
  "-Dspring.profiles.active=prod", \
  "-Xlog:os+container=info", \
  "-Xlog:gc*=info:file=/tmp/jvm-gc.log:time,level,tags", \
  "-XX:+UnlockDiagnosticVMOptions", \
  "-XX:NativeMemoryTracking=summary", \
  "-jar", \
  "/app/yunyu-server.jar"]
```

### 2. 每个参数的作用

- `-Xlog:os+container=info`
  用于输出 JVM 对容器 CPU、内存限制的识别结果，确认 JVM 是否正确识别 Docker 的资源上限。

- `-Xlog:gc*=info:file=/tmp/jvm-gc.log:time,level,tags`
  将 GC 日志输出到 `/tmp/jvm-gc.log`，用于观察垃圾回收频率、停顿时间和堆变化趋势。

- `-XX:+UnlockDiagnosticVMOptions`
  开启 JVM 的诊断能力，供一些高级诊断参数使用。

- `-XX:NativeMemoryTracking=summary`
  开启本地内存跟踪，便于后续使用 `jcmd` 查看 JVM 堆外、本地内存、线程、元空间等占用情况。

## 四、启动后如何查看

### 1. 先查看容器日志

如果是 Docker 容器：

```bash
docker logs <容器名或容器ID>
```

重点关注包含下面含义的日志：

- JVM 识别到的容器内存上限
- JVM 识别到的 CPU 核数
- 是否启用了容器感知

如果你看到了 `Memory Limit is:`、`active_processor_count` 之类的信息，就说明 `-Xlog:os+container=info` 已经生效。

### 2. 查看 GC 日志

进入容器：

```bash
docker exec -it <容器名或容器ID> sh
```

查看 GC 日志：

```bash
tail -f /tmp/jvm-gc.log
```

重点看这些现象：

- 服务空载时是否频繁 GC
- Full GC 是否出现
- Young GC 是否过于频繁
- GC 后堆是否持续增长不回落

### 3. 查看 Java 进程 PID

进入容器后执行：

```bash
jcmd
```

通常会看到类似输出：

```text
1 /app/yunyu-server.jar
```

这里的 `1` 就是 Java 进程 PID。

### 4. 查看 JVM 堆信息

```bash
jcmd 1 GC.heap_info
```

这个命令可以看到：

- 当前堆大小
- 新生代与老年代分布
- 已使用与可用空间

### 5. 查看本地内存摘要

```bash
jcmd 1 VM.native_memory summary
```

重点关注这些部分：

- `Java Heap`
- `Class`
- `Thread`
- `Code`
- `GC`
- `Internal`
- `Other`

这一步很关键，因为它能告诉你：

- 是堆太大
- 还是线程太多
- 还是元空间偏大
- 还是堆外内存偏大

## 五、如何理解查看结果

### 1. 如果 `docker stats` 很高，但 `Java Heap` 不高

这通常说明问题不在堆，而更可能在：

- 线程栈
- 元空间
- Tomcat / Hikari / 驱动的本地开销
- JVM 代码缓存

### 2. 如果 GC 非常频繁

这通常说明：

- 当前堆偏小
- 请求分配对象过多
- 有些接口返回数据量偏大

这时不应该盲目继续压低内存，而应该结合接口访问模式分析。

### 3. 如果空载时内存已经很高

这通常要优先排查：

- `springdoc/swagger-ui` 是否开启
- 连接池是否默认开得过大
- 是否存在不必要的自动配置
- 是否有重复依赖，例如多套 JSON 库同时存在

## 六、Docker 中的常用排查命令

### 1. 查看容器整体资源占用

```bash
docker stats
```

### 2. 查看 Java 进程

```bash
docker exec -it <容器名或容器ID> jcmd
```

### 3. 查看堆信息

```bash
docker exec -it <容器名或容器ID> jcmd 1 GC.heap_info
```

### 4. 查看本地内存摘要

```bash
docker exec -it <容器名或容器ID> jcmd 1 VM.native_memory summary
```

### 5. 查看 GC 日志

```bash
docker exec -it <容器名或容器ID> tail -n 200 /tmp/jvm-gc.log
```

## 七、当前项目的建议排查顺序

针对 Yunyu 当前后端，建议按下面顺序排查：

1. 先关闭 `prod` 下的 Swagger。
2. 重新部署后观察 `docker stats` 是否下降。
3. 增加 JVM 诊断参数并收集 `GC.heap_info` 与 `VM.native_memory summary`。
4. 判断 300MB 主要来自堆、线程、元空间还是本地内存。
5. 再决定是否需要继续做连接池优化、依赖裁剪或 `native-image` 改造。

## 八、补充说明

- `docker stats` 只能看总量，不能直接判断堆是否过大。
- JVM 诊断参数建议先用于排查环境，不建议一开始就同时叠加大量性能优化参数。
- 如果后续要做 `native-image`，建议先把当前 JVM 模式下的内存结构看清楚，这样更容易评估改造收益。
