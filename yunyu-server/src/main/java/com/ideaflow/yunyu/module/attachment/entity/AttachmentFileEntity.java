package com.ideaflow.yunyu.module.attachment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 附件文件实体类。
 * 作用：映射 attachment_file 表，承载附件元数据、存储定位信息和上传人信息。
 */
@Data
@TableName("attachment_file")
public class AttachmentFileEntity {

    @TableId(type = IdType.AUTO)
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
    private Integer deleted;
}
