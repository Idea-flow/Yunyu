-- 功能：新增友链表。
-- 作用：支持友链申请、审核、排序与上下线管理。

CREATE TABLE IF NOT EXISTS `friend_link` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '友链ID',
  `site_name` VARCHAR(100) NOT NULL COMMENT '站点名称',
  `site_url` VARCHAR(255) NOT NULL COMMENT '站点地址',
  `logo_url` VARCHAR(255) DEFAULT NULL COMMENT '站点图标地址',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '站点简介',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人名称',
  `contact_email` VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
  `contact_message` VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
  `theme_color` VARCHAR(7) DEFAULT NULL COMMENT '卡片主题色',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING / APPROVED / REJECTED / OFFLINE',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_friend_link_site_name_deleted` (`site_name`, `deleted`),
  UNIQUE KEY `uk_friend_link_site_url_deleted` (`site_url`, `deleted`),
  KEY `idx_friend_link_status_deleted_sort_order` (`status`, `deleted`, `sort_order`, `id`),
  KEY `idx_friend_link_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='友链表';
