# Native Image 部署方案

## 文档目标

本文档用于在不改变当前 JVM 部署流程的前提下，为 `Yunyu` 后端新增一套独立的 `GraalVM Native Image` 打包与部署方案。

当前 native 方案的定位是：

- 保留现有 `Dockerfile` 和现有 JVM 镜像流程不动
- 新增 `native` 构建入口
- 新增 `Dockerfile.native`
- 新增 Native 镜像工作流
- 将 native 环境中的非必要功能尽量下掉，优先追求更低运行内存和更快启动速度

## 一、当前仓库已补充的 native 文件

当前仓库新增了以下 native 相关文件：

- `yunyu-server/src/main/resources/application-native.yml`
- `yunyu-server/Dockerfile.native`
- `.github/workflows/build-yunyu-server-native-image.yml`
- `yunyu-server/pom.xml` 中的 `native` profile

## 二、native 环境中主动下掉的功能

为了优先追求小内存和更轻运行时，`application-native.yml` 中默认做了这些收缩：

- 关闭 `springdoc api-docs`
- 关闭 `swagger-ui`
- 关闭虚拟线程
- 只保留 `actuator health,info`

这样做的目的是：

- 降低扫描和元数据加载成本
- 降低 native 兼容风险
- 保持运行镜像更轻

## 三、方式一：本机直接生成原生可执行文件

### 1. 适用场景

适合你在本地或构建机先验证 native 能不能跑通。

### 2. 前置条件

需要本机具备：

- GraalVM JDK 25
- `native-image` 组件
- Maven 可用

### 3. 构建命令

进入后端目录：

```bash
cd yunyu-server
```

执行：

```bash
./mvnw -Pnative -DskipTests native:compile
```

构建成功后，产物通常位于：

```bash
target/yunyu-server
```

### 4. 启动方式

```bash
./target/yunyu-server --spring.profiles.active=native
```

## 四、方式二：使用 Spring Boot Buildpacks 直接打原生镜像

### 1. 适用场景

适合本地或 CI 中直接产出 OCI 镜像，而不手写复杂 Docker 构建逻辑。

### 2. 构建命令

```bash
cd yunyu-server
./mvnw -Pnative -DskipTests spring-boot:build-image \
  -Dspring-boot.build-image.imageName=yunyu-server-native:latest
```

### 3. 产物说明

构建完成后，本地 Docker 中会直接出现镜像：

```bash
yunyu-server-native:latest
```

### 4. 启动示例

```bash
docker run -d \
  --name yunyu-server-native \
  -p 20000:20000 \
  -e SPRING_PROFILES_ACTIVE=native \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://yunyu-mysql:3306/yunyu?useUnicode=true\&characterEncoding=utf8\&serverTimezone=Asia/Shanghai \
  -e SPRING_DATASOURCE_USERNAME=yunyu \
  -e SPRING_DATASOURCE_PASSWORD=yunyu123456 \
  -e YUNYU_JWT_SECRET=请替换成正式密钥 \
  yunyu-server-native:latest
```

## 五、方式三：使用 Dockerfile.native 多阶段构建原生镜像

### 1. 适用场景

适合你希望完全掌控 Docker 打包过程，或在 CI 中复用标准 Docker 镜像构建流程。

### 2. 构建命令

项目根目录执行：

```bash
docker build -f yunyu-server/Dockerfile.native -t yunyu-server-native:latest yunyu-server
```

### 3. 运行命令

```bash
docker run -d \
  --name yunyu-server-native \
  -p 20000:20000 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://yunyu-mysql:3306/yunyu?useUnicode=true\&characterEncoding=utf8\&serverTimezone=Asia/Shanghai \
  -e SPRING_DATASOURCE_USERNAME=yunyu \
  -e SPRING_DATASOURCE_PASSWORD=yunyu123456 \
  -e YUNYU_JWT_SECRET=请替换成正式密钥 \
  yunyu-server-native:latest
```

说明：

- `Dockerfile.native` 已固定使用 `native` profile 启动
- 容器入口会执行：

```bash
/app/yunyu-server --spring.profiles.active=native
```

## 六、方式四：GitHub Actions 自动构建 native 镜像

仓库新增了：

- `.github/workflows/build-yunyu-server-native-image.yml`

该工作流会：

- 监听 `yunyu-server/**` 变更
- 使用 `Dockerfile.native` 构建 native 镜像
- 推送到 `ghcr.io/<owner>/yunyu-server-native`

镜像命名示例：

- `ghcr.io/<github-owner>/yunyu-server-native:latest`
- `ghcr.io/<github-owner>/yunyu-server-native:sha-xxxxxxx`

## 七、服务器部署建议

### 方案 A：保留当前 JVM 部署不动，只新增 native 版本容器

适合并行验证。

建议：

- 当前 JVM 服务继续跑在 `20000`
- native 服务先跑在 `20001`
- 通过反向代理或临时端口验证内存、启动时间和接口兼容性

示例：

```bash
docker run -d \
  --name yunyu-server-native \
  -p 20001:20000 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://yunyu-mysql:3306/yunyu?useUnicode=true\&characterEncoding=utf8\&serverTimezone=Asia/Shanghai \
  -e SPRING_DATASOURCE_USERNAME=yunyu \
  -e SPRING_DATASOURCE_PASSWORD=yunyu123456 \
  -e YUNYU_JWT_SECRET=请替换成正式密钥 \
  ghcr.io/<github-owner>/yunyu-server-native:latest
```

### 方案 B：验证通过后再替换 JVM 版本

如果 native 版本验证通过，可以将部署镜像切换到：

```bash
ghcr.io/<github-owner>/yunyu-server-native:latest
```

## 八、推荐验证顺序

建议按以下顺序落地：

1. 本地先执行 `./mvnw -Pnative -DskipTests native:compile`
2. 本地验证核心接口是否正常
3. 再用 `spring-boot:build-image` 或 `Dockerfile.native` 打镜像
4. 服务器并行部署一个 `20001` 端口的 native 实例
5. 对比以下指标：

- 启动时间
- 空载内存
- 登录接口是否正常
- 前台内容接口是否正常
- 后台 CRUD 是否正常

6. 验证通过后再决定是否替换正式 JVM 服务

## 九、当前方案的已知风险

当前 native 方案已经给出完整打包入口，但是否能一次构建通过，仍取决于运行库兼容情况。

你这个项目最可能需要进一步补充 native 兼容的点主要是：

- `MyBatis-Plus`
- `JJWT`
- 部分反射和 JSON 序列化链路

因此更建议把 native 方案当作：

- 第一阶段先新增并验证
- 第二阶段再逐步修兼容
- 第三阶段再考虑替换正式部署

## 十、一句话建议

对于当前这个“小博客 + Spring Boot + JVM 基线偏大”的场景，最稳的落地方式是：

- **当前 JVM 部署流程保持不动**
- **并行新增一条 native 镜像链路**
- **先跑通、先对比、再替换**
