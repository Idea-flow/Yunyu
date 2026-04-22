/**
 * 后台附件预签名请求类型。
 * 作用：描述前端向后端申请预签名时提交的文件元信息。
 */
export interface AdminAttachmentPresignRequest {
  fileName: string
  contentType: string
  sizeBytes: number
}

/**
 * 后台附件预签名响应类型。
 * 作用：描述前端直传所需的签名地址、请求头和对象信息。
 */
export interface AdminAttachmentPresignResponse {
  uploadUrl: string
  httpMethod: 'PUT'
  headers: Record<string, string>
  storageConfigKey: string
  bucket: string
  objectKey: string
  accessUrl: string
  expireAt: string
}

/**
 * 后台附件秒传检查请求类型。
 * 作用：描述前端在申请预签名前提交的哈希检查参数。
 */
export interface AdminAttachmentExistsCheckRequest {
  sha256: string
}

/**
 * 后台附件秒传检查响应类型。
 * 作用：描述哈希是否命中已有附件及命中详情。
 */
export interface AdminAttachmentExistsCheckResponse {
  exists: boolean
  attachment: AdminAttachmentItem | null
}

/**
 * 后台附件上传完成请求类型。
 * 作用：描述前端完成直传后回执所需的对象信息和哈希字段。
 */
export interface AdminAttachmentCompleteRequest {
  fileName: string
  contentType: string
  sizeBytes: number
  sha256: string
  bucket: string
  objectKey: string
  storageConfigKey: string
  etag?: string
}

/**
 * 后台附件条目类型。
 * 作用：统一描述附件列表和上传回执的附件元数据结构。
 */
export interface AdminAttachmentItem {
  id: number
  fileName: string
  fileExt: string | null
  mimeType: string
  sizeBytes: number
  sha256: string
  storageProvider: string
  storageConfigKey: string
  bucket: string
  objectKey: string
  accessUrl: string
  etag: string | null
  uploaderUserId: number
  createdTime: string
  updatedTime: string
}

/**
 * 后台附件查询参数类型。
 * 作用：统一描述附件列表接口支持的筛选和分页字段。
 */
export interface AdminAttachmentQuery {
  keyword?: string
  mimeType?: string
  pageNo?: number
  pageSize?: number
}

/**
 * 后台附件列表响应类型。
 * 作用：统一描述后台附件列表接口的分页返回结构。
 */
export interface AdminAttachmentListResponse {
  list: AdminAttachmentItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 附件直传结果类型。
 * 作用：统一描述一次前端直传完成后返回给业务页面的结果对象。
 */
export interface AdminAttachmentUploadResult {
  attachment: AdminAttachmentItem
  sha256: string
}
