import type { AdminSiteConfigForm } from '../types/admin-site-config'

/**
 * 后台站点配置组合式函数。
 * 作用：统一封装后台站点设置页的读取与保存接口，避免页面中散落请求路径和认证细节。
 */
export function useAdminSiteConfig() {
  const apiClient = useApiClient()

  /**
   * 查询站点配置。
   *
   * @returns 站点配置
   */
  async function getSiteConfig() {
    return await apiClient.request<AdminSiteConfigForm>('/api/admin/site-config')
  }

  /**
   * 更新站点配置。
   *
   * @param payload 站点配置表单
   * @returns 更新后的站点配置
   */
  async function updateSiteConfig(payload: AdminSiteConfigForm) {
    return await apiClient.request<AdminSiteConfigForm>('/api/admin/site-config', {
      method: 'PUT',
      body: payload
    })
  }

  return {
    getSiteConfig,
    updateSiteConfig
  }
}
