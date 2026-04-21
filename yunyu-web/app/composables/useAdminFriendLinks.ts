import type {
  AdminFriendLinkForm,
  AdminFriendLinkItem,
  AdminFriendLinkListResponse,
  AdminFriendLinkQuery,
  AdminFriendLinkStatus
} from '../types/friend-link'
import { toAdminFriendLinkItem } from '../types/friend-link'

/**
 * 后台友链接口条目类型。
 * 作用：匹配后端友链管理接口返回的单条数据结构。
 */
interface AdminFriendLinkApiItem {
  id: number
  siteName: string
  siteUrl: string
  logoUrl: string | null
  description: string | null
  contactName: string | null
  contactEmail: string | null
  contactMessage: string | null
  themeColor: string | null
  sortOrder: number
  status: AdminFriendLinkStatus
  createdTime: string
  updatedTime: string
}

/**
 * 后台友链接口列表响应类型。
 * 作用：匹配后端友链管理列表接口的分页结构。
 */
interface AdminFriendLinkApiListResponse {
  list: AdminFriendLinkApiItem[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

/**
 * 后台友链组合式函数。
 * 作用：统一封装友链管理页的真实 CRUD 与状态审核接口。
 */
export function useAdminFriendLinks() {
  const apiClient = useApiClient()
  const basePath = '/api/admin/friend-links'

  /**
   * 查询后台友链列表。
   *
   * @param query 查询参数
   * @returns 列表结果
   */
  async function listFriendLinks(query: AdminFriendLinkQuery = {}): Promise<AdminFriendLinkListResponse> {
    const response = await apiClient.request<AdminFriendLinkApiListResponse>(basePath, {
      method: 'GET',
      query
    })

    return {
      ...response,
      list: response.list.map(item => toAdminFriendLinkItem(item))
    }
  }

  /**
   * 查询单个友链详情。
   *
   * @param itemId 友链ID
   * @returns 友链详情
   */
  async function getFriendLink(itemId: number): Promise<AdminFriendLinkItem> {
    const response = await apiClient.request<AdminFriendLinkApiItem>(`${basePath}/${itemId}`)
    return toAdminFriendLinkItem(response)
  }

  /**
   * 创建友链。
   *
   * @param payload 表单数据
   * @returns 新建后的友链
   */
  async function createFriendLink(payload: AdminFriendLinkForm): Promise<AdminFriendLinkItem> {
    const response = await apiClient.request<AdminFriendLinkApiItem>(basePath, {
      method: 'POST',
      body: payload
    })

    return toAdminFriendLinkItem(response)
  }

  /**
   * 更新友链。
   *
   * @param itemId 友链ID
   * @param payload 表单数据
   * @returns 更新后的友链
   */
  async function updateFriendLink(itemId: number, payload: AdminFriendLinkForm): Promise<AdminFriendLinkItem> {
    const response = await apiClient.request<AdminFriendLinkApiItem>(`${basePath}/${itemId}`, {
      method: 'PUT',
      body: payload
    })

    return toAdminFriendLinkItem(response)
  }

  /**
   * 更新友链状态。
   *
   * @param itemId 友链ID
   * @param status 目标状态
   * @returns 更新后的友链
   */
  async function updateFriendLinkStatus(itemId: number, status: AdminFriendLinkStatus): Promise<AdminFriendLinkItem> {
    const response = await apiClient.request<AdminFriendLinkApiItem>(`${basePath}/${itemId}/status`, {
      method: 'PUT',
      body: { status }
    })

    return toAdminFriendLinkItem(response)
  }

  /**
   * 删除友链。
   *
   * @param itemId 友链ID
   */
  async function deleteFriendLink(itemId: number) {
    await apiClient.request<void>(`${basePath}/${itemId}`, {
      method: 'DELETE'
    })
  }

  return {
    listFriendLinks,
    getFriendLink,
    createFriendLink,
    updateFriendLink,
    updateFriendLinkStatus,
    deleteFriendLink
  }
}
