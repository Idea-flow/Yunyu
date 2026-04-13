# Video Player 目录说明

## 1. 当前状态

这个目录已经整理成“播放器自包含模块”。

目标是：

- 复制整个 `app/components/video` 目录到其他 Nuxt 项目即可复用
- 播放器目录内部不再依赖 `@nuxt/ui`
- 播放器目录内部不再依赖目录外的按钮、徽标、图标和 toast 方案

## 2. 目录内包含什么

- [VideoPlayer.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/VideoPlayer.vue)
  播放器总入口，负责手机 / 平板 / 桌面三端分发，并挂载目录内 toast 视图
- [VideoPlayerMobile.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/VideoPlayerMobile.vue)
  手机端播放器视图
- [VideoPlayerTablet.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/VideoPlayerTablet.vue)
  平板端播放器视图
- [VideoPlayerDesktop.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/VideoPlayerDesktop.vue)
  桌面端播放器视图
- [VideoToastViewport.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/VideoToastViewport.vue)
  目录内专用 toast 视图
- [base/VideoButton.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/base/VideoButton.vue)
  目录内基础按钮组件
- [base/VideoBadge.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/base/VideoBadge.vue)
  目录内基础徽标组件
- [icons/VideoIcon.vue](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/icons/VideoIcon.vue)
  目录内统一图标组件
- [icons/icon-map.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/icons/icon-map.ts)
  图标名称与本地 SVG 文件的映射关系
- `icons/*.svg`
  本地化的 Lucide 风格 SVG 资源文件，后续可按文件单独替换与维护
- [composables/useVideoPlayerController.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/composables/useVideoPlayerController.ts)
  播放控制核心逻辑
- [composables/useVideoToast.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/composables/useVideoToast.ts)
  目录内专用 toast 管理器
- [utils/video-iframe.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/utils/video-iframe.ts)
  iframe 解析工具
- [utils/video-source-order.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/utils/video-source-order.ts)
  线路排序工具
- [SELF_CONTAINED.md](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/SELF_CONTAINED.md)
  本次自包含改造方案文档
- [USAGE.md](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/USAGE.md)
  接入与使用说明
- [index.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/index.ts)
  可选统一导出入口

## 3. 为什么现在可以整体复制

播放器目录内部现在已经完成这些收口：

- 组件之间全部使用目录内相对引用
- 播放控制逻辑放在目录内 `composables/`
- 按钮、徽标、图标都在目录内
- 图标资源已经拆成 `icons/*.svg` 独立文件
- toast 能力也在目录内

所以复制这个目录后，不需要额外再去补：

- 外部 UI 组件库
- 外部播放器专用 toast 实现
- 目录外播放器工具文件

另外，`icons/VideoIcon.vue` 现在已经内置响应式尺寸档位：

- `compact`
- `default`
- `action`
- `hero`

播放器内部已经统一改成按语义使用这些档位，后续如果你要整体缩放图标，只需要改这一处。

## 4. 当前仍保留的兼容出口

为了不破坏当前项目已有引用，仓库里仍保留这些旧路径转发文件：

- [app/composables/useVideoPlayerController.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/composables/useVideoPlayerController.ts)
- [app/utils/video-iframe.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/utils/video-iframe.ts)
- [app/utils/video-source-order.ts](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/utils/video-source-order.ts)

这些文件现在只是兼容出口，真正实现已经都在 `app/components/video` 目录内。

## 5. 剩余运行时依赖

当前目录仍保留一项播放引擎依赖：

- `hls.js`

它负责 `m3u8` 播放能力，不属于 UI 组件库依赖。

如果你后续还想继续做到“连 HLS 播放引擎也不依赖 npm 包”，需要再单独做 vendor 化或替代实现评估。

## 6. 如何接入

直接看：

- [USAGE.md](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video/USAGE.md)
