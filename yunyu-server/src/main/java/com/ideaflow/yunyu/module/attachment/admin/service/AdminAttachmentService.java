package com.ideaflow.yunyu.module.attachment.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.attachment.admin.dto.AdminAttachmentCompleteRequest;
import com.ideaflow.yunyu.module.attachment.admin.dto.AdminAttachmentExistsCheckRequest;
import com.ideaflow.yunyu.module.attachment.admin.dto.AdminAttachmentPresignRequest;
import com.ideaflow.yunyu.module.attachment.admin.dto.AdminAttachmentQueryRequest;
import com.ideaflow.yunyu.module.attachment.entity.AttachmentFileEntity;
import com.ideaflow.yunyu.module.attachment.mapper.AttachmentFileMapper;
import com.ideaflow.yunyu.module.attachment.admin.vo.AdminAttachmentItemResponse;
import com.ideaflow.yunyu.module.attachment.admin.vo.AdminAttachmentListResponse;
import com.ideaflow.yunyu.module.attachment.admin.vo.AdminAttachmentExistsCheckResponse;
import com.ideaflow.yunyu.module.attachment.admin.vo.AdminAttachmentPresignResponse;
import com.ideaflow.yunyu.module.storage.model.SiteStorageS3Profile;
import com.ideaflow.yunyu.module.storage.admin.service.SiteStorageS3ConfigService;
import com.ideaflow.yunyu.security.SecurityUtils;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 后台附件管理服务类。
 * 作用：处理附件预签名、上传完成落库、分页查询和远程删除对象等核心业务逻辑。
 */
@Service
public class AdminAttachmentService {

    private static final DateTimeFormatter OBJECT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final AttachmentFileMapper attachmentFileMapper;
    private final SiteStorageS3ConfigService siteStorageS3ConfigService;

    /**
     * 创建后台附件管理服务。
     *
     * @param attachmentFileMapper 附件 Mapper
     * @param siteStorageS3ConfigService 站点 S3 配置服务
     */
    public AdminAttachmentService(AttachmentFileMapper attachmentFileMapper,
                                  SiteStorageS3ConfigService siteStorageS3ConfigService) {
        this.attachmentFileMapper = attachmentFileMapper;
        this.siteStorageS3ConfigService = siteStorageS3ConfigService;
    }

    /**
     * 生成附件上传预签名。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    public AdminAttachmentPresignResponse createPresign(AdminAttachmentPresignRequest request) {
        SiteStorageS3Profile activeProfile = siteStorageS3ConfigService.getActiveProfileOrThrow();
        validateUploadRequest(request, activeProfile);

        String objectKey = buildObjectKey(request.getFileName());
        String accessUrl = buildAccessUrl(activeProfile, objectKey);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(activeProfile.getBucket())
                .key(objectKey)
                .contentType(request.getContentType().trim())
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest;
        try (S3Presigner presigner = createPresigner(activeProfile)) {
            presignedPutObjectRequest = presigner.presignPutObject(PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(activeProfile.getPresignExpireSeconds()))
                    .putObjectRequest(putObjectRequest)
                    .build());
        } catch (SdkException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "生成上传签名失败，请检查 S3 配置");
        }

        AdminAttachmentPresignResponse response = new AdminAttachmentPresignResponse();
        response.setUploadUrl(presignedPutObjectRequest.url().toString());
        response.setHttpMethod("PUT");
        response.setHeaders(Map.of("Content-Type", request.getContentType().trim()));
        response.setStorageConfigKey(activeProfile.getProfileKey());
        response.setBucket(activeProfile.getBucket());
        response.setObjectKey(objectKey);
        response.setAccessUrl(accessUrl);
        response.setExpireAt(LocalDateTime.ofInstant(
                presignedPutObjectRequest.expiration(),
                ZoneId.systemDefault()
        ));
        return response;
    }

    /**
     * 检查附件是否已存在。
     * 作用：在预签名前按文件哈希执行秒传判断，命中时直接复用已有附件。
     *
     * @param request 秒传检查请求
     * @return 秒传检查结果
     */
    public AdminAttachmentExistsCheckResponse checkExists(AdminAttachmentExistsCheckRequest request) {
        AttachmentFileEntity existingEntity = findExistingAttachmentBySha256(request.getSha256());
        AdminAttachmentExistsCheckResponse response = new AdminAttachmentExistsCheckResponse();
        if (existingEntity == null) {
            response.setExists(false);
            response.setAttachment(null);
            return response;
        }

        response.setExists(true);
        response.setAttachment(toItemResponse(existingEntity));
        return response;
    }

    /**
     * 完成附件上传回执并落库。
     *
     * @param request 上传完成请求
     * @return 附件条目响应
     */
    @Transactional(rollbackFor = Exception.class)
    public AdminAttachmentItemResponse completeUpload(AdminAttachmentCompleteRequest request) {
        String normalizedSha256 = normalizeSha256(request.getSha256());

        AttachmentFileEntity existingEntity = attachmentFileMapper.selectOne(new LambdaQueryWrapper<AttachmentFileEntity>()
                .eq(AttachmentFileEntity::getSha256, normalizedSha256)
                .eq(AttachmentFileEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (existingEntity != null) {
            return toItemResponse(existingEntity);
        }

        SiteStorageS3Profile profile = siteStorageS3ConfigService.getProfileByKeyOrThrow(request.getStorageConfigKey());
        validateCompleteRequest(request, profile);

        AttachmentFileEntity entity = new AttachmentFileEntity();
        entity.setFileName(request.getFileName().trim());
        entity.setFileExt(resolveFileExt(request.getFileName()));
        entity.setMimeType(request.getContentType().trim().toLowerCase(Locale.ROOT));
        entity.setSizeBytes(request.getSizeBytes());
        entity.setSha256(normalizedSha256);
        entity.setStorageProvider("S3");
        entity.setStorageConfigKey(profile.getProfileKey());
        entity.setBucket(request.getBucket().trim());
        entity.setObjectKey(request.getObjectKey().trim());
        entity.setAccessUrl(buildAccessUrl(profile, request.getObjectKey().trim()));
        entity.setEtag(normalizeNullableText(request.getEtag()));
        entity.setUploaderUserId(SecurityUtils.getCurrentUser().getUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setDeleted(0);
        attachmentFileMapper.insert(entity);
        return toItemResponse(entity);
    }

    /**
     * 查询附件列表。
     *
     * @param request 查询请求
     * @return 附件分页列表
     */
    public AdminAttachmentListResponse listAttachments(AdminAttachmentQueryRequest request) {
        long pageNo = normalizePageNo(request.getPageNo());
        long pageSize = normalizePageSize(request.getPageSize());

        LambdaQueryWrapper<AttachmentFileEntity> queryWrapper = new LambdaQueryWrapper<AttachmentFileEntity>()
                .eq(AttachmentFileEntity::getDeleted, 0)
                .orderByDesc(AttachmentFileEntity::getCreatedTime)
                .orderByDesc(AttachmentFileEntity::getId);

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String keyword = request.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(AttachmentFileEntity::getFileName, keyword)
                    .or()
                    .like(AttachmentFileEntity::getObjectKey, keyword)
                    .or()
                    .like(AttachmentFileEntity::getSha256, keyword));
        }

        if (request.getMimeType() != null && !request.getMimeType().isBlank()) {
            String mimeType = request.getMimeType().trim().toLowerCase(Locale.ROOT);
            if (mimeType.endsWith("/")) {
                queryWrapper.likeRight(AttachmentFileEntity::getMimeType, mimeType);
            } else {
                queryWrapper.eq(AttachmentFileEntity::getMimeType, mimeType);
            }
        }

        Page<AttachmentFileEntity> page = attachmentFileMapper.selectPage(new Page<>(pageNo, pageSize), queryWrapper);
        Long totalPages = page.getPages() <= 0 ? 1 : page.getPages();

        return new AdminAttachmentListResponse(
                page.getRecords().stream().map(this::toItemResponse).toList(),
                page.getTotal(),
                pageNo,
                pageSize,
                totalPages
        );
    }

    /**
     * 删除附件。
     * 作用：先删除远程 S3 对象，成功后再执行本地软删除。
     *
     * @param attachmentId 附件ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long attachmentId) {
        AttachmentFileEntity entity = findAttachmentOrThrow(attachmentId);
        SiteStorageS3Profile profile = siteStorageS3ConfigService.getProfileByKeyOrThrow(entity.getStorageConfigKey());
        deleteRemoteObject(entity, profile);

        attachmentFileMapper.update(null, new LambdaUpdateWrapper<AttachmentFileEntity>()
                .eq(AttachmentFileEntity::getId, attachmentId)
                .eq(AttachmentFileEntity::getDeleted, 0)
                .set(AttachmentFileEntity::getDeleted, 1)
                .set(AttachmentFileEntity::getUpdatedTime, LocalDateTime.now()));
    }

    /**
     * 删除远程对象。
     *
     * @param entity 附件实体
     * @param profile S3 配置
     */
    private void deleteRemoteObject(AttachmentFileEntity entity, SiteStorageS3Profile profile) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(entity.getBucket())
                .key(entity.getObjectKey())
                .build();

        try (S3Client client = createS3Client(profile)) {
            client.deleteObject(request);
        } catch (NoSuchKeyException ignored) {
            // 对象不存在时按幂等成功处理。
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return;
            }
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "删除远程 S3 文件失败：" + exception.awsErrorDetails().errorMessage());
        } catch (SdkException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "删除远程 S3 文件失败，请检查存储配置");
        }
    }

    /**
     * 校验预签名请求。
     *
     * @param request 预签名请求
     * @param profile S3 配置
     */
    private void validateUploadRequest(AdminAttachmentPresignRequest request, SiteStorageS3Profile profile) {
        String contentType = request.getContentType() == null ? "" : request.getContentType().trim().toLowerCase(Locale.ROOT);
        if (profile.getAllowedContentTypes() == null || profile.getAllowedContentTypes().stream().noneMatch(item -> item.equalsIgnoreCase(contentType))) {
            throw new BizException(ResultCode.BAD_REQUEST, "当前文件类型不在允许范围内：" + contentType);
        }

        long maxFileSizeBytes = (long) profile.getMaxFileSizeMb() * 1024L * 1024L;
        if (request.getSizeBytes() > maxFileSizeBytes) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件大小超过限制，最大允许 " + profile.getMaxFileSizeMb() + "MB");
        }
    }

    /**
     * 校验上传完成请求。
     *
     * @param request 上传完成请求
     * @param profile S3 配置
     */
    private void validateCompleteRequest(AdminAttachmentCompleteRequest request, SiteStorageS3Profile profile) {
        if (!profile.getBucket().equals(request.getBucket().trim())) {
            throw new BizException(ResultCode.BAD_REQUEST, "bucket 与当前 S3 配置不匹配");
        }
        if (!request.getObjectKey().startsWith("attachments/")) {
            throw new BizException(ResultCode.BAD_REQUEST, "objectKey 不合法");
        }

        String contentType = request.getContentType() == null ? "" : request.getContentType().trim().toLowerCase(Locale.ROOT);
        if (profile.getAllowedContentTypes() == null || profile.getAllowedContentTypes().stream().noneMatch(item -> item.equalsIgnoreCase(contentType))) {
            throw new BizException(ResultCode.BAD_REQUEST, "当前文件类型不在允许范围内：" + contentType);
        }

        long maxFileSizeBytes = (long) profile.getMaxFileSizeMb() * 1024L * 1024L;
        if (request.getSizeBytes() > maxFileSizeBytes) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件大小超过限制，最大允许 " + profile.getMaxFileSizeMb() + "MB");
        }
    }

    /**
     * 按 sha256 查找已存在附件。
     * 作用：用于上传前秒传判断，命中后直接复用已有附件信息并跳过重复上传。
     *
     * @param sha256 文件哈希
     * @return 已存在附件；未命中返回 null
     */
    private AttachmentFileEntity findExistingAttachmentBySha256(String sha256) {
        if (sha256 == null || sha256.isBlank()) {
            return null;
        }

        String normalizedSha256 = normalizeSha256(sha256);
        return attachmentFileMapper.selectOne(new LambdaQueryWrapper<AttachmentFileEntity>()
                .eq(AttachmentFileEntity::getSha256, normalizedSha256)
                .eq(AttachmentFileEntity::getDeleted, 0)
                .last("LIMIT 1"));
    }

    /**
     * 查询附件并校验存在性。
     *
     * @param attachmentId 附件ID
     * @return 附件实体
     */
    private AttachmentFileEntity findAttachmentOrThrow(Long attachmentId) {
        if (attachmentId == null || attachmentId <= 0) {
            throw new BizException(ResultCode.BAD_REQUEST, "附件 ID 不合法");
        }

        AttachmentFileEntity entity = attachmentFileMapper.selectOne(new LambdaQueryWrapper<AttachmentFileEntity>()
                .eq(AttachmentFileEntity::getId, attachmentId)
                .eq(AttachmentFileEntity::getDeleted, 0)
                .last("LIMIT 1"));
        if (entity == null) {
            throw new BizException(ResultCode.NOT_FOUND, "附件不存在");
        }
        return entity;
    }

    /**
     * 创建 S3 预签名客户端。
     *
     * @param profile S3 配置
     * @return S3 预签名客户端
     */
    private S3Presigner createPresigner(SiteStorageS3Profile profile) {
        return S3Presigner.builder()
                .region(Region.of(profile.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey())
                ))
                .endpointOverride(URI.create(profile.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(Boolean.TRUE.equals(profile.getPathStyleAccess()))
                        .build())
                .build();
    }

    /**
     * 创建 S3 客户端。
     *
     * @param profile S3 配置
     * @return S3 客户端
     */
    private S3Client createS3Client(SiteStorageS3Profile profile) {
        return S3Client.builder()
                .region(Region.of(profile.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(profile.getAccessKey(), profile.getSecretKey())
                ))
                .endpointOverride(URI.create(profile.getEndpoint()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(Boolean.TRUE.equals(profile.getPathStyleAccess()))
                        .build())
                .build();
    }

    /**
     * 构建对象键。
     *
     * @param fileName 文件名
     * @return 对象键
     */
    private String buildObjectKey(String fileName) {
        String extension = resolveFileExt(fileName);
        String datePath = LocalDate.now().format(OBJECT_DATE_FORMATTER);
        if (extension.isBlank()) {
            return "attachments/" + datePath + "/" + UUID.randomUUID();
        }
        return "attachments/" + datePath + "/" + UUID.randomUUID() + "." + extension;
    }

    /**
     * 构建访问地址。
     *
     * @param profile S3 配置
     * @param objectKey 对象键
     * @return 访问地址
     */
    private String buildAccessUrl(SiteStorageS3Profile profile, String objectKey) {
        String objectPath = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        String publicBaseUrl = normalizeNullableText(profile.getPublicBaseUrl());
        if (!publicBaseUrl.isBlank()) {
            String normalizedBaseUrl = publicBaseUrl.endsWith("/")
                    ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                    : publicBaseUrl;
            return normalizedBaseUrl + "/" + objectPath;
        }

        String endpoint = profile.getEndpoint().trim();
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalizedEndpoint + "/" + profile.getBucket() + "/" + objectPath;
    }

    /**
     * 提取文件后缀。
     *
     * @param fileName 文件名
     * @return 文件后缀
     */
    private String resolveFileExt(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        String extension = fileName.substring(dotIndex + 1).trim().toLowerCase(Locale.ROOT);
        return extension.replaceAll("[^a-z0-9]", "");
    }

    /**
     * 规范化 sha256。
     *
     * @param sha256 原始哈希
     * @return 规范化哈希
     */
    private String normalizeSha256(String sha256) {
        if (sha256 == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "sha256 不能为空");
        }
        String normalized = sha256.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[a-f0-9]{64}$")) {
            throw new BizException(ResultCode.BAD_REQUEST, "sha256 格式不正确");
        }
        return normalized;
    }

    /**
     * 规范化可空文本。
     *
     * @param value 原始值
     * @return 规范化结果
     */
    private String normalizeNullableText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 规范化页码。
     *
     * @param pageNo 原始页码
     * @return 规范化页码
     */
    private long normalizePageNo(Integer pageNo) {
        if (pageNo == null || pageNo < 1) {
            return 1;
        }
        return pageNo;
    }

    /**
     * 规范化分页大小。
     *
     * @param pageSize 原始分页大小
     * @return 规范化分页大小
     */
    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 50);
    }

    /**
     * 转换附件条目响应。
     *
     * @param entity 附件实体
     * @return 附件条目响应
     */
    private AdminAttachmentItemResponse toItemResponse(AttachmentFileEntity entity) {
        AdminAttachmentItemResponse response = new AdminAttachmentItemResponse();
        response.setId(entity.getId());
        response.setFileName(entity.getFileName());
        response.setFileExt(entity.getFileExt());
        response.setMimeType(entity.getMimeType());
        response.setSizeBytes(entity.getSizeBytes());
        response.setSha256(entity.getSha256());
        response.setStorageProvider(entity.getStorageProvider());
        response.setStorageConfigKey(entity.getStorageConfigKey());
        response.setBucket(entity.getBucket());
        response.setObjectKey(entity.getObjectKey());
        response.setAccessUrl(entity.getAccessUrl());
        response.setEtag(entity.getEtag());
        response.setUploaderUserId(entity.getUploaderUserId());
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());
        return response;
    }
}
