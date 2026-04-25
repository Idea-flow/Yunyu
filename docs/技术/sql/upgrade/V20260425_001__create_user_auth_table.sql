-- 功能：新增用户认证方式表。
-- 作用：支持本地/第三方登录身份绑定，补齐 auth 模块落库结构。

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
