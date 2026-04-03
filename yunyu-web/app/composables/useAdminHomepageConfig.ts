import type { AdminHomepageConfigForm } from '../types/admin-site-config'

/**
 * 后台首页配置组合式函数。
 * 作用：统一封装后台首页首屏与首页模块配置的读取和保存接口。
 */
export function useAdminHomepageConfig() {
  const apiClient = useApiClient()

  /**
   * 查询首页配置。
   *
   * @returns 首页配置
   */
  async function getHomepageConfig() {
    return await apiClient.request<AdminHomepageConfigForm>('/api/admin/site/homepage-config')
  }

  /**
   * 更新首页配置。
   *
   * @param payload 首页配置表单
   * @returns 更新后的首页配置
   */
  async function updateHomepageConfig(payload: AdminHomepageConfigForm) {
    return await apiClient.request<AdminHomepageConfigForm>('/api/admin/site/homepage-config', {
      method: 'PUT',
      body: payload
    })
  }

  return {
    getHomepageConfig,
    updateHomepageConfig
  }
}
