-- 云屿 / Yunyu
-- 功能：初始化一批可用于本地开发、联调和前台演示的测试数据。
-- 作用：补充动漫内容站风格的用户、分类、标签、专题、文章、正文、评论与站点配置。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `comment`;
DELETE FROM `friend_link`;
DELETE FROM `topic_post`;
DELETE FROM `post_tag`;
DELETE FROM `post_content`;
DELETE FROM `post`;
DELETE FROM `topic`;
DELETE FROM `tag`;
DELETE FROM `category`;
DELETE FROM `user_auth`;
DELETE FROM `user`;

INSERT INTO `user` (
  `id`, `email`, `user_name`, `avatar_url`, `password`, `password_hash`, `role`, `status`,
  `last_login_at`, `last_login_ip`, `created_time`, `updated_time`, `deleted`
)
VALUES
  (
    1,
    'admin@yunyu.local',
    '星野澄',
    'https://image.pollinations.ai/prompt/anime%20girl%20portrait%20clean%20soft%20light?width=512&height=512&seed=3201&nologo=true',
    'Admin@123456',
    '$2a$10$admin.demo.hash.value',
    'SUPER_ADMIN',
    'ACTIVE',
    '2026-03-31 22:15:00',
    '127.0.0.1',
    '2026-03-01 09:00:00',
    '2026-03-31 22:15:00',
    0
  ),
  (
    2,
    'editor@yunyu.local',
    '雾岛栞',
    'https://image.pollinations.ai/prompt/anime%20editor%20portrait%20blue%20hair%20illustration?width=512&height=512&seed=3202&nologo=true',
    'Editor@123456',
    '$2a$10$editor.demo.hash.value',
    'USER',
    'ACTIVE',
    '2026-03-30 19:40:00',
    '127.0.0.1',
    '2026-03-02 10:00:00',
    '2026-03-30 19:40:00',
    0
  ),
  (
    3,
    'reader@yunyu.local',
    '朝雾未央',
    'https://image.pollinations.ai/prompt/anime%20girl%20portrait%20warm%20sunset%20illustration?width=512&height=512&seed=3203&nologo=true',
    'Reader@123456',
    '$2a$10$reader.demo.hash.value',
    'USER',
    'ACTIVE',
    '2026-03-29 21:10:00',
    '127.0.0.1',
    '2026-03-03 11:00:00',
    '2026-03-29 21:10:00',
    0
  );

INSERT INTO `user_auth` (
  `id`, `user_id`, `auth_type`, `auth_identity`, `auth_name`, `auth_email`,
  `email_verified`, `raw_user_info`, `created_time`, `updated_time`
)
VALUES
  (
    1, 1, 'LOCAL', 'admin@yunyu.local', '星野澄', 'admin@yunyu.local',
    1, JSON_OBJECT('source', 'seed', 'role', 'SUPER_ADMIN'),
    '2026-03-01 09:00:00', '2026-03-31 22:15:00'
  ),
  (
    2, 2, 'LOCAL', 'editor@yunyu.local', '雾岛栞', 'editor@yunyu.local',
    1, JSON_OBJECT('source', 'seed', 'role', 'EDITOR'),
    '2026-03-02 10:00:00', '2026-03-30 19:40:00'
  ),
  (
    3, 3, 'LOCAL', 'reader@yunyu.local', '朝雾未央', 'reader@yunyu.local',
    1, JSON_OBJECT('source', 'seed', 'role', 'READER'),
    '2026-03-03 11:00:00', '2026-03-29 21:10:00'
  );

INSERT INTO `category` (
  `id`, `name`, `slug`, `description`, `cover_url`, `sort_order`, `status`,
  `created_time`, `updated_time`, `deleted`
)
VALUES
  (
    1,
    '新番观察',
    'season-watch',
    '追踪季度新番、口碑变化与值得补完的作品。',
    'https://image.pollinations.ai/prompt/anime%20spring%20season%20illustration%20sky?width=1200&height=675&seed=1101&nologo=true',
    10,
    'ACTIVE',
    '2026-03-01 10:00:00',
    '2026-03-20 10:00:00',
    0
  ),
  (
    2,
    '场景美学',
    'scene-aesthetics',
    '收录城市夜景、海边列车、教室与天台等高情绪场景。',
    'https://image.pollinations.ai/prompt/anime%20city%20night%20aesthetic%20illustration?width=1200&height=675&seed=1102&nologo=true',
    20,
    'ACTIVE',
    '2026-03-01 10:05:00',
    '2026-03-20 10:05:00',
    0
  ),
  (
    3,
    '角色档案',
    'character-file',
    '整理角色设定、成长线与名场面表现。',
    'https://image.pollinations.ai/prompt/anime%20character%20design%20sheet%20illustration?width=1200&height=675&seed=1103&nologo=true',
    30,
    'ACTIVE',
    '2026-03-01 10:10:00',
    '2026-03-20 10:10:00',
    0
  ),
  (
    4,
    '音乐与配乐',
    'music-score',
    '围绕片头曲、片尾曲与氛围配乐做内容整理。',
    'https://image.pollinations.ai/prompt/anime%20music%20studio%20illustration%20soft%20light?width=1200&height=675&seed=1104&nologo=true',
    40,
    'ACTIVE',
    '2026-03-01 10:15:00',
    '2026-03-20 10:15:00',
    0
  ),
  (
    5,
    '入坑指南',
    'starter-guide',
    '面向新读者的观看路线、补番顺序和风格推荐。',
    'https://image.pollinations.ai/prompt/anime%20library%20guide%20illustration%20warm?width=1200&height=675&seed=1105&nologo=true',
    50,
    'ACTIVE',
    '2026-03-01 10:20:00',
    '2026-03-20 10:20:00',
    0
  );

INSERT INTO `tag` (
  `id`, `name`, `slug`, `description`, `status`, `created_time`, `updated_time`, `deleted`
)
VALUES
  (1, '治愈系', 'healing', '偏温柔、慢节奏、舒缓情绪的内容。', 'ACTIVE', '2026-03-01 11:00:00', '2026-03-21 09:00:00', 0),
  (2, '都市夜景', 'city-night', '城市霓虹、夜色和雨幕氛围。', 'ACTIVE', '2026-03-01 11:05:00', '2026-03-21 09:05:00', 0),
  (3, '校园', 'campus', '教室、社团、青春关系线。', 'ACTIVE', '2026-03-01 11:10:00', '2026-03-21 09:10:00', 0),
  (4, '奇幻冒险', 'fantasy-adventure', '架空世界与成长旅程。', 'ACTIVE', '2026-03-01 11:15:00', '2026-03-21 09:15:00', 0),
  (5, '配乐精选', 'soundtrack', '围绕 OP/ED 和配乐名场面展开。', 'ACTIVE', '2026-03-01 11:20:00', '2026-03-21 09:20:00', 0),
  (6, '角色成长', 'character-growth', '角色自我完成与关系变化。', 'ACTIVE', '2026-03-01 11:25:00', '2026-03-21 09:25:00', 0),
  (7, '壁纸向', 'wallpaper', '适合作为封面和视觉收藏的内容。', 'ACTIVE', '2026-03-01 11:30:00', '2026-03-21 09:30:00', 0),
  (8, '新手入门', 'beginner', '帮助新读者快速进入作品语境。', 'ACTIVE', '2026-03-01 11:35:00', '2026-03-21 09:35:00', 0);

INSERT INTO `topic` (
  `id`, `name`, `slug`, `summary`, `cover_url`, `status`, `sort_order`,
  `created_time`, `updated_time`, `deleted`
)
VALUES
  (
    1,
    '2026 春季追番清单',
    'spring-2026-watchlist',
    '用一页内容看完春季新番里最值得先追的作品与观看理由。',
    'https://image.pollinations.ai/prompt/anime%20spring%20watchlist%20cover%20illustration?width=1200&height=675&seed=2101&nologo=true',
    'ACTIVE',
    10,
    '2026-03-05 10:00:00',
    '2026-03-26 10:00:00',
    0
  ),
  (
    2,
    '城市夜色美学',
    'city-night-aesthetics',
    '把最有氛围感的电车窗景、雨夜路口和天台夜风都收进一个专题。',
    'https://image.pollinations.ai/prompt/anime%20night%20city%20aesthetic%20cover?width=1200&height=675&seed=2102&nologo=true',
    'ACTIVE',
    20,
    '2026-03-05 10:10:00',
    '2026-03-26 10:10:00',
    0
  ),
  (
    3,
    '入坑路线图',
    'starter-roadmap',
    '给第一次接触动画内容站的新读者准备的阅读路线与分类导航。',
    'https://image.pollinations.ai/prompt/anime%20starter%20guide%20map%20illustration?width=1200&height=675&seed=2103&nologo=true',
    'ACTIVE',
    30,
    '2026-03-05 10:20:00',
    '2026-03-26 10:20:00',
    0
  );

INSERT INTO `post` (
  `id`, `title`, `slug`, `summary`, `cover_url`, `user_id`, `category_id`, `status`,
  `is_top`, `is_recommend`, `has_video`, `allow_comment`, `seo_title`, `seo_description`,
  `published_at`, `sort_order`, `view_count`, `like_count`, `comment_count`,
  `created_time`, `updated_time`, `deleted`
)
VALUES
  (
    1,
    '2026 春季新番先看哪 5 部：从情绪密度到作画稳定度的一次筛选',
    'spring-2026-first-five',
    '如果你只想先追 5 部作品，这篇会按情绪浓度、画面完成度和追更压力给出最稳妥的入场顺序。',
    'https://image.pollinations.ai/prompt/anime%20spring%20festival%20key%20visual?width=1600&height=900&seed=4101&nologo=true',
    1,
    1,
    'PUBLISHED',
    1,
    1,
    0,
    1,
    '2026 春季新番先看哪 5 部',
    '按情绪密度、作画稳定度和追更压力整理出的 2026 春季新番入门推荐。',
    '2026-03-27 20:00:00',
    90,
    4821,
    326,
    3,
    '2026-03-25 14:00:00',
    '2026-03-27 20:00:00',
    0
  ),
  (
    2,
    '雨夜电车窗景为什么总能打动人：12 个高情绪城市镜头拆解',
    'rainy-city-train-scenes',
    '从霓虹倒影、玻璃反光和人物留白三个角度，拆解动画中最容易让人沉浸的城市夜景镜头。',
    'https://image.pollinations.ai/prompt/anime%20rainy%20city%20train%20window%20illustration?width=1600&height=900&seed=4102&nologo=true',
    2,
    2,
    'PUBLISHED',
    0,
    1,
    0,
    1,
    '雨夜电车窗景镜头拆解',
    '拆解动画里最有氛围感的雨夜电车与城市夜景镜头语言。',
    '2026-03-26 21:10:00',
    80,
    3680,
    281,
    2,
    '2026-03-24 16:10:00',
    '2026-03-26 21:10:00',
    0
  ),
  (
    3,
    '从“温柔而坚定”到“终于开口”：本季最完整的角色成长线',
    'character-growth-arc',
    '这篇围绕一条完整成长线，记录角色从沉默到表达、从观望到行动的关键节点。',
    'https://image.pollinations.ai/prompt/anime%20character%20growth%20storyboard%20illustration?width=1600&height=900&seed=4103&nologo=true',
    2,
    3,
    'PUBLISHED',
    0,
    1,
    0,
    1,
    '角色成长线观察',
    '记录一个角色从沉默到表达的完整成长节奏与名场面。',
    '2026-03-24 20:30:00',
    70,
    2950,
    214,
    1,
    '2026-03-22 09:30:00',
    '2026-03-24 20:30:00',
    0
  ),
  (
    4,
    '一张配乐单就够了：适合深夜循环播放的 8 首动画氛围曲',
    'midnight-anime-soundtracks',
    '当你只想在夜里开着台灯慢慢听，这 8 首动画配乐能把整个房间都变得柔软下来。',
    'https://image.pollinations.ai/prompt/anime%20music%20room%20night%20illustration?width=1600&height=900&seed=4104&nologo=true',
    1,
    4,
    'PUBLISHED',
    0,
    0,
    0,
    1,
    '深夜循环播放的动画氛围曲',
    '适合夜里循环播放的动画配乐精选，覆盖片头、片尾和背景音乐。',
    '2026-03-22 22:00:00',
    60,
    2234,
    168,
    1,
    '2026-03-20 11:00:00',
    '2026-03-22 22:00:00',
    0
  ),
  (
    5,
    '第一次来云屿应该怎么逛：分类、专题、标签三条主线一次看懂',
    'how-to-use-yunyu',
    '如果你是第一次进入这个站点，这篇会告诉你最省心的阅读路径与内容组织方式。',
    'https://image.pollinations.ai/prompt/anime%20guide%20board%20illustration%20library?width=1600&height=900&seed=4105&nologo=true',
    1,
    5,
    'PUBLISHED',
    0,
    1,
    0,
    1,
    '第一次来云屿应该怎么逛',
    '帮助新用户快速看懂云屿的分类、标签和专题体系。',
    '2026-03-21 19:20:00',
    50,
    1890,
    141,
    4,
    '2026-03-19 15:20:00',
    '2026-03-21 19:20:00',
    0
  ),
  (
    6,
    '海边列车、黄昏天台和放学路口：适合做壁纸的 9 个动画场景',
    'nine-anime-wallpaper-scenes',
    '这一篇不做剧情分析，只挑那些一眼就想保存下来的场景镜头。',
    'https://image.pollinations.ai/prompt/anime%20seaside%20train%20sunset%20illustration?width=1600&height=900&seed=4106&nologo=true',
    3,
    2,
    'PUBLISHED',
    0,
    1,
    0,
    1,
    '适合做壁纸的动画场景',
    '海边列车、黄昏天台和放学路口等适合收藏做壁纸的高颜值场景。',
    '2026-03-20 20:10:00',
    40,
    4110,
    356,
    2,
    '2026-03-18 08:40:00',
    '2026-03-20 20:10:00',
    0
  );

INSERT INTO `post_content` (
  `id`, `post_id`, `content_markdown`, `content_html`, `content_plain_text`,
  `content_toc_json`, `video_url`, `reading_time`, `created_time`, `updated_time`
)
VALUES
  (
    1,
    1,
    '# 2026 春季新番先看哪 5 部

## 这次筛选只看三个维度

- 情绪密度是否稳定
- 作画完成度是否足够
- 连载节奏会不会造成追更压力

## 推荐顺序

### 1. 先看最稳的一部
这类作品通常第一集就能给出明确的基调，后续口碑波动也相对较小。

### 2. 再补情绪更重的作品
如果你偏好细腻关系线，可以在第二梯队开始补充。

## 适合什么样的观众

> 如果你本季时间不多，先从完成度更稳的作品开始，会更容易建立追番惯性。
',
    '<h1 id="spring-2026-watch">2026 春季新番先看哪 5 部</h1><p>这篇不追求一次把所有作品都讲完，而是优先给出最适合入场的顺序。</p><h2 id="three-factors">这次筛选只看三个维度</h2><ul><li>情绪密度是否稳定</li><li>作画完成度是否足够</li><li>连载节奏会不会造成追更压力</li></ul><h2 id="watch-order">推荐顺序</h2><h3 id="order-one">1. 先看最稳的一部</h3><p>这类作品通常第一集就能给出明确的基调，后续口碑波动也相对较小。</p><h3 id="order-two">2. 再补情绪更重的作品</h3><p>如果你偏好细腻关系线，可以在第二梯队开始补充。</p><h2 id="for-whom">适合什么样的观众</h2><blockquote><p>如果你本季时间不多，先从完成度更稳的作品开始，会更容易建立追番惯性。</p></blockquote>',
    '这篇不追求一次把所有作品都讲完，而是优先给出最适合入场的顺序。这次筛选只看三个维度：情绪密度是否稳定、作画完成度是否足够、连载节奏会不会造成追更压力。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'spring-2026-watch', 'text', '2026 春季新番先看哪 5 部', 'level', 1),
      JSON_OBJECT('id', 'three-factors', 'text', '这次筛选只看三个维度', 'level', 2),
      JSON_OBJECT('id', 'watch-order', 'text', '推荐顺序', 'level', 2),
      JSON_OBJECT('id', 'for-whom', 'text', '适合什么样的观众', 'level', 2)
    ),
    NULL,
    5,
    '2026-03-25 14:10:00',
    '2026-03-27 20:00:00'
  ),
  (
    2,
    2,
    '# 雨夜电车窗景为什么总能打动人

## 氛围的关键不是下雨
真正有力量的是玻璃反光、城市灯牌和人物留白一起成立。

## 三个高情绪镜头

### 霓虹倒影
车窗上的反光，会让城市像漂浮在角色脸侧。

### 电车报站声
哪怕没有台词，观众也能感受到移动中的孤独感。

### 留白的视线
镜头不需要解释人物在想什么，只需要给她一段安静的看向窗外的时间。
',
    '<h1 id="rainy-city-window">雨夜电车窗景为什么总能打动人</h1><p>真正让人沉浸的，并不是雨本身，而是玻璃反光、城市灯牌和人物留白一起成立。</p><h2 id="key-is-atmosphere">氛围的关键不是下雨</h2><p>下雨只是媒介，真正建立情绪的是镜头内部的层次。</p><h2 id="three-scenes">三个高情绪镜头</h2><h3 id="neon-reflection">霓虹倒影</h3><p>车窗上的反光，会让城市像漂浮在角色脸侧。</p><h3 id="station-voice">电车报站声</h3><p>哪怕没有台词，观众也能感受到移动中的孤独感。</p><h3 id="blank-look">留白的视线</h3><p>镜头不需要解释人物在想什么，只需要给她一段安静的看向窗外的时间。</p>',
    '真正让人沉浸的，并不是雨本身，而是玻璃反光、城市灯牌和人物留白一起成立。下雨只是媒介，真正建立情绪的是镜头内部的层次。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'rainy-city-window', 'text', '雨夜电车窗景为什么总能打动人', 'level', 1),
      JSON_OBJECT('id', 'key-is-atmosphere', 'text', '氛围的关键不是下雨', 'level', 2),
      JSON_OBJECT('id', 'three-scenes', 'text', '三个高情绪镜头', 'level', 2)
    ),
    NULL,
    4,
    '2026-03-24 16:20:00',
    '2026-03-26 21:10:00'
  ),
  (
    3,
    3,
    '# 本季最完整的角色成长线

## 成长不是突然爆发
真正动人的角色变化，往往来自很多个微小决定叠加起来。

## 三个关键节点

1. 第一次明确拒绝
2. 第一次主动表达
3. 第一次为别人做决定
',
    '<h1 id="character-growth-main">本季最完整的角色成长线</h1><p>真正动人的角色变化，往往来自很多个微小决定叠加起来。</p><h2 id="growth-not-sudden">成长不是突然爆发</h2><p>角色最有力量的变化，通常发生在观众差一点忽略的细节里。</p><h2 id="three-turning-points">三个关键节点</h2><ol><li>第一次明确拒绝</li><li>第一次主动表达</li><li>第一次为别人做决定</li></ol>',
    '真正动人的角色变化，往往来自很多个微小决定叠加起来。角色最有力量的变化，通常发生在观众差一点忽略的细节里。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'character-growth-main', 'text', '本季最完整的角色成长线', 'level', 1),
      JSON_OBJECT('id', 'growth-not-sudden', 'text', '成长不是突然爆发', 'level', 2),
      JSON_OBJECT('id', 'three-turning-points', 'text', '三个关键节点', 'level', 2)
    ),
    NULL,
    4,
    '2026-03-22 09:40:00',
    '2026-03-24 20:30:00'
  ),
  (
    4,
    4,
    '# 深夜循环播放的 8 首动画氛围曲

## 为什么夜里更适合听配乐
当信息量变少以后，旋律里那些细小起伏会更容易被感知到。

## 推荐的收听顺序

- 先听片尾曲
- 再听钢琴主题
- 最后补弦乐主旋律
',
    '<h1 id="midnight-playlist">深夜循环播放的 8 首动画氛围曲</h1><p>当信息量变少以后，旋律里那些细小起伏会更容易被感知到。</p><h2 id="why-night">为什么夜里更适合听配乐</h2><p>深夜会放大音色细节，也更适合慢节奏的旋律推进。</p><h2 id="listen-order">推荐的收听顺序</h2><ul><li>先听片尾曲</li><li>再听钢琴主题</li><li>最后补弦乐主旋律</li></ul>',
    '当信息量变少以后，旋律里那些细小起伏会更容易被感知到。深夜会放大音色细节，也更适合慢节奏的旋律推进。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'midnight-playlist', 'text', '深夜循环播放的 8 首动画氛围曲', 'level', 1),
      JSON_OBJECT('id', 'why-night', 'text', '为什么夜里更适合听配乐', 'level', 2),
      JSON_OBJECT('id', 'listen-order', 'text', '推荐的收听顺序', 'level', 2)
    ),
    NULL,
    3,
    '2026-03-20 11:10:00',
    '2026-03-22 22:00:00'
  ),
  (
    5,
    5,
    '# 第一次来云屿应该怎么逛

## 三条最省心的入口

### 看分类
适合先按兴趣看内容方向。

### 看专题
适合一次性阅读同主题内容。

### 看标签
适合快速找到某种氛围或某类镜头。
',
    '<h1 id="how-to-use-yunyu-main">第一次来云屿应该怎么逛</h1><p>如果你是第一次进入这个站点，这篇会告诉你最省心的阅读路径。</p><h2 id="three-entry-points">三条最省心的入口</h2><h3 id="entry-category">看分类</h3><p>适合先按兴趣看内容方向。</p><h3 id="entry-topic">看专题</h3><p>适合一次性阅读同主题内容。</p><h3 id="entry-tag">看标签</h3><p>适合快速找到某种氛围或某类镜头。</p>',
    '如果你是第一次进入这个站点，这篇会告诉你最省心的阅读路径。可以从分类、专题和标签三条主线进入。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'how-to-use-yunyu-main', 'text', '第一次来云屿应该怎么逛', 'level', 1),
      JSON_OBJECT('id', 'three-entry-points', 'text', '三条最省心的入口', 'level', 2)
    ),
    NULL,
    3,
    '2026-03-19 15:30:00',
    '2026-03-21 19:20:00'
  ),
  (
    6,
    6,
    '# 适合做壁纸的 9 个动画场景

## 选择标准

- 构图完整
- 色彩有记忆点
- 单张截图也能成立

## 最推荐的三个关键词

1. 海边列车
2. 黄昏天台
3. 放学路口
',
    '<h1 id="wallpaper-scenes-main">适合做壁纸的 9 个动画场景</h1><p>这一篇不做剧情分析，只挑那些一眼就想保存下来的场景镜头。</p><h2 id="selection-rules">选择标准</h2><ul><li>构图完整</li><li>色彩有记忆点</li><li>单张截图也能成立</li></ul><h2 id="three-keywords">最推荐的三个关键词</h2><ol><li>海边列车</li><li>黄昏天台</li><li>放学路口</li></ol>',
    '这一篇不做剧情分析，只挑那些一眼就想保存下来的场景镜头。选择标准包括构图完整、色彩有记忆点、单张截图也能成立。',
    JSON_ARRAY(
      JSON_OBJECT('id', 'wallpaper-scenes-main', 'text', '适合做壁纸的 9 个动画场景', 'level', 1),
      JSON_OBJECT('id', 'selection-rules', 'text', '选择标准', 'level', 2),
      JSON_OBJECT('id', 'three-keywords', 'text', '最推荐的三个关键词', 'level', 2)
    ),
    NULL,
    3,
    '2026-03-18 08:50:00',
    '2026-03-20 20:10:00'
  );

INSERT INTO `post_tag` (`id`, `post_id`, `tag_id`, `created_time`)
VALUES
  (1, 1, 1, '2026-03-27 20:00:00'),
  (2, 1, 4, '2026-03-27 20:00:00'),
  (3, 1, 8, '2026-03-27 20:00:00'),
  (4, 2, 2, '2026-03-26 21:10:00'),
  (5, 2, 7, '2026-03-26 21:10:00'),
  (6, 3, 6, '2026-03-24 20:30:00'),
  (7, 3, 3, '2026-03-24 20:30:00'),
  (8, 4, 5, '2026-03-22 22:00:00'),
  (9, 4, 1, '2026-03-22 22:00:00'),
  (10, 5, 8, '2026-03-21 19:20:00'),
  (11, 5, 1, '2026-03-21 19:20:00'),
  (12, 6, 7, '2026-03-20 20:10:00'),
  (13, 6, 2, '2026-03-20 20:10:00');

INSERT INTO `topic_post` (`id`, `topic_id`, `post_id`, `sort_order`, `created_time`)
VALUES
  (1, 1, 1, 10, '2026-03-27 20:00:00'),
  (2, 1, 3, 20, '2026-03-24 20:30:00'),
  (3, 2, 2, 10, '2026-03-26 21:10:00'),
  (4, 2, 6, 20, '2026-03-20 20:10:00'),
  (5, 3, 5, 10, '2026-03-21 19:20:00'),
  (6, 3, 1, 20, '2026-03-27 20:00:00');

INSERT INTO `comment` (
  `id`, `post_id`, `user_id`, `reply_comment_id`, `root_id`, `content`, `status`, `ip`,
  `created_time`, `updated_time`, `deleted`
)
VALUES
  (1, 1, 3, NULL, NULL, '这个排序很实用，我这种时间不多的读者终于知道该先追什么了。', 'APPROVED', '127.0.0.1', '2026-03-27 21:10:00', '2026-03-27 21:10:00', 0),
  (2, 1, 2, 1, 1, '后面我会继续补每部作品更细的观看建议。', 'APPROVED', '127.0.0.1', '2026-03-27 21:30:00', '2026-03-27 21:30:00', 0),
  (3, 1, 3, NULL, NULL, '希望下一篇也能加上偏治愈系的推荐。', 'APPROVED', '127.0.0.1', '2026-03-27 22:00:00', '2026-03-27 22:00:00', 0),
  (4, 2, 1, NULL, NULL, '这篇把电车窗景为什么动人说得很准，尤其是“留白的视线”这一点。', 'APPROVED', '127.0.0.1', '2026-03-26 22:10:00', '2026-03-26 22:10:00', 0),
  (5, 2, 3, NULL, NULL, '想看你继续写天台夜景的镜头分析。', 'APPROVED', '127.0.0.1', '2026-03-26 22:28:00', '2026-03-26 22:28:00', 0),
  (6, 3, 1, NULL, NULL, '角色成长这篇很稳，适合放到专题里。', 'APPROVED', '127.0.0.1', '2026-03-24 22:05:00', '2026-03-24 22:05:00', 0),
  (7, 4, 3, NULL, NULL, '求这 8 首曲子的完整歌单。', 'APPROVED', '127.0.0.1', '2026-03-22 22:30:00', '2026-03-22 22:30:00', 0),
  (8, 5, 2, NULL, NULL, '新用户引导这篇很有必要，首页也可以直接挂入口。', 'APPROVED', '127.0.0.1', '2026-03-21 20:00:00', '2026-03-21 20:00:00', 0),
  (9, 5, 3, 8, 8, '同意，尤其是分类和专题一起看会很顺。', 'APPROVED', '127.0.0.1', '2026-03-21 20:22:00', '2026-03-21 20:22:00', 0),
  (10, 5, 1, NULL, NULL, '后续前台第一版我会把这篇放进明显入口。', 'APPROVED', '127.0.0.1', '2026-03-21 20:40:00', '2026-03-21 20:40:00', 0),
  (11, 6, 1, NULL, NULL, '海边列车这个关键词真的太适合做封面了。', 'APPROVED', '127.0.0.1', '2026-03-20 21:00:00', '2026-03-20 21:00:00', 0),
  (12, 6, 2, NULL, NULL, '希望后面补一个壁纸专题。', 'APPROVED', '127.0.0.1', '2026-03-20 21:12:00', '2026-03-20 21:12:00', 0);

INSERT INTO `friend_link` (
  `id`, `site_name`, `site_url`, `logo_url`, `description`, `contact_name`, `contact_email`,
  `contact_message`, `theme_color`, `sort_order`, `status`, `created_time`, `updated_time`, `deleted`
)
VALUES
  (
    1,
    '风铃图书室',
    'https://library.yunyu-demo.local',
    'https://image.pollinations.ai/prompt/soft%20bookstore%20logo%20hand-drawn?width=256&height=256&seed=6101&nologo=true',
    '收藏阅读、手账与温柔生活方式的小型内容站。',
    '白井遥',
    'hello@library.yunyu-demo.local',
    '想和云屿做长期互访，把温柔内容放进同一片风里。',
    '#7CC6B8',
    10,
    'APPROVED',
    '2026-03-18 10:00:00',
    '2026-03-28 10:00:00',
    0
  ),
  (
    2,
    '星潮车站',
    'https://station.yunyu-demo.local',
    'https://image.pollinations.ai/prompt/sky%20train%20station%20icon%20illustration?width=256&height=256&seed=6102&nologo=true',
    '记录夜色、电车和城市边缘情绪的视觉博客。',
    '神谷澈',
    'station@yunyu-demo.local',
    '希望交换友链，一起做城市夜景主题的内容联动。',
    '#5FA8FF',
    20,
    'APPROVED',
    '2026-03-19 11:20:00',
    '2026-03-28 11:20:00',
    0
  ),
  (
    3,
    '浮云手札',
    'https://notes.yunyu-demo.local',
    'https://image.pollinations.ai/prompt/cloud%20notes%20logo%20gentle%20illustration?width=256&height=256&seed=6103&nologo=true',
    '写作、插画与独立创作随笔的小岛日志。',
    '绪方凛',
    'note@yunyu-demo.local',
    '我们的内容气质很接近，想申请加入友链列表。',
    '#F2A65A',
    30,
    'APPROVED',
    '2026-03-20 09:40:00',
    '2026-03-29 09:40:00',
    0
  ),
  (
    4,
    '晨雾邮局',
    'https://post.yunyu-demo.local',
    '',
    '分享明信片、旅行见闻与手绘地图的小站。',
    '早川芽衣',
    'post@yunyu-demo.local',
    '刚上线不久，想申请和云屿交换友链。',
    '#F4BF75',
    0,
    'PENDING',
    '2026-03-30 14:10:00',
    '2026-03-30 14:10:00',
    0
  );

INSERT INTO `site_config` (`config_key`, `config_name`, `config_json`, `remark`)
VALUES
  (
    'site.base',
    '站点基础配置',
    JSON_OBJECT(
      'siteName', '云屿',
      'siteSubTitle', '在二次元场景与情绪里漫游的内容站',
      'logoUrl', 'https://image.pollinations.ai/prompt/anime%20island%20logo%20minimal?width=256&height=256&seed=5101&nologo=true',
      'faviconUrl', 'https://image.pollinations.ai/prompt/anime%20island%20icon%20minimal?width=128&height=128&seed=5102&nologo=true',
      'footerText', '云屿 Yunyu · 用更柔软的方式整理动画内容'
    ),
    '动漫内容站演示数据'
  ),
  (
    'site.seo',
    '站点SEO配置',
    JSON_OBJECT(
      'defaultTitle', '云屿 Yunyu - 动漫内容站演示',
      'defaultDescription', '聚焦新番观察、场景美学、角色成长与专题化内容组织的动漫风格内容站。'
    ),
    '动漫内容站演示数据'
  ),
  (
    'site.theme',
    '站点主题配置',
    JSON_OBJECT(
      'primaryColor', '#38bdf8',
      'secondaryColor', '#f97316'
    ),
    '动漫内容站演示数据'
  ),
  (
    'homepage_config',
    '首页配置',
    JSON_OBJECT(
      'heroEnabled', true,
      'heroLayout', 'brand',
      'heroBackgroundMode', 'soft-glow',
      'heroEyebrow', 'Yunyu / 云屿',
      'heroTitle', '把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站',
      'heroSubtitle', '记录动画、技术、审美和长期创作主题，用更轻的首页组织内容入口。',
      'heroPrimaryButtonText', '查看文章',
      'heroPrimaryButtonLink', '/posts',
      'heroSecondaryButtonText', '进入专题',
      'heroSecondaryButtonLink', '/topics',
      'heroVisualPostId', NULL,
      'heroVisualClickable', true,
      'heroKeywords', JSON_ARRAY('写作', '动画', '技术', '长期主义'),
      'showHeroKeywords', true,
      'showHeroStats', true,
      'heroStats', JSON_ARRAY(
        JSON_OBJECT('label', '文章', 'value', '48'),
        JSON_OBJECT('label', '专题', 'value', '12'),
        JSON_OBJECT('label', '分类', 'value', '8')
      ),
      'showFeaturedSection', true,
      'featuredSectionTitle', '主打内容',
      'showLatestSection', true,
      'latestSectionTitle', '最新文章',
      'showCategorySection', true,
      'categorySectionTitle', '分类',
      'showTopicSection', true,
      'topicSectionTitle', '专题'
    ),
    '首页无封面首屏演示数据'
  ),
  (
    'site.feature',
    '站点功能开关',
    JSON_OBJECT(
      'allowRegister', true,
      'allowComment', true,
      'enableSearch', true,
      'enableSubscribe', false
    ),
    '动漫内容站演示数据'
  )
ON DUPLICATE KEY UPDATE
  `config_name` = VALUES(`config_name`),
  `config_json` = VALUES(`config_json`),
  `remark` = VALUES(`remark`),
  `updated_time` = CURRENT_TIMESTAMP;

SET FOREIGN_KEY_CHECKS = 1;
