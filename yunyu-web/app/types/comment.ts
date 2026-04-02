/**
 * 前台评论作者类型。
 * 作用：统一描述评论区中作者展示所需的最小用户信息。
 */
export interface SiteCommentAuthor {
  userId: number
  userName: string
  avatarUrl: string
}

/**
 * 前台评论条目类型。
 * 作用：统一描述文章评论区的楼层结构、回复关系与作者信息。
 */
export interface SiteCommentItem {
  id: number
  postId: number
  replyCommentId: number | null
  rootId: number | null
  content: string
  replyToUserName: string | null
  createdTime: string
  author: SiteCommentAuthor
  replies: SiteCommentItem[]
}

/**
 * 前台评论列表响应类型。
 * 作用：承接文章评论区的根评论分页结果与当前文章可见评论总数。
 */
export interface SiteCommentListResponse {
  list: SiteCommentItem[]
  total: number
  commentCount: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 前台评论查询参数类型。
 * 作用：统一描述文章评论区分页请求参数。
 */
export interface SiteCommentQuery {
  pageNo?: number
  pageSize?: number
}

/**
 * 前台评论发布请求类型。
 * 作用：统一描述前台发布评论时提交的正文与回复目标。
 */
export interface SiteCommentCreateRequest {
  content: string
  replyCommentId?: number | null
}

/**
 * 前台评论发布响应类型。
 * 作用：承接评论发布后的审核状态与是否立即展示结果。
 */
export interface SiteCommentCreateResponse {
  commentId: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  visible: boolean
  message: string
}

/**
 * 后台评论状态类型。
 * 作用：统一约束评论管理页使用的审核状态枚举值。
 */
export type AdminCommentStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/**
 * 后台评论条目类型。
 * 作用：统一描述评论管理页表格展示的评论、文章、作者与审核信息。
 */
export interface AdminCommentItem {
  id: number
  postId: number
  postTitle: string
  postSlug: string
  userId: number
  userName: string
  userEmail: string
  replyCommentId: number | null
  rootId: number | null
  replyToUserName: string | null
  content: string
  status: AdminCommentStatus
  ip: string
  createdTime: string
  updatedTime: string
}

/**
 * 后台评论列表响应类型。
 * 作用：承接评论管理页的分页结果。
 */
export interface AdminCommentListResponse {
  list: AdminCommentItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台评论查询参数类型。
 * 作用：统一描述评论管理页的筛选条件与分页参数。
 */
export interface AdminCommentQuery {
  keyword?: string
  status?: AdminCommentStatus
  postId?: number
  userId?: number
  pageNo?: number
  pageSize?: number
}
