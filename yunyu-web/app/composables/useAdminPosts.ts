import type { AdminPostForm, AdminPostItem, AdminPostListResponse, AdminPostQuery } from '../types/post'

/**
 * 后台文章管理组合式函数。
 * 作用：统一封装后台文章增删改查接口，避免页面中散落接口路径和请求细节。
 */
export function useAdminPosts() {
  const apiClient = useApiClient()

  /**
   * 查询后台文章列表。
   *
   * @param query 查询参数
   * @returns 文章列表响应
   */
  async function listPosts(query: AdminPostQuery = {}) {
    return await apiClient.request<AdminPostListResponse>('/api/admin/posts', {
      method: 'GET',
      query
    })
  }

  /**
   * 查询单篇文章详情。
   *
   * @param postId 文章ID
   * @returns 文章详情
   */
  async function getPost(postId: number) {
    return await apiClient.request<AdminPostItem>(`/api/admin/posts/${postId}`)
  }

  /**
   * 创建文章。
   *
   * @param payload 文章表单
   * @returns 创建后的文章
   */
  async function createPost(payload: AdminPostForm) {
    return await apiClient.request<AdminPostItem>('/api/admin/posts', {
      method: 'POST',
      body: payload
    })
  }

  /**
   * 更新文章。
   *
   * @param postId 文章ID
   * @param payload 文章表单
   * @returns 更新后的文章
   */
  async function updatePost(postId: number, payload: AdminPostForm) {
    return await apiClient.request<AdminPostItem>(`/api/admin/posts/${postId}`, {
      method: 'PUT',
      body: payload
    })
  }

  /**
   * 删除文章。
   *
   * @param postId 文章ID
   */
  async function deletePost(postId: number) {
    await apiClient.request<void>(`/api/admin/posts/${postId}`, {
      method: 'DELETE'
    })
  }

  return {
    listPosts,
    getPost,
    createPost,
    updatePost,
    deletePost
  }
}
