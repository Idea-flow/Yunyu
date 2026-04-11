# Yunyu Server Native Image 本地构建与接口验证命令

## 1. 文档目标

本文档用于提供 `yunyu-server` 在本地进行 Native Image 构建、启动与接口验证时可直接执行的命令清单。

适用场景：

1. 本地修改了 `yunyu-native-image-support` 或 `yunyu-server`
2. 想重新构建 Native 二进制
3. 想快速验证 Native 环境下常用接口是否正常

## 2. 前置条件

执行前请先确认本机具备以下条件：

1. 已安装 GraalVM JDK 25
2. 已安装 Maven
3. 本地 MySQL 可访问
4. 数据库 `yunyu` 已准备好，或允许系统自动初始化

当前本地验证默认使用：

1. MySQL 地址：`127.0.0.1:3306`
2. 服务端口：`20000`
3. Spring Profile：`native`

## 3. 本地构建命令

### 3.1 先安装 Native Support 工程

进入目录：

```bash
cd /Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-native-image-support
```

执行：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests install
```

作用：

1. 把 `yunyu-native-image-support-core`
2. `yunyu-native-image-support-lambda-override`
3. `yunyu-native-image-support-starter`

安装到本地 Maven 仓库，供 `yunyu-server` Native 构建时引用。

### 3.2 再构建 `yunyu-server` Native 二进制

进入目录：

```bash
cd /Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server
```

执行：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests -Pnative package
```

注意：

1. 当前 Native 构建依赖 `yunyu-server/pom.xml` 中 `native` profile 里的两条关键 buildArgs：
   - `--features=com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeFeature`
   - `-Dyunyu.native.applicationClass=com.ideaflow.yunyu.YunyuServerApplication`
2. 这两条参数不能删除
3. 删除后虽然可能仍能构建并启动，但登录接口与 `LambdaQueryWrapper` 相关接口会在运行时报 `writeReplace()` 相关异常

构建成功后产物位于：

```bash
/Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server/target/yunyu-server
```

## 4. 本地启动命令

进入目录：

```bash
cd /Users/wangpenglong/projects/full-stack-project/Yunyu/yunyu-server
```

执行：

```bash
./target/yunyu-server --spring.profiles.active=native
```

启动成功后，默认访问地址：

```bash
http://127.0.0.1:20000
```

## 5. 常用验证命令

以下命令用于验证 Native Image 是否已经具备基本可用性。

### 5.1 健康检查

```bash
curl -i http://127.0.0.1:20000/actuator/health
```

预期：

1. 返回 `HTTP/1.1 200`
2. 响应体包含 `status":"UP"`

### 5.2 登录接口

```bash
curl -i -H 'Content-Type: application/json' \
  -d '{"account":"yunyu","password":"yunyu"}' \
  http://127.0.0.1:20000/api/auth/login
```

预期：

1. 返回 `HTTP/1.1 200`
2. `code=0`
3. 返回 `accessToken`

如果这里报错，优先检查：

1. `yunyu-server/pom.xml` 的 `native` profile 中是否仍然保留：
   - `--features=com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeFeature`
   - `-Dyunyu.native.applicationClass=${yunyu.native.applicationClass}`
2. Native 二进制是否是在保留这两条参数的前提下重新构建出来的

### 5.3 文章分页列表接口

```bash
curl -i 'http://127.0.0.1:20000/api/site/posts?pageNo=1&pageSize=10'
```

预期：

1. 返回 `HTTP/1.1 200`
2. `code=0`
3. 返回分页数据 `list / total / totalPages`

说明：

这个接口可用于验证 Native 环境下分页查询与常规 MyBatis-Plus 查询链路是否正常。

### 5.4 `LambdaQueryWrapper` 查询接口

当前项目里，文章分页接口已经覆盖到了 `LambdaQueryWrapper` 查询链路，因此本地最常用的 Lambda 验证命令可以直接使用：

```bash
curl -i 'http://127.0.0.1:20000/api/site/posts?pageNo=1&pageSize=10'
```

如果这个接口正常返回，通常可以说明：

1. `LambdaUtils`
2. `SerializedLambda`
3. 自定义 GraalVM Feature
4. MyBatis-Plus Lambda Native 兼容链路

已经基本工作正常。

如果这个接口和登录接口同时失败，且日志里出现：

1. `writeReplace()`
2. `NoSuchMethodException`
3. `NoSuchMethodError`

通常可以直接判断为：

1. Native 构建时缺少 `YunyuNativeRuntimeFeature`
2. 或缺少 `yunyu.native.applicationClass` 传参

### 5.5 Swagger 检查

Native 模式下当前默认关闭 `springdoc`，所以执行下面命令时：

```bash
curl -i http://127.0.0.1:20000/swagger-ui.html
```

预期是：

1. 不作为通过标准
2. 即使返回非 200，也不代表 Native 构建失败

因为当前 `application-native.yml` 中本来就关闭了 Swagger 相关配置。

## 6. 推荐验证顺序

建议按以下顺序验证：

1. 先看启动日志，确认数据库初始化链路正常
2. 再跑健康检查
3. 再跑登录接口
4. 最后跑文章分页接口

这是因为：

1. 健康检查能确认服务整体可用
2. 登录接口能确认安全链路、数据库访问和 JSON 序列化可用
3. 文章分页接口能确认 MyBatis-Plus 与 Lambda 查询链路可用

## 7. 常用排错命令

### 7.1 查看端口占用

```bash
lsof -i :20000
```

### 7.2 停止正在运行的 Native 进程

如果前台运行，直接 `Ctrl+C` 即可。

如果后台运行，可先查进程：

```bash
ps -Ao pid,command | grep yunyu-server | grep -v grep
```

再执行：

```bash
kill <PID>
```

### 7.3 重新构建前清理旧产物

进入 `yunyu-server` 目录后执行：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests clean
```

如果怀疑 `yunyu-native-image-support` 的本地安装产物不一致，也可以在 support 工程重新执行一次：

```bash
mvn -q -Dmaven.repo.local=/Users/wangpenglong/usr/local/mavenRepository/aliRepository -DskipTests clean install
```

## 8. 当前通过标准

当前本地 Native 验证通过，建议至少满足以下标准：

1. Native 二进制可成功构建
2. Native 二进制可成功启动
3. `GET /actuator/health` 返回 `200`
4. `POST /api/auth/login` 返回 `200`
5. `GET /api/site/posts?pageNo=1&pageSize=10` 返回 `200`

只要上述 5 项全部通过，就可以认为当前 Native Image 基本具备可部署性。
