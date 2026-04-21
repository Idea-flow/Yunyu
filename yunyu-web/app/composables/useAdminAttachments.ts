import type {
  AdminAttachmentCompleteRequest,
  AdminAttachmentItem,
  AdminAttachmentListResponse,
  AdminAttachmentPresignRequest,
  AdminAttachmentPresignResponse,
  AdminAttachmentQuery,
  AdminAttachmentUploadResult
} from '../types/attachment'

/**
 * 后台附件组合式函数。
 * 作用：统一封装附件预签名、前端直传、上传回执和删除操作，供后台编辑页复用。
 */
export function useAdminAttachments() {
  const apiClient = useApiClient()

  /**
   * 申请附件上传预签名。
   *
   * @param payload 预签名请求
   * @returns 预签名响应
   */
  async function presignUpload(payload: AdminAttachmentPresignRequest) {
    return await apiClient.request<AdminAttachmentPresignResponse>('/api/admin/attachments/presign', {
      method: 'POST',
      body: payload
    })
  }

  /**
   * 提交附件上传完成回执。
   *
   * @param payload 上传完成请求
   * @returns 附件条目
   */
  async function completeUpload(payload: AdminAttachmentCompleteRequest) {
    return await apiClient.request<AdminAttachmentItem>('/api/admin/attachments/complete', {
      method: 'POST',
      body: payload
    })
  }

  /**
   * 删除附件。
   *
   * @param attachmentId 附件ID
   */
  async function deleteAttachment(attachmentId: number) {
    await apiClient.request<void>(`/api/admin/attachments/${attachmentId}`, {
      method: 'DELETE'
    })
  }

  /**
   * 查询附件列表。
   *
   * @param query 查询参数
   * @returns 附件分页列表
   */
  async function listAttachments(query: AdminAttachmentQuery = {}) {
    return await apiClient.request<AdminAttachmentListResponse>('/api/admin/attachments', {
      method: 'GET',
      query
    })
  }

  /**
   * 执行一次完整的前端直传。
   *
   * @param file 文件对象
   * @returns 上传结果
   */
  async function uploadFile(file: File): Promise<AdminAttachmentUploadResult> {
    const contentType = file.type || 'application/octet-stream'
    const sha256 = await computeSha256(file)
    const presign = await presignUpload({
      fileName: file.name,
      contentType,
      sizeBytes: file.size
    })

    const headers = new Headers()
    Object.entries(presign.headers || {}).forEach(([key, value]) => {
      if (value) {
        headers.set(key, value)
      }
    })

    const uploadResponse = await fetch(presign.uploadUrl, {
      method: presign.httpMethod || 'PUT',
      headers,
      body: file
    })

    if (!uploadResponse.ok) {
      throw new Error(`上传到对象存储失败（HTTP ${uploadResponse.status}）`)
    }

    const etag = uploadResponse.headers.get('etag') || undefined
    const attachment = await completeUpload({
      fileName: file.name,
      contentType,
      sizeBytes: file.size,
      sha256,
      bucket: presign.bucket,
      objectKey: presign.objectKey,
      storageConfigKey: presign.storageConfigKey,
      etag
    })

    return {
      attachment,
      sha256
    }
  }

  /**
   * 计算文件 SHA-256。
   *
   * @param file 文件对象
   * @returns 小写十六进制哈希
   */
  async function computeSha256(file: File) {
    const buffer = await file.arrayBuffer()
    const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
    const hashArray = Array.from(new Uint8Array(hashBuffer))
    return hashArray.map(byte => byte.toString(16).padStart(2, '0')).join('')
  }

  return {
    presignUpload,
    completeUpload,
    listAttachments,
    deleteAttachment,
    uploadFile
  }
}
