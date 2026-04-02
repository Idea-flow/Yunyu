import type {
  AdminCommentItem,
  AdminCommentListResponse,
  AdminCommentQuery,
  AdminCommentStatus
} from '../types/comment'

/**
 * 格式化后台时间字符串。
 * 作用：将后端返回的评论时间统一转换为后台更易读的中文日期时间格式。
 *
 * @param value 原始时间值
 * @returns 格式化后的时间文案
 */
function formatAdminCommentTime(value: string | null | undefined) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

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
    createdTime: formatAdminCommentTime(item.createdTime),
    updatedTime: formatAdminCommentTime(item.updatedTime)
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
    updateCommentStatus,
    deleteComment
  }
}
