import type {
  AdminS3ConfigForm,
  AdminS3ConnectionTestResponse,
  AdminS3ProfileForm,
  AdminSiteConfigForm
} from '../types/admin-site-config'

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

  /**
   * 查询站点 S3 配置。
   *
   * @returns S3 配置
   */
  async function getS3Config() {
    return await apiClient.request<AdminS3ConfigForm>('/api/admin/site/storage/s3')
  }

  /**
   * 更新站点 S3 配置。
   *
   * @param payload S3 配置
   * @returns 更新后的 S3 配置
   */
  async function updateS3Config(payload: AdminS3ConfigForm) {
    return await apiClient.request<AdminS3ConfigForm>('/api/admin/site/storage/s3', {
      method: 'PUT',
      body: payload
    })
  }

  /**
   * 测试 S3 配置连接。
   *
   * @param payload S3 配置项
   * @returns 测试结果
   */
  async function testS3Connection(payload: AdminS3ProfileForm) {
    return await apiClient.request<AdminS3ConnectionTestResponse>('/api/admin/site/storage/s3/test', {
      method: 'POST',
      body: payload
    })
  }

  return {
    getSiteConfig,
    updateSiteConfig,
    getS3Config,
    updateS3Config,
    testS3Connection
  }
}
