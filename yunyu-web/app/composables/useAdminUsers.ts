import type { AdminUserForm, AdminUserItem, AdminUserListResponse, AdminUserQuery } from '../types/user'

/**
 * 后台用户管理组合式函数。
 * 作用：统一封装后台用户增删改查接口，避免页面中直接拼接接口路径和请求方式。
 */
export function useAdminUsers() {
  const apiClient = useApiClient()

  /**
   * 查询后台用户列表。
   *
   * @param query 查询参数
   * @returns 用户列表结果
   */
  async function listUsers(query: AdminUserQuery = {}) {
    return await apiClient.request<AdminUserListResponse>('/api/admin/users', {
      method: 'GET',
      query
    })
  }

  /**
   * 查询单个用户详情。
   *
   * @param userId 用户ID
   * @returns 用户详情
   */
  async function getUser(userId: number) {
    return await apiClient.request<AdminUserItem>(`/api/admin/users/${userId}`)
  }

  /**
   * 创建用户。
   *
   * @param payload 创建表单
   * @returns 创建后的用户
   */
  async function createUser(payload: AdminUserForm) {
    return await apiClient.request<AdminUserItem>('/api/admin/users', {
      method: 'POST',
      body: payload
    })
  }

  /**
   * 更新用户。
   *
   * @param userId 用户ID
   * @param payload 更新表单
   * @returns 更新后的用户
   */
  async function updateUser(userId: number, payload: AdminUserForm) {
    return await apiClient.request<AdminUserItem>(`/api/admin/users/${userId}`, {
      method: 'PUT',
      body: payload
    })
  }

  /**
   * 删除用户。
   *
   * @param userId 用户ID
   */
  async function deleteUser(userId: number) {
    await apiClient.request<void>(`/api/admin/users/${userId}`, {
      method: 'DELETE'
    })
  }

  return {
    listUsers,
    getUser,
    createUser,
    updateUser,
    deleteUser
  }
}
