package com.ideaflow.yunyu.module.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 后台附件秒传检查请求类。
 * 作用：承接前端在预签名前提交的文件哈希，用于判断附件是否已存在并可直接复用。
 */
@Data
public class AdminAttachmentExistsCheckRequest {

    @Schema(description = "文件内容的 SHA-256 摘要。用于检查后台是否已存在同一文件。", example = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a")
    @NotBlank(message = "sha256 不能为空")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "sha256 格式不正确")
    private String sha256;
}
