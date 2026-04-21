package com.ideaflow.yunyu.module.attachment.vo;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * 后台附件预签名响应类。
 * 作用：向前端返回直传所需的预签名地址、请求头和对象访问信息。
 */
@Data
public class AdminAttachmentPresignResponse {

    private String uploadUrl;
    private String httpMethod;
    private Map<String, String> headers;
    private String storageConfigKey;
    private String bucket;
    private String objectKey;
    private String accessUrl;
    private LocalDateTime expireAt;
}
