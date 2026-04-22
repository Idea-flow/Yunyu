package com.ideaflow.yunyu.module.attachment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台附件秒传检查请求类。
 * 作用：承接前端在预签名前提交的文件哈希，用于判断附件是否已存在并可直接复用。
 */
@Data
public class AdminAttachmentExistsCheckRequest {

    @NotBlank(message = "sha256 不能为空")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "sha256 格式不正确")
    private String sha256;
}

