# Yunyu Server Native Image GitHub Workflow 与 Docker 部署方案

## 1. 文档目标

本文档用于落实 `main-native-image` 分支后续要补充的两类能力：

1. GitHub Actions 自动构建 `yunyu-server` Native Image
2. 基于 Native 二进制的 Docker 镜像与部署文件

本文档当前只做方案设计，不直接进入代码开发实现。

## 2. 本次要解决的核心问题

围绕当前仓库状态，本次主要要解决下面 4 个问题：

1. 新工作流 `build-yunyu-server-native-image.yml` 如何设计
2. `main-native-image` 分支如何触发该工作流
3. `yunyu-native-image-support-starter` 只存在于源码仓库内、未发布到公共 Maven 仓库时，GitHub Actions 如何拿到依赖
4. Native Image 是否需要单独的 `Dockerfile` 与 `docker-compose.yml`

## 3. 当前仓库现状

### 3.1 已存在的 JVM 镜像工作流

当前仓库已存在：

- [build-yunyu-server-image.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/.github/workflows/build-yunyu-server-image.yml)

该工作流特点是：

1. 只在 `main` 分支触发
2. 构建的是 JVM 版本的 Docker 镜像
3. 直接使用 [yunyu-server/Dockerfile](/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server/Dockerfile)
4. 推送到 `GHCR`

### 3.2 已存在的部署文件

当前仓库已存在：

- [docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker-compose.yml)

它当前服务的对象是：

1. MySQL 容器
2. JVM 版本的 `yunyu-server` 镜像

因此，Native Image 部署如果沿用同一个文件，会和现有 JVM 部署语义混在一起，不利于后续维护。

### 3.3 Native Support 依赖现状

当前 `yunyu-server` 依赖：

- `com.ideaflow.yunyu:yunyu-native-image-support-starter:0.0.1-SNAPSHOT`

但这个依赖当前并没有发布到 Maven Central、GitHub Packages Maven 或其他公共 Maven 仓库，而是来自当前仓库内的源码工程：

- `/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-native-image-support`

这意味着 GitHub Actions 不能直接只执行 `mvn -f yunyu-server/pom.xml ...`，否则会缺少本地仓库里的 SNAPSHOT 依赖。

## 4. 关于“Linux / macOS / Windows Native Docker 镜像”的结论

这里需要先把一个关键边界说明清楚。

### 4.1 Docker 镜像的真正适用范围

严格来说，**可部署的 Docker 镜像主要对应 Linux 容器运行时**。

原因：

1. 生产环境中的 Docker 容器本质上跑的是 Linux 容器
2. `macOS` 与 `Windows` 常见场景下不是直接部署 Linux 生产容器
3. Native Image 生成出来的二进制本身也和目标操作系统、目标 CPU 架构强相关

所以如果目标是“部署镜像”，最合理的正式交付是：

1. Linux `amd64` Docker 镜像
2. Linux `arm64` Docker 镜像

### 4.2 `macOS` / `Windows` 更合理的交付形态

如果你希望 GitHub Actions 也支持 `macOS` 与 `Windows`，更合理的交付不是 Docker 镜像，而是：

1. `macOS` Native 二进制压缩包
2. `Windows` Native 二进制压缩包

也就是说，建议区分成两类产物：

1. **部署产物**
   - Linux 多架构 Docker 镜像

2. **分发 / 本地验证产物**
   - macOS Native 二进制
   - Windows Native 二进制

## 5. GitHub Workflow 设计建议

### 5.1 工作流文件名

建议新增：

- `.github/workflows/build-yunyu-server-native-image.yml`

### 5.2 触发条件

按你的要求，建议触发条件固定为：

1. `push` 到 `main-native-image`
2. `workflow_dispatch`

建议监听路径：

1. `yunyu-server/**`
2. `yunyu-native-image-support/**`
3. `docker/native-image/**`
4. `.github/workflows/build-yunyu-server-native-image.yml`

这样可以避免和 JVM 镜像工作流互相干扰。

### 5.3 当前最终采用的工作流形态

结合“要产出 `linux/amd64` 与 `linux/arm64` 两套 Native Docker 镜像”的目标，当前最终采用：

1. 单个 `build-and-push` Job
2. `docker buildx` 多平台构建
3. 在 Docker 多阶段构建里直接完成 Native 编译

这样做的原因是：

1. Native 二进制和目标 CPU 架构绑定
2. 不能只在 GitHub Runner 上编出一份二进制再拿去封装成所有平台镜像
3. 多平台镜像需要在各自目标平台容器上下文里分别完成 Native 编译

所以当前工作流实际步骤会变成：

1. `checkout` 仓库
2. `setup-qemu`
3. `setup-buildx`
4. 登录 `GHCR`
5. 通过 `docker/native-image/Dockerfile` 多阶段构建
6. 在构建阶段先安装 `yunyu-native-image-support`
7. 再构建 `yunyu-server` Native 二进制
8. 最后推送 `linux/amd64` 与 `linux/arm64` 镜像

### 5.4 为什么最终改成在 Docker 多阶段构建里完成 Native 编译

前面的预案里曾倾向于“先在 Runner 上编二进制，再封装镜像”，但真正落实到多平台镜像时，最终需要调整为：

1. 在 Docker 构建阶段直接完成 Native 编译
2. 让 `buildx` 针对不同目标平台分别产出对应二进制

当前这样做的收益是：

1. 不需要额外处理跨架构 Native 二进制分发
2. `yunyu-native-image-support` 可直接在同一构建链中 install 并消费
3. 最终推送到 `GHCR` 的就是可直接运行的多架构 Native 镜像

## 6. `yunyu-native-image-support-starter` 不上传 Maven 仓库时的处理方案

### 6.1 结论

**当前不需要上传到 Maven 公共仓库。**

因为源码已经和 `yunyu-server` 在同一个 Git 仓库中，可以直接在工作流里先构建并 install 本地依赖。

### 6.2 当前最终做法

当前最终做法不是先在 Workflow Step 里单独执行 Maven，而是：

1. 在 `docker/native-image/Dockerfile` 的构建阶段内
2. 先执行 `yunyu-native-image-support` 的 `install`
3. 再执行 `yunyu-server` 的 `-Pnative package`

核心命令仍然是：

```bash
mvn -B -f yunyu-native-image-support/pom.xml -DskipTests install
mvn -B -f yunyu-server/pom.xml -DskipTests -Pnative package
```

只是执行位置从“工作流独立步骤”变成了“Docker 多阶段构建内部步骤”。

### 6.3 为什么这条路更适合当前仓库

优点：

1. 不需要把内部 SNAPSHOT 发布到外部仓库
2. 分支内改动可以直接联动构建
3. 最符合当前“Native 支撑工程与业务工程同仓维护”的现状
4. 工作流对开发分支更友好

### 6.4 什么时候才考虑上传 Maven 仓库

只有在出现以下情况时，才建议考虑发布到独立 Maven 仓库：

1. `yunyu-native-image-support` 需要被多个仓库复用
2. 需要独立版本发布与回滚
3. 需要把 Native Support 从当前主仓库中彻底解耦

当前阶段不需要为了 GitHub Actions 先做这一步。

## 7. 是否需要新增 Native 专用 Dockerfile 和 docker-compose

### 7.1 结论

**建议需要。**

并且建议放到：

- `docker/native-image/Dockerfile`
- `docker/native-image/docker-compose.yml`

### 7.2 为什么建议单独建立 `docker/native-image`

原因：

1. 当前根目录 [docker-compose.yml](/Users/wangpenglong/projects/full-stack-project/Yunyu/docker-compose.yml) 服务的是 JVM 镜像部署
2. Native 镜像的基础镜像、入口命令、运行参数和 JVM 镜像明显不同
3. 把两种部署方式混在同一个 `Dockerfile` / `docker-compose.yml` 里，后续维护成本会更高

所以建议目录结构如下：

```text
docker
└── native-image
    ├── Dockerfile
    └── docker-compose.yml
```

### 7.3 Native Dockerfile 的当前职责

当前 Native Dockerfile 需要承担两段职责：

1. 构建阶段
   - 安装 Maven
   - install `yunyu-native-image-support`
   - 构建 `yunyu-server` Native 二进制

2. 运行阶段
   - 复制 Native 可执行文件
   - 暴露 `20000` 端口
   - 设置默认启动命令

### 7.4 Native docker-compose.yml 的职责建议

建议单独提供 Native 版 `docker-compose.yml`，方便部署时明确区分：

1. JVM 版部署
2. Native 版部署

建议内容：

1. MySQL 服务
2. `yunyu-server-native` 服务
3. Native 镜像地址变量
4. 与当前根 `docker-compose.yml` 保持相同的数据库环境变量习惯

## 8. 推荐的工作流实现方向

### 8.1 第一阶段目标

第一阶段当前落地为：

1. `main-native-image` 分支触发
2. 通过 Docker 多阶段构建生成 Linux Native 二进制
3. 推送 `linux/amd64`、`linux/arm64` Native 镜像到 `GHCR`

这是最符合“部署”诉求的第一优先级。

### 8.2 第二阶段可选增强

如果你确实还需要 `macOS` 和 `Windows`，建议第二阶段再加：

1. `macos-latest` Job
2. `windows-latest` Job
3. 产出 Native 二进制压缩包
4. 作为 Workflow Artifacts 或 GitHub Release 附件上传

但这里的产物建议是：

1. `.tar.gz`
2. `.zip`

而不是 Docker 镜像。

## 9. 建议的镜像命名

建议 Native 镜像单独命名，避免和 JVM 镜像混淆：

```text
ghcr.io/<github-owner>/yunyu-server-native
```

建议标签策略：

1. `latest`
2. `sha-<commit>`

后续如果需要，也可以增加：

1. `branch-main-native-image`

## 10. 当前推荐的实施顺序

建议按下面顺序落地：

1. 先新增文档
2. 再新增 `docker/native-image/Dockerfile`
3. 再新增 `docker/native-image/docker-compose.yml`
4. 再新增 `.github/workflows/build-yunyu-server-native-image.yml`
5. 最后再跑一次 GitHub Actions 联调

## 11. 当前建议的最终结论

当前最稳妥的方案是：

1. 新增 `build-yunyu-server-native-image.yml`
2. 仅在 `main-native-image` 分支触发
3. 工作流通过 Docker 多阶段构建，在镜像构建阶段先 `install` `yunyu-native-image-support`
4. 再构建 `yunyu-server` Native 二进制
5. 最终直接产出 Linux 多架构 Native Docker 镜像
6. 不要求先上传 `yunyu-native-image-support-starter` 到 Maven 公共仓库
7. 新增 `docker/native-image/Dockerfile` 与 `docker/native-image/docker-compose.yml`

## 12. 开发前待确认项

在正式开始开发前，建议你确认下面两个决策：

### 12.1 `macOS` / `Windows` 的目标是否要改成“二进制制品”而不是“Docker 镜像”

推荐答案：

1. Linux：交付 Docker 镜像
2. macOS / Windows：交付 Native 二进制制品

### 12.2 第一阶段是否只先做 Linux Docker 镜像

推荐答案：

1. 先做 `linux/amd64` 和 `linux/arm64` 的 Native Docker 镜像工作流
2. `macOS` / `Windows` 产物放到第二阶段

如果你确认这两个点，我下一步就可以开始实际补：

1. `.github/workflows/build-yunyu-server-native-image.yml`
2. `docker/native-image/Dockerfile`
3. `docker/native-image/docker-compose.yml`
