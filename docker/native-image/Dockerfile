# syntax=docker/dockerfile:1.7

# Maven 提供阶段：直接复用官方 Maven 镜像中的 Maven 安装目录，避免在 GraalVM 镜像内再走系统包管理器。
FROM maven:3.9.11-eclipse-temurin-25 AS maven

# Native 构建阶段：直接在目标平台容器内构建 `yunyu-native-image-support` 与 `yunyu-server`，
# 这样无需把 starter 发布到外部 Maven 仓库，也能产出对应架构的 Native 二进制。
FROM ghcr.io/graalvm/native-image-community:25 AS builder

# 复制 Maven 运行目录：避免依赖 GraalVM 基础镜像中的 `microdnf` 或系统仓库状态。
COPY --from=maven /usr/share/maven /usr/share/maven

# 复制 `mvn` 启动脚本：让当前 Builder 镜像可以直接执行 Maven 命令。
COPY --from=maven /usr/bin/mvn /usr/bin/mvn

# 配置 Maven 环境变量：确保后续 `mvn` 能正确找到自身目录。
ENV MAVEN_HOME=/usr/share/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

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
