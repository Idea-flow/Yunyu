# Video Player 自包含改造方案

## 1. 目标

本次改造目标是把 `app/components/video` 整理成真正可整体复制的自包含目录。

目标约束：

- 播放器目录内部不再依赖 `@nuxt/ui`
- 播放器目录内部不再依赖目录外的按钮、徽标、图标、toast 实现
- 其他使用 Tailwind CSS 的 Nuxt 项目复制整个目录后即可接入播放器 UI
- 播放器已有能力不回退：
  - `mp4`
  - `m3u8`
  - `iframe / iframeFull`
  - 手机 / 平板 / 桌面三端视图
  - 全屏 / 画中画 / 切线 / 进度 / 音量 / 加载提示

## 2. 本次改造范围

### 2.1 移除外部 UI 依赖

从播放器目录中移除：

- `UButton`
- `UBadge`
- `UIcon`
- `useToast`

### 2.2 增加目录内基础设施

在播放器目录内部新增：

- `base/VideoButton.vue`
- `base/VideoBadge.vue`
- `icons/*.svg`
- `icons/icon-map.ts`
- `icons/VideoIcon.vue`
- `composables/useVideoToast.ts`
- `VideoToastViewport.vue`

## 3. 替换策略

### 3.1 按钮

原先依赖 `UButton` 的位置统一替换为目录内 `VideoButton`。

职责：

- 提供图标按钮 / 图标加文案按钮两种形态
- 提供 `ghost / soft` 两种视觉变体
- 提供 `neutral / warning / success` 等基础语义色

### 3.2 徽标

原先依赖 `UBadge` 的位置统一替换为目录内 `VideoBadge`。

职责：

- 仅承载轻量语义徽标
- 使用 Tailwind 原子类实现边框、背景和文字颜色

### 3.3 图标

原先依赖 `UIcon` 的位置统一替换为目录内 `icons/VideoIcon.vue`。

职责：

- 使用目录内本地 SVG 文件承载 Lucide 风格图标
- `VideoIcon.vue` 只负责统一入口与名称分发
- `icon-map.ts` 负责维护图标名称与 svg 文件映射
- 后续替换单个图标时，只需要改对应 svg 文件

当前内置图标包括：

- copy
- expand
- minimize
- picture-in-picture
- play
- rotate-ccw
- rotate-cw
- skip-forward
- volume
- volume-off
- close
- check
- chevron-right
- list-video
- loader

### 3.4 Toast

原先依赖 `useToast` 的位置统一替换为目录内 `useVideoToast`。

职责：

- 在播放器根组件内提供 toast 管理器
- 在桌面 / 手机 / 平板组件与控制器 composable 内共享通知
- 提供 info / success / warning / error 四类反馈
- 用 `VideoToastViewport.vue` 渲染到页面顶部

## 4. 目录内最终结构

```text
app/components/video/
  base/
    VideoBadge.vue
    VideoButton.vue
  icons/
    *.svg
    icon-map.ts
    VideoIcon.vue
  composables/
    useVideoPlayerController.ts
    useVideoToast.ts
  utils/
    video-iframe.ts
    video-source-order.ts
  VideoPlayer.vue
  VideoPlayerMobile.vue
  VideoPlayerTablet.vue
  VideoPlayerDesktop.vue
  VideoToastViewport.vue
  player.types.ts
  video-player.css
  README.md
  USAGE.md
  SELF_CONTAINED.md
  index.ts
```

## 5. 说明

这次改造会把播放器 UI 与目录内交互依赖全部内聚进 `video` 目录。

当前仍保留的一项运行时依赖是：

- `hls.js`

它负责 `m3u8` 播放能力，属于播放引擎依赖，不属于 UI 组件库依赖。

如果后续还要做到“连播放引擎也零外部依赖”，需要再单独评估是否把 HLS 播放实现一并内置或 vendor 化。
