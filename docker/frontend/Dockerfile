# syntax=docker/dockerfile:1.7

# 构建阶段：安装依赖并构建 Nuxt 4 SSR 应用。
# 构建上下文为项目根目录，通过 COPY yunyu-web/ 引入前端源码。
FROM node:22-alpine AS builder

WORKDIR /app

# 启用 pnpm（锁定 v9，与 lockfileVersion 9.0 匹配）
RUN corepack enable && corepack prepare pnpm@9 --activate

# 配置 pnpm store 路径，与 cache mount 保持一致。
RUN pnpm config set store-dir /root/.pnpm-store

# 先复制依赖描述文件，利用 Docker 层缓存加速重复构建。
COPY yunyu-web/package.json yunyu-web/pnpm-lock.yaml ./

RUN --mount=type=cache,target=/root/.pnpm-store \
    pnpm install --frozen-lockfile

# 复制全部前端源码并构建。
COPY yunyu-web/ ./

RUN pnpm build

# 运行阶段：只保留 Nuxt 构建产物 .output，减小镜像体积。
FROM node:22-alpine AS runtime

WORKDIR /app

COPY --from=builder /app/.output ./

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000

# 启动 Nuxt SSR 服务。
CMD ["node", "server/index.mjs"]
