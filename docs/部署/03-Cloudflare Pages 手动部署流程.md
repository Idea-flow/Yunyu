# Cloudflare Pages 手动部署流程

## 文档目标

本文档用于说明 `yunyu-web` 如何通过 Cloudflare 控制台手动接入 GitHub 仓库并完成第一版部署。

## 一、当前是否必须补 Cloudflare 专属适配代码

当前阶段结论：

- 第一版不一定必须补 Cloudflare 专属适配代码
- 你可以先直接使用 Cloudflare Pages 的 Git 集成方式部署
- 如果 Cloudflare 构建时不能正确识别当前 Nuxt 输出，再回头补充 Pages 专属适配配置

这样做的原因是：

- Nuxt 官方当前文档说明，Cloudflare Pages 的 Git 集成场景通常可以零配置部署
- Cloudflare 官方 Nuxt 文档给出的常见构建配置是 `build command = npm run build`、`build directory = dist`
- 你当前项目的前端目录已经独立，适合直接在 Cloudflare 后台把根目录指向 `yunyu-web`

## 二、部署前准备

在开始前，请先确认：

1. 代码仓库已经推送到 GitHub
2. 前端代码目录是 `yunyu-web`
3. 后端接口已经通过 `1Panel` 反向代理成可访问地址
4. 你已经确定后端接口域名，例如 `https://api.你的域名`

## 三、Cloudflare 后台操作步骤

### 步骤 1：进入 Pages

1. 登录 Cloudflare 控制台
2. 进入 `Workers & Pages`
3. 点击 `Create application`
4. 选择 `Pages`

### 步骤 2：连接 GitHub 仓库

1. 选择 `Import an existing Git repository`
2. 授权 Cloudflare 访问你的 GitHub
3. 选择当前项目仓库
4. 点击开始配置

### 步骤 3：填写构建信息

建议按以下值填写：

- Production branch：`main`
- Root directory：`yunyu-web`
- Build command：`pnpm build`
- Build output directory：`dist`

如果页面中可填写安装命令，则填写：

```bash
pnpm install
```

如果页面中可以指定 Node.js 版本，建议与 Nuxt 常规环境保持一致，使用 Cloudflare 提供的较新稳定版本即可。

## 四、环境变量配置

在 Pages 项目的环境变量中增加：

```bash
YUNYU_PUBLIC_API_BASE=https://api.你的域名
```

如果你临时还没有正式接口域名，也可以先写：

```bash
YUNYU_PUBLIC_API_BASE=http://服务器IP:20000
```

但第一版更推荐直接填写已经由 `1Panel` 反代过的接口域名。

## 五、首次发布后验证

发布成功后，重点检查：

1. 首页是否可以正常打开
2. 页面请求是否发往 `YUNYU_PUBLIC_API_BASE`
3. 登录、列表、详情页是否能拿到后端数据
4. 浏览器控制台是否存在跨域报错
5. Cloudflare Pages 构建日志是否有 `Nuxt` 构建异常

## 六、可能遇到的问题

### 1. 构建成功但页面请求不到后端

优先检查：

- `YUNYU_PUBLIC_API_BASE` 是否配置正确
- `1Panel` 反向代理是否生效
- 后端接口域名是否已配置 HTTPS
- 浏览器控制台是否报 CORS 错误

### 2. Cloudflare 构建失败

优先检查：

- Root directory 是否填写为 `yunyu-web`
- 是否正确使用 `pnpm install` 和 `pnpm build`
- 是否有依赖安装失败

如果这里失败，再考虑补充 Cloudflare 专属适配配置。

### 3. 页面能打开但 SSR 或路由异常

如果出现这类问题，第二步再处理：

- 补充 Nuxt 针对 Cloudflare 的部署适配配置
- 根据实际构建日志决定是否切换到更明确的 Cloudflare Pages preset

## 七、第一版建议

第一版最稳妥的做法是：

1. 后端先通过 `docker compose` 跑起来
2. 用 `1Panel` 把后端反代为 `api.你的域名`
3. 前端直接去 Cloudflare Pages 后台连接 GitHub 仓库
4. 配置 `YUNYU_PUBLIC_API_BASE=https://api.你的域名`
5. 先验证业务可用

等第一版跑通以后，再决定是否补更完整的 Cloudflare 适配和一键部署能力。
