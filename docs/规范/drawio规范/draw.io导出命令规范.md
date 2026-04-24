# draw.io导出命令规范

> 本文用于统一说明在 macOS 环境下，如何使用 `/Applications/draw.io.app` 导出 `.drawio` 文件的附属产物。
>
> 注意：
>
> - 本项目正式图源仍以 `.drawio` 为准。
> - `.png`、`.svg`、`.pdf` 等导出文件默认不是必选产物。
> - 只有在明确需要截图、插图、分享或打印时，才执行导出。

## 一、适用范围

- 适用于 macOS 本地安装的 draw.io 桌面版应用
- 适用于单文件导出与目录批量导出
- 适用于 `.png`、`.svg`、`.pdf`、`.jpg` 等常见格式

当前项目默认使用的可执行文件路径为：

```bash
/Applications/draw.io.app/Contents/MacOS/draw.io
```

为了避免路径中包含空格、括号等特殊字符时出现问题，后续命令统一建议用单引号包裹路径。

## 二、基础命令格式

通用命令格式：

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f 导出格式 -o 输出路径 输入文件.drawio
```

参数说明：

- `-x`：执行导出
- `-f`：指定导出格式，例如 `png`、`svg`、`pdf`
- `-o`：指定输出文件或输出目录

## 三、常用导出命令

### 1. 导出 PNG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

### 2. 导出 SVG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f svg -o './01-进程与线程关系图.svg' './01-进程与线程关系图.drawio'
```

### 3. 导出 PDF

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f pdf -o './01-进程与线程关系图.pdf' './01-进程与线程关系图.drawio'
```

### 4. 导出 JPG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f jpg -o './01-进程与线程关系图.jpg' './01-进程与线程关系图.drawio'
```

## 四、常用增强参数

### 1. 导出透明背景 PNG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -t -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

说明：

- `-t` 表示透明背景
- 更适合后续插入到深色背景、彩色背景或排版系统中

### 2. 导出时嵌入图源

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f svg -e -o './01-进程与线程关系图.svg' './01-进程与线程关系图.drawio'
```

说明：

- `-e` 表示在 PNG、SVG、PDF 中嵌入 diagram 数据
- 适合需要保留可追溯图源信息的场景
- 本项目默认不强制开启，按需使用

### 3. 设置边距

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -b 16 -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

说明：

- `-b` 表示导出边距，单位为像素
- 适合导出后需要直接贴到文档中，避免图贴边过紧

### 4. 缩放导出

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -s 2 -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

说明：

- `-s` 表示按倍数缩放导出
- 当默认尺寸不够清晰时，可以通过该参数提升输出分辨率

### 5. 指定宽度或高度

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png --width 1600 -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png --height 1200 -o './01-进程与线程关系图.png' './01-进程与线程关系图.drawio'
```

说明：

- `--width` 和 `--height` 会按比例适配
- 更适合对外部展示尺寸有明确要求时使用

## 五、多页文件导出

### 1. 导出指定页

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -p 2 -o './01-进程与线程关系图-第2页.png' './01-进程与线程关系图.drawio'
```

说明：

- `-p` 是页码参数
- 页码从 `1` 开始计数

### 2. PDF 导出全部页面

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f pdf -a -o './01-进程与线程关系图.pdf' './01-进程与线程关系图.drawio'
```

说明：

- `-a` 表示导出全部页面
- 主要用于多页 draw.io 文件导出 PDF

## 六、批量导出命令

### 1. 批量导出目录下所有 drawio 文件为 PNG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -r -f png -o './export' './drawio'
```

说明：

- 输入路径可以是目录
- `-r` 表示递归处理子目录
- `-o` 建议指定单独输出目录，避免和源文件混放

### 2. 批量导出目录下所有 drawio 文件为 SVG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -r -f svg -o './export-svg' './drawio'
```

## 七、推荐使用原则

- 正式图源只维护 `.drawio`
- 只有明确需要插图、分享、打印、汇报时才导出附属文件
- 导出文件尽量不要回写到正式知识目录中，除非这次任务明确要求这样做
- 若是临时预览，建议导出到单独临时目录，避免污染正式结构
- 若是要作为正式插图使用，图片应放入对应文档目录下的 `资源/文档名/img/`

## 八、常见命令模板

### 1. 当前目录下导出单文件 PNG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f png -o './输出.png' './输入.drawio'
```

### 2. 当前目录下导出单文件 SVG

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f svg -o './输出.svg' './输入.drawio'
```

### 3. 当前目录下导出单文件 PDF

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -f pdf -o './输出.pdf' './输入.drawio'
```

### 4. 批量递归导出整个目录

```bash
'/Applications/draw.io.app/Contents/MacOS/draw.io' -x -r -f png -o './export' './drawio目录'
```

## 九、补充说明

- 上述命令基于本机 `/Applications/draw.io.app` 的命令帮助校验过参数
- 如果后续 draw.io 桌面版升级，建议重新执行一次 `--help` 核对参数
- 如无明确要求，本项目仍以 `.drawio` 源文件维护为第一优先级
