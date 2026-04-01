# Yunyu Web

`Yunyu` 前端项目，基于 `Nuxt 4` 构建。

当前阶段目标：

- 承载内容站前台页面
- 后续承载后台管理界面
- 与 `yunyu-server` 对接认证与业务接口

## 安装依赖

```bash
pnpm install
```

## 启动开发环境

```bash
pnpm dev
```

默认访问地址：

- [http://localhost:19999](http://localhost:19999)

## 环境变量

可通过 `.env` 配置后端接口地址：

```bash
YUNYU_PUBLIC_API_BASE=http://127.0.0.1:20000
```

## 常用命令

```bash
pnpm dev
pnpm build
pnpm preview
pnpm generate
```

## 当前骨架说明

- 已完成 `Nuxt 4` 最小项目初始化
- 已替换默认欢迎页
- 已补充基础页面渲染入口
- 已补充后端接口地址运行时配置
