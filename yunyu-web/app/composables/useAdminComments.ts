import { formatChineseDateTime } from '~/utils/date'
import type {
  AdminCommentItem,
  AdminCommentListResponse,
  AdminCommentQuery,
  AdminCommentThreadGroup,
  AdminCommentThreadGroupListResponse,
  AdminCommentThreadReplyItem,
  AdminCommentThreadRootItem,
  AdminCommentStatus
} from '../types/comment'

/**
 * 转换后台评论条目。
 * 作用：清洗后端可空字段并统一格式化时间，降低页面层模板判断复杂度。
 *
 * @param item 后端评论条目
 * @returns 前端可直接使用的评论条目
 */
function toAdminCommentItem(item: AdminCommentItem): AdminCommentItem {
  return {
    ...item,
    postTitle: item.postTitle || '未知文章',
    postSlug: item.postSlug || '',
    userName: item.userName || '未知用户',
    userEmail: item.userEmail || '',
    replyToUserName: item.replyToUserName || null,
    ip: item.ip || '',
    createdTime: formatChineseDateTime(item.createdTime, '-'),
    updatedTime: formatChineseDateTime(item.updatedTime, '-')
  }
}

/**
 * 转换后台评论回复流条目。
 * 作用：统一格式化回复项时间字段，并清洗可空字段，便于树形审核页直接渲染。
 *
 * @param item 后端回复流条目
 * @returns 前端可直接使用的回复流条目
 */
function toAdminCommentThreadReplyItem(item: AdminCommentThreadReplyItem): AdminCommentThreadReplyItem {
  return {
    ...item,
    replyToUserName: item.replyToUserName || null,
    userName: item.userName || '未知用户',
    userEmail: item.userEmail || '',
    ip: item.ip || '',
    createdTime: formatChineseDateTime(item.createdTime, '-'),
    updatedTime: formatChineseDateTime(item.updatedTime, '-')
  }
}

/**
 * 转换后台评论主评论条目。
 * 作用：统一格式化主评论时间字段，并递归清洗其下回复流数据。
 *
 * @param item 后端主评论条目
 * @returns 前端可直接使用的主评论条目
 */
function toAdminCommentThreadRootItem(item: AdminCommentThreadRootItem): AdminCommentThreadRootItem {
  return {
    ...item,
    userName: item.userName || '未知用户',
    userEmail: item.userEmail || '',
    ip: item.ip || '',
    createdTime: formatChineseDateTime(item.createdTime, '-'),
    updatedTime: formatChineseDateTime(item.updatedTime, '-'),
    replies: (item.replies || []).map(reply => toAdminCommentThreadReplyItem(reply))
  }
}

/**
 * 转换后台评论文章分组条目。
 * 作用：统一格式化文章分组时间字段，并递归清洗评论树数据。
 *
 * @param item 后端文章分组条目
 * @returns 前端可直接使用的文章分组条目
 */
function toAdminCommentThreadGroup(item: AdminCommentThreadGroup): AdminCommentThreadGroup {
  return {
    ...item,
    postTitle: item.postTitle || '未知文章',
    postSlug: item.postSlug || '',
    latestCommentTime: item.latestCommentTime ? formatChineseDateTime(item.latestCommentTime, '-') : null,
    roots: (item.roots || []).map(root => toAdminCommentThreadRootItem(root))
  }
}

/**
 * 后台评论管理组合式函数。
 * 作用：统一封装评论管理页的列表查询、审核状态更新与删除接口。
 */
export function useAdminComments() {
  const apiClient = useApiClient()

  /**
   * 查询后台评论列表。
   * 作用：按关键词、状态和分页参数获取评论管理页数据。
   *
   * @param query 查询参数
   * @returns 评论分页结果
   */
  async function listComments(query: AdminCommentQuery = {}): Promise<AdminCommentListResponse> {
    const response = await apiClient.request<AdminCommentListResponse>('/api/admin/comments', {
      method: 'GET',
      query
    })

    return {
      ...response,
      list: response.list.map(item => toAdminCommentItem(item))
    }
  }

  /**
   * 查询后台评论树形审核列表。
   * 作用：按文章分组获取后台评论树形审核视图，供新版评论管理页直接渲染。
   *
   * @param query 查询参数
   * @returns 按文章分组的评论审核列表
   */
  async function listCommentThreadGroups(query: AdminCommentQuery = {}): Promise<AdminCommentThreadGroupListResponse> {
    const response = await apiClient.request<AdminCommentThreadGroupListResponse>('/api/admin/comments/thread-groups', {
      method: 'GET',
      query
    })

    return {
      ...response,
      list: response.list.map(item => toAdminCommentThreadGroup(item))
    }
  }

  /**
   * 更新评论审核状态。
   * 作用：供后台评论管理页执行通过、驳回或退回待审操作。
   *
   * @param commentId 评论ID
   * @param status 目标状态
   * @returns 更新后的评论条目
   */
  async function updateCommentStatus(commentId: number, status: AdminCommentStatus) {
    const response = await apiClient.request<AdminCommentItem>(`/api/admin/comments/${commentId}/status`, {
      method: 'PUT',
      body: { status }
    })

    return toAdminCommentItem(response)
  }

  /**
   * 删除评论。
   * 作用：供后台评论管理页执行评论及其回复分支的软删除操作。
   *
   * @param commentId 评论ID
   */
  async function deleteComment(commentId: number) {
    await apiClient.request<void>(`/api/admin/comments/${commentId}`, {
      method: 'DELETE'
    })
  }

  return {
    listComments,
    listCommentThreadGroups,
    updateCommentStatus,
    deleteComment
  }
}
