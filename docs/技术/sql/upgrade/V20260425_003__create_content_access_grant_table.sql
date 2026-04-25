-- 功能：新增内容访问授权缓存表。
-- 作用：记录用户或访客在文章/尾部隐藏内容上的授权结果与过期时间。

CREATE TABLE IF NOT EXISTS `content_access_grant` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `scope_type` VARCHAR(32) NOT NULL COMMENT '授权范围类型：ARTICLE / TAIL_HIDDEN',
  `scope_id` BIGINT UNSIGNED NOT NULL COMMENT '授权范围ID，通常是文章ID',
  `rule_type` VARCHAR(64) NOT NULL COMMENT '规则类型：LOGIN / ACCESS_CODE / WECHAT_ACCESS_CODE',
  `grant_target_type` VARCHAR(32) NOT NULL COMMENT '授权主体类型：USER / VISITOR',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
  `visitor_id_hash` VARCHAR(128) DEFAULT NULL COMMENT '访客标识哈希值',
  `granted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `expire_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_scope_type_scope_id_rule_type` (`scope_type`, `scope_id`, `rule_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_visitor_id_hash` (`visitor_id_hash`),
  KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容访问授权缓存表';
