# 无封面首屏与 `homepage_config` 设计方案

## 文档目标

本文档用于明确 `Yunyu` 首页下一阶段的两个核心方向：

- 首页首屏从“绑定某篇文章封面”升级为“无封面的品牌首屏”
- 在现有站点配置体系中增加 `homepage_config`，让首页首屏与首页局部模块支持独立配置

本文档不是视觉灵感记录，而是后续前端实现、后台表单设计、接口扩展和配置持久化的执行依据。

## 一、为什么要做“无封面首屏”

当前首页如果长期绑定某篇文章封面，会有几个问题：

- 首屏气质会跟着文章封面频繁变化，站点品牌感不稳定
- 首页第一屏容易变成“某篇文章推广位”，而不是“博客 / 内容网站首页”
- 当封面质量不够稳定时，首页整体高级感会被拖低
- 首屏标题、站点定位、导航入口会受图片干扰，识别效率下降

因此首页第一屏更适合改成：

- 一个稳定的品牌首屏
- 一个表达站点定位的首屏
- 一个告诉用户“从哪里开始看”的首屏

文章封面与主打内容，适合放在首屏之后承接，而不是占据整张首页封面。

## 二、无封面首屏的设计目标

无封面首屏的目标不是“更空”，而是“更稳”。

首页首屏需要同时完成 3 件事：

1. 让用户知道这是一个什么网站
2. 让用户知道这个网站主要写什么
3. 让用户知道下一步应该点哪里

因此首屏设计应该遵循以下原则：

- 不绑定具体文章封面
- 不堆大量模块
- 不同时展示文章、分类、专题、说明卡、数据卡
- 标题是第一视觉
- 行动按钮明确
- 背景是品牌氛围，不是内容图片

## 三、无封面首屏页面方案

### 3.1 页面定位

首页首屏建议采用：

- `Minimal Single Column`
- `Content First`
- `Editorial Minimal`

也就是：

- 大标题主导
- 副标题简洁
- 少量关键词
- 两个按钮
- 下方再进入内容区

### 3.2 首屏结构

推荐结构如下：

1. 品牌眉题
2. 主标题
3. 副标题
4. 关键词标签
5. 主按钮
6. 次按钮
7. 轻量统计或关键词列

### 3.3 页面线框

```text
+-------------------------------------------------------------+
| 品牌眉题                                                    |
|                                                             |
| 大标题：一句长期稳定的站点主张                              |
|                                                             |
| 副标题：一句更具体的内容说明                                |
|                                                             |
| [查看文章]   [进入专题]                                     |
|                                                             |
| #写作   #技术   #审美   #长期主义                           |
|                                                             |
| 文章 48   专题 12   分类 8                                  |
+-------------------------------------------------------------+

+-------------------------- 首屏以下 --------------------------+
| 主打文章 / 最新文章                                         |
| 分类入口                                                    |
| 专题入口                                                    |
+-------------------------------------------------------------+
```

## 四、无封面首屏的具体视觉建议

### 4.1 背景

首屏背景不再使用文章图片，改为可配置的品牌背景。

推荐背景模式：

- `gradient-grid`
  作用：干净、现代、稳定，适合长期使用
- `soft-glow`
  作用：更柔和，适合个人博客
- `minimal-lines`
  作用：更克制，适合技术内容站
- `keyword-cloud`
  作用：用几个关键词做弱展示，个性更明显

不建议：

- 视频背景
- 高强度 3D
- 大量粒子动画
- 高对比大图拼贴

### 4.2 标题

标题建议使用“长期稳定主张”，而不是文章标题。

示例：

- 把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站
- 记录技术、审美与创作的个人博客
- 这里写文章，也整理一个人长期关注的主题

### 4.3 副标题

副标题只承担补充说明，不重复标题。

建议长度：

- 桌面端 1 到 2 行
- 移动端 2 到 3 行

### 4.4 CTA

首屏只保留两个按钮：

- 主按钮：`查看文章`
- 次按钮：`进入专题` 或 `查看分类`

不建议在首屏放太多入口按钮。

### 4.5 关键词与轻量统计

可选内容：

- 关键词：`写作`、`技术`、`设计`、`阅读`
- 轻量统计：文章数、专题数、分类数

这里只能轻量存在，不能变成后台数据面板。

## 五、首屏以下的页面结构建议

无封面首屏落地后，首页建议结构为：

1. 无封面品牌首屏
2. 主打内容区
3. 最新文章区
4. 分类入口区
5. 专题入口区

如果需要进一步简化，可收成：

1. 无封面品牌首屏
2. 最新文章区
3. 分类 / 专题入口区

## 六、`homepage_config` 设计目标

`homepage_config` 的目标不是把首页所有内容都配置化，而是把“首页品牌首屏”和“首页局部开关”配置化。

它主要解决以下问题：

- 首页主标题不应写死在代码里
- 首页副标题应支持长期调整
- 首页按钮文案与跳转不应依赖固定路由写死
- 首屏背景模式应支持切换
- 首页是否显示关键词、统计、主打区应支持开关

## 七、推荐持久化方案

当前项目已存在 `site_config` 表，建议继续复用，不新增首页总表。

推荐方式：

- `config_key = homepage_config`
- `config_json` 存放首页专属配置

这样做的好处：

- 与现有配置体系一致
- 方便后台统一管理
- 先落地快，后续也可扩展

## 八、`homepage_config` JSON 结构

推荐结构如下：

```json
{
  "heroEnabled": true,
  "heroLayout": "brand",
  "heroBackgroundMode": "gradient-grid",
  "heroEyebrow": "Yunyu / 云屿",
  "heroTitle": "把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站",
  "heroSubtitle": "记录技术、审美、创作与阅读的个人博客与内容网站",
  "heroPrimaryButtonText": "查看文章",
  "heroPrimaryButtonLink": "/posts",
  "heroSecondaryButtonText": "进入专题",
  "heroSecondaryButtonLink": "/topics",
  "heroKeywords": [
    "写作",
    "技术",
    "审美",
    "长期主义"
  ],
  "showHeroKeywords": true,
  "showHeroStats": true,
  "heroStats": [
    { "label": "文章", "value": "48" },
    { "label": "专题", "value": "12" },
    { "label": "分类", "value": "8" }
  ],
  "showFeaturedSection": true,
  "featuredSectionTitle": "主打内容",
  "showLatestSection": true,
  "latestSectionTitle": "最新文章",
  "showCategorySection": true,
  "categorySectionTitle": "分类",
  "showTopicSection": true,
  "topicSectionTitle": "专题"
}
```

## 九、前端字段设计

### 9.1 前台读取模型

建议新增前台首页配置类型：

```ts
/**
 * 首页首屏统计项类型。
 * 作用：统一描述首页首屏可配置统计展示项。
 */
export interface HomePageHeroStat {
  label: string
  value: string
}

/**
 * 前台首页配置类型。
 * 作用：统一描述首页品牌首屏和首页模块开关配置。
 */
export interface HomePageConfig {
  heroEnabled: boolean
  heroLayout: 'brand'
  heroBackgroundMode: 'gradient-grid' | 'soft-glow' | 'minimal-lines' | 'keyword-cloud'
  heroEyebrow: string
  heroTitle: string
  heroSubtitle: string
  heroPrimaryButtonText: string
  heroPrimaryButtonLink: string
  heroSecondaryButtonText: string
  heroSecondaryButtonLink: string
  heroKeywords: string[]
  showHeroKeywords: boolean
  showHeroStats: boolean
  heroStats: HomePageHeroStat[]
  showFeaturedSection: boolean
  featuredSectionTitle: string
  showLatestSection: boolean
  latestSectionTitle: string
  showCategorySection: boolean
  categorySectionTitle: string
  showTopicSection: boolean
  topicSectionTitle: string
}
```

### 9.2 后台表单模型

建议新增后台首页配置表单类型：

```ts
/**
 * 后台首页配置表单类型。
 * 作用：统一描述后台首页设置页需要维护的首页首屏和模块开关字段。
 */
export interface AdminHomepageConfigForm {
  heroEnabled: boolean
  heroLayout: 'brand'
  heroBackgroundMode: 'gradient-grid' | 'soft-glow' | 'minimal-lines' | 'keyword-cloud'
  heroEyebrow: string
  heroTitle: string
  heroSubtitle: string
  heroPrimaryButtonText: string
  heroPrimaryButtonLink: string
  heroSecondaryButtonText: string
  heroSecondaryButtonLink: string
  heroKeywords: string[]
  showHeroKeywords: boolean
  showHeroStats: boolean
  heroStats: { label: string, value: string }[]
  showFeaturedSection: boolean
  featuredSectionTitle: string
  showLatestSection: boolean
  latestSectionTitle: string
  showCategorySection: boolean
  categorySectionTitle: string
  showTopicSection: boolean
  topicSectionTitle: string
}
```

## 十、后端字段设计

### 10.1 推荐方式

不建议把 `homepage_config` 混进当前平铺的 `AdminSiteConfigUpdateRequest`。

更合理的方式是新增独立请求对象：

- `AdminHomepageConfigUpdateRequest`
- `AdminHomepageConfigResponse`

这样职责更清晰，也不会把站点基础信息和首页配置混成一个超大对象。

### 10.2 后端请求对象

建议字段如下：

```java
/**
 * 后台首页配置更新请求类。
 * 作用：承接站长在后台提交的首页首屏和首页模块显示配置，并在进入业务层前完成基础校验。
 */
public class AdminHomepageConfigUpdateRequest {

    private Boolean heroEnabled;
    private String heroLayout;
    private String heroBackgroundMode;
    private String heroEyebrow;
    private String heroTitle;
    private String heroSubtitle;
    private String heroPrimaryButtonText;
    private String heroPrimaryButtonLink;
    private String heroSecondaryButtonText;
    private String heroSecondaryButtonLink;
    private List<String> heroKeywords;
    private Boolean showHeroKeywords;
    private Boolean showHeroStats;
    private List<AdminHomepageHeroStatRequest> heroStats;
    private Boolean showFeaturedSection;
    private String featuredSectionTitle;
    private Boolean showLatestSection;
    private String latestSectionTitle;
    private Boolean showCategorySection;
    private String categorySectionTitle;
    private Boolean showTopicSection;
    private String topicSectionTitle;
}
```

### 10.3 后端统计项对象

```java
/**
 * 后台首页首屏统计项请求类。
 * 作用：统一承接首页首屏统计展示项配置。
 */
public class AdminHomepageHeroStatRequest {

    private String label;
    private String value;
}
```

### 10.4 返回对象

建议返回对象与请求对象基本一致：

- `AdminHomepageConfigResponse`
- `SiteHomepageConfigResponse`

其中：

- 后台返回完整可编辑字段
- 前台返回当前首页渲染需要的配置字段

## 十一、接口设计建议

推荐新增独立接口，不与当前基础站点设置接口混用。

### 11.1 后台接口

```text
GET  /api/admin/site/homepage-config
PUT  /api/admin/site/homepage-config
```

### 11.2 前台接口

有两种方式可选：

#### 方式 A：聚合进首页接口

把 `homepageConfig` 加到当前首页聚合响应：

```ts
export interface SiteHomeResponse {
  siteInfo: SiteBaseInfo
  homepageConfig: HomePageConfig
  featuredPosts: SitePostSummary[]
  latestPosts: SitePostSummary[]
  categories: SiteCategoryItem[]
  topics: SiteTopicItem[]
}
```

这是最推荐的方式，因为首页渲染本身就依赖首页配置。

#### 方式 B：单独接口

```text
GET /api/site/homepage-config
```

如果后续首页配置会在多个页面复用，也可以拆出来。

当前阶段更推荐方式 A。

## 十二、字段校验建议

### 12.1 文本长度

- `heroEyebrow`：`<= 64`
- `heroTitle`：`<= 120`
- `heroSubtitle`：`<= 255`
- `heroPrimaryButtonText`：`<= 32`
- `heroSecondaryButtonText`：`<= 32`
- 分区标题：`<= 32`

### 12.2 数组数量

- `heroKeywords`：最多 6 个
- `heroStats`：最多 4 个

### 12.3 枚举值

- `heroLayout`：当前固定 `brand`
- `heroBackgroundMode`：
  - `gradient-grid`
  - `soft-glow`
  - `minimal-lines`
  - `keyword-cloud`

## 十三、推荐实现顺序

### P0

- 新增 `homepage_config` 文档与 JSON 结构
- 前端按“无封面首屏”方案重构 Hero

### P1

- 新增前端 `HomePageConfig` 类型
- 后端新增 `AdminHomepageConfigUpdateRequest`
- 首页聚合接口增加 `homepageConfig`

### P2

- 后台新增“首页配置”编辑页或分组标签
- 支持切换背景模式、按钮文本、关键词和统计项

## 十四、结论

`Yunyu` 首页下一阶段最合理的方向不是继续依赖文章封面，而是建立一个稳定的、可配置的品牌首屏。

这意味着：

- 首屏不再绑定具体文章
- 首屏专门负责品牌表达与入口引导
- 主打文章移动到首屏之后承接
- 首页核心字段通过 `homepage_config` 配置化

这样首页才会更像“个人博客网站 / 内容网站首页”，而不是“某一篇文章的封面页”。
