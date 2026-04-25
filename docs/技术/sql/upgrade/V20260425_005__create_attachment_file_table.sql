-- 功能：新增附件文件表。
-- 作用：支持附件前端直传、哈希去重与对象存储定位。

CREATE TABLE IF NOT EXISTS `attachment_file` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_ext` VARCHAR(32) DEFAULT NULL COMMENT '文件后缀',
  `mime_type` VARCHAR(128) NOT NULL COMMENT '文件MIME类型',
  `size_bytes` BIGINT UNSIGNED NOT NULL COMMENT '文件大小（字节）',
  `sha256` CHAR(64) NOT NULL COMMENT '文件SHA-256（小写十六进制）',
  `storage_provider` VARCHAR(32) NOT NULL DEFAULT 'S3' COMMENT '存储提供方',
  `storage_config_key` VARCHAR(64) NOT NULL COMMENT '命中的S3配置键',
  `bucket` VARCHAR(128) NOT NULL COMMENT '桶名',
  `object_key` VARCHAR(255) NOT NULL COMMENT '对象键',
  `access_url` VARCHAR(500) NOT NULL COMMENT '访问地址',
  `etag` VARCHAR(128) DEFAULT NULL COMMENT '对象ETag',
  `uploader_user_id` BIGINT UNSIGNED NOT NULL COMMENT '上传人用户ID',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attachment_sha256_deleted` (`sha256`, `deleted`),
  UNIQUE KEY `uk_attachment_bucket_object_key_deleted` (`bucket`, `object_key`, `deleted`),
  KEY `idx_attachment_uploader_created` (`uploader_user_id`, `created_time`, `id`),
  KEY `idx_attachment_mime_created` (`mime_type`, `created_time`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='附件文件表';
