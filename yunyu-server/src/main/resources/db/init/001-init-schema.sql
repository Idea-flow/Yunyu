-- 云屿 / Yunyu
-- 功能：当前阶段 MySQL 8 数据库初始化脚本，用于创建核心业务表与基础站点配置。
-- 作用：作为第一阶段建库基线，可用于首次安装流程执行。

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` VARCHAR(128) NOT NULL COMMENT '邮箱，登录账号',
  `user_name` VARCHAR(64) NOT NULL COMMENT '用户名称/展示名称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `password` VARCHAR(255) NOT NULL COMMENT '密码原值',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色：SUPER_ADMIN / USER',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE / DISABLED',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email_deleted` (`email`, `deleted`),
  KEY `idx_user_status_deleted_created_time` (`status`, `deleted`, `created_time`, `id`),
  KEY `idx_user_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `user_auth` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '认证ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `auth_type` VARCHAR(32) NOT NULL COMMENT '认证类型：LOCAL / GITHUB / GOOGLE / LINUX_DO',
  `auth_identity` VARCHAR(191) NOT NULL COMMENT '认证唯一标识，本地可存邮箱，第三方存平台唯一用户ID',
  `auth_name` VARCHAR(100) DEFAULT NULL COMMENT '第三方平台用户名或展示名',
  `auth_email` VARCHAR(128) DEFAULT NULL COMMENT '第三方平台邮箱',
  `email_verified` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证：0否 1是',
  `raw_user_info` JSON DEFAULT NULL COMMENT '第三方原始用户信息JSON',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_auth_type_identity` (`auth_type`, `auth_identity`),
  UNIQUE KEY `uk_user_auth_user_id_type` (`user_id`, `auth_type`),
  KEY `idx_user_auth_email` (`auth_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户认证方式表';

CREATE TABLE IF NOT EXISTS `category` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `slug` VARCHAR(120) NOT NULL COMMENT '分类唯一标识',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分类描述',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '分类封面图URL',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE / DISABLED',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name_deleted` (`name`, `deleted`),
  UNIQUE KEY `uk_category_slug_deleted` (`slug`, `deleted`),
  KEY `idx_category_status_deleted_sort_order` (`status`, `deleted`, `sort_order`, `id`),
  KEY `idx_category_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类表';

CREATE TABLE IF NOT EXISTS `tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `slug` VARCHAR(120) NOT NULL COMMENT '标签唯一标识',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '标签描述',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE / DISABLED',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name_deleted` (`name`, `deleted`),
  UNIQUE KEY `uk_tag_slug_deleted` (`slug`, `deleted`),
  KEY `idx_tag_status_deleted_created_time` (`status`, `deleted`, `created_time`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签表';

CREATE TABLE IF NOT EXISTS `topic` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '专题ID',
  `name` VARCHAR(100) NOT NULL COMMENT '专题名称',
  `slug` VARCHAR(120) NOT NULL COMMENT '专题唯一标识',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '专题简介',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '专题封面图URL',
  `status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE / DISABLED',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_name_deleted` (`name`, `deleted`),
  UNIQUE KEY `uk_topic_slug_deleted` (`slug`, `deleted`),
  KEY `idx_topic_status_deleted_sort_order` (`status`, `deleted`, `sort_order`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专题表';

CREATE TABLE IF NOT EXISTS `post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `slug` VARCHAR(220) NOT NULL COMMENT '文章唯一标识',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
  `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '分类ID',
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT / PUBLISHED / OFFLINE',
  `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶：0否 1是',
  `is_recommend` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否推荐：0否 1是',
  `has_video` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否有视频：0否 1是',
  `allow_comment` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许评论：0否 1是',
  `seo_title` VARCHAR(255) DEFAULT NULL COMMENT 'SEO标题',
  `seo_description` VARCHAR(500) DEFAULT NULL COMMENT 'SEO描述',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `view_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览量',
  `like_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_slug_deleted` (`slug`, `deleted`),
  KEY `idx_post_publish_list` (`status`, `deleted`, `published_at` DESC, `id` DESC),
  KEY `idx_post_recommend_list` (`status`, `deleted`, `is_recommend`, `published_at` DESC, `id` DESC),
  KEY `idx_post_category_publish` (`category_id`, `status`, `deleted`, `published_at` DESC, `id` DESC),
  KEY `idx_post_user_manage` (`user_id`, `status`, `deleted`, `updated_time` DESC, `id` DESC),
  KEY `idx_post_top_publish` (`status`, `deleted`, `is_top`, `sort_order`, `published_at` DESC, `id` DESC),
  KEY `idx_post_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章主表';

CREATE TABLE IF NOT EXISTS `post_content` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
  `content_markdown` LONGTEXT NOT NULL COMMENT 'Markdown正文',
  `content_html` LONGTEXT NOT NULL COMMENT 'HTML正文',
  `content_plain_text` LONGTEXT DEFAULT NULL COMMENT '纯文本内容',
  `content_toc_json` JSON DEFAULT NULL COMMENT '目录JSON',
  `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频直链地址',
  `reading_time` INT NOT NULL DEFAULT 0 COMMENT '预计阅读时长，单位分钟',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_content_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章内容表';

CREATE TABLE IF NOT EXISTS `post_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章标签关联ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag_post_id_tag_id` (`post_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';

CREATE TABLE IF NOT EXISTS `topic_post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '专题文章关联ID',
  `topic_id` BIGINT UNSIGNED NOT NULL COMMENT '专题ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_post_topic_id_post_id` (`topic_id`, `post_id`),
  KEY `idx_topic_post_topic_sort` (`topic_id`, `sort_order`, `id`),
  KEY `idx_topic_post_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专题文章关联表';

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '文章ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论用户ID',
  `reply_comment_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '直接回复的评论ID',
  `root_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '根评论ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING / APPROVED / REJECTED',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '评论IP',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_comment_post_status_root_created_time` (`post_id`, `status`, `deleted`, `root_id`, `created_time`, `id`),
  KEY `idx_comment_user_status_created_time` (`user_id`, `status`, `created_time`, `id`),
  KEY `idx_comment_reply_comment_id` (`reply_comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

CREATE TABLE IF NOT EXISTS `site_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(64) NOT NULL COMMENT '配置键，如 site.base / site.seo / site.theme / site.feature',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_json` JSON NOT NULL COMMENT '配置内容JSON',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_site_config_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点配置表';

INSERT INTO `site_config` (`config_key`, `config_name`, `config_json`, `remark`)
VALUES
  ('site.base', '站点基础配置', JSON_OBJECT('siteName', '', 'siteSubTitle', '', 'logoUrl', '', 'faviconUrl', '', 'footerText', ''), '初始化占位配置'),
  ('site.seo', '站点SEO配置', JSON_OBJECT('defaultTitle', '', 'defaultDescription', '', 'defaultShareImage', ''), '初始化占位配置'),
  ('site.theme', '站点主题配置', JSON_OBJECT('primaryColor', '', 'secondaryColor', '', 'homeStyle', 'default'), '初始化占位配置'),
  ('homepage_config', '首页配置', JSON_OBJECT('heroEnabled', true, 'heroLayout', 'brand', 'heroBackgroundMode', 'gradient-grid', 'heroEyebrow', 'Yunyu / 云屿', 'heroTitle', '把热爱、写作与长期观察，整理成一个可以慢慢逛的内容站', 'heroSubtitle', '记录技术、审美、创作与阅读的个人博客与内容网站', 'heroPrimaryButtonText', '查看文章', 'heroPrimaryButtonLink', '/posts', 'heroSecondaryButtonText', '进入专题', 'heroSecondaryButtonLink', '/topics', 'heroVisualPostId', NULL, 'heroVisualClickable', true, 'heroKeywords', JSON_ARRAY('写作', '技术', '审美', '长期主义'), 'showHeroKeywords', true, 'showHeroStats', true, 'heroStats', JSON_ARRAY(), 'showFeaturedSection', true, 'featuredSectionTitle', '主打内容', 'showLatestSection', true, 'latestSectionTitle', '最新文章', 'showCategorySection', true, 'categorySectionTitle', '分类', 'showTopicSection', true, 'topicSectionTitle', '专题'), '首页无封面首屏配置'),
  ('site.feature', '站点功能开关', JSON_OBJECT('allowRegister', false, 'allowComment', true, 'enableSearch', false, 'enableSubscribe', false), '初始化占位配置')
ON DUPLICATE KEY UPDATE
  `config_name` = VALUES(`config_name`),
  `config_json` = VALUES(`config_json`),
  `remark` = VALUES(`remark`),
  `updated_time` = CURRENT_TIMESTAMP;
