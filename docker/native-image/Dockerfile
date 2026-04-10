# syntax=docker/dockerfile:1.7

# Native 构建阶段：直接在目标平台容器内构建 `yunyu-native-image-support` 与 `yunyu-server`，
# 这样无需把 starter 发布到外部 Maven 仓库，也能产出对应架构的 Native 二进制。
FROM ghcr.io/graalvm/native-image-community:25 AS builder

# 安装 Maven 与 Native 编译依赖：用于在容器内完成 Maven 构建以及 GraalVM 的本地链接步骤。
RUN microdnf install -y maven findutils tar gzip gcc binutils glibc-devel zlib-devel && microdnf clean all

WORKDIR /workspace

# 仅复制 Native 构建所需源码目录，减少构建上下文并避免把无关工程带入镜像。
COPY yunyu-native-image-support ./yunyu-native-image-support
COPY yunyu-server ./yunyu-server

# 先安装 Native Support，再构建 `yunyu-server` Native 二进制。
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f yunyu-native-image-support/pom.xml -DskipTests install && \
    mvn -B -f yunyu-server/pom.xml -DskipTests -Pnative package

# 运行阶段：只保留 Native 可执行文件和最小运行依赖，减小最终镜像体积。
FROM debian:bookworm-slim AS runtime

# 安装常见运行依赖：证书、时区与 Native 二进制常见依赖库。
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates tzdata libstdc++6 zlib1g && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制 Native 可执行文件作为镜像最终产物。
COPY --from=builder /workspace/yunyu-server/target/yunyu-server /app/yunyu-server

RUN chmod +x /app/yunyu-server

# 运行时默认同时启用 `prod` 与 `native` 配置：
# `prod` 提供正式环境配置语义，`native` 提供 Native 专用兼容配置。
ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod,native

EXPOSE 20000

# Native 镜像直接启动二进制，不再经过 `java -jar`。
ENTRYPOINT ["/app/yunyu-server"]
