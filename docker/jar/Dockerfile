# syntax=docker/dockerfile:1.7

# JAR 构建阶段：先安装当前仓库内的 Native Support 依赖，再构建 `yunyu-server` 的 JVM JAR 包。
FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /workspace

# 仅复制 JAR 构建所需源码目录，避免把无关工程带入构建上下文。
COPY yunyu-native-image-support ./yunyu-native-image-support
COPY yunyu-server ./yunyu-server

# 先 install 本仓库内的 support 工程，再构建 `yunyu-server`。
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -f yunyu-native-image-support/pom.xml -DskipTests install && \
    mvn -B -f yunyu-server/pom.xml -DskipTests clean package

# JVM 运行阶段：只保留最终 JAR 与运行时 JRE。
FROM eclipse-temurin:25-jre

WORKDIR /app

# 复制构建好的 Spring Boot 可执行 JAR。
COPY --from=builder /workspace/yunyu-server/target/yunyu-server-0.0.1-SNAPSHOT.jar /app/yunyu-server.jar

EXPOSE 20000

# JVM 镜像默认使用 `prod` profile 启动。
ENTRYPOINT ["java", "-Xms128m", "-Xmx1g", "-Dspring.profiles.active=prod", "-jar", "/app/yunyu-server.jar"]
