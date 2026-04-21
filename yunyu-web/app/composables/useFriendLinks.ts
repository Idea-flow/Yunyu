import type {
  SiteFriendLinkApplyRequest,
  SiteFriendLinkApplyResponse,
  SiteFriendLinkItem
} from '../types/friend-link'

/**
 * 前台友链接口组合式函数。
 * 作用：统一封装友链展示与友链申请相关公开接口，避免页面层直接拼接后端地址。
 */
export function useFriendLinks() {
  const apiClient = useApiClient()

  /**
   * 查询前台友链列表。
   *
   * @returns 已通过审核的友链列表
   */
  async function listFriendLinks() {
    return await apiClient.request<SiteFriendLinkItem[]>('/api/site/friend-links')
  }

  /**
   * 提交友链申请。
   *
   * @param payload 友链申请数据
   * @returns 提交结果
   */
  async function applyFriendLink(payload: SiteFriendLinkApplyRequest) {
    return await apiClient.request<SiteFriendLinkApplyResponse>('/api/site/friend-links/applications', {
      method: 'POST',
      body: payload,
      withAuth: false
    })
  }

  return {
    listFriendLinks,
    applyFriendLink
  }
}
