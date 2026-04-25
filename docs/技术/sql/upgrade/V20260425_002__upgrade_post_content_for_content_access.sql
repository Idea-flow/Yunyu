-- 功能：为 post_content 表补齐内容访问控制相关字段。
-- 作用：支持正文访问控制配置与尾部隐藏内容的存储。

ALTER TABLE `post_content`
  ADD COLUMN IF NOT EXISTS `content_access_config_json` JSON DEFAULT NULL COMMENT '统一内容访问控制配置' AFTER `content_toc_json`,
  ADD COLUMN IF NOT EXISTS `tail_hidden_content_markdown` MEDIUMTEXT DEFAULT NULL COMMENT '尾部隐藏内容 Markdown' AFTER `content_access_config_json`,
  ADD COLUMN IF NOT EXISTS `tail_hidden_content_html` MEDIUMTEXT DEFAULT NULL COMMENT '尾部隐藏内容 HTML' AFTER `tail_hidden_content_markdown`;
