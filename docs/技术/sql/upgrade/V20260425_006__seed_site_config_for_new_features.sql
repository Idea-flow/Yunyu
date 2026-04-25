-- 功能：补齐新功能所需站点配置初始化数据。
-- 作用：确保内容访问控制、首页配置和 S3 存储配置在老环境升级后有可用默认值。

INSERT INTO `site_config` (`config_key`, `config_name`, `config_json`, `remark`)
VALUES
  ('site.content-access', '站点内容访问配置', JSON_OBJECT('wechatAccessCodeEnabled', false, 'wechatAccessCode', '', 'wechatAccessCodeHint', '关注公众号后输入访问验证码', 'wechatQrCodeUrl', ''), '初始化站点内容访问配置'),
  ('storage.s3.profiles', '站点 S3 存储配置', JSON_OBJECT('activeProfileKey', '', 'profiles', JSON_ARRAY()), '初始化 S3 多环境配置'),
  ('homepage_config', '首页配置', JSON_OBJECT('heroEnabled', true, 'heroLayout', 'brand', 'heroBackgroundMode', 'gradient-grid', 'heroEyebrow', 'Yunyu / 云屿', 'heroTitle', '把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站', 'heroSubtitle', '记录技术、审美、创作与阅读的个人博客与内容网站', 'heroPrimaryButtonText', '查看文章', 'heroPrimaryButtonLink', '/posts', 'heroSecondaryButtonText', '进入专题', 'heroSecondaryButtonLink', '/topics', 'heroVisualPostId', NULL, 'heroVisualClickable', true, 'heroKeywords', JSON_ARRAY('写作', '技术', '审美', '长期主义'), 'showHeroKeywords', true, 'showHeroStats', true, 'heroStats', JSON_ARRAY(), 'showFeaturedSection', true, 'featuredSectionTitle', '主打内容', 'showLatestSection', true, 'latestSectionTitle', '最新文章', 'showCategorySection', true, 'categorySectionTitle', '分类', 'showTopicSection', true, 'topicSectionTitle', '专题'), '首页无封面首屏配置')
ON DUPLICATE KEY UPDATE
  `config_name` = VALUES(`config_name`),
  `config_json` = VALUES(`config_json`),
  `remark` = VALUES(`remark`),
  `updated_time` = CURRENT_TIMESTAMP;
