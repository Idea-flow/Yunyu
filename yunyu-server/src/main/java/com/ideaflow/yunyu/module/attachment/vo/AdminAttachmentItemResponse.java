package com.ideaflow.yunyu.module.attachment.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 后台附件条目响应类。
 * 作用：向后台附件管理和上传回执返回单个附件记录信息。
 */
@Data
public class AdminAttachmentItemResponse {

    private Long id;
    private String fileName;
    private String fileExt;
    private String mimeType;
    private Long sizeBytes;
    private String sha256;
    private String storageProvider;
    private String storageConfigKey;
    private String bucket;
    private String objectKey;
    private String accessUrl;
    private String etag;
    private Long uploaderUserId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
