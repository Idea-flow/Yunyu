package com.ideaflow.yunyu.module.site.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 后台 S3 配置更新请求类。
 * 作用：承接后台 S3 配置页提交的完整配置集合与当前启用配置标识。
 */
@Data
public class AdminSiteStorageS3ConfigUpdateRequest {

    @Schema(description = "当前启用的 S3 配置键。可不传；不传时后端会自动取 `enabled=true` 的那一项。若传入，必须与唯一启用的配置项一致。", example = "r2-main")
    private String activeProfileKey;

    @Schema(description = "S3 配置列表。保存时至少提供一个配置项，且必须且只能有一个 `enabled=true`。")
    private List<AdminSiteStorageS3ProfileRequest> profiles;
}
