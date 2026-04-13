# Video Player 使用说明

## 1. 使用目标

当前目录已经适合复制到其他使用 Tailwind CSS 的 Nuxt 项目。

默认做法：

1. 复制整个 [app/components/video](/Users/wangpenglong/projects/nuxt/asmr-serverless/app/components/video) 目录
2. 在目标项目安装 `hls.js`
3. 直接引入 `VideoPlayer.vue`

## 2. 目录结构

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
  SELF_CONTAINED.md
  USAGE.md
  index.ts
```

## 3. 复制到其他 Nuxt 项目时要做什么

### 3.1 复制目录

把整个 `app/components/video` 目录复制到目标项目相同位置：

- `app/components/video`

### 3.2 安装播放引擎依赖

播放器当前仍依赖 `hls.js` 播放 `m3u8`：

```bash
pnpm add hls.js
```

或者：

```bash
npm install hls.js
```

### 3.3 不需要额外安装 UI 组件库

当前播放器目录内部已经自带：

- 按钮
- 徽标
- Lucide 风格本地图标文件
- toast

所以不再需要：

- `@nuxt/ui`
- `UButton`
- `UBadge`
- `UIcon`
- `useToast`

## 4. 最小接入示例

```vue
<script setup lang="ts">
import VideoPlayer from '~/components/video/VideoPlayer.vue'
import type { VideoSourceItem } from '~/components/video/player.types'

const activeSourceId = ref<number | null>(null)

const sources: VideoSourceItem[] = [
  {
    id: 1,
    label: '主线路',
    sourceType: 'mp4',
    sourceUrl: 'https://example.com/demo.mp4'
  },
  {
    id: 2,
    label: '备用线路',
    sourceType: 'm3u8',
    sourceUrl: 'https://example.com/demo.m3u8'
  }
]
</script>

<template>
  <VideoPlayer
    v-model:active-source-id="activeSourceId"
    :sources="sources"
    poster-url="https://example.com/poster.jpg"
  />
</template>
```

## 5. 常见使用方式

### 5.1 隐藏外围信息区

```vue
<VideoPlayer
  :sources="sources"
  :controls="false"
/>
```

### 5.2 手动控制默认线路

```vue
<script setup lang="ts">
const activeSourceId = ref<number | null>(2)
</script>

<template>
  <VideoPlayer
    v-model:active-source-id="activeSourceId"
    :sources="sources"
  />
</template>
```

### 5.3 用统一导出入口引入

```ts
import { VideoPlayer } from '~/components/video'
```

如果你的项目开启了 Nuxt 组件自动导入，也可以直接在模板中使用 `VideoPlayer`。

### 5.4 单独使用图标并控制响应式尺寸

`VideoIcon` 已经内置响应式尺寸档位，默认会在手机端略小、平板和桌面端逐步放大：

```vue
<script setup lang="ts">
import { VideoIcon } from '~/components/video'
</script>

<template>
  <VideoIcon name="play" size="hero" />
</template>
```

可选 `size`：

- `compact`：更适合 toast、关闭按钮、辅助状态图标
- `default`：常规控制按钮图标
- `action`：快进、快退这类强调操作图标
- `hero`：中间播放按钮这类主操作图标

## 6. `sources` 数据格式

```ts
interface VideoSourceItem {
  id: number
  label: string
  sourceType: 'mp4' | 'm3u8' | 'iframe' | 'iframeFull'
  sourceUrl: string
}
```

字段说明：

- `id`：播放源唯一标识
- `label`：线路名称
- `sourceType`：播放源类型
- `sourceUrl`：视频直链、m3u8 地址或 iframe 数据

## 7. 各种源类型怎么填

### 7.1 `mp4`

```ts
{
  id: 1,
  label: 'MP4线路',
  sourceType: 'mp4',
  sourceUrl: 'https://example.com/demo.mp4'
}
```

### 7.2 `m3u8`

```ts
{
  id: 2,
  label: 'M3U8线路',
  sourceType: 'm3u8',
  sourceUrl: 'https://example.com/demo.m3u8'
}
```

### 7.3 `iframe`

```ts
{
  id: 3,
  label: '嵌入线路',
  sourceType: 'iframe',
  sourceUrl: 'https://example.com/embed/abc'
}
```

### 7.4 `iframeFull`

```ts
{
  id: 4,
  label: '完整嵌入',
  sourceType: 'iframeFull',
  sourceUrl: '<iframe src=\"https://example.com/embed/abc\" allowfullscreen></iframe>'
}
```

## 8. 当前自包含到了什么程度

当前目录内部已经自包含这些能力：

- 播放器三端视图
- 基础按钮
- 基础徽标
- 统一图标入口
- 本地 Lucide 风格 SVG 文件
- 目录内 toast
- 播放控制逻辑
- iframe 工具
- 线路排序工具

当前仍保留的唯一外部运行时依赖是：

- `hls.js`

## 9. 如果你还要继续做更彻底的模块化

可以继续往下走两步：

1. 把 `hls.js` 依赖包装成可替换适配层
2. 把这个目录进一步抽成独立 package 或 Nuxt layer
