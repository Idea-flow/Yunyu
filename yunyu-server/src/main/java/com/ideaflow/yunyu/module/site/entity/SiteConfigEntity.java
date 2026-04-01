package com.ideaflow.yunyu.module.site.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 站点配置实体类。
 * 作用：映射 `site_config` 表，为前台站点信息读取和后台系统配置扩展提供统一持久化对象。
 */
@Data
@TableName("site_config")
public class SiteConfigEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configName;
    private String configJson;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
